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

public class Position {
    
    private LayoutManager layoutManager;

    public Position(LayoutManager lm) {
        layoutManager = lm;
    }

    public LayoutManager getLM() {
        return layoutManager;
    }

    /**
     * Overridden by NonLeafPosition to return the Position of its
     * child LM.
     */
    public Position getPosition() {
        return null;
    }
    
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Position");
        if (getLM() != null) {
            sb.append(" {");
            sb.append(getLM());
            sb.append("}");
        }
        return sb.toString();
    }
}

