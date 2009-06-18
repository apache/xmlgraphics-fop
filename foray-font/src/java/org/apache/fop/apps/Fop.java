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

package org.apache.fop.apps;

// Java
import java.io.OutputStream;

// XML
import org.xml.sax.helpers.DefaultHandler;

// FOP
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
public class Fop {

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
     * @param stream the output stream
     * @throws FOPException if setting up the DefaultHandler fails
     * @deprecated End-users should use {@link FopFactory#newFop(String, FOUserAgent, OutputStream)} 
     *             instead! This constructor will become invisible with FOP 1.0.
     */
    public Fop(String outputFormat, FOUserAgent ua, OutputStream stream) throws FOPException {
        this.outputFormat = outputFormat;

        foUserAgent = ua;
        if (foUserAgent == null) {
            foUserAgent = FopFactory.newInstance().newFOUserAgent();
        }
        
        this.stream = stream;
        
        createDefaultHandler();
    }

    /**
     * Constructor for use with already-created FOUserAgents. It uses MIME types to select the 
     * output format (ex. "application/pdf" for PDF).
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param ua FOUserAgent object
     * @throws FOPException if setting up the DefaultHandler fails
     * @deprecated End-users should use {@link FopFactory#newFop(String, FOUserAgent)} instead!
     *             This constructor will become invisible with FOP 1.0.
     */
    public Fop(String outputFormat, FOUserAgent ua) throws FOPException {
        this.outputFormat = outputFormat;

        foUserAgent = ua;
        if (foUserAgent == null) {
            foUserAgent = FopFactory.newInstance().newFOUserAgent();
        }
        
        createDefaultHandler();
    }

    /**
     * Constructor for FOP with a default FOUserAgent. It uses MIME types to select the 
     * output format (ex. "application/pdf" for PDF).
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @deprecated End-users should use {@link FopFactory#newFop(String)} instead!
     *             This constructor will become invisible with FOP 1.0.
     */
    public Fop(String outputFormat) {
        this.outputFormat = outputFormat;
        foUserAgent = FopFactory.newInstance().newFOUserAgent();
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
     * @deprecated Use one of the factory methods on {@link FopFactory} with an OutputStream
     *             parameter instead. This method will be removed with FOP 1.0.
     */
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    /**
     * Creates a DefaultHandler object used to generate the document.
     * Note this object implements the ContentHandler interface.
     * For processing with a Transformer object, this DefaultHandler object
     * can be used in the SAXResult constructor.
     * Alternatively, for processing with a SAXParser, this object can be
     * used as the DefaultHandler argument to its parse() methods.
     *
     * @throws FOPException if setting up the DefaultHandler fails
     */
    private void createDefaultHandler() throws FOPException {
        this.foTreeBuilder = new FOTreeBuilder(outputFormat, foUserAgent, stream);
    }

    /**
     * Returns the DefaultHandler object used to generate the document.
     * Checking for null and the exception is only for the deprecated constructor.
     * @return the SAX DefaultHandler for handling the SAX events.
     * @throws FOPException if setting up the DefaultHandler fails
     */
    public DefaultHandler getDefaultHandler() throws FOPException {
        if (foTreeBuilder == null) {
            createDefaultHandler();
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
     *             This method will be removed with FOP 1.0.
     */
    public static String getVersion() {
        return org.apache.fop.Version.getVersion();
    }
}
