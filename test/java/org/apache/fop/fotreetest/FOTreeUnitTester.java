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

package org.apache.fop.fotreetest;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.xml.sax.XMLReader;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.util.ConsoleEventListenerForTests;


/**
 * Base class for unit-testing the FO tree building code. It performs the necessary setup
 * to parse an FO file and register a proper {@link FOEventHandler}. That handler will be
 * the entry point to test classes from the FObj hierarchy.
 */
public abstract class FOTreeUnitTester extends TestCase {

    private XMLReader foReader;

    private FopFactory fopFactory;

    /**
     * Should be implemented by children testcases for properly setting up the custom
     * FOEventHandler needed to test FObj classes.
     */
    public abstract static class FOEventHandlerFactory {

        /**
         * This method is called by FOTreeUnitTester when creating a {@link Fop} instance.
         * That lets pass to the custom FOEventHandler the proper user agent that will be
         * used by this instance.
         * 
         * @param foUserAgent the user agent needed by the Fop instance that will be used
         * to create the FO tree
         * @return the appropriate FOEventHandler for performing the tests
         */
        public abstract FOEventHandler createFOEventHandler(FOUserAgent foUserAgent);
    }

    public FOTreeUnitTester() throws Exception {
        // Stuff that needs to be set up only once and will be re-used for each test
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        SAXParser parser;
        parser = spf.newSAXParser();
        foReader = parser.getXMLReader();
        fopFactory = FopFactory.newInstance();
    }

    /**
     * Launches FOP on the given FO file.
     * 
     * @param filename path to the test FO file
     * @param factory to create the appropriate FOEventHandler for performing tests 
     */
    public void setUp(String filename, FOEventHandlerFactory factory) throws Exception {
        FOUserAgent ua = fopFactory.newFOUserAgent();
        ua.setFOEventHandlerOverride(factory.createFOEventHandler(ua));
        ua.getEventBroadcaster().addEventListener(
                new ConsoleEventListenerForTests(filename));

        Fop fop = fopFactory.newFop(ua);

        foReader.setContentHandler(fop.getDefaultHandler());
        foReader.setDTDHandler(fop.getDefaultHandler());
        foReader.setErrorHandler(fop.getDefaultHandler());
        foReader.setEntityResolver(fop.getDefaultHandler());

        foReader.parse(new File("test/fotree/unittests/" + filename).toURL().toExternalForm());
    }
}
