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

package org.apache.fop.render.pdf;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.xmp.Metadata;

import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fo.extensions.xmp.XMPMetadata;
import org.apache.fop.pdf.PDFAnnotList;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFNumsArray;
import org.apache.fop.pdf.PDFObject;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFParentTree;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFResources;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFStructTreeRoot;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFDocumentHandler;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFDocumentNavigationHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;

/**
 * {@code IFDocumentHandler} implementation that produces PDF.
 */
public class PDFDocumentHandler extends AbstractBinaryWritingIFDocumentHandler {

    /** logging instance */
    private static Log log = LogFactory.getLog(PDFDocumentHandler.class);

    /** the following variables are used for accessibility */
    private int pageSequenceCounter;
    private DocumentBuilder parser = null;
    private Document doc = null;
    private Map structElemType = new HashMap();
    private boolean accessEnabled = false;
    private int parentTreeKey = -1;
    private int pageLinkCount = 0;
    private int mcidKey = -1;
    private PDFParentTree parentTree = null;
    private Map structTreeMap = new HashMap();
    private List parentTreeList = new java.util.ArrayList();

    private static class NamespaceContextImpl implements NamespaceContext {

        private String uri;
        private String prefix;

        public NamespaceContextImpl() {
        }

        public NamespaceContextImpl(String prefix, String uri) {
            this.uri = uri;
            this.prefix = prefix;
        }

        public String getNamespaceURI(String prefix) {
            return uri;
        }

        public void setNamespaceURI(String uri) {
            this.uri = uri;
        }

        public String getPrefix(String uri) {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public Iterator getPrefixes(String uri) {
            return null;
        }

    }

    private static final class ParentTreeEntry {
        private final int position;
        private final PDFObject object;
        private ParentTreeEntry(int p, PDFObject o) {
            position = p;
            object = o;
        }
        private int getPosition() {
            return position;
        }
        private PDFObject getPDFObject() {
            return object;
        }
     }


    /** the PDF Document being created */
    protected PDFDocument pdfDoc;

    /**
     * Utility class which enables all sorts of features that are not directly connected to the
     * normal rendering process.
     */
    protected PDFRenderingUtil pdfUtil;

    /** the /Resources object of the PDF document being created */
    protected PDFResources pdfResources;

    /** The current content generator */
    protected PDFContentGenerator generator;

    /** the current annotation list to add annotations to */
    protected PDFResourceContext currentContext;

    /** the current page to add annotations to */
    protected PDFPage currentPage;

    /** the current page's PDF reference */
    protected PageReference currentPageRef;

    /** Used for bookmarks/outlines. */
    protected Map pageReferences = new java.util.HashMap();

    private final PDFDocumentNavigationHandler documentNavigationHandler
            = new PDFDocumentNavigationHandler(this);

    /**
     * Default constructor.
     */
    public PDFDocumentHandler() {
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return !accessEnabled;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_PDF;
    }

    /** {@inheritDoc} */
    public void setContext(IFContext context) {
        super.setContext(context);
        this.pdfUtil = new PDFRenderingUtil(context.getUserAgent());
    }

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        return new PDFRendererConfigurator(getUserAgent());
    }

    /** {@inheritDoc} */
    public IFDocumentNavigationHandler getDocumentNavigationHandler() {
        return this.documentNavigationHandler;
    }

    PDFRenderingUtil getPDFUtil() {
        return this.pdfUtil;
    }

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        super.startDocument();
        try {
            this.pdfDoc = pdfUtil.setupPDFDocument(this.outputStream);
            this.accessEnabled = getUserAgent().accessibilityEnabled();
            if (accessEnabled) {
                //TODO: make document language variable, see note on wiki page PDF Accessibility
                //TODO:                and follow-up emails on fop-dev
                this.pdfDoc.getRoot().setLanguage("en");
                parentTree = new PDFParentTree();
                pageSequenceCounter = 0;
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                parser = factory.newDocumentBuilder();
            }
        } catch (IOException e) {
            throw new IFException("I/O error in startDocument()", e);
        } catch (ParserConfigurationException pce) {
            throw new IFException("Error creating new DocumentBuilder", pce);
        }
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        pdfUtil.generateDefaultXMPMetadata();
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        try {
            pdfDoc.getResources().addFonts(pdfDoc, fontInfo);
            if (getUserAgent().accessibilityEnabled()) {
                PDFNumsArray nums = parentTree.getNums();
                for (int i = 0; i <= this.parentTreeKey; i++) {
                    PDFArray tArray = new PDFArray();
                    for (int j = 0; j < parentTreeList.size(); j++) {
                        if (((ParentTreeEntry)parentTreeList.get(j)).getPosition() == i) {
                            tArray.add(((ParentTreeEntry)parentTreeList.get(j)).getPDFObject());
                        }
                    }
                    if (tArray.length() == 1) {
                        nums.put(i, tArray.get(0));
                    } else if (tArray.length() > 1) {
                        nums.put(i, tArray);
                    }
                }
                parentTree.setNums(nums);
                getStructTreeRoot().addParentTree(parentTree);
                pdfDoc.outputTrailer(this.outputStream);
                parser = null;
                doc = null;
                structElemType = null;
                parentTree = null;
                structTreeMap = null;
                parentTreeList = null;
            } else {
                pdfDoc.outputTrailer(this.outputStream);
            }
            this.pdfDoc = null;
            pdfResources = null;
            this.generator = null;
            currentContext = null;
            currentPage = null;
        } catch (IOException ioe) {
            throw new IFException("I/O error in endDocument()", ioe);
        }
        super.endDocument();
    }

    private PDFStructTreeRoot getStructTreeRoot() {
        return this.pdfDoc.getRoot().getStructTreeRoot();
    }


    /** {@inheritDoc} */
    public void startPageSequence(String id) throws IFException {
        //TODO page sequence title, country and language

        if (getUserAgent().accessibilityEnabled()) {
            try {
                if (doc == null) {
                    doc = parser.parse(
                            new ByteArrayInputStream(this.getUserAgent().getReducedFOTree()));
                }
                PDFStructElem parent = (PDFStructElem)getStructTreeRoot().getFirstChild();
                PDFStructElem structElemPart = new PDFStructElem("page-sequence", parent);
                this.pdfDoc.assignObjectNumber(structElemPart);
                this.pdfDoc.addTrailerObject(structElemPart);
                parent.addKid(structElemPart);

                String xpathExpr = "/fo:root/fo:page-sequence["
                        + Integer.toString(++pageSequenceCounter) + "]/*";
                XPath xpath = XPathFactory.newInstance().newXPath();
                NamespaceContext namespaceContext = new NamespaceContextImpl("fo",
                        "http://www.w3.org/1999/XSL/Format");
                xpath.setNamespaceContext(namespaceContext);

                NodeList nodes = (NodeList) xpath.evaluate(xpathExpr, doc,
                        XPathConstants.NODESET);

                for (int i = 0, n = nodes.getLength(); i < n; i++) {
                    Node node = nodes.item(i);
                    if (node.getNodeName().equals("fo:flow")
                            || node.getNodeName().equals("fo:static-content")) {
                        PDFStructElem structElemSect = new PDFStructElem(
                                node.getLocalName(), structElemPart);
                        this.pdfDoc.assignObjectNumber(structElemSect);
                        this.pdfDoc.addTrailerObject(structElemSect);
                        structElemPart.addKid(structElemSect);
                        NodeList iNodes = node.getChildNodes();
                        for (int j = 0, m = iNodes.getLength(); j < m; j++) {
                            processContent(iNodes.item(j), structElemSect, 1);
                        }
                    }
                }
            } catch (SAXException e) {
                throw new IFException("SAX error in startPageSequence()", e);
            } catch (XPathExpressionException e) {
                throw new IFException("Error while evaluating XPath expression", e);
            } catch (IOException ioe) {
                throw new IFException("I/O error while parsing structure tree", ioe);
            }
        }
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, String pageMasterName, Dimension size)
                throws IFException {
        // used for accessibility
        this.parentTreeKey = this.parentTreeKey + this.pageLinkCount + 1;
        this.mcidKey = 0;
        this.pageLinkCount = 0;
        //
        this.pdfResources = this.pdfDoc.getResources();

        this.currentPage = this.pdfDoc.getFactory().makePage(
            this.pdfResources,
            (int)Math.round(size.getWidth() / 1000),
            (int)Math.round(size.getHeight() / 1000), index,
            parentTreeKey);   // used for accessibility
        pdfUtil.generatePageLabel(index, name);

        currentPageRef = new PageReference(currentPage, size);
        this.pageReferences.put(new Integer(index), currentPageRef);

        this.generator = new PDFContentGenerator(this.pdfDoc, this.outputStream,
                this.currentPage, this.accessEnabled);
        // Transform the PDF's default coordinate system (0,0 at lower left) to the PDFPainter's
        AffineTransform basicPageTransform = new AffineTransform(1, 0, 0, -1, 0,
                size.height / 1000f);
        generator.concatenate(basicPageTransform);
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        return new PDFPainter(this);
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            this.documentNavigationHandler.commit();
            this.pdfDoc.registerObject(generator.getStream());
            currentPage.setContents(generator.getStream());
            PDFAnnotList annots = currentPage.getAnnotations();
            if (annots != null) {
                this.pdfDoc.addObject(annots);
            }
            this.pdfDoc.addObject(currentPage);
            this.generator.flushPDFDoc();
            this.generator = null;
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        if (extension instanceof XMPMetadata) {
            pdfUtil.renderXMPMetadata((XMPMetadata)extension);
        } else if (extension instanceof Metadata) {
            XMPMetadata wrapper = new XMPMetadata(((Metadata)extension));
            pdfUtil.renderXMPMetadata(wrapper);
        } else {
            log.debug("Don't know how to handle extension object. Ignoring: "
                    + extension + " (" + extension.getClass().getName() + ")");
        }
    }

    PageReference getPageReference(int pageIndex) {
        return (PageReference)this.pageReferences.get(
                new Integer(pageIndex));
    }

    static final class PageReference {

        private final PDFReference pageRef;
        private final Dimension pageDimension;

        private PageReference(PDFPage page, Dimension dim) {
            this.pageRef = page.makeReference();
            this.pageDimension = new Dimension(dim);
        }

        public PDFReference getPageRef() {
            return this.pageRef;
        }

        public Dimension getPageDimension() {
            return this.pageDimension;
        }
    }

    /**
     * Used for accessibility
     * @param position in parentTree
     * @param o reference of PDFObject to be added to parentTree
     */
    void addToParentTree(int position, PDFObject o) {
        PDFNumsArray nums = parentTree.getNums();
        nums.put(position, o);
        parentTree.setNums(nums);
    }


    /**
     * Used for accessibility
     * @param position in parentTree
     * @param o object to be added to parentTree
     */
    void addToTempList(int position, PDFObject o) {
        ParentTreeEntry myEntry = new ParentTreeEntry(position, o);
        this.parentTreeList.add(myEntry);
    }


    /**
     * Return the PDFObject
     * @param ptr this is the key
     * @return PDFObject referenced with ptr
     */
    PDFObject getTrailerObject(String ptr) {
        return (PDFObject) this.structTreeMap.get(ptr);
    }

    /**
     * Return the parent PDFObject referenced by ptr
     * @param ptr this is the key
     * @return PDFObject parent of PDFObject referenced with ptr
     */
    PDFObject getParentTrailerObject(String ptr) {
        PDFStructElem tempStructElem = (PDFStructElem) this.structTreeMap.get(ptr);
        return tempStructElem.getParentStructElem();
    }

    /**
     * Adds a link object as child to StructElem
     * @param ptr of PDFStructElem
     * @param o PDFLink object
     */
    void addLinkToStructElem(String ptr, PDFObject o) {
        PDFDictionary dict = new PDFDictionary();
        dict.put("Type", new PDFName("OBJR"));
        dict.put("Pg", this.currentPage);
        dict.put("Obj", o);
        PDFStructElem tempStructElem = (PDFStructElem) structTreeMap.get(ptr);
        tempStructElem.addKid(dict);
    }

    /**
     * Adds a child to StructElem, called from PDFPainter.drawImage
     * @param ptr of PDFStructElem
     * @param mcid sequence number within page
     */
    void addChildToStructElemImage(String ptr, int mcid) {
        PDFStructElem tempStructElem = (PDFStructElem) structTreeMap.get(ptr);
        tempStructElem.addMCIDKid(mcid);
        tempStructElem.addPage(this.currentPage);
        if (!tempStructElem.getLevel1()) {
            addMeToParent(tempStructElem);
        }
    }

    /**
     * Adds a child to StructElem, called from PDFPainter.drawText
     * @param ptr of PDFSturctElem
     * @param mcid sequence number within page
     */
    void addChildToStructElemText(String ptr, int mcid) {
        PDFDictionary dict = new PDFDictionary();
        dict.put("Type", new PDFName("MCR"));
        dict.put("Pg", this.currentPage);
        dict.put("MCID", mcid);
        PDFStructElem tempStructElem = (PDFStructElem) structTreeMap.get(ptr);
        tempStructElem.addKid(dict);
        if (!tempStructElem.getLevel1()) {
            addMeToParent(tempStructElem);
        }
    }

    /**
     * Add child PDFStructElem to parent child elements
     * Repeat until level 1 or child already exists
     * @param childStructElem to be added
     */
    protected void addMeToParent(PDFStructElem childStructElem) {
        PDFStructElem parentStructElem = (PDFStructElem) childStructElem.getParentStructElem();
        // test if child already exists or not
        if (parentStructElem.addUniqueKid(childStructElem)) {
            if (!parentStructElem.getLevel1()) {
                addMeToParent(parentStructElem);
            }
        }
    }

    /**
     * increment MCID value
     */
    void incMCID() {
        this.mcidKey++;
    }

    /**
     * MCID is a sequential number per page
     * @return MCID value
     */
    int getMCID() {
        return this.mcidKey;
    }

    /**
     * Used for accessibility
     * @param ptr pointer into map of all structElems
     * @return type of found structElem
     */
    String getStructElemType(String ptr) {
        return (String) structElemType.get(ptr);
    }

    /**
     * Used for accessibility
     * @param me node being processed
     * @param parent parent node in DOM of me
     * @param depth depth level in DOM, static-content & flow are 0
     */
    private void processContent(Node me, PDFStructElem parent, int depth) {
        String ptr;
        Node attr = me.getAttributes().getNamedItem("foi:ptr");
        if (attr != null) {
            ptr = attr.getNodeValue();
        } else {
            log.error("Accessibility: missing foi:ptr");
            ptr = "";
        }
        String s = me.getLocalName();
        PDFStructElem structElem = new PDFStructElem(s, parent);
        this.pdfDoc.assignObjectNumber(structElem);
        this.pdfDoc.addTrailerObject(structElem);
        if (depth == 1) {
            parent.addKid(structElem);
            structElem.setLevel1();
        }
        if (s.equals("external-graphic") || s.equals("instream-foreign-object")) {
            Node altTextNode = me.getAttributes().getNamedItem("fox:alt-text");
            if (altTextNode != null) {
                structElem.put("Alt", altTextNode.getNodeValue());
            } else {
                log.warn("fo:" + s
                 + " requires an alternative text attribute fox:alt-text for accessibility");
                structElem.put("Alt", "No alternate text specified");
            }
        }
        // the following map is used e.g. in PDFPainter.drawText
        structElemType.put(ptr, structElem.get("S").toString());
        // this map will be used for fast access of the StructElem by ptr
        structTreeMap.put(ptr, structElem);
        NodeList nodes = me.getChildNodes();
        depth++;
        for (int i = 0, n = nodes.getLength(); i < n; i++) {
            processContent(nodes.item(i), structElem, depth);
        }
    }

    /**
     * used for accessibility
     * @return mcid to be used for next link to be processed
     */
    int getPageLinkCountPlusPageParentKey() {
        this.pageLinkCount++;
        return (this.parentTreeKey + this.pageLinkCount);
    }

    /**
     * used for accessibility
     * @return current parentTreeKey
     */
    int getCurrentParentTreeKey() {
        return this.parentTreeKey;
    }

}
