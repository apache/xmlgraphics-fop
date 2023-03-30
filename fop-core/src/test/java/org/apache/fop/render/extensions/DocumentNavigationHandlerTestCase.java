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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.pdf.PDFLinearizationTestCase;
import org.apache.fop.pdf.PDFVTTestCase;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFParser;
import org.apache.fop.render.intermediate.IFUtil;
import org.apache.fop.render.intermediate.extensions.AbstractAction;
import org.apache.fop.render.intermediate.extensions.Bookmark;
import org.apache.fop.render.intermediate.extensions.BookmarkTree;
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
                new HashMap<String, StructureTreeElement>(), new HashMap<String, GoToXYAction>());
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

    @Test
    public void testGotoXYMergedIF() throws Exception {
        InputStream ifXml = getClass().getResourceAsStream("link.if.xml");
        ByteArrayOutputStream pdf = iFToPDF(ifXml, "<fop/>");
        Assert.assertTrue(pdf.toString().contains("/S /GoTo"));
    }

    private ByteArrayOutputStream iFToPDF(InputStream is, String fopxconf) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FOUserAgent userAgent = FopFactory.newInstance(new File(".").toURI(),
                new ByteArrayInputStream(fopxconf.getBytes())).newFOUserAgent();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Source src = new StreamSource(is);
        IFDocumentHandler documentHandler
                = userAgent.getRendererFactory().createDocumentHandler(userAgent, MimeConstants.MIME_PDF);
        documentHandler.setResult(new StreamResult(out));
        IFUtil.setupFonts(documentHandler);
        IFParser parser = new IFParser();
        Result res = new SAXResult(parser.getContentHandler(documentHandler, userAgent));
        transformer.transform(src, res);
        return out;
    }

    @Test
    public void testGotoXYPrevousPage() throws SAXException, IFException, IOException {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        PDFDocumentHandler documentHandler = new PDFDocumentHandler(new IFContext(ua));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(bos));
        documentHandler.setFontInfo(new FontInfo());
        documentHandler.startDocument();

        documentHandler.startPage(0, "", "", new Dimension());
        documentHandler.endPage();

        documentHandler.startPage(1, "", "", new Dimension());
        final List<GoToXYAction> goToXYActions = new ArrayList<GoToXYAction>();
        PDFDocumentNavigationHandler pdfDocumentNavigationHandler = new PDFDocumentNavigationHandler(documentHandler) {
            public void addResolvedAction(AbstractAction action) throws IFException {
                super.addResolvedAction(action);
                goToXYActions.add((GoToXYAction) action);
            }
        };
        DocumentNavigationHandler navigationHandler = new DocumentNavigationHandler(pdfDocumentNavigationHandler,
                new HashMap<String, StructureTreeElement>(), new HashMap<String, GoToXYAction>());
        QName xy = DocumentNavigationExtensionConstants.GOTO_XY;
        Attributes attributes = mock(Attributes.class);
        when(attributes.getValue("page-index")).thenReturn("0");
        when(attributes.getValue("page-index-relative")).thenReturn("-1");
        when(attributes.getValue("x")).thenReturn("0");
        when(attributes.getValue("y")).thenReturn("0");
        navigationHandler.startElement(xy.getNamespaceURI(), xy.getLocalName(), null, attributes);
        navigationHandler.endElement(xy.getNamespaceURI(), xy.getLocalName(), null);
        documentHandler.endPage();
        documentHandler.endDocument();

        Assert.assertEquals(goToXYActions.get(0).getPageIndex(), 0);

        Collection<StringBuilder> objs = PDFLinearizationTestCase.readObjs(
                new ByteArrayInputStream(bos.toByteArray())).values();
        String pages = PDFVTTestCase.getObj(objs, "/Type /Pages");
        String action = PDFVTTestCase.getObj(objs, "/Type /Action");
        String pageRef = action.split("\\[")[1].split(" /XYZ")[0];
        Assert.assertTrue(pageRef.endsWith(" 0 R"));
        Assert.assertTrue(pages.contains("/Kids [" + pageRef));
    }

    @Test
    public void testGotoXYUniqueLinks() throws IFException, SAXException {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        PDFDocumentHandler documentHandler = new PDFDocumentHandler(new IFContext(ua));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(bos));
        documentHandler.setFontInfo(new FontInfo());
        documentHandler.startDocument();

        PDFDocumentNavigationHandler pdfDocumentNavigationHandler = new PDFDocumentNavigationHandler(documentHandler);
        DocumentNavigationHandler navigationHandler = new DocumentNavigationHandler(pdfDocumentNavigationHandler,
                new HashMap<String, StructureTreeElement>(), new HashMap<String, GoToXYAction>());
        QName xy = DocumentNavigationExtensionConstants.GOTO_XY;

        Attributes attributes = mock(Attributes.class);
        when(attributes.getValue("page-index")).thenReturn("0");
        when(attributes.getValue("x")).thenReturn("0");
        when(attributes.getValue("y")).thenReturn("0");

        documentHandler.startPage(0, "", "", new Dimension());
        navigationHandler.startElement(xy.getNamespaceURI(), xy.getLocalName(), null, attributes);
        navigationHandler.endElement(xy.getNamespaceURI(), xy.getLocalName(), null);
        documentHandler.endPage();
        documentHandler.startPage(1, "", "", new Dimension());
        navigationHandler.startElement(xy.getNamespaceURI(), xy.getLocalName(), null, attributes);
        navigationHandler.endElement(xy.getNamespaceURI(), xy.getLocalName(), null);
        documentHandler.endPage();

        Iterator<String> i = Arrays.asList(bos.toString().split("\n")).iterator();
        List<String> pageLink = new ArrayList<String>();
        while (i.hasNext()) {
            if (i.next().equals("/S /GoTo")) {
                pageLink.add(i.next());
            }
        }
        Assert.assertEquals(pageLink.size(), 2);
        Assert.assertFalse(pageLink.get(0).equals(pageLink.get(1)));
    }

    @Test
    public void testBookmarkGotoXY() throws SAXException, IFException {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        PDFDocumentHandler documentHandler = new PDFDocumentHandler(new IFContext(ua));
        documentHandler.setResult(new StreamResult(new ByteArrayOutputStream()));
        documentHandler.setFontInfo(new FontInfo());
        documentHandler.startDocument();

        documentHandler.startPage(0, "", "", new Dimension());
        documentHandler.endPage();

        int currentPage = 1;
        documentHandler.startPage(currentPage, "", "", new Dimension());

        final List<BookmarkTree> trees = new ArrayList<BookmarkTree>();
        PDFDocumentNavigationHandler pdfDocumentNavigationHandler = new PDFDocumentNavigationHandler(documentHandler) {
            public void renderBookmarkTree(BookmarkTree tree) throws IFException {
                trees.add(tree);
            }
        };
        DocumentNavigationHandler navigationHandler = new DocumentNavigationHandler(pdfDocumentNavigationHandler,
                new HashMap<String, StructureTreeElement>(), new HashMap<String, GoToXYAction>());
        Attributes attributes = mock(Attributes.class);
        when(attributes.getValue("page-index")).thenReturn("0");
        when(attributes.getValue("x")).thenReturn("0");
        when(attributes.getValue("y")).thenReturn("0");

        for (QName q : Arrays.asList(DocumentNavigationExtensionConstants.BOOKMARK_TREE,
                DocumentNavigationExtensionConstants.BOOKMARK,
                DocumentNavigationExtensionConstants.GOTO_XY)) {
            navigationHandler.startElement(q.getNamespaceURI(), q.getLocalName(), null, attributes);
        }
        for (QName q : Arrays.asList(DocumentNavigationExtensionConstants.GOTO_XY,
                DocumentNavigationExtensionConstants.BOOKMARK,
                DocumentNavigationExtensionConstants.BOOKMARK_TREE)) {
            navigationHandler.endElement(q.getNamespaceURI(), q.getLocalName(), null);
        }

        documentHandler.endPage();

        Bookmark b = (Bookmark) trees.get(0).getBookmarks().get(0);
        GoToXYAction a = (GoToXYAction) b.getAction();
        Assert.assertEquals(a.getPageIndex(), 0);
    }

    @Test
    public void testGotoXYMissingDestIF() throws Exception {
        InputStream ifXml = getClass().getResourceAsStream("linkmissingdest.if.xml");
        String fopxconf = "<fop version=\"1.0\">\n"
                + "  <accessibility>true</accessibility>\n"
                + "  <renderers>\n"
                + "    <renderer mime=\"application/pdf\">\n"
                + "      <version>1.5</version>\n"
                + "    </renderer>\n"
                + "  </renderers>\n"
                + "</fop>";
        ByteArrayOutputStream pdf = iFToPDF(ifXml, fopxconf);
        Assert.assertTrue(pdf.toString().contains("/S /GoTo"));
    }
}
