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

package org.apache.fop.events.model;

import java.util.Stack;
import java.util.concurrent.*;
import java.util.function.Function;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.utils.DefaultErrorListener;

/**
 * This is a parser for the event model XML.
 */
public final class EventModelParser {

    private EventModelParser() {
    }

    /** Logger instance */
    private static final Log LOG = LogFactory.getLog(EventModelParser.class);

    private static SAXTransformerFactory tFactory
        = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    abstract static class EventModelParserWorker {
        public abstract EventModel parse(Source src) throws TransformerException;
    }

    private static EventModelParserWorker init() {
        String javaVersion =  System.getProperty("java.version");
        if ( javaVersion.compareTo( "25" ) < 0 ) {
            // preserve the classic fop parsing behaviour on java 24 or less
            return new EventModelParserWorker() {
                @Override
                public EventModel parse(Source src) throws TransformerException {
                    Transformer transformer = tFactory.newTransformer();
                    transformer.setErrorListener(new DefaultErrorListener(LOG));
                    EventModel model = new EventModel();
                    SAXResult res = new SAXResult(getContentHandler(model));
                    transformer.transform(src, res);
                    return model;
                }
            };
        } else {
            return new EventModelParserWorker() {
                // new parsing behaviour on java 25+ (independent thread)
                @Override
                public EventModel parse(Source src) throws TransformerException {
                    EventModel model = new EventModel();
                    // Create a single-thread executor for this parsing operation
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    try ( AutoCloseable executorAc = () -> executor.shutdown() ) {
                        // Submit the parsing task and wait for completion
                        Future<Void> parsingTask = executor.submit(() -> {
                            try {
                                Transformer transformer = tFactory.newTransformer();
                                transformer.setErrorListener(new DefaultErrorListener(LOG));
                                SAXResult res = new SAXResult(getContentHandler(model));
                                transformer.transform(src, res);
                                return null;
                            } catch (TransformerException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        // Block until parsing is complete
                        parsingTask.get();
                        return model;
                    } catch (ExecutionException e) {
                        if (e.getCause() instanceof RuntimeException &&
                                e.getCause().getCause() instanceof TransformerException) {
                            throw (TransformerException) e.getCause().getCause();
                        }
                        throw new TransformerException(e.getCause());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new TransformerException("Parsing was interrupted", e);
                    } catch (Exception e) {
                        throw new TransformerException("Parsing generic error", e);
                    }
                }
            };
        }
    }

    private static final EventModelParserWorker PARSER_WORKER = init();

    /**
     * Parses an event model file into an EventModel instance.
     * @param src the Source instance pointing to the XML file
     * @return the created event model structure
     * @throws TransformerException if an error occurs while parsing the XML file
     */
    public static EventModel parse(Source src) throws TransformerException {
        return PARSER_WORKER.parse(src);
    }

    /**
     * Creates a new ContentHandler instance that you can send the event model XML to. The parsed
     * content is accumulated in the model structure.
     * @param model the EventModel
     * @return the ContentHandler instance to receive the SAX stream from the XML file
     */
    public static ContentHandler getContentHandler(EventModel model) {
        return new Handler(model);
    }

    private static class Handler extends DefaultHandler {

        private EventModel model;
        private Stack objectStack = new Stack();

        public Handler(EventModel model) {
            this.model = model;
        }

        /** {@inheritDoc} */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {
            try {
                if ("event-model".equals(localName)) {
                    if (objectStack.size() > 0) {
                        throw new SAXException("event-model must be the root element");
                    }
                    objectStack.push(model);
                } else if ("producer".equals(localName)) {
                    EventProducerModel producer = new EventProducerModel(
                            attributes.getValue("name"));
                    EventModel parent = (EventModel)objectStack.peek();
                    parent.addProducer(producer);
                    objectStack.push(producer);
                } else if ("method".equals(localName)) {
                    EventSeverity severity = EventSeverity.valueOf(attributes.getValue("severity"));
                    String ex = attributes.getValue("exception");
                    EventMethodModel method = new EventMethodModel(
                            attributes.getValue("name"), severity);
                    if (ex != null && ex.length() > 0) {
                        method.setExceptionClass(ex);
                    }
                    EventProducerModel parent = (EventProducerModel)objectStack.peek();
                    parent.addMethod(method);
                    objectStack.push(method);
                } else if ("parameter".equals(localName)) {
                    String className = attributes.getValue("type");
                    Class type;
                    try {
                        type = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new SAXException("Could not find Class for: " + className, e);
                    }
                    String name = attributes.getValue("name");
                    EventMethodModel parent = (EventMethodModel)objectStack.peek();
                    objectStack.push(parent.addParameter(type, name));
                } else {
                    throw new SAXException("Invalid element: " + qName);
                }
            } catch (ClassCastException cce) {
                throw new SAXException("XML format error: " + qName, cce);
            }
        }

        /** {@inheritDoc} */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            objectStack.pop();
        }

    }

}
