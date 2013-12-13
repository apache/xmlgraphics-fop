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

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.layoutmgr.BestFitLayoutUtils.BestFitPosition;

/**
 * A type of penalty used to specify a set of alternatives for the layout engine
 * to choose from. The chosen alternative must have an occupied size
 * that is less than the available BPD of the current page.
 */
public class BestFitPenalty extends KnuthPenalty {

    public class Variant {

        public final List<ListElement> knuthList;
        public final int width;

        public Variant(List<ListElement> knuthList, int width) {
            this.knuthList = knuthList;
            this.width = width;
        }
        public KnuthElement toPenalty() {
            return new KnuthPenalty(width, 0, false, null, false);
        }
    }

    private final BestFitPosition bestFitPosition;

    private final List<Variant> variantList;

    public BestFitPenalty(BestFitPosition pos) {
        super(0, 0, false, pos, false);
        this.bestFitPosition = pos;
        variantList = new ArrayList<Variant>();
    }

    public void addVariant(List<ListElement> knuthList, int width) {
        variantList.add(new Variant(knuthList, width));
    }

    public void activatePenalty(Variant bestVariant) {
        bestFitPosition.setKnuthList(bestVariant.knuthList);
    }

    public List<Variant> getVariantList() {
        return variantList;
    }

    @Override
    public String toString() {
        String str = super.toString();
        StringBuffer buffer = new StringBuffer(64);
        buffer.append(" number of variants = " + variantList.size());
        return str + buffer.toString();
    }

}
