package org.medal.not_instrumented;

import org.junit.Test;
import org.medal.clear.flow.agent.SequenceLogger;
import org.medal.test.TestClient;
import org.medal.test.TestServer;

public class AgentTest {
    
    public AgentTest() {
    }

    @Test
    public void testPremain() {
        
        TestClient client = new TestClient();
        TestServer server = new TestServer();
        
        client.clientMethod(server);               
        
        String report = SequenceLogger.getReport();
        System.out.println(report);
    }
    
}
