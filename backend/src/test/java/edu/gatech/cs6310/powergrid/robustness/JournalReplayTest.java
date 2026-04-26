package edu.gatech.cs6310.powergrid.robustness;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.service.CompanyService;
import edu.gatech.cs6310.powergrid.service.InfrastructureService;

class JournalReplayTest {

    @Test
    void replayRestoresStateWrittenAfterLastCheckpoint(@TempDir Path tmp) throws IOException {
        PowerGridSystem pgs = new PowerGridSystem(50, 25, 10);
        CheckpointManager cpm = new CheckpointManager(tmp.toString());
        TransactionJournal journal = new TransactionJournal(cpm);
        CompanyService companies = new CompanyService(pgs, journal);
        InfrastructureService infra = new InfrastructureService(pgs, journal);

        companies.addCompany("Atlanta Power Co", "APC", new BigDecimal("0.12"));
        infra.addPlant("APC", "P1", new Location(0, 0), new BigDecimal("1000"), new BigDecimal("0.05"));

        List<JournalCommand> replayed = journal.replay();
        assertThat(replayed).hasSize(2);

        PowerGridSystem target = new PowerGridSystem(50, 25, 10);
        for (JournalCommand cmd : replayed) cmd.apply(target);

        assertThat(target.companies()).containsKey("APC");
        assertThat(target.plants()).containsKey("P1");
        assertThat(target.companies().get("APC").getPlantIds()).contains("P1");
    }

    @Test
    void corruptTailEntryIsSkipped(@TempDir Path tmp) throws IOException {
        PowerGridSystem pgs = new PowerGridSystem(50, 25, 10);
        CheckpointManager cpm = new CheckpointManager(tmp.toString());
        TransactionJournal journal = new TransactionJournal(cpm);
        CompanyService companies = new CompanyService(pgs, journal);

        companies.addCompany("Atlanta Power Co", "APC", new BigDecimal("0.12"));
        // torn write
        Files.writeString(journal.journalFile(), "{\"type\":\"AddCompany\",\"longName\":\"X\"\tDEADBEEF\n",
            StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        List<JournalCommand> replayed = journal.replay();
        assertThat(replayed).hasSize(1);
        assertThat(replayed.get(0)).isInstanceOf(JournalCommand.AddCompanyCmd.class);
    }

    @Test
    void truncateClearsJournal(@TempDir Path tmp) throws IOException {
        PowerGridSystem pgs = new PowerGridSystem(50, 25, 10);
        CheckpointManager cpm = new CheckpointManager(tmp.toString());
        TransactionJournal journal = new TransactionJournal(cpm);
        CompanyService companies = new CompanyService(pgs, journal);

        companies.addCompany("Atlanta Power Co", "APC", new BigDecimal("0.12"));
        assertThat(journal.replay()).isNotEmpty();
        journal.truncate();
        assertThat(journal.replay()).isEmpty();
    }
}
