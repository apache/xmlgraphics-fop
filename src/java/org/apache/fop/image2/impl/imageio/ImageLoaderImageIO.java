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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageFlavor;
import org.apache.fop.image2.ImageInfo;
import org.apache.fop.image2.ImageSessionContext;
import org.apache.fop.image2.impl.AbstractImageLoader;
import org.apache.fop.image2.impl.ImageBuffered;
import org.apache.fop.image2.impl.ImageRendered;
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
        ImageReader reader = (ImageReader)iter.next();
        ImageReadParam param = reader.getDefaultReadParam();
        reader.setInput(imgStream, true, true); //Don't need metadata anymore at this stage
        BufferedImage imageData = reader.read(0, param);
        
        if (ImageFlavor.BUFFERED_IMAGE.equals(this.targetFlavor)) {
            return new ImageBuffered(info, imageData);
        } else {
            return new ImageRendered(info, imageData);
        }
    }


}
