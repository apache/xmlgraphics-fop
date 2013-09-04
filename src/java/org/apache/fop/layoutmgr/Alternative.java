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

import java.util.List;

public class Alternative {

    /** remaining BPD after inserting the alternative */
    private int remainingBPD;
    /** width of the alternative in block-progressing-dimension */
    private final int width;
    /** Knuth element list */
    private final List<ListElement> knuthList;
    /** Indicates whether we should consider the alternative or not */
    private boolean enabled;

    public Alternative(List<ListElement> knuthList, int width) {
        this.knuthList = knuthList;
        this.width = width;
        this.remainingBPD = 0;
        this.enabled = false;
    }

    public List<ListElement> getKnuthList() {
        return knuthList;
    }

    public int getRemainingBPD() {
        return remainingBPD;
    }

    public int getWidth() {
        return width;
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
            public Alternative filter(List<Alternative> alternatives) {
                for (Alternative alt : alternatives) {
                    if (alt.isEnabled()) {
                        return alt;
                    }
                }
                return null;
            }
        },

        SMALLEST_FIT("smallest-fit") {

            @Override
            public Alternative filter(List<Alternative> alternatives) {
                int biggestDiff = -Integer.MAX_VALUE;
                Alternative bestAlt = null;

                for (Alternative alt : alternatives) {
                    if (alt.isEnabled() && alt.getRemainingBPD() > biggestDiff) {
                        biggestDiff = alt.getRemainingBPD();
                        bestAlt = alt;
                    }
                }
                return bestAlt;
            }
        },

        BIGGEST_FIT("biggest-fit") {

            @Override
            public Alternative filter(List<Alternative> alternatives) {
                int smallestDiff = Integer.MAX_VALUE;
                Alternative bestAlt = null;

                for (Alternative alt : alternatives) {
                    if (alt.isEnabled() && alt.getRemainingBPD() < smallestDiff) {
                        smallestDiff = alt.getRemainingBPD();
                        bestAlt = alt;
                    }
                }
                return bestAlt;
            }
        };

        private String strategyName;

        FittingStrategy(String strategyName) {
            this.strategyName = strategyName;
        }

        public String getStrategyName() {
            return strategyName;
        }

        /**
         * @param alternatives the list of potential candidate {@link Alternative}
         * @return the best alternative according to the strategy being employed
         */
        public abstract Alternative filter(List<Alternative> alternatives);

    }
}
