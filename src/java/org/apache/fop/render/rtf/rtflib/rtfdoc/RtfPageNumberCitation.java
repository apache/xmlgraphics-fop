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

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.io.Writer;
import java.io.IOException;

/**
 * @author Christopher Scott, scottc@westinghouse.com
 * @author Boris Pouderous, boris.pouderous@free.fr
 */
public class RtfPageNumberCitation extends RtfContainer {
    /* Page field :
       "{\field {\*\fldinst {PAGEREF xx}} {\fldrslt}}" where xx represents the
       'id' of the referenced page
    */

    /** constant for field */
    public static final String RTF_FIELD = "field";
    /** constant for field pageref model */
    public static final String RTF_FIELD_PAGEREF_MODEL = "fldinst { PAGEREF }";
    /** constant for field result */
    public static final String RTF_FIELD_RESULT = "fldrslt";

    // The 'id' of the referenced page
    private String id = null;

    /** Create an RTF page number citation as a child of given container with default attributes */
    RtfPageNumberCitation (IRtfPageNumberCitationContainer parent, Writer w, String id)
            throws IOException {
        super((RtfContainer)parent, w);
        this.id = id;
    }

    /** Create an RTF page number citation as a child of given
     *    paragraph, copying its attributes */
    RtfPageNumberCitation (RtfParagraph parent, Writer w, String id)
            throws IOException {
        // add the attributes ant text attributes of the parent paragraph
        super((RtfContainer)parent, w, parent.attrib);
        if (parent.getTextAttributes() != null) {
            attrib.set(parent.getTextAttributes());
        }
        this.id = id;
    }

    /**
     * Write the content
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent() throws IOException {
        // If we have a valid ID
        if (isValid()) {
            // Build page reference field
            String pageRef = RTF_FIELD_PAGEREF_MODEL;
            final int insertionIndex = pageRef.indexOf("}");
            pageRef = pageRef.substring(0, insertionIndex)
                + "\"" + id
                + "\"" + " "
                + pageRef.substring(insertionIndex, pageRef.length());
            id = null;

            // Write RTF content
            writeGroupMark(true);
            writeControlWord(RTF_FIELD);
            writeGroupMark(true);
            writeAttributes(attrib, RtfText.ATTR_NAMES); // Added by Boris Poudérous
            writeStarControlWord(pageRef);
            writeGroupMark(false);
            writeGroupMark(true);
            writeControlWord(RTF_FIELD_RESULT);
            writeGroupMark(false);
            writeGroupMark(false);
        }
    }

    /** checks that the 'ref-id' attribute exists */
    private boolean isValid() {
        return (id != null);
    }

    /**
     * @return true if this element would generate no "useful" RTF content
     */
    public boolean isEmpty() {
        return false;
    }
}