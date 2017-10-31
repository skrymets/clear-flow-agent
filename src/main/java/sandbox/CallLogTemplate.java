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
package sandbox;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.medal.clear.flow.agent.callgraph.CallGraph;
import org.medal.clear.flow.agent.callgraph.ParticipantData;
import org.medal.clear.flow.agent.callgraph.ParticipantNode;

/**
 *
 * @author skrymets
 */
public class CallLogTemplate {

    static ThreadLocal<CallGraph> callGraph = new ThreadLocal<CallGraph>() {
        @Override
        protected CallGraph initialValue() {
            final CallGraph graph = new CallGraph();
            return graph;
        }
    };

    static ThreadLocal<Deque<ParticipantNode>> callStack = new ThreadLocal<Deque<ParticipantNode>>() {
        @Override
        protected Deque<ParticipantNode> initialValue() {
            ConcurrentLinkedDeque<ParticipantNode> deque = new ConcurrentLinkedDeque<>();
            deque.push(callGraph.get().createNode(new ParticipantData()));
            return deque;
        }
    };

    static void logEntry(int instanceHashCode) {
        // ParticipantNode previousStep = callStack.get().element();
        // ParticipantNode thisStep = previousStep.getGraph().createNode();
        // callStack.get().push(thisStep);
        callStack.get().push(callStack.get().element().connectNodeFromRight(callStack.get().element().getGraph().createNode()).getRight());

    }

    static void logExit(int instanceHashCode) {
    }

    static class Client {

        public int clientMethod(Server server) {

            logEntry(((Object) this).hashCode());

            final int serverResponse = server.serverMethod();

            logExit(((Object) this).hashCode());

            return serverResponse;
        }
    }

    static class Server {

        public int serverMethod() {
            
            logEntry(((Object) this).hashCode());

            int result = 42;

            logExit(((Object) this).hashCode());

            return result;

        }

    }

    private final Client proxy;
    private final Server server;

    public CallLogTemplate() {
        proxy = new Client();
        server = new Server();
    }

    public int doWork() {
        for (int i = 0; i < 10; i++) {
            proxy.clientMethod(server);
        }
        int value = proxy.clientMethod(server);

        System.out.println(callGraph.get().toString());

        return value;

    }

    public static void main(String[] args) {
        CallLogTemplate clt = new CallLogTemplate();
        clt.doWork();
    }

}
