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
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import org.medal.clear.flow.agent.callgraph.CallData;
import org.medal.clear.flow.agent.callgraph.CallEdge;
import org.medal.clear.flow.agent.callgraph.CallGraph;
import org.medal.clear.flow.agent.callgraph.ParticipantData;
import org.medal.clear.flow.agent.callgraph.ParticipantNode;

/**
 *
 * @author skrymets
 */
public class SequenceLogger {

    protected static final long ENTRY_POINT_CODE = 0L;

    public static ThreadLocal<Map<Long, String>> classDictionaries = new ThreadLocal<Map<Long, String>>() {
        @Override
        protected Map<Long, String> initialValue() {
            Map<Long, String> classDictionary = new HashMap<>();
            return classDictionary;
        }
    };

    public static ThreadLocal<Map<Long, String>> signatureDictionaries = new ThreadLocal<Map<Long, String>>() {
        @Override
        protected Map<Long, String> initialValue() {
            Map<Long, String> signatureDictionary = new HashMap<>();
            return signatureDictionary;
        }
    };

    public static ThreadLocal<CallGraph> callGraphsCache = new ThreadLocal<CallGraph>() {
        @Override
        protected CallGraph initialValue() {
            CallGraph callGraph = new CallGraph();
            return callGraph;
        }
    };

    /**
     * Create a call log record.
     *
     * @param sourceClassId    a class identifier of an object that makes this call
     * @param sourceInstanceId an instance identifier of an object that makes this call
     * @param targetClassId    a class identifier of an object whose instance is being
     *                         called
     * @param targetMethodId   an identifier of an method that is being called
     * @param threadId         an identifier of a thread in which this call has been made
     */
    public static void logMethodCall(
            long sourceClassId,
            long sourceInstanceId,
            long targetClassId,
            long targetMethodId,
            long threadId
    ) {

        CallGraph callGraph = callGraphsCache.get();
        ParticipantNode sourceParticipant = callGraph.findParticipant(sourceClassId);
        if (sourceParticipant == null) {
            sourceParticipant = callGraph.createNode();
            sourceParticipant.setData(new ParticipantData(sourceClassId));
        }

        ParticipantNode targetParticipant = callGraph.findParticipant(targetClassId);
        if (targetParticipant == null) {
            targetParticipant = callGraph.createNode();
            targetParticipant.setData(new ParticipantData(targetClassId));
        }

        CallEdge edge = sourceParticipant.connectNodeFromRight(targetParticipant);
        edge.setData(
                new CallData(
                        System.nanoTime(),
                        sourceInstanceId,
                        targetMethodId,
                        threadId
                ));

    }

    public static void logReturnFromMethod(long classId, long instanceId, long methodId, long threadId) {

    }

    public static Map<Long, String> getSignatureDictionary() {
        return signatureDictionaries.get();
    }

    public static Map<Long, String> getClassDictionary() {
        return classDictionaries.get();
    }
    
    public static CallGraph getCallGraph() {
        return callGraphsCache.get();
    }

    public static String getReport() {

        final CallData EMPTY_CD = new CallData(0L, 0L, 0L, 0L);
        final ParticipantData EMPTY_PD = new ParticipantData(0L);

        CallGraph graph = getCallGraph();
        Set<CallEdge> calls = new TreeSet<>(graph.getEdges());

        StringBuilder rb = new StringBuilder();
        for (CallEdge call : calls) {

            String messageName = resolveMessageName(firstNonNull(call.getData(), EMPTY_CD));

            String leftNodeClass = resolveNodeClassName(firstNonNull(call.getLeft().getData(), EMPTY_PD));
            String rightNodeClass = resolveNodeClassName(firstNonNull(call.getRight().getData(), EMPTY_PD));

            rb
                    .append(leftNodeClass)
                    .append(" --- ")
                    .append(messageName)
                    .append(" --> ")
                    .append(rightNodeClass)
                    .append("\n");

        }

        return rb.toString();
    }

    private static String resolveMessageName(CallData data) {
        return firstNonNull(signatureDictionaries.get().get(data.getMethodId()), "EMPTY_SIGNATURE");
    }

    private static String resolveNodeClassName(ParticipantData data) {
        return firstNonNull(classDictionaries.get().get(data.getClassId()), "EMPTY_CLASS_NAME");
    }

}
