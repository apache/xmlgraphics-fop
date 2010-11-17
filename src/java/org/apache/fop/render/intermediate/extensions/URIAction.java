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
public class URIAction extends AbstractAction implements DocumentNavigationExtensionConstants {

    private String uri;
    private boolean newWindow;

    /**
     * Creates a new instance.
     * @param uri the target URI
     * @param newWindow true if the link should be opened in a new window
     */
    public URIAction(String uri, boolean newWindow) {
        if (uri == null) {
            throw new NullPointerException("uri must not be null");
        }
        this.uri = uri;
        this.newWindow = newWindow;
    }

    /**
     * Returns the target URI.
     * @return the target URI
     */
    public String getURI() {
        return this.uri;
    }

    /**
     * Indicates whether the link shall be opened in a new window.
     * @return true if a new window shall be opened
     */
    public boolean isNewWindow() {
        return this.newWindow;
    }

    /** {@inheritDoc} */
    public boolean isSame(AbstractAction other) {
        if (other == null) {
            throw new NullPointerException("other must not be null");
        }
        if (!(other instanceof URIAction)) {
            return false;
        }
        URIAction otherAction = (URIAction)other;
        if (!getURI().equals(otherAction.getURI())) {
            return false;
        }
        if (isNewWindow() != otherAction.isNewWindow()) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public String getIdPrefix() {
        return "fop-" + GOTO_URI.getLocalName();
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (hasId()) {
            atts.addAttribute(null, "id", "id", XMLUtil.CDATA, getId());
        }
        atts.addAttribute(null, "uri", "uri", XMLUtil.CDATA, getURI());
        if (isNewWindow()) {
            atts.addAttribute(null, "show-destination", "show-destination", XMLUtil.CDATA, "new");
        }
        handler.startElement(GOTO_URI.getNamespaceURI(),
                GOTO_URI.getLocalName(), GOTO_URI.getQName(), atts);
        handler.endElement(GOTO_URI.getNamespaceURI(),
                GOTO_URI.getLocalName(), GOTO_URI.getQName());
    }

}
