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

package org.apache.fop.accessibility.fo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import org.xml.sax.SAXException;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFSerializer;

/**
 * Test for reproducing a NullPointerException occurring with activated PDF/UA and when an image
 * is marked as an artifact.
 * @see FOP-2646
 */
public class ArtifactImageBugTestCase {

    @Test
    public void testMarkerStateTrackingBug() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        formatFO(needResource("artifact-image-npe.fo"), out, MimeConstants.MIME_PDF);
        //checkPDF(out);
    }

    private void formatFO(URL foFile, ByteArrayOutputStream out, String mimeFopIf)
        throws IOException, SAXException, TransformerException {
        FopFactory fopFactory = getFopFactory();
        FOUserAgent userAgent = fopFactory.newFOUserAgent();

        if (mimeFopIf.equals(MimeConstants.MIME_FOP_IF)) {
            IFSerializer serializer = new IFSerializer(new IFContext(userAgent));
            IFDocumentHandler targetHandler
                    = userAgent.getRendererFactory().createDocumentHandler(userAgent, MimeConstants.MIME_PDF);
            serializer.mimicDocumentHandler(targetHandler);
            userAgent.setDocumentHandlerOverride(serializer);
        }

        Fop fop = fopFactory.newFop(mimeFopIf, userAgent, out);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(foFile.openStream());
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
    }

    private FopFactory getFopFactory() throws IOException, SAXException {
        return FopFactory.newInstance(new File(".").toURI(),
                needResource("/org/apache/fop/pdf/PDFUA.xconf").openStream());
    }

    private URL needResource(String resourceName) {
        return needResource(getClass(), resourceName);
    }

    private URL needResource(Class contextClass, String resourceName) {
        URL url = contextClass.getResource(resourceName);
        if (url == null) {
            throw new MissingResourceException("Resource not found: " + resourceName,
                    contextClass.getName(), resourceName);
        }
        return url;
    }

}
