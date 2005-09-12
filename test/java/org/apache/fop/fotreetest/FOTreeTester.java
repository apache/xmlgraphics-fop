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

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.fo.Constants;

import org.apache.fop.fotreetest.ext.TestElementMapping;

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
        
        //Setup identity Transformer 
        Transformer transformer = tfactory.newTransformer();
        Source src = new StreamSource(testFile);
        
        //Setup FOP for area tree rendering
        FOUserAgent ua = new FOUserAgent();
        ua.setBaseURL(testFile.getParentFile().toURL().toString());
        ua.setFOEventHandlerOverride(new DummyFOEventHandler(ua));
        ua.addElementMapping(new TestElementMapping());
        Fop fop = new Fop(Constants.RENDER_XML, ua);
        
        SAXResult fores = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, fores);
        
        List results = collector.getResults();
        if (results.size() > 0) {
            for (int i = 0; i < results.size(); i++) {
                System.out.println(((Exception)results.get(i)).getMessage());
            }
            throw (Exception)results.get(0);
        }
    }

}
