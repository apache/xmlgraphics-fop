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

import org.apache.fop.layoutmgr.AlternativeManager.Alternative;
import org.apache.fop.layoutmgr.AlternativeManager.FittingStrategy;

/**
 * A dummy penalty used in {@link BestFitLayoutManager} to store
 * the different alternatives in {@link fox:best-fit}
 */

public class BestFitPenalty extends KnuthPenalty {

    private LinkedList<Alternative> alternatives;
    private FittingStrategy strategy;

    public BestFitPenalty(FittingStrategy strategy, Position pos) {
        super(0, 0, false, pos, false);
        this.strategy = strategy;
        alternatives = new LinkedList<Alternative>();
    }

    public void addAlternative(Alternative alternative) {
        alternatives.add(alternative);
    }

    public Alternative getAlternative(int index) {
        return alternatives.get(index);
    }

    public int getAlternativeCount() {
        return alternatives.size();
    }

    public FittingStrategy getStrategyType() {
        return strategy;
    }

    /** {@inheritDoc} */
    public String toString() {
        String str = super.toString();
        StringBuffer buffer = new StringBuffer(64);
        buffer.append(" number of alternatives = " + getAlternativeCount());
        buffer.append(" fitting-strategy = " + strategy);
        return str + buffer.toString();
    }

}
