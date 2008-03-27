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

/**
 * The level at which a resource is to reside in the AFP output
 */
public class ResourceLevel {
    private static final String EXTERNAL = "external";

    private static final String PRINT_FILE = "print-file";

    private static final String DOCUMENT = "document";

    private static final String PAGE_GROUP = "page-group";

    private static final String PAGE = "page";
    
    /**
     * where the resource will reside in the AFP output
     */
    private String level = PAGE; // default is page level
    
    /**
     * the destination location of the resource
     */
    private String dest = null;
    
    /**
     * @return true if this is a page level resource group
     */
    public boolean isPage() {
       return level.equals(PAGE);
    }
    
    /**
     * @return true if this is a page group level resource group
     */
    public boolean isPageGroup() {
        return level.equals(PAGE_GROUP);
    }

    /**
     * @return true if this is a document level resource group
     */
    public boolean isDocument() {
        return level.equals(DOCUMENT);
    }

    /**
     * @return true if this is an external level resource group
     */
    public boolean isExternal() {
        return level.equals(EXTERNAL);
    }

    /**
     * @return true if this is a print-file level resource group
     */
    public boolean isPrintFile() {
        return level.equals(PRINT_FILE);
    }

    private boolean isValid(String lvl) {
        return lvl.equals(EXTERNAL)
            || lvl.equals(PRINT_FILE)
            || lvl.equals(DOCUMENT)
            || lvl.equals(PAGE_GROUP)
            || lvl.equals(PAGE);
    }
    
    /**
     * Sets the resource placement level within the AFP output
     * @param level the resource level (page, page-group, document, print-file or external)
     * @return true if the resource level was successfully set
     */
    public boolean setLevel(String level) {
        if (isValid(level)) {
            this.level = level;
            return true;
        }
        return false;
    }

    /**
     * @return the external destination of the resource
     */
    public String getExternalDest() {
        return dest;
    }

    /**
     * Sets the external destination of the resource
     * @param dest the external destination of the resource
     */
    public void setExternalDest(String dest) {
        this.dest = dest;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "level=" + level + (isExternal() ? ", dest=" + dest : "");
    }    
}