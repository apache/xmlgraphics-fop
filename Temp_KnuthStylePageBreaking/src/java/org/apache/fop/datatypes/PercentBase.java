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

package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.PropertyException;

/**
 * Interface for objects that can be used as base objects for percentage
 * computations
 */
public interface PercentBase {
    
    //Types of values to store in layoutDimension on FObj
    
    /** table units */
    LayoutDimension TABLE_UNITS = new LayoutDimension("table-units");
    /** Block IPD */
    LayoutDimension BLOCK_IPD = new LayoutDimension("block-ipd");
    /** Block BPD */
    LayoutDimension BLOCK_BPD = new LayoutDimension("block-bpd");
    /** Reference Area IPD */
    LayoutDimension REFERENCE_AREA_IPD = new LayoutDimension("reference-area-ipd");
    /** Reference Area BPD */
    LayoutDimension REFERENCE_AREA_BPD = new LayoutDimension("reference-area-bpd");
    /** Intrinsic width of an image or foreign-object */ 
    LayoutDimension IMAGE_INTRINSIC_WIDTH = new LayoutDimension("image-intrinsic-width");
    /** Intrinsic height of an image or foreign-object */ 
    LayoutDimension IMAGE_INTRINSIC_HEIGHT = new LayoutDimension("image-intrinsic-heigth");
    
    /**
     * Determines whether a numeric property is created or one with a percentage
     * base.
     * @return 0 for length, 1 for percentage
     */
    int getDimension();
    
    double getBaseValue();

    /**
     * @return the integer size in millipoints of the object (this will be used 
     * as the base to which a percentage will be applied to compute the length 
     * of the referencing item)
     * @throws PropertyException if a problem occurs during evaluation of this
     *     value.
     */
    int getBaseLength() throws PropertyException;
    
    /** Enum class for dimension types. */
    public class LayoutDimension {
        
        private String name;
        
        /**
         * Constructor to add a new named item.
         * @param name Name of the item.
         */
        protected LayoutDimension(String name) {
            this.name = name;
        }
        
        /** @see java.lang.Object#toString() */
        public String toString() {
            return super.toString() + "[" + name + "]";
        }
    }
}
