/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

/* $Id: XMLRenderer.java,v 1.20 2004/04/25 04:45:28 gmazza Exp $ */

package org.apache.fop.render.xml;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.awt.geom.Rectangle2D;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

// FOP
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.Area;
import org.apache.fop.area.BeforeFloat;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.NormalFlow;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MainReference;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Span;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineBlockParent;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.TextArea;

/**
 * Renderer that renders areas to XML for debugging purposes.
 * This creates an xml that contains the information of the area
 * tree. It does not output any state or derived information.
 * The output can be used to build a new area tree (@see AreaTreeBuilder)
 * which can be rendered to any renderer.
 */
public class XMLRenderer extends PrintRenderer {

    /** XML MIME type */
    public static final String XML_MIME_TYPE = "application/x-fop-areatree";

    /** Main namespace in use. */
    public static final String NS = "";
    
    /** CDATA type */
    public static final String CDATA = "CDATA";
    
    /** An empty Attributes object used when no attributes are needed. */
    public static final Attributes EMPTY_ATTS = new AttributesImpl();
    
    private boolean startedSequence = false;
    private RendererContext context;

    /** TransformerHandler that the generated XML is written to */
    protected TransformerHandler handler;
    
    /** AttributesImpl instance that can be used during XML generation. */
    protected AttributesImpl atts = new AttributesImpl();
    
    /** The OutputStream to write the generated XML to. */
    protected OutputStream out;
    
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

        //
        //userAgent.addExtensionHandler();
        XMLHandler handler = new XMLXMLHandler();
        userAgent.getXMLHandlerRegistry().addXMLHandler(handler);
    }

    /**
     * Sets an outside TransformerHandler to use instead of the default one
     * create in this class in startRenderer().
     * @param handler Overriding TransformerHandler
     */
    public void setTransformerHandler(TransformerHandler handler) {
        this.handler = handler;
    }

    private boolean isCoarseXml() {
        return ((Boolean) 
            userAgent.getRendererOptions().get("fineDetail")).booleanValue();
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
        try {
            handler.comment(comment.toCharArray(), 0, comment.length());
        } catch (SAXException saxe) {
            handleSAXException(saxe);
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
                String name = Trait.getTraitName(traitEntry.getKey());
                if ("break-before".equals(name) || "break-after".equals(name)) {
                    continue;
                }
                String value = traitEntry.getValue().toString();
                addAttribute(name, value);
                if ("font-family".equals(name)) {
                    addAttribute("font-name", fontInfo.getMetricsFor(value).getFontName());
                    addAttribute("font-style", fontInfo.getFontStyleFor(value));
                    addAttribute("font-weight", fontInfo.getFontWeightFor(value));
                }
            }
        }
    }

    private String createString(Rectangle2D rect) {
        return "" + (int) rect.getX() + " " + (int) rect.getY() + " "
                  + (int) rect.getWidth() + " " + (int) rect.getHeight();
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
                this.handler = factory.newTransformerHandler();
                StreamResult res = new StreamResult(outputStream);
                handler.setResult(res);
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
        comment("Produced by " 
            + (userAgent.getProducer() != null ? userAgent.getProducer() : ""));
        startElement("areaTree");
    }

    /**
     * @see org.apache.fop.render.Renderer#stopRenderer()
     */
    public void stopRenderer() throws IOException {
        if (startedSequence) {
            endElement("pageSequence");
        }
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
        addAttribute("nr", page.getPageNumberString());
        startElement("pageViewport", atts);
        startElement("page");
        super.renderPage(page);
        endElement("page");
        endElement("pageViewport");
    }

    /**
     * @see org.apache.fop.render.Renderer#startPageSequence(LineArea)
     */
    public void startPageSequence(LineArea seqTitle) {
        if (startedSequence) {
            endElement("pageSequence");
        }
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
     * @see org.apache.fop.render.AbstractRenderer#renderRegionViewport(RegionViewport)
     */
    protected void renderRegionViewport(RegionViewport port) {
        if (port != null) {
            atts.clear();
            addAreaAttributes(port);
            addTraitAttributes(port);
            addAttribute("rect", port.getViewArea());
            startElement("regionViewport", atts);
            RegionReference region = port.getRegionReference();
            atts.clear();
            addAreaAttributes(region);
            addTraitAttributes(region);
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
                startElement("regionBody", atts);
                renderBodyRegion((BodyRegion) region);
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
        addAttribute("columnGap", mr.getColumnGap());
        addAttribute("width", mr.getWidth());
        startElement("mainReference", atts);

        Span span = null;
        List spans = mr.getSpans();
        for (int count = 0; count < spans.size(); count++) {
            span = (Span) spans.get(count);
            atts.clear();
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
        if (block instanceof BlockViewport) {
            BlockViewport bvp = (BlockViewport)block;
            boolean abspos = false;
            if (bvp.getPositioning() == Block.ABSOLUTE) {
                addAttribute("positioning", "absolute");
                abspos = true;
            } else if (bvp.getPositioning() == Block.FIXED) {
                addAttribute("positioning", "fixed");
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
            if (block.getPositioning() == Block.RELATIVE) {
                addAttribute("positioning", "relative");
            }
            if (block.getXOffset() != 0) {
                addAttribute("left-offset", block.getXOffset());
            }
            if (block.getYOffset() != 0) {
                addAttribute("top-offset", block.getYOffset());
            }
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
        if (line.getStartIndent() != 0) {
            addAttribute("start-indent", line.getStartIndent());
        }
        startElement("lineArea", atts);
        super.renderLineArea(line);
        endElement("lineArea");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderViewport(Viewport)
     */
    protected void renderViewport(Viewport viewport) {
        atts.clear();
        addAreaAttributes(viewport);
        addTraitAttributes(viewport);
        addAttribute("offset", viewport.getOffset());
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
        addAttribute("pos", pos);
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
        addAttribute("pos", pos);
        startElement("foreignObject", atts);
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        context.setProperty(XMLXMLHandler.HANDLER, handler);
        renderXML(context, doc, ns);
        endElement("foreignObject");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderCharacter(Character)
     */
    protected void renderCharacter(org.apache.fop.area.inline.Character ch) {
        atts.clear();
        addAreaAttributes(ch);
        addTraitAttributes(ch);
        addAttribute("offset", ch.getOffset());
        addAttribute("baseline", ch.getBaselineOffset());
        startElement("char", atts);
        characters(ch.getChar());
        endElement("char");
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
        characters(text.getTextArea());
        endElement("text");
        super.renderText(text);
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
        startElement("inlineblockparent", atts);
        super.renderInlineBlockParent(ibp);
        endElement("inlineblockparent");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderLeader(Leader)
     */
    protected void renderLeader(Leader area) {
        String style = "solid";
        switch (area.getRuleStyle()) {
            case EN_DOTTED:
                style = "dotted";
                break;
            case EN_DASHED:
                style = "dashed";
                break;
            case EN_SOLID:
                break;
            case EN_DOUBLE:
                style = "double";
                break;
            case EN_GROOVE:
                style = "groove";
                break;
            case EN_RIDGE:
                style = "ridge";
                break;
            case EN_NONE:
                style = "none";
                break;
            default:
                style = "--NYI--";
        }
        atts.clear();
        addAreaAttributes(area);
        addTraitAttributes(area);
        addAttribute("offset", area.getOffset());
        addAttribute("ruleStyle", style);
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

