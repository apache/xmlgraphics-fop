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

package org.apache.fop.render.ps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.ps.ImageEncoder;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;
import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.image.loader.batik.BatikImageFlavors;
import org.apache.fop.image.loader.batik.BatikUtil;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.ps.svg.PSSVGGraphics2D;
import org.apache.fop.svg.SVGEventProducer;
import org.apache.fop.svg.SVGUserAgent;
import org.apache.fop.svg.font.FOPFontFamilyResolverImpl;

/**
 * Image handler implementation which handles SVG images for PostScript output.
 */
public class PSImageHandlerSVG implements ImageHandler {

    private static final Color FALLBACK_COLOR = new Color(255, 33, 117);
    private HashMap<String, String> gradientsFound = new HashMap<String, String>();

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        BatikImageFlavors.SVG_DOM
    };

    /** {@inheritDoc} */
    public void handleImage(RenderingContext context, Image image, Rectangle pos)
                throws IOException {
        PSRenderingContext psContext = (PSRenderingContext)context;
        PSGenerator gen = psContext.getGenerator();
        ImageXMLDOM imageSVG = (ImageXMLDOM)image;

        if (shouldRaster(imageSVG)) {
            InputStream is = renderSVGToInputStream(context, imageSVG);

            float x = (float) pos.getX() / 1000f;
            float y = (float) pos.getY() / 1000f;
            float w = (float) pos.getWidth() / 1000f;
            float h = (float) pos.getHeight() / 1000f;
            Rectangle2D targetRect = new Rectangle2D.Double(x, y, w, h);

            MaskedImage mi = convertToRGB(ImageIO.read(is));
            BufferedImage ri = mi.getImage();
            ImageEncoder encoder = ImageEncodingHelper.createRenderedImageEncoder(ri);
            Dimension imgDim = new Dimension(ri.getWidth(), ri.getHeight());
            String imgDescription = ri.getClass().getName();
            ImageEncodingHelper helper = new ImageEncodingHelper(ri);
            ColorModel cm = helper.getEncodedColorModel();
            PSImageUtils.writeImage(encoder, imgDim, imgDescription, targetRect, cm, gen, ri, mi.getMaskColor());
        } else {
            //Controls whether text painted by Batik is generated using text or path operations
            boolean strokeText = false;
            //TODO Configure text stroking

            SVGUserAgent ua = new SVGUserAgent(context.getUserAgent(),
                    new FOPFontFamilyResolverImpl(psContext.getFontInfo()), new AffineTransform());

            PSSVGGraphics2D graphics = new PSSVGGraphics2D(strokeText, gen);
            graphics.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());

            BridgeContext ctx = new PSBridgeContext(ua,
                    (strokeText ? null : psContext.getFontInfo()),
                    context.getUserAgent().getImageManager(),
                    context.getUserAgent().getImageSessionContext());

            //Cloning SVG DOM as Batik attaches non-thread-safe facilities (like the CSS engine)
            //to it.
            Document clonedDoc = BatikUtil.cloneSVGDocument(imageSVG.getDocument());

            GraphicsNode root;
            try {
                GVTBuilder builder = new GVTBuilder();
                root = builder.build(ctx, clonedDoc);
            } catch (Exception e) {
                SVGEventProducer eventProducer = SVGEventProducer.Provider.get(
                        context.getUserAgent().getEventBroadcaster());
                eventProducer.svgNotBuilt(this, e, image.getInfo().getOriginalURI());
                return;
            }
            // get the 'width' and 'height' attributes of the SVG document
            float w = (float)ctx.getDocumentSize().getWidth() * 1000f;
            float h = (float)ctx.getDocumentSize().getHeight() * 1000f;

            float sx = pos.width / w;
            float sy = pos.height / h;

            ctx = null;

            gen.commentln("%FOPBeginSVG");
            gen.saveGraphicsState();
            final boolean clip = false;
            if (clip) {
                /*
                 * Clip to the svg area.
                 * Note: To have the svg overlay (under) a text area then use
                 * an fo:block-container
                 */
                gen.writeln("newpath");
                gen.defineRect(pos.getMinX() / 1000f, pos.getMinY() / 1000f,
                        pos.width / 1000f, pos.height / 1000f);
                gen.writeln("clip");
            }

            // transform so that the coordinates (0,0) is from the top left
            // and positive is down and to the right. (0,0) is where the
            // viewBox puts it.
            gen.concatMatrix(sx, 0, 0, sy, pos.getMinX() / 1000f, pos.getMinY() / 1000f);

            AffineTransform transform = new AffineTransform();
            // scale to viewbox
            transform.translate(pos.getMinX(), pos.getMinY());
            gen.getCurrentState().concatMatrix(transform);
            try {
                root.paint(graphics);
            } catch (Exception e) {
                SVGEventProducer eventProducer = SVGEventProducer.Provider.get(
                        context.getUserAgent().getEventBroadcaster());
                eventProducer.svgRenderingError(this, e, image.getInfo().getOriginalURI());
            }

            gen.restoreGraphicsState();
            gen.commentln("%FOPEndSVG");
        }
    }

    private InputStream renderSVGToInputStream(RenderingContext context, ImageXMLDOM imageSVG) throws IOException {
        PNGTranscoder png = new PNGTranscoder();
        Float width = getDimension(imageSVG.getDocument(), "width") * 8;
        png.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, width);
        Float height = getDimension(imageSVG.getDocument(), "height") * 8;
        png.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, height);
        TranscoderInput input = new TranscoderInput(imageSVG.getDocument());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(os);
        try {
            png.transcode(input, output);
        } catch (TranscoderException ex) {
            SVGEventProducer eventProducer = SVGEventProducer.Provider.get(
                    context.getUserAgent().getEventBroadcaster());
            eventProducer.svgRenderingError(this, ex, imageSVG.getInfo().getOriginalURI());
        } finally {
            os.flush();
            os.close();
        }
        return new ByteArrayInputStream(os.toByteArray());
    }

    private MaskedImage convertToRGB(BufferedImage alphaImage) {
        int[] red = new int[256];
        int[] green = new int[256];
        int[] blue = new int[256];
        BufferedImage rgbImage = new BufferedImage(alphaImage.getWidth(),
                alphaImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        //Count occurances of each colour in image
        for (int cx = 0; cx < alphaImage.getWidth(); cx++) {
            for (int cy = 0; cy < alphaImage.getHeight(); cy++) {
                int pixelValue = alphaImage.getRGB(cx, cy);
                Color pixelColor = new Color(pixelValue);
                red[pixelColor.getRed()]++;
                green[pixelColor.getGreen()]++;
                blue[pixelColor.getBlue()]++;
            }
        }
        //Find colour not in image
        Color alphaSwap = null;
        for (int i = 0; i < 256; i++) {
            if (red[i] == 0) {
                alphaSwap = new Color(i, 0, 0);
                break;
            } else if (green[i] == 0) {
                alphaSwap = new Color(0, i, 0);
                break;
            } else if (blue[i] == 0) {
                alphaSwap = new Color(0, 0, i);
                break;
            }
        }
        //Check if all variations are used in all three colours
        if (alphaSwap == null) {
            //Fallback colour is no unique colour channel can be found
            alphaSwap = FALLBACK_COLOR;
        }
        //Replace alpha channel with the new mask colour
        for (int cx = 0; cx < alphaImage.getWidth(); cx++) {
            for (int cy = 0; cy < alphaImage.getHeight(); cy++) {
                int pixelValue = alphaImage.getRGB(cx, cy);
                if (pixelValue == 0) {
                    rgbImage.setRGB(cx, cy, alphaSwap.getRGB());
                } else {
                    rgbImage.setRGB(cx, cy, alphaImage.getRGB(cx, cy));
                }
            }
        }
        return new MaskedImage(rgbImage, alphaSwap);
    }

    private static class MaskedImage {
        private Color maskColor = new Color(0, 0, 0);
        private BufferedImage image;

        public MaskedImage(BufferedImage image, Color maskColor) {
            this.image = image;
            this.maskColor = maskColor;
        }

        public Color getMaskColor() {
            return maskColor;
        }

        public BufferedImage getImage() {
            return image;
        }
    }

    private Float getDimension(Document document, String dimension) {
        if (document.getFirstChild().getAttributes().getNamedItem(dimension) != null) {
            String width = document.getFirstChild().getAttributes().getNamedItem(dimension).getNodeValue();
            width = width.replaceAll("[^\\d.]", "");
            return Float.parseFloat(width);
        }
        return null;
    }

    private boolean shouldRaster(ImageXMLDOM image) {
        //A list of objects on which to check opacity
        try {
        List<String> gradMatches = new ArrayList<String>();
        gradMatches.add("radialGradient");
        gradMatches.add("linearGradient");
        return recurseSVGElements(image.getDocument().getChildNodes(), gradMatches, false);
        } finally {
            gradientsFound.clear();
        }
    }

    private boolean recurseSVGElements(NodeList childNodes, List<String> gradMatches, boolean isMatched) {
        boolean opacityFound = false;
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node curNode = childNodes.item(i);
            if (isMatched && curNode.getLocalName() != null && curNode.getLocalName().equals("stop")) {
                if (curNode.getAttributes().getNamedItem("style") != null) {
                    String[] stylePairs = curNode.getAttributes().getNamedItem("style").getNodeValue()
                            .split(";");
                    for (int styleAtt = 0; styleAtt < stylePairs.length; styleAtt++) {
                        String[] style = stylePairs[styleAtt].split(":");
                        if (style[0].equalsIgnoreCase("stop-opacity")) {
                            if (Double.parseDouble(style[1]) < 1) {
                                return true;
                            }
                        }
                    }
                }
                if (curNode.getAttributes().getNamedItem("stop-opacity") != null) {
                    String opacityValue = curNode.getAttributes().getNamedItem("stop-opacity").getNodeValue();
                    if (Double.parseDouble(opacityValue) < 1) {
                        return true;
                    }
                }
            }
            String nodeName = curNode.getLocalName();
            boolean inMatch = false;
            if (!isMatched) {
                inMatch = nodeName != null && gradMatches.contains(nodeName);
                if (inMatch) {
                    gradientsFound.put(curNode.getAttributes().getNamedItem("id").getNodeValue(), nodeName);
                }
            } else {
                inMatch = true;
            }
            opacityFound = recurseSVGElements(curNode.getChildNodes(), gradMatches, inMatch);
            if (opacityFound) {
                return true;
            }
        }
        return opacityFound;
    }

    /** {@inheritDoc} */
    public int getPriority() {
        return 400;
    }

    /** {@inheritDoc} */
    public Class getSupportedImageClass() {
        return ImageXMLDOM.class;
    }

    /** {@inheritDoc} */
    public ImageFlavor[] getSupportedImageFlavors() {
        return FLAVORS;
    }

    /** {@inheritDoc} */
    public boolean isCompatible(RenderingContext targetContext, Image image) {
        if (targetContext instanceof PSRenderingContext) {
            PSRenderingContext psContext = (PSRenderingContext)targetContext;
            return !psContext.isCreateForms()
                && (image == null || (image instanceof ImageXMLDOM
                        && image.getFlavor().isCompatible(BatikImageFlavors.SVG_DOM)));
        }
        return false;
    }

}
