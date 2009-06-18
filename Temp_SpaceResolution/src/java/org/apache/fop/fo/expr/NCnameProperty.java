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

package org.apache.fop.fo.expr;

import org.apache.fop.fo.properties.Property;
import org.apache.fop.datatypes.ColorType;

/**
 * Class for handling NC Name objects
 */
public class NCnameProperty extends Property {

    private final String ncName;

    /**
     * Constructor
     * @param ncName string representing the ncName
     */
    public NCnameProperty(String ncName) {
        this.ncName = ncName;
    }

    /**
     * If a system color, return the corresponding value (appears to be
     * unimplemented)
     * @return ColorType object corresponding to the NCName
     * @throws PropertyException in all cases currently
     */
    public ColorType getColor() throws PropertyException {
        throw new PropertyException("Not a Color");
    }

    /**
     * @return the name as a String (should be specified with quotes!)
     */
    public String getString() {
        return this.ncName;
    }

    /**
     * @return the name as an Object.
     */
    public Object getObject() {
        return this.ncName;
    }
    
    /**
     * @return ncName for this
     */
    public String getNCname() {
        return this.ncName;
    }

}
