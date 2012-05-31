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

package org.apache.fop.render.bitmap;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.apache.fop.render.RendererConfigOptions;

public enum BitmapRendererConfigOptions implements RendererConfigOptions {
    JAVA2D_TRANSPARENT_PAGE_BACKGROUND("transparent-page-background", false),
    BACKGROUND_COLOR("background-color", Color.WHITE),
    ANTI_ALIASING("anti-aliasing", true),
    RENDERING_QUALITY_ELEMENT("rendering"),
    RENDERING_QUALITY("quality", true),
    RENDERING_SPEED("speed"),
    COLOR_MODE("color-mode", BufferedImage.TYPE_INT_ARGB),
    COLOR_MODE_RGBA("rgba"),
    COLOR_MODE_RGB("rgb"),
    COLOR_MODE_GRAY("gray"),
    COLOR_MODE_BINARY("binary"),
    COLOR_MODE_BILEVEL("bi-level");

    private final String name;
    private final Object defaultValue;

    private BitmapRendererConfigOptions(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    private BitmapRendererConfigOptions(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public static BitmapRendererConfigOptions getValue(String str) {
        for (BitmapRendererConfigOptions opt : BitmapRendererConfigOptions.values()) {
            if (opt.getName().equalsIgnoreCase(str)) {
                return opt;
            }
        }
        return null;
    }
}
