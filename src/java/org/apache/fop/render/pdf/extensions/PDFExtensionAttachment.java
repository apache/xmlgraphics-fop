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

package org.apache.fop.render.pdf.extensions;

import org.apache.xmlgraphics.util.XMLizable;

import org.apache.fop.fo.extensions.ExtensionAttachment;

public abstract class PDFExtensionAttachment implements ExtensionAttachment, XMLizable {

    /** The category URI for this extension attachment. */
    public static final String CATEGORY = "apache:fop:extensions:pdf";

    /** The prefix to use with qualified names for this extension attachment. */
    public static final String PREFIX = "pdf";

    /**
     * Default constructor.
     */
    public PDFExtensionAttachment() {
    }

    public String getPrefix() {
        return PREFIX;
    }

    public String getCategory() {
        return CATEGORY;
    }
}
