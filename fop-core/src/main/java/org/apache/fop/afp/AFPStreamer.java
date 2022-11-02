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

package org.apache.fop.afp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.io.TempResourceURIGenerator;

import org.apache.fop.afp.modca.ResourceGroup;
import org.apache.fop.afp.modca.StreamedResourceGroup;
import org.apache.fop.apps.io.InternalResourceResolver;

/**
 * Manages the streaming of the AFP output
 */
public class AFPStreamer implements Streamable {
    /** Static logging instance */
    private static final Log LOG = LogFactory.getLog(AFPStreamer.class);

    private static final String DEFAULT_EXTERNAL_RESOURCE_FILENAME = "resources.afp";

    private static final TempResourceURIGenerator TEMP_URI_GENERATOR
            = new TempResourceURIGenerator("AFPDataStream_");

    private final Factory factory;

    private final InternalResourceResolver resourceResolver;

    /** A mapping of external resource destinations to resource groups */
    private final Map<URI, ResourceGroup> pathResourceGroupMap = new HashMap<URI, ResourceGroup>();

    private StreamedResourceGroup printFileResourceGroup;

    /** Sets the default resource group file path */
    private URI defaultResourceGroupUri;

    private final URI tempUri;

    /** temporary document outputstream */
    private OutputStream tempOutputStream;

    /** the final outputstream */
    private OutputStream outputStream;

    private DataStream dataStream;

    /**
     * Main constructor
     *
     * @param factory a factory
     * @param resourceResolver resource resolver
     */
    public AFPStreamer(Factory factory, InternalResourceResolver resourceResolver) {
        this.factory = factory;
        this.resourceResolver = resourceResolver;
        this.tempUri = TEMP_URI_GENERATOR.generate();
        defaultResourceGroupUri = URI.create(DEFAULT_EXTERNAL_RESOURCE_FILENAME);

    }

    /**
     * Creates a new DataStream
     *
     * @param paintingState the AFP painting state
     * @return a new {@link DataStream}
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public DataStream createDataStream(AFPPaintingState paintingState) throws IOException {
        this.tempOutputStream = new BufferedOutputStream(resourceResolver.getOutputStream(tempUri));
        this.dataStream = factory.createDataStream(paintingState, tempOutputStream);
        return dataStream;
    }

    /**
     * Sets the default resource group URI.
     *
     * @param uri the default resource group URI
     */
    public void setDefaultResourceGroupUri(URI uri) {
        this.defaultResourceGroupUri = uri;
    }

    /**
     * Returns the resource group for a given resource info
     *
     * @param level a resource level
     * @return a resource group for the given resource info
     */
    public ResourceGroup getResourceGroup(AFPResourceLevel level) {
        ResourceGroup resourceGroup = null;
        if (level.isInline()) { // no resource group for inline level
            return null;
        }
        if (level.isExternal()) {
            URI uri = level.getExternalURI();
            if (uri == null) {
                LOG.warn("No file path provided for external resource, using default.");
                uri = defaultResourceGroupUri;
            }
            resourceGroup = pathResourceGroupMap.get(uri);
            if (resourceGroup == null) {
                OutputStream os = null;
                try {
                    os = new BufferedOutputStream(resourceResolver.getOutputStream(uri));
                } catch (IOException ioe) {
                    LOG.error("Failed to create/open external resource group for uri '"
                            + uri + "'");
                } finally {
                    if (os != null) {
                        resourceGroup = factory.createStreamedResourceGroup(os);
                        pathResourceGroupMap.put(uri, resourceGroup);
                    }
                }
            }
        } else if (level.isPrintFile()) {
            if (printFileResourceGroup == null) {
                // use final outputstream for print-file resource group
                printFileResourceGroup = factory.createStreamedResourceGroup(outputStream);
            }
            resourceGroup = printFileResourceGroup;
        } else {
            // resource group in afp document datastream
            resourceGroup = dataStream.getResourceGroup(level);
        }
        return resourceGroup;
    }

    /**
     * Closes off the AFP stream writing the document stream
     *
     * @throws IOException if an an I/O exception of some sort has occurred
     */
    // write out any external resource groups
    public void close() throws IOException {
        for (ResourceGroup resourceGroup : pathResourceGroupMap.values()) {
            // TODO - Why not a Map<URI, StreamedResourceGroup>, if all the elements are expected to be of that type?
            assert (resourceGroup instanceof StreamedResourceGroup);
            ((StreamedResourceGroup) resourceGroup).close();
        }
        // close any open print-file resource group
        if (printFileResourceGroup != null) {
            printFileResourceGroup.close();
        }
        // write out document
        writeToStream(outputStream);
        outputStream.close();
    }

    /**
     * Sets the final outputstream
     *
     * @param outputStream an outputstream
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        tempOutputStream.close();
        InputStream tempInputStream = resourceResolver.getResource(tempUri);
        IOUtils.copy(tempInputStream, os);
        //TODO this should notify the stream provider that it is safe to delete the temp data
        tempInputStream.close();
        os.flush();
    }
}
