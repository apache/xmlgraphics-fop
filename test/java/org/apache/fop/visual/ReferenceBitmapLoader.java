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

package org.apache.fop.visual;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * BitmapProducer implementation that simply loads preproduced reference bitmaps from a
 * certain directory.
 * <p>
 * Here's what the configuration element looks like for the class:
 * <p>
 * <pre>
 * <producer classname="org.apache.fop.visual.ReferenceBitmapLoader">
 *   <directory>C:/Temp/ref-bitmaps</directory>
 * </producer>
 * </pre>
 */
public class ReferenceBitmapLoader extends AbstractBitmapProducer implements Configurable {

    private File bitmapDirectory;

    public ReferenceBitmapLoader(URI baseUri) {
        super(baseUri);
    }

    /** @see org.apache.avalon.framework.configuration.Configurable */
    public void configure(Configuration cfg) throws ConfigurationException {
        this.bitmapDirectory = new File(cfg.getChild("directory").getValue(null));
        if (!bitmapDirectory.exists()) {
            throw new ConfigurationException("Directory could not be found: " + bitmapDirectory);
        }
    }

    /** @see org.apache.fop.visual.BitmapProducer */
    public BufferedImage produce(File src, int index, ProducerContext context) {
        try {
            File bitmap = new File(bitmapDirectory, src.getName() + ".png");
            if (bitmap.exists()) {
                return BitmapComparator.getImage(bitmap);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }

}
