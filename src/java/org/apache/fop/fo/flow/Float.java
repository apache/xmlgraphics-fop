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
import org.apache.fop.fo.ToBeImplementedElement;
import org.apache.fop.layoutmgr.AddLMVisitor;

/**
 * fo:float element.
 */
public class Float extends ToBeImplementedElement {

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public Float(FONode parent) {
        super(parent);
        this.name = "fo:float";
    }

    private void setup() {

        // this.propertyList.get("float");
        // this.propertyList.get("clear");

    }

    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveFloat(this);
    }

    public String getName() {
        return "fo:float";
    }
}
