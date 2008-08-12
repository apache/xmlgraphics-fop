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

package org.apache.fop.render.intermediate.extensions;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.render.intermediate.IFConstants;

/**
 * Constants for the IF bookmark extension.
 */
public interface BookmarkExtensionConstants {

    /** Namespace URI for the bookmark extension */
    String NAMESPACE = IFConstants.NAMESPACE + "/bookmarks";

    /** the bookmark-tree element */
    QName BOOKMARK_TREE = new QName(NAMESPACE, "bookmark-tree");
    /** the bookmark element */
    QName BOOKMARK = new QName(NAMESPACE, "bookmark");
    /** the goto-xy element */
    QName GOTO_XY = new QName(NAMESPACE, "goto-xy");
    /** the goto-uri element */
    QName GOTO_URI = new QName(NAMESPACE, "goto-uri");

}
