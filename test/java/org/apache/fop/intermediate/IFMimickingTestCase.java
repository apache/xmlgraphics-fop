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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventListener;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFSerializer;

/**
 * This test checks the correct mimicking of a different output format.
 */
public class IFMimickingTestCase extends TestCase {

    private FopFactory fopFactory;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        fopFactory = FopFactory.newInstance();
        File configFile = new File("test/test-no-xml-metrics.xconf");
        fopFactory.setUserConfig(configFile);
    }

    /**
     * Tests IF document handler mimicking with PDF output.
     * @throws Exception if an error occurs
     */
    public void testMimickingPDF() throws Exception {
        doTestMimicking(MimeConstants.MIME_PDF);
    }

    /**
     * Tests IF document handler mimicking with PostScript output.
     * @throws Exception if an error occurs
     */
    public void testMimickingPS() throws Exception {
        doTestMimicking(MimeConstants.MIME_POSTSCRIPT);
    }

    /**
     * Tests IF document handler mimicking with TIFF output.
     * @throws Exception if an error occurs
     */
    public void testMimickingTIFF() throws Exception {
        doTestMimicking(MimeConstants.MIME_TIFF);
    }

    private void doTestMimicking(String mime) throws FOPException, IFException,
            TransformerException {
        //Set up XMLRenderer to render to a DOM
        DOMResult domResult = new DOMResult();

        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        userAgent.getEventBroadcaster().addEventListener(new EventListener() {

            public void processEvent(Event event) {
                if (event.getEventGroupID().equals("org.apache.fop.fonts.FontEventAdapter")) {
                    fail("There must be no font-related event! Got: "
                            + EventFormatter.format(event));
                }
            }

        });

        //Create an instance of the target renderer so the XMLRenderer can use its font setup
        IFDocumentHandler targetHandler = userAgent.getRendererFactory().createDocumentHandler(
                userAgent, mime);

        //Setup painter
        IFSerializer serializer = new IFSerializer();
        serializer.setContext(new IFContext(userAgent));
        serializer.mimicDocumentHandler(targetHandler);
        serializer.setResult(domResult);

        userAgent.setDocumentHandlerOverride(serializer);

        Fop fop = fopFactory.newFop(userAgent);

        //minimal-pdf-a.fo uses the Gladiator font so is an ideal FO file for this test:
        StreamSource src = new StreamSource(new File("test/xml/pdf-a/minimal-pdf-a.fo"));

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        setErrorListener(transformer);

        transformer.transform(src, new SAXResult(fop.getDefaultHandler()));
    }

    /**
     * Sets an error listener which doesn't swallow errors like Xalan's default one.
     * @param transformer the transformer to set the error listener on
     */
    protected void setErrorListener(Transformer transformer) {
        transformer.setErrorListener(new ErrorListener() {

            public void error(TransformerException exception) throws TransformerException {
                throw exception;
            }

            public void fatalError(TransformerException exception) throws TransformerException {
                throw exception;
            }

            public void warning(TransformerException exception) throws TransformerException {
                //ignore
            }

        });
    }

}
