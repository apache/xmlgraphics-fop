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

package org.apache.fop.render.pcl;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.fo.extensions.ExtensionElementMapping;

/**
 * Constants used for PCL output.
 */
interface PCLConstants {

    /** Source transparency mode */
    QName SRC_TRANSPARENCY = new QName(ExtensionElementMapping.URI, null, "source-transparency");

    /** Disable clipping */
    Object DISABLE_CLIPPING = new QName(ExtensionElementMapping.URI, null, "disable-clipping");

    /** Enables/Disables a color canvas for bitmap production (required for SVG, for example) */
    Object COLOR_CANVAS = new QName(ExtensionElementMapping.URI, null, "color-canvas");

}
