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

import org.apache.fop.render.afp.DataObjectInfo;
import org.apache.fop.render.afp.ImageObjectInfo;
import org.apache.fop.render.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.render.afp.tools.StringUtils;
import org.apache.xmlgraphics.image.codec.tiff.TIFFImage;

/**
 * Creator of MO;DCA data objects
 */
public class DataObjectFactory {
    private static final String IMAGE_NAME_PREFIX = "IMG";
    private static final String GRAPHIC_NAME_PREFIX = "GRA";
//    private static final String BARCODE_NAME_PREFIX = "BAR";
//    private static final String OTHER_NAME_PREFIX = "OTH";

    private int imageCount = 0;
    private int graphicCount = 0;
    
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
     *            
     * TODO: move this method somewhere appropriate in commons
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
     * @param info the image object info
     * @return a newly created image object
     */
    protected ImageObject createImage(ImageObjectInfo info) {
        String name = IMAGE_NAME_PREFIX
                + StringUtils.lpad(String.valueOf(++imageCount), '0', 5);
        ImageObject imageObj = new ImageObject(name);
        if (info.hasCompression()) {
            int compression = info.getCompression();
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
        imageObj.setImageParameters(info.getWidthRes(), info.getHeightRes(), 
                info.getDataWidth(), info.getDataHeight());
        if (info.isColor()) {
            imageObj.setImageIDESize((byte)24);
            imageObj.setImageData(info.getData());
        } else {
            convertToGrayScaleImage(imageObj, info.getData(),
                    info.getDataWidth(), info.getDataHeight(),
                    info.getBitsPerPixel());
        }
        return imageObj;
    }
    
    /**
     * Helper method to create a graphic in the current container and to return
     * the object.
     * @param info the data object info
     * @return a newly created graphics object
     */
    protected GraphicsObject createGraphic(DataObjectInfo info) {
        String name = GRAPHIC_NAME_PREFIX
            + StringUtils.lpad(String.valueOf(++graphicCount), '0', 5);
        GraphicsObject graphicsObj = new GraphicsObject(name);
        return graphicsObj;
    }

    /**
     * Creates and returns a new data object
     * @param dataObjectInfo the data object info
     * @return a newly created data object
     */
    public AbstractDataObject create(DataObjectInfo dataObjectInfo) {
        AbstractDataObject dataObject;
        if (dataObjectInfo instanceof ImageObjectInfo) {
            dataObject = createImage((ImageObjectInfo)dataObjectInfo);
        } else {
            dataObject = createGraphic(dataObjectInfo);
        }
        dataObject.setViewport(dataObjectInfo.getX(), dataObjectInfo.getY(),
                dataObjectInfo.getWidth(), dataObjectInfo.getHeight(),
                dataObjectInfo.getWidthRes(), dataObjectInfo.getHeightRes(),
                dataObjectInfo.getRotation());

        dataObject.setFullyQualifiedName(
            FullyQualifiedNameTriplet.TYPE_DATA_OBJECT_INTERNAL_RESOURCE_REF,
            FullyQualifiedNameTriplet.FORMAT_CHARSTR, dataObject.getName());

        return dataObject;
    }
}
