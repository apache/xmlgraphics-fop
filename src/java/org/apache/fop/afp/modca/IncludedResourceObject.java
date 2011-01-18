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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;

import org.apache.fop.afp.util.AFPResourceUtil;
import org.apache.fop.afp.util.ResourceAccessor;


/**
 * Encapsulates an included resource object that is loaded from an external file.
 */
public class IncludedResourceObject extends AbstractNamedAFPObject {

    private ResourceAccessor resourceAccessor;
    private URI uri;

    /**
     * Main constructor.
     * @param name the name of the included resource
     * @param resourceAccessor the resource accessor to load the external file with
     * @param uri the URI of the external file
     */
    public IncludedResourceObject(String name,
            ResourceAccessor resourceAccessor, URI uri) {
        super(name);
        this.resourceAccessor = resourceAccessor;
        this.uri = uri;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        InputStream in = resourceAccessor.createInputStream(this.uri);
        try {
            AFPResourceUtil.copyResourceFile(in, os);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
