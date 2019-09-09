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

package org.apache.fop.render.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.AbstractRendererConfiguratorTest;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PDFRendererConfBuilder;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;

public class PDFRendererConfiguratorTestCase extends
        AbstractRendererConfiguratorTest<PDFRendererConfigurator, PDFRendererConfBuilder> {

    public PDFRendererConfiguratorTestCase() throws IFException {
        super(MimeConstants.MIME_PDF, PDFRendererConfBuilder.class, PDFDocumentHandler.class);
        FOUserAgent userAgent = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        docHandler = new MyPDFDocumentHandler(new IFContext(userAgent));
        docHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
    }

    static class MyPDFDocumentHandler extends PDFDocumentHandler {
        MyPDFDocumentHandler(IFContext context) {
            super(context);
        }

        PDFDocument getThePDFDocument() {
            return getPDFDocument();
        }
    }

    @Override
    public void setUpDocumentHandler() {
    }

    @Override
    protected PDFRendererConfigurator createConfigurator() {
        return new PDFRendererConfigurator(userAgent, new PDFRendererConfig.PDFRendererConfigParser());
    }

    private MyPDFDocumentHandler getDocHandler() {
        return (MyPDFDocumentHandler) docHandler;
    }

    @Test
    public void testFormXObjectEnabled() throws Exception {
        parseConfig(createBuilder().setFormXObjectEnabled(true));
        docHandler.startDocument();
        Assert.assertTrue(getDocHandler().getThePDFDocument().isFormXObjectEnabled());
    }
}
