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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.image2.ImageSessionContext;
import org.apache.fop.image2.ImageSource;
import org.apache.fop.image2.util.ImageUtil;
import org.apache.fop.image2.util.SoftMapCache;

/**
 * Abstract base class for classes implementing ImageSessionContext. This class provides all the
 * special treatment for Source creation, i.e. it provides optimized Source objects where possible.
 */
public abstract class AbstractImageSessionContext implements ImageSessionContext {

    /** logger */
    private static Log log = LogFactory.getLog(AbstractImageSessionContext.class);

    /**
     * Attempts to resolve the given URI.
     * @param uri URI to access
     * @return A {@link javax.xml.transform.Source} object, or null if the URI
     * cannot be resolved. 
     */
    protected abstract Source resolveURI(String uri);

    /** {@inheritDoc} */
    public Source newSource(String uri) {
        Source source = resolveURI(uri);
        if (source == null) {
            if (log.isDebugEnabled()) {
                log.debug("URI could not be resolved: " + uri);
            }
            return null;
        }
        if (!(source instanceof StreamSource)) {
            //Return any non-stream Sources and let the ImageLoaders deal with them
            return source;
        }
        
        ImageSource imageSource = null;
        
        String resolvedURI = source.getSystemId();
        URL url;
        try {
            url = new URL(resolvedURI);
        } catch (MalformedURLException e) {
            url = null;
        } 
        File f = FileUtils.toFile(url);
        if (f != null) {
            boolean directFileAccess = true;
            InputStream in = null;
            if (source instanceof StreamSource) {
                StreamSource streamSource = (StreamSource)source; 
                in = streamSource.getInputStream();
                in = ImageUtil.decorateMarkSupported(in);
                try {
                    if (ImageUtil.isGZIPCompressed(in)) {
                        //GZIPped stream are not seekable, so buffer/cache like other URLs
                        directFileAccess = false;
                    }
                } catch (IOException ioe) {
                    log.error("Error while checking the InputStream for GZIP compression."
                            + " Could not load image from system identifier '"
                            + source.getSystemId() + "' (" + ioe.getMessage() + ")");
                    return null;
                }
            }
            
            if (directFileAccess) {
                //Close as the file is reopened in a more optimal way
                IOUtils.closeQuietly(in);
                try {
                    //We let the OS' file system cache do the caching for us
                    //--> lower Java memory consumption, probably no speed loss
                    imageSource = new ImageSource(ImageIO.createImageInputStream(f),
                            resolvedURI, true);
                } catch (IOException ioe) {
                    log.error("Unable to create ImageInputStream for local file"
                            + " from system identifier '"
                            + source.getSystemId() + "' (" + ioe.getMessage() + ")");
                }
            }
        }
        
        if (imageSource == null) {
            // Got a valid source, obtain an InputStream from it
            InputStream in = null;
            if (source instanceof StreamSource) {
                StreamSource ssrc = (StreamSource)source;
                if (ssrc.getReader() != null && ssrc.getInputStream() == null) {
                    //We don't handle Reader instances here so return the Source unchanged
                    return ssrc;
                }
                in = ssrc.getInputStream();
            }
            if (in == null && url != null) {
                try {
                    in = url.openStream();
                } catch (Exception ex) {
                    log.error("Unable to obtain stream from system identifier '" 
                        + source.getSystemId() + "'");
                }
            }
            if (in == null) {
                log.error("The Source that was returned from URI resolution didn't contain"
                        + " an InputStream for URI: " + uri);
                return null;
            }

            try {
                //Buffer and uncompress if necessary
                in = ImageUtil.autoDecorateInputStream(in);
                imageSource = new ImageSource(
                        ImageIO.createImageInputStream(in), source.getSystemId(), false);
            } catch (IOException ioe) {
                log.error("Unable to create ImageInputStream for InputStream"
                        + " from system identifier '"
                        + source.getSystemId() + "' (" + ioe.getMessage() + ")");
            }
        }
        return imageSource;
    }
    
    private SoftMapCache sessionSources = new SoftMapCache(false); //no need for synchronization
    
    /** {@inheritDoc} */
    public Source getSource(String uri) {
        return (Source)sessionSources.remove(uri);
    }
    
    /** {@inheritDoc} */
    public Source needSource(String uri) throws FileNotFoundException {
        Source src = getSource(uri);
        if (src == null) {
            if (log.isDebugEnabled()) {
                log.debug("Creating new Source for " + uri);
                
            }
            src = newSource(uri);
            if (src == null) {
                throw new FileNotFoundException("Image not found: " + uri);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Reusing Source for " + uri);
            }
        }
        return src;
    }
    
    /** {@inheritDoc} */
    public void returnSource(String uri, Source src) {
        //Safety check to make sure the Preloaders behave
        ImageInputStream in = ImageUtil.getImageInputStream(src);
        try {
            if (in != null && in.getStreamPosition() != 0) {
                throw new IllegalStateException("ImageInputStream is not reset for: " + uri);
            }
        } catch (IOException ioe) {
            //Ignore exception
            ImageUtil.closeQuietly(src);
        }
        
        if (isReusable(src)) {
            //Only return the Source if it's reusable
            log.debug("Returning Source for " + uri);
            sessionSources.put(uri, src);
        } else {
            //Otherwise, try to close if possible and forget about it
            ImageUtil.closeQuietly(src);
        }
    }
    
    /**
     * Indicates whether a Source is reusable. A Source object is reusable if it's an
     * {@link ImageSource} (containing an {@link ImageInputStream}) or a {@link DOMSource}.
     * @param src the Source object
     * @return true if the Source is reusable
     */
    protected boolean isReusable(Source src) {
        if (src instanceof ImageSource) {
            ImageSource is = (ImageSource)src;
            if (is.getImageInputStream() != null) {
                return true;
            }
        }
        if (src instanceof DOMSource) {
            return true;
        }
        return false;
    }
}
