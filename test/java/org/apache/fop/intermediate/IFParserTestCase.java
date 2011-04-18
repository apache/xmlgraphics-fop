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

package org.apache.fop.intermediate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFParser;
import org.apache.fop.render.intermediate.IFSerializer;

/**
 * Tests the intermediate format parser.
 */
public class IFParserTestCase extends AbstractIFTestCase {

    /**
     * Constructor for the test suite that is used for each test file.
     * @param testFile the test file to run
     * @throws IOException if an I/O error occurs while loading the test case
     */
    public IFParserTestCase(File testFile) throws IOException {
        super(testFile);
    }

    /** {@inheritDoc} */
    @Override
    protected void parseAndRender(Source src, OutputStream out) throws Exception {
        IFParser parser = new IFParser();

        FOUserAgent userAgent = createUserAgent();

        IFDocumentHandler documentHandler = userAgent.getRendererFactory().createDocumentHandler(
                userAgent, getTargetMIME());
        documentHandler.setResult(new StreamResult(out));
        documentHandler.setDefaultFontInfo(new FontInfo());
        parser.parse(src, documentHandler, userAgent);
    }

    /** {@inheritDoc} */
    @Override
    protected Document parseAndRenderToIntermediateFormat(Source src) throws Exception {
        IFParser parser = new IFParser();

        FOUserAgent userAgent = createUserAgent();

        IFSerializer serializer = new IFSerializer();
        serializer.setContext(new IFContext(userAgent));
        DOMResult domResult = new DOMResult();
        serializer.setResult(domResult);

        parser.parse(src, serializer, userAgent);

        return (Document)domResult.getNode();
    }

    /** {@inheritDoc} */
    @Override
    public void runTest() throws Exception {
        testParserToIntermediateFormat();
        testParserToPDF();
    }

}
