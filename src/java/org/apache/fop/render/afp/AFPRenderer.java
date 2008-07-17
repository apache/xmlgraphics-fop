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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.Block;
import org.apache.fop.area.CTM;
import org.apache.fop.area.LineArea;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.datatypes.URISpecification;
import org.apache.fop.events.ResourceEventProducer;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.render.AbstractPathOrientedRenderer;
import org.apache.fop.render.AbstractState;
import org.apache.fop.render.Graphics2DAdapter;
import org.apache.fop.render.RendererContext;
import org.apache.fop.render.afp.extensions.AFPElementMapping;
import org.apache.fop.render.afp.extensions.AFPPageSetup;
import org.apache.fop.render.afp.fonts.AFPFont;
import org.apache.fop.render.afp.fonts.AFPFontCollection;
import org.apache.fop.render.afp.modca.AFPConstants;
import org.apache.fop.render.afp.modca.AFPDataStream;
import org.apache.fop.render.afp.modca.PageObject;

/**
 * This is an implementation of a FOP Renderer that renders areas to AFP.
 * <p>
 * A renderer is primarily designed to convert a given area tree into the output
 * document format. It should be able to produce pages and fill the pages with
 * the text and graphical content. Usually the output is sent to an output
 * stream. Some output formats may support extra information that is not
 * available from the area tree or depends on the destination of the document.
 * Each renderer is given an area tree to render to its output format. The area
 * tree is simply a representation of the pages and the placement of text and
 * graphical objects on those pages.
 * </p>
 * <p>
 * The renderer will be given each page as it is ready and an output stream to
 * write the data out. All pages are supplied in the order they appear in the
 * document. In order to save memory it is possible to render the pages out of
 * order. Any page that is not ready to be rendered is setup by the renderer
 * first so that it can reserve a space or reference for when the page is ready
 * to be rendered.The renderer is responsible for managing the output format and
 * associated data and flow.
 * </p>
 * <p>
 * Each renderer is totally responsible for its output format. Because font
 * metrics (and therefore layout) are obtained in two different ways depending
 * on the renderer, the renderer actually sets up the fonts being used. The font
 * metrics are used during the layout process to determine the size of
 * characters.
 * </p>
 * <p>
 * The render context is used by handlers. It contains information about the
 * current state of the renderer, such as the page, the position, and any other
 * miscellaneous objects that are required to draw into the page.
 * </p>
 * <p>
 * A renderer is created by implementing the Renderer interface. However, the
 * AbstractRenderer does most of what is needed, including iterating through the
 * tree parts, so it is this that is extended. This means that this object only
 * need to implement the basic functionality such as text, images, and lines.
 * AbstractRenderer's methods can easily be overridden to handle things in a
 * different way or do some extra processing.
 * </p>
 * <p>
 * The relevant AreaTree structures that will need to be rendered are Page,
 * Viewport, Region, Span, Block, Line, Inline. A renderer implementation
 * renders each individual page, clips and aligns child areas to a viewport,
 * handle all types of inline area, text, image etc and draws various lines and
 * rectangles.
 * </p>
 *
 * Note: There are specific extensions that have been added to the FO. They are
 * specific to their location within the FO and have to be processed accordingly
 * (ie. at the start or end of the page).
 *
 */
public class AFPRenderer extends AbstractPathOrientedRenderer {

    private static final int X = 0;

    private static final int Y = 1;

    private static final int X1 = 0;

    private static final int Y1 = 1;

    private static final int X2 = 2;

    private static final int Y2 = 3;

    /**
     * The afp data stream object responsible for generating afp data
     */
    private AFPDataStream afpDataStream = null;

    /**
     * The map of page segments
     */
    private Map/*<String,String>*/pageSegmentsMap = null;

    /**
     * The map of saved incomplete pages
     */
    private Map pages = null;

    /** drawing state */
    private AFPState currentState = new AFPState();

    private boolean gocaEnabled = false;

    /**
     * Constructor for AFPRenderer.
     */
    public AFPRenderer() {
        super();
    }

    /** {@inheritDoc} */
    public void setupFontInfo(FontInfo inFontInfo) {
        this.fontInfo = inFontInfo;
        FontManager fontManager = userAgent.getFactory().getFontManager();
        FontCollection[] fontCollections = new FontCollection[] {
            new AFPFontCollection(userAgent.getEventBroadcaster(), getFontList())
        };
        fontManager.setup(getFontInfo(), fontCollections);
    }

    /** {@inheritDoc} */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
    }

    /** {@inheritDoc} */
    public void startRenderer(OutputStream outputStream) throws IOException {
        currentState.setColor(new Color(255, 255, 255));
        getAFPDataStream().setPortraitRotation(currentState.getPortraitRotation());
        afpDataStream.setLandscapeRotation(currentState.getLandscapeRotation());
        afpDataStream.setOutputStream(outputStream);
    }

    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {
        getAFPDataStream().write();
        afpDataStream = null;
    }

    /** {@inheritDoc} */
    public void startPageSequence(LineArea seqTitle) {
        getAFPDataStream().endPageGroup();
        afpDataStream.startPageGroup();
    }

    /** {@inheritDoc} */
    public boolean supportsOutOfOrder() {
        // return false;
        return true;
    }

    /** {@inheritDoc} */
    public void preparePage(PageViewport page) {
        final int pageRotation = 0;
        int pageWidth = currentState.getPageWidth();
        int pageHeight = currentState.getPageHeight();
        getAFPDataStream().startPage(pageWidth, pageHeight, pageRotation,
                getResolution(), getResolution());

        renderPageObjectExtensions(page);

        getPages().put(page, getAFPDataStream().savePage());
    }

    private Map/*<PageViewport, PageObject>*/ getPages() {
        if (this.pages == null) {
            this.pages = new java.util.HashMap/*<PageViewport, PageObject>*/();
        }
        return this.pages;
    }

    /** {@inheritDoc} */
    public void processOffDocumentItem(OffDocumentItem odi) {
        // TODO
        log.debug("NYI processOffDocumentItem(" + odi + ")");
    }

    /** {@inheritDoc} */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new AFPGraphics2DAdapter();
    }

    /** {@inheritDoc} */
    public void startVParea(CTM ctm, Rectangle2D clippingRect) {
        saveGraphicsState();
        if (ctm != null) {
            AffineTransform at = ctm.toAffineTransform();
            concatenateTransformationMatrix(at);
        }
        if (clippingRect != null) {
            clipRect((float)clippingRect.getX() / 1000f,
                    (float)clippingRect.getY() / 1000f,
                    (float)clippingRect.getWidth() / 1000f,
                    (float)clippingRect.getHeight() / 1000f);
        }
    }

    /** {@inheritDoc} */
    public void endVParea() {
        restoreGraphicsState();
    }

    /** {@inheritDoc} */
    protected void concatenateTransformationMatrix(AffineTransform at) {
        if (!at.isIdentity()) {
            currentState.concatenate(at);
        }
    }

    /** {@inheritDoc} */
    public void renderPage(PageViewport pageViewport) throws IOException, FOPException {
        currentState.clear();

        Rectangle2D bounds = pageViewport.getViewArea();

        AffineTransform basicPageTransform = new AffineTransform();
        int resolution = currentState.getResolution();
        double scale = mpt2units(1);
        basicPageTransform.scale(scale, scale);

        currentState.concatenate(basicPageTransform);

        if (getPages().containsKey(pageViewport)) {
            getAFPDataStream().restorePage(
                    (PageObject)getPages().remove(pageViewport));
        } else {
            int pageWidth
                = (int)Math.round(mpt2units((float)bounds.getWidth()));
            currentState.setPageWidth(pageWidth);
            int pageHeight
                = (int)Math.round(mpt2units((float)bounds.getHeight()));
            currentState.setPageHeight(pageHeight);

            final int pageRotation = 0;
            getAFPDataStream().startPage(pageWidth, pageHeight, pageRotation,
                    resolution, resolution);

            renderPageObjectExtensions(pageViewport);
        }

        super.renderPage(pageViewport);

        AFPPageFonts pageFonts = currentState.getPageFonts();
        if (pageFonts != null && !pageFonts.isEmpty()) {
            getAFPDataStream().addFontsToCurrentPage(pageFonts);
        }

        getAFPDataStream().endPage();
    }

    /** {@inheritDoc} */
    public void clip() {
        // TODO
        log.debug("NYI clip()");
    }

    /** {@inheritDoc} */
    public void clipRect(float x, float y, float width, float height) {
        // TODO
        log.debug("NYI clipRect(x=" + x + ",y=" + y + ",width=" + width + ", height=" + height + ")");
    }

    /** {@inheritDoc} */
    public void moveTo(float x, float y) {
        // TODO
        log.debug("NYI moveTo(x=" + x + ",y=" + y + ")");
    }

    /** {@inheritDoc} */
    public void lineTo(float x, float y) {
        // TODO
        log.debug("NYI lineTo(x=" + x + ",y=" + y + ")");
    }

    /** {@inheritDoc} */
    public void closePath() {
        // TODO
        log.debug("NYI closePath()");
    }

    private int[] mpts2units(float[] srcPts, float[] dstPts) {
        return transformPoints(srcPts, dstPts, true);
    }

    private int[] pts2units(float[] srcPts, float[] dstPts) {
        return transformPoints(srcPts, dstPts, false);
    }

    private int[] mpts2units(float[] srcPts) {
        return transformPoints(srcPts, null, true);
    }

    private int[] pts2units(float[] srcPts) {
        return transformPoints(srcPts, null, false);
    }

    private float mpt2units(float mpt) {
        return mpt / ((float)AFPConstants.DPI_72_MPTS / currentState.getResolution());
    }

    /** {@inheritDoc} */
    public void fillRect(float x, float y, float width, float height) {
        float[] srcPts = new float[] {x * 1000, y * 1000};
        float[] dstPts = new float[srcPts.length];
        int[] coords = mpts2units(srcPts, dstPts);
        int x2 = coords[X] + Math.round(mpt2units(width * 1000));
        int thickness = Math.round(mpt2units(height * 1000));
        getAFPDataStream().createLine(
                coords[X],
                coords[Y],
                x2,
                coords[Y],
                thickness,
                currentState.getColor());
    }

    /** {@inheritDoc} */
    public void drawBorderLine(float x1, float y1, float x2, float y2,
            boolean horz, boolean startOrBefore, int style, Color col) {
        float[] srcPts = new float[] {x1 * 1000, y1 * 1000, x2 * 1000, y2 * 1000};
        float[] dstPts = new float[srcPts.length];
        int[] coords = mpts2units(srcPts, dstPts);

        float width = dstPts[X2] - dstPts[X1];
        float height = dstPts[Y2] - dstPts[Y1];
        if ((width < 0) || (height < 0)) {
            log.error("Negative extent received. Border won't be painted.");
            return;
        }

        switch (style) {
        case Constants.EN_DOUBLE:
            if (horz) {
                float h3 = height / 3;
                float ym2 = dstPts[Y1] + h3 + h3;
                afpDataStream.createLine(
                        coords[X1],
                        coords[Y1],
                        coords[X2],
                        coords[Y1],
                        Math.round(h3),
                        col);
                afpDataStream.createLine(
                        coords[X1],
                        Math.round(ym2),
                        coords[X2],
                        Math.round(ym2),
                        Math.round(h3),
                        col);
            } else {
                float w3 = width / 3;
                float xm2 = dstPts[X1] + w3 + w3;
                afpDataStream.createLine(
                        coords[X1],
                        coords[Y1],
                        coords[X1],
                        coords[Y2],
                        Math.round(w3),
                        col);
                afpDataStream.createLine(
                        Math.round(xm2),
                        coords[Y1],
                        Math.round(xm2),
                        coords[Y2],
                        Math.round(w3),
                        col);
            }
            break;
        case Constants.EN_DASHED:
            if (horz) {
                float w2 = 2 * height;
                while (coords[X1] + w2 < coords[X2]) {
                    afpDataStream.createLine(
                            coords[X1],
                            coords[Y1],
                            coords[X1] + Math.round(w2),
                            coords[Y1],
                            Math.round(height),
                            col);
                    coords[X1] += 2 * w2;
                }
            } else {
                float h2 = 2 * width;
                while (coords[Y1] + h2 < coords[Y2]) {
                    afpDataStream.createLine(
                            coords[X1],
                            coords[Y2],
                            coords[X1],
                            coords[Y1] + Math.round(h2),
                            Math.round(width),
                            col);
                    coords[Y1] += 2 * h2;
                }
            }
            break;
        case Constants.EN_DOTTED:
            if (horz) {
                while (coords[X1] + height < coords[X2]) {
                    afpDataStream.createLine(
                            coords[X1],
                            coords[Y1],
                            coords[X1] + Math.round(height),
                            coords[Y1],
                            Math.round(height),
                            col
                    );
                    coords[X1] += 2 * height;
                }
            } else {
                while (y1 + width < y2) {
                    afpDataStream.createLine(
                            coords[X1],
                            coords[Y1],
                            coords[X1],
                            coords[Y1] + Math.round(width),
                            Math.round(width),
                            col);
                    coords[Y1] += 2 * width;
                }
            }
            break;
        case Constants.EN_GROOVE:
        case Constants.EN_RIDGE: {
            float colFactor = (style == EN_GROOVE ? 0.4f : -0.4f);
            if (horz) {
                Color uppercol = lightenColor(col, -colFactor);
                Color lowercol = lightenColor(col, colFactor);
                float h3 = height / 3;
                afpDataStream.createLine(
                        coords[X1],
                        coords[Y1],
                        coords[X2],
                        coords[Y1],
                        Math.round(h3),
                        uppercol);
                afpDataStream.createLine(
                        coords[X1],
                        Math.round(dstPts[Y1] + h3),
                        coords[X2],
                        Math.round(dstPts[Y1] + h3),
                        Math.round(h3),
                        col);
                afpDataStream.createLine(
                        coords[X1],
                        Math.round(dstPts[Y1] + h3 + h3),
                        coords[X2],
                        Math.round(dstPts[Y1] + h3 + h3),
                        Math.round(h3),
                        lowercol);
            } else {
                Color leftcol = lightenColor(col, -colFactor);
                Color rightcol = lightenColor(col, colFactor);
                float w3 = width / 3;
                float xm1 = dstPts[X1] + (w3 / 2);
                afpDataStream.createLine(
                        Math.round(xm1),
                        coords[Y1],
                        Math.round(xm1),
                        coords[Y2],
                        Math.round(w3),
                        leftcol);
                afpDataStream.createLine(
                        Math.round(xm1 + w3),
                        coords[Y1],
                        Math.round(xm1 + w3),
                        coords[Y2],
                        Math.round(w3),
                        col);
                afpDataStream.createLine(
                        Math.round(xm1 + w3 + w3),
                        coords[Y1],
                        Math.round(xm1 + w3 + w3),
                        coords[Y2],
                        Math.round(w3),
                        rightcol);
            }
            break;
        }
        case Constants.EN_HIDDEN:
            break;
        case Constants.EN_INSET:
        case Constants.EN_OUTSET:
        default:
              afpDataStream.createLine(
                      coords[X1],
                      coords[Y1],
                      (horz ? coords[X2] : coords[X1]),
                      (horz ? coords[Y1] : coords[Y2]),
                      Math.abs(Math.round(horz ? height : width)),
                      col);
        }
    }

    /** {@inheritDoc} */
    protected RendererContext createRendererContext(int x, int y, int width,
            int height, Map foreignAttributes) {
        RendererContext context;
        context = super.createRendererContext(x, y, width, height,
                foreignAttributes);
        context.setProperty(AFPRendererContextConstants.AFP_FONT_INFO,
                this.fontInfo);
        context.setProperty(AFPRendererContextConstants.AFP_DATASTREAM,
                getAFPDataStream());
        context.setProperty(AFPRendererContextConstants.AFP_STATE, getState());
        return context;
    }

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
            ImageFlavor.RAW_CCITTFAX, ImageFlavor.GRAPHICS2D,
            ImageFlavor.BUFFERED_IMAGE, ImageFlavor.RENDERED_IMAGE,
            ImageFlavor.XML_DOM };

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle2D pos, Map foreignAttributes) {
        uri = URISpecification.getURL(uri);
        currentState.setImageUri(uri);
        Rectangle posInt = new Rectangle((int) pos.getX(), (int) pos.getY(),
                (int) pos.getWidth(), (int) pos.getHeight());
        Point origin = new Point(currentIPPosition, currentBPPosition);
        int x = origin.x + posInt.x;
        int y = origin.y + posInt.y;

        String name = (String)getPageSegments().get(uri);
        if (name != null) {
            float[] srcPts = {x, y};
            int[] coords = mpts2units(srcPts);
            getAFPDataStream().createIncludePageSegment(name, coords[X], coords[Y]);
        } else {
            ImageManager manager = getUserAgent().getFactory().getImageManager();
            ImageInfo info = null;
            InputStream in = null;
            try {
                ImageSessionContext sessionContext = getUserAgent()
                        .getImageSessionContext();
                info = manager.getImageInfo(uri, sessionContext);

                // Only now fully load/prepare the image
                Map hints = ImageUtil.getDefaultHints(sessionContext);
                org.apache.xmlgraphics.image.loader.Image img = manager
                        .getImage(info, FLAVORS, hints, sessionContext);

                // ...and process the image
                if (img instanceof ImageGraphics2D) {
                    ImageGraphics2D imageG2D = (ImageGraphics2D) img;
                    RendererContext context = createRendererContext(posInt.x,
                            posInt.y, posInt.width, posInt.height,
                            foreignAttributes);
                    getGraphics2DAdapter().paintImage(
                            imageG2D.getGraphics2DImagePainter(), context,
                            origin.x + posInt.x, origin.y + posInt.y,
                            posInt.width, posInt.height);
                } else if (img instanceof ImageRendered) {
                    ImageRendered imgRend = (ImageRendered) img;
                    RenderedImage ri = imgRend.getRenderedImage();
                    drawBufferedImage(info, ri, getResolution(), posInt.x
                            + currentIPPosition, posInt.y + currentBPPosition,
                            posInt.width, posInt.height, foreignAttributes);
                } else if (img instanceof ImageRawCCITTFax) {
                    ImageRawCCITTFax ccitt = (ImageRawCCITTFax) img;
                    in = ccitt.createInputStream();
                    byte[] buf = IOUtils.toByteArray(in);
                    float[] srcPts = new float[] {
                            posInt.x + currentIPPosition,
                            posInt.y + currentBPPosition,
                            (float)posInt.getWidth(),
                            (float)posInt.getHeight()
                    };
                    int[] coords = mpts2units(srcPts);
                    String mimeType = info.getMimeType();
                    // create image object parameters
                    ImageObjectInfo imageObjectInfo = new ImageObjectInfo();
                    imageObjectInfo.setBuffered(false);
                    imageObjectInfo.setUri(uri);
                    imageObjectInfo.setMimeType(mimeType);

                    ObjectAreaInfo objectAreaInfo = new ObjectAreaInfo();
                    objectAreaInfo.setX(coords[X]);
                    objectAreaInfo.setY(coords[Y]);
                    int resolution = currentState.getResolution();
                    int w = Math.round(mpt2units((float)posInt.getWidth() * 1000));
                    int h = Math.round(mpt2units((float)posInt.getHeight() * 1000));
                    objectAreaInfo.setWidth(w);
                    objectAreaInfo.setHeight(h);
                    objectAreaInfo.setWidthRes(resolution);
                    objectAreaInfo.setHeightRes(resolution);
                    imageObjectInfo.setObjectAreaInfo(objectAreaInfo);

                    imageObjectInfo.setData(buf);
                    imageObjectInfo.setDataHeight(ccitt.getSize().getHeightPx());
                    imageObjectInfo.setDataWidth(ccitt.getSize().getWidthPx());
                    imageObjectInfo.setColor(currentState.isColorImages());
                    imageObjectInfo.setBitsPerPixel(currentState.getBitsPerPixel());
                    imageObjectInfo.setCompression(ccitt.getCompression());
                    imageObjectInfo.setResourceInfoFromForeignAttributes(foreignAttributes);
                    getAFPDataStream().createObject(imageObjectInfo);
                } else if (img instanceof ImageXMLDOM) {
                    ImageXMLDOM imgXML = (ImageXMLDOM) img;
                    renderDocument(imgXML.getDocument(), imgXML
                            .getRootNamespace(), pos, foreignAttributes);
                } else {
                    throw new UnsupportedOperationException(
                            "Unsupported image type: " + img);
                }

            } catch (ImageException ie) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider
                        .get(getUserAgent().getEventBroadcaster());
                eventProducer.imageError(this, (info != null ? info.toString()
                        : uri), ie, null);
            } catch (FileNotFoundException fe) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider
                        .get(getUserAgent().getEventBroadcaster());
                eventProducer.imageNotFound(this, (info != null ? info
                        .toString() : uri), fe, null);
            } catch (IOException ioe) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider
                        .get(getUserAgent().getEventBroadcaster());
                eventProducer.imageIOError(this, (info != null ? info
                        .toString() : uri), ioe, null);
            } finally {
                if (in != null) {
                    IOUtils.closeQuietly(in);
                }
            }
        }
    }

    /**
     * Writes a RenderedImage to an OutputStream as raw sRGB bitmaps.
     *
     * @param image
     *            the RenderedImage
     * @param out
     *            the OutputStream
     * @throws IOException
     *             In case of an I/O error.
     * @deprecated use ImageEncodingHelper.encodeRenderedImageAsRGB(image, out)
     *             directly instead
     */
    public static void writeImage(RenderedImage image, OutputStream out)
            throws IOException {
        ImageEncodingHelper.encodeRenderedImageAsRGB(image, out);
    }

    /**
     * Draws a BufferedImage to AFP.
     *
     * @param imageInfo
     *            the image info
     * @param image
     *            the RenderedImage
     * @param imageRes
     *            the resolution of the BufferedImage
     * @param x
     *            the x coordinate (in mpt)
     * @param y
     *            the y coordinate (in mpt)
     * @param width
     *            the width of the viewport (in mpt)
     * @param height
     *            the height of the viewport (in mpt)
     * @param foreignAttributes
     *            a mapping of foreign attributes
     */
    public void drawBufferedImage(ImageInfo imageInfo, RenderedImage image,
            int imageRes, int x, int y, int width, int height, Map foreignAttributes) {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        try {
            // Serialize image
            // TODO Eventually, this should be changed not to buffer as this
            // increases the
            // memory consumption (see PostScript output)
            ImageEncodingHelper.encodeRenderedImageAsRGB(image, baout);
        } catch (IOException ioe) {
            ResourceEventProducer eventProducer = ResourceEventProducer.Provider
                    .get(getUserAgent().getEventBroadcaster());
            eventProducer.imageWritingError(this, ioe);
            return;
        }

        // create image object parameters
        ImageObjectInfo imageObjectInfo = new ImageObjectInfo();
        imageObjectInfo.setBuffered(true);
        if (imageInfo != null) {
            imageObjectInfo.setUri(imageInfo.getOriginalURI());
            imageObjectInfo.setMimeType(imageInfo.getMimeType());
        }

        ObjectAreaInfo objectAreaInfo = new ObjectAreaInfo();

        float[] srcPts = new float[] {x, y};
        int[] coords = mpts2units(srcPts);
        objectAreaInfo.setX(coords[X]);
        objectAreaInfo.setY(coords[Y]);
        int w = Math.round(mpt2units(width));
        int h = Math.round(mpt2units(height));
        objectAreaInfo.setWidth(w);
        objectAreaInfo.setHeight(h);

        objectAreaInfo.setWidthRes(imageRes);
        objectAreaInfo.setHeightRes(imageRes);
        imageObjectInfo.setObjectAreaInfo(objectAreaInfo);

        imageObjectInfo.setData(baout.toByteArray());
        imageObjectInfo.setDataHeight(image.getHeight());
        imageObjectInfo.setDataWidth(image.getWidth());
        imageObjectInfo.setColor(currentState.isColorImages());
        imageObjectInfo.setBitsPerPixel(currentState.getBitsPerPixel());
        imageObjectInfo.setResourceInfoFromForeignAttributes(foreignAttributes);
        getAFPDataStream().createObject(imageObjectInfo);
    }

    /** {@inheritDoc} */
    public void updateColor(Color col, boolean fill) {
        if (fill) {
            currentState.setColor(col);
        }
    }

    /** {@inheritDoc} */
    public void restoreStateStackAfterBreakOut(List breakOutList) {
        log.debug("Block.FIXED --> restoring context after break-out");
        AbstractState.AbstractData data;
        Iterator it = breakOutList.iterator();
        while (it.hasNext()) {
            data = (AbstractState.AbstractData)it.next();
            saveGraphicsState();
            concatenateTransformationMatrix(data.getTransform());
        }
    }

    /** {@inheritDoc} */
    protected List breakOutOfStateStack() {
        log.debug("Block.FIXED --> break out");
        List breakOutList = new java.util.ArrayList();
        AbstractState.AbstractData data;
        while (true) {
            data = currentState.getData();
            if (currentState.pop() == null) {
                break;
            }
            breakOutList.add(0, data); //Insert because of stack-popping
        }
        return breakOutList;
    }

    /** {@inheritDoc} */
    public void saveGraphicsState() {
        currentState.push();
    }

    /** {@inheritDoc} */
    public void restoreGraphicsState() {
        currentState.pop();
    }

    /** Indicates the beginning of a text object. */
    public void beginTextObject() {
        //TODO maybe?
        log.debug("NYI beginTextObject()");
    }

    /** Indicates the end of a text object. */
    public void endTextObject() {
        //TODO maybe?
        log.debug("NYI endTextObject()");
    }

    /** {@inheritDoc} */
    public void renderImage(Image image, Rectangle2D pos) {
        drawImage(image.getURL(), pos, image.getForeignAttributes());
    }

    /** {@inheritDoc} */
    public void renderText(TextArea text) {
        log.debug(text.getText());
        renderInlineAreaBackAndBorders(text);

        String name = getInternalFontNameForArea(text);
        int fontSize = ((Integer) text.getTrait(Trait.FONT_SIZE)).intValue();
        currentState.setFontSize(fontSize);
        AFPFont font = (AFPFont)fontInfo.getFonts().get(name);

        // Set letterSpacing
        // float ls = fs.getLetterSpacing() / this.currentFontSize;

        // Create an AFPFontAttributes object from the current font details
        AFPFontAttributes afpFontAttributes
            = new AFPFontAttributes(name, font, fontSize);

        AFPPageFonts pageFonts = currentState.getPageFonts();
        if (!pageFonts.containsKey(afpFontAttributes.getFontKey())) {
            // Font not found on current page, so add the new one
            afpFontAttributes.setFontReference(currentState.incrementPageFontCount());
            pageFonts.put(afpFontAttributes.getFontKey(), afpFontAttributes);
        } else {
            // Use the previously stored font attributes
            afpFontAttributes = (AFPFontAttributes) pageFonts.get(afpFontAttributes.getFontKey());
        }

        // Try and get the encoding to use for the font
        String encoding = null;

        try {
            encoding = font.getCharacterSet(fontSize).getEncoding();
        } catch (Throwable ex) {
            encoding = AFPConstants.EBCIDIC_ENCODING;
            log.warn("renderText():: Error getting encoding for font '"
                    + font.getFullName() + "' - using default encoding "
                    + encoding);
        }

        byte[] data = null;
        try {
            String worddata = text.getText();
            data = worddata.getBytes(encoding);
        } catch (UnsupportedEncodingException usee) {
            log.error("renderText:: Font " + afpFontAttributes.getFontKey()
                    + " caused UnsupportedEncodingException");
            return;
        }

        int fontReference = afpFontAttributes.getFontReference();

        int x = (currentIPPosition + text.getBorderAndPaddingWidthStart());
        int y = (currentBPPosition + text.getOffset() + text.getBaselineOffset());
        float[] srcPts = new float[] {x, y};
        int[] coords = mpts2units(srcPts);

        Color color = (Color) text.getTrait(Trait.COLOR);

        int variableSpaceCharacterIncrement = font.getWidth(' ', fontSize) / 1000
          + text.getTextWordSpaceAdjust()
          + text.getTextLetterSpaceAdjust();
        variableSpaceCharacterIncrement = Math.round(mpt2units(variableSpaceCharacterIncrement));

        int interCharacterAdjustment = Math.round(mpt2units(text.getTextLetterSpaceAdjust()));

        AFPTextDataInfo textDataInfo = new AFPTextDataInfo();
        textDataInfo.setFontReference(fontReference);
        textDataInfo.setX(coords[X]);
        textDataInfo.setY(coords[Y]);
        textDataInfo.setColor(color);
        textDataInfo.setVariableSpaceCharacterIncrement(variableSpaceCharacterIncrement);
        textDataInfo.setInterCharacterAdjustment(interCharacterAdjustment);
        textDataInfo.setData(data);
        getAFPDataStream().createText(textDataInfo);
        // word.getOffset() = only height of text itself
        // currentBlockIPPosition: 0 for beginning of line; nonzero
        // where previous line area failed to take up entire allocated space

        super.renderText(text);

        renderTextDecoration(font, fontSize, text, coords[Y], coords[X]);
    }

    /**
     * Render leader area. This renders a leader area which is an area with a
     * rule.
     *
     * @param area
     *            the leader area to render
     */
    public void renderLeader(Leader area) {
        renderInlineAreaBackAndBorders(area);

        int style = area.getRuleStyle();
        float startx = (currentIPPosition + area
                .getBorderAndPaddingWidthStart()) / 1000f;
        float starty = (currentBPPosition + area.getOffset()) / 1000f;
        float endx = (currentIPPosition + area.getBorderAndPaddingWidthStart() + area
                .getIPD()) / 1000f;
        float ruleThickness = area.getRuleThickness() / 1000f;
        Color col = (Color) area.getTrait(Trait.COLOR);

        switch (style) {
        case EN_SOLID:
        case EN_DASHED:
        case EN_DOUBLE:
        case EN_DOTTED:
        case EN_GROOVE:
        case EN_RIDGE:
            drawBorderLine(startx, starty, endx, starty + ruleThickness, true,
                    true, style, col);
            break;
        default:
            throw new UnsupportedOperationException("rule style not supported");
        }
        super.renderLeader(area);
    }

    /**
     * Sets the rotation to be used for portrait pages, valid values are 0
     * (default), 90, 180, 270.
     *
     * @param rotation
     *            The rotation in degrees.
     */
    public void setPortraitRotation(int rotation) {
        currentState.setPortraitRotation(rotation);
    }

    /**
     * Sets the rotation to be used for landsacpe pages, valid values are 0, 90,
     * 180, 270 (default).
     *
     * @param rotation
     *            The rotation in degrees.
     */
    public void setLandscapeRotation(int rotation) {
        currentState.setLandscapeRotation(rotation);
    }

    /**
     * Get the MIME type of the renderer.
     *
     * @return The MIME type of the renderer
     */
    public String getMimeType() {
        return MimeConstants.MIME_AFP;
    }

    private Map/*<String,String>*/getPageSegments() {
        if (pageSegmentsMap == null) {
            pageSegmentsMap = new java.util.HashMap/*<String,String>*/();
        }
        return pageSegmentsMap;
    }

    /**
     * Method to render the page extension.
     * <p>
     *
     * @param pageViewport
     *            the page object
     */
    private void renderPageObjectExtensions(PageViewport pageViewport) {

        this.pageSegmentsMap = null;
        if (pageViewport.getExtensionAttachments() != null
                && pageViewport.getExtensionAttachments().size() > 0) {
            // Extract all AFPPageSetup instances from the attachment list on
            // the s-p-m
            Iterator it = pageViewport.getExtensionAttachments().iterator();
            while (it.hasNext()) {
                ExtensionAttachment attachment = (ExtensionAttachment) it.next();
                if (AFPPageSetup.CATEGORY.equals(attachment.getCategory())) {
                    AFPPageSetup aps = (AFPPageSetup) attachment;
                    String element = aps.getElementName();
                    if (AFPElementMapping.INCLUDE_PAGE_OVERLAY.equals(element)) {
                        String overlay = aps.getName();
                        if (overlay != null) {
                            getAFPDataStream()
                                    .createIncludePageOverlay(overlay);
                        }
                    } else if (AFPElementMapping.INCLUDE_PAGE_SEGMENT
                            .equals(element)) {
                        String name = aps.getName();
                        String source = aps.getValue();
                        getPageSegments().put(source, name);
                    } else if (AFPElementMapping.TAG_LOGICAL_ELEMENT
                            .equals(element)) {
                        String name = aps.getName();
                        String value = aps.getValue();
                        getAFPDataStream().createTagLogicalElement(name, value);
                    } else if (AFPElementMapping.NO_OPERATION.equals(element)) {
                        String content = aps.getContent();
                        if (content != null) {
                            getAFPDataStream().createNoOperation(content);
                        }
                    }
                }
            }
        }

    }

    /**
     * Sets the number of bits used per pixel
     *
     * @param bitsPerPixel
     *            number of bits per pixel
     */
    public void setBitsPerPixel(int bitsPerPixel) {
        currentState.setBitsPerPixel(bitsPerPixel);
    }

    /**
     * Sets whether images are color or not
     *
     * @param colorImages
     *            color image output
     */
    public void setColorImages(boolean colorImages) {
        currentState.setColorImages(colorImages);
    }

    /**
     * Returns the AFPDataStream
     *
     * @return the AFPDataStream
     */
    public AFPDataStream getAFPDataStream() {
        if (afpDataStream == null) {
            this.afpDataStream = new AFPDataStream();
        }
        return afpDataStream;
    }

    /**
     * Sets the output/device resolution
     *
     * @param resolution
     *            the output resolution (dpi)
     */
    public void setResolution(int resolution) {
        ((AFPState)getState()).setResolution(resolution);
    }

    /**
     * Returns the output/device resolution.
     *
     * @return the resolution in dpi
     */
    public int getResolution() {
        return ((AFPState)getState()).getResolution();
    }

    /**
     * @return the current AFP state
     */
    protected AbstractState getState() {
        if (currentState == null) {
            currentState = new AFPState();
        }
        return currentState;
    }

    /**
     * @param enabled
     *            true if AFP GOCA is enabled for SVG support
     */
    protected void setGOCAEnabled(boolean enabled) {
        this.gocaEnabled = enabled;
    }

    /**
     * @return true of AFP GOCA is enabled for SVG support
     */
    protected boolean isGOCAEnabled() {
        return this.gocaEnabled;
    }

    // TODO: remove this and use the superclass implementation
    /** {@inheritDoc} */
    protected void renderReferenceArea(Block block) {
        // save position and offset
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;

        //Establish a new coordinate system
        AffineTransform at = new AffineTransform();
        at.translate(currentIPPosition, currentBPPosition);
        at.translate(block.getXOffset(), block.getYOffset());
        at.translate(0, block.getSpaceBefore());

        if (!at.isIdentity()) {
            saveGraphicsState();
            concatenateTransformationMatrix(at);
        }

        currentIPPosition = 0;
        currentBPPosition = 0;
        handleBlockTraits(block);

        List children = block.getChildAreas();
        if (children != null) {
            renderBlocks(block, children);
        }

        if (!at.isIdentity()) {
            restoreGraphicsState();
        }

        // stacked and relative blocks effect stacking
        currentIPPosition = saveIP;
        currentBPPosition = saveBP;
    }

    protected int[] transformPoints(float[] srcPts, float[] dstPts) {
        return transformPoints(srcPts, dstPts, true);
    }

    protected int[] transformPoints(float[] srcPts, float[] dstPts, boolean milli) {
        if (dstPts == null) {
            dstPts = new float[srcPts.length];
        }
        AbstractState state = (AbstractState)getState();
        AffineTransform at = state.getData().getTransform();
        at.transform(srcPts, 0, dstPts, 0, srcPts.length / 2);
        int[] coords = new int[srcPts.length];
        for (int i = 0; i < srcPts.length; i++) {
            if (!milli) {
                dstPts[i] *= 1000;
            }
            coords[i] = Math.round(dstPts[i]);
        }
        return coords;
    }

}
