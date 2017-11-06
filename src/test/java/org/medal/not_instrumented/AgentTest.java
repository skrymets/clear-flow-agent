package org.medal.not_instrumented;

import org.junit.Before;
import org.junit.Test;
import org.medal.clear.flow.agent.SequenceLogger;
import org.medal.test.OuterClass.InnerClass;
import org.medal.test.TestClient;
import org.medal.test.TestServer;
import org.medal.test.ValueProvider;

public class AgentTest {
    
    public AgentTest() {
    }

    @Before
    public void resetCaches() {
        //SequenceLogger.reset();
    }
    
    //@Test
    public void testPremain() {
        
        TestClient client = new TestClient();
        TestServer server = new TestServer();
        
        client.clientMethod(server);               
        
        String report = SequenceLogger.getReport();
        System.out.println(report);
    }
    
    @Test
    public void inheritanceTest_1() {
       
        TestServer server = new TestServer();
        server.serverMethod((new ValueProvider().getValue()));
        
        String report = SequenceLogger.getReport();
        System.out.println(report);
    }
    
}
