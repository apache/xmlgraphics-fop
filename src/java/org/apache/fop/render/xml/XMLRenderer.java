/*
 * $Id: XMLRenderer.java,v 1.43 2003/03/07 09:46:34 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.render.xml;

// Java
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.awt.geom.Rectangle2D;

// XML
/* org.w3c.dom.Document is not imported to avoid conflict with
   org.apache.fop.control.Document */

// FOP
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.fo.FOUserAgent;
import org.apache.fop.control.Document;
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
import org.apache.fop.area.inline.Word;
import org.apache.fop.fo.properties.RuleStyle;
import org.apache.fop.fo.FOTreeControl;

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
        userAgent.setDefaultXMLHandler(XML_MIME_TYPE, handler);
        String svg = "http://www.w3.org/2000/svg";
        userAgent.addXMLHandler(XML_MIME_TYPE, svg, handler);
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
    public void setupFontInfo(FOTreeControl foTreeControl) {

        /* use PDF's font setup to get PDF metrics */
        org.apache.fop.render.pdf.FontSetup.setup((Document)foTreeControl, null);
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
                inline.render(this);
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
        String prop = "";
        Map map = block.getTraits();
        if (map != null) {
            prop = " props=\"" + getPropString(map) + "\"";
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
    public void renderViewport(Viewport viewport) {
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
        org.w3c.dom.Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        context.setProperty(XMLXMLHandler.WRITER, writer);
        userAgent.renderXML(context, doc, ns);
        writeEndTag("</foreignObject>");
    }

    /**
     * @see org.apache.fop.render.Renderer#renderCharacter(Character)
     */
    public void renderCharacter(org.apache.fop.area.inline.Character ch) {
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
    public void renderInlineSpace(Space space) {
        writeElement("<space width=\"" + space.getWidth() + "\"/>");
    }

    /**
     * @see org.apache.fop.render.Renderer#renderWord(Word)
     */
    public void renderWord(Word word) {
        String prop = "";
        Map map = word.getTraits();
        if (map != null) {
            prop = " props=\"" + getPropString(map) + "\"";
        }
        writeElement("<word wsadjust=\"" + word.getWSadjust() + "\""
             + prop + ">" + word.getWord() + "</word>");
        super.renderWord(word);
    }

    /**
     * @see org.apache.fop.render.Renderer#renderInlineParent(InlineParent)
     */
    public void renderInlineParent(InlineParent ip) {
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
    public void renderLeader(Leader area) {
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

