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

/* $Id: $ */

package org.apache.fop.render.afp;

import java.io.File;

/**
 * The level at which a resource is to reside in the AFP output
 */
public class ResourceInfo {
    private static final String LEVEL_EXTERNAL = "external";

    private static final String LEVEL_PRINT_FILE = "print-file";

    private static final String LEVEL_DOCUMENT = "document";

    private static final String LEVEL_PAGE_GROUP = "page-group";

    private static final String LEVEL_PAGE = "page";
    
    /**
     * the reference name of this resource
     */
    private String name = null;

    /**
     * where the resource will reside in the AFP output
     */
    private String level = LEVEL_PAGE; // default is page level
    
    /**
     * the external resource group file
     */
    private File externalResourceGroupFile = null;
    
    /**
     * @return true if this is a page level resource group
     */
    public boolean isPage() {
       return level.equals(LEVEL_PAGE);
    }
    
    /**
     * @return true if this is a page group level resource group
     */
    public boolean isPageGroup() {
        return level.equals(LEVEL_PAGE_GROUP);
    }

    /**
     * @return true if this is a document level resource group
     */
    public boolean isDocument() {
        return level.equals(LEVEL_DOCUMENT);
    }

    /**
     * @return true if this is an external level resource group
     */
    public boolean isExternal() {
        return level.equals(LEVEL_EXTERNAL);
    }

    /**
     * @return true if this is a print-file level resource group
     */
    public boolean isPrintFile() {
        return level.equals(LEVEL_PRINT_FILE);
    }

    private boolean isValid(String lvl) {
        return lvl.equals(LEVEL_EXTERNAL)
            || lvl.equals(LEVEL_PRINT_FILE)
            || lvl.equals(LEVEL_DOCUMENT)
            || lvl.equals(LEVEL_PAGE_GROUP)
            || lvl.equals(LEVEL_PAGE);
    }
    
    /**
     * Sets the resource placement level within the AFP output
     * @param lvl the resource level (page, page-group, document, print-file or external)
     * @return true if the resource level was successfully set
     */
    public boolean setLevel(String lvl) {
        if (lvl != null && isValid(lvl)) {
            this.level = lvl;
            return true;
        }
        return false;
    }

    /**
     * @return the external resource group file of this resource
     */
    public File getExternalResourceGroupFile() {
        return this.externalResourceGroupFile;
    }

    /**
     * @return the destination file path of the external resource group file
     */
    public String getExternalResourceGroupDest() {
        if (externalResourceGroupFile != null) {
            return externalResourceGroupFile.getAbsolutePath();
        }
        return null;
    }
    
    /**
     * @return true if this resource has a defined external resource group file destination
     */
    public boolean hasExternalResourceGroupFile() {
        return getExternalResourceGroupFile() != null;
    }

    /**
     * Sets the external destination of the resource
     * @param file the external resource group file
     */
    public void setExternalResourceGroupFile(File file) {
        this.externalResourceGroupFile = file;
    }
    
    /**
     * Sets the resource reference name
     * @param resourceName the resource reference name
     */
    public void setName(String resourceName) {
        this.name = resourceName;
    } 

    /**
     * @return the resource reference name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "ResourceInfo(name=" + name  + ", level=" + level
            + (isExternal() ? ", externalResourceGroupFile=" + externalResourceGroupFile : "")
            + ")";
    }
}