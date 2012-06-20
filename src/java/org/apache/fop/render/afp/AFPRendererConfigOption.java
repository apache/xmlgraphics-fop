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

package org.apache.fop.render.afp;

import java.net.URI;

import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.render.RendererConfigOption;

/**
 * An enumeration of the configuration options available for the AFP renderer.
 */
public enum AFPRendererConfigOption implements RendererConfigOption {
    DEFAULT_RESOURCE_LEVELS("default-resource-levels", AFPResourceLevelDefaults.class),
    IMAGES("images", null),
    IMAGES_JPEG("jpeg", null),
    IMAGES_DITHERING_QUALITY("dithering-quality", Float.class),
    IMAGES_FS45("fs45", Boolean.class),
    IMAGES_MAPPING_OPTION("mapping_option", Byte.class),
    IMAGES_MODE("mode", Boolean.class),
    IMAGES_NATIVE("native", Boolean.class),
    IMAGES_WRAP_PSEG("pseg", Boolean.class),
    JPEG_ALLOW_JPEG_EMBEDDING("allow-embedding", Boolean.class),
    JPEG_BITMAP_ENCODING_QUALITY("bitmap-encoding-quality", Float.class),
    RENDERER_RESOLUTION("renderer-resolution", Integer.class),
    RESOURCE_GROUP_URI("resource-group-file", URI.class),
    SHADING("shading", AFPShadingMode.class),
    LINE_WIDTH_CORRECTION("line-width-correction", Float.class),
    GOCA("goca", Boolean.class),
    GOCA_TEXT("text", Boolean.class);

    private final String name;

    private final Class<?> type;

    private AFPRendererConfigOption(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    /** {@inheritDoc}}*/
    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }
}
