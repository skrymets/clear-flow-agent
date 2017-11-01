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
public class ParticipantData {

    private final long instanceCode;
    private final long classCode;

    public ParticipantData(long instanceCode, long classCode) {
        this.instanceCode = instanceCode;
        this.classCode = classCode;
    }

    public long getInstanceCode() {
        return instanceCode;
    }

    public long getClassCode() {
        return classCode;
    }


    @Override
    public String toString() {
        return "" + instanceCode;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (int) (this.instanceCode ^ (this.instanceCode >>> 32));
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
        final ParticipantData other = (ParticipantData) obj;
        if (this.instanceCode != other.instanceCode) {
            return false;
        }
        return true;
    }

}
