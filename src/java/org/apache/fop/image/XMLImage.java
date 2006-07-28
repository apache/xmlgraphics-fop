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

package org.apache.fop.image;

// Java
import org.w3c.dom.Document;
import javax.xml.parsers.SAXParserFactory;

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
     * Returns the fully qualified classname of an XML parser for
     * Batik classes that apparently need it (error messages, perhaps)
     * @return an XML parser classname
     */
    public static String getParserName() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            return factory.newSAXParser().getXMLReader().getClass().getName();
        } catch (Exception e) {
            return null;
        }
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
