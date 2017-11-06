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
package org.apache.fop.render.extensions;

import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.extensions.AbstractAction;
import org.apache.fop.render.intermediate.extensions.DocumentNavigationExtensionConstants;
import org.apache.fop.render.intermediate.extensions.DocumentNavigationHandler;
import org.apache.fop.render.intermediate.extensions.GoToXYAction;
import org.apache.fop.render.pdf.PDFDocumentHandler;
import org.apache.fop.render.pdf.PDFDocumentNavigationHandler;

public class DocumentNavigationHandlerTestCase {
    @Test
    public void testGotoXY() throws SAXException, IFException {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        PDFDocumentHandler documentHandler = new PDFDocumentHandler(new IFContext(ua));
        documentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        documentHandler.setFontInfo(new FontInfo());
        documentHandler.startDocument();

        documentHandler.startPage(0, "", "", new Dimension());
        documentHandler.endPage();

        int currentPage = 1;
        documentHandler.startPage(currentPage, "", "", new Dimension());
        final List<GoToXYAction> goToXYActions = new ArrayList<GoToXYAction>();
        PDFDocumentNavigationHandler pdfDocumentNavigationHandler = new PDFDocumentNavigationHandler(documentHandler) {
            public void addResolvedAction(AbstractAction action) throws IFException {
                super.addResolvedAction(action);
                goToXYActions.add((GoToXYAction) action);
            }
        };
        DocumentNavigationHandler navigationHandler = new DocumentNavigationHandler(pdfDocumentNavigationHandler,
                new HashMap<String, StructureTreeElement>());
        QName xy = DocumentNavigationExtensionConstants.GOTO_XY;
        Attributes attributes = mock(Attributes.class);
        when(attributes.getValue("page-index")).thenReturn("0");
        when(attributes.getValue("x")).thenReturn("0");
        when(attributes.getValue("y")).thenReturn("0");
        navigationHandler.startElement(xy.getNamespaceURI(), xy.getLocalName(), null, attributes);
        navigationHandler.endElement(xy.getNamespaceURI(), xy.getLocalName(), null);
        documentHandler.endPage();

        //Since user may merge IF files we want to use current page
        Assert.assertEquals(goToXYActions.get(0).getPageIndex(), currentPage);
    }
}
