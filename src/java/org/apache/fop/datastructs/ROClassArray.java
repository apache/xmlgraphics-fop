/**
 * Provides a Read-Only array of <tt>Class</tt>.
 * $Id$
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
 *
 */
package org.apache.fop.datastructs;

import java.lang.Class;

public class ROClassArray {
    
    private Class[] carray;

    /**
     * The length of the array; analogous to the <i>length</i> field of an
     * array.
     */
    public final int length;

    public ROClassArray(Class[] carray) {
        this.carray = carray;
        length = carray.length;
    }

    /**
     * @param index an <tt>int</tt>; the offset of the value to retrieve
     * @return the <tt>Class</tt> at the <i>index</i> offset
     */
    public Class get(int index) {
        return carray[index];
    }
}
