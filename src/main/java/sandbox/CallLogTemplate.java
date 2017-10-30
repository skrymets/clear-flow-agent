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

import org.medal.clear.flow.agent.callgraph.CallEdge;
import org.medal.clear.flow.agent.callgraph.CallGraph;
import org.medal.clear.flow.agent.callgraph.ParticipantData;
import org.medal.clear.flow.agent.callgraph.ParticipantNode;

/**
 *
 * @author skrymets
 */
public class CallLogTemplate {

    static ThreadLocal<CallGraph> tl = new ThreadLocal<CallGraph>() {
        @Override
        protected CallGraph initialValue() {
            final CallGraph graph = new CallGraph();
            ParticipantNode threadEntryPoint = graph.createNode(new ParticipantData());
            return graph;
        }
    };

    static void logEntry() {
        CallGraph cg = tl.get();
        ParticipantNode previousStep = cg.getLastNode();
        ParticipantNode thisCall = cg.createNode(); // this become the last step now
        CallEdge callEdge = previousStep.connect(thisCall);
    }

    static void logExit() {
        CallGraph cg = tl.get();
    }

    static class Client {

        public int clientMethod(Server server) {

            return server.serverMethod();

        }
    }

    static class Server {

        public int serverMethod() {

            int result = 42;
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
        return proxy.clientMethod(server);
    }

}
