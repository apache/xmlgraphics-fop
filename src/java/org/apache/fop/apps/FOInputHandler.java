/*
 * $Id$
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */

package org.apache.fop.apps;

// Imported SAX classes
import java.io.File;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Manages input if it is an xsl:fo file
 */
public class FOInputHandler extends InputHandler {

    File fofile;
    public FOInputHandler(File fofile) {
        this.fofile = fofile;
    }

    public InputSource getInputSource() {
        return super.fileInputSource(fofile);
    }

    public XMLReader getParser() throws FOPException {
        return super.createParser();
    }

}

