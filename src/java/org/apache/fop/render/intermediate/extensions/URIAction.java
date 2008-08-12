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

package org.apache.fop.render.intermediate.extensions;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.util.XMLUtil;

/**
 * Action class which represents a "URI" action, i.e. an action that will call up an external
 * resource identified by a URI.
 */
public class URIAction extends AbstractAction implements BookmarkExtensionConstants {

    private String uri;

    /**
     * Creates a new instance.
     * @param uri the target URI
     */
    public URIAction(String uri) {
        this.uri = uri;
    }

    /**
     * Returns the target URI.
     * @return the target URI
     */
    public String getURI() {
        return this.uri;
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(null, "uri", "uri", XMLUtil.CDATA, getURI());
        handler.startElement(GOTO_URI.getNamespaceURI(),
                GOTO_URI.getLocalName(), GOTO_URI.getQName(), atts);
        handler.endElement(GOTO_URI.getNamespaceURI(),
                GOTO_URI.getLocalName(), GOTO_URI.getQName());
    }

}
