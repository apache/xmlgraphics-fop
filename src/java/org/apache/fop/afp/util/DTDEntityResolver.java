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

package org.apache.fop.afp.util;

import java.io.IOException;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.apache.fop.afp.fonts.FontRuntimeException;

/**
 * An entity resolver for both DOM and SAX models of the SAX document.<br>
 * The entity resolver only handles queries for the DTD. It will find any URI
 * with a recognised public id and return an {@link org.xml.sax.InputSource}.
 * <br>
 *
 * <p>This work was authored by Joe Schmetzer (joe@exubero.com).</p>
 */
public class DTDEntityResolver implements EntityResolver {

    /** Public ID for the AFP fonts 1.0 DTD. */
    public static final String AFP_DTD_1_0_ID
        = "-//APACHE/DTD AFP Installed Font Definition DTD 1.0//EN";

    /** Resource location for the AFP fonts 1.0 DTD. */
    public static final String AFP_DTD_1_0_RESOURCE
        =  "afp-fonts-1.0.dtd";

    /** Public ID for the AFP fonts 1.1 DTD. */
    public static final String AFP_DTD_1_1_ID
        = "-//APACHE/DTD AFP Installed Font Definition DTD 1.1//EN";

    /** Resource location for the AFP fonts 1.1 DTD. */
    public static final String AFP_DTD_1_1_RESOURCE
        =  "afp-fonts-1.1.dtd";

    /** Public ID for the AFP fonts 1.2 DTD. */
    public static final String AFP_DTD_1_2_ID
        = "-//APACHE/DTD AFP Installed Font Definition DTD 1.2//EN";

    /** Resource location for the AFP fonts 1.2 DTD. */
    public static final String AFP_DTD_1_2_RESOURCE
        =  "afp-fonts-1.2.dtd";

    /**
     * Resolve the combination of system and public identifiers.
     * If this resolver recognises the publicId, it will handle the resolution
     * from the classpath, otherwise it will return null and allow the default
     * resolution to occur.
     *
     * @param publicId the public identifier to use
     * @param systemId the system identifier to resolve
     * @return An input source to the entity or null if not handled
     * @throws IOException an error reading the stream
     */
    public InputSource resolveEntity(String publicId, String systemId)
    throws IOException {

        URL resource = null;
        if (AFP_DTD_1_2_ID.equals(publicId)) {
            resource = getResource(AFP_DTD_1_2_RESOURCE);
        } else if (AFP_DTD_1_1_ID.equals(publicId)) {
            resource = getResource(AFP_DTD_1_1_RESOURCE);
        } else if (AFP_DTD_1_0_ID.equals(publicId)) {
            throw new FontRuntimeException(
                "The AFP Installed Font Definition 1.0 DTD is not longer supported");
        } else if (systemId != null && systemId.indexOf("afp-fonts.dtd") >= 0) {
            throw new FontRuntimeException(
                "The AFP Installed Font Definition DTD must be specified using the public id");
        } else {
            return null;
        }

        InputSource inputSource = new InputSource(resource.openStream());
        inputSource.setPublicId(publicId);
        inputSource.setSystemId(systemId);

        return inputSource;
    }

    /**
     * Returns the URL of a resource on the classpath
     * @param resourcePath the path to the resource relative to the root of the
     * classpath.
     * @return the URL of the required resource
     * @throws FontRuntimeException if the resource could not be found.
     */
    private URL getResource(String resourcePath) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        URL resource = cl.getResource(resourcePath);
        if (resource == null) {
            throw new FontRuntimeException("Resource " + resourcePath
                    + "could not be found on the classpath");
        }

        return resource;
    }
}
