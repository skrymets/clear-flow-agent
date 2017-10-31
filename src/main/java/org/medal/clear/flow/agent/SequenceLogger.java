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
package org.medal.clear.flow.agent;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.commons.lang3.ObjectUtils;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import org.medal.clear.flow.agent.callgraph.CallData;
import org.medal.clear.flow.agent.callgraph.CallEdge;
import org.medal.clear.flow.agent.callgraph.CallGraph;
import org.medal.clear.flow.agent.callgraph.ParticipantData;
import org.medal.clear.flow.agent.callgraph.ParticipantNode;
import org.medal.graph.DataObject;
import org.medal.graph.Graph;

/**
 *
 * @author skrymets
 */
public class SequenceLogger {

    protected static final long ENTRY_POINT_CODE = 0L;

    public static ThreadLocal<Map<Long, String>> signatureDictionaries = new ThreadLocal<Map<Long, String>>() {
        @Override
        protected Map<Long, String> initialValue() {
            Map<Long, String> signatureDictionary = new HashMap<>();
            return signatureDictionary;
        }
    };

    public static ThreadLocal<Map<Long, ParticipantNode>> nodesCache = new ThreadLocal<Map<Long, ParticipantNode>>() {
        @Override
        protected Map<Long, ParticipantNode> initialValue() {
            Map<Long, ParticipantNode> map = new HashMap<>();
            return map;
        }
    };

    public static ThreadLocal<Deque<ParticipantNode>> callStack = new ThreadLocal<Deque<ParticipantNode>>() {
        @Override
        protected Deque<ParticipantNode> initialValue() {
            CallGraph graph = new CallGraph();
            ConcurrentLinkedDeque<ParticipantNode> deque = new ConcurrentLinkedDeque<>();
            final ParticipantNode entryPoint = graph.createNode(new ParticipantData(ENTRY_POINT_CODE));
            deque.push(entryPoint);
            return deque;
        }
    };

    public static void logEntry(long instanceCode, long messageCode) {

        ParticipantNode previousNode = callStack.get().element();

        ParticipantNode thisNode;
        final Map<Long, ParticipantNode> existingNodes = nodesCache.get();
        if (existingNodes.containsKey(instanceCode)) {
            thisNode = existingNodes.get(instanceCode);
        } else {
            thisNode = previousNode.getGraph().createNode();
            thisNode.setData(new ParticipantData(instanceCode));
            existingNodes.put(instanceCode, thisNode);
        }

        CallEdge edge = thisNode.connectNodeFromLeft(previousNode);
        edge.setData(new CallData(System.nanoTime(), messageCode));

        callStack.get().push(thisNode);
    }

    public static void logExit() {

        Deque<ParticipantNode> deque = callStack.get();
        if (deque.isEmpty()) {
            return;
        }

        deque.pop();

    }

    public static Map<Long, String> getSignatureDictionaries() {
        return signatureDictionaries.get();
    }

    public static String getReport() {

        final CallData EMPTY = new CallData(0L, 0L);

        CallGraph graph = (CallGraph) callStack.get().element().getGraph();
        Set<CallEdge> calls = new TreeSet<>(graph.getEdges());

        StringBuilder rb = new StringBuilder();
        for (CallEdge call : calls) {

            String messageName = resolveMessageName(firstNonNull(call.getData(), EMPTY));
            rb
                    .append(call.getLeft().getId())
                    .append(" ---> ")
                    .append(messageName)
                    .append("\n");

        }

        return rb.toString();
    }

    private static String resolveMessageName(CallData data) {
        return firstNonNull(signatureDictionaries.get().get(data.getMessageCode()), "");
    }

}
