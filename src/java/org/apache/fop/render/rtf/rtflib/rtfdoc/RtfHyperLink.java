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

/**
 * Creates an hyperlink.
 * This class belongs to the <fo:basic-link> tag processing.
 * @author <a href="mailto:a.putz@skynamics.com">Andreas Putz</a>
 *
 * {\field {\*\fldinst HYPERLINK "http://www.test.de"   }{\fldrslt Joe Smith}}
 */
public class RtfHyperLink
extends RtfContainer
implements IRtfTextContainer,
           IRtfTextrunContainer {

    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////

    /** The url of the image */
    protected String url = null;

    /** RtfText */
    protected RtfText mText = null;

    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////


    /**
     * A constructor.
     *
     * @param parent a <code>RtfContainer</code> value
     * @param writer a <code>Writer</code> value
     * @param str text of the link
     * @param attr a <code>RtfAttributes</code> value
     * @throws IOException for I/O problems
     */
    public RtfHyperLink (IRtfTextContainer parent, Writer writer, String str, RtfAttributes attr)
        throws IOException {
        super ((RtfContainer) parent, writer, attr);
        new RtfText (this, writer, str, attr);
    }

    /**
     * A constructor.
     *
     * @param parent a <code>RtfContainer</code> value
     * @param writer a <code>Writer</code> value
     * @param attr a <code>RtfAttributes</code> value
     * @throws IOException for I/O problems
     */
    public RtfHyperLink (RtfTextrun parent, Writer writer, RtfAttributes attr)
        throws IOException {
        super ((RtfContainer) parent, writer, attr);
    }


    //////////////////////////////////////////////////
    // @@ RtfElement implementation
    //////////////////////////////////////////////////

    /**
     * Writes the RTF content to m_writer.
     *
     * @exception IOException On error
     */
    public void writeRtfPrefix () throws IOException {
        super.writeGroupMark (true);
        super.writeControlWord ("field");

        super.writeGroupMark (true);
        super.writeStarControlWord ("fldinst");

        writer.write ("HYPERLINK \"" + url + "\" ");
        super.writeGroupMark (false);

        super.writeGroupMark (true);
        super.writeControlWord ("fldrslt");

        // start a group for this paragraph and write our own attributes if needed
        if (attrib != null && attrib.isSet ("cs")) {
            writeGroupMark (true);
            writeAttributes(attrib, new String [] {"cs"});
        }
    }

    /**
     * Writes the RTF content to m_writer.
     *
     * @exception IOException On error
     */
    public void writeRtfSuffix () throws IOException {
        if (attrib != null && attrib.isSet ("cs")) {
            writeGroupMark (false);
        }
        super.writeGroupMark (false);
        super.writeGroupMark (false);
    }


    //////////////////////////////////////////////////
    // @@ IRtfContainer implementation
    //////////////////////////////////////////////////

    /**
     * close current text run if any and start a new one with default attributes
     * @param str if not null, added to the RtfText created
     * @throws IOException for I/O problems
     * @return new RtfText object
     */
    public RtfText newText (String str) throws IOException {
        return newText (str, null);
    }

    /**
     * close current text run if any and start a new one
     * @param str if not null, added to the RtfText created
     * @param attr attributes of text to add
     * @throws IOException for I/O problems
     * @return the new RtfText object
     */
    public RtfText newText (String str, RtfAttributes attr) throws IOException {
        closeAll ();
        mText = new RtfText (this, writer, str, attr);
        return mText;
    }

    /**
     * IRtfTextContainer requirement:
     * @return a copy of our attributes
     */
    public RtfAttributes getTextContainerAttributes() {
        if (attrib == null) {
            return null;
        }
        return (RtfAttributes) this.attrib.clone ();
    }


    /**
     * add a line break
     * @throws IOException for I/O problems
     */
    public void newLineBreak () throws IOException {
        new RtfLineBreak (this, writer);
    }


    //////////////////////////////////////////////////
    // @@ Common container methods
    //////////////////////////////////////////////////

    private void closeCurrentText () throws IOException {
        if (mText != null) {
            mText.close ();
        }
    }

    private void closeAll () throws IOException {
        closeCurrentText();
    }


    //////////////////////////////////////////////////
    // @@ Member access
    //////////////////////////////////////////////////

    /**
     * Sets the url of the external link.
     *
     * @param url Link url like "http://..."
     */
    public void setExternalURL (String url) {
        this.url = url;
    }

    /**
     * Sets the url of the external link.
     *
     * @param jumpTo Name of the text mark
     */
    public void setInternalURL (String jumpTo) {
        int now = jumpTo.length ();
        int max = RtfBookmark.MAX_BOOKMARK_LENGTH;
        this.url = "#" + jumpTo.substring (0, now > max ? max : now);
        this.url = this.url.replace ('.', RtfBookmark.REPLACE_CHARACTER);
        this.url = this.url.replace (' ', RtfBookmark.REPLACE_CHARACTER);
    }

    /**
     *
     * @return false (always)
     */
    public boolean isEmpty () {
        return false;
    }

    /**
     * @return a text run
     * @throws IOException if not caught
     */
    public RtfTextrun getTextrun() throws IOException {
        RtfTextrun textrun = RtfTextrun.getTextrun(this, writer, null);
        return textrun;
    }
}
