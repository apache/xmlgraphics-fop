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

/* $Id$ */
 
package org.apache.fop.layoutmgr;

public class Position {
    
    private LayoutManager layoutManager;
    private int index = -1;

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
    
    public boolean generatesAreas() {
        return false;
    }
    
    public void setIndex(int value) {
        this.index = value;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public String getShortLMName() {
        if (getLM() != null) {
            String lm = getLM().toString();
            int idx = lm.lastIndexOf('.');
            if (idx >= 0 && lm.indexOf('@') > 0) {
                return(lm.substring(idx + 1));
            } else {
                return lm;
            }
        } else {
            return "null";
        }
    }
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Position:").append(getIndex()).append("(");
        sb.append(getShortLMName());
        sb.append(")");
        return sb.toString();
    }
}

