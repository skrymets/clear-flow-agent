package org.medal.not_instrumented;

import org.medal.test.TestServer;
import org.medal.test.TestClient;
import org.junit.Test;
import static org.junit.Assert.*;
import org.medal.clear.flow.agent.SequenceLogger;

public class AgentTest {
    
    public AgentTest() {
    }

    @Test
    public void testPremain() {
        System.out.println("Hello, I'm an agent.");
        assertTrue(true);
        
        TestClient proxy = new TestClient();
        TestServer server = new TestServer();
        proxy.clientMethod(server);
        
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        String report = SequenceLogger.getReport();
        System.out.println(report);
    }
    
}
