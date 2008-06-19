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
 * A glue.
 */
public class Glue extends KnuthElement {

    private int stretch;

    private int shrink;

    public Glue(int length, int stretch, int shrink, String content) {
        super(length, content);
        this.stretch = stretch;
        this.shrink = shrink;
    }

    public Glue(int length, int stretch, int shrink) {
        this(length, stretch, shrink, "");
    }

    /**
     * @return the stretch
     */
    public int getStretch() {
        return stretch;
    }

    /**
     * @return the shrink
     */
    public int getShrink() {
        return shrink;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Glue w = " + getLength() + " str = " + stretch + " shr = " + shrink
                + super.toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isGlue() {
        return true;
    }
}
