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

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>RTF file header, contains style, font and other document-level information.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch),
 * Andreas Putz (a.putz@skynamics.com), and
 * Marc Wilhelm Kuester.</p>
 */

class RtfHeader extends RtfContainer {
    private static final String CHARSET = "ansi";
    private final Map userProperties = new HashMap();

    /** Create an RTF header */
    RtfHeader(RtfFile f, Writer w) throws IOException {
        super(f, w);
        new RtfFontTable(this, w);
        new RtfGenerator(this, w);
//        m_userProperties.put("jforVersion",JForVersionInfo.getLongVersionInfo());
    }

    /** Overridden to write our own data before our children's data */
    protected void writeRtfContent() throws IOException {
        writeControlWord(CHARSET);
        writeUserProperties();
        RtfColorTable.getInstance().writeColors(this);
        super.writeRtfContent();
        RtfTemplate.getInstance().writeTemplate(this);
        RtfStyleSheetTable.getInstance().writeStyleSheet(this);
        writeFootnoteProperties();

    }

    /** write user properties if any */
    private void writeUserProperties() throws IOException {
        if (userProperties.size() > 0) {
            writeGroupMark(true);
            writeStarControlWord("userprops");
            for (Iterator it = userProperties.entrySet().iterator(); it.hasNext();) {
                final Map.Entry entry = (Map.Entry)it.next();
                writeGroupMark(true);
                writeControlWord("propname");
                RtfStringConverter.getInstance().writeRtfString(writer,
                        entry.getKey().toString());
                writeGroupMark(false);
                writeControlWord("proptype30");
                writeGroupMark(true);
                writeControlWord("staticval");
                RtfStringConverter.getInstance().writeRtfString(writer,
                        entry.getValue().toString());
                writeGroupMark(false);
            }
            writeGroupMark(false);
        }
    }

    /** write directly to our Writer
     *  TODO should check that this done at the right point, or even better, store
     *  what is written here to render it in writeRtfContent. <-- it is for the color table
     */
    void write(String toWrite) throws IOException {
        writer.write(toWrite);
    }

    /** write to our Writer using an RtfStringConverter */
    void writeRtfString(String toWrite) throws IOException {
        RtfStringConverter.getInstance().writeRtfString(writer, toWrite);
    }

    /**
     *write properties for footnote handling
     */
    private void writeFootnoteProperties() throws IOException {
        newLine();
        writeControlWord("fet0");  //footnotes, not endnotes
        writeControlWord("ftnbj"); //place footnotes at the end of the
                                   //page (should be the default, but
                                   //Word 2000 thinks otherwise)
    }
}
