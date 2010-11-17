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

package org.apache.fop.render.xml;

import org.apache.fop.render.Renderer;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;
import org.apache.fop.util.DOM2SAX;

import org.xml.sax.ContentHandler;

/**
 * XML handler for the XML renderer.
 */
public class XMLXMLHandler implements XMLHandler {

    /** Key for getting the TransformerHandler from the RendererContext */
    public static final String HANDLER = "handler";

    /** {@inheritDoc} */
    public void handleXML(RendererContext context,
                org.w3c.dom.Document doc, String ns) throws Exception {
        ContentHandler handler = (ContentHandler) context.getProperty(HANDLER);

        new DOM2SAX(handler).writeDocument(doc, true);
    }

    /** {@inheritDoc} */
    public boolean supportsRenderer(Renderer renderer) {
        return (renderer instanceof XMLRenderer);
    }

    /** {@inheritDoc} */
    public String getNameSpace() {
        return null; //Handle all XML content
    }

}

