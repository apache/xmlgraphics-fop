/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.svg;

import org.apache.fop.apps.FOPException;
import org.apache.fop.area.*;
import org.apache.fop.area.inline.*;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.image.*;
import org.apache.fop.svg.SVGUtilities;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.fo.FOUserAgent;

import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Text;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.dom.util.DOMUtilities;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.swing.ImageIcon;

import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;

public class SVGRenderer extends AbstractRenderer implements XMLHandler {
    public static final String mimeType = "image/svg+xml";
    static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    Document svgDocument;
    Element svgRoot;
    Element currentPageG = null;
    Element lastLink = null;
    String lastViewbox = null;

    Element docDefs = null;
    Element pageDefs = null;
    Element pagesGroup = null;

    // first sequence title
    Title docTitle = null;

    RendererContext context;

    OutputStream ostream;

    float totalWidth = 0;
    float totalHeight = 0;
    float sequenceWidth = 0;
    float sequenceHeight = 0;

    protected float pageWidth = 0;
    protected float pageHeight = 0;
    protected int pageNumber = 0;

    protected HashMap fontNames = new HashMap();
    protected HashMap fontStyles = new HashMap();
    protected Color saveColor = null;

    protected IDReferences idReferences = null;

    /**
     * The current (internal) font name
     */
    protected String currentFontName;

    /**
     * The current font size in millipoints
     */
    protected int currentFontSize;

    /**
     * The current colour's red, green and blue component
     */
    protected float currentRed = 0;
    protected float currentGreen = 0;
    protected float currentBlue = 0;

    public SVGRenderer() {
        context = new RendererContext(mimeType);
    }

    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
        userAgent.setDefaultXMLHandler(mimeType, this);
        userAgent.addXMLHandler(mimeType, svgNS, this);
    }

    public void setupFontInfo(FontInfo fontInfo) {
        // create a temp Image to test font metrics on
        BufferedImage fontImage =
          new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        org.apache.fop.render.awt.FontSetup.setup(fontInfo,
                fontImage.createGraphics());
    }

    public void setProducer(String producer) {
    }

    public void startRenderer(OutputStream outputStream)
    throws IOException {
        ostream = outputStream;
        DOMImplementation impl =
          SVGDOMImplementation.getDOMImplementation();
        svgDocument = impl.createDocument(svgNS, "svg", null);
        ProcessingInstruction pi =
          svgDocument.createProcessingInstruction("xml", " version=\"1.0\" encoding=\"ISO-8859-1\"");
        svgRoot = svgDocument.getDocumentElement();
        svgDocument.insertBefore(pi, svgRoot);

        docDefs = svgDocument.createElementNS(svgNS, "defs");
        svgRoot.appendChild(docDefs);

        pagesGroup = svgDocument.createElementNS(svgNS, "g");
        pageDefs = svgDocument.createElementNS(svgNS, "defs");
        pagesGroup.appendChild(pageDefs);
        svgRoot.appendChild(pagesGroup);

    }

    /**
     *
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

    public void startPageSequence(Title seqTitle) {
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
            Element svgTitle = svgDocument.createElementNS(svgNS, "title");
            Text strNode = svgDocument.createTextNode(str);
            svgTitle.appendChild(strNode);
            svgRoot.insertBefore(svgTitle, svgRoot.getFirstChild());
        }
    }

    public void renderPage(PageViewport page) throws IOException,
    FOPException {
        float lastWidth = pageWidth;
        float lastHeight = pageHeight;

        Rectangle2D area = page.getViewArea();
        pageWidth = (float) area.getWidth() / 1000f;
        pageHeight = (float) area.getHeight() / 1000f;

        // if there is a link from the last page
        if (lastLink != null) {
            lastLink.setAttributeNS(null, "xlink:href",
                                    "#svgView(viewBox(" + totalWidth + ", "+
                                    sequenceHeight + ", " + pageWidth + ", " +
                                    pageHeight + "))");
            pagesGroup.appendChild(lastLink);
        }

        currentPageG = svgDocument.createElementNS(svgNS, "svg");
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

        Element use = svgDocument.createElementNS(svgNS, "use");
        use.setAttributeNS(null, "xlink:href", "#Page-" + pageNumber);
        use.setAttributeNS(null, "x", "" + totalWidth);
        use.setAttributeNS(null, "y", "" + (sequenceHeight - pageHeight));
        pagesGroup.appendChild(use);

        Element lastPageLink = svgDocument.createElementNS(svgNS, "a");
        if (lastLink != null) {
            lastPageLink.setAttributeNS(null, "xlink:href", lastViewbox);
        } else {
            lastPageLink.setAttributeNS(null, "xlink:href",
                                        "#svgView(viewBox(" + totalWidth + ", " +
                                        (sequenceHeight - pageHeight) + ", " + pageWidth +
                                        ", " + pageHeight + "))");
        }
        pagesGroup.appendChild(lastPageLink);

        // setup a link to the next page, only added when the
        // next page is rendered
        Element rect = SVGUtilities.createRect(svgDocument, totalWidth,
                                               (sequenceHeight - pageHeight), pageWidth / 2, pageHeight);
        rect.setAttributeNS(null, "style", "fill:blue;visibility:hidden");
        lastPageLink.appendChild(rect);

        lastLink = svgDocument.createElementNS(svgNS, "a");
        rect = SVGUtilities.createRect(svgDocument,
                                       totalWidth + pageWidth / 2,
                                       (sequenceHeight - pageHeight), pageWidth / 2, pageHeight);
        rect.setAttributeNS(null, "style", "fill:blue;visibility:hidden");
        lastLink.appendChild(rect);

        lastViewbox = "#svgView(viewBox(" + totalWidth + ", " +
                      (sequenceHeight - pageHeight) + ", " + pageWidth +
                      ", " + pageHeight + "))";

        pageNumber++;

    }

    public void renderForeignObject(ForeignObject fo) {
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        userAgent.renderXML(context, doc, ns);
    }

    public void handleXML(RendererContext context, Document doc,
                          String ns) throws Exception {
        if (svgNS.equals(ns)) {
            if (!(doc instanceof SVGDocument)) {
                DOMImplementation impl =
                  SVGDOMImplementation.getDOMImplementation();
                doc = DOMUtilities.deepCloneDocument(doc, impl);
            }
            SVGSVGElement svg = ((SVGDocument) doc).getRootElement();
            Element view = svgDocument.createElementNS(svgNS, "svg");
            Node newsvg = svgDocument.importNode(svg, true);
            //view.setAttributeNS(null, "viewBox", "0 0 ");
            view.setAttributeNS(null, "x",
                                "" + currentBlockIPPosition / 1000f);
            view.setAttributeNS(null, "y", "" + currentBPPosition / 1000f);

            // this fixes a problem where the xmlns is repeated sometimes
            Element ele = (Element) newsvg;
            ele.setAttributeNS(XMLSupport.XMLNS_NAMESPACE_URI, "xmlns",
                               svgNS);
            if (ele.hasAttributeNS(null, "xmlns")) {
                ele.removeAttributeNS(null, "xmlns");
            }

            view.appendChild(newsvg);
            currentPageG.appendChild(view);
        }
    }

    public void renderLeader(Leader area) {
        String style = "stroke:black;stroke-width:" +
                       (area.getRuleThickness() / 1000) + ";";
        switch (area.getRuleStyle()) {
            case Leader.DOTTED:
                style += "stroke-dasharray:1,1";
                break;
            case Leader.DASHED:
                style += "stroke-dasharray:5,1";
                break;
            case Leader.SOLID:
                break;
            case Leader.DOUBLE:
                break;
            case Leader.GROOVE:
                break;
            case Leader.RIDGE:
                break;
        }
        Element line = SVGUtilities.createLine(svgDocument,
                                               currentBlockIPPosition / 1000,
                                               (currentBPPosition + area.getOffset() -
                                                area.getRuleThickness() / 2) / 1000,
                                               (currentBlockIPPosition + area.getWidth()) / 1000,
                                               (currentBPPosition + area.getOffset() -
                                                area.getRuleThickness() / 2) / 1000);
        line.setAttributeNS(null, "style", style);
        currentPageG.appendChild(line);

        super.renderLeader(area);
    }

    public void renderWord(Word word) {
        Element text = SVGUtilities.createText(svgDocument,
                                               currentBlockIPPosition / 1000,
                                               (currentBPPosition + word.getOffset()) / 1000,
                                               word.getWord());
        currentPageG.appendChild(text);

        super.renderWord(word);
    }

    public void renderCharacter(org.apache.fop.area.inline.Character ch) {
        Element text = SVGUtilities.createText(svgDocument,
                                               currentBlockIPPosition / 1000,
                                               (currentBPPosition + ch.getOffset()) / 1000,
                                               "" + ch.getChar());
        currentPageG.appendChild(text);

        super.renderCharacter(ch);
    }
}

