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

import org.apache.fop.layoutmgr.AddLMVisitor;

/**
 * This class is a placeholder for elements that have not been implemented.
 */
public class ToBeImplementedElement extends FObj {

    /**
     * @param parent FONode that is the parent of this object
     */
    protected ToBeImplementedElement(FONode parent) {
        super(parent);
    }

    /**
     * This is a hook for the AddLMVisitor class to be able to access
     * this object.
     * @param aLMV the AddLMVisitor object that can access this object.
     */
    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveToBeImplementedElement(this);
    }

}
