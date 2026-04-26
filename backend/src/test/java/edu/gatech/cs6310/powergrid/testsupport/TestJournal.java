package edu.gatech.cs6310.powergrid.testsupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.gatech.cs6310.powergrid.robustness.CheckpointManager;
import edu.gatech.cs6310.powergrid.robustness.TransactionJournal;

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
