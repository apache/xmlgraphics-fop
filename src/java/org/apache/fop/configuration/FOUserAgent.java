/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.fop.configuration;

// Java
import java.util.Map;
import java.util.logging.Logger;
import java.io.InputStream;


/**
 * The User Agent for fo.
 * This user agent is used by the processing to obtain user configurable
 * options.
 * <p>
 * Renderer specific extensions (that do not produce normal areas on
 * the output) will be done like so:
 * <br>
 * The extension will create an area, custom if necessary
 * <br>
 * this area will be added to the user agent with a key
 * <br>
 * the renderer will know keys for particular extensions
 * <br>
 * eg. bookmarks will be held in a special hierarchical area representing
 * the title and bookmark structure
 * <br>
 * These areas may contain resolveable areas that will be processed
 * with other resolveable areas
 */
public class FOUserAgent {

    private Logger log;
    /** Map containing various default values */
    public Map defaults = new java.util.HashMap();
    /** Map containing XML handlers for various document types */
    public Map handlers = new java.util.HashMap();
    private String baseURL;
    private float px2mm = 0.35277777777777777778f; //72dpi (=25.4/dpi)

    /**
     * Sets the logger.
     * @param log Logger to use
     */
    public void enableLogging(Logger log) {
        this.log = log;
    }

    /**
     * Returns the logger to use.
     * (todo) This breaks IoC/SoC. Should be improved.
     */
    public Logger getLogger() {
        return this.log;
    }

    /**
     * Sets the base URL.
     * @param baseURL base URL
     */
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * Returns the base URL.
     * @return the base URL
     */
    public String getBaseURL() {
        if ((this.baseURL == null) || (this.baseURL.trim().equals(""))) {
            return "file:.";
        } else {
            return this.baseURL;
        }
    }


    /**
     * Get an input stream for a reference.
     * Temporary solution until API better.
     * @param uri URI to access
     * @return InputStream for accessing the resource.
     */
    public InputStream getStream(String uri) {
        return null;
    }

    /**
     * Returns the conversion factor from pixel units to millimeters. This
     * depends on the desired reolution.
     * @return float conversion factor
     */
    public float getPixelUnitToMillimeter() {
        return this.px2mm;
    }

    /**
     * Sets the resolution in dpi.
     * @param dpi resolution in dpi
     */
    public void setResolution(int dpi) {
        this.px2mm = (float)(25.4 / dpi);
    }

    /**
     * If to create hot links to footnotes and before floats.
     * @return True if hot links dhould be created
     */
    public boolean linkToFootnotes() {
        return true;
    }

}

