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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.io.Writer;
import java.io.IOException;

/**
 * Models the keep together attributes of paragraphs
 */
public class RtfParagraphKeepTogether extends RtfContainer {

    /** constant for unset status */
    public static final int STATUS_NULL = 0;
    /** constant for open paragraph */
    public static final int STATUS_OPEN_PARAGRAPH = 1;
    /** constant for close paragraph */
    public static final int STATUS_CLOSE_PARAGRAPH = 2;

    private int status = STATUS_NULL;


    /**    RtfParagraphKeepTogether*/
    RtfParagraphKeepTogether(IRtfParagraphContainer parent, Writer w) throws IOException {
        super((RtfContainer)parent, w);
    }

    /**
     * Write the content
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent() throws IOException {

        //First reet paragraph properties
        // create a new one with keepn
        if (status == STATUS_OPEN_PARAGRAPH) {
            writeControlWord("pard");
            writeControlWord("par");
            writeControlWord("keepn");
            writeGroupMark(true);
            status = STATUS_NULL;
        }


        if (status == STATUS_CLOSE_PARAGRAPH) {
            writeGroupMark(false);
            status = STATUS_NULL;
        }

    }


    /**
     * set the status
     * @param status the status to be set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return true if this element would generate no "useful" RTF content
     */
    public boolean isEmpty() {
        return false;
    }

}
