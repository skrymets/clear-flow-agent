package org.medal.not_instrumented;

import org.junit.Test;
import org.medal.clear.flow.agent.SequenceLogger;
import org.medal.graph.Edge;
import org.medal.graph.Graph;
import org.medal.graph.Node;
import org.medal.graph.impl.GraphImpl;

public class AgentTest {
    
    public AgentTest() {
    }

    @Test
    public void testPremain() {
        
        Graph graph = new GraphImpl();
        Node node1 = graph.createNode();
        Node node2 = graph.createNode();
        Edge edge = node1.connect(node2);
        System.out.println(graph.toString());
        
        
        String report = SequenceLogger.getReport();
        System.out.println(report);
    }
    
}
