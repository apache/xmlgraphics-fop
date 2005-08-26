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

package org.apache.fop.area.inline;

/**
 * Abstract base class for both TextArea and Character.
 */
public abstract class AbstractTextArea extends InlineArea {

    private int iTextWordSpaceAdjust = 0;
    private int iTextLetterSpaceAdjust = 0;

    /**
     * Get text word space adjust.
     *
     * @return the text word space adjustment
     */
    public int getTextWordSpaceAdjust() {
        return iTextWordSpaceAdjust;
    }

    /**
     * Set text word space adjust.
     *
     * @param iTWSadjust the text word space adjustment
     */
    public void setTextWordSpaceAdjust(int iTWSadjust) {
        iTextWordSpaceAdjust = iTWSadjust;
    }
    /**
     * Get text letter space adjust.
     *
     * @return the text letter space adjustment
     */
    public int getTextLetterSpaceAdjust() {
        return iTextLetterSpaceAdjust;
    }

    /**
     * Set text letter space adjust.
     *
     * @param iTLSadjust the text letter space adjustment
     */
    public void setTextLetterSpaceAdjust(int iTLSadjust) {
        iTextLetterSpaceAdjust = iTLSadjust;
    }
}
