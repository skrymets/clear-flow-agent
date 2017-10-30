/*
 * Copyright 2017 skrymets.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.medal.clear.flow.agent.callgraph;

import org.medal.graph.Edge;
import org.medal.graph.EdgeFactory;
import org.medal.graph.IDProvider;
import org.medal.graph.NodeFactory;
import org.medal.graph.id.NumberIDProvider;
import org.medal.graph.impl.AbstractGraph;

/**
 *
 * @author skrymets
 */
public class CallGraph extends AbstractGraph<String, ParticipantData, CallData, ParticipantNode, CallEdge> {

    private final IDProvider<String> idProvider;
        
    ParticipantNode lastNode;

    public CallGraph() {
        idProvider = new IDProvider<String>() {
            private final NumberIDProvider numberIDProvider = new NumberIDProvider();

            @Override
            public String createId() {
                return Long.toHexString(numberIDProvider.createId());
            }
        };
    }

    @Override
    public ParticipantNode createNode(ParticipantData payload) {
        lastNode = super.createNode(payload);
        return lastNode;
    }
    
    

    @Override
    protected NodeFactory<String, ParticipantData, CallData, ParticipantNode, CallEdge> getNodeFactory() {
        return () -> new ParticipantNode(CallGraph.this);
    }

    @Override
    protected EdgeFactory<String, ParticipantData, CallData, ParticipantNode, CallEdge> getEdgeFactory() {
        return (ParticipantNode left, ParticipantNode right, Edge.Link direction) -> new CallEdge(CallGraph.this, left, right, direction);
    }

    @Override
    protected IDProvider<String> getIdProvider() {
        return idProvider;
    }

    public ParticipantNode getLastNode() {
        return lastNode;
    }

}
