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
import org.apache.fop.fo.FObj;

/**
 * Class modelling the fo:multi-toggle property.
 */
public class MultiToggle extends FObj {

    static boolean notImplementedWarningGiven = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public MultiToggle(FONode parent) {
        super(parent);

        if (!notImplementedWarningGiven) {
            getLogger().warn("fo:multi-toggle is not yet implemented.");
            notImplementedWarningGiven = true;
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:multi-toggle";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_MULTI_TOGGLE;
    }
}
