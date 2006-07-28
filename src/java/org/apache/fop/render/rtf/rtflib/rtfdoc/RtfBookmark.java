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
    RtfBookmark (RtfContainer parent, Writer w, String bookmark) throws IOException {
        super (parent, w);

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
