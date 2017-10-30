package org.medal.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleCodeFlow {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleCodeFlow.class);

    public SimpleCodeFlow() {
    }

    @Test
    public void doTest() {
        LOG.info("Code flow starts here");
    }

}
