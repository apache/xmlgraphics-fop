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

package org.apache.fop.accessibility;

import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.fo.FOElementMapping;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.render.intermediate.IFConstants;
import org.apache.fop.util.XMLConstants;

/**
 * Converts structure tree events to SAX events.
 */
public final class StructureTree2SAXEventAdapter implements StructureTreeEventHandler {

    private final ContentHandler contentHandler;

    private StructureTree2SAXEventAdapter(ContentHandler currentContentHandler) {
        this.contentHandler = currentContentHandler;
    }

    /**
     * Factory method that creates a new instance.
     * @param contentHandler The handler that receives SAX events
     * @return -
     */
    public static StructureTreeEventHandler newInstance(ContentHandler contentHandler) {
        return new StructureTree2SAXEventAdapter(contentHandler);
    }

    /** {@inheritDoc} */
    public void startPageSequence(Locale locale, String role) {
        try {
            AttributesImpl attributes = new AttributesImpl();
            if (role != null) {
                attributes.addAttribute("", "type", "type", XMLConstants.CDATA, role);
            }
            contentHandler.startPrefixMapping(
                    InternalElementMapping.STANDARD_PREFIX, InternalElementMapping.URI);
            contentHandler.startPrefixMapping(
                    ExtensionElementMapping.STANDARD_PREFIX, ExtensionElementMapping.URI);
            contentHandler.startElement(IFConstants.NAMESPACE,
                    IFConstants.EL_STRUCTURE_TREE, IFConstants.EL_STRUCTURE_TREE,
                    attributes);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    public void endPageSequence() {
        try {
            contentHandler.endElement(IFConstants.NAMESPACE, IFConstants.EL_STRUCTURE_TREE,
                    IFConstants.EL_STRUCTURE_TREE);
            contentHandler.endPrefixMapping(
                    ExtensionElementMapping.STANDARD_PREFIX);
            contentHandler.endPrefixMapping(
                    InternalElementMapping.STANDARD_PREFIX);

        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    public StructureTreeElement startNode(String name, Attributes attributes, StructureTreeElement parent) {
        try {
            if (name.equals("#PCDATA")) {
                name = "marked-content";
                contentHandler.startElement(IFConstants.NAMESPACE, name,
                        name, attributes);
            } else {
                contentHandler.startElement(FOElementMapping.URI, name,
                        FOElementMapping.STANDARD_PREFIX + ":" + name,
                        attributes);
            }
            return null;
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    public void endNode(String name) {
        try {
            contentHandler.endElement(FOElementMapping.URI, name,
                    FOElementMapping.STANDARD_PREFIX + ":" + name);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    public StructureTreeElement startImageNode(String name, Attributes attributes, StructureTreeElement parent) {
        return startNode(name, attributes, null);
    }

    /** {@inheritDoc} */
    public StructureTreeElement startReferencedNode(String name, Attributes attributes, StructureTreeElement parent) {
        return startNode(name, attributes, null);
    }

}
