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

package org.apache.fop.render.afp;

/**
 * A resource level
 */
public class ResourceLevel {
    
    /** page level **/
    public static final int PAGE = 0;

    /** page group level **/
    public static final int PAGE_GROUP = 1;

    /** document level **/
    public static final int DOCUMENT = 2;

    /** print file level **/
    public static final int PRINT_FILE = 3;

    /** external level **/
    public static final int EXTERNAL = 4;
    
    private static final String NAME_PAGE = "page";
    private static final String NAME_PAGE_GROUP = "page-group";
    private static final String NAME_DOCUMENT = "document";
    private static final String NAME_PRINT_FILE = "print-file";
    private static final String NAME_EXTERNAL = "external";

    private static final String[] NAMES
        = new String[] {NAME_PAGE, NAME_PAGE_GROUP, NAME_DOCUMENT, NAME_PRINT_FILE, NAME_EXTERNAL};

    
    
    /** where the resource will reside in the AFP output */
    private int level = PRINT_FILE; // default is print-file level

    /** the external resource group file path */
    private String extFilePath = null;

    /**
     * Sets the resource placement level within the AFP output
     * 
     * @param lvl the resource level (page, page-group, document, print-file or external)
     * @return true if the resource level was successfully set
     */
    public static ResourceLevel valueOf(String lvl) {
        ResourceLevel level = null;
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equals(lvl)) {
                level = new ResourceLevel(i);
                break;
            }
        }
        if (lvl == null) {
            throw new IllegalArgumentException("Unknown resource level '" + lvl + "'");
        }
        return level;
    }
    
    /**
     * Main constructor
     * 
     * @param level the resource level
     */
    public ResourceLevel(int level) {
        setLevel(level);
    }

    /**
     * Sets the resource level
     * 
     * @param level the resource level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns true if this is a page level resource group
     * 
     * @return true if this is a page level resource group
     */
    public boolean isPage() {
       return level == PAGE;
    }
    
    /**
     * Returns true if this is a page group level resource group
     * 
     * @return true if this is a page group level resource group
     */
    public boolean isPageGroup() {
        return level == PAGE_GROUP;
    }

    /**
     * Returns true if this is a document level resource group
     * 
     * @return true if this is a document level resource group
     */
    public boolean isDocument() {
        return level == DOCUMENT;
    }

    /**
     * Returns true if this is an external level resource group
     * 
     * @return true if this is an external level resource group
     */
    public boolean isExternal() {
        return level == EXTERNAL;
    }

    /**
     * Returns true if this is a print-file level resource group
     * 
     * @return true if this is a print-file level resource group
     */
    public boolean isPrintFile() {
        return level == PRINT_FILE;
    }
    
    /**
     * Returns the destination file path of the external resource group file
     * 
     * @return the destination file path of the external resource group file
     */
    public String getExternalFilePath() {
        return this.extFilePath;
    }

    /**
     * Sets the external destination of the resource
     * 
     * @param filePath the external resource group file
     */
    public void setExternalFilePath(String filePath) {
        this.extFilePath = filePath;
    }

    /** {@inheritDoc} */
    public String toString() {
        return NAMES[level] +  (isExternal() ? ", file=" + extFilePath : "");
    }
    
    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof ResourceLevel)) {
            return false;
        }

        ResourceLevel rl = (ResourceLevel)obj;
        return (level == level)
            && (extFilePath == rl.extFilePath
                    || extFilePath != null && extFilePath.equals(rl.extFilePath));
    }
    
    /** {@inheritDoc} */
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + level;
        hash = 31 * hash + (null == extFilePath ? 0 : extFilePath.hashCode());
        return hash;
    }
}