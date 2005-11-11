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

/* $Id$ */

package org.apache.fop.render.svg;

import org.apache.fop.apps.FOPException;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.svg.SVGUtilities;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.apps.FOUserAgent;

import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;
/* org.w3c.dom.Document is not imported to avoid conflict with
   org.apache.fop.control.Document */
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.dom.util.DOMUtilities;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;

/**
 * This is the SVG renderer.
 */
public class SVGRenderer extends AbstractRenderer implements XMLHandler {

    /** SVG MIME type */
    public static final String SVG_MIME_TYPE = "image/svg+xml";

    /** SVG namespace */
    public static final String SVG_NAMESPACE = SVGDOMImplementation.SVG_NAMESPACE_URI;

    private org.w3c.dom.Document svgDocument;
    private Element svgRoot;
    private Element currentPageG = null;
    private Element lastLink = null;
    private String lastViewbox = null;

    private Element docDefs = null;
    private Element pageDefs = null;
    private Element pagesGroup = null;

    // first sequence title
    private LineArea docTitle = null;

    private RendererContext context;

    private OutputStream ostream;

    private float totalWidth = 0;
    private float totalHeight = 0;
    private float sequenceWidth = 0;
    private float sequenceHeight = 0;

    private float pageWidth = 0;
    private float pageHeight = 0;
    private int pageNumber = 0;

    private HashMap fontNames = new HashMap();
    private HashMap fontStyles = new HashMap();
    private Color saveColor = null;

    /**
     * The current (internal) font name
     */
    private String currentFontName;

    /**
     * The current font size in millipoints
     */
    private int currentFontSize;

    /**
     * The current colour's red, green and blue component
     */
    private float currentRed = 0;
    private float currentGreen = 0;
    private float currentBlue = 0;

    /**
     * Creates a new SVG renderer.
     */
    public SVGRenderer() {
        context = new RendererContext(this, SVG_MIME_TYPE);
    }

    /**
     * @see org.apache.fop.render.Renderer#setUserAgent(FOUserAgent)
     */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
        userAgent.getXMLHandlerRegistry().addXMLHandler(this);
    }

    /**
     * @see org.apache.fop.render.Renderer#setupFontInfo(FontInfo)
     */
    public void setupFontInfo(FontInfo fontInfo) {
        // create a temp Image to test font metrics on
        BufferedImage fontImage =
          new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        org.apache.fop.render.java2d.FontSetup.setup(fontInfo,
                fontImage.createGraphics());
    }

    /**
     * @see org.apache.fop.render.Renderer#startRenderer(OutputStream)
     */
    public void startRenderer(OutputStream outputStream)
                throws IOException {
        ostream = outputStream;
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        svgDocument = impl.createDocument(SVG_NAMESPACE, "svg", null);
        svgRoot = svgDocument.getDocumentElement();
        /*
        ProcessingInstruction pi =
            svgDocument.createProcessingInstruction("xml",
                        " version=\"1.0\" encoding=\"ISO-8859-1\"");
        svgDocument.insertBefore(pi, svgRoot);
        */

        docDefs = svgDocument.createElementNS(SVG_NAMESPACE, "defs");
        svgRoot.appendChild(docDefs);

        pagesGroup = svgDocument.createElementNS(SVG_NAMESPACE, "g");
        pageDefs = svgDocument.createElementNS(SVG_NAMESPACE, "defs");
        pagesGroup.appendChild(pageDefs);
        svgRoot.appendChild(pagesGroup);

    }

    /**
     * @see org.apache.fop.render.Renderer#stopRenderer()
     */
    public void stopRenderer() throws IOException {
        totalWidth += sequenceWidth;
        if (sequenceHeight > totalHeight) {
            totalHeight = sequenceHeight;
        }

        svgRoot.setAttributeNS(null, "width", "" + (totalWidth + 1));
        svgRoot.setAttributeNS(null, "height", "" + (totalHeight + 1));
        //svgRoot.setAttributeNS(null, "viewBox", "0 0 " + pageWidth + " " + pageHeight);
        SVGTranscoder svgT = new SVGTranscoder();
        TranscoderInput input = new TranscoderInput(svgDocument);
        TranscoderOutput output =
          new TranscoderOutput(new OutputStreamWriter(ostream));
        try {
            svgT.transcode(input, output);
        } catch (TranscoderException e) {
            log.error("could not write svg file :" + e.getMessage(), e);
        }
        ostream.flush();
        ostream = null;

        svgDocument = null;
        svgRoot = null;
        currentPageG = null;
        lastLink = null;

        totalWidth = 0;
        totalHeight = 0;

        pageNumber = 0;
    }

    /**
     * @see org.apache.fop.render.Renderer#startPageSequence(LineArea)
     */
    public void startPageSequence(LineArea seqTitle) {
        totalWidth += sequenceWidth;
        if (sequenceHeight > totalHeight) {
            totalHeight = sequenceHeight;
        }
        sequenceWidth = 0;
        sequenceHeight = 0;
        if (seqTitle != null && docTitle == null) {
            // convert first title to a string and set for svg document title
            docTitle = seqTitle;
            String str = convertTitleToString(seqTitle);
            Element svgTitle = svgDocument.createElementNS(SVG_NAMESPACE, "title");
            Text strNode = svgDocument.createTextNode(str);
            svgTitle.appendChild(strNode);
            svgRoot.insertBefore(svgTitle, svgRoot.getFirstChild());
        }
    }

    /**
     * @see org.apache.fop.render.Renderer#renderPage(PageViewport)
     */
    public void renderPage(PageViewport page) throws IOException, FOPException {
        float lastWidth = pageWidth;
        float lastHeight = pageHeight;

        Rectangle2D area = page.getViewArea();
        pageWidth = (float) area.getWidth() / 1000f;
        pageHeight = (float) area.getHeight() / 1000f;

        // if there is a link from the last page
        if (lastLink != null) {
            lastLink.setAttributeNS(null, "xlink:href", "#svgView(viewBox("
                                    + totalWidth + ", "
                                    + sequenceHeight + ", "
                                    + pageWidth + ", "
                                    + pageHeight + "))");
            pagesGroup.appendChild(lastLink);
        }

        currentPageG = svgDocument.createElementNS(SVG_NAMESPACE, "svg");
        currentPageG.setAttributeNS(null, "viewbox",
                                    "0 0 " + (int) pageWidth + " " + (int) pageHeight);
        currentPageG.setAttributeNS(null, "width",
                                    "" + ((int) pageWidth + 1));
        currentPageG.setAttributeNS(null, "height",
                                    "" + ((int) pageHeight + 1));
        currentPageG.setAttributeNS(null, "id", "Page-" + pageNumber);
        currentPageG.setAttributeNS(null, "style", "font-family:sanserif;font-size:12");
        pageDefs.appendChild(currentPageG);

        if (pageWidth > sequenceWidth) {
            sequenceWidth = pageWidth;
        }
        sequenceHeight += pageHeight;

        Element border =
          SVGUtilities.createRect(svgDocument, 0, 0, pageWidth,
                                  pageHeight);
        border.setAttributeNS(null, "style", "fill:none;stroke:black");
        currentPageG.appendChild(border);

        // render the page contents
        super.renderPage(page);

        Element use = svgDocument.createElementNS(SVG_NAMESPACE, "use");
        use.setAttributeNS(null, "xlink:href", "#Page-" + pageNumber);
        use.setAttributeNS(null, "x", "" + totalWidth);
        use.setAttributeNS(null, "y", "" + (sequenceHeight - pageHeight));
        pagesGroup.appendChild(use);

        Element lastPageLink = svgDocument.createElementNS(SVG_NAMESPACE, "a");
        if (lastLink != null) {
            lastPageLink.setAttributeNS(null, "xlink:href", lastViewbox);
        } else {
            lastPageLink.setAttributeNS(null, "xlink:href",
                    "#svgView(viewBox("
                        + totalWidth + ", "
                        + (sequenceHeight - pageHeight) + ", "
                        + pageWidth + ", "
                        + pageHeight + "))");
        }
        pagesGroup.appendChild(lastPageLink);

        // setup a link to the next page, only added when the
        // next page is rendered
        Element rect = SVGUtilities.createRect(svgDocument, totalWidth,
                    (sequenceHeight - pageHeight), pageWidth / 2, pageHeight);
        rect.setAttributeNS(null, "style", "fill:blue;visibility:hidden");
        lastPageLink.appendChild(rect);

        lastLink = svgDocument.createElementNS(SVG_NAMESPACE, "a");
        rect = SVGUtilities.createRect(svgDocument,
                                       totalWidth + pageWidth / 2,
                                       (sequenceHeight - pageHeight), pageWidth / 2, pageHeight);
        rect.setAttributeNS(null, "style", "fill:blue;visibility:hidden");
        lastLink.appendChild(rect);

        lastViewbox = "#svgView(viewBox("
                    + totalWidth + ", "
                    + (sequenceHeight - pageHeight) + ", "
                    + pageWidth + ", "
                    + pageHeight + "))";

        pageNumber++;

    }

    /**
     * Method renderForeignObject.
     * @param fo the foreign object
     */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        org.w3c.dom.Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        renderXML(context, doc, ns);
    }

    /** @see org.apache.fop.render.XMLHandler */
    public void handleXML(RendererContext context, 
                org.w3c.dom.Document doc, String ns) throws Exception {
        if (SVG_NAMESPACE.equals(ns)) {
            if (!(doc instanceof SVGDocument)) {
                DOMImplementation impl =
                  SVGDOMImplementation.getDOMImplementation();
                doc = DOMUtilities.deepCloneDocument(doc, impl);
            }
            SVGSVGElement svg = ((SVGDocument) doc).getRootElement();
            Element view = svgDocument.createElementNS(SVG_NAMESPACE, "svg");
            Node newsvg = svgDocument.importNode(svg, true);
            //view.setAttributeNS(null, "viewBox", "0 0 ");
            view.setAttributeNS(null, "x", "" + currentIPPosition / 1000f);
            view.setAttributeNS(null, "y", "" + currentBPPosition / 1000f);

            // this fixes a problem where the xmlns is repeated sometimes
            Element ele = (Element) newsvg;
            ele.setAttributeNS(XMLSupport.XMLNS_NAMESPACE_URI, "xmlns",
                               SVG_NAMESPACE);
            if (ele.hasAttributeNS(null, "xmlns")) {
                ele.removeAttributeNS(null, "xmlns");
            }

            view.appendChild(newsvg);
            currentPageG.appendChild(view);
        }
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderLeader(Leader)
     */
    public void renderLeader(Leader area) {
        String style = "stroke:black;stroke-width:"
                       + (area.getRuleThickness() / 1000) + ";";
        switch (area.getRuleStyle()) {
            case EN_DOTTED:
                style += "stroke-dasharray:1,1";
                break;
            case EN_DASHED:
                style += "stroke-dasharray:5,1";
                break;
            case EN_SOLID:
                break;
            case EN_DOUBLE:
                break;
            case EN_GROOVE:
                break;
            case EN_RIDGE:
                break;
        }
        Element line = SVGUtilities.createLine(svgDocument,
                        currentIPPosition / 1000,
                        (currentBPPosition + area.getOffset()
                            - area.getRuleThickness() / 2) / 1000,
                        (currentIPPosition + area.getIPD()) / 1000,
                        (currentBPPosition + area.getOffset()
                            - area.getRuleThickness() / 2) / 1000);
        line.setAttributeNS(null, "style", style);
        currentPageG.appendChild(line);

        super.renderLeader(area);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderText(TextArea)
     */
    public void renderText(TextArea text) {
        Element textElement = SVGUtilities.createText(svgDocument,
                                               currentIPPosition / 1000,
                                               (currentBPPosition + text.getOffset() 
                                                + text.getBaselineOffset()) / 1000,
                                               text.getText());
        currentPageG.appendChild(textElement);

        super.renderText(text);
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#renderCharacter(Character)
     */
    public void renderCharacter(org.apache.fop.area.inline.Character ch) {
        Element text = SVGUtilities.createText(svgDocument,
                                               currentIPPosition / 1000,
                                               (currentBPPosition + ch.getOffset()
                                                + ch.getBaselineOffset()) / 1000,
                                               "" + ch.getChar());
        currentPageG.appendChild(text);

        super.renderCharacter(ch);
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return SVG_MIME_TYPE;
    }

    /** @see org.apache.fop.render.XMLHandler#getNamespace() */
    public String getNamespace() {
        return SVG_NAMESPACE;
    }

}

