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

package org.apache.fop.apps.io;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * This class represents a resolved resource.  The type property is used by FOP to identify the resource
 *  content.
 *
 */
public class Resource extends FilterInputStream {

    private final String type;

    /**
     * @param type resource type
     * @param inputStream input stream of the resource
     */
    public Resource(String type, InputStream inputStream) {
        super(inputStream);
        this.type = type;
    }

    /**
     * Constructs a resource of 'unknown' type.
     *
     * @param inputStream input stream of the resource
     */
    public Resource(InputStream inputStream) {
        this("unknown", inputStream);
    }

    /**
     * @return the resource type
     */
    public String getType() {
        return this.type;
    }

}
