/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Document;

// FOP
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.BeforeFloat;
import org.apache.fop.area.Block;
import org.apache.fop.area.BodyRegion;
import org.apache.fop.area.Flow;
import org.apache.fop.area.Footnote;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.MainReference;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Span;
import org.apache.fop.area.Title;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Container;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fo.FOTreeControl;
import org.apache.fop.fo.pagination.Region;

/**
 * Renderer that renders areas to XML for debugging purposes.
 * This creates an xml that contains the information of the area
 * tree. It does not output any state or derived information.
 * The output can be used to build a new area tree (@see AreaTreeBuilder)
 * which can be rendered to any renderer.
 */
public class XMLRenderer extends AbstractRenderer {

    /** XML MIME type */
    public static final String XML_MIME_TYPE = "text/xml";

    private boolean startedSequence = false;
    private RendererContext context;

    /**
     * indentation to use for pretty-printing the XML
     */
    protected int indent = 0;

    /**
     * the application producing the XML
     */
    protected String producer;

    /**
     * the writer used to output the XML
     */
    protected PrintWriter writer;

    /**
     * options
     */
    private boolean consistentOutput = false;

    /**
     * Creates a new XML renderer.
     */
    public XMLRenderer() {
        context = new RendererContext(XML_MIME_TYPE);
    }

    /**
     * @see org.apache.fop.render.Renderer#setUserAgent(FOUserAgent)
     */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);

        //
        //userAgent.addExtensionHandler();
        XMLHandler handler = new XMLXMLHandler();
        setDefaultXMLHandler(userAgent, XML_MIME_TYPE, handler);
        String svg = "http://www.w3.org/2000/svg";
        addXMLHandler(userAgent, XML_MIME_TYPE, svg, handler);
    }

    /**
     * write out spaces to make indent
     */
    protected void writeIndent() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < this.indent; i++) {
            s = s.append("  ");
        }
        this.writer.write(s.toString());
    }

    /**
     * write out an element
     *
     * @param element the full text of the element including tags
     */
    protected void writeElement(String element) {
        writeIndent();
        this.writer.write(element + "\n");
    }

    /**
     * write out an empty-element-tag
     *
     * @param tag the text of the tag
     */
    protected void writeEmptyElementTag(String tag) {
        writeIndent();
        this.writer.write(tag + "\n");
    }

    /**
     * write out an end tag
     *
     * @param tag the text of the tag
     */
    protected void writeEndTag(String tag) {
        this.indent--;
        writeIndent();
        this.writer.write(tag + "\n");
    }

    /**
     * write out a start tag
     *
     * @param tag the text of the tag
     */
    protected void writeStartTag(String tag) {
        writeIndent();
        this.writer.write(tag + "\n");
        this.indent++;
    }

    /**
     * set up the font info
     *
     * @param fontInfo the font info object to set up
     */
    public void setupFontInfo(FontInfo fontInfo) {
        FontSetup.setup(fontInfo, null);
    }

    private boolean isCoarseXml() {
        return ((Boolean) options.get("fineDetail")).booleanValue();
    }

    /**
     * @see org.apache.fop.render.Renderer#startRenderer(OutputStream)
     */
    public void startRenderer(OutputStream outputStream)
                throws IOException {
        getLogger().debug("rendering areas to XML");
        this.writer = new PrintWriter(outputStream);
        this.writer.write("<?xml version=\"1.0\"?>\n"
                + "<!-- produced by " + this.producer + " -->\n");
        writeStartTag("<areaTree>");
    }

    /**
     * @see org.apache.fop.render.Renderer#stopRenderer()
     */
    public void stopRenderer() throws IOException {
        writeEndTag("</pageSequence>");
        writeEndTag("</areaTree>");
        this.writer.flush();
        getLogger().debug("written out XML");
    }

    /**
     * @see org.apache.fop.render.Renderer#renderPage(PageViewport)
     */
    public void renderPage(PageViewport page) throws IOException, FOPException {
        writeStartTag("<pageViewport bounds=\""
                      + createString(page.getViewArea()) + "\">");
        writeStartTag("<page>");
        super.renderPage(page);
        writeEndTag("</page>");
        writeEndTag("</pageViewport>");
    }

    private String createString(Rectangle2D rect) {
        return "" + (int) rect.getX() + " " + (int) rect.getY() + " "
                  + (int) rect.getWidth() + " " + (int) rect.getHeight();
    }

    /**
     * @see org.apache.fop.render.Renderer#startPageSequence(Title)
     */
    public void startPageSequence(Title seqTitle) {
        if (startedSequence) {
            writeEndTag("</pageSequence>");
        }
        startedSequence = true;
        writeStartTag("<pageSequence>");
        if (seqTitle != null) {
            writeStartTag("<title>");
            List children = seqTitle.getInlineAreas();

            for (int count = 0; count < children.size(); count++) {
                InlineArea inline = (InlineArea) children.get(count);
                renderInlineArea(inline);
            }

            writeEndTag("</title>");
        }
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderRegionViewport(RegionViewport)
     */
    protected void renderRegionViewport(RegionViewport port) {
        if (port != null) {
            writeStartTag("<regionViewport rect=\""
                          + createString(port.getViewArea()) + "\">");
            RegionReference region = port.getRegion();
            if (region.getRegionClass() == Region.BEFORE_CODE) {
                writeStartTag("<regionBefore>");
                renderRegion(region);
                writeEndTag("</regionBefore>");
            } else if (region.getRegionClass() == Region.START_CODE) {
                writeStartTag("<regionStart>");
                renderRegion(region);
                writeEndTag("</regionStart>");
            } else if (region.getRegionClass() == Region.BODY_CODE) {
                writeStartTag("<regionBody>");
                renderBodyRegion((BodyRegion) region);
                writeEndTag("</regionBody>");
            } else if (region.getRegionClass() == Region.END_CODE) {
                writeStartTag("<regionEnd>");
                renderRegion(region);
                writeEndTag("</regionEnd>");
            } else if (region.getRegionClass() == Region.AFTER_CODE) {
                writeStartTag("<regionAfter>");
                renderRegion(region);
                writeEndTag("</regionAfter>");
            }
            writeEndTag("</regionViewport>");
        }
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderBeforeFloat(BeforeFloat)
     */
    protected void renderBeforeFloat(BeforeFloat bf) {
        writeStartTag("<beforeFloat>");
        super.renderBeforeFloat(bf);
        writeEndTag("</beforeFloat>");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderFootnote(Footnote)
     */
    protected void renderFootnote(Footnote footnote) {
        writeStartTag("<footnote>");
        super.renderFootnote(footnote);
        writeEndTag("</footnote>");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderMainReference(MainReference)
     */
    protected void renderMainReference(MainReference mr) {
        writeStartTag("<mainReference columnGap=\""
                      + mr.getColumnGap() + "\" width=\"" + mr.getWidth() + "\">");

        Span span = null;
        List spans = mr.getSpans();
        for (int count = 0; count < spans.size(); count++) {
            span = (Span) spans.get(count);
            writeStartTag("<span>");
            for (int c = 0; c < span.getColumnCount(); c++) {
                Flow flow = (Flow) span.getFlow(c);

                renderFlow(flow);
            }
            writeEndTag("</span>");
        }
        writeEndTag("</mainReference>");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderFlow(Flow)
     */
    protected void renderFlow(Flow flow) {
        // the normal flow reference area contains stacked blocks
        writeStartTag("<flow>");
        super.renderFlow(flow);
        writeEndTag("</flow>");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderBlock(Block)
     */
    protected void renderBlock(Block block) {
        String prop = " width=\"" + block.getWidth() +
                      "\" ipd=\"" + block.getIPD() +
                      "\" height=\"" + block.getHeight() + "\"";
        Map map = block.getTraits();
        if (map != null) {
            prop = prop + " props=\"" + getPropString(map) + "\"";
        }
        writeStartTag("<block" + prop + ">");
        super.renderBlock(block);
        writeEndTag("</block>");
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderLineArea(LineArea)
     */
    protected void renderLineArea(LineArea line) {
        String prop = "";
        Map map = line.getTraits();
        if (map != null) {
            prop = " props=\"" + getPropString(map) + "\"";
        }
        writeStartTag("<lineArea height=\"" + line.getHeight() + "\""
                      + prop + ">");
        super.renderLineArea(line);
        writeEndTag("</lineArea>");
    }

    /**
     * @see org.apache.fop.render.Renderer#renderViewport(Viewport)
     */
    protected void renderViewport(Viewport viewport) {
        writeStartTag("<viewport>");
        super.renderViewport(viewport);
        writeEndTag("</viewport>");
    }

    /**
     * Renders an image
     * @param image the image
     */
    public void renderImage(Image image) {
        writeElement("<image url=\"" + image.getURL() + "\"/>");
    }

    /**
     * @see org.apache.fop.render.Renderer#renderContainer(Container)
     */
    public void renderContainer(Container cont) {
        writeStartTag("<container>");

        super.renderContainer(cont);
        writeEndTag("</container>");
    }

    /**
     * Renders an fo:foreing-object.
     * @param fo the foreign object
     */
    public void renderForeignObject(ForeignObject fo) {
        writeStartTag("<foreignObject>");
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        context.setProperty(XMLXMLHandler.WRITER, writer);
        renderXML(userAgent, context, doc, ns);
        writeEndTag("</foreignObject>");
    }

    /**
     * @see org.apache.fop.render.Renderer#renderCharacter(Character)
     */
    protected void renderCharacter(org.apache.fop.area.inline.Character ch) {
        String prop = "";
        Map map = ch.getTraits();
        if (map != null) {
            prop = " props=\"" + getPropString(map) + "\"";
        }
        writeElement("<char" + prop + ">" + ch.getChar() + "</char>");
    }

    /**
     * @see org.apache.fop.render.Renderer#renderInlineSpace(Space)
     */
    protected void renderInlineSpace(Space space) {
        writeElement("<space width=\"" + space.getWidth() + "\"/>");
    }

    /**
     * @see org.apache.fop.render.Renderer#renderText(TextArea)
     */
    protected void renderText(TextArea text) {
        String prop = "";
        Map map = text.getTraits();
        if (map != null) {
            prop = " props=\"" + getPropString(map) + "\"";
        }
        writeElement("<text tsadjust=\"" + text.getTextSpaceAdjust() + "\""
             + prop + ">" + text.getTextArea() + "</text>");
        super.renderText(text);
    }

    /**
     * @see org.apache.fop.render.Renderer#renderInlineParent(InlineParent)
     */
    protected void renderInlineParent(InlineParent ip) {
        String prop = "";
        Map map = ip.getTraits();
        if (map != null) {
            prop = " props=\"" + getPropString(map) + "\"";
        }
        writeStartTag("<inlineparent" + prop + ">");
        super.renderInlineParent(ip);
        writeEndTag("</inlineparent>");
    }

    /**
     * @see org.apache.fop.render.Renderer#renderLeader(Leader)
     */
    protected void renderLeader(Leader area) {
        String style = "solid";
        switch (area.getRuleStyle()) {
            case RuleStyle.DOTTED:
                style = "dotted";
                break;
            case RuleStyle.DASHED:
                style = "dashed";
                break;
            case RuleStyle.SOLID:
                break;
            case RuleStyle.DOUBLE:
                style = "double";
                break;
            case RuleStyle.GROOVE:
                style = "groove";
                break;
            case RuleStyle.RIDGE:
                style = "ridge";
                break;
        }
        writeElement("<leader width=\"" + area.getWidth()
                        + "\" ruleStyle=\"" + style
                        + "\" ruleThickness=\"" + area.getRuleThickness()
                        + "\"/>");
        super.renderLeader(area);
    }

    /**
     * Builds a String with attributes from the trait map.
     * @param traitMap the trait map
     * @return String the generated attributes
     */
    protected String getPropString(Map traitMap) {
        StringBuffer strbuf = new StringBuffer();
        Iterator iter = traitMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry traitEntry = (Map.Entry) iter.next();
            strbuf.append(Trait.getTraitName(traitEntry.getKey()));
            strbuf.append(':');
            strbuf.append(traitEntry.getValue().toString());
            strbuf.append(';');
        }
        return strbuf.toString();
    }

}

