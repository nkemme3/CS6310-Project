package edu.gatech.cs6310.powergrid.robustness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Atomic checkpoint I/O. Writes are done to a temp file then renamed over the
 * destination, so readers never see a partially-written file even if the
 * process crashes mid-write.
 */
@Component
public class CheckpointManager {

    private static final Logger log = LoggerFactory.getLogger(CheckpointManager.class);

    private final Path dataDir;
    private final Path checkpointFile;
    private final Path tempFile;
    private final ObjectMapper mapper = CheckpointMapper.build();

    public CheckpointManager(@Value("${powergrid.robustness.data-dir:./data}") String dataDirPath) {
        this.dataDir = Path.of(dataDirPath).toAbsolutePath().normalize();
        this.checkpointFile = dataDir.resolve("checkpoint.json");
        this.tempFile = dataDir.resolve("checkpoint.json.tmp");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create data directory: " + dataDir, e);
        }
    }

    public Path dataDir() { return dataDir; }
    public Path checkpointFile() { return checkpointFile; }

    public synchronized void write(SystemSnapshot snapshot) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), snapshot);
        Files.move(tempFile, checkpointFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        log.debug("Checkpoint written: {} ({} bytes)", checkpointFile, Files.size(checkpointFile));
    }

    public synchronized Optional<SystemSnapshot> read() {
        if (!Files.exists(checkpointFile)) {
            return Optional.empty();
        }
        try {
            SystemSnapshot snap = mapper.readValue(checkpointFile.toFile(), SystemSnapshot.class);
            return Optional.of(snap);
        } catch (IOException e) {
            log.error("Failed to read checkpoint at {}: {}", checkpointFile, e.getMessage());
            return Optional.empty();
        }
    }
}
