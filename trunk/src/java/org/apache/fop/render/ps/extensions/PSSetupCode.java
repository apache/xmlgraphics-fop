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

package org.apache.fop.render.ps.extensions;

import java.io.Serializable;

import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * This is the pass-through value object for the PostScript extension.
 */
public class PSSetupCode implements ExtensionAttachment, Serializable {

    /** The category URI for this extension attachment. */
    public static final String CATEGORY = "apache:fop:extensions:postscript";
    
    private String name;
    private String content;

    /**
     * No-argument contructor.
     */
    public PSSetupCode() {
        //nop
    }
    
    /**
     * Default constructor.
     * @param name the name of the setup code object, may be null
     * @param content the content of the setup code object
     */
    public PSSetupCode(String name, String content) {
        this.name = name;
        this.content = content;
    }
    
    /** @return the content */
    public String getContent() {
        return content;
    }
    
    /**
     * Sets the content for the setup code object.
     * @param content The content to set.
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /** @return the name */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the setup code object.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** @see org.apache.fop.fo.extensions.ExtensionAttachment#getCategory() */
    public String getCategory() {
        return CATEGORY;
    }
    
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        return "PSSetupCode(name=" + getName() + ")";
    }
}
