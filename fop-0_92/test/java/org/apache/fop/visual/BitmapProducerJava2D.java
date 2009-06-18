/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * BitmapProducer implementation that uses the Java2DRenderer to create bitmaps.
 * <p>
 * Here's what the configuration element looks like for the class:
 * <p>
 * <pre>
 * <producer classname="org.apache.fop.visual.BitmapProducerJava2D">
 *   <delete-temp-files>false</delete-temp-files>
 * </producer>
 * </pre>
 * <p>
 * The "delete-temp-files" element is optional and defaults to true.
 */
public class BitmapProducerJava2D extends AbstractBitmapProducer implements Configurable {

    // configure fopFactory as desired
    private FopFactory fopFactory = FopFactory.newInstance();
    
    private boolean deleteTempFiles;

    /** @see org.apache.avalon.framework.configuration.Configurable */
    public void configure(Configuration cfg) throws ConfigurationException {
        this.deleteTempFiles = cfg.getChild("delete-temp-files").getValueAsBoolean(true);
    }

    /** @see org.apache.fop.visual.BitmapProducer */
    public BufferedImage produce(File src, ProducerContext context) {
        try {
            FOUserAgent userAgent = fopFactory.newFOUserAgent();
            userAgent.setTargetResolution(context.getTargetResolution());
            userAgent.setBaseURL(src.getParentFile().toURL().toString());
            
            File outputFile = new File(context.getTargetDir(), src.getName() + ".java2d.png");
            OutputStream out = new FileOutputStream(outputFile);
            out = new BufferedOutputStream(out);
            try {
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PNG, userAgent, out);
                SAXResult res = new SAXResult(fop.getDefaultHandler());
                
                Transformer transformer = getTransformer(context);
                transformer.transform(new StreamSource(src), res);
            } finally {
                IOUtils.closeQuietly(out);
            }
            
            BufferedImage img = BitmapComparator.getImage(outputFile);
            if (deleteTempFiles) {
                if (!outputFile.delete()) {
                    log.warn("Cannot delete " + outputFile);
                    outputFile.deleteOnExit();
                }
            }
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
            return null;
        }
    }

}
