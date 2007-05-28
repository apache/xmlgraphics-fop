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

package org.apache.fop.render.xml;

// Java
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.util.QName;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.Area;
import org.apache.fop.area.AreaTreeObject;
import org.apache.fop.area.BeforeFloat;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.CTM;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MainReference;
import org.apache.fop.area.NormalFlow;
import org.apache.fop.area.OffDocumentExtensionAttachment;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.BookmarkData;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Span;
import org.apache.fop.area.Trait;
import org.apache.fop.area.Trait.InternalLink;
import org.apache.fop.area.Trait.Background;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineBlockParent;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.util.ColorUtil;
import org.apache.fop.util.XMLizable;

/**
 * Renderer that renders areas to XML for debugging purposes.
 * This creates an xml that contains the information of the area
 * tree. It does not output any state or derived information.
 * The output can be used to build a new area tree (@see AreaTreeBuilder)
 * which can be rendered to any renderer.
 */
public class XMLRenderer extends PrintRenderer {

    /** XML MIME type */
    public static final String XML_MIME_TYPE = MimeConstants.MIME_FOP_AREA_TREE;

    /** Main namespace in use. */
    public static final String NS = "";

    /** CDATA type */
    public static final String CDATA = "CDATA";

    /** An empty Attributes object used when no attributes are needed. */
    public static final Attributes EMPTY_ATTS = new AttributesImpl();

    private boolean startedSequence = false;
    private RendererContext context;
    private boolean compactFormat = false;

    /** If not null, the XMLRenderer will mimic another renderer by using its font setup. */
    protected Renderer mimic;

    /** ContentHandler that the generated XML is written to */
    protected ContentHandler handler;

    /** AttributesImpl instance that can be used during XML generation. */
    protected AttributesImpl atts = new AttributesImpl();

    /** The OutputStream to write the generated XML to. */
    protected OutputStream out;

    /** A list of ExtensionAttachements received through processOffDocumentItem() */
    protected List extensionAttachments;

    /**
     * Creates a new XML renderer.
     */
    public XMLRenderer() {
        context = new RendererContext(this, XML_MIME_TYPE);
    }

    /**
     * @see org.apache.fop.render.Renderer#setUserAgent(FOUserAgent)
     */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);

        XMLHandler xmlHandler = new XMLXMLHandler();
        userAgent.getXMLHandlerRegistry().addXMLHandler(xmlHandler);
        Boolean b = (Boolean)userAgent.getRendererOptions().get("compact-format");
        if (b != null) {
            setCompactFormat(b.booleanValue());
        }
    }

    /**
     * Call this method to make the XMLRenderer mimic a different renderer by using its font
     * setup. This is useful when working with the intermediate format parser.
     * @param renderer the renderer to mimic
     */
    public void mimicRenderer(Renderer renderer) {
        this.mimic = renderer;
    }

    /** @see org.apache.fop.render.PrintRenderer#setupFontInfo(org.apache.fop.fonts.FontInfo) */
    public void setupFontInfo(FontInfo inFontInfo) {
        if (mimic != null) {
            mimic.setupFontInfo(inFontInfo);
        } else {
            super.setupFontInfo(inFontInfo);
        }
    }

    /**
     * Sets an outside TransformerHandler to use instead of the default one
     * create in this class in startRenderer().
     * @param handler Overriding TransformerHandler
     */
    public void setContentHandler(ContentHandler handler) {
        this.handler = handler;
    }

    public void setCompactFormat(boolean compact) {
        this.compactFormat = compact;
    }
    
    private boolean isDetailedFormat() {
        return !this.compactFormat;
    }

    /**
     * Handles SAXExceptions.
     * @param saxe the SAXException to handle
     */
    protected void handleSAXException(SAXException saxe) {
        throw new RuntimeException(saxe.getMessage());
    }

    /**
     * Writes a comment to the generated XML.
     * @param comment the comment
     */
    protected void comment(String comment) {
        if (handler instanceof LexicalHandler) { 
            try {
                ((LexicalHandler) handler).comment(comment.toCharArray(), 0, comment.length());
            } catch (SAXException saxe) {
                handleSAXException(saxe);
            }
        }
    }
    
    /**
     * Starts a new element (without attributes).
     * @param tagName tag name of the element
     */
    protected void startElement(String tagName) {
        startElement(tagName, EMPTY_ATTS);
    }

    /**
     * Starts a new element.
     * @param tagName tag name of the element
     * @param atts attributes to add
     */
    protected void startElement(String tagName, Attributes atts) {
        try {
            handler.startElement(NS, tagName, tagName, atts);
        } catch (SAXException saxe) {
            handleSAXException(saxe);
        }
    }

    /**
     * Ends an element.
     * @param tagName tag name of the element
     */
    protected void endElement(String tagName) {
        try {
            handler.endElement(NS, tagName, tagName);
        } catch (SAXException saxe) {
            handleSAXException(saxe);
        }
    }

    /**
     * Sends plain text to the XML
     * @param text the text
     */
    protected void characters(String text) {
        try {
            char[] ca = text.toCharArray();
            handler.characters(ca, 0, ca.length);
        } catch (SAXException saxe) {
            handleSAXException(saxe);
        }
    }

    /**
     * Adds a new attribute to the protected member variable "atts".
     * @param name name of the attribute
     * @param value value of the attribute
     */
    protected void addAttribute(String name, String value) {
        atts.addAttribute(NS, name, name, CDATA, value);
    }

    /**
     * Adds a new attribute to the protected member variable "atts".
     * @param name name of the attribute
     * @param value value of the attribute
     */
    protected void addAttribute(QName name, String value) {
        atts.addAttribute(name.getNamespaceURI(), name.getLocalName(), name.getQName(), 
                CDATA, value);
    }

    /**
     * Adds a new attribute to the protected member variable "atts".
     * @param name name of the attribute
     * @param value value of the attribute
     */
    protected void addAttribute(String name, int value) {
        addAttribute(name, Integer.toString(value));
    }

    /**
     * Adds a new attribute to the protected member variable "atts".
     * @param name name of the attribute
     * @param rect a Rectangle2D to format and use as attribute value
     */
    protected void addAttribute(String name, Rectangle2D rect) {
        addAttribute(name, createString(rect));
    }

    /**
     * Adds the general Area attributes.
     * @param area Area to extract attributes from
     */
    protected void addAreaAttributes(Area area) {
        addAttribute("ipd", area.getIPD());
        addAttribute("bpd", area.getBPD());
        if (isDetailedFormat()) {
            if (area.getIPD() != 0) {
                addAttribute("ipda", area.getAllocIPD());
            }
            if (area.getBPD() != 0) {
                addAttribute("bpda", area.getAllocBPD());
            }
            addAttribute("bap", area.getBorderAndPaddingWidthStart() + " "
                    + area.getBorderAndPaddingWidthEnd() + " "
                    + area.getBorderAndPaddingWidthBefore() + " "
                    + area.getBorderAndPaddingWidthAfter());
        }
    }

    /**
     * Adds attributes from traits of an Area.
     * @param area Area to extract traits from
     */
    protected void addTraitAttributes(Area area) {
        Map traitMap = area.getTraits();
        if (traitMap != null) {
            Iterator iter = traitMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry traitEntry = (Map.Entry) iter.next();
                Object key = traitEntry.getKey();
                String name = Trait.getTraitName(key);
                Class clazz = Trait.getTraitClass(key);
                if ("break-before".equals(name) || "break-after".equals(name)) {
                    continue;
                }
                Object value = traitEntry.getValue();
                if (key == Trait.FONT) {
                    FontTriplet triplet = (FontTriplet)value;
                    addAttribute("font-name", triplet.getName());
                    addAttribute("font-style", triplet.getStyle());
                    addAttribute("font-weight", triplet.getWeight());
                } else if (clazz.equals(InternalLink.class)) {
                    InternalLink iLink = (InternalLink)value;
                    addAttribute(name, iLink.xmlAttribute());
                } else if (clazz.equals(Background.class)) {
                    Background bkg = (Background)value;
                    //TODO Remove the following line (makes changes in the test checks necessary)
                    addAttribute(name, bkg.toString());
                    if (bkg.getColor() != null) {
                        addAttribute("bkg-color", bkg.getColor().toString());
                    }
                    if (bkg.getURL() != null) {
                        addAttribute("bkg-img", bkg.getURL());
                        String repString;
                        int repeat = bkg.getRepeat();
                        switch (repeat) {
                        case Constants.EN_REPEAT:
                            repString = "repeat";
                            break;
                        case Constants.EN_REPEATX:
                            repString = "repeat-x";
                            break;
                        case Constants.EN_REPEATY:
                            repString = "repeat-y";
                            break;
                        case Constants.EN_NOREPEAT:
                            repString = "no-repeat";
                            break;
                        default:
                            throw new IllegalStateException(
                                    "Illegal value for repeat encountered: " + repeat);
                        }
                        addAttribute("bkg-repeat", repString);
                        addAttribute("bkg-horz-offset", bkg.getHoriz());
                        addAttribute("bkg-vert-offset", bkg.getVertical());
                    }
                } else if (clazz.equals(Color.class)) {
                    Color c = (Color)value;
                    addAttribute(name, ColorUtil.colorToString(c));
                } else if (key == Trait.START_INDENT || key == Trait.END_INDENT) {
                    if (((Integer)value).intValue() != 0) {
                        addAttribute(name, value.toString());
                    }
                } else {
                    addAttribute(name, value.toString());
                }
            }
        }
        
        transferForeignObjects(area);
    }

    private void transferForeignObjects(AreaTreeObject ato) {
        Map prefixes = new java.util.HashMap();
        Iterator iter = ato.getForeignAttributes().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            QName qname = (QName)entry.getKey();
            prefixes.put(qname.getPrefix(), qname.getNamespaceURI());
            addAttribute(qname, (String)entry.getValue());
        }
        //Namespace declarations
        iter = prefixes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String qn = "xmlns:" + (String)entry.getKey();
            atts.addAttribute("", (String)entry.getKey(), qn, 
                    CDATA, (String)entry.getValue());
        }
    }

    private String createString(Rectangle2D rect) {
        return "" + (int) rect.getX() + " " + (int) rect.getY() + " "
                  + (int) rect.getWidth() + " " + (int) rect.getHeight();
    }

    private void handleDocumentExtensionAttachments() {
        if (extensionAttachments != null && extensionAttachments.size() > 0) {
            handleExtensionAttachments(extensionAttachments);
            extensionAttachments.clear();
        }
    }

    /** @see org.apache.fop.render.AbstractRenderer#processOffDocumentItem(OffDocumentItem) */
    public void processOffDocumentItem(OffDocumentItem oDI) {
        if (oDI instanceof BookmarkData) {
            renderBookmarkTree((BookmarkData) oDI);
        } else if (oDI instanceof OffDocumentExtensionAttachment) {
            ExtensionAttachment attachment = ((OffDocumentExtensionAttachment)oDI).getAttachment();
            if (extensionAttachments == null) {
                extensionAttachments = new java.util.ArrayList();
            }
            extensionAttachments.add(attachment);
        } else {
            String warn = "Ignoring OffDocumentItem: " + oDI;
            log.warn(warn);
        }
    }

    /**
     * Renders a BookmarkTree object
     * @param bookmarkRoot the BookmarkData object representing the top of the tree
     */
    protected void renderBookmarkTree(BookmarkData bookmarkRoot) {
        if (bookmarkRoot.getWhenToProcess() == OffDocumentItem.END_OF_DOC) {
            endPageSequence();
        }
        /* If this kind of handling is also necessary for other renderers, then
           better add endPageSequence to the Renderer interface and call it
           explicitly from model.endDocument() */

        startElement("bookmarkTree");
        for (int i = 0; i < bookmarkRoot.getCount(); i++) {
            renderBookmarkItem(bookmarkRoot.getSubData(i));
        }
        endElement("bookmarkTree");
    }

    private void renderBookmarkItem(BookmarkData bm) {
        atts.clear();
        addAttribute("title", bm.getBookmarkTitle());
        addAttribute("show-children", String.valueOf(bm.showChildItems()));
        PageViewport pv = bm.getPageViewport();
        String pvKey = pv == null ? null : pv.getKey();
        addAttribute("internal-link",
                     InternalLink.makeXMLAttribute(pvKey, bm.getIDRef()));
        startElement("bookmark", atts);
        for (int i = 0; i < bm.getCount(); i++) {
            renderBookmarkItem(bm.getSubData(i));
        }
        endElement("bookmark");
    }

    /**
     * @see org.apache.fop.render.Renderer#startRenderer(OutputStream)
     */
    public void startRenderer(OutputStream outputStream)
                throws IOException {
        log.debug("Rendering areas to Area Tree XML");

        if (this.handler == null) {
            SAXTransformerFactory factory
                = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
            try {
                TransformerHandler transformerHandler = factory.newTransformerHandler();
                this.handler = transformerHandler;
                StreamResult res = new StreamResult(outputStream);
                transformerHandler.setResult(res);
            } catch (TransformerConfigurationException tce) {
                throw new RuntimeException(tce.getMessage());
            }

            this.out = outputStream;
        }

        try {
            handler.startDocument();
        } catch (SAXException saxe) {
            handleSAXException(saxe);
        }
        if (userAgent.getProducer() != null) {
            comment("Produced by " + userAgent.getProducer());
        }
        startElement("areaTree");
    }

    /**
     * @see org.apache.fop.render.Renderer#stopRenderer()
     */
    public void stopRenderer() throws IOException {
        endPageSequence();
        endElement("areaTree");
        try {
            handler.endDocument();
        } catch (SAXException saxe) {
            handleSAXException(saxe);
        }
        if (this.out != null) {
            this.out.flush();
        }
        log.debug("Written out Area Tree XML");
    }

    /**
     * @see org.apache.fop.render.Renderer#renderPage(PageViewport)
     */
    public void renderPage(PageViewport page) throws IOException, FOPException {
        atts.clear();
        addAttribute("bounds", page.getViewArea());
        addAttribute("key", page.getKey());
        addAttribute("nr", page.getPageNumber());
        addAttribute("formatted-nr", page.getPageNumberString());
        addAttribute("simple-page-master-name", page.getSimplePageMasterName());
        if (page.isBlank()) {
            addAttribute("blank", "true");
        }
        transferForeignObjects(page);
        startElement("pageViewport", atts);
        startElement("page");

        handlePageExtensionAttachments(page);
        super.renderPage(page);

        endElement("page");
        endElement("pageViewport");
    }

    private void handleExtensionAttachments(List attachments) {
        if (attachments != null && attachments.size() > 0) {
            startElement("extension-attachments");
            Iterator i = attachments.iterator();
            while (i.hasNext()) {
                ExtensionAttachment attachment = (ExtensionAttachment)i.next();
                if (attachment instanceof XMLizable) {
                    try {
                        ((XMLizable)attachment).toSAX(this.handler);
                    } catch (SAXException e) {
                        log.error("Error while serializing Extension Attachment", e);
                    }
                } else {
                    String warn = "Ignoring non-XMLizable ExtensionAttachment: " + attachment;
                    log.warn(warn);
                }
            }
            endElement("extension-attachments");
        }
    }

    private void handlePageExtensionAttachments(PageViewport page) {
        handleExtensionAttachments(page.getExtensionAttachments());
    }

    /**
     * @see org.apache.fop.render.Renderer#startPageSequence(LineArea)
     */
    public void startPageSequence(LineArea seqTitle) {
        handleDocumentExtensionAttachments();
        endPageSequence();  // move this before handleDocumentExtensionAttachments() ?
        startedSequence = true;
        startElement("pageSequence");
        if (seqTitle != null) {
            startElement("title");
            List children = seqTitle.getInlineAreas();

            for (int count = 0; count < children.size(); count++) {
                InlineArea inline = (InlineArea) children.get(count);
                renderInlineArea(inline);
            }

            endElement("title");
        }
    }

    /**
     * Tells the renderer to finish the current PageSequence
     */
    public void endPageSequence() {
        if (startedSequence) {
            endElement("pageSequence");
        }
        startedSequence = false;
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderRegionViewport(RegionViewport)
     */
    protected void renderRegionViewport(RegionViewport port) {
        if (port != null) {
            atts.clear();
            addAreaAttributes(port);
            addTraitAttributes(port);
            addAttribute("rect", port.getViewArea());
            if (port.isClip()) {
                addAttribute("clipped", "true");
            }
            startElement("regionViewport", atts);
            RegionReference region = port.getRegionReference();
            atts.clear();
            addAreaAttributes(region);
            addTraitAttributes(region);
            addAttribute("name", region.getRegionName());
            addAttribute("ctm", region.getCTM().toString());
            if (region.getRegionClass() == FO_REGION_BEFORE) {
                startElement("regionBefore", atts);
                renderRegion(region);
                endElement("regionBefore");
            } else if (region.getRegionClass() == FO_REGION_START) {
                startElement("regionStart", atts);
                renderRegion(region);
                endElement("regionStart");
            } else if (region.getRegionClass() == FO_REGION_BODY) {
                BodyRegion body = (BodyRegion)region;
                if (body.getColumnCount() != 1) {
                    addAttribute("columnGap", body.getColumnGap());
                    addAttribute("columnCount", body.getColumnCount());
                }
                startElement("regionBody", atts);
                renderBodyRegion(body);
                endElement("regionBody");
            } else if (region.getRegionClass() == FO_REGION_END) {
                startElement("regionEnd", atts);
                renderRegion(region);
                endElement("regionEnd");
            } else if (region.getRegionClass() == FO_REGION_AFTER) {
                startElement("regionAfter", atts);
                renderRegion(region);
                endElement("regionAfter");
            }
            endElement("regionViewport");
        }
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    protected void startVParea(CTM ctm, Rectangle2D clippingRect) {
        //only necessary for graphical output
    }

    /** @see org.apache.fop.render.AbstractRenderer#endVParea() */
    protected void endVParea() {
        //only necessary for graphical output
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderInlineAreaBackAndBorders(
     *          org.apache.fop.area.inline.InlineArea)
     */
    protected void renderInlineAreaBackAndBorders(InlineArea area) {
        //only necessary for graphical output
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderBeforeFloat(BeforeFloat)
     */
    protected void renderBeforeFloat(BeforeFloat bf) {
        startElement("beforeFloat");
        super.renderBeforeFloat(bf);
        endElement("beforeFloat");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderFootnote(Footnote)
     */
    protected void renderFootnote(Footnote footnote) {
        startElement("footnote");
        super.renderFootnote(footnote);
        endElement("footnote");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderMainReference(MainReference)
     */
    protected void renderMainReference(MainReference mr) {
        atts.clear();
        addAreaAttributes(mr);
        addTraitAttributes(mr);
        if (mr.getColumnCount() != 1) {
            addAttribute("columnGap", mr.getColumnGap());
        }
        startElement("mainReference", atts);

        Span span = null;
        List spans = mr.getSpans();
        for (int count = 0; count < spans.size(); count++) {
            span = (Span) spans.get(count);
            atts.clear();
            if (span.getColumnCount() != 1) {
                addAttribute("columnCount", span.getColumnCount());
            }
            addAreaAttributes(span);
            addTraitAttributes(span);
            startElement("span", atts);
            for (int c = 0; c < span.getColumnCount(); c++) {
                NormalFlow flow = (NormalFlow) span.getNormalFlow(c);

                renderFlow(flow);
            }
            endElement("span");
        }
        endElement("mainReference");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderFlow(NormalFlow)
     */
    protected void renderFlow(NormalFlow flow) {
        // the normal flow reference area contains stacked blocks
        atts.clear();
        addAreaAttributes(flow);
        addTraitAttributes(flow);
        startElement("flow", atts);
        super.renderFlow(flow);
        endElement("flow");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderBlock(Block)
     */
    protected void renderBlock(Block block) {
        atts.clear();
        addAreaAttributes(block);
        addTraitAttributes(block);
        int positioning = block.getPositioning();
        if (block instanceof BlockViewport) {
            BlockViewport bvp = (BlockViewport)block;
            boolean abspos = false;
            if (bvp.getPositioning() == Block.ABSOLUTE
                    || bvp.getPositioning() == Block.FIXED) {
                abspos = true;
            }
            if (abspos) {
                addAttribute("left-position", bvp.getXOffset());
                addAttribute("top-position", bvp.getYOffset());
            }
            addAttribute("ctm", bvp.getCTM().toString());
            if (bvp.getClip()) {
                addAttribute("clipped", "true");
            }
        } else {
            if (block.getXOffset() != 0) {
                addAttribute("left-offset", block.getXOffset());
            }
            if (block.getYOffset() != 0) {
                addAttribute("top-offset", block.getYOffset());
            }
        }
        switch (positioning) {
        case Block.RELATIVE:
            addAttribute("positioning", "relative");
            break;
        case Block.ABSOLUTE:
            addAttribute("positioning", "absolute");
            break;
        case Block.FIXED:
            addAttribute("positioning", "fixed");
            break;
        default: //nop
        }
        startElement("block", atts);
        super.renderBlock(block);
        endElement("block");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderLineArea(LineArea)
     */
    protected void renderLineArea(LineArea line) {
        atts.clear();
        addAreaAttributes(line);
        addTraitAttributes(line);
        startElement("lineArea", atts);
        super.renderLineArea(line);
        endElement("lineArea");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderInlineArea(InlineArea)
     */
    protected void renderInlineArea(InlineArea inlineArea) {
        atts.clear();
        if (inlineArea.getClass() == InlineArea.class) {
            // Generic inline area. This is implemented to allow the 0x0 "dummy"
            // area generated by fo:wrapper to pass its id.
            addAreaAttributes(inlineArea);
            addTraitAttributes(inlineArea);
            startElement("inline", atts);
            endElement("inline");
        } else {
            super.renderInlineArea(inlineArea);
            // calls specific renderers for Text, Space, Viewport, etc. etc.
        }
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderViewport(Viewport)
     */
    protected void renderViewport(Viewport viewport) {
        atts.clear();
        addAreaAttributes(viewport);
        addTraitAttributes(viewport);
        addAttribute("offset", viewport.getOffset());
        addAttribute("pos", viewport.getContentPosition());
        if (viewport.getClip()) {
            addAttribute("clip", "true");
        }
        startElement("viewport", atts);
        super.renderViewport(viewport);
        endElement("viewport");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer
     */
    public void renderImage(Image image, Rectangle2D pos) {
        atts.clear();
        addAreaAttributes(image);
        addTraitAttributes(image);
        addAttribute("url", image.getURL());
        //addAttribute("pos", pos);
        startElement("image", atts);
        endElement("image");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderContainer(Container)
     */
    public void renderContainer(Container cont) {
        startElement("container");
        super.renderContainer(cont);
        endElement("container");
    }

    /**
     * Renders an fo:foreing-object.
     * @param fo the foreign object
     * @param pos the position of the foreign object
     * @see org.apache.fop.render.AbstractRenderer#renderForeignObject(ForeignObject, Rectangle2D)
     */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        atts.clear();
        addAreaAttributes(fo);
        addTraitAttributes(fo);
        String ns = fo.getNameSpace();
        addAttribute("ns", ns);
        startElement("foreignObject", atts);
        Document doc = fo.getDocument();
        context.setProperty(XMLXMLHandler.HANDLER, handler);
        renderXML(context, doc, ns);
        endElement("foreignObject");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderInlineSpace(Space)
     */
    protected void renderInlineSpace(Space space) {
        atts.clear();
        addAreaAttributes(space);
        addTraitAttributes(space);
        addAttribute("offset", space.getOffset());
        startElement("space", atts);
        endElement("space");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderText(TextArea)
     */
    protected void renderText(TextArea text) {
        atts.clear();
        if (text.getTextWordSpaceAdjust() != 0) {
            addAttribute("twsadjust", text.getTextWordSpaceAdjust());
        }
        if (text.getTextLetterSpaceAdjust() != 0) {
            addAttribute("tlsadjust", text.getTextLetterSpaceAdjust());
        }
        addAttribute("offset", text.getOffset());
        addAttribute("baseline", text.getBaselineOffset());
        addAreaAttributes(text);
        addTraitAttributes(text);
        startElement("text", atts);
        super.renderText(text);
        endElement("text");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderWord(WordArea)
     */
    protected void renderWord(WordArea word) {
        atts.clear();
        addAttribute("offset", word.getOffset());
        int[] letterAdjust = word.getLetterAdjustArray(); 
        if (letterAdjust != null) {
            StringBuffer sb = new StringBuffer(64);
            boolean nonZeroFound = false;
            for (int i = 0, c = letterAdjust.length; i < c; i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(letterAdjust[i]);
                nonZeroFound |= (letterAdjust[i] != 0);
            }
            if (nonZeroFound) {
                addAttribute("letter-adjust", sb.toString());
            }
        }
        startElement("word", atts);
        characters(word.getWord());
        endElement("word");
        super.renderWord(word);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderSpace(SpaceArea)
     */
    protected void renderSpace(SpaceArea space) {
        atts.clear();
        addAttribute("offset", space.getOffset());
        if (!space.isAdjustable()) {
            addAttribute("adj", "false"); //default is true
        }
        startElement("space", atts);
        characters(space.getSpace());
        endElement("space");
        super.renderSpace(space);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderInlineParent(InlineParent)
     */
    protected void renderInlineParent(InlineParent ip) {
        atts.clear();
        addAreaAttributes(ip);
        addTraitAttributes(ip);
        addAttribute("offset", ip.getOffset());
        startElement("inlineparent", atts);
        super.renderInlineParent(ip);
        endElement("inlineparent");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderInlineBlockParent(InlineBlockParent)
     */
    protected void renderInlineBlockParent(InlineBlockParent ibp) {
        atts.clear();
        addAreaAttributes(ibp);
        addTraitAttributes(ibp);
        addAttribute("offset", ibp.getOffset());
        startElement("inlineblockparent", atts);
        super.renderInlineBlockParent(ibp);
        endElement("inlineblockparent");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderLeader(Leader)
     */
    protected void renderLeader(Leader area) {
        atts.clear();
        addAreaAttributes(area);
        addTraitAttributes(area);
        addAttribute("offset", area.getOffset());
        addAttribute("ruleStyle", area.getRuleStyleAsString());
        addAttribute("ruleThickness", area.getRuleThickness());
        startElement("leader", atts);
        endElement("leader");
        super.renderLeader(area);
    }

    /** @see org.apache.fop.render.AbstractRenderer#getMimeType() */
    public String getMimeType() {
        return XML_MIME_TYPE;
    }

}

