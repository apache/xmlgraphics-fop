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

package org.apache.fop.fo.extensions.svg;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.util.XMLResourceDescriptor;

import org.apache.fop.svg.FOPSAXSVGDocumentFactory;
import org.apache.fop.util.ContentHandlerFactory;

/**
 * ContentHandlerFactory which constructs ContentHandlers that build SVG DOM Documents.
 */
public class SVGDOMContentHandlerFactory implements ContentHandlerFactory {

    /**
     * Default Constructor.
     */
    public SVGDOMContentHandlerFactory() {
        //nop
    }

    /** {@inheritDoc} */
    public String[] getSupportedNamespaces() {
        return new String[] {SVGDOMImplementation.SVG_NAMESPACE_URI};
    }

    /** {@inheritDoc} */
    public ContentHandler createContentHandler() throws SAXException {
        return new Handler();
    }

    private static class Handler extends FOPSAXSVGDocumentFactory
                implements ContentHandlerFactory.ObjectSource {

        private ObjectBuiltListener obListener;

        public Handler() throws SAXException {
            super(XMLResourceDescriptor.getXMLParserClassName());
        }

        /** {@inheritDoc} */
        public Object getObject() {
            return getDocument();
        }

        /** {@inheritDoc} */
        public void setObjectBuiltListener(ObjectBuiltListener listener) {
            this.obListener = listener;
        }

        /** {@inheritDoc} */
        public void endDocument() throws SAXException {
            super.endDocument();
            if (obListener != null) {
                obListener.notifyObjectBuilt(getObject());
            }
        }

    }

}
