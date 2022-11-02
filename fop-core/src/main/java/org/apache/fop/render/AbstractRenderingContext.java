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

package org.apache.fop.render;

import java.util.Collections;
import java.util.Map;

import org.apache.fop.apps.FOUserAgent;

/**
 * Abstract base class for RenderingContext implementations.
 */
public abstract class AbstractRenderingContext implements RenderingContext {

    private FOUserAgent userAgent;
    private Map hints;

    /**
     * Main constructor.
     * @param userAgent the user agent
     */
    public AbstractRenderingContext(FOUserAgent userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Returns the user agent.
     *
     * @return   The user agent
     */
    public FOUserAgent getUserAgent() {
        return userAgent;
    }

    /** {@inheritDoc} */
    public void putHints(Map additionalHints) {
        if (additionalHints == null) {
            return;
        }
        if (this.hints == null) {
            this.hints = new java.util.HashMap();
        }
        this.hints.putAll(additionalHints);
    }

    /** {@inheritDoc} */
    public void putHint(Object key, Object value) {
        this.hints.put(key, value);
    }

    /** {@inheritDoc} */
    public Map getHints() {
        if (this.hints == null) {
            return Collections.EMPTY_MAP;
        } else {
            return Collections.unmodifiableMap(this.hints);
        }
    }

    /** {@inheritDoc} */
    public Object getHint(Object key) {
        if (this.hints == null) {
            return null;
        } else {
            return this.hints.get(key);
        }
    }
}

