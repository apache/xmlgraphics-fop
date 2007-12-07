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

package org.apache.fop.image2.impl.imageio;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.w3c.dom.Element;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSessionContext;
import org.apache.fop.image2.impl.AbstractImageLoader;
import org.apache.fop.image2.impl.ImageBuffered;
import org.apache.fop.image2.impl.ImageRendered;
import org.apache.fop.image2.impl.PreloaderPNG;
import org.apache.fop.image2.util.ImageUtil;

/**
 * An ImageLoader implementation based on ImageIO for loading bitmap images.
 */
public class ImageLoaderImageIO extends AbstractImageLoader {

    private ImageFlavor targetFlavor;

    /**
     * Main constructor.
     * @param targetFlavor the target flavor
     */
    public ImageLoaderImageIO(ImageFlavor targetFlavor) {
        if (!(ImageFlavor.BUFFERED_IMAGE.equals(targetFlavor)
                || ImageFlavor.RENDERED_IMAGE.equals(targetFlavor))) {
            throw new IllegalArgumentException("Unsupported target ImageFlavor: " + targetFlavor);
        }
        this.targetFlavor = targetFlavor;
    }
    
    /** {@inheritDoc} */
    public ImageFlavor getTargetFlavor() {
        return this.targetFlavor;
    }

    /** {@inheritDoc} */
    public Image loadImage(ImageInfo info, Map hints, ImageSessionContext session)
            throws ImageException, IOException {
        Source src = session.needSource(info.getOriginalURI());
        ImageInputStream imgStream = ImageUtil.needImageInputStream(src);
        Iterator iter = ImageIO.getImageReaders(imgStream);
        if (!iter.hasNext()) {
            throw new ImageException("No ImageIO ImageReader found .");
        }

        IIOMetadata iiometa = (IIOMetadata)info.getCustomObjects().get(
                PreloaderPNG.IMAGEIO_METADATA);
        boolean ignoreMetadata = (iiometa != null);
        
        ImageReader reader = (ImageReader)iter.next();
        ImageReadParam param = reader.getDefaultReadParam();
        reader.setInput(imgStream, true, ignoreMetadata);
        final int pageIndex = 0; //Always the first page at the moment
        BufferedImage imageData = reader.read(pageIndex, param);
        if (iiometa == null) {
            iiometa = reader.getImageMetadata(pageIndex);
        }
        
        ColorModel cm = imageData.getColorModel();

        Color transparentColor = null;
        if (cm instanceof IndexColorModel) {
            //transparent color will be extracted later from the image
        } else {
            //ImageIOUtil.dumpMetadataToSystemOut(iiometa);
            //Retrieve the transparent color from the metadata
            if (iiometa != null && iiometa.isStandardMetadataFormatSupported()) {
                Element metanode = (Element)iiometa.getAsTree(
                        IIOMetadataFormatImpl.standardMetadataFormatName);
                Element dim = ImageIOUtil.getChild(metanode, "Transparency");
                if (dim != null) {
                    Element child;
                    child = ImageIOUtil.getChild(dim, "TransparentColor");
                    if (child != null) {
                        String value = child.getAttribute("value");
                        if (value == null || value.length() == 0) {
                            //ignore
                        } else if (cm.getNumColorComponents() == 1) {
                            int gray = Integer.parseInt(value);
                            transparentColor = new Color(gray, gray, gray);
                        } else {
                            StringTokenizer st = new StringTokenizer(value);
                            transparentColor = new Color(
                                    Integer.parseInt(st.nextToken()),
                                    Integer.parseInt(st.nextToken()),
                                    Integer.parseInt(st.nextToken()));
                        }
                    }
                }
            }
        }
        
        if (ImageFlavor.BUFFERED_IMAGE.equals(this.targetFlavor)) {
            return new ImageBuffered(info, imageData, transparentColor);
        } else {
            return new ImageRendered(info, imageData, transparentColor);
        }
    }


}
