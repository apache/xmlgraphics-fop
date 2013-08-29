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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple alternative manager that holds a set of {@link Alternative}
 */
public class AlternativeManager {

    public static class Alternative {

        /** remaining BPD after inserting the alternative */
        private int remainingBPD;
        /** width of the alternative in block-progressing-dimension */
        private int width;
        /** Knuth element list */
        private List<ListElement> knuthList;

        public Alternative(List<ListElement> knuthList, int width) {
            this.knuthList = knuthList;
            this.width = width;
            this.remainingBPD = 0;
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
    }

    public static enum FittingStrategy {

        FIRST_FIT {

            @Override
            public Alternative filter(List<Alternative> alternatives) {
                for (Alternative alt : alternatives) {
                    if (alt.getRemainingBPD() > 0) {
                        return alt;
                    }
                }
                return null;
            }
        },

        SMALLEST_FIT {

            @Override
            public Alternative filter(List<Alternative> alternatives) {
                Iterator<Alternative> iter = alternatives.iterator();
                int biggestDiff = -Integer.MAX_VALUE;
                Alternative bestAlt = null;

                while (iter.hasNext()) {
                    Alternative alt = iter.next();
                    if (alt.getRemainingBPD() > biggestDiff) {
                        biggestDiff = alt.getRemainingBPD();
                        bestAlt = alt;
                    }
                }
                return bestAlt;
            }
        },

        BIGGEST_FIT {

            @Override
            public Alternative filter(List<Alternative> alternatives) {
                Iterator<Alternative> iter = alternatives.iterator();
                int smallestDiff = Integer.MAX_VALUE;
                Alternative bestAlt = null;

                while (iter.hasNext()) {
                    Alternative alt = iter.next();
                    if (alt.getRemainingBPD() < smallestDiff) {
                        smallestDiff = alt.getRemainingBPD();
                        bestAlt = alt;
                    }
                }
                return bestAlt;
            }
        };

        public abstract Alternative filter(List<Alternative> alternatives);

    }

    private LinkedList<Alternative> alternatives;
    private LinkedList<Alternative> bestAlternatives;

    public AlternativeManager() {
        alternatives = new LinkedList<Alternative>();
        bestAlternatives = new LinkedList<Alternative>();
    }

    public void addAlternative(Alternative alt) {
        alternatives.add(alt);
    }

    public boolean hasAny() {
        return bestAlternatives.size() > 0;
    }

    public Alternative getNextBestAlternative() {
        return bestAlternatives.removeFirst();
    }

    public Alternative getBestAlternative(FittingStrategy strategy) {
        try {
            Alternative bestAlt = strategy.filter(alternatives);
            if (bestAlt != null) {
                bestAlternatives.add(bestAlt);
            }
            return bestAlt;
        } catch (NullPointerException e) {
            return null;
        } finally {
            // We don't need the store alternatives anymore
            alternatives.clear();
        }
    }
}
