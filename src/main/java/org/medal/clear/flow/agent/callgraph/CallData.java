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

/**
 *
 * @author skrymets
 */
public class CallData {

    private final long callTime;
    private final long messageCode;

    public CallData(long callTime, long messageCode) {
        this.callTime = callTime;
        this.messageCode = messageCode;
    }

    public long getCallTime() {
        return callTime;
    }

    public long getMessageCode() {
        return messageCode;
    }

    @Override
    public String toString() {
        return "" + messageCode + "@" + callTime;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (int) (this.callTime ^ (this.callTime >>> 32));
        hash = 13 * hash + (int) (this.messageCode ^ (this.messageCode >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CallData other = (CallData) obj;
        if (this.callTime != other.callTime) {
            return false;
        }
        if (this.messageCode != other.messageCode) {
            return false;
        }
        return true;
    }
    
    
}
