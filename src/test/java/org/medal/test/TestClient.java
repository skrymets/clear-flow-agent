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
package org.medal.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author skrymets
 */
public class TestClient {

    private static final Logger LOG = LoggerFactory.getLogger(TestClient.class);

    private final String clientVersion;

    public TestClient() {

        StringBuilder versionString = new StringBuilder();
        versionString.append("1").append(".").append("0");
        clientVersion = versionString.toString();

    }

    public String getClientVersion() {
        return clientVersion;
    }

    public int clientMethod(TestServer server) {

        LOG.info("{} {}",
                LOG.getName(),
                getClientVersion()
        );

        StringBuilder vs = new StringBuilder();
        vs.append(clientVersion).append("_").append("0001");
        String callVersion = vs.toString();

        final int serverResponse = server.serverMethod(callVersion);
        server.serverMethod2();
        
        return serverResponse;
    }

}
