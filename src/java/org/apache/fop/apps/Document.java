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

package org.apache.fop.apps;

// FOP
import org.apache.fop.area.AreaTree;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fonts.FontInfo;

// SAX
import org.xml.sax.SAXException;

/**
 * Class storing information for the FOP Document being processed, and managing
 * the processing of it.
 */
public class Document {
            
    /** The parent Driver object */
    private Driver driver;

    /** The Font information relevant for this document */
    private FontInfo fontInfo;
    
    /** The current AreaTree for the PageSequence being rendered. */
    public AreaTree areaTree;

    /** The AreaTreeModel for the PageSequence being rendered. */
    public AreaTreeModel atModel;

    /**
     * Structure handler used to notify structure
     * events such as start end element.
     */
    public FOInputHandler foInputHandler;

    /**
     * Main constructor
     * @param driver the Driver object that is the "parent" of this Document
     */
    public Document(Driver driver) {
        this.driver = driver;
        this.fontInfo = new FontInfo();
    }

    /**
     * Retrieve the font information for this document
     * @return the FontInfo instance for this document
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /**
     * Public accessor for the parent Driver of this Document
     * @return the parent Driver for this Document
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * Get the area tree for this layout handler.
     *
     * @return the area tree for this document
     */
    public AreaTree getAreaTree() {
        return areaTree;
    }

    /**
     * @return the FOInputHandler for parsing this FO Tree
     */
    public FOInputHandler getFOInputHandler() {
        return foInputHandler;
    }

}
