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


package org.apache.fop.fo;

// Java
import java.util.Map;
import java.util.Set;

// FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.FontInfo;

/**
 * An interface for classes that are conceptually the parent class of the
 * fo.pagination.Root object. The purpose of the interface is to maintain
 * encapsulation of the FO Tree classes, but to acknowledge that a higher-level
 * object is needed to control the building of the FO Tree, to provide it
 * with information about the environment, and to keep track of meta-type
 * information.
 */
public interface FOTreeControl {

    /**
     * Returns the set of ID references found in the FO Tree.
     * @return the ID references
     */
    Set getIDReferences();

    /**
     * @return the FOInputHandler for parsing this FO Tree
     */
    FOInputHandler getFOInputHandler();

    /**
     * @return the Driver associated with this FO Tree
     */
    Driver getDriver();
    
    /**
     * @return the FontInfo object associated with this FOTree
     */
    FontInfo getFontInfo();
    
}
