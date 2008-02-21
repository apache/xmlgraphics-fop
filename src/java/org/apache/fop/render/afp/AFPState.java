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

package org.apache.fop.render.afp;

/**
 * This keeps information about the current state when writing to an AFP datastream.
 */
public class AFPState extends org.apache.fop.render.AbstractState {

    /** {@inheritDoc} */
    protected AbstractData instantiateData() {
        return new AFPData();
    }

    /**
     * Sets if the current painted shape is to be filled
     * @param fill true if the current painted shape is to be filled
     * @return true if the fill value has changed
     */
    protected boolean setFill(boolean fill) {
        if (fill != ((AFPData)getData()).filled) {
            ((AFPData)getData()).filled = fill;
            return true;
        }
        return false;
    }

    /**
     * Gets the current page fonts
     * @return the current page fonts
     */
    protected AFPPageFonts getPageFonts() {
        if (((AFPData)getData()).pageFonts == null) {
            ((AFPData)getData()).pageFonts = new AFPPageFonts();
        }
        return ((AFPData)getData()).pageFonts;
    }
    
    private class AFPData extends org.apache.fop.render.AbstractState.AbstractData {
        private static final long serialVersionUID = -1789481244175275686L;

        /** The current fill status */
        private boolean filled = false;
        
        /** The fonts on the current page */
        private AFPPageFonts pageFonts = null;

        /** {@inheritDoc} */
        public Object clone() throws CloneNotSupportedException {
            AFPData obj = (AFPData)super.clone();
            obj.filled = this.filled;
            obj.pageFonts = this.pageFonts;
            return obj;
        }
    }
}