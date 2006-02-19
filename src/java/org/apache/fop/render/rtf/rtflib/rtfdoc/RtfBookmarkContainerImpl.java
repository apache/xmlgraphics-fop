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
 * RTF Bookmark container implementation.
 * Nearly all containers or elements can have a bookmark, that is why the bookmark container is
 * implemented as stand alone.
 * @author <a href="mailto:a.putz@skynamics.com">Andreas Putz</a>
 */
public class RtfBookmarkContainerImpl extends RtfContainer implements IRtfBookmarkContainer {
    //////////////////////////////////////////////////
    // @@ Members
    //////////////////////////////////////////////////

    /** Rtf bookmark */
    private RtfBookmark mBookmark = null;


    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Constructor.
     * Create an RTF container as a child of given container.
     *
     * @param parent The parent container
     * @param w Writer
     *
     * @exception IOException On error
     */
    RtfBookmarkContainerImpl (RtfContainer parent, Writer w) throws IOException {
        super (parent, w, null);
    }

    /**
     * Constructor.
     * Create an RTF container as a child of given container.
     *
     * @param parent The parent container
     * @param w Writer
     * @param attr Rtf attributes
     *
     * @exception IOException On error
     */
    RtfBookmarkContainerImpl (RtfContainer parent, Writer w, RtfAttributes attr) throws IOException
    {
        super (parent, w, attr);
    }


    //////////////////////////////////////////////////
    // @@ Public methods
    //////////////////////////////////////////////////

    /**
     * Create a new RTF bookmark.
     *
     * @param bookmark Name of the bookmark
     *
     * @return RTF bookmark
     *
     * @throws IOException On eror
     */
    public RtfBookmark newBookmark (String bookmark) throws IOException {
        if (mBookmark != null) {
            mBookmark.close ();
        }

        mBookmark = new RtfBookmark (this, writer, bookmark);

        return mBookmark;
    }
}