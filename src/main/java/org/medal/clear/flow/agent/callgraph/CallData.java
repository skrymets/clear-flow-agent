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
    private final long callerId;
    private final long threadId;
    private final long methodId;

    public CallData(long nanoTime, long callerId, long methodId, long threadId) {
        this.callTime = nanoTime;
        this.threadId = threadId;
        this.methodId = methodId;
        this.callerId = callerId;
    }

    public long getCallTime() {
        return callTime;
    }

    public long getMethodId() {
        return methodId;
    }

    public long getThreadId() {
        return threadId;
    }

    public long getCallerId() {
        return callerId;
    }

    @Override
    public String toString() {
        return String.valueOf(methodId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (int) (this.callTime ^ (this.callTime >>> 32));
        hash = 13 * hash + (int) (this.threadId ^ (this.threadId >>> 32));
        hash = 13 * hash + (int) (this.methodId ^ (this.methodId >>> 32));
        hash = 13 * hash + (int) (this.callerId ^ (this.callerId >>> 32));
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
        if (this.methodId != other.methodId) {
            return false;
        }
        if (this.threadId != other.threadId) {
            return false;
        }
        if (this.callerId != other.callerId) {
            return false;
        }
        return true;
    }

}
