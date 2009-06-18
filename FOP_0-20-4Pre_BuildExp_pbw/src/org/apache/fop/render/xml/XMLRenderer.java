/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.xml;

// FOP
import org.apache.fop.svg.*;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.area.*;
import org.apache.fop.area.inline.*;
import org.apache.fop.pdf.*;
import org.apache.fop.fo.properties.LeaderPattern;
import org.apache.fop.fo.FOUserAgent;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.apps.FOPException;

// Avalon
import org.apache.avalon.framework.logger.Logger;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Document;

/**
 * Renderer that renders areas to XML for debugging purposes.
 * This creates an xml that contains the information of the area
 * tree. It does not output any state or derived information.
 * The output can be used to build a new area tree (@see AreaTreeBuilder)
 * which can be rendered to any renderer.
 */
public class XMLRenderer extends AbstractRenderer {
    public static final String mimeType = "text/xml";

    boolean startedSequence = false;
    RendererContext context;

    public void setProducer(String producer) {
    }

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

    public XMLRenderer() {
        context = new RendererContext(mimeType);
    }

    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);

        //
        //userAgent.addExtensionHandler();
        XMLHandler handler = new XMLXMLHandler();
        userAgent.setDefaultXMLHandler(mimeType, handler);
        String svg = "http://www.w3.org/2000/svg";
        userAgent.addXMLHandler(mimeType, svg, handler);
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

        /* use PDF's font setup to get PDF metrics */
        org.apache.fop.render.pdf.FontSetup.setup(fontInfo);
    }

    private boolean isCoarseXml() {
        return ((Boolean) options.get("fineDetail")).booleanValue();
    }

    /**
     */
    public void startRenderer(OutputStream outputStream)
    throws IOException {
        log.debug("rendering areas to XML");
        this.writer = new PrintWriter(outputStream);
        this.writer.write( "<?xml version=\"1.0\"?>\n<!-- produced by " +
                           this.producer + " -->\n");
        writeStartTag("<areaTree>");
    }

    /**
     */
    public void stopRenderer() throws IOException {
        writeEndTag("</pageSequence>");
        writeEndTag("</areaTree>");
        this.writer.flush();
        log.debug("written out XML");
    }

    public void renderPage(PageViewport page) throws IOException,
    FOPException {
        writeStartTag("<pageViewport bounds=\"" +
                      createString(page.getViewArea()) + "\">");
        writeStartTag("<page>");
        super.renderPage(page);
        writeEndTag("</page>");
        writeEndTag("</pageViewport>");
    }

    private String createString(Rectangle2D rect) {
        return "" + (int) rect.getX() + " " + (int) rect.getY() + " " +
               (int) rect.getWidth() + " " + (int) rect.getHeight();
    }

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
                inline.render(this);
            }

            writeEndTag("</title>");
        }
    }

    protected void renderRegionViewport(RegionViewport port) {
        if (port != null) {
            writeStartTag("<regionViewport rect=\"" +
                          createString(port.getViewArea()) + "\">");
            RegionReference region = port.getRegion();
            if (region.getRegionClass() == RegionReference.BEFORE) {
                writeStartTag("<regionBefore>");
                renderRegion(region);
                writeEndTag("</regionBefore>");
            } else if (region.getRegionClass() == RegionReference.START) {
                writeStartTag("<regionStart>");
                renderRegion(region);
                writeEndTag("</regionStart>");
            } else if (region.getRegionClass() == RegionReference.BODY) {
                writeStartTag("<regionBody>");
                renderBodyRegion((BodyRegion) region);
                writeEndTag("</regionBody>");
            } else if (region.getRegionClass() == RegionReference.END) {
                writeStartTag("<regionEnd>");
                renderRegion(region);
                writeEndTag("</regionEnd>");
            } else if (region.getRegionClass() == RegionReference.AFTER) {
                writeStartTag("<regionAfter>");
                renderRegion(region);
                writeEndTag("</regionAfter>");
            }
            writeEndTag("</regionViewport>");
        }
    }

    protected void renderBeforeFloat(BeforeFloat bf) {
        writeStartTag("<beforeFloat>");
        super.renderBeforeFloat(bf);
        writeEndTag("</beforeFloat>");
    }

    protected void renderFootnote(Footnote footnote) {
        writeStartTag("<footnote>");
        super.renderFootnote(footnote);
        writeEndTag("</footnote>");
    }

    protected void renderMainReference(MainReference mr) {
        writeStartTag("<mainReference columnGap=\"" +
                      mr.getColumnGap() + "\" width=\"" + mr.getWidth() + "\">");

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

    // the normal flow reference area contains stacked blocks
    protected void renderFlow(Flow flow) {
        writeStartTag("<flow>");
        super.renderFlow(flow);
        writeEndTag("</flow>");
    }

    protected void renderBlock(Block block) {
        String prop = "";
        List list = block.getTraitList();
        if (list != null) {
            prop = " props=\"" + getPropString(list) + "\"";
        }
        writeStartTag("<block" + prop + ">");
        super.renderBlock(block);
        writeEndTag("</block>");
    }

    protected void renderLineArea(LineArea line) {
        String prop = "";
        List list = line.getTraitList();
        if (list != null) {
            prop = " props=\"" + getPropString(list) + "\"";
        }
        writeStartTag("<lineArea height=\"" + line.getHeight() + "\"" +
                      prop + ">");
        super.renderLineArea(line);
        writeEndTag("</lineArea>");
    }

    public void renderViewport(Viewport viewport) {
        writeStartTag("<viewport>");
        super.renderViewport(viewport);
        writeEndTag("</viewport>");
    }

    public void renderImage(Image image) {
        writeElement("<image url=\"" + image.getURL() + "\"/>");
    }

    public void renderContainer(Container cont) {
        writeStartTag("<container>");

        super.renderContainer(cont);
        writeEndTag("</container>");
    }

    public void renderForeignObject(ForeignObject fo) {
        writeStartTag("<foreignObject>");
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        context.setProperty(XMLXMLHandler.WRITER, writer);
        userAgent.renderXML(context, doc, ns);
        writeEndTag("</foreignObject>");
    }

    public void renderCharacter(org.apache.fop.area.inline.Character ch) {
        String prop = "";
        List list = ch.getTraitList();
        if (list != null) {
            prop = " props=\"" + getPropString(list) + "\"";
        }
        writeElement("<char" + prop + ">" + ch.getChar() + "</char>");
    }

    public void renderInlineSpace(Space space) {
        writeElement("<space width=\"" + space.getWidth() + "\"/>");
    }

    public void renderWord(Word word) {
        String prop = "";
        List list = word.getTraitList();
        if (list != null) {
            prop = " props=\"" + getPropString(list) + "\"";
        }
        writeElement("<word" + prop + ">" + word.getWord() + "</word>");
        super.renderWord(word);
    }

    public void renderInlineParent(InlineParent ip) {
        String prop = "";
        List list = ip.getTraitList();
        if (list != null) {
            prop = " props=\"" + getPropString(list) + "\"";
        }
        writeStartTag("<inlineparent" + prop + ">");
        super.renderInlineParent(ip);
	writeEndTag("</inlineparent>");
    }

    public void renderLeader(Leader area) {
        String style = "solid";
        switch (area.getRuleStyle()) {
            case Leader.DOTTED:
                style = "dotted";
                break;
            case Leader.DASHED:
                style = "dashed";
                break;
            case Leader.SOLID:
                break;
            case Leader.DOUBLE:
                style = "double";
                break;
            case Leader.GROOVE:
                style = "groove";
                break;
            case Leader.RIDGE:
                style = "ridge";
                break;
        }
        writeElement("<leader width=\"" + area.getWidth() + "\" ruleStyle=\"" + style +
                     "\" ruleThickness=\"" + area.getRuleThickness() + "\"/>");
        super.renderLeader(area);
    }

    protected String getPropString(List list) {
        String str = "";
        for (int count = 0; count < list.size(); count++) {
            Trait prop = (Trait) list.get(count);
            switch (prop.propType) {
                case Trait.INTERNAL_LINK:
                    str += "internal-link:" + prop.data;
                    break;
                case Trait.EXTERNAL_LINK:
                    str += "external-link:" + prop.data;
                    break;
                case Trait.FONT_FAMILY:
                    str += "font-family:" + prop.data;
                    break;
                case Trait.FONT_SIZE:
                    str += "font-size:" + prop.data;
                    break;
                case Trait.FONT_WEIGHT:
                    str += "font-weight:" + prop.data;
                    break;
                case Trait.FONT_STYLE:
                    str += "font-style:" + prop.data;
                    break;
                case Trait.COLOR:
                    str += "color:" + prop.data;
                    break;
                case Trait.BACKGROUND:
                    str += "background:" + prop.data;
                    break;
                case Trait.UNDERLINE:
                    str += "underline:" + prop.data;
                    break;
                case Trait.OVERLINE:
                    str += "overline:" + prop.data;
                    break;
                case Trait.LINETHROUGH:
                    str += "linethrough:" + prop.data;
                    break;
                case Trait.OFFSET:
                    str += "offset:" + prop.data;
                    break;
                case Trait.SHADOW:
                    str += "shadow:" + prop.data;
                    break;
                default:
                    break;
            }
            str += ";";
        }
        return str;
    }
}

