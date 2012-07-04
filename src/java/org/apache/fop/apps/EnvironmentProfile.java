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

import java.net.URI;

import org.apache.fop.apps.io.ResourceResolver;
import org.apache.fop.fonts.FontManager;

/**
 * The environment profile represents the system in which FOP is invoked. Some of FOPs services rely
 * upon features within the system, as such, the client may want to restrict access to these
 * services. This object allows clients to control those restrictions by implementing the interfaces
 * that the environment profile holds.
 */
public interface EnvironmentProfile {

    /**
     * Returns resource resolver for this environment.
     *
     * @return the resource resolver
     */
    ResourceResolver getResourceResolver();

    /**
     * Returns the font manager with restrictions/allowances set for this environment.
     *
     * @return the font manager
     */
    FontManager getFontManager();

    /**
     * The default base URI used for resolving URIs.
     *
     * @return the default base URI
     */
    URI getDefaultBaseURI();
}
