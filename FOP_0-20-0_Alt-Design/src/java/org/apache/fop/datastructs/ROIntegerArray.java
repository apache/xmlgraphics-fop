/**
 * Provides a Read-Only <tt>Integer</tt> array.
 *
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
// $Id$
package org.apache.fop.datastructs;

import java.util.List;
import java.util.Collections;
import java.util.Arrays;

public class ROIntegerArray {
    
    private Integer[] iarray;

    /**
     * The length of the array; analogous to the <i>length</i> field of an
     * array.
     */
    public final int length;

    public ROIntegerArray(Integer[] iarray) {
        this.iarray = iarray;
        length = iarray.length;
    }

    /**
     * @param index an <tt>int</tt>; the offset of the value to retrieve
     * @return the <tt>Integer</tt> at the <i>index</i> offset
     */
    public Integer get(int index) {
        return iarray[index];
    }

    public List immutableList () {
        return Collections.unmodifiableList(Arrays.asList(iarray));
    }
}
