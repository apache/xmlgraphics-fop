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

/**
 * A dummy penalty used in {@link BestFitLayoutManager} to store
 * the different alternatives in {@link fox:best-fit}
 */

public class BestFitPenalty extends KnuthPenalty {

    private final LinkedList<Alternative> alternatives;
    private final FittingStrategy strategy;
    private Alternative bestAlternative = null;

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

    public FittingStrategy getStrategyType() {
        return strategy;
    }

    public Alternative getBestAlternative() {
        if (bestAlternative != null) {
            return bestAlternative;
        } else {
            bestAlternative = strategy.filter(alternatives);
            return bestAlternative;
        }
    }

//    public void setBestAlternative(Alternative bestAlternative) {
//        this.bestAlternative = bestAlternative;
//    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        String str = super.toString();
        StringBuffer buffer = new StringBuffer(64);
        buffer.append(" number of alternatives = " + getAlternatives().size());
        buffer.append(" fitting-strategy = " + strategy);
        return str + buffer.toString();
    }

}
