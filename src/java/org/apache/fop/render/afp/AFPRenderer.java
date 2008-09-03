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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.apache.fop.render.afp.modca.DataStream;
import org.apache.fop.render.afp.modca.PageObject;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.impl.ImageXMLDOM;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.ps.ImageEncodingHelper;

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

    /** Normal PDF resolution (72dpi) */
    public static final int NORMAL_AFP_RESOLUTION = 72;

    private static final int X = 0;
    private static final int Y = 1;

    /** resource manager */
    private AFPResourceManager resourceManager;

    /** drawing state */
    private final AFPState state;

    /** unit converter */
    private final AFPUnitConverter unitConv;

    /** line painter */
    private AFPBorderPainter borderPainter;

    /** The map of page segments */
    private final Map/*<String,String>*/pageSegmentMap = new java.util.HashMap/*<String,String>*/();

    /** The map of saved incomplete pages */
    private final Map pages = new java.util.HashMap/*<PageViewport,PageObject>*/();

    /** the afp datastream */
    private DataStream dataStream;

    /** data object information factory */
    private final AFPDataObjectInfoFactory dataObjectInfoFactory;


    /**
     * Constructor for AFPRenderer.
     */
    public AFPRenderer() {
        super();
        this.resourceManager = new AFPResourceManager();
        this.state = new AFPState();
        this.dataObjectInfoFactory = new AFPDataObjectInfoFactory(state);
        this.unitConv = state.getUnitConverter();
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
        state.setColor(new Color(255, 255, 255));
        resourceManager.setOutputStream(outputStream);

        this.dataStream = resourceManager.getDataStream();
        dataStream.setPortraitRotation(state.getPortraitRotation());
        dataStream.setLandscapeRotation(state.getLandscapeRotation());
        dataStream.startDocument();

        this.borderPainter = new AFPBorderPainter(state, dataStream);
    }

    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {
        dataStream.endDocument();
        resourceManager.writeToStream();
        resourceManager = null;
    }

    /** {@inheritDoc} */
    public void startPageSequence(LineArea seqTitle) {
        try {
            dataStream.startPageGroup();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    public boolean supportsOutOfOrder() {
        return true;
    }

    /** {@inheritDoc} */
    public void preparePage(PageViewport page) {
        final int pageRotation = 0;
        int pageWidth = state.getPageWidth();
        int pageHeight = state.getPageHeight();
        int resolution = state.getResolution();
        dataStream.startPage(pageWidth, pageHeight, pageRotation,
                resolution, resolution);

        renderPageObjectExtensions(page);

        PageObject currentPage = dataStream.savePage();
        pages.put(page, currentPage);
    }

    /** {@inheritDoc} */
    public void processOffDocumentItem(OffDocumentItem odi) {
        // TODO
        log.debug("NYI processOffDocumentItem(" + odi + ")");
    }

    /** {@inheritDoc} */
    public Graphics2DAdapter getGraphics2DAdapter() {
        return new AFPGraphics2DAdapter(this);
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
            state.concatenate(at);
        }
    }

    /**
     * Returns the base AFP transform
     *
     * @return the base AFP transform
     */
    private AffineTransform getBaseTransform() {
        AffineTransform baseTransform = new AffineTransform();
        double scale = unitConv.mpt2units(1);
        baseTransform.scale(scale, scale);
        return baseTransform;
    }

    /** {@inheritDoc} */
    public void renderPage(PageViewport pageViewport) throws IOException, FOPException {
        state.clear();

        Rectangle2D bounds = pageViewport.getViewArea();

        state.concatenate(getBaseTransform());

        if (pages.containsKey(pageViewport)) {
            dataStream.restorePage(
                    (PageObject)pages.remove(pageViewport));
        } else {
            int pageWidth
                = Math.round(unitConv.mpt2units((float)bounds.getWidth()));
            state.setPageWidth(pageWidth);
            int pageHeight
                = Math.round(unitConv.mpt2units((float)bounds.getHeight()));
            state.setPageHeight(pageHeight);

            final int pageRotation = 0;
            int resolution = state.getResolution();
            dataStream.startPage(pageWidth, pageHeight, pageRotation,
                    resolution, resolution);

            renderPageObjectExtensions(pageViewport);
        }

        super.renderPage(pageViewport);

        AFPPageFonts pageFonts = state.getPageFonts();
        if (pageFonts != null && !pageFonts.isEmpty()) {
            dataStream.addFontsToCurrentPage(pageFonts);
        }

        dataStream.endPage();
    }

    /** {@inheritDoc} */
    public void clip() {
        // TODO
        log.debug("NYI clip()");
    }

    /** {@inheritDoc} */
    public void clipRect(float x, float y, float width, float height) {
        // TODO
        log.debug("NYI clipRect(x=" + x + ",y=" + y
                    + ",width=" + width + ", height=" + height + ")");
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

    /** {@inheritDoc} */
    public void fillRect(float x, float y, float width, float height) {
        borderPainter.fillRect(x, y, width, height);
    }

    /** {@inheritDoc} */
    public void drawBorderLine(float x1, float y1, float x2, float y2,
            boolean horz, boolean startOrBefore, int style, Color col) {
        borderPainter.drawBorderLine(x1, y1, x2, y2, horz, startOrBefore, style, col);
    }

    /** {@inheritDoc} */
    protected RendererContext createRendererContext(int x, int y, int width,
            int height, Map foreignAttributes) {
        RendererContext context;
        context = super.createRendererContext(x, y, width, height,
                foreignAttributes);
        context.setProperty(AFPRendererContextConstants.AFP_FONT_INFO,
                this.fontInfo);
        context.setProperty(AFPRendererContextConstants.AFP_RESOURCE_MANAGER,
                this.resourceManager);
        context.setProperty(AFPRendererContextConstants.AFP_STATE, state);
        return context;
    }

    private static final ImageFlavor[] FLAVORS = new ImageFlavor[] {
        ImageFlavor.RAW_JPEG, ImageFlavor.RAW_CCITTFAX, ImageFlavor.RAW_EPS,
        ImageFlavor.GRAPHICS2D, ImageFlavor.BUFFERED_IMAGE, ImageFlavor.RENDERED_IMAGE,
        ImageFlavor.XML_DOM };

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle2D pos, Map foreignAttributes) {
        uri = URISpecification.getURL(uri);
        state.setImageUri(uri);
        Rectangle posInt = new Rectangle((int) pos.getX(), (int) pos.getY(),
                (int) pos.getWidth(), (int) pos.getHeight());
        String name = (String)pageSegmentMap.get(uri);
        int x = currentIPPosition + posInt.x;
        int y = currentBPPosition + posInt.y;
        float[] srcPts = {x, y};
        int[] coords = unitConv.mpts2units(srcPts);
        if (name != null) {
            dataStream.createIncludePageSegment(name, coords[X], coords[Y]);
        } else {
            ImageManager manager = userAgent.getFactory().getImageManager();
            ImageInfo info = null;
            try {
                ImageSessionContext sessionContext = userAgent
                        .getImageSessionContext();
                info = manager.getImageInfo(uri, sessionContext);

                // Only now fully load/prepare the image
                Map hints = ImageUtil.getDefaultHints(sessionContext);
                org.apache.xmlgraphics.image.loader.Image img = manager.getImage(
                        info, FLAVORS, hints, sessionContext);

                Point origin = new Point(currentIPPosition, currentBPPosition);
                AFPAbstractImageFactory factory = dataObjectInfoFactory.getFactory(img);
                if (factory != null) {
                    AFPImageInfo afpImageInfo
                    = new AFPImageInfo(uri, pos, origin, info, img, foreignAttributes);
                    AFPDataObjectInfo dataObjectInfo = null;
                    try {
                        dataObjectInfo = factory.create(afpImageInfo);
                    } catch (IOException ioe) {
                        ResourceEventProducer eventProducer
                            = ResourceEventProducer.Provider.get(userAgent.getEventBroadcaster());
                        eventProducer.imageWritingError(this, ioe);
                        throw ioe;
                    }
                    resourceManager.createObject(dataObjectInfo);
                } else if (img instanceof ImageGraphics2D) { // ...and process the image
                    ImageGraphics2D imageG2D = (ImageGraphics2D) img;
                    RendererContext rendererContext = createRendererContext(
                            x, y, posInt.width, posInt.height, foreignAttributes);
                    getGraphics2DAdapter().paintImage(
                            imageG2D.getGraphics2DImagePainter(), rendererContext,
                            origin.x + posInt.x, origin.y + posInt.y, posInt.width,
                            posInt.height);
                } else if (img instanceof ImageXMLDOM) {
                    ImageXMLDOM imgXML = (ImageXMLDOM) img;
                    renderDocument(imgXML.getDocument(), imgXML.getRootNamespace(),
                            posInt, foreignAttributes);
                } else {
                    throw new UnsupportedOperationException(
                            "Unsupported image type: " + img);
                }
            } catch (ImageException ie) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider
                        .get(userAgent.getEventBroadcaster());
                eventProducer.imageError(this, (info != null ? info.toString()
                        : uri), ie, null);
            } catch (FileNotFoundException fe) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider
                        .get(userAgent.getEventBroadcaster());
                eventProducer.imageNotFound(this, (info != null ? info.toString()
                        : uri), fe, null);
            } catch (IOException ioe) {
                ResourceEventProducer eventProducer = ResourceEventProducer.Provider
                        .get(userAgent.getEventBroadcaster());
                eventProducer.imageIOError(this, (info != null ? info.toString()
                        : uri), ioe, null);
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

    /** {@inheritDoc} */
    public void updateColor(Color col, boolean fill) {
        if (fill) {
            state.setColor(col);
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
            data = state.getData();
            if (state.pop() == null) {
                break;
            }
            breakOutList.add(0, data); //Insert because of stack-popping
        }
        return breakOutList;
    }

    /** {@inheritDoc} */
    public void saveGraphicsState() {
        state.push();
    }

    /** {@inheritDoc} */
    public void restoreGraphicsState() {
        state.pop();
    }

    /** Indicates the beginning of a text object. */
    public void beginTextObject() {
        //TODO PDF specific maybe?
        log.debug("NYI beginTextObject()");
    }

    /** Indicates the end of a text object. */
    public void endTextObject() {
        //TODO PDF specific maybe?
        log.debug("NYI endTextObject()");
    }

    /** {@inheritDoc} */
    public void renderImage(Image image, Rectangle2D pos) {
        drawImage(image.getURL(), pos, image.getForeignAttributes());
    }

    /** {@inheritDoc} */
    public void renderText(TextArea text) {
        renderInlineAreaBackAndBorders(text);

        String name = getInternalFontNameForArea(text);
        int fontSize = ((Integer) text.getTrait(Trait.FONT_SIZE)).intValue();
        state.setFontSize(fontSize);
        AFPFont font = (AFPFont)fontInfo.getFonts().get(name);

        // Set letterSpacing
        // float ls = fs.getLetterSpacing() / this.currentFontSize;

        // Create an AFPFontAttributes object from the current font details
        AFPFontAttributes afpFontAttributes
            = new AFPFontAttributes(name, font, fontSize);

        AFPPageFonts pageFonts = state.getPageFonts();
        if (!pageFonts.containsKey(afpFontAttributes.getFontKey())) {
            // Font not found on current page, so add the new one
            afpFontAttributes.setFontReference(state.incrementPageFontCount());
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
        int[] coords = unitConv.mpts2units(srcPts);

        Color color = (Color) text.getTrait(Trait.COLOR);

        int variableSpaceCharacterIncrement = font.getWidth(' ', fontSize) / 1000
          + text.getTextWordSpaceAdjust()
          + text.getTextLetterSpaceAdjust();
        variableSpaceCharacterIncrement
            = Math.round(unitConv.mpt2units(variableSpaceCharacterIncrement));

        int interCharacterAdjustment
            = Math.round(unitConv.mpt2units(text.getTextLetterSpaceAdjust()));

        AFPTextDataInfo textDataInfo = new AFPTextDataInfo();
        textDataInfo.setFontReference(fontReference);
        textDataInfo.setX(coords[X]);
        textDataInfo.setY(coords[Y]);
        textDataInfo.setColor(color);
        textDataInfo.setVariableSpaceCharacterIncrement(variableSpaceCharacterIncrement);
        textDataInfo.setInterCharacterAdjustment(interCharacterAdjustment);
        textDataInfo.setData(data);
        textDataInfo.setOrientation(state.getOrientation());
        dataStream.createText(textDataInfo);
        // word.getOffset() = only height of text itself
        // currentBlockIPPosition: 0 for beginning of line; nonzero
        // where previous line area failed to take up entire allocated space

        super.renderText(text);

        //TODO fix text decoration
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
     * Get the MIME type of the renderer.
     *
     * @return The MIME type of the renderer
     */
    public String getMimeType() {
        return MimeConstants.MIME_AFP;
    }

    /**
     * Method to render the page extension.
     * <p>
     *
     * @param pageViewport
     *            the page object
     */
    private void renderPageObjectExtensions(PageViewport pageViewport) {
        pageSegmentMap.clear();
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
                            dataStream.createIncludePageOverlay(overlay);
                        }
                    } else if (AFPElementMapping.INCLUDE_PAGE_SEGMENT
                            .equals(element)) {
                        String name = aps.getName();
                        String source = aps.getValue();
                        pageSegmentMap.put(source, name);
                    } else if (AFPElementMapping.TAG_LOGICAL_ELEMENT
                            .equals(element)) {
                        String name = aps.getName();
                        String value = aps.getValue();
                        dataStream.createTagLogicalElement(name, value);
                    } else if (AFPElementMapping.NO_OPERATION.equals(element)) {
                        String content = aps.getContent();
                        if (content != null) {
                            dataStream.createNoOperation(content);
                        }
                    }
                }
            }
        }

    }

    /**
     * Sets the rotation to be used for portrait pages, valid values are 0
     * (default), 90, 180, 270.
     *
     * @param rotation
     *            The rotation in degrees.
     */
    public void setPortraitRotation(int rotation) {
        state.setPortraitRotation(rotation);
    }

    /**
     * Sets the rotation to be used for landsacpe pages, valid values are 0, 90,
     * 180, 270 (default).
     *
     * @param rotation
     *            The rotation in degrees.
     */
    public void setLandscapeRotation(int rotation) {
        state.setLandscapeRotation(rotation);
    }

    /**
     * Sets the number of bits used per pixel
     *
     * @param bitsPerPixel
     *            number of bits per pixel
     */
    public void setBitsPerPixel(int bitsPerPixel) {
        state.setBitsPerPixel(bitsPerPixel);
    }

    /**
     * Sets whether images are color or not
     *
     * @param colorImages
     *            color image output
     */
    public void setColorImages(boolean colorImages) {
        state.setColorImages(colorImages);
    }

    /**
     * Returns the AFPDataStream
     *
     * @return the AFPDataStream
     */
    public DataStream getAFPDocumentStream() {
        return this.dataStream;
    }

    /**
     * Sets the output/device resolution
     *
     * @param resolution
     *            the output resolution (dpi)
     */
    public void setResolution(int resolution) {
        state.setResolution(resolution);
    }

    /**
     * Returns the output/device resolution.
     *
     * @return the resolution in dpi
     */
    public int getResolution() {
        return state.getResolution();
    }

    /**
     * Returns the current AFP state
     *
     * @return the current AFP state
     */
    public AbstractState getState() {
        return this.state;
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

    /**
     * Sets the default resource group file path
     * @param filePath the default resource group file path
     */
    public void setDefaultResourceGroupFilePath(String filePath) {
        resourceManager.setDefaultResourceGroupFilePath(filePath);
    }

}
