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

import org.medal.graph.impl.AbstractEdge;

/**
 *
 * @author skrymets
 */
public class CallEdge extends AbstractEdge<String, ParticipantData, CallData, ParticipantNode, CallEdge> implements Comparable<CallEdge> {

    public CallEdge(CallGraph graph, ParticipantNode left, ParticipantNode right, Link link) {
        super(graph, left, right, link);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("[").append(getLeft()).append("] ")
                .append("---(")
                .append("E_").append(getId()).append(":").append(getData()).append(")")
                .append((getDirected() == Link.DIRECTED) ? "-->" : "---")
                .append(" [").append(getRight()).append("]")
                .toString();
    }

    @Override
    public int compareTo(CallEdge obj) {
        if (this == obj) {
            return 0;
        }
        if (obj == null) {
            return 1;
        }
        if (getData() == null) {
            if (obj.getData() == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (obj.getData() == null) {
            return 1;
        } else {
            return (int) (getData().getCallTime() - obj.getData().getCallTime());
        }
    }

}
