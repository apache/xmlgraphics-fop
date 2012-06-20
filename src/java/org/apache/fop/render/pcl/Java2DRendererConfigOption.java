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

package org.apache.fop.render.pcl;

import org.apache.fop.render.RendererConfigOption;

/**
 * An enumeration of the renderer configuration options available to the Java2D renderer via the
 * FOP conf.
 */
public enum Java2DRendererConfigOption implements RendererConfigOption {

    RENDERING_MODE("rendering", PCLRenderingMode.class),
    TEXT_RENDERING("text-rendering", Boolean.class),
    DISABLE_PJL("disable-pjl", Boolean.class);

    private final String name;

    private final Class<?> type;

    private Java2DRendererConfigOption(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    /** {@inheritDoc} */
    public String getName() {
        return name;
    }

    Class<?> getType() {
        return type;
    }
}
