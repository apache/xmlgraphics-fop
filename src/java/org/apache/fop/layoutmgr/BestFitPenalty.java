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

import org.apache.fop.layoutmgr.BestFitLayoutUtils.BestFitPosition;

/**
 * A penalty class used to specify a set of alternatives for the layout engine
 * to choose from. The chosen alternative must have an occupied size
 * that is less than the available BPD of the current page
 * and it must also be the best match when it is evaluated by {@link FittingStrategy}.
 */
public class BestFitPenalty extends KnuthPenalty {

    private final List<ListElement> knuthList;
    public boolean ignorePenalty;

    public BestFitPenalty(int width, List<ListElement> knuthList, Position pos) {
        super(width, 0, false, pos, false);
        this.knuthList = knuthList;
    }

    public void activateContent() {
        BestFitPosition pos = getBestFitPosition();
        pos.setKnuthList(knuthList);
    }

    public int getWidth() {
        if (ignorePenalty) {
            return 0;
        }
        return super.getWidth();
    }

    public BestFitPosition getBestFitPosition() {
        Position pos = super.getPosition();
        while (pos != null) {
            if (pos instanceof BestFitPosition) {
                return (BestFitPosition) pos;
            }
            pos = pos.getPosition();
        }
        return null;
    }

    @Override
    public String toString() {
        String str = super.toString();
        StringBuffer buffer = new StringBuffer(64);
//        buffer.append(" number of alternatives = " + alternatives.size());
//        buffer.append(" fitting-strategy = " + strategy.getStrategyName());
        return str + buffer.toString();
    }

}
