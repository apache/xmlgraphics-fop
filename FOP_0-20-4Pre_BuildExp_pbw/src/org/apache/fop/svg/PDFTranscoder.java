/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.awt.AlphaComposite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.HashSet;
import java.util.Set;

import org.apache.batik.transcoder.*;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg.SVGContext; 
import org.apache.batik.dom.util.DocumentFactory;

import org.apache.batik.ext.awt.image.GraphicsUtil;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.gvt.renderer.ImageRendererFactory;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.image.resources.Messages;

import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.FloatKey;
import org.apache.batik.transcoder.keys.LengthKey;
import org.apache.batik.transcoder.keys.PaintKey;
import org.apache.batik.transcoder.keys.PaintKey;
import org.apache.batik.transcoder.keys.Rectangle2DKey;
import org.apache.batik.transcoder.keys.StringKey;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.*;

import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;

import org.apache.batik.bridge.*;
import org.apache.batik.swing.svg.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.gvt.*;
import org.apache.batik.gvt.renderer.*;
import org.apache.batik.gvt.filter.*;
import org.apache.batik.gvt.event.*;

import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.css.*;
import org.w3c.dom.svg.SVGLength;

import org.apache.fop.svg.*;
import org.apache.fop.pdf.*;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * This class enables to transcode an input to a pdf document.
 *
 * <p>Two transcoding hints (<tt>KEY_WIDTH</tt> and
 * <tt>KEY_HEIGHT</tt>) can be used to respectively specify the image
 * width and the image height. If only one of these keys is specified,
 * the transcoder preserves the aspect ratio of the original image.
 *
 * <p>The <tt>KEY_BACKGROUND_COLOR</tt> defines the background color
 * to use for opaque image formats, or the background color that may
 * be used for image formats that support alpha channel.
 *
 * <p>The <tt>KEY_AOI</tt> represents the area of interest to paint
 * in device space.
 *
 * <p>Three additional transcoding hints that act on the SVG
 * processor can be specified:
 *
 * <p><tt>KEY_LANGUAGE</tt> to set the default language to use (may be
 * used by a &lt;switch> SVG element for example),
 * <tt>KEY_USER_STYLESHEET_URI</tt> to fix the URI of a user
 * stylesheet, and <tt>KEY_PIXEL_TO_MM</tt> to specify the pixel to
 * millimeter conversion factor.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 */
public class PDFTranscoder extends XMLAbstractTranscoder {

    public static final TranscodingHints.Key KEY_STROKE_TEXT =
        new StringKey();

    /**
     * The user agent dedicated to an <tt>ImageTranscoder</tt>.
     */
    protected UserAgent userAgent = new ImageTranscoderUserAgent();

    /**
     * Constructs a new <tt>ImageTranscoder</tt>.
     */
    public PDFTranscoder() {
        hints.put(KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
                  SVGConstants.SVG_NAMESPACE_URI);
        hints.put(KEY_DOCUMENT_ELEMENT, SVGConstants.SVG_SVG_TAG);
        hints.put(KEY_DOM_IMPLEMENTATION,
                  SVGDOMImplementation.getDOMImplementation());
    }

    /**
     * Transcodes the specified Document as an image in the specified output.
     *
     * @param document the document to transcode
     * @param uri the uri of the document or null if any
     * @param output the ouput where to transcode
     * @exception TranscoderException if an error occured while transcoding
     */
    protected void transcode(Document document, String uri,
                             TranscoderOutput output) throws TranscoderException {

        if (!(document instanceof SVGOMDocument)) {
            throw new TranscoderException(Messages.formatMessage("notsvg",
                    null));
        }
        SVGDocument svgDoc = (SVGDocument)document;
        SVGSVGElement root = svgDoc.getRootElement();
        // initialize the SVG document with the appropriate context
        String parserClassname = (String)hints.get(KEY_XML_PARSER_CLASSNAME);

        /*boolean stroke = true;
        if (hints.containsKey(KEY_STROKE_TEXT)) {
            stroke = ((Boolean)hints.get(KEY_STROKE_TEXT)).booleanValue();
        }*/
        PDFDocumentGraphics2D graphics = new PDFDocumentGraphics2D(false);

        // build the GVT tree
        GVTBuilder builder = new GVTBuilder();
        BridgeContext ctx = new BridgeContext(userAgent);
        TextPainter textPainter = null;
        textPainter = new StrokingTextPainter();
        ctx.setTextPainter(textPainter);

        PDFTextElementBridge pdfTextElementBridge = new PDFTextElementBridge(graphics.getFontState());
        ctx.putBridge(pdfTextElementBridge);

        PDFAElementBridge pdfAElementBridge = new PDFAElementBridge();
        AffineTransform currentTransform = new AffineTransform(1, 0, 0, 1, 0, 0);
        pdfAElementBridge.setCurrentTransform(currentTransform);
        ctx.putBridge(pdfAElementBridge);
        ctx.putBridge(new PDFImageElementBridge());
        GraphicsNode gvtRoot;
        try {
            gvtRoot = builder.build(ctx, svgDoc);
        } catch (BridgeException ex) {
            throw new TranscoderException(ex);
        }
        // get the 'width' and 'height' attributes of the SVG document
        float docWidth = (float)ctx.getDocumentSize().getWidth();
        float docHeight = (float)ctx.getDocumentSize().getHeight();
        ctx = null;
        builder = null;

        // compute the image's width and height according the hints
        float imgWidth = -1;
        if (hints.containsKey(ImageTranscoder.KEY_WIDTH)) {
            imgWidth =
                ((Float)hints.get(ImageTranscoder.KEY_WIDTH)).floatValue();
        }
        float imgHeight = -1;
        if (hints.containsKey(ImageTranscoder.KEY_HEIGHT)) {
            imgHeight =
                ((Float)hints.get(ImageTranscoder.KEY_HEIGHT)).floatValue();
        }
        float width, height;
        if (imgWidth > 0 && imgHeight > 0) {
            width = imgWidth;
            height = imgHeight;
        } else if (imgHeight > 0) {
            width = (docWidth * imgHeight) / docHeight;
            height = imgHeight;
        } else if (imgWidth > 0) {
            width = imgWidth;
            height = (docHeight * imgWidth) / docWidth;
        } else {
            width = docWidth;
            height = docHeight;
        }
        // compute the preserveAspectRatio matrix
        AffineTransform Px;
        String ref = null;
        try {
            ref = new URL(uri).getRef();
        } catch (MalformedURLException ex) {
            // nothing to do, catched previously
        }

        try {
            Px = ViewBox.getViewTransform(ref, root, width, height);
        } catch (BridgeException ex) {
            throw new TranscoderException(ex);
        }

        if (Px.isIdentity() && (width != docWidth || height != docHeight)) {
            // The document has no viewBox, we need to resize it by hand.
            // we want to keep the document size ratio
            float d = Math.max(docWidth, docHeight);
            float dd = Math.max(width, height);
            float scale = dd / d;
            Px = AffineTransform.getScaleInstance(scale, scale);
        }
        // take the AOI into account if any
        if (hints.containsKey(ImageTranscoder.KEY_AOI)) {
            Rectangle2D aoi = (Rectangle2D)hints.get(ImageTranscoder.KEY_AOI);
            // transform the AOI into the image's coordinate system
            aoi = Px.createTransformedShape(aoi).getBounds2D();
            AffineTransform Mx = new AffineTransform();
            double sx = width / aoi.getWidth();
            double sy = height / aoi.getHeight();
            Mx.scale(sx, sy);
            double tx = -aoi.getX();
            double ty = -aoi.getY();
            Mx.translate(tx, ty);
            // take the AOI transformation matrix into account
            // we apply first the preserveAspectRatio matrix
            Px.preConcatenate(Mx);
        }
        // prepare the image to be painted
        int w = (int)width;
        int h = (int)height;

        graphics.setupDocument(output.getOutputStream(), w, h);
        graphics.setSVGDimension(docWidth, docHeight);
        currentTransform.setTransform(1, 0, 0, -1, 0, height);
        /*if (!stroke) {
            textPainter = new PDFTextPainter(graphics.getFontState());
            ctx.setTextPainter(textPainter);
        }*/

        if (hints.containsKey(ImageTranscoder.KEY_BACKGROUND_COLOR)) {
            graphics.setBackgroundColor((Color)hints.get(ImageTranscoder.KEY_BACKGROUND_COLOR));
        }
        graphics.setGraphicContext(new org.apache.batik.ext.awt.g2d.GraphicContext());

        gvtRoot.paint(graphics);

        try {
            graphics.finish();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TranscoderException(ex);
        }
    }

    /**
     * Creates a <tt>DocumentFactory</tt> that is used to create an SVG DOM
     * tree. The specified DOM Implementation is ignored and the Batik
     * SVG DOM Implementation is automatically used.
     *
     * @param domImpl the DOM Implementation (not used)
     * @param parserClassname the XML parser classname
     */
    protected DocumentFactory createDocumentFactory(DOMImplementation domImpl,
            String parserClassname) {
        return new SAXSVGDocumentFactory(parserClassname);
    }

    // --------------------------------------------------------------------
    // UserAgent implementation
    // --------------------------------------------------------------------

    /**
     * A user agent implementation for <tt>ImageTranscoder</tt>.
     */
    protected class ImageTranscoderUserAgent extends UserAgentAdapter {

        /**
         * Returns the default size of this user agent (400x400).
         */
        public Dimension2D getViewportSize() {
            return new Dimension(400, 400);
        }

        /**
         * Displays the specified error message using the <tt>ErrorHandler</tt>.
         */
        public void displayError(String message) {
            try {
                getErrorHandler().error(new TranscoderException(message));
            } catch (TranscoderException ex) {
                throw new RuntimeException();
            }
        }

        /**
         * Displays the specified error using the <tt>ErrorHandler</tt>.
         */
        public void displayError(Exception e) {
            try {
                getErrorHandler().error(new TranscoderException(e));
            } catch (TranscoderException ex) {
                throw new RuntimeException();
            }
        }

        /**
         * Displays the specified message using the <tt>ErrorHandler</tt>.
         */
        public void displayMessage(String message) {
            try {
                getErrorHandler().warning(new TranscoderException(message));
            } catch (TranscoderException ex) {
                throw new RuntimeException();
            }
        }

        /**
         * Returns the pixel to millimeter conversion factor specified in the
         * <tt>TranscodingHints</tt> or 0.3528 if any.
         */
        public float getPixelToMM() {
            if (getTranscodingHints().containsKey(ImageTranscoder.KEY_PIXEL_TO_MM)) {
                return ((Float)getTranscodingHints().get(ImageTranscoder.KEY_PIXEL_TO_MM)).floatValue();
            } else {
                // return 0.3528f; // 72 dpi
                return 0.26458333333333333333333333333333f;    // 96dpi
            }
        }

        /**
         * Returns the user language specified in the
         * <tt>TranscodingHints</tt> or "en" (english) if any.
         */
        public String getLanguages() {
            if (getTranscodingHints().containsKey(ImageTranscoder.KEY_LANGUAGE)) {
                return (String)getTranscodingHints().get(ImageTranscoder.KEY_LANGUAGE);
            } else {
                return "en";
            }
        }

        public String getMedia() {
            return "print";
        }

        /**
         * Returns the user stylesheet specified in the
         * <tt>TranscodingHints</tt> or null if any.
         */
        public String getUserStyleSheetURI() {
            return (String)getTranscodingHints().get(ImageTranscoder.KEY_USER_STYLESHEET_URI);
        }

        /**
         * Returns the XML parser to use from the TranscodingHints.
         */
        public String getXMLParserClassName() {
            if (getTranscodingHints().containsKey(KEY_XML_PARSER_CLASSNAME)) {
                return (String)getTranscodingHints().get(KEY_XML_PARSER_CLASSNAME);
            } else {
                return XMLResourceDescriptor.getXMLParserClassName();
            }
        }

        public boolean isXMLParserValidating() {
            return false;
        }

        /**
         * Unsupported operation.
         */
        public AffineTransform getTransform() {
            return null;
        }
    }
}
