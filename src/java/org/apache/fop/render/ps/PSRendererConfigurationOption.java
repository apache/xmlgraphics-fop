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

package org.apache.fop.render.ps;

import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.render.RendererConfigOption;

/**
 * An enumeration of the PostScript renderer configuration options along with their default values.
 */
public enum PSRendererConfigurationOption implements RendererConfigOption {
    /** Indicates whether landscape pages should be rotated, default: false */
    AUTO_ROTATE_LANDSCAPE("auto-rotate-landscape", false),
    /** Sets the PostScript language leven, default: {@see PSGenerator#DEFAULT_LANGUAGE_LEVEL}*/
    LANGUAGE_LEVEL("language-level", PSGenerator.DEFAULT_LANGUAGE_LEVEL),
    /** Whether resources should be optimized in a post-processing run, default: false */
    OPTIMIZE_RESOURCES("optimize-resources", false),
    /** Indicates whether the "safe setpagedevice" mode is active, default: false */
    SAFE_SET_PAGE_DEVICE("safe-set-page-device", false),
    /** Indicates whether the PostScript output should be DSC compliant, default: true*/
    DSC_COMPLIANT("dsc-compliant", true),
    RENDERING_MODE("rendering", PSRenderingMode.QUALITY);

    private final String name;
    private final Object defaultValue;

    private PSRendererConfigurationOption(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
