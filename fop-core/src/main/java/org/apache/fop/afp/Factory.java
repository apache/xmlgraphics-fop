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

package org.apache.fop.afp;

import java.io.OutputStream;

import org.apache.fop.afp.goca.GraphicsData;
import org.apache.fop.afp.ioca.BandImage;
import org.apache.fop.afp.ioca.ImageContent;
import org.apache.fop.afp.ioca.ImageRasterData;
import org.apache.fop.afp.ioca.ImageSegment;
import org.apache.fop.afp.ioca.ImageSizeParameter;
import org.apache.fop.afp.ioca.Tile;
import org.apache.fop.afp.ioca.TilePosition;
import org.apache.fop.afp.ioca.TileSize;
import org.apache.fop.afp.ioca.TileTOC;
import org.apache.fop.afp.modca.ActiveEnvironmentGroup;
import org.apache.fop.afp.modca.ContainerDataDescriptor;
import org.apache.fop.afp.modca.Document;
import org.apache.fop.afp.modca.GraphicsDataDescriptor;
import org.apache.fop.afp.modca.GraphicsObject;
import org.apache.fop.afp.modca.IMImageObject;
import org.apache.fop.afp.modca.ImageDataDescriptor;
import org.apache.fop.afp.modca.ImageObject;
import org.apache.fop.afp.modca.IncludeObject;
import org.apache.fop.afp.modca.IncludePageSegment;
import org.apache.fop.afp.modca.InvokeMediumMap;
import org.apache.fop.afp.modca.MapCodedFont;
import org.apache.fop.afp.modca.MapContainerData;
import org.apache.fop.afp.modca.MapDataResource;
import org.apache.fop.afp.modca.ObjectAreaDescriptor;
import org.apache.fop.afp.modca.ObjectAreaPosition;
import org.apache.fop.afp.modca.ObjectContainer;
import org.apache.fop.afp.modca.ObjectEnvironmentGroup;
import org.apache.fop.afp.modca.Overlay;
import org.apache.fop.afp.modca.PageDescriptor;
import org.apache.fop.afp.modca.PageGroup;
import org.apache.fop.afp.modca.PageObject;
import org.apache.fop.afp.modca.PresentationEnvironmentControl;
import org.apache.fop.afp.modca.PresentationTextDescriptor;
import org.apache.fop.afp.modca.PresentationTextObject;
import org.apache.fop.afp.modca.ResourceEnvironmentGroup;
import org.apache.fop.afp.modca.ResourceGroup;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.afp.modca.StreamedResourceGroup;
import org.apache.fop.afp.modca.TagLogicalElement;
import org.apache.fop.afp.util.StringUtils;

/**
 * Creator of MO:DCA structured field objects
 */
public class Factory {

    private static final String OBJECT_ENVIRONMENT_GROUP_NAME_PREFIX = "OEG";

    private static final String ACTIVE_ENVIRONMENT_GROUP_NAME_PREFIX = "AEG";

    private static final String IMAGE_NAME_PREFIX = "IMG";

    private static final String GRAPHIC_NAME_PREFIX = "GRA";

    private static final String BARCODE_NAME_PREFIX = "BAR";

//    private static final String OTHER_NAME_PREFIX = "OTH";

    private static final String OBJECT_CONTAINER_NAME_PREFIX = "OC";

    private static final String RESOURCE_NAME_PREFIX = "RES";

    private static final String RESOURCE_GROUP_NAME_PREFIX = "RG";

    private static final String PAGE_GROUP_NAME_PREFIX = "PGP";

    private static final String PAGE_NAME_PREFIX = "PGN";

    private static final String OVERLAY_NAME_PREFIX = "OVL";

    private static final String PRESENTATION_TEXT_NAME_PREFIX = "PT";

    private static final String DOCUMENT_NAME_PREFIX = "DOC";

    private static final String IM_IMAGE_NAME_PREFIX = "IMIMG";

    private static final String IMAGE_SEGMENT_NAME_PREFIX = "IS";


    /** the page group count */
    private int pageGroupCount;

    /** the page count */
    private int pageCount;

    /** the image count */
    private int imageCount;

    /** the im image count */
    private int imImageCount;

    /** the image segment count */
    private int imageSegmentCount;

    /** the graphic count */
    private int graphicCount;

    /** the object container count */
    private int objectContainerCount;

    /** the resource count */
    private int resourceCount;

    /** the resource group count */
    private int resourceGroupCount;

    /** the overlay count */
    private int overlayCount;

    /** the presentation text object count */
    private int textObjectCount;

    /** the active environment group count */
    private int activeEnvironmentGroupCount;

    /** the document count */
    private int documentCount;

    /** the object environment group count */
    private int objectEnvironmentGroupCount;

    /**
     * Main constructor
     */
    public Factory() {
    }

    /**
     * Creates a new IOCA {@link ImageObject}
     *
     * @return a new {@link ImageObject}
     */
    public ImageObject createImageObject() {
        String name = IMAGE_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++imageCount), '0', 5);
        return new ImageObject(this, name);
    }

    /**
     * Creates an IOCA {@link IMImageObject}
     *
     * @return a new {@link IMImageObject}
     */
    public IMImageObject createIMImageObject() {
        String name = IM_IMAGE_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++imImageCount), '0', 3);
        return new IMImageObject(name);
    }

    /**
     * Creates a new GOCA {@link GraphicsObject}
     *
     * @return a new {@link GraphicsObject}
     */
    public GraphicsObject createGraphicsObject() {
        String name = GRAPHIC_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++graphicCount), '0', 5);
        return new GraphicsObject(this, name);
    }

    /**
     * Creates a new MO:DCA {@link ObjectContainer}
     *
     * @return a new {@link ObjectContainer}
     */
    public ObjectContainer createObjectContainer() {
        String name = OBJECT_CONTAINER_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++objectContainerCount), '0', 6);
        return new ObjectContainer(this, name);
    }

    /**
     * Creates a new MO:DCA {@link ResourceObject}
     *
     * @param resourceName the resource object name
     * @return a new {@link ResourceObject}
     */
    public ResourceObject createResource(String resourceName) {
        return new ResourceObject(resourceName);
    }

    /**
     * Creates a new MO:DCA {@link ResourceObject}
     *
     * @return a new {@link ResourceObject}
     */
    public ResourceObject createResource() {
        String name = RESOURCE_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++resourceCount), '0', 5);
        return createResource(name);
    }

    /**
     * Creates a new MO:DCA {@link PageGroup}
     * @return a new {@link PageGroup}
     */
    public PageGroup createPageGroup() {
        String name = PAGE_GROUP_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++pageGroupCount), '0', 5);
        return new PageGroup(this, name);
    }

    /**
     * Creates a new MO:DCA {@link ActiveEnvironmentGroup}
     *
     * @param width the page width
     * @param height the page height
     * @param widthRes the page width resolution
     * @param heightRes the page height resolution
     * @return a new {@link ActiveEnvironmentGroup}
     */
    public ActiveEnvironmentGroup createActiveEnvironmentGroup(
            int width, int height, int widthRes, int heightRes) {
        String name = ACTIVE_ENVIRONMENT_GROUP_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++activeEnvironmentGroupCount), '0', 5);
        return new ActiveEnvironmentGroup(this, name, width, height, widthRes, heightRes);
    }

    /**
     * Creates a new MO:DCA {@link ResourceGroup}
     *
     * @return a new {@link ResourceGroup}
     */
    public ResourceGroup createResourceGroup() {
        String name = RESOURCE_GROUP_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++resourceGroupCount), '0', 6);
        return new ResourceGroup(name);
    }

    /**
     * Creates a new MO:DCA {@link StreamedResourceGroup}
     *
     * @param os the outputstream of the streamed resource group
     * @return a new {@link StreamedResourceGroup}
     */
    public StreamedResourceGroup createStreamedResourceGroup(OutputStream os) {
        String name = RESOURCE_GROUP_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++resourceGroupCount), '0', 6);
        return new StreamedResourceGroup(name, os);
    }

    /**
     * Creates a new MO:DCA {@link PageObject}.
     *
     * @param pageWidth
     *            the width of the page
     * @param pageHeight
     *            the height of the page
     * @param pageRotation
     *            the rotation of the page
     * @param pageWidthRes
     *            the width resolution of the page
     * @param pageHeightRes
     *            the height resolution of the page
     *
     * @return a new {@link PageObject}
     */
    public PageObject createPage(int pageWidth, int pageHeight, int pageRotation,
            int pageWidthRes, int pageHeightRes) {
        String pageName = PAGE_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++pageCount), '0', 5);
        return new PageObject(this, pageName, pageWidth, pageHeight,
            pageRotation, pageWidthRes, pageHeightRes);
    }


    /**
     * Creates a new MO:DCA {@link PresentationTextObject}.
     *
     * @return a new {@link PresentationTextObject}
     */
    public PresentationTextObject createPresentationTextObject() {
        String textObjectName = PRESENTATION_TEXT_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++textObjectCount), '0', 6);
        return new PresentationTextObject(textObjectName);
    }


    /**
     * Creates a new MO:DCA {@link Overlay}.
     *
     * @param width
     *            the width of the overlay
     * @param height
     *            the height of the overlay
     * @param widthRes
     *            the width resolution of the overlay
     * @param heightRes
     *            the height resolution of the overlay
     * @param overlayRotation
     *            the rotation of the overlay
     *
     * @return a new {@link Overlay}.
     */
    public Overlay createOverlay(int width, int height,
            int widthRes, int heightRes, int overlayRotation) {
        String overlayName = OVERLAY_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++overlayCount), '0', 5);
        return new Overlay(this, overlayName, width, height,
                overlayRotation, widthRes, heightRes);
    }

    /**
     * Creates a MO:DCA {@link Document}
     *
     * @return a new {@link Document}
     */
    public Document createDocument() {
        String documentName = DOCUMENT_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++documentCount), '0', 5);
        return new Document(this, documentName);
    }

    /**
     * Creates a MO:DCA {@link MapCodedFont}
     *
     * @return a new {@link MapCodedFont}
     */
    public MapCodedFont createMapCodedFont() {
        return new MapCodedFont();
    }

    /**
     * Creates a MO:DCA {@link IncludePageSegment}
     *
     * @param name the page segment name
     * @param x the x coordinate
     * @param y the y coordinate
     *
     * @return a new {@link IncludePageSegment}
     */
    public IncludePageSegment createIncludePageSegment(String name, int x, int y) {
        return new IncludePageSegment(name, x, y);
    }

    /**
     * Creates a MO:DCA {@link IncludeObject}
     *
     * @param name the name of this include object
     * @return a new {@link IncludeObject}
     */
    public IncludeObject createInclude(String name) {
        return new IncludeObject(name);
    }

    /**
     * Creates a MO:DCA {@link TagLogicalElement}
     *
     * @param state the attribute state for the TLE
     * @return a new {@link TagLogicalElement}
     */
    public TagLogicalElement createTagLogicalElement(TagLogicalElement.State state) {
        return new TagLogicalElement(state);
    }

    /**
     * Creates a new {@link DataStream}
     *
     * @param paintingState the AFP painting state
     * @param outputStream an outputstream to write to
     * @return a new {@link DataStream}
     */
    public DataStream createDataStream(AFPPaintingState paintingState, OutputStream outputStream) {
        return new DataStream(this, paintingState, outputStream);
    }

    /**
     * Creates a new MO:DCA {@link PageDescriptor}
     *
     * @param width the page width.
     * @param height the page height.
     * @param widthRes the page width resolution.
     * @param heightRes the page height resolution.
     * @return a new {@link PageDescriptor}
     */
    public PageDescriptor createPageDescriptor(int width, int height, int widthRes, int heightRes) {
        return new PageDescriptor(width, height, widthRes, heightRes);
    }

    /**
     * Returns a new MO:DCA {@link ObjectEnvironmentGroup}
     *
     * @return a new {@link ObjectEnvironmentGroup}
     */
    public ObjectEnvironmentGroup createObjectEnvironmentGroup() {
        String oegName = OBJECT_ENVIRONMENT_GROUP_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++objectEnvironmentGroupCount), '0', 5);
        return new ObjectEnvironmentGroup(oegName);
    }

    /**
     * Creates a new GOCA {@link GraphicsData}
     *
     * @return a new {@link GraphicsData}
     */
    public GraphicsData createGraphicsData() {
        return new GraphicsData();
    }

    /**
     * Creates a new {@link ObjectAreaDescriptor}
     *
     * @param width the object width.
     * @param height the object height.
     * @param widthRes the object width resolution.
     * @param heightRes the object height resolution.
     * @return a new {@link ObjectAreaDescriptor}
     */
    public ObjectAreaDescriptor createObjectAreaDescriptor(
            int width, int height, int widthRes, int heightRes) {
        return new ObjectAreaDescriptor(width, height, widthRes, heightRes);
    }

    /**
     * Creates a new {@link ObjectAreaPosition}
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param rotation the coordinate system rotation (must be 0, 90, 180, 270).
     * @return a new {@link ObjectAreaPosition}
     */
    public ObjectAreaPosition createObjectAreaPosition(int x, int y,
            int rotation) {
        return new ObjectAreaPosition(
                x, y, rotation);
    }

    /**
     * Creates a new {@link ImageDataDescriptor}
     *
     * @param width the image width
     * @param height the image height
     * @param widthRes the x resolution of the image
     * @param heightRes the y resolution of the image
     * @return a new {@link ImageDataDescriptor}
     */
    public ImageDataDescriptor createImageDataDescriptor(
            int width, int height, int widthRes, int heightRes) {
        return new ImageDataDescriptor(
                width, height, widthRes, heightRes);
    }

    /**
     * Creates a new GOCA {@link GraphicsDataDescriptor}
     *
     * @param xlwind the left edge of the graphics window
     * @param xrwind the right edge of the graphics window
     * @param ybwind the top edge of the graphics window
     * @param ytwind the bottom edge of the graphics window
     * @param widthRes the x resolution of the graphics window
     * @param heightRes the y resolution of the graphics window
     * @return a new {@link GraphicsDataDescriptor}
     */
    public GraphicsDataDescriptor createGraphicsDataDescriptor(
            int xlwind, int xrwind, int ybwind, int ytwind, int widthRes, int heightRes) {
        return new GraphicsDataDescriptor(
                xlwind, xrwind, ybwind, ytwind, widthRes, heightRes);
    }

    /**
     * Creates a new MO:DCA {@link ContainerDataDescriptor}
     *
     * @param dataWidth the container data width
     * @param dataHeight the container data height
     * @param widthRes the container data width resolution
     * @param heightRes the container data height resolution
     * @return a new {@link ContainerDataDescriptor}
     */
    public ContainerDataDescriptor createContainerDataDescriptor(
            int dataWidth, int dataHeight, int widthRes, int heightRes) {
        return new ContainerDataDescriptor(dataWidth, dataHeight, widthRes, heightRes);
    }

    /**
     * Creates a new MO:DCA {@link MapContainerData}
     *
     * @param optionValue the option value
     * @return a new {@link MapContainerData}
     */
    public MapContainerData createMapContainerData(byte optionValue) {
        return new MapContainerData(optionValue);
    }

    /**
     * Creates a new MO:DCA {@link MapDataResource}
     *
     * @return a new {@link MapDataResource}
     */
    public MapDataResource createMapDataResource() {
        return new MapDataResource();
    }

    /**
     * Creates a new PTOCA {@link PresentationTextDescriptor}
     * @param width presentation width
     * @param height presentation height
     * @param widthRes resolution of presentation width
     * @param heightRes resolution of presentation height
     * @return a new {@link PresentationTextDescriptor}
     */
    public PresentationTextDescriptor createPresentationTextDataDescriptor(
            int width, int height, int widthRes, int heightRes) {
        return new PresentationTextDescriptor(width, height,
                widthRes, heightRes);
    }

    /**
     * Creates a new MO:DCA {@link PresentationEnvironmentControl}
     *
     * @return a new {@link PresentationEnvironmentControl}
     */
    public PresentationEnvironmentControl createPresentationEnvironmentControl() {
        return new PresentationEnvironmentControl();
    }

    /**
     * Creates a new MO:DCA {@link InvokeMediumMap}
     *
     * @param name the object name
     * @return a new {@link InvokeMediumMap}
     */
    public InvokeMediumMap createInvokeMediumMap(String name) {
        return new InvokeMediumMap(name);
    }

    /**
     * Creates a new MO:DCA {@link ResourceEnvironmentGroup}
     *
     * @return a new {@link ResourceEnvironmentGroup}
     */
    public ResourceEnvironmentGroup createResourceEnvironmentGroup() {
        return new ResourceEnvironmentGroup();
    }

    /**
     * Creates a new IOCA {@link ImageSegment}
     *
     * @return a new {@link ImageSegment}
     */
    public ImageSegment createImageSegment() {
        String name = IMAGE_SEGMENT_NAME_PREFIX
        + StringUtils.lpad(String.valueOf(++imageSegmentCount), '0', 2);
        return new ImageSegment(this, name);
    }

    /**
     * Creates an new IOCA {@link ImageContent}
     *
     * @return an {@link ImageContent}
     */
    public ImageContent createImageContent() {
        return new ImageContent();
    }

    /**
     * Creates a new IOCA {@link ImageRasterData}
     *
     * @param rasterData raster data
     * @return a new {@link ImageRasterData}
     */
    public ImageRasterData createImageRasterData(byte[] rasterData) {
        return new ImageRasterData(rasterData);
    }

    /**
     * Creates an new IOCA {@link ImageSizeParameter}.
     *
     * @param hsize The horizontal size of the image.
     * @param vsize The vertical size of the image.
     * @param hresol The horizontal resolution of the image.
     * @param vresol The vertical resolution of the image.
     * @return a new {@link ImageSizeParameter}
     */
    public ImageSizeParameter createImageSizeParameter(int hsize, int vsize,
            int hresol, int vresol) {
        return new ImageSizeParameter(hsize, vsize, hresol, vresol);
    }

    public TileTOC createTileTOC() {
        return new TileTOC();
    }

    public TileSize createTileSize(int dataWidth, int dataHeight, int dataWidthRes, int dataHeightRes) {
        return new TileSize(dataWidth, dataHeight, dataWidthRes, dataHeightRes);
    }

    public TilePosition createTilePosition() {
        return new TilePosition();
    }

    public Tile createTile() {
        return new Tile();
    }

    public BandImage createBandImage() {
        return new BandImage();
    }
}
