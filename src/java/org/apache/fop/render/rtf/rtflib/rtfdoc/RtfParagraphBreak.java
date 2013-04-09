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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.IOException;
import java.io.Writer;

/**  Class which represents a paragraph break.*/

public class RtfParagraphBreak extends RtfElement {
    private static final String DEFAULT_PARAGRAPH = "par";

    private String controlWord = DEFAULT_PARAGRAPH;

    RtfParagraphBreak(RtfContainer parent, Writer w)
            throws IOException {
        super(parent, w);
    }

    /**
     * @return true if this element would generate no "useful" RTF content
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * write RTF code of all our children
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent() throws IOException {
        if (controlWord != null) {
            writeControlWord(controlWord);
        }
    }

    /**
     * Whether or not the break can be skipped.
     * If the paragraph marks a table cell end it is not possible
     * @return boolean
     */
    public boolean canHide() {
        return this.controlWord.equals (DEFAULT_PARAGRAPH);
    }

    /**
     * Sets a different control word for this paragraph. If this method
     * is used the paragraph will always be displayed (@see canHide))
     * @param controlWord the new control word
     */
    public void switchControlWord(String controlWord) {
        this.controlWord = controlWord;
    }
}
