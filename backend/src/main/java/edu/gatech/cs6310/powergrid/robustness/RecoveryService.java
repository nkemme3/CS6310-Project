package edu.gatech.cs6310.powergrid.robustness;

import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;

@Component
public class RecoveryService {

    private static final Logger log = LoggerFactory.getLogger(RecoveryService.class);

    private final PowerGridSystem pgs;
    private final CheckpointManager checkpoints;
    private final TransactionJournal journal;

    public RecoveryService(PowerGridSystem pgs, CheckpointManager checkpoints, TransactionJournal journal) {
        this.pgs = pgs;
        this.checkpoints = checkpoints;
        this.journal = journal;
    }

    @PostConstruct
    public void recover() {
        Optional<SystemSnapshot> snap = checkpoints.read();
        if (snap.isPresent()) {
            pgs.restoreFrom(snap.get());
            log.info("Recovered {} companies, {} plants, {} substations, {} transformers, {} customers, {} employees, {} issues, {} rate plans, {} ledger entries, {} users from checkpoint",
                snap.get().companies() == null ? 0 : snap.get().companies().size(),
                snap.get().plants() == null ? 0 : snap.get().plants().size(),
                snap.get().substations() == null ? 0 : snap.get().substations().size(),
                snap.get().transformers() == null ? 0 : snap.get().transformers().size(),
                snap.get().customers() == null ? 0 : snap.get().customers().size(),
                snap.get().employees() == null ? 0 : snap.get().employees().size(),
                snap.get().issues() == null ? 0 : snap.get().issues().size(),
                snap.get().ratePlans() == null ? 0 : snap.get().ratePlans().size(),
                snap.get().ledger() == null ? 0 : snap.get().ledger().size(),
                snap.get().users() == null ? 0 : snap.get().users().size());
        } else {
            log.info("No existing checkpoint; starting with empty state");
        }

        List<JournalCommand> pending = journal.replay();
        if (!pending.isEmpty()) {
            synchronized (pgs.lock()) {
                for (JournalCommand cmd : pending) {
                    try {
                        cmd.apply(pgs);
                    } catch (RuntimeException ex) {
                        log.warn("Skipping journal command during replay: {} ({})", cmd.getClass().getSimpleName(), ex.getMessage());
                    }
                }
            }
            log.info("Replayed {} journal commands", pending.size());
        }
    }
}
