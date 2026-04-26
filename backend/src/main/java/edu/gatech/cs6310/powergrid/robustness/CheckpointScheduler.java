package edu.gatech.cs6310.powergrid.robustness;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;

@Component
public class CheckpointScheduler {

    private static final Logger log = LoggerFactory.getLogger(CheckpointScheduler.class);

    private final PowerGridSystem pgs;
    private final CheckpointManager checkpoints;
    private final TransactionJournal journal;

    public CheckpointScheduler(PowerGridSystem pgs, CheckpointManager checkpoints, TransactionJournal journal) {
        this.pgs = pgs;
        this.checkpoints = checkpoints;
        this.journal = journal;
    }

    @Scheduled(
        initialDelayString = "${powergrid.robustness.checkpoint-interval-ms:60000}",
        fixedDelayString   = "${powergrid.robustness.checkpoint-interval-ms:60000}"
    )
    public void checkpointNow() {
        try {
            SystemSnapshot snap = pgs.toSnapshot();
            checkpoints.write(snap);
            journal.truncate();
            log.debug("Checkpoint complete");
        } catch (IOException e) {
            log.error("Checkpoint failed: {}", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected checkpoint failure", e);
        }
    }
}
