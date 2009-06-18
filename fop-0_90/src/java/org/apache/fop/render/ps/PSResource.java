/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.render.ps;

/**
 * Represents a PostScript resource (file, font, procset etc.).
 */
public class PSResource {

    /** a file resource */
    public static final String TYPE_FILE = "file";
    /** a font resource */
    public static final String TYPE_FONT = "font";
    /** a procset resource */
    public static final String TYPE_PROCSET = "procset";
    
    private String type;
    private String name;
    
    /**
     * Main constructor
     * @param type type of the resource
     * @param name name of the resource
     */
    public PSResource(String type, String name) {
        this.type = type;
        this.name = name;
    }
    
    /** @return the type of the resource */
    public String getType() {
        return this.type;
    }
    
    /** @return the name of the resource */
    public String getName() {
        return this.name;
    }
    
    /** @return the <resource> specification as defined in DSC v3.0 spec. */
    public String getResourceSpecification() {
        StringBuffer sb = new StringBuffer();
        sb.append(getType()).append(" ").append(PSGenerator.convertStringToDSC(getName()));
        return sb.toString();
    }
    
}
