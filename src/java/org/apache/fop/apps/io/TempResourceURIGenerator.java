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

package org.apache.fop.apps.io;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Creates a URI for any temporary resource used within FOP.
 */
public final class TempResourceURIGenerator {

    public static final String TMP_SCHEMA = "tmp";

    private final String tempURIPrefix;

    private final AtomicLong counter;

    /**
     * @param uriPrefix a prefix used to name the unique URI
     */
    public TempResourceURIGenerator(String uriPrefix) {
        counter = new AtomicLong();
        tempURIPrefix = URI.create(TMP_SCHEMA + ":///" + uriPrefix).toASCIIString();
    }

    /**
     * Generate a unique URI for a temporary resource
     * @return the URI
     */
    public URI generate() {
        return URI.create(tempURIPrefix + getUniqueId());
    }

    private String getUniqueId() {
        return Long.toHexString(counter.getAndIncrement());
    }

    public static boolean isTempUri(URI uri) {
        return TMP_SCHEMA.equals(uri.getScheme());
    }
}
