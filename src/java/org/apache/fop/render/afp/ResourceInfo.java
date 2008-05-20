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
public class ResourceInfo {
            
    /**
     * the reference name of this resource
     */
    private String name = null;
        
    /**
     * the resource level (default to print-file)
     */
    private ResourceLevel level = new ResourceLevel(ResourceLevel.PRINT_FILE);
    
    
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
        return "ResourceInfo(" + (name != null ? "name=" + name : "" ) + ", level=" + level + ")";
    }

    /**
     * @return the resource level
     */
    public ResourceLevel getLevel() {
        return this.level;
    }

    /**
     * Sets the resource level
     * @param resourceLevel the resource level
     */
    public void setLevel(ResourceLevel resourceLevel) {
        this.level = resourceLevel;
    }
}