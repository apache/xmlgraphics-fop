/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 
package org.apache.fop.layout;

/**
 * This class holds information about text-decoration.
 */
public class TextState {

    private boolean underlined;
    private boolean overlined;
    private boolean linethrough;

    /**
     * @return true if text should be underlined
     */
    public boolean getUnderlined() {
        return underlined;
    }

    /**
     * Set text as underlined.
     * @param ul true if underline should be enabled
     */
    public void setUnderlined(boolean ul) {
        this.underlined = ul;
    }

    /**
     * @return true if text should be overlined
     */
    public boolean getOverlined() {
        return overlined;
    }

    /**
     * Set text as overlined.
     * @param ol true if overline should be enabled
     */
    public void setOverlined(boolean ol) {
        this.overlined = ol;
    }

    /**
     * @return true if text should have a line through the middle
     */
    public boolean getLineThrough() {
        return linethrough;
    }

    /**
     * Controls if text should have a line through the middle.
     * @param lt true if line through should be enabled
     */
    public void setLineThrough(boolean lt) {
        this.linethrough = lt;
    }

}
