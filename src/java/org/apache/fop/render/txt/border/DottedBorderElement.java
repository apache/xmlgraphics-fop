/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.render.txt.border;

/**
 * This class is responsible for managing of dotted border elements.
 */
public class DottedBorderElement extends AbstractBorderElement {
    
    private static final char MIDDLE_DOT = '\u00B7';

    /**
     * Merges dotted border element with another border element. Here merging
     * is quite simple: returning <code>this</code> without any comparing.
     * 
     * @param e instance of AbstractBorderElement
     * @return instance of DottedBorderElement
     */
    public AbstractBorderElement merge(AbstractBorderElement e) {
        return this;
    }

    /**
     * @see org.apache.fop.render.txt.border.AbstractBorderElement#convert2Char()
     */
    public char convert2Char() {
        return MIDDLE_DOT;
    }
}
