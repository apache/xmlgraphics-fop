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

package org.apache.fop.image;

// Java
import org.w3c.dom.Document;

// FOP
import org.apache.fop.apps.FOFileHandler;

/**
 * This is an implementation for XML-based images such as SVG.
 *
 * @see AbstractFopImage
 * @see FopImage
 */
public class XMLImage extends AbstractFopImage {

    private Document doc;
    private String namespace = "";

    /**
     * @see org.apache.fop.image.AbstractFopImage#AbstractFopImage(FopImage.ImageInfo)
     */
    public XMLImage(FopImage.ImageInfo imgInfo) {
        super(imgInfo);
        if (imgInfo.data instanceof Document) {
            doc = (Document)imgInfo.data;
            loaded = loaded | ORIGINAL_DATA;
        }
        namespace = imgInfo.str;
    }

    /**
     * creates a SAX parser, using the value of org.xml.sax.parser
     * defaulting to org.apache.xerces.parsers.SAXParser
     *
     * @return the created SAX parser
     */
    public static String getParserName() {
        String parserClassName = FOFileHandler.getParserClassName();
        return parserClassName;
    }

    /**
     * Returns the XML document as a DOM document.
     * @return the DOM document
     */
    public Document getDocument() {
        return this.doc;
    }

    /**
     * Returns the namespace of the XML document.
     * @return the namespace
     */
    public String getNameSpace() {
        return this.namespace;
    }
}
