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

package org.apache.fop.prototype.breaking;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.fop.prototype.breaking.layout.Layout;


/**
 * TODO javadoc
 */
class FeasibleBreaks<L extends Layout> {

    static class BestBreak<L> {

        L layout;

        double demerits;

        int difference;

        /**
         * @param layout
         * @param demerits
         * @param difference
         */
        BestBreak(L layout, double demerits, int difference) {
            this.layout = layout;
            this.demerits = demerits;
            this.difference = difference;
        }

        void set(L layout, double demerits, int difference) {
            this.layout = layout;
            this.demerits = demerits;
            this.difference = difference;
        }
    }

    private BestBreak<L> best = null;

    private Collection<Layout> alternatives = new LinkedList<Layout>();

    void add(L layout, double demerits, int difference) {
        if (best == null) {
            best = new BestBreak<L>(layout, demerits, difference);
        } else if (demerits < best.demerits) {
            alternatives.add(best.layout);
            best.set(layout, demerits, difference);
        } else {
            alternatives.add(layout);
        }
    }

    BestBreak<L> getBest() {
        return best;
    }

    Collection<Layout> getAlternatives() {
        return alternatives;
    }
}
