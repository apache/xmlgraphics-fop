/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.rtf.rtflib.rtfdoc;

import java.util.*;
import java.io.Writer;
import java.io.IOException;
//import org.apache.fop.rtf.rtflib.jfor.main.JForVersionInfo;

/**  RTF file header, contains style, font and other document-level information.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  @author Andreas Putz a.putz@skynamics.com
 */

class RtfHeader extends RtfContainer {
    private final String m_charset = "ansi";
    private final Map m_userProperties = new HashMap();

    /** Create an RTF header */
    RtfHeader(RtfFile f,Writer w) throws IOException {
        super(f,w);
        new RtfFontTable(this,w);
//        m_userProperties.put("jforVersion",JForVersionInfo.getLongVersionInfo());
    }

    /** Overridden to write our own data before our children's data */
    protected void writeRtfContent() throws IOException {
        writeControlWord(m_charset);
        writeUserProperties();
        RtfColorTable.getInstance().writeColors(this);
        super.writeRtfContent();
         RtfTemplate.getInstance().writeTemplate(this);
           RtfStyleSheetTable.getInstance().writeStyleSheet(this);

    }

    /** write user properties if any */
    private void writeUserProperties() throws IOException {
        if(m_userProperties.size() > 0) {
            writeGroupMark(true);
            writeStarControlWord("userprops");
            for(Iterator it = m_userProperties.entrySet().iterator(); it.hasNext(); ) {
                final Map.Entry entry = (Map.Entry)it.next();
                writeGroupMark(true);
                writeControlWord("propname");
                RtfStringConverter.getInstance().writeRtfString(m_writer,entry.getKey().toString());
                writeGroupMark(false);
                writeControlWord("proptype30");
                writeGroupMark(true);
                writeControlWord("staticval");
                RtfStringConverter.getInstance().writeRtfString(m_writer,entry.getValue().toString());
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
        m_writer.write(toWrite);
    }

    /** write to our Writer using an RtfStringConverter */
    void writeRtfString(String toWrite) throws IOException {
        RtfStringConverter.getInstance().writeRtfString(m_writer,toWrite);
    }
}
