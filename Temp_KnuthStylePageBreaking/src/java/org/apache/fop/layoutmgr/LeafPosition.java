/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
 
package org.apache.fop.layoutmgr;

public class LeafPosition extends Position {

    private int iLeafPos;

    public LeafPosition(LayoutManager lm, int pos) {
        super(lm);
        iLeafPos = pos;
    }

    public int getLeafPos() {
        return iLeafPos;
    }
    
    /** @see java.lang.Object#toString()*/
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" {pos=").append(getLeafPos());
        sb.append(", lm=").append(getLM()).append("}");
        return sb.toString();
    }
}

