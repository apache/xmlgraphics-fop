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

package org.apache.fop.render.intermediate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.fop.accessibility.StructureTree2SAXEventAdapter;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.util.XMLConstants;
import org.apache.fop.util.XMLUtil;

/**
 * Saves structure tree events as SAX events in order to replay them when it's
 * time to stream the structure tree to the output.
 */
final class IFStructureTreeBuilder implements StructureTreeEventHandler {

    static final class IFStructureTreeElement implements StructureTreeElement {

        private final String id;

        IFStructureTreeElement() {
            this.id = null;
        }

        IFStructureTreeElement(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    /** A SAX handler that records events to replay them later. */
    static class SAXEventRecorder extends DefaultHandler {

        private final List<SAXEventRecorder.Event> events = new ArrayList<SAXEventRecorder.Event>();

        private abstract static class Event {
            abstract void replay(ContentHandler handler) throws SAXException;
        }

        private abstract static class Element extends SAXEventRecorder.Event {

            protected final String uri;
            protected final String localName;
            protected final String qName;

            private Element(String uri, String localName, String qName) {
                this.uri = uri;
                this.localName = localName;
                this.qName = qName;
            }
        }

        private static final class StartElement extends SAXEventRecorder.Element {

            private final Attributes attributes;

            private StartElement(String uri, String localName, String qName,
                    Attributes attributes) {
                super(uri, localName, qName);
                this.attributes = attributes;
            }

            @Override
            void replay(ContentHandler handler) throws SAXException {
                handler.startElement(uri, localName, qName, attributes);
            }
        }

        private static final class EndElement extends SAXEventRecorder.Element {

            private EndElement(String uri, String localName, String qName) {
                super(uri, localName, qName);
            }

            @Override
            void replay(ContentHandler handler) throws SAXException {
                handler.endElement(uri, localName, qName);
            }
        }

        private static final class StartPrefixMapping extends SAXEventRecorder.Event {

            private final String prefix;
            private final String uri;

            private StartPrefixMapping(String prefix, String uri) {
                this.prefix = prefix;
                this.uri = uri;
            }

            @Override
            void replay(ContentHandler handler) throws SAXException {
                handler.startPrefixMapping(prefix, uri);
            }
        }

        private static final class EndPrefixMapping extends SAXEventRecorder.Event {

            private final String prefix;

            private EndPrefixMapping(String prefix) {
                this.prefix = prefix;
            }

            @Override
            void replay(ContentHandler handler) throws SAXException {
                handler.endPrefixMapping(prefix);
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            events.add(new StartElement(uri, localName, qName, attributes));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            events.add(new EndElement(uri, localName, qName));
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            events.add(new StartPrefixMapping(prefix, uri));
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            events.add(new EndPrefixMapping(prefix));
        }

        /**
         * Replays the recorded events.
         *
         * @param handler {@code ContentHandler} to replay events on
         */
        public void replay(ContentHandler handler) throws SAXException {
            for (SAXEventRecorder.Event e : events) {
                e.replay(handler);
            }
        }
    }

    private StructureTreeEventHandler delegate;

    private final List<SAXEventRecorder> pageSequenceEventRecorders
            = new ArrayList<SAXEventRecorder>();

    private SAXEventRecorder retrievedMarkersEventRecorder;

    private int idCounter;

    /**
     * Replay SAX events for a page sequence.
     * @param handler The handler that receives SAX events
     * @param pageSequenceIndex The index of the page sequence
     * @throws SAXException
     */
    public void replayEventsForPageSequence(ContentHandler handler,
            int pageSequenceIndex) throws SAXException {
        pageSequenceEventRecorders.get(pageSequenceIndex).replay(handler);
    }

    public void replayEventsForRetrievedMarkers(ContentHandler handler) throws SAXException {
        if (!retrievedMarkersEventRecorder.events.isEmpty()) {
            delegate = StructureTree2SAXEventAdapter.newInstance(handler);
            delegate.startPageSequence(null, null);
            retrievedMarkersEventRecorder.replay(handler);
            delegate.endPageSequence();
            prepareRetrievedMarkersEventRecorder();
        }
    }

    public void startPageSequence(Locale locale, String role) {
        SAXEventRecorder eventRecorder = new SAXEventRecorder();
        pageSequenceEventRecorders.add(eventRecorder);
        delegate = StructureTree2SAXEventAdapter.newInstance(eventRecorder);
        delegate.startPageSequence(locale, role);
    }

    public void endPageSequence() {
         delegate.endPageSequence();
         prepareRetrievedMarkersEventRecorder();
    }

    private void prepareRetrievedMarkersEventRecorder() {
        SAXEventRecorder eventRecorder = new SAXEventRecorder();
        retrievedMarkersEventRecorder = eventRecorder;
        delegate = StructureTree2SAXEventAdapter.newInstance(eventRecorder);
    }

    public StructureTreeElement startNode(String name, Attributes attributes, StructureTreeElement parent) {
        if (parent != null) {
            attributes = addParentAttribute(new AttributesImpl(attributes), parent);
        }
        delegate.startNode(name, attributes, null);
        return new IFStructureTreeElement();
    }

    private AttributesImpl addParentAttribute(AttributesImpl attributes, StructureTreeElement parent) {
        if (parent != null) {
            attributes.addAttribute(InternalElementMapping.URI,
                    InternalElementMapping.STRUCT_REF,
                    InternalElementMapping.STANDARD_PREFIX + ":" + InternalElementMapping.STRUCT_REF,
                    XMLConstants.CDATA,
                    ((IFStructureTreeElement) parent).getId());
        }
        return attributes;
    }

    public void endNode(String name) {
        delegate.endNode(name);
    }

    public StructureTreeElement startImageNode(String name, Attributes attributes, StructureTreeElement parent) {
        String id = getNextID();
        AttributesImpl atts = addIDAttribute(attributes, id);
        addParentAttribute(atts, parent);
        delegate.startImageNode(name, atts, null);
        return new IFStructureTreeElement(id);
    }

    public StructureTreeElement startReferencedNode(String name, Attributes attributes, StructureTreeElement parent) {
        String id = getNextID();
        AttributesImpl atts = addIDAttribute(attributes, id);
        addParentAttribute(atts, parent);
        delegate.startReferencedNode(name, atts, null);
        return new IFStructureTreeElement(id);
    }

    private String getNextID() {
        return Integer.toHexString(idCounter++);
    }

    private AttributesImpl addIDAttribute(Attributes attributes, String id) {
        AttributesImpl atts = new AttributesImpl(attributes);
        atts.addAttribute(InternalElementMapping.URI,
                InternalElementMapping.STRUCT_ID,
                InternalElementMapping.STANDARD_PREFIX + ":" + InternalElementMapping.STRUCT_ID,
                XMLUtil.CDATA,
                id);
        return atts;
    }
}
