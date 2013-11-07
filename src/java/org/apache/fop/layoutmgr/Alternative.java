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

package org.apache.fop.layoutmgr;

import java.util.LinkedList;
import java.util.List;

/**
 * An alternative has a set of fitness traits (e.g. occupied bpd and ipd,
 * adjustment ratio, remaining size, etc.) that determine how good its
 * eligibility is when evaluated by a fitting strategy.
 */
public class Alternative {

    /** Remaining BPD after inserting the alternative. */
    private int remainingBPD;
    /** Size of the alternative in block-progression-direction. */
    private final int length;
    private final List<ListElement> knuthList;
    private boolean enabled = true;

    public Alternative(List<ListElement> knuthList, int length) {
        this.knuthList = knuthList;
        this.length = length;
        this.remainingBPD = 0;
    }

    public List<ListElement> getKnuthList() {
        return knuthList;
    }

    public int getRemainingBPD() {
        return remainingBPD;
    }

    public int getLength() {
        return length;
    }

    public void setRemainingBPD(int remainingBPD) {
        this.remainingBPD = remainingBPD;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static enum FittingStrategy {

        FIRST_FIT("first-fit") {

            @Override
            public List<Alternative> filter(List<Alternative> alternatives) {
                List<Alternative> alts = new LinkedList<Alternative>();
                for (Alternative alt : alternatives) {
                    if (alt.isEnabled()) {
                        alts.add(alt);
                        break;
                    }
                }
                return alts;
            }
        },

        SMALLEST_FIT("smallest-fit") {

            @Override
            public List<Alternative> filter(List<Alternative> alternatives) {
                int biggestDiff = -Integer.MAX_VALUE;
                Alternative bestAlt = null;

                for (Alternative alt : alternatives) {
                    if (alt.isEnabled() && alt.getRemainingBPD() > biggestDiff) {
                        biggestDiff = alt.getRemainingBPD();
                        bestAlt = alt;
                    }
                }
                List<Alternative> alts = new LinkedList<Alternative>();
                alts.add(bestAlt);
                return alts;
            }
        },

        BIGGEST_FIT("biggest-fit") {

            @Override
            public List<Alternative> filter(List<Alternative> alternatives) {
                int smallestDiff = Integer.MAX_VALUE;
                Alternative bestAlt = null;

                for (Alternative alt : alternatives) {
                    if (alt.isEnabled() && alt.getRemainingBPD() < smallestDiff) {
                        smallestDiff = alt.getRemainingBPD();
                        bestAlt = alt;
                    }
                }
                List<Alternative> alts = new LinkedList<Alternative>();
                alts.add(bestAlt);
                return alts;
            }
        },

        ANY("any") {

            @Override
            public List<Alternative> filter(List<Alternative> alternatives) {
                List<Alternative> alts = new LinkedList<Alternative>();

                int remainingSpace = Integer.MAX_VALUE;
                for (Alternative alt : alternatives) {
                    if (alt.isEnabled() && alt.getLength() <= remainingSpace) {
                        alts.add(alt);
                        remainingSpace = alt.getRemainingBPD();
                    }
                }
                return alts;
            }
        };

        private String strategyName;

        FittingStrategy(String strategyName) {
            this.strategyName = strategyName;
        }

        public String getStrategyName() {
            return strategyName;
        }

        public static FittingStrategy make(String strategyName) {
            for (FittingStrategy fs : FittingStrategy.values()) {
                if (fs.getStrategyName().equals(strategyName)) {
                    return fs;
                }
            }
            return null;
        }

        /**
         * @param alternatives the list of potential candidate {@link Alternative}
         * @return the best alternative according to the strategy being employed
         */
        public abstract List<Alternative> filter(List<Alternative> alternatives);

    }
}
