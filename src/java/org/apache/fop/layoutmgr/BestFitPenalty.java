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

import org.apache.fop.layoutmgr.Alternative.FittingStrategy;
import org.apache.fop.layoutmgr.BestFitLayoutUtils.BestFitPosition;

/**
 * A penalty class used to specify a set of alternatives for the layout engine
 * to choose from. The chosen alternative must have an occupied size
 * that is less than the available BPD of the current page
 * and it must also be the best match when it is evaluated by {@link FittingStrategy}.
 */
public class BestFitPenalty extends KnuthPenalty {

    private final LinkedList<Alternative> alternatives;
    private final FittingStrategy strategy;
    public boolean canFit = true;
    private int currentAltIndex;

    public BestFitPenalty(FittingStrategy strategy, Position pos) {
        super(0, 0, false, pos, false);
        this.strategy = strategy;
        alternatives = new LinkedList<Alternative>();
    }

    public void addAlternative(Alternative alternative) {
        alternatives.add(alternative);
    }

    public List<Alternative> getAlternatives() {
        return alternatives;
    }

    public FittingStrategy getFittingStrategy() {
        return strategy;
    }

    @Override
    public int getWidth() {
        if (currentAltIndex == -1) {
            return 0;
        }
        return alternatives.get(currentAltIndex).getLength();
    }

    public boolean hasMoreAlternatives() {
        return currentAltIndex != -1;
    }

    public void considerNextAlternative() {
        if (currentAltIndex < alternatives.size() - 1) {
            currentAltIndex++;
        } else {
            currentAltIndex = -1;
        }
    }

    @Override
    public Position getPosition() {
        if (currentAltIndex != -1) {
            Position pos = super.getPosition();
            if (alternatives.size() > 0) {
                getBestFitPosition().setKnuthList(alternatives.get(currentAltIndex).getKnuthList());
            }
            return pos;
        }
        return null;
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
        buffer.append(" number of alternatives = " + getAlternatives().size());
        buffer.append(" fitting-strategy = " + strategy.getStrategyName());
        return str + buffer.toString();
    }

}
