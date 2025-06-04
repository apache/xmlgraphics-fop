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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    private String altText;

    /**
     * Creates a new instance.
     * @param uri the target URI
     * @param newWindow true if the link should be opened in a new window
     */
    public URIAction(String uri, boolean newWindow, String altText) {
        if (uri == null) {
            throw new NullPointerException("uri must not be null");
        }
        this.uri = uri;
        this.newWindow = newWindow;
        this.altText = altText;
        setID(createID(getIDPrefix(), uri + newWindow));
    }

    private String createID(String idPrefix, String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(url.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : thedigest) {
                hex.append(String.format("%02x", b));
            }

            return idPrefix + hex;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
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
        if (getStructureTreeElement() != null) {
            return getStructureTreeElement().equals(other.getStructureTreeElement());
        }
        return true;
    }

    /** {@inheritDoc} */
    public String getIDPrefix() {
        return "fop-" + GOTO_URI.getLocalName();
    }

    /** {@inheritDoc} */
    public void toSAX(ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        if (hasID()) {
            atts.addAttribute("", "id", "id", XMLUtil.CDATA, getID());
        }
        atts.addAttribute("", "uri", "uri", XMLUtil.CDATA, getURI());
        if (altText != null && !altText.isEmpty()) {
            atts.addAttribute("", "alt-text", "alt-text", XMLUtil.CDATA, altText);
        }
        if (isNewWindow()) {
            atts.addAttribute("", "show-destination", "show-destination", XMLUtil.CDATA, "new");
        }
        handler.startElement(GOTO_URI.getNamespaceURI(),
                GOTO_URI.getLocalName(), GOTO_URI.getQName(), atts);
        handler.endElement(GOTO_URI.getNamespaceURI(),
                GOTO_URI.getLocalName(), GOTO_URI.getQName());
    }

    public String getAltText() {
        return altText;
    }
}
