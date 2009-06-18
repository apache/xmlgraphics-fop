/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

import org.apache.fop.fotreetest.ext.TestElementMapping;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Test driver class for FO tree tests.
 */
public class FOTreeTester {

    private SAXTransformerFactory tfactory 
            = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    /**
     * Runs a test.
     * @param testFile the test file.
     * @throws Exception if a test or FOP itself fails
     */
    public void runTest(File testFile) throws Exception {
        ResultCollector collector = ResultCollector.getInstance();
        collector.reset();
        
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        SAXParser parser = spf.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        
        //Setup FOP for area tree rendering
        FopFactory factory = FopFactory.newInstance();
        factory.addElementMapping(new TestElementMapping());
        
        FOUserAgent ua = factory.newFOUserAgent();
        ua.setBaseURL(testFile.getParentFile().toURL().toString());
        ua.setFOEventHandlerOverride(new DummyFOEventHandler(ua));

        //Used to set values in the user agent through processing instructions
        reader = new PIListener(reader, ua);
        
        Fop fop = new Fop(MimeConstants.MIME_FOP_AREA_TREE, ua);
        
        reader.setContentHandler(fop.getDefaultHandler());
        reader.setDTDHandler(fop.getDefaultHandler());
        reader.setErrorHandler(fop.getDefaultHandler());
        reader.setEntityResolver(fop.getDefaultHandler());
        reader.parse(testFile.toURL().toExternalForm());
        
        List results = collector.getResults();
        if (results.size() > 0) {
            for (int i = 0; i < results.size(); i++) {
                System.out.println(((Exception)results.get(i)).getMessage());
            }
            throw (Exception)results.get(0);
        }
    }

    private class PIListener extends XMLFilterImpl {
        
        private FOUserAgent userAgent;
        
        public PIListener(XMLReader parent, FOUserAgent userAgent) {
            super(parent);
            this.userAgent = userAgent;
        }

        /** @see org.xml.sax.helpers.XMLFilterImpl */
        public void processingInstruction(String target, String data) throws SAXException {
            if ("fop-useragent-break-indent-inheritance".equals(target)) {
                userAgent.getFactory().setBreakIndentInheritanceOnReferenceAreaBoundary(
                        Boolean.valueOf(data).booleanValue());
            }
            super.processingInstruction(target, data);
        }
        
    }
    
}
