/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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


package org.apache.fop.area;

// FOP
import org.apache.fop.fo.extensions.Bookmarks;

// Avalon
import org.apache.avalon.framework.logger.Logger;

// Java
import java.util.Set;

/**
 * An interface for classes that are conceptually the parent class of the
 * area.AreaTree object. The purpose of the interface is to keep the AreaTree
 * isolated from apps, but to acknowledge that a higher-level object is needed
 * to control the Area Tree, to provide it with information about the
 * environment, and to keep track of meta information.
 */
public interface AreaTreeControl {

    /**
     * @return the Bookmark object encapsulating the bookmarks for the FO Tree.
     */
    Bookmarks getBookmarks();

    /**
     * @return the Logger being used with this FO Tree
     */
    Logger getLogger();

    /**
     * The current set of IDs in the document.
     * @return the Set of IDReferences in the document.
     */
    Set getIDReferences();

}
