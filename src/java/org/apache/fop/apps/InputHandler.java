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

// SAX
import java.io.File;
import java.net.URL;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


abstract public class InputHandler {


    abstract public InputSource getInputSource();
    abstract public XMLReader getParser() throws FOPException;

    /**
     * create an InputSource from a File
     *
     * @param file the File
     * @return the InputSource created
     */
    static public InputSource fileInputSource(File file) {
        /* this code adapted from James Clark's in XT */
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");
        if (fSep != null && fSep.length() == 1)
            path = path.replace(fSep.charAt(0), '/');
        if (path.length() > 0 && path.charAt(0) != '/')
            path = '/' + path;
        try {
            return new InputSource(new URL("file", null, path).toString());
        } catch (java.net.MalformedURLException e) {
            throw new Error("unexpected MalformedURLException");
        }
    }

    /**
     * creates a SAX parser, using the value of org.xml.sax.parser
     * defaulting to org.apache.xerces.parsers.SAXParser
     *
     * @return the created SAX parser
     */
    protected static XMLReader createParser() throws FOPException {
        String parserClassName = System.getProperty("org.xml.sax.parser");
        if (parserClassName == null) {
            parserClassName = "org.apache.xerces.parsers.SAXParser";
        }
        Fop.logger.config("using SAX parser " + parserClassName);

        try {
            return (XMLReader)Class.forName(parserClassName).newInstance();
        } catch (ClassNotFoundException e) {
            throw new FOPException(e);
        } catch (InstantiationException e) {
            throw new FOPException("Could not instantiate "
                                   + parserClassName, e);
        } catch (IllegalAccessException e) {
            throw new FOPException("Could not access " + parserClassName, e);
        } catch (ClassCastException e) {
            throw new FOPException(parserClassName + " is not a SAX driver",
                                   e);
        }
    }

}

