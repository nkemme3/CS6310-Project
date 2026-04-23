package edu.gatech.cs6310.powergrid.testsupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.gatech.cs6310.powergrid.robustness.CheckpointManager;
import edu.gatech.cs6310.powergrid.robustness.TransactionJournal;

/**
 * Convenience factory for building a {@link TransactionJournal} rooted in a
 * fresh temp directory. Used by service-layer unit tests that don't want to
 * stand up a full Spring context but do need a real journal to satisfy
 * constructor dependencies.
 */
public final class TestJournal {

    private TestJournal() {}

    public static TransactionJournal inTempDir() {
        try {
            Path tmp = Files.createTempDirectory("powergrid-test-");
            tmp.toFile().deleteOnExit();
            CheckpointManager cm = new CheckpointManager(tmp.toString());
            return new TransactionJournal(cm);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create test journal directory", e);
        }
    }
}
