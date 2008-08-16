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

import org.apache.fop.prototype.breaking.layout.Layout;

/**
 * TODO javadoc
 */
public class CompletedPart {

    Layout layout;

    double demerits;

    int difference;

    Collection<Alternative> alternatives;

    /**
     * @param layout
     * @param demerits
     * @param difference
     */
    public/*TODO*/ CompletedPart(Layout layout, double demerits, int difference, Collection<Alternative> alternatives) {
        this.layout = layout;
        this.demerits = demerits;
        this.difference = difference;
        this.alternatives = alternatives;
    }

    /**
     * @return the layout
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * @return the demerits
     */
    public double getDemerits() {
        return demerits;
    }

    /**
     * @return the difference
     */
    public int getDifference() {
        return difference;
    }

    /**
     * @return the alternatives
     */
    public Collection<Alternative> getAlternatives() {
        return alternatives;
    }
}
