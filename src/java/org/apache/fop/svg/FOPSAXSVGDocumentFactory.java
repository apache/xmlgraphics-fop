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
 
package org.apache.fop.svg;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;

/**
 * This is a special subclass to allow setting a special EntityResolver.
 */
public class FOPSAXSVGDocumentFactory extends SAXSVGDocumentFactory {

    private EntityResolver additionalResolver;

    /**
     * Creates a new DocumentFactory object.
     * @param parser The SAX2 parser classname.
     */
    public FOPSAXSVGDocumentFactory(String parser) {
        super(parser);
    }
    
    /**
     * Sets an additional entity resolver. It will be used before the default
     * entity resolving.
     * @param resolver Additional resolver
     */
    public void setAdditionalEntityResolver(EntityResolver resolver) {
        this.additionalResolver = resolver;
    }
    
    /**
     * @see org.xml.sax.EntityResolver#resolveEntity(String, String)
     */
    public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException {
        if (this.additionalResolver != null) {
            try {
                InputSource result = this.additionalResolver.resolveEntity(publicId, systemId);
                if (result != null) {
                    return result;
                }
            } catch (IOException ioe) {
                /**@todo Batik's SAXSVGDocumentFactory should throw IOException,
                 * so we don't have to handle it here. */
                throw new SAXException(ioe);
            }
        }
        return super.resolveEntity(publicId, systemId);
    }

}
