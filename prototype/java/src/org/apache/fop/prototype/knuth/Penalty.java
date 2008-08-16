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

package org.apache.fop.prototype.knuth;

/**
 * A penalty.
 */
public class Penalty extends KnuthElement {

    public static final Penalty DEFAULT_PENALTY = new Penalty(0, 0);

    public static final int INFINITE = 1000;

    private int penalty;

    public Penalty(int length, int penalty, String content) {
        super(length, content);
        this.penalty = penalty;
    }

    public Penalty(int length, int penalty) {
        this(length, penalty, "");
    }

    public boolean isForcedBreak() {
        return penalty == -INFINITE;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPenalty() {
        return true;
    }

    /**
     * @return the penalty
     */
    public int getPenalty() {
        return penalty;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Pen w = " + getLength() + " p = " + penalty + super.toString();
    }

}
