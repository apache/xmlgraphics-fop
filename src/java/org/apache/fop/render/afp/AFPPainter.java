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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;

import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageProcessingHints;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

import org.apache.fop.afp.AFPBorderPainter;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.afp.AbstractAFPPainter;
import org.apache.fop.afp.BorderPaintingInfo;
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
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.AbstractIFPainter;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.CharUtilities;

/**
 * IFPainter implementation that produces AFP (MO:DCA).
 */
public class AFPPainter extends AbstractIFPainter {




    //** logging instance */
    private static Log log = LogFactory.getLog(AFPPainter.class);

    private static final int X = 0;
    private static final int Y = 1;

    private AFPDocumentHandler documentHandler;

    /** the border painter */
    private AFPBorderPainterAdapter borderPainter;
    /** the rectangle painter */
    private AbstractAFPPainter rectanglePainter;

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

        this.borderPainter = new AFPBorderPainterAdapter(
                new AFPBorderPainter(getPaintingState(), getDataStream()), this, documentHandler);
        this.rectanglePainter = documentHandler.createRectanglePainter();
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
        AFPRenderingContext renderingContext = new AFPRenderingContext(
                getUserAgent(),
                documentHandler.getResourceManager(),
                getPaintingState(),
                getFontInfo(),
                getContext().getForeignAttributes());
        return renderingContext;
    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect) throws IFException {
        String name = documentHandler.getPageSegmentNameFor(uri);
        if (name != null) {
            float[] srcPts = {rect.x, rect.y};
            int[] coords = unitConv.mpts2units(srcPts);
            int width = Math.round(unitConv.mpt2units(rect.width));
            int height = Math.round(unitConv.mpt2units(rect.height));

            getDataStream().createIncludePageSegment(name, coords[X], coords[Y], width, height);
        } else {
            drawImageUsingURI(uri, rect);
        }
    }

    /** {@inheritDoc} */
    protected void drawImage(Image image, Rectangle rect,
            RenderingContext context, boolean convert, Map additionalHints)
    throws IOException, ImageException {


        AFPRenderingContext afpContext = (AFPRenderingContext)context;

        AFPResourceInfo resourceInfo = AFPImageHandler.createResourceInformation(
                image.getInfo().getOriginalURI(),
                afpContext.getForeignAttributes());

        //Check if the image is cached before processing it again
        if (afpContext.getResourceManager().isObjectCached(resourceInfo)) {

            AFPObjectAreaInfo areaInfo = AFPImageHandler.createObjectAreaInfo(
                    afpContext.getPaintingState(), rect);

            afpContext.getResourceManager().includeCachedObject(resourceInfo, areaInfo);

        } else {
            super.drawImage(image, rect, context, convert, additionalHints);
        }

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
            try {
                rectanglePainter.paint(rectanglePaintInfo);
            } catch (IOException ioe) {
                throw new IFException("IO error while painting rectangle", ioe);
            }
        }
    }

    /** {@inheritDoc} */
    public void drawBorderRect(Rectangle rect, BorderProps before, BorderProps after,
            BorderProps start, BorderProps end, Color innerBackgroundColor) throws IFException {
        if (before != null || after != null || start != null || end != null) {
            try {
                this.borderPainter.drawBorders(rect, before, after, start, end,
                        innerBackgroundColor);
            } catch (IFException ife) {
                throw new IFException("Error while painting borders", ife);
            }
        }
    }


    //TODO Try to resolve the name-clash between the AFPBorderPainter in the afp package
    //and this one. Not done for now to avoid a lot of re-implementation and code duplication.

    private static class AFPBorderPainterAdapter extends BorderPainter {


        private final class BorderImagePainter implements Graphics2DImagePainter {
            private final double esf;
            private final Rectangle borderRect;
            private final BorderProps bpsStart;
            private final BorderProps bpsEnd;
            private final BorderProps bpsBefore;
            private final BorderProps bpsAfter;
            private final boolean[] roundCorner;
            private final Color innerBackgroundColor;

            /* TODO represent border related parameters in a class */
            private BorderImagePainter(double esf, Rectangle borderRect,
                    BorderProps bpsStart, BorderProps bpsEnd,
                    BorderProps bpsBefore, BorderProps bpsAfter,
                    boolean[] roundCorner, Color innerBackgroundColor) {
                this.esf = esf;
                this.borderRect = borderRect;
                this.bpsStart = bpsStart;
                this.bpsBefore = bpsBefore;
                this.roundCorner = roundCorner;
                this.bpsEnd = bpsEnd;
                this.bpsAfter = bpsAfter;
                this.innerBackgroundColor = innerBackgroundColor;
            }

            public void paint(Graphics2D g2d, Rectangle2D area) {

                //background
                Area background = new Area(area);
                Area cornerRegion = new Area();
                Area[] cornerBorder
                        = new Area[]{new Area(), new Area(), new Area(), new Area()};

                if (roundCorner[BEFORE_START]) {

                    AffineTransform transform =  new AffineTransform();
                    int beforeRadius = (int)(esf * bpsBefore.getRadiusStart());
                    int startRadius = (int)(esf * bpsStart.getRadiusStart());

                    int beforeWidth = bpsBefore.width;
                    int startWidth = bpsStart.width;
                    int corner = BEFORE_START;

                    background.subtract(makeCornerClip(beforeRadius, startRadius,
                            transform));

                    Area clip = new Area(new Rectangle(0, 0, startRadius, beforeRadius));
                    clip.transform(transform);
                    cornerRegion.add(clip);

                    cornerBorder[BEFORE].add(makeCornerBorderBPD(beforeRadius,
                                    startRadius, beforeWidth, startWidth, transform));

                    cornerBorder[START].add(makeCornerBorderIPD(beforeRadius,
                            startRadius, beforeWidth, startWidth, transform));
                }

                if (roundCorner[BEFORE_END]) {
                    AffineTransform transform
                            = new AffineTransform(-1, 0, 0, 1, borderRect.width, 0);

                    int beforeRadius = (int)(esf * bpsBefore.getRadiusEnd());
                    int startRadius = (int)(esf * bpsEnd.getRadiusStart());

                    int beforeWidth = bpsBefore.width;
                    int startWidth = bpsEnd.width;
                    int corner = BEFORE_END;

                    background.subtract(makeCornerClip(beforeRadius, startRadius,
                            transform));

                    Area clip = new Area(new Rectangle(0, 0, startRadius, beforeRadius));
                    clip.transform(transform);
                    cornerRegion.add(clip);

                    cornerBorder[BEFORE].add(makeCornerBorderBPD(beforeRadius,
                                    startRadius, beforeWidth, startWidth, transform));

                    cornerBorder[END].add(makeCornerBorderIPD(beforeRadius,
                            startRadius, beforeWidth, startWidth, transform));
                }

                if (roundCorner[AFTER_END]) {
                    AffineTransform transform = new AffineTransform(-1, 0, 0, -1,
                            borderRect.width, borderRect.height);

                    int beforeRadius = (int)(esf * bpsAfter.getRadiusEnd());
                    int startRadius = (int)(esf * bpsEnd.getRadiusEnd());

                    int beforeWidth = bpsAfter.width;
                    int startWidth = bpsEnd.width;
                    int corner = AFTER_END;

                    background.subtract(makeCornerClip(beforeRadius, startRadius,
                            transform));

                    Area clip = new Area(new Rectangle(0, 0, startRadius, beforeRadius));
                    clip.transform(transform);
                    cornerRegion.add(clip);

                    cornerBorder[AFTER].add(makeCornerBorderBPD(beforeRadius,
                            startRadius, beforeWidth, startWidth, transform));
                    cornerBorder[END].add(makeCornerBorderIPD(beforeRadius,
                            startRadius, beforeWidth, startWidth, transform));
                }

                if (roundCorner[AFTER_START]) {
                    AffineTransform transform
                            = new AffineTransform(1, 0, 0, -1, 0, borderRect.height);

                    int beforeRadius = (int)(esf * bpsAfter.getRadiusStart());
                    int startRadius = (int)(esf * bpsStart.getRadiusEnd());

                    int beforeWidth = bpsAfter.width;
                    int startWidth = bpsStart.width;
                    int corner = AFTER_START;

                    background.subtract(makeCornerClip(beforeRadius, startRadius,
                            transform));

                    Area clip = new Area(new Rectangle(0, 0, startRadius, beforeRadius));
                    clip.transform(transform);
                    cornerRegion.add(clip);

                    cornerBorder[AFTER].add(makeCornerBorderBPD(beforeRadius,
                                    startRadius, beforeWidth, startWidth, transform));
                    cornerBorder[START].add(makeCornerBorderIPD(beforeRadius,
                            startRadius, beforeWidth, startWidth, transform));
                }

                g2d.setColor(innerBackgroundColor);
                g2d.fill(background);

                //paint the borders
                //TODO refactor to repeating code into method
                if (bpsBefore != null && bpsBefore.width > 0) {
                    GeneralPath borderPath = new GeneralPath();
                    borderPath.moveTo(0, 0);
                    borderPath.lineTo(borderRect.width, 0);
                    borderPath.lineTo(
                            borderRect.width - (bpsEnd == null ? 0 : bpsEnd.width),
                            bpsBefore.width);
                    borderPath.lineTo(bpsStart == null ? 0 : bpsStart.width, bpsBefore.width);

                    Area border = new Area(borderPath);

                    border.subtract(cornerRegion);

                    g2d.setColor(bpsBefore.color);
                    g2d.fill(border);
                    g2d.fill(cornerBorder[BEFORE]);
                }

                if (bpsEnd != null && bpsEnd.width > 0) {
                    GeneralPath borderPath = new GeneralPath();
                    borderPath.moveTo(borderRect.width, 0);
                    borderPath.lineTo(borderRect.width, borderRect.height);
                    borderPath.lineTo(
                            borderRect.width - bpsEnd.width,
                            borderRect.height - (bpsAfter == null ? 0 : bpsAfter.width));
                    borderPath.lineTo(
                            borderRect.width - bpsEnd.width,
                            bpsBefore == null ? 0 : bpsBefore.width);

                    Area border = new Area(borderPath);

                    border.subtract(cornerRegion);

                    g2d.setColor(bpsEnd.color);
                    g2d.fill(border);
                    g2d.fill(cornerBorder[END]);
                }

                if (bpsAfter != null && bpsAfter.width > 0) {
                    GeneralPath borderPath = new GeneralPath();
                    borderPath.moveTo(0, borderRect.height);
                    borderPath.lineTo(borderRect.width, borderRect.height);
                    borderPath.lineTo(
                            borderRect.width - (bpsEnd == null ? 0 : bpsEnd.width),
                            borderRect.height - bpsAfter.width);
                    borderPath.lineTo(
                            bpsStart == null ? 0 : bpsStart.width,
                            borderRect.height - bpsAfter.width);
                    Area border = new Area(borderPath);

                    border.subtract(cornerRegion);

                    g2d.setColor(bpsAfter.color);
                    g2d.fill(border);
                    g2d.fill(cornerBorder[AFTER]);
                }

                if (bpsStart != null && bpsStart.width > 0) {

                    GeneralPath borderPath = new GeneralPath();
                    borderPath.moveTo(bpsStart.width,
                            bpsBefore == null ? 0 : bpsBefore.width);
                    borderPath.lineTo(bpsStart.width,
                            borderRect.height - (bpsAfter == null ? 0 : bpsAfter.width));
                    borderPath.lineTo(0, borderRect.height);
                    borderPath.lineTo(0, 0);

                    Area border = new Area(borderPath);

                    border.subtract(cornerRegion);

                    g2d.setColor(bpsStart.color);
                    g2d.fill(border);
                    g2d.fill(cornerBorder[START]);
                }
            }

            public Dimension getImageSize() {
                return borderRect.getSize();
            }
        }

        public static final String CORNER_MODE_PROPERTY = "fop.round-corners.afp";
        public static final String MODE_SEPERATE = "seperate";
        public static final String MODE_ALL_IN_ONE = "all-in-one";
        public static final String MODE_DEFAULT = "all-in-one";

        private AFPBorderPainter delegate;
        private final AFPPainter painter;
        private final AFPDocumentHandler documentHandler;

        public AFPBorderPainterAdapter(AFPBorderPainter borderPainter, AFPPainter painter,
                AFPDocumentHandler documentHandler) {
            this.delegate = borderPainter;
            this.painter = painter;
            this.documentHandler = documentHandler;
        }

        public void drawBorders(final Rectangle borderRect,
                final BorderProps bpsBefore, final BorderProps bpsAfter,
                final BorderProps bpsStart, final BorderProps bpsEnd, Color innerBackgroundColor)
                throws IFException {

            if (isRoundedCornersSupported()) {
                if (MODE_SEPERATE.equals(System.getProperty(CORNER_MODE_PROPERTY))) {
                    try {
                        drawRectangularBorders(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd);
                    } catch (IOException ioe) {
                        throw new IFException("Error drawing border", ioe);
                    }
                    drawSeperateRoundedCorners(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd,
                            innerBackgroundColor);
                } else {
                    drawRoundedCorners(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd,
                            innerBackgroundColor);
                }
            } else {
                try {
                    drawRectangularBorders(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd);
                } catch (IOException ioe) {
                    throw new IFException("Error drawing border", ioe);
                }
            }
        }

        private boolean isModeSeperate() {
            return (MODE_SEPERATE.equals(System.getProperty(CORNER_MODE_PROPERTY, MODE_DEFAULT)));
        }

        private boolean isModeAllInOne() {
            return (MODE_ALL_IN_ONE.equals(System.getProperty(CORNER_MODE_PROPERTY, MODE_DEFAULT)));
        }

        private boolean isBackgroundRequired(BorderProps bpsBefore, BorderProps bpsAfter,
                BorderProps bpsStart, BorderProps bpsEnd) {

            boolean rtn = !(isRoundedCornersSupported() && isModeAllInOne()
                    && hasRoundedCorners(bpsBefore,  bpsAfter,
                     bpsStart,  bpsEnd));
            return rtn;
        }

        private boolean hasRoundedCorners( final BorderProps bpsBefore, final BorderProps bpsAfter,
                final BorderProps bpsStart, final BorderProps bpsEnd) {

            return ((bpsStart == null ? false : bpsStart.getRadiusStart() > 0)
                        && (bpsBefore == null ? false : bpsBefore.getRadiusStart() > 0))
                || ((bpsBefore == null ? false : bpsBefore.getRadiusEnd() > 0)
                        && (bpsEnd == null ? false : bpsEnd.getRadiusStart() > 0))
                || ((bpsEnd == null ? false : bpsEnd.getRadiusEnd() > 0)
                        && (bpsAfter == null ? false : bpsAfter.getRadiusEnd() > 0))
                        || ((bpsAfter == null ? false : bpsAfter.getRadiusStart() > 0)
                        && (bpsStart == null ? false : bpsStart.getRadiusEnd() > 0));
        }

        private void drawSeperateRoundedCorners(final Rectangle borderRect,
                final BorderProps bpsBefore, final BorderProps bpsAfter,
                final BorderProps bpsStart, final BorderProps bpsEnd,
                final Color innerBackgroundColor)
                    throws IFException {

            double esf = cornerScaleFactor(borderRect.width, borderRect.height,
                    bpsBefore,  bpsAfter,
                    bpsStart,  bpsEnd);

            if (bpsBefore != null && bpsStart != null) {
                int beforeRadiusStart = (int)(esf *  bpsBefore.getRadiusStart());
                int startRadiusStart = (int)(esf *  bpsStart.getRadiusStart());

                Rectangle area = new Rectangle(borderRect.x, borderRect.y,
                        startRadiusStart,  beforeRadiusStart);

                drawCorner(BEFORE_START, area, bpsBefore, bpsStart,
                        beforeRadiusStart, startRadiusStart, innerBackgroundColor);
            }

            if (bpsEnd != null && bpsBefore != null) {
                int endRadiusStart = (int)(esf *  bpsEnd.getRadiusStart());
                int beforeRadiusEnd = (int)(esf *  bpsBefore.getRadiusEnd());
                Rectangle area = new Rectangle(borderRect.x + borderRect.width - endRadiusStart,
                        borderRect.y, endRadiusStart,  beforeRadiusEnd);

                drawCorner(BEFORE_END, area, bpsBefore, bpsEnd,
                        beforeRadiusEnd, endRadiusStart, innerBackgroundColor);
            }


            if (bpsEnd != null && bpsAfter != null) {
                int endRadiusEnd = (int)(esf *  bpsEnd.getRadiusEnd());
                int afterRadiusEnd = (int)(esf *  bpsAfter.getRadiusEnd());

                Rectangle area = new Rectangle(borderRect.x + borderRect.width - endRadiusEnd,
                        borderRect.y + borderRect.height - afterRadiusEnd,
                        endRadiusEnd,   afterRadiusEnd);

                drawCorner(AFTER_END, area, bpsAfter, bpsEnd,
                        afterRadiusEnd, endRadiusEnd, innerBackgroundColor);

            }

            if (bpsStart != null && bpsAfter != null) {
                int startRadiusEnd = (int)(esf *  bpsStart.getRadiusEnd());
                int afterRadiusStart = (int)(esf *  bpsAfter.getRadiusStart());
                Rectangle area = new Rectangle(borderRect.x ,
                        borderRect.y + borderRect.height - afterRadiusStart,
                        startRadiusEnd, afterRadiusStart);

                drawCorner(AFTER_START, area, bpsAfter, bpsStart,
                        afterRadiusStart, startRadiusEnd, innerBackgroundColor);
            }
        }

        private void drawRoundedCorners(final Rectangle borderRect,
                final BorderProps bpsBefore, final BorderProps bpsAfter,
                final BorderProps bpsStart, final BorderProps bpsEnd,
                final Color innerBackgroundColor)
        throws IFException {


            final double esf = cornerScaleFactor(borderRect.width, borderRect.height,
                    bpsBefore,  bpsAfter,
                    bpsStart,  bpsEnd);

            final boolean[] roundCorner = new boolean[]{
                    bpsBefore != null  && bpsStart != null
                        && bpsBefore.getRadiusStart() > 0
                        && bpsStart.getRadiusStart() > 0
                        && bpsBefore.mode != BorderProps.COLLAPSE_OUTER
                        && bpsStart.mode != BorderProps.COLLAPSE_OUTER,
                    bpsEnd != null && bpsBefore != null
                        && bpsEnd.getRadiusStart() > 0
                        && bpsBefore.getRadiusEnd() > 0
                        && bpsEnd.mode != BorderProps.COLLAPSE_OUTER
                        && bpsBefore.mode != BorderProps.COLLAPSE_OUTER,
                    bpsEnd != null && bpsAfter != null
                        && bpsEnd.getRadiusEnd() > 0
                        && bpsAfter.getRadiusEnd() > 0
                        && bpsEnd.mode != BorderProps.COLLAPSE_OUTER
                        && bpsAfter.mode != BorderProps.COLLAPSE_OUTER,
                    bpsStart != null && bpsAfter != null
                        && bpsStart.getRadiusEnd() > 0
                        && bpsAfter.getRadiusStart() > 0
                        && bpsStart.mode != BorderProps.COLLAPSE_OUTER
                        && bpsAfter.mode != BorderProps.COLLAPSE_OUTER
                   };


            if (!roundCorner[BEFORE_START] && !roundCorner[BEFORE_END]
                        && !roundCorner[AFTER_END] && !roundCorner[AFTER_START]) {
                try {
                    drawRectangularBorders(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd);
                } catch (IOException ioe) {
                    throw new IFException("IO error drawing borders", ioe);
                }
                return;
            }

            String areaKey = makeKey(borderRect,
                    bpsBefore, bpsEnd, bpsAfter,
                    bpsStart, innerBackgroundColor);

            Graphics2DImagePainter painter = null;
            String name = documentHandler.getCachedRoundedCorner(areaKey);

            if (name == null) {

                name = documentHandler.cacheRoundedCorner(areaKey);

                painter = new BorderImagePainter(esf, borderRect,
                        bpsStart, bpsEnd, bpsBefore, bpsAfter,
                        roundCorner, innerBackgroundColor);
            }
            paintCornersAsBitmap(painter, borderRect, name);
        }


        private Area makeCornerClip(final int beforeRadius, final int startRadius,
                 final AffineTransform transform) {

            Rectangle clipR = new Rectangle(0, 0, startRadius, beforeRadius);

            Area clip = new Area(clipR);

            Ellipse2D.Double e = new  Ellipse2D.Double();
            e.x = 0;
            e.y = 0;
            e.width = 2 * startRadius;
            e.height = 2 * beforeRadius;

            clip.subtract(new Area(e));

            clip.transform(transform);
            return clip;
        }


        private Area makeCornerBorderBPD(final int beforeRadius, final int startRadius,
                final int beforeWidth, final int startWidth, final AffineTransform transform) {

            Rectangle clipR = new Rectangle(0, 0, startRadius, beforeRadius);

            Ellipse2D.Double e = new  Ellipse2D.Double();
            e.x = 0;
            e.y = 0;
            e.width = 2 * startRadius;
            e.height = 2 * beforeRadius;

            Ellipse2D.Double i = new  Ellipse2D.Double();
            i.x = startWidth;
            i.y = beforeWidth;
            i.width = 2 * (startRadius - startWidth);
            i.height = 2 * (beforeRadius - beforeWidth);

            Area clip = new Area(e);
            clip.subtract(new Area(i));
            clip.intersect(new Area(clipR));

            GeneralPath cut = new GeneralPath();
            cut.moveTo(0, 0);
            float borderWidthRatio = ((float)beforeWidth) / startWidth;
            if (beforeWidth * startRadius > startWidth * beforeRadius) {
                cut.lineTo(startRadius, borderWidthRatio * startRadius);
                cut.lineTo(startRadius, 0);

            } else {
                cut.lineTo(startRadius, borderWidthRatio * startRadius);
                cut.lineTo(startRadius, 0);
            }

            clip.intersect(new Area(cut));
            clip.transform(transform);
            return clip;
        }


        private Area makeCornerBorderIPD(final int beforeRadius, final int startRadius,
                final int beforeWidth, final int startWidth, final AffineTransform transform) {

            Rectangle clipR = new Rectangle(0, 0, startRadius, beforeRadius);


            Ellipse2D.Double e = new  Ellipse2D.Double();
            e.x = 0;
            e.y = 0;
            e.width = 2 * startRadius;
            e.height = 2 * beforeRadius;

            Ellipse2D.Double i = new  Ellipse2D.Double();
            i.x = startWidth;
            i.y = beforeWidth;
            i.width = 2 * (startRadius - startWidth);
            i.height = 2 * (beforeRadius - beforeWidth);

            Area clip = new Area(e);
            clip.subtract(new Area(i));
            clip.intersect(new Area(clipR));

            GeneralPath cut = new GeneralPath();
            cut.moveTo(0, 0);
            float borderWidthRatio = ((float)beforeWidth) / startWidth;
            if (beforeWidth * startRadius > startWidth * beforeRadius) {
                cut.lineTo(startRadius, borderWidthRatio * startRadius);
                cut.lineTo(startRadius, 0);

            } else {
                cut.lineTo(startRadius, borderWidthRatio * startRadius);
                cut.lineTo(startRadius, 0);
            }

            clip.subtract(new Area(cut));
            clip.transform(transform);
            return clip;
        }

        /* TODO collect  parameters in a useful structure */
        private void paintCorner(final Graphics2D g2d, final int beforeWidth,
                final int startWidth, final  int beforeRadius,
                final int startRadius, final Color innerBackgroundColor,
                final Color beforeColor, final Color startColor) {

            //Draw the before-srart corner
            Ellipse2D.Double inner = new  Ellipse2D.Double();
            inner.x = startWidth;
            inner.y = beforeWidth;
            inner.width =  2 * (startRadius - startWidth);
            inner.height =  2 * (beforeRadius - beforeWidth);

            Ellipse2D.Double outer = new  Ellipse2D.Double();
            outer.x = 0;
            outer.y = 0;
            outer.width =   2 * (startRadius);
            outer.height =  2 * (beforeRadius);

            Area border = new Area(outer);
            border.subtract(new Area(inner));

            GeneralPath afterCut = new GeneralPath();
            GeneralPath beforeCut = new GeneralPath();
            afterCut.moveTo(0, 0);
            beforeCut.moveTo(0, 0);
            float borderWidthRatio = ((float)beforeWidth) / startWidth;
            if (beforeWidth * startRadius > startWidth * beforeRadius) {
                afterCut.lineTo(startRadius, borderWidthRatio * startRadius);
                beforeCut.lineTo(1f / borderWidthRatio * beforeRadius, beforeRadius);

                afterCut.lineTo(startRadius, 0);
                beforeCut.lineTo(0, beforeRadius);
            } else {
                afterCut.lineTo(startRadius, (borderWidthRatio * startRadius));
                beforeCut.lineTo(1f / borderWidthRatio * beforeRadius, beforeRadius);

                afterCut.lineTo(startRadius, 0);
                beforeCut.lineTo(0, beforeRadius);

            }

            //start
            g2d.setColor(startColor);
            g2d.fill(border);

            //before
            border = new Area(outer);
            border.subtract(new Area(inner));
            border.subtract(new Area(beforeCut));

            //start
            g2d.setColor(beforeColor);
            g2d.fill(border);

            //paint background
            if (innerBackgroundColor == null) {
                g2d.setColor(Color.white);
              //  log.warn("No background color set");

            } else {
                g2d.setColor(innerBackgroundColor);
            }

            g2d.fill(inner);
        }


        private void drawCorner(final int corner, final Rectangle area,
                final BorderProps before, final BorderProps start,
                final int beforeRadius, final int startRadius, final Color innerBackground)
                throws IFException {

            if (beforeRadius > 0 && startRadius > 0) {
                String cornerKey = makeCornerKey(corner, before, start,
                        beforeRadius, startRadius, innerBackground);

                Graphics2DImagePainter painter = null;

                String name = documentHandler.getCachedRoundedCorner(cornerKey);

                //  If the corner is not in the cache we construct a Graphics2DImagePainter
                //  that paints the corner
                if (name == null) {
                    //Cache the name
                    name = documentHandler.cacheRoundedCorner(cornerKey);

                    // create the Graphics2DImagePainter
                    painter = new Graphics2DImagePainter() {
                        public void paint(Graphics2D g2d, Rectangle2D area) {

                            int beforeWidth = before.width;
                            int startWidth = start.width;

                            Color beforeColor = before.color;
                            Color startColor = start.color;

                            //No transformation
                            AffineTransform t;
                            switch(corner) {
                            case BEFORE_START:
                                //No transform required
                                break;
                            case BEFORE_END:
                                t = new AffineTransform(-1, 0, 0, 1, startRadius, 0);
                                g2d.transform(t);
                                break;
                            case AFTER_END:
                                t = new AffineTransform(-1, 0, 0, -1, startRadius, beforeRadius);
                                g2d.transform(t);
                                break;
                            case AFTER_START:
                                t = new AffineTransform(1, 0, 0, -1, 0, beforeRadius);
                                g2d.transform(t);
                                break;
                            default: break;
                            }

                            paintCorner(g2d, beforeWidth, startWidth,
                                    beforeRadius, startRadius, innerBackground,
                                    beforeColor,  startColor);
                        }

                        public Dimension getImageSize() {
                            return area.getSize();
                        }
                    };
                }
                paintCornersAsBitmap(painter, area, name);
            }
        }


        private String makeCornerKey(int corner, BorderProps beforeProps, BorderProps startProps,
                int beforeRadius, int startRadius, Color innerBackgroundColor) {

           return hash(new StringBuffer()
                   .append(corner)
                   .append(":")
                   .append(beforeRadius)
                   .append(":")
                   .append(startRadius)
                   .append(":")
                   .append(beforeProps.width)
                   .append(":")
                   .append(startProps.width)
                   .append(":")
                   .append(beforeProps.color)
                   .append(":")
                   .append(startProps.color)
                   .append(":")
                   .append(innerBackgroundColor)
                   .toString());
        }


        private String makeKey(Rectangle area, BorderProps beforeProps,
                BorderProps endProps, BorderProps afterProps, BorderProps startProps,
                 Color innerBackgroundColor) {

            return hash(new StringBuffer()
                    .append(area.width)
                    .append(":")
                    .append(area.height)
                    .append(":")
                    .append(beforeProps)
                    .append(":")
                    .append(endProps)
                    .append(":")
                    .append(afterProps)
                    .append(":")
                    .append(startProps)
                    .append(":")
                    .append(innerBackgroundColor)
                    .toString());
        }


        private String hash(String text) {

            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (Exception e) {
                throw new RuntimeException("Internal error", e);
            }

            byte[] result = md.digest(text.getBytes());

            StringBuffer sb = new StringBuffer();
            char[] digits = {'0', '1', '2', '3', '4', '5', '6',
                    '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            for (int idx = 0; idx < 6; ++idx) {
              byte b = result[idx];
              sb.append(digits[(b & 0xf0) >> 4]);
              sb.append(digits[b & 0x0f]);
            }
            return sb.toString();
        }

        private void paintCornersAsBitmap(Graphics2DImagePainter painter,
                Rectangle boundingBox, String name) throws IFException {
            //TODO parameters ok?
            ImageInfo info = new ImageInfo(name, null);

            ImageSize size = new ImageSize();
            size.setSizeInMillipoints(boundingBox.width, boundingBox.height);

            //Use the foreign attributes map to set image handling hints
            Map map = new java.util.HashMap(2);
            map.put(AFPForeignAttributeReader.RESOURCE_NAME, name);
            map.put(AFPForeignAttributeReader.RESOURCE_LEVEL, "print-file");

            AFPRenderingContext context = (AFPRenderingContext)
                    this.painter.createRenderingContext(/*map*/);

            size.setResolution(context.getPaintingState().getResolution());
            size.calcPixelsFromSize();
            info.setSize(size);
            ImageGraphics2D img = new ImageGraphics2D(info, painter);

            Map hints = new java.util.HashMap();

            hints.put(ImageHandlerUtil.CONVERSION_MODE, ImageHandlerUtil.CONVERSION_MODE_BITMAP);
            hints.put("TARGET_RESOLUTION",
                    new Integer(context.getPaintingState().getResolution()));


            try {
                this.painter.drawImage(img, boundingBox, context, true, hints);
            } catch (IOException ioe) {
                throw new IFException(
                        "I/O error while painting corner using a bitmap", ioe);
            } catch (ImageException ie) {
                throw new IFException(
                        "Image error while painting corner using a bitmap", ie);
            }
        }

        protected void clip() throws IOException {
            //not supported by AFP
        }

        protected void closePath() throws IOException {
            //used for clipping only, so not implemented
        }

        protected void moveTo(int x, int y) throws IOException {
            //used for clipping only, so not implemented
        }

        protected void lineTo(int x, int y) throws IOException {
            //used for clipping only, so not implemented
        }

        protected void saveGraphicsState() throws IOException {
            //used for clipping only, so not implemented
        }

        protected void restoreGraphicsState() throws IOException {
            //used for clipping only, so not implemented
        }

        private float toPoints(int mpt) {
            return mpt / 1000f;
        }

        protected void drawBorderLine(                           // CSOK: ParameterNumber
                int x1, int y1, int x2, int y2, boolean horz,
                boolean startOrBefore, int style, Color color) throws IOException {
            BorderPaintingInfo borderPaintInfo = new BorderPaintingInfo(
                    toPoints(x1), toPoints(y1), toPoints(x2), toPoints(y2),
                    horz, style, color);
            delegate.paint(borderPaintInfo);
        }

        public void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
        throws IOException {
            if (start.y != end.y) {
                //TODO Support arbitrary lines if necessary
                throw new UnsupportedOperationException(
                "Can only deal with horizontal lines right now");
            }

            //Simply delegates to drawBorderLine() as AFP line painting is not very sophisticated.
            int halfWidth = width / 2;
            drawBorderLine(start.x, start.y - halfWidth, end.x, start.y + halfWidth,
                    true, true, style.getEnumValue(), color);
        }


        protected void arcTo(double startAngle, double endAngle, int cx, int cy, int width,
                int height) throws IOException {
            throw new UnsupportedOperationException(
            "Can only deal with horizontal lines right now");

        }


        protected void changeCoords(double a, double b, double c, double d, double e, double f) {
            throw new UnsupportedOperationException(
            "Can only deal with horizontal lines right now");
        }

        protected void cubicBezierTo(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y)
        throws IOException {
            throw new UnsupportedOperationException(
            "Cannot handle cubic Bezier");
        }

        protected void rotateCoordinates(double angle) throws IOException {
            throw new UnsupportedOperationException(
            "Cannot handle coordinate rotation");
        }

        protected void scaleCoordinates(float xScale, float yScale) throws IOException {
            throw new UnsupportedOperationException(
            "Cannot handle coordinate scaling");
        }

        protected void translateCoordinates(int xTranslate, int yTranslate) throws IOException {
            throw new UnsupportedOperationException(
            "Cannot handle coordinate  translation");
        }
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
    throws IFException {
        try {
            this.borderPainter.drawLine(start, end, width, color, style);
        } catch (IOException ioe) {
            throw new IFException("I/O error in drawLine()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void drawText(                                        // CSOK: MethodLength
            int x, int y, final int letterSpacing, final int wordSpacing, final int[] dx,
            final String text) throws IFException {
        final int fontSize = this.state.getFontSize();
        getPaintingState().setFontSize(fontSize);

        FontTriplet triplet = new FontTriplet(
                state.getFontFamily(), state.getFontStyle(), state.getFontWeight());
        //TODO Ignored: state.getFontVariant()
        String fontKey = getFontInfo().getInternalFontKey(triplet);
        if (fontKey == null) {
            triplet = new FontTriplet("any", Font.STYLE_NORMAL, Font.WEIGHT_NORMAL);
            fontKey = getFontInfo().getInternalFontKey(triplet);
        }

        // register font as necessary
        Map/*<String,FontMetrics>*/ fontMetricMap = documentHandler.getFontInfo().getFonts();
        final AFPFont afpFont = (AFPFont)fontMetricMap.get(fontKey);
        final Font font = getFontInfo().getFontInstance(triplet, fontSize);
        AFPPageFonts pageFonts = getPaintingState().getPageFonts();
        AFPFontAttributes fontAttributes = pageFonts.registerFont(fontKey, afpFont, fontSize);

        final int fontReference = fontAttributes.getFontReference();

        final int[] coords = unitConv.mpts2units(new float[] {x, y} );

        final CharacterSet charSet = afpFont.getCharacterSet(fontSize);

        if (afpFont.isEmbeddable()) {
            try {
                documentHandler.getResourceManager().embedFont(afpFont, charSet);
            } catch (IOException ioe) {
                throw new IFException("Error while embedding font resources", ioe);
            }
        }

        AbstractPageObject page = getDataStream().getCurrentPage();
        PresentationTextObject pto = page.getPresentationTextObject();
        try {
            pto.createControlSequences(new PtocaProducer() {

                public void produce(PtocaBuilder builder) throws IOException {
                    Point p = getPaintingState().getPoint(coords[X], coords[Y]);
                    builder.setTextOrientation(getPaintingState().getRotation());
                    builder.absoluteMoveBaseline(p.y);
                    builder.absoluteMoveInline(p.x);

                    builder.setExtendedTextColor(state.getTextColor());
                    builder.setCodedFont((byte)fontReference);

                    int l = text.length();
                    int dxl = (dx != null ? dx.length : 0);
                    StringBuffer sb = new StringBuffer();

                    if (dxl > 0 && dx[0] != 0) {
                        int dxu = Math.round(unitConv.mpt2units(dx[0]));
                        builder.relativeMoveInline(-dxu);
                    }

                    //Following are two variants for glyph placement.
                    //SVI does not seem to be implemented in the same way everywhere, so
                    //a fallback alternative is preserved here.
                    final boolean usePTOCAWordSpacing = true;
                    if (usePTOCAWordSpacing) {

                        int interCharacterAdjustment = 0;
                        if (letterSpacing != 0) {
                            interCharacterAdjustment = Math.round(unitConv.mpt2units(
                                    letterSpacing));
                        }
                        builder.setInterCharacterAdjustment(interCharacterAdjustment);

                        int spaceWidth = font.getCharWidth(CharUtilities.SPACE);
                        int fixedSpaceCharacterIncrement = Math.round(unitConv.mpt2units(
                                spaceWidth + letterSpacing));
                        int varSpaceCharacterIncrement = fixedSpaceCharacterIncrement;
                        if (wordSpacing != 0) {
                            varSpaceCharacterIncrement = Math.round(unitConv.mpt2units(
                                    spaceWidth + wordSpacing + letterSpacing));
                        }
                        builder.setVariableSpaceCharacterIncrement(varSpaceCharacterIncrement);

                        boolean fixedSpaceMode = false;

                        for (int i = 0; i < l; i++) {
                            char orgChar = text.charAt(i);
                            float glyphAdjust = 0;
                            if (CharUtilities.isFixedWidthSpace(orgChar)) {
                                flushText(builder, sb, charSet);
                                builder.setVariableSpaceCharacterIncrement(
                                        fixedSpaceCharacterIncrement);
                                fixedSpaceMode = true;
                                sb.append(CharUtilities.SPACE);
                                int charWidth = font.getCharWidth(orgChar);
                                glyphAdjust += (charWidth - spaceWidth);
                            } else {
                                if (fixedSpaceMode) {
                                    flushText(builder, sb, charSet);
                                    builder.setVariableSpaceCharacterIncrement(
                                            varSpaceCharacterIncrement);
                                    fixedSpaceMode = false;
                                }
                                char ch;
                                if (orgChar == CharUtilities.NBSPACE) {
                                    ch = ' '; //converted to normal space to allow word spacing
                                } else {
                                    ch = orgChar;
                                }
                                sb.append(ch);
                            }

                            if (i < dxl - 1) {
                                glyphAdjust += dx[i + 1];
                            }

                            if (glyphAdjust != 0) {
                                flushText(builder, sb, charSet);
                                int increment = Math.round(unitConv.mpt2units(glyphAdjust));
                                builder.relativeMoveInline(increment);
                            }
                        }
                    } else {
                        for (int i = 0; i < l; i++) {
                            char orgChar = text.charAt(i);
                            float glyphAdjust = 0;
                            if (CharUtilities.isFixedWidthSpace(orgChar)) {
                                sb.append(CharUtilities.SPACE);
                                int spaceWidth = font.getCharWidth(CharUtilities.SPACE);
                                int charWidth = font.getCharWidth(orgChar);
                                glyphAdjust += (charWidth - spaceWidth);
                            } else {
                                sb.append(orgChar);
                            }

                            if ((wordSpacing != 0) && CharUtilities.isAdjustableSpace(orgChar)) {
                                glyphAdjust += wordSpacing;
                            }
                            glyphAdjust += letterSpacing;
                            if (i < dxl - 1) {
                                glyphAdjust += dx[i + 1];
                            }

                            if (glyphAdjust != 0) {
                                flushText(builder, sb, charSet);
                                int increment = Math.round(unitConv.mpt2units(glyphAdjust));
                                builder.relativeMoveInline(increment);
                            }
                        }
                    }
                    flushText(builder, sb, charSet);
                }

                private void flushText(PtocaBuilder builder, StringBuffer sb,
                        final CharacterSet charSet) throws IOException {
                    if (sb.length() > 0) {
                        builder.addTransparentData(charSet.encodeChars(sb));
                        sb.setLength(0);
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


    /** {@inheritDoc} */
    public void clipBackground(Rectangle rect, BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) throws IFException {

      //not supported by AFP
    }

    /** {@inheritDoc} */
    public boolean isBackgroundRequired(BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) {
        return borderPainter.isBackgroundRequired( bpsBefore,  bpsAfter,
                 bpsStart,  bpsEnd);
    }

    /** {@inheritDoc} */
    public void fillBackground(Rectangle rect, Paint fill, BorderProps bpsBefore,
            BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) throws IFException {
        // not supported in AFP
    }
}
