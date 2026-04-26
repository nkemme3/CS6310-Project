package edu.gatech.cs6310.powergrid.robustness;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TransactionJournal {

    private static final Logger log = LoggerFactory.getLogger(TransactionJournal.class);

    private final Path journalFile;
    private final ObjectMapper mapper = CheckpointMapper.build();
    private final Object writeLock = new Object();

    public TransactionJournal(CheckpointManager cpm) {
        this.journalFile = cpm.dataDir().resolve("journal.log");
        try {
            if (!Files.exists(journalFile)) Files.createFile(journalFile);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create journal file: " + journalFile, e);
        }
    }

    public Path journalFile() { return journalFile; }

    public void append(JournalCommand cmd) {
        synchronized (writeLock) {
            try {
                String json = mapper.writeValueAsString(cmd);
                CRC32 crc = new CRC32();
                crc.update(json.getBytes(StandardCharsets.UTF_8));
                String line = json + "\t" + Long.toHexString(crc.getValue()) + "\n";
                Files.writeString(journalFile, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                // fsync
                try (RandomAccessFile raf = new RandomAccessFile(journalFile.toFile(), "rw")) {
                    raf.getFD().sync();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Journal write failed", e);
            }
        }
    }

    public List<JournalCommand> replay() {
        List<JournalCommand> out = new ArrayList<>();
        if (!Files.exists(journalFile)) return out;
        try (BufferedReader reader = Files.newBufferedReader(journalFile, StandardCharsets.UTF_8)) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;
                int tab = line.lastIndexOf('\t');
                if (tab <= 0) {
                    log.warn("Journal line {} has no CRC delimiter, skipping", lineNum);
                    continue;
                }
                String json = line.substring(0, tab);
                String crcHex = line.substring(tab + 1);
                CRC32 crc = new CRC32();
                crc.update(json.getBytes(StandardCharsets.UTF_8));
                long expected;
                try {
                    expected = Long.parseLong(crcHex, 16);
                } catch (NumberFormatException nfe) {
                    log.warn("Journal line {} has invalid CRC hex, skipping", lineNum);
                    continue;
                }
                if (crc.getValue() != expected) {
                    log.warn("Journal line {} CRC mismatch (expected {}, got {}); skipping", lineNum,
                        Long.toHexString(expected), Long.toHexString(crc.getValue()));
                    continue;
                }
                try {
                    JournalCommand cmd = mapper.readValue(json, JournalCommand.class);
                    out.add(cmd);
                } catch (IOException ioe) {
                    log.warn("Journal line {} failed to deserialize: {}", lineNum, ioe.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Failed to read journal at {}: {}", journalFile, e.getMessage());
        }
        return out;
    }

    public void truncate() {
        synchronized (writeLock) {
            try {
                Files.writeString(journalFile, "", StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            } catch (IOException e) {
                log.error("Failed to truncate journal: {}", e.getMessage());
            }
        }
    }
}
