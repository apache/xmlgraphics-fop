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
 
package org.apache.fop.image2.impl;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.DSCParser;
import org.apache.xmlgraphics.ps.dsc.DSCParserConstants;
import org.apache.xmlgraphics.ps.dsc.events.DSCComment;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentBoundingBox;
import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSize;
import org.apache.fop.image2.util.ImageInputStreamAdapter;
import org.apache.fop.image2.util.ImageUtil;

/**
 * Image preloader for EPS images (Encapsulated PostScript).
 */
public class PreloaderEPS extends AbstractImagePreloader {

    /** Key for binary header object used in custom objects of the ImageInfo class. */
    public static final Object EPS_BINARY_HEADER = EPSBinaryFileHeader.class;
    /** Key for bounding box used in custom objects of the ImageInfo class. */
    public static final Object EPS_BOUNDING_BOX = Rectangle2D.class;
    
    /** {@inheritDoc} */
    public ImageInfo preloadImage(String uri, Source src, FOUserAgent userAgent)
            throws IOException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        ImageInputStream in = ImageUtil.needImageInputStream(src);
        in.mark();
        ByteOrder originalByteOrder = in.getByteOrder();
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        EPSBinaryFileHeader binaryHeader = null;
        try {
            long magic = in.readUnsignedInt();
            // Check if binary header
            boolean supported = false;
            if (magic == 0xC6D3D0C5L) {
                supported = true; //binary EPS

                binaryHeader = readBinaryFileHeader(in);
                in.reset();
                in.mark(); //Mark start of file again
                in.seek(binaryHeader.psStart);
                
            } else if (magic == 0x53502125L) { //"%!PS" in little endian
                supported = true; //ascii EPS
                in.reset();
                in.mark(); //Mark start of file again
            } else {
                in.reset();
            }
            
            if (supported) {
                ImageInfo info = new ImageInfo(uri, src, getMimeType());
                boolean success = determineSize(in, userAgent, info);
                in.reset(); //Need to go back to start of file
                if (!success) {
                    //No BoundingBox found, so probably no EPS
                    return null;
                }
                if (in.getStreamPosition() != 0) {
                    throw new IllegalStateException("Need to be at the start of the file here");
                }
                if (binaryHeader != null) {
                    info.getCustomObjects().put(EPS_BINARY_HEADER, binaryHeader);
                }
                return info;
            } else {
                return null;
            }
        } finally {
            in.setByteOrder(originalByteOrder);
        }
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_EPS;
    }

    private EPSBinaryFileHeader readBinaryFileHeader(ImageInputStream in) throws IOException {
        EPSBinaryFileHeader offsets = new EPSBinaryFileHeader();
        offsets.psStart = in.readUnsignedInt();
        offsets.psLength = in.readUnsignedInt();
        offsets.wmfStart = in.readUnsignedInt();
        offsets.wmfLength = in.readUnsignedInt();
        offsets.tiffStart = in.readUnsignedInt();
        offsets.tiffLength = in.readUnsignedInt();
        return offsets;
    }
    
    private boolean determineSize(ImageInputStream in, FOUserAgent userAgent, ImageInfo info)
            throws IOException {

        in.mark();
        try {
            Rectangle2D bbox = null;
            DSCParser parser;
            try {
                parser = new DSCParser(new ImageInputStreamAdapter(in));
                outerLoop:
                while (parser.hasNext()) {
                    DSCEvent event = parser.nextEvent();
                    switch (event.getEventType()) {
                    case DSCParserConstants.HEADER_COMMENT:
                    case DSCParserConstants.COMMENT:
                        //ignore
                        break;
                    case DSCParserConstants.DSC_COMMENT:
                        DSCComment comment = event.asDSCComment();
                        if (comment instanceof DSCCommentBoundingBox) {
                            DSCCommentBoundingBox bboxComment = (DSCCommentBoundingBox)comment;
                            if (DSCConstants.BBOX.equals(bboxComment.getName()) && bbox == null) {
                                bbox = (Rectangle2D)bboxComment.getBoundingBox().clone();
                                //BoundingBox is good but HiRes is better so continue
                            } else if (DSCConstants.HIRES_BBOX.equals(bboxComment.getName())) {
                                bbox = (Rectangle2D)bboxComment.getBoundingBox().clone();
                                //HiRefBBox is great so stop
                                break outerLoop;
                            }
                        }
                        break;
                    default:
                        //No more header so stop
                        break outerLoop;
                    }
                }
                if (bbox == null) {
                    return false;
                }
            } catch (DSCException e) {
                throw new IOException("Error while parsing EPS file: " + e.getMessage());
            }
            
            ImageSize size = new ImageSize();
            size.setSizeInMillipoints(
                    (int)Math.round(bbox.getWidth() * 1000),
                    (int)Math.round(bbox.getHeight() * 1000));
            size.setResolution(userAgent.getSourceResolution());
            size.calcPixelsFromSize();
            info.setSize(size);
            info.getCustomObjects().put(EPS_BOUNDING_BOX, bbox);
            return true;
        } finally {
            in.reset();
        }
    }

    /**
     * Holder class for various pointers to the contents of the EPS file.
     */
    public static class EPSBinaryFileHeader {
        
        private long psStart = 0;
        private long psLength = 0;
        private long wmfStart = 0;
        private long wmfLength = 0;
        private long tiffStart = 0;
        private long tiffLength = 0;

        /**
         * Returns the start offset of the PostScript section.
         * @return the start offset
         */
        public long getPSStart() {
            return psStart;
        }
        
        /**
         * Returns the length of the PostScript section.
         * @return the length of the PostScript section (in bytes)
         */
        public long getPSLength() {
            return psLength;
        }
        
        /**
         * Indicates whether the EPS has a WMF preview.
         * @return true if there is a WMF preview
         */
        public boolean hasWMFPreview() {
            return (wmfStart != 0);
        }
        
        /**
         * Returns the start offset of the WMF preview.
         * @return the start offset (or 0 if there's no WMF preview)
         */
        public long getWMFStart() {
            return wmfStart;
        }
        
        /**
         * Returns the length of the WMF preview.
         * @return the length of the WMF preview (in bytes)
         */
        public long getWMFLength() {
            return wmfLength;
        }
        
        /**
         * Indicates whether the EPS has a TIFF preview.
         * @return true if there is a TIFF preview
         */
        public boolean hasTIFFPreview() {
            return (tiffStart != 0);
        }
        
        /**
         * Returns the start offset of the TIFF preview.
         * @return the start offset (or 0 if there's no TIFF preview)
         */
        public long getTIFFStart() {
            return tiffStart;
        }
        
        /**
         * Returns the length of the TIFF preview.
         * @return the length of the TIFF preview (in bytes)
         */
        public long getTIFFLength() {
            return tiffLength;
        }
        
    }
    
}
