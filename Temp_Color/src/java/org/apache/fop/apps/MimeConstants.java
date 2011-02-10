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

/**
 * Frequently used MIME types for various file formats used when working with Apache FOP.
 */
public interface MimeConstants extends org.apache.xmlgraphics.util.MimeConstants {

    /** Apache FOP's AWT preview (non-standard MIME type) */
    String MIME_FOP_AWT_PREVIEW = "application/X-fop-awt-preview";
    /** Apache FOP's Direct Printing (non-standard MIME type) */
    String MIME_FOP_PRINT       = "application/X-fop-print";
    /** Apache FOP's area tree XML */
    String MIME_FOP_AREA_TREE   = "application/X-fop-areatree";
    /** Apache FOP's intermediate format XML */
    String MIME_FOP_IF          = "application/X-fop-intermediate-format";
}
