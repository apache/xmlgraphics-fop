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

import java.io.Writer;
import java.io.IOException;

/**
 * RTF Bookmark.
 * Create an RTF bookmark as a child of given container with default attributes.
 * This class belongs to the "id" attribute processing.
 * @author <a href="mailto:a.putz@skynamics.com">Andreas Putz</a>
 */
public class RtfBookmark extends RtfElement {
    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////

    /** Name of the bokkmark */
    private String bookmark = null;
    /** Word 2000 supports a length of 40 characters only */
    public static final int MAX_BOOKMARK_LENGTH = 40;
    /** Word 2000 converts '.' in bookmarks to "_", thats why we control this replacement. */
    public static final char REPLACE_CHARACTER = '_';


    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Constructor.
     *
     * @param parent a <code>RtfBookmarkContainer</code> value
     * @param writer a <code>Writer</code> value
     * @param bookmark Name of the bookmark
     */
    RtfBookmark (IRtfBookmarkContainer parent, Writer w, String bookmark) throws IOException {
        super ((RtfContainer) parent, w);

        int now = bookmark.length ();

        this.bookmark = bookmark.substring (0,
                now < MAX_BOOKMARK_LENGTH ? now : MAX_BOOKMARK_LENGTH);
        this.bookmark = this.bookmark.replace ('.', REPLACE_CHARACTER);
        this.bookmark = this.bookmark.replace (' ', REPLACE_CHARACTER);
    }


    //////////////////////////////////////////////////
    // @@ RtfElement implementation
    //////////////////////////////////////////////////

    /**
     * Is called before writing the Rtf content.
     *
     * @throws IOException On Error
     */
    public void writeRtfPrefix () throws IOException {
        startBookmark ();
    }

    /**
     * Writes the RTF content to m_writer.
     *
     * @exception IOException On error
     */
    public void writeRtfContent () throws IOException {
//        this.getRtfFile ().getLog ().logInfo ("Write bookmark '" + bookmark + "'.");
        // No content to write
    }

    /**
     * Is called after writing the Rtf content.
     *
     * @throws IOException On Error
     */
    public void writeRtfSuffix () throws IOException {
        endBookmark ();
    }


    //////////////////////////////////////////////////
    // @@ Private methods
    //////////////////////////////////////////////////

    /**
     * Writes RTF content to begin the bookmark.
     *
     * @throws IOException On error
     */
    private void startBookmark () throws IOException {

        // {\*\bkmkstart test}
        writeRtfBookmark ("bkmkstart");
    }

    /**
     * Writes RTF content to close the bookmark.
     *
     * @throws IOException On error
     */
    private void endBookmark () throws IOException {

        // {\*\bkmkend test}
        writeRtfBookmark ("bkmkend");
    }

    /**
     * Writes the rtf bookmark.
     *
     * @param tag Begin or close tag
     *
     * @throws IOException On error
     */
    private void writeRtfBookmark (String tag) throws IOException {
        if (bookmark == null) {
            return;

        }

        this.writeGroupMark (true);

        //changed. Now using writeStarControlWord
        this.writeStarControlWord (tag);

        writer.write (bookmark);
        this.writeGroupMark (false);
    }

        /**
         * @return true if this element would generate no "useful" RTF content
         */
        public boolean isEmpty() {
            return bookmark == null || bookmark.trim().length() == 0;
        }
}
