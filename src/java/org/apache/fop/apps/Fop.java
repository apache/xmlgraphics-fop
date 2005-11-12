/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

package org.apache.fop.apps;

// Java
import java.io.OutputStream;

// XML
import org.xml.sax.helpers.DefaultHandler;

// FOP
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOTreeBuilder;

/**
 * Primary class that activates the FOP process for embedded usage.
 * <P>
 * JAXP is the standard method of embedding FOP in Java programs.
 * Please check our 
 * <a href="http://xmlgraphics.apache.org/fop/trunk/embedding.html">embedding page</a>
 * for samples (these are also available within the distribution in 
 * FOP_DIR\examples\embedding)
 * <P>
 * Methods within FOUserAgent are available to customize portions of the
 * process.  For example, a specific Renderer object can be specified, 
 * also ElementMappings which determine elements in the FO that can be
 * processed) can be added.
 * <P>
 * At the moment, it is recommended not to reuse an instance of this
 * class for more than one rendering run.
 */
public class Fop implements Constants {

    // desired output type: RENDER_PDF, RENDER_PS, etc.
    //private int renderType = NOT_SET;
    
    // desired output format: MIME type such as "application/pdf", "application/postscript" etc.
    private String outputFormat = null;

    // output stream to send results to
    private OutputStream stream = null;

    // FOUserAgent object to set processing options
    private FOUserAgent foUserAgent = null;

    // FOTreeBuilder object to maintain reference for access to results
    private FOTreeBuilder foTreeBuilder = null;

    /**
     * Constructor for use with already-created FOUserAgents. It uses MIME types to select the 
     * output format (ex. "application/pdf" for PDF).
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param ua FOUserAgent object
     * @throws IllegalArgumentException if an unsupported renderer type was requested.
     */
    public Fop(String outputFormat, FOUserAgent ua) {
        this.outputFormat = outputFormat;

        foUserAgent = ua;
        if (foUserAgent == null) {
            foUserAgent = new FOUserAgent();
        }
    }

    /**
     * Constructor for FOP with a default FOUserAgent. It uses MIME types to select the 
     * output format (ex. "application/pdf" for PDF).
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @throws IllegalArgumentException if an unsupported renderer type was requested.
     */
    public Fop(String outputFormat) {
        this(outputFormat, null);
    }

    /**
     * Constructor for use with already-created FOUserAgents
     * @param renderType the type of renderer to use.  Must be one of
     * <ul>
     * <li>Fop.RENDER_PDF</li>
     * <li>Fop.RENDER_AWT</li>
     * <li>Fop.RENDER_PRINT</li>
     * <li>Fop.RENDER_MIF</li>
     * <li>Fop.RENDER_XML</li>
     * <li>Fop.RENDER_PCL</li>
     * <li>Fop.RENDER_PS</li>
     * <li>Fop.RENDER_TXT</li>
     * <li>Fop.RENDER_SVG</li>
     * <li>Fop.RENDER_RTF</li>
     * <li>Fop.RENDER_TIFF</li>
     * <li>Fop.RENDER_PNG</li>
     * </ul>
     * @param ua FOUserAgent object
     * @throws IllegalArgumentException if an unsupported renderer type was requested.
     * @deprecated Use {@link org.apache.fop.apps.Fop#Fop(java.lang.String, FOUserAgent)} instead!
     *             This constructor will be removed.
     */
    public Fop(int renderType, FOUserAgent ua) {
        this(getMimeTypeForRenderType(renderType), ua);
    }

    /**
     * Constructor that creates a default FOUserAgent
     * @see org.apache.fop.apps.Fop#Fop(int, FOUserAgent)
     * @deprecated Use {@link org.apache.fop.apps.Fop#Fop(java.lang.String)} instead!
     *             This constructor will be removed.
     */
    public Fop(int renderType) {
        this(renderType, null);
    }

    private static String getMimeTypeForRenderType(int renderType) {
        switch(renderType) {
        case Constants.RENDER_PDF: return MimeConstants.MIME_PDF;
        case Constants.RENDER_PS: return MimeConstants.MIME_POSTSCRIPT;
        case Constants.RENDER_PCL: return MimeConstants.MIME_PCL;
        case Constants.RENDER_MIF: return MimeConstants.MIME_MIF;
        case Constants.RENDER_RTF: return MimeConstants.MIME_RTF;
        case Constants.RENDER_SVG: return MimeConstants.MIME_SVG;
        case Constants.RENDER_TXT: return MimeConstants.MIME_PLAIN_TEXT;

        //Bitmap formats
        case Constants.RENDER_PNG: return MimeConstants.MIME_PNG;
        case Constants.RENDER_TIFF: return MimeConstants.MIME_TIFF;
        
        //Area tree XML: FOP-specific
        case Constants.RENDER_XML: return MimeConstants.MIME_FOP_AREA_TREE;
        
        //Non-standard pseudo MIME types
        case Constants.RENDER_AWT: return MimeConstants.MIME_FOP_AWT_PREVIEW;
        case Constants.RENDER_PRINT: return MimeConstants.MIME_FOP_PRINT;
        default:
            throw new IllegalArgumentException("Illegal renderType value: " + renderType);
        }
    }
    
    /**
     * Get the FOUserAgent instance for this process
     * @return the user agent
     */
    public FOUserAgent getUserAgent() {
        return foUserAgent;
    }

    /**
     * Set the OutputStream to use to output the result of the Render
     * (if applicable)
     * @param stream the stream to output the result of rendering to
     */
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    /**
     * Returns a DefaultHandler object used to generate the document.
     * Note this object implements the ContentHandler interface.
     * For processing with a Transformer object, this DefaultHandler object
     * can be used in the SAXResult constructor.
     * Alternatively, for processing with a SAXParser, this object can be
     * used as the DefaultHandler argument to its parse() methods.
     *
     * @return a SAX DefaultHandler for handling the SAX events.
     * @throws FOPException if setting up the DefaultHandler fails
     */
    public DefaultHandler getDefaultHandler() throws FOPException {
        if (foTreeBuilder == null) {
            this.foTreeBuilder = new FOTreeBuilder(outputFormat, foUserAgent, stream);
        }
        return this.foTreeBuilder;
    }

    /**
     * Returns the results of the rendering process. Information includes
     * the total number of pages generated and the number of pages per
     * page-sequence. Call this method only after the rendering process is
     * finished. Note that the results are only available for output formats
     * which make use of FOP's layout engine (PDF, PS, etc.).
     * @return the results of the rendering process, or null for flow-oriented 
     * output formats like RTF and MIF.
     */
    public FormattingResults getResults() {
        if (foTreeBuilder == null) {
            throw new IllegalStateException(
                    "Results are only available after calling getDefaultHandler().");
        } else {
            return foTreeBuilder.getResults();
        }
    }

    /**
     * Get the version of FOP
     * @return the version string
     * @deprecated Use {@link org.apache.fop.Version#getVersion()} instead!
     */
    public static String getVersion() {
        return org.apache.fop.Version.getVersion();
    }
}
