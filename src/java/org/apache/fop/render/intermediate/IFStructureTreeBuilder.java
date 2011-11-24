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
import org.xml.sax.helpers.DefaultHandler;

import org.apache.fop.accessibility.StructureTree2SAXEventAdapter;
import org.apache.fop.accessibility.StructureTreeEventHandler;

/**
 * Saves structure tree events as SAX events in order to replay them when it's
 * time to stream the structure tree to the output.
 */
final class IFStructureTreeBuilder implements StructureTreeEventHandler {

    private StructureTreeEventHandler delegate;

    private final List<SAXEventRecorder> pageSequenceEventRecorders = new ArrayList<SAXEventRecorder>();

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

    /** {@inheritDoc} */
    public void startPageSequence(Locale locale) {
        SAXEventRecorder eventRecorder = new SAXEventRecorder();
        pageSequenceEventRecorders.add(eventRecorder);
        delegate = StructureTree2SAXEventAdapter.newInstance(eventRecorder);
        delegate.startPageSequence(locale);
    }

    /** {@inheritDoc} */
    public void endPageSequence() {
         delegate.endPageSequence();
    }

    /** {@inheritDoc} */
    public void startNode(String name, Attributes attributes) {
        delegate.startNode(name, attributes);
    }

    /** {@inheritDoc} */
    public void endNode(String name) {
        delegate.endNode(name);
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

            protected final String prefix;
            protected final String uri;

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

            protected final String prefix;

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
        };

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            events.add(new EndElement(uri, localName, qName));
        };

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            events.add(new StartPrefixMapping(prefix, uri));
        };

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            events.add(new EndPrefixMapping(prefix));
        };

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
}
