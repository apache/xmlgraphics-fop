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
 
package org.apache.fop.datatypes;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.Property;

/**
 * This interface is used as a base for compound datatypes.
 */
public interface CompoundDatatype extends Constants {
    
    /**
     * Sets a component of the compound datatype.
     * @param Constants ID of the component
     * @param cmpnValue value of the component
     * @param bIsDefault Indicates if it's the default value
     */
    void setComponent(int cmpId, Property cmpnValue, boolean bIsDefault);

    /**
     * Returns a component of the compound datatype.
     * @param Constants ID of the component
     * @return the value of the component
     */
    Property getComponent(int cmpId);
}
