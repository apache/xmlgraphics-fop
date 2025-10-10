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

import org.xml.sax.helpers.DefaultHandler;

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
    private String outputFormat;

    // output stream to send results to
    private OutputStream stream;

    // FOUserAgent object to set processing options
    private final FOUserAgent foUserAgent;

    // FOTreeBuilder object to maintain reference for access to results
    private FOTreeBuilder foTreeBuilder;

    /**
     * Constructor for use with already-created FOUserAgents. It uses MIME types to select the
     * output format (ex. "application/pdf" for PDF).
     * @param outputFormat the MIME type of the output format to use (ex. "application/pdf").
     * @param ua FOUserAgent object
     * @param stream the output stream
     * @throws FOPException if setting up the DefaultHandler fails
     */
    Fop(String outputFormat, FOUserAgent ua, OutputStream stream) throws FOPException {
        if (ua == null) {
            throw new FOPException("Cannot create a new Fop instance without a User Agent.");
        }
        this.outputFormat = outputFormat;

        foUserAgent = ua;

        this.stream = stream;

        createDefaultHandler();
    }

    /**
     * Get the FOUserAgent instance associated with the rendering run represented by this instance.
     * @return the user agent
     *
     * @deprecated this getter doesn't need to exist. By virtue of the fact that the client has
     * an instance of this object, it means they also have the {@link FOUserAgent} since this's
     * constructor is only used in {@link FOUserAgent}
     */
    @Deprecated
    public FOUserAgent getUserAgent() {
        return foUserAgent;
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
     * Returns the DefaultHandler object that will receive the SAX stream containing the
     * FO document to be rendered.
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

}
