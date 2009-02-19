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

package org.apache.fop.render.intermediate;

import org.apache.fop.apps.MimeConstants;
import org.apache.fop.util.XMLConstants;

/**
 * Constants for the intermediate format.
 */
public interface IFConstants extends XMLConstants {

    /** MIME type of the intermediate format. */
    String MIME_TYPE = MimeConstants.MIME_FOP_IF;

    /** XML namespace of the intermediate format. */
    String NAMESPACE = "http://xmlgraphics.apache.org/fop/intermediate";

    String EL_DOCUMENT = "document";
    String EL_HEADER = "header";
    String EL_TRAILER = "trailer";
    String EL_PAGE_SEQUENCE = "page-sequence";
    String EL_PAGE = "page";
    String EL_PAGE_HEADER = "page-header";
    String EL_PAGE_TRAILER = "page-trailer";
    String EL_PAGE_CONTENT = "content";
    String EL_VIEWPORT = "viewport";
    String EL_GROUP = "g";
    String EL_IMAGE = "image";
    String EL_CLIP_RECT = "clip-rect";
    String EL_RECT = "rect";
    String EL_LINE = "line";
    String EL_BORDER_RECT = "border-rect";
    String EL_FONT = "font";
    String EL_TEXT = "text";
    /** used for accessibility */
    String EL_STRUCTURE_TREE = "structure-tree";  
}
