/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.configuration;

// Java
import java.net.URL;
import java.util.List;

// Fop
import org.apache.fop.apps.FOPException;
import org.apache.fop.tools.URLBuilder;

/**
 * FontInfo contains meta information on fonts (where is the metrics file etc.)
 */

public class FontInfo {

    private String metricsFile, embedFile, name;
    private boolean kerning;
    private List fontTriplets;

    public FontInfo(String name, String metricsFile, boolean kerning,
                    List fontTriplets, String embedFile) {
        this.name = name;
        this.metricsFile = metricsFile;
        this.embedFile = embedFile;
        this.kerning = kerning;
        this.fontTriplets = fontTriplets;
    }

    /**
     * @return the URL to the metrics file
     */
    public URL getMetricsFile() throws FOPException {
        try {
            return URLBuilder.buildURL(Configuration.getFontBaseURL(), metricsFile);
        } catch (Exception e) {
            throw new FOPException("Invalid font metrics file: " + metricsFile 
                    + " (" + e.getMessage() + ")");
        }
    }

    /**
     * @return the url to the font
     */
    public URL getEmbedFile() throws FOPException {
        // check if it's a URL and convert it to a filename
        if (embedFile == null) return null;
        try {
            return URLBuilder.buildURL(Configuration.getFontBaseURL(), embedFile);
        } catch (Exception e) {
            throw new FOPException("Invalid font file (embedFile): " + embedFile 
                    + " (" + e.getMessage() + ")");
        }
    }

    public boolean getKerning() {
        return kerning;
    }

    public List getFontTriplets() {
        return fontTriplets;
    }

}

