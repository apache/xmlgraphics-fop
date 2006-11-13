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

package org.apache.fop.layoutmgr.breaking;

/**
 * A length with three components: the natural value, the amount of authorized shrink, the
 * amount of authorized stretch.
 */
public class ElasticLength {

    private int shrink;

    private int length;

    private int stretch;

    /**
     * Creates a new length of 0, with 0 shrink and 0 stretch.
     */
    public ElasticLength() {
        shrink = 0;
        length = 0;
        stretch = 0;
    }

    /**
     * Creates a copy of the given length.
     *
     * @param el an elastic length
     */
    public ElasticLength(ElasticLength el) {
        shrink = el.shrink;
        length = el.length;
        stretch = el.stretch;
    }

    /**
     * Creates a new length with the given components.
     *
     * @param shrink amount of authorized shrink
     * @param length natural length
     * @param stretch amount of authorized stretch
     */
    public ElasticLength(int shrink, int length, int stretch) {
        set(shrink, length, stretch);
    }

    /**
     * Returns the amount by which this length may be shrinked.
     *
     * @return the amount by which this length may be shrinked.
     */
    public int getShrink() {
        return shrink;
    }

    /**
     * Returns the natural value of this length.
     *
     * @return the natural value of this length
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the amount by which this length may be stretched.
     *
     * @return the amount by which this length may be stretched.
     */
    public int getStretch() {
        return stretch;
    }

    /**
     * Resets the three components of this length to 0.
     */
    public void reset() {
        set(0, 0, 0);
    }

    /**
     * Sets the components of this length to the given values.
     *
     * @param shrink authorized shrink
     * @param length natural length
     * @param stretch authorized stretch
     */
    public void set(int shrink, int length, int stretch) {
        this.shrink = shrink;
        this.length = length;
        this.stretch = stretch;
    }

    /**
     * Sets the components of this length to those of the given length.
     *
     * @param elasticLength a length
     */
    public void set(ElasticLength elasticLength) {
        set(elasticLength.shrink, elasticLength.length, elasticLength.stretch);
    }

    /**
     * Adds the given values to the components of this length.
     *
     * @param shrink additional authorized shrink
     * @param length additional natural length
     * @param stretch additional authorized stretch
     */
    public void add(int shrink, int length, int stretch) {
        this.shrink += shrink;
        this.length += length;
        this.stretch += stretch;
    }

    /**
     * Adds to the components of this length those of the given length.
     *
     * @param elasticLength a length of which each component will be added to this ones'
     */
    public void add(ElasticLength elasticLength) {
        add(elasticLength.shrink, elasticLength.length, elasticLength.stretch);
    }

    public String toString() {
        return "[" + shrink + "," + length + "," + stretch + "]";
    }
}
