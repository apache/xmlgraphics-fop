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

package org.apache.fop.render.afp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;

import org.apache.fop.afp.AFPBorderPainter;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPRectanglePainter;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.afp.DataStream;
import org.apache.fop.afp.RectanglePaintingInfo;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.AFPFontAttributes;
import org.apache.fop.afp.fonts.AFPPageFonts;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.modca.AbstractPageObject;
import org.apache.fop.afp.modca.PresentationTextObject;
import org.apache.fop.afp.ptoca.PtocaBuilder;
import org.apache.fop.afp.ptoca.PtocaProducer;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;

/**
 * IFPainter implementation that produces AFP (MO:DCA).
 */
public class AFPPainter extends AbstractIFPainter {

    /** logging instance */
    private static Log log = LogFactory.getLog(AFPPainter.class);

    private static final int X = 0;
    private static final int Y = 1;

    private AFPDocumentHandler documentHandler;

    /** the line painter */
    private AFPBorderPainter borderPainter;
    /** the rectangle painter */
    private AFPRectanglePainter rectanglePainter;

    /** unit converter */
    private final AFPUnitConverter unitConv;

    /**
     * Default constructor.
     * @param documentHandler the parent document handler
     */
    public AFPPainter(AFPDocumentHandler documentHandler) {
        super();
        this.documentHandler = documentHandler;
        this.state = IFState.create();
        this.borderPainter = new AFPBorderPainter(getPaintingState(), getDataStream());
        this.rectanglePainter = new AFPRectanglePainter(getPaintingState(), getDataStream());
        this.unitConv = getPaintingState().getUnitConverter();
    }

    /** {@inheritDoc} */
    protected IFContext getContext() {
        return this.documentHandler.getContext();
    }

    FontInfo getFontInfo() {
        return this.documentHandler.getFontInfo();
    }

    AFPPaintingState getPaintingState() {
        return this.documentHandler.getPaintingState();
    }

    DataStream getDataStream() {
        return this.documentHandler.getDataStream();
    }

    /** {@inheritDoc} */
    public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect)
            throws IFException {
        //AFP doesn't support clipping, so we treat viewport like a group
        //this is the same code as for startGroup()
        try {
            saveGraphicsState();
            concatenateTransformationMatrix(transform);
        } catch (IOException ioe) {
            throw new IFException("I/O error in startViewport()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endViewport() throws IFException {
        try {
            restoreGraphicsState();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endViewport()", ioe);
        }
    }

    private void concatenateTransformationMatrix(AffineTransform at) {
        if (!at.isIdentity()) {
            getPaintingState().concatenate(at);
        }
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform transform) throws IFException {
        try {
            saveGraphicsState();
            concatenateTransformationMatrix(transform);
        } catch (IOException ioe) {
            throw new IFException("I/O error in startGroup()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endGroup() throws IFException {
        try {
            restoreGraphicsState();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endGroup()", ioe);
        }
    }

    /** {@inheritDoc} */
    protected Map createDefaultImageProcessingHints(ImageSessionContext sessionContext) {
        Map hints = super.createDefaultImageProcessingHints(sessionContext);

        //AFP doesn't support alpha channels
        hints.put(ImageProcessingHints.TRANSPARENCY_INTENT,
                ImageProcessingHints.TRANSPARENCY_INTENT_IGNORE);
        return hints;
    }

    /** {@inheritDoc} */
    protected RenderingContext createRenderingContext() {
        AFPRenderingContext psContext = new AFPRenderingContext(
                getUserAgent(),
                documentHandler.getResourceManager(),
                getPaintingState(),
                getFontInfo(),
                getContext().getForeignAttributes());
        return psContext;
    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect) throws IFException {
        drawImageUsingURI(uri, rect);
    }

    /** {@inheritDoc} */
    public void drawImage(Document doc, Rectangle rect) throws IFException {
        drawImageUsingDocument(doc, rect);
    }

    /** {@inheritDoc} */
    public void clipRect(Rectangle rect) throws IFException {
        //Not supported!
    }

    private float toPoint(int mpt) {
        return mpt / 1000f;
    }

    /** {@inheritDoc} */
    public void fillRect(Rectangle rect, Paint fill) throws IFException {
        if (fill == null) {
            return;
        }
        if (rect.width != 0 && rect.height != 0) {
            if (fill instanceof Color) {
                getPaintingState().setColor((Color)fill);
            } else {
                throw new UnsupportedOperationException("Non-Color paints NYI");
            }
            RectanglePaintingInfo rectanglePaintInfo = new RectanglePaintingInfo(
                    toPoint(rect.x), toPoint(rect.y), toPoint(rect.width), toPoint(rect.height));
            rectanglePainter.paint(rectanglePaintInfo);
        }
    }

    /** {@inheritDoc} */
    public void drawBorderRect(Rectangle rect, BorderProps before, BorderProps after,
            BorderProps start, BorderProps end) throws IFException {
        if (before != null || after != null || start != null || end != null) {
            /*
            try {
                endTextObject();
                this.borderPainter.drawBorders(rect, before, after, start, end);
            } catch (IOException ioe) {
                throw new IFException("I/O error in drawBorderRect()", ioe);
            }*/
        }
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
                throws IFException {
        /*
        try {
            endTextObject();
            this.borderPainter.drawLine(start, end, width, color, style);
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawLine()", ioe);
        }*/
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, final int[] dx, int[] dy, final String text)
            throws IFException {
        int fontSize = this.state.getFontSize();
        getPaintingState().setFontSize(fontSize);

        FontTriplet triplet = new FontTriplet(
                state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
        //TODO Ignored: state.getFontVariant()
        String fontKey = getFontInfo().getInternalFontKey(triplet);

        // register font as necessary
        Map/*<String,FontMetrics>*/ fontMetricMap = documentHandler.getFontInfo().getFonts();
        final AFPFont afpFont = (AFPFont)fontMetricMap.get(fontKey);
        AFPPageFonts pageFonts = getPaintingState().getPageFonts();
        AFPFontAttributes fontAttributes = pageFonts.registerFont(fontKey, afpFont, fontSize);

        final int fontReference = fontAttributes.getFontReference();

        final int[] coords = unitConv.mpts2units(new float[] {x, y} );

        final CharacterSet charSet = afpFont.getCharacterSet(fontSize);

        AbstractPageObject page = getDataStream().getCurrentPage();
        PresentationTextObject pto = page.getPresentationTextObject();
        try {
            pto.createControlSequences(new PtocaProducer() {

                public void produce(PtocaBuilder builder) throws IOException {
                    Point p = getPaintingState().getPoint(coords[X], coords[Y]);
                    builder.setTextOrientation(getPaintingState().getRotation());
                    builder.absoluteMoveBaseline(p.y);
                    builder.absoluteMoveInline(p.x);

                    builder.setVariableSpaceCharacterIncrement(0);
                    builder.setInterCharacterAdjustment(0);
                    builder.setExtendedTextColor(state.getTextColor());
                    builder.setCodedFont((byte)fontReference);

                    if (dx == null) {
                        //No glyph-shifting necessary, so take a shortcut
                        builder.addTransparentData(text.getBytes(charSet.getEncoding()));
                    } else {
                        int l = text.length();
                        int dxl = dx.length;
                        StringBuffer sb = new StringBuffer();

                        if (dxl > 0 && dx[0] != 0) {
                            int dxu = Math.round(unitConv.mpt2units(dx[0]));
                            builder.relativeMoveInline(-dxu);
                        }
                        for (int i = 0; i < l; i++) {
                            sb.append(text.charAt(i));
                            float glyphAdjust = 0;

                            if (i < dxl - 1) {
                                glyphAdjust += dx[i + 1];
                            }

                            if (glyphAdjust != 0) {
                                if (sb.length() > 0) {
                                    String t = sb.toString();
                                    builder.addTransparentData(t.getBytes(charSet.getEncoding()));
                                    sb.setLength(0);
                                }
                                int increment = Math.round(unitConv.mpt2units(glyphAdjust));
                                builder.relativeMoveInline(increment);
                            }
                        }
                        if (sb.length() > 0) {
                            String t = sb.toString();
                            builder.addTransparentData(t.getBytes(charSet.getEncoding()));
                        }
                    }
                }

            });
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawText()", ioe);
        }
    }

    /**
     * Saves the graphics state of the rendering engine.
     * @throws IOException if an I/O error occurs
     */
    protected void saveGraphicsState() throws IOException {
        getPaintingState().save();
    }

    /**
     * Restores the last graphics state of the rendering engine.
     * @throws IOException if an I/O error occurs
     */
    protected void restoreGraphicsState() throws IOException {
        getPaintingState().restore();
    }

}
