/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.svg;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class OperatorValidator {

    private static interface Match {

        boolean match(String line);
    }

    private static class MatchSequence implements OperatorValidator.Match {

        private final Queue<OperatorValidator.Match> expectedMatches = new LinkedList<OperatorValidator.Match>();

        private OperatorValidator.Match currentMatch;

        private static final OperatorValidator.Match FINAL_MATCH = new Match() {

            public boolean match(String line) {
                return false;
            }
        };

        public boolean isExhausted() {
            return currentMatch == FINAL_MATCH;
        }

        public void addMatch(OperatorValidator.Match match) {
            if (currentMatch == null) {
                currentMatch = match;
            } else {
                expectedMatches.add(match);
            }
        }

        public boolean match(String line) {
            boolean match = currentMatch.match(line);
            if (match) {
                if(expectedMatches.isEmpty()) {
                    currentMatch = FINAL_MATCH;
                } else {
                    currentMatch = expectedMatches.remove();
                }
            }
            return match;
        }
    }

    private static class OperatorMatch implements OperatorValidator.Match {

        final String operator;

        final String line;

        OperatorMatch(String operator, String line) {
            this.operator = operator;
            this.line = line;
        }

        public boolean match(String line) {
            if (line.contains(operator)) {
                assertEquals(this.line, line);
                return true;
            }
            return false;
        }
    }

    private final OperatorValidator.MatchSequence matchSequence = new MatchSequence();

    public OperatorValidator addOperatorMatch(String operator, String expectedLine) {
        matchSequence.addMatch(new OperatorMatch(operator, expectedLine));
        return this;
    }

    public void check(String line) {
        matchSequence.match(line);
    }

    public void end() {
        assertTrue("Expected operators remain", matchSequence.isExhausted());
    }

}