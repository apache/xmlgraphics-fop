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
 
package org.apache.fop.fo.properties;


/**
 * A length quantity in XSL which is specified as "auto".
 */
public class AutoLength extends LengthProperty {

    /**
     * @see org.apache.fop.datatypes.Length#isAuto()
     */
    public boolean isAuto() {
        return true;
    }

    // Should we do something intelligent here to set the actual size?
    // Would need a reference object!
    //    protected void computeValue() {
    //    }

    public boolean isAbsolute() {
        return false;
    }
    /**
     * Returns the length in 1/1000ths of a point (millipoints)
     * @return the length in millipoints
     */
    public int getValue() {
        return 0;
    }

    /**
     * Returns the value as numeric.
     * @return the length in millipoints
     */
    public double getNumericValue() {
        return 0;
    }

    /**
     * @see org.apache.fop.fo.properties.Property#getString()
     */
    public String getString() {
        return "auto";
    }

}
