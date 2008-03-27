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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.fop.render.afp.DataObjectParameters;
import org.apache.fop.render.afp.ImageObjectParameters;
import org.apache.fop.render.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.render.afp.tools.StringUtils;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImage;

/**
 * A Resource Group contains a set of overlays.
 */
public final class ResourceGroup extends AbstractNamedAFPObject {
    
    /**
     * Default name for the resource group
     */
    private static final String DEFAULT_NAME = "RG000001";

    /**
     * Mapping of resource uri to data resource object (image/graphic) 
     */
    private Map/*<String,AbstractAFPObject>*/ resourceMap = null;

    /**
     * Default constructor
     */
    public ResourceGroup() {
        this(DEFAULT_NAME);
    }

    /**
     * Constructor for the ResourceGroup, this takes a
     * name parameter which must be 8 characters long.
     * @param name the resource group name
     */
    public ResourceGroup(String name) {
        super(name);
    }

    private static final String IMAGE_NAME_PREFIX = "IMG";
    private static final String GRAPHIC_NAME_PREFIX = "GRA";
    private static final String PAGE_SEGMENT_NAME_PREFIX = "PAG";
    private static final String BARCODE_NAME_PREFIX = "BAR";
    private static final String OTHER_NAME_PREFIX = "OTH";
    
    /**
     * Converts a byte array containing 24 bit RGB image data to a grayscale
     * image.
     * 
     * @param io
     *            the target image object
     * @param raw
     *            the buffer containing the RGB image data
     * @param width
     *            the width of the image in pixels
     * @param height
     *            the height of the image in pixels
     * @param bitsPerPixel
     *            the number of bits to use per pixel
     */
    private static void convertToGrayScaleImage(ImageObject io, byte[] raw, int width,
            int height, int bitsPerPixel) {
        int pixelsPerByte = 8 / bitsPerPixel;
        int bytewidth = (width / pixelsPerByte);
        if ((width % pixelsPerByte) != 0) {
            bytewidth++;
        }
        byte[] bw = new byte[height * bytewidth];
        byte ib;
        for (int y = 0; y < height; y++) {
            ib = 0;
            int i = 3 * y * width;
            for (int x = 0; x < width; x++, i += 3) {

                // see http://www.jguru.com/faq/view.jsp?EID=221919
                double greyVal = 0.212671d * ((int) raw[i] & 0xff) + 0.715160d
                        * ((int) raw[i + 1] & 0xff) + 0.072169d
                        * ((int) raw[i + 2] & 0xff);
                switch (bitsPerPixel) {
                case 1:
                    if (greyVal < 128) {
                        ib |= (byte) (1 << (7 - (x % 8)));
                    }
                    break;
                case 4:
                    greyVal /= 16;
                    ib |= (byte) ((byte) greyVal << ((1 - (x % 2)) * 4));
                    break;
                case 8:
                    ib = (byte) greyVal;
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Unsupported bits per pixel: " + bitsPerPixel);
                }

                if ((x % pixelsPerByte) == (pixelsPerByte - 1)
                        || ((x + 1) == width)) {
                    bw[(y * bytewidth) + (x / pixelsPerByte)] = ib;
                    ib = 0;
                }
            }
        }
        io.setImageIDESize((byte) bitsPerPixel);
        io.setImageData(bw);
    }

    /**
     * Helper method to create an image on the current container and to return
     * the object.
     * @param params the set of image object parameters
     * @return a newly created image object
     */
    private ImageObject createImage(ImageObjectParameters params) {
        String name = IMAGE_NAME_PREFIX
                + StringUtils.lpad(String.valueOf(getResourceCount() + 1), '0', 5);
        ImageObject imageObj = new ImageObject(name);
        if (params.hasCompression()) {
            int compression = params.getCompression();
            switch (compression) {
            case TIFFImage.COMP_FAX_G3_1D:
                imageObj.setImageEncoding(ImageContent.COMPID_G3_MH);
                    break;
                case TIFFImage.COMP_FAX_G3_2D:
                    imageObj.setImageEncoding(ImageContent.COMPID_G3_MR);
                    break;
                case TIFFImage.COMP_FAX_G4_2D:
                    imageObj.setImageEncoding(ImageContent.COMPID_G3_MMR);
                    break;
                default:
                    throw new IllegalStateException(
                            "Invalid compression scheme: " + compression);
            }
        }
        imageObj.setFullyQualifiedName(
                FullyQualifiedNameTriplet.TYPE_BEGIN_RESOURCE_OBJECT_REF,
                FullyQualifiedNameTriplet.FORMAT_URL, params.getUri());
        imageObj.setImageParameters(params.getWidthRes(), params.getHeightRes(), 
                params.getImageDataWidth(), params.getImageDataHeight());
        if (params.isColor()) {
            imageObj.setImageIDESize((byte)24);
            imageObj.setImageData(params.getData());
        } else {
            convertToGrayScaleImage(imageObj, params.getData(),
                    params.getImageDataWidth(), params.getImageDataHeight(),
                    params.getBitsPerPixel());
        }
        return imageObj;
    }
    
    /**
     * Helper method to create a graphic in the current container and to return
     * the object.
     * @param params the data object parameters
     * @return a newly created graphics object
     */
    private GraphicsObject createGraphic(DataObjectParameters params) {
        String name = GRAPHIC_NAME_PREFIX
            + StringUtils.lpad(String.valueOf(getResourceCount() + 1), '0', 5);
        GraphicsObject graphicsObj = new GraphicsObject(name);
        return graphicsObj;
    }

    /**
     * Adds a data object to this resource group
     * @param params the data object parameters
     * @return an include object reference
     */
    public IncludeObject addObject(DataObjectParameters params) {
        ResourceObject resourceObj = (ResourceObject)getResourceMap().get(params.getUri());
        if (resourceObj == null) {
            AbstractDataObject dataObj;
            if (params instanceof ImageObjectParameters) {
                dataObj = createImage((ImageObjectParameters)params);
            } else {
                dataObj = createGraphic(params);
            }
            // TODO: AC - rotation?
            int rotation = 0;
            dataObj.setViewport(params.getX(), params.getY(),
                    params.getWidth(), params.getHeight(),
                    params.getWidthRes(), params.getHeightRes(), rotation);
            
            // Wrap the data object in a resource object
            resourceObj = new ResourceObject(dataObj.getName(), dataObj);
            getResourceMap().put(params.getUri(), resourceObj);
        }
        IncludeObject includeObj = new IncludeObject(resourceObj);
        //includeObj.setObjectAreaSize(params.getX(), params.getY());
        return includeObj;
    }
    
    /**
     * @return the number of resources contained in this resource group
     */
    public int getResourceCount() {
        if (resourceMap != null) {
           return resourceMap.size(); 
        }
        return 0;
    }
    
    /**
     * Returns true if the resource exists within this resource group,
     * false otherwise.
     * 
     * @param uri the uri of the resource
     * @return true if the resource exists within this resource group
     */
    public boolean resourceExists(String uri) {
        return getResourceMap().containsKey(uri);
    }
    
    /**
     * Returns the list of resources
     * @return the list of resources
     */
    public Map/*<String,AbstractAFPObject>*/ getResourceMap() {
        if (resourceMap == null) {
            resourceMap = new java.util.HashMap/*<String,AbstractAFPObject>*/();
        }
        return resourceMap;
    }

    /**
     * {@inheritDoc}
     */
    public void writeContent(OutputStream os) throws IOException {
        if (resourceMap != null) {
            super.writeObjects(resourceMap.values(), os);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA8; // Structured field id byte 2
        data[5] = (byte) 0xC6; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA9; // Structured field id byte 2
        data[5] = (byte) 0xC6; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return this.name + " " + this.resourceMap;
    }
}
