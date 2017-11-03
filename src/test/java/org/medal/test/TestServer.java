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

/**
 *
 * @author skrymets
 */
public class TestServer {

    public int serverMethod() {
        int result = 42;
        return result;
    }

    public void serverMethod2() {
        String guid = "AB_CD_EF";
        String toLowerCase = guid.toLowerCase();
        System.out.println(toLowerCase);
    }

}