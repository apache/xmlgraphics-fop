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

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.FOTreeVisitor;

/**
 * Implementation for fo:wrapper formatting object.
 * The wrapper object serves as
 * a property holder for it's children objects.
 *
 * Content: (#PCDATA|%inline;|%block;)*
 * Properties: id
 */
public class Wrapper extends FObjMixed {

    /**
     * @param parent FONode that is the parent of this object
     */
    public Wrapper(FONode parent) {
        super(parent);
    }

    /**
     * @return true (Wrapper contains Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveWrapper(this);
    }
    
    public String getName() {
        return "fo:wrapper";
    }

}

