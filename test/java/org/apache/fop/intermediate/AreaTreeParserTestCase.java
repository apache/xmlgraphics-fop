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
import java.util.Collection;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.AreaTreeParser;
import org.apache.fop.area.RenderPagesModel;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.layoutengine.LayoutEngineTestUtils;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.xml.XMLRenderer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;

/**
 * Tests the area tree parser.
 */
@RunWith(Parameterized.class)
public class AreaTreeParserTestCase extends AbstractIntermediateTestCase {

    /**
     * Creates the parameters for this test.
     *
     * @return the list of file arrays populated with test files
     * @throws IOException if an I/O error occurs while reading the test file
     */
    @Parameters
    public static Collection<File[]> getParameters() throws IOException {
        return LayoutEngineTestUtils.getTestFiles();
    }
    /**
     * Constructor for the test suite that is used for each test file.
     * @param testFile the test file to run
     * @throws IOException
     * @throws IOException if an I/O error occurs while loading the test case
     */
    public AreaTreeParserTestCase(File testFile) throws IOException {
        super(testFile);
    }

    /** {@inheritDoc} */
    protected String getIntermediateFileExtension() {
        return ".at.xml";
    }

    /** {@inheritDoc} */
    protected Document buildIntermediateDocument(Templates templates)
                throws Exception {
        Transformer transformer = templates.newTransformer();
        setErrorListener(transformer);

        //Set up XMLRenderer to render to a DOM
        TransformerHandler handler = testAssistant.getTransformerFactory().newTransformerHandler();
        DOMResult domResult = new DOMResult();
        handler.setResult(domResult);

        FOUserAgent userAgent = createUserAgent();

        //Create an instance of the target renderer so the XMLRenderer can use its font setup
        Renderer targetRenderer = userAgent.getRendererFactory().createRenderer(
                userAgent, getTargetMIME());

        XMLRenderer renderer = new XMLRenderer();
        renderer.mimicRenderer(targetRenderer);
        renderer.setContentHandler(handler);
        renderer.setUserAgent(userAgent);

        userAgent.setRendererOverride(renderer);

        Fop fop = fopFactory.newFop(MimeConstants.MIME_FOP_AREA_TREE, userAgent);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(new DOMSource(testDoc), res);

        return (Document)domResult.getNode();
    }

    /** {@inheritDoc} */
    protected void parseAndRender(Source src, OutputStream out) throws Exception {
        AreaTreeParser parser = new AreaTreeParser();

        FOUserAgent userAgent = createUserAgent();
        FontInfo fontInfo = new FontInfo();
        AreaTreeModel treeModel = new RenderPagesModel(userAgent,
                getTargetMIME(), fontInfo, out);
        parser.parse(src, treeModel, userAgent);
        treeModel.endDocument();
    }

    /** {@inheritDoc} */
    protected Document parseAndRenderToIntermediateFormat(Source src) throws Exception {
        AreaTreeParser parser = new AreaTreeParser();

        //Set up XMLRenderer to render to a DOM
        TransformerHandler handler = testAssistant.getTransformerFactory().newTransformerHandler();
        DOMResult domResult = new DOMResult();
        handler.setResult(domResult);
        XMLRenderer renderer = new XMLRenderer();
        renderer.setContentHandler(handler);

        FOUserAgent userAgent = createUserAgent();
        userAgent.setRendererOverride(renderer);
        renderer.setUserAgent(userAgent);

        FontInfo fontInfo = new FontInfo();
        AreaTreeModel treeModel = new RenderPagesModel(userAgent,
                MimeConstants.MIME_FOP_AREA_TREE, fontInfo, null);
        parser.parse(src, treeModel, userAgent);
        treeModel.endDocument();

        return (Document)domResult.getNode();
    }

    @Override
    @Test
    public void runTest() throws Exception {
        try {
            testParserToIntermediateFormat();
            testParserToPDF();
        } catch (Exception e) {
            org.apache.commons.logging.LogFactory.getLog(this.getClass()).error(
                    "Error on " + testFile.getName());
            throw e;
        }
    }

}
