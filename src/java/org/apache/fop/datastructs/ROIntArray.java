/*
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
// $Id$
package org.apache.fop.datastructs;

/**
 * Provides a Read-Only <tt>int</tt> array.
 * 
 * @author pbw
 */
public class ROIntArray {
    
    private int[] iarray;

    /**
     * The length of the array; analogous to the <i>length</i> field of an
     * array.
     */
    public final int length;

    /**
     * Initialise with single integer array.  N.B. the ROIntArray is a
     * reference to the initialising array.  Any subsequent changes to the
     * contents of the initialising array will be reflected in the
     * ROIntArray.
     * @param iarray an <tt>int[]</tt>
     */
    public ROIntArray(int[] iarray) {
        this.iarray = iarray;
        length = iarray.length;
    }

    /**
     * Initialise with an array of arrays.  The elements of the argument
     * arrays are copied, in order, into the ROIntArray.
     * @param iarrays an <tt>int[][]</tt>
     */
    public ROIntArray(int[][] iarrays) {
        int i, j, k;
        i = 0;
        for (j = 0; j < iarrays.length; j++)
            for (k = 0; k < iarrays[j].length; k++)
                i++;
        length = i;
        iarray = new int[length];
        i = 0;
        for (j = 0; j < iarrays.length; j++)
            for (k = 0; k < iarrays[j].length; k++)
                iarray[i++] = iarrays[j][k];
    }

    /**
     * @param index an <tt>int</tt>; the offset of the value to retrieve
     * @return the <tt>int</tt> at the <i>index</i> offset
     */
    public int get(int index) {
        return iarray[index];
    }
    
    /**
     * @return a copy of the array
     */
    public int[] copyarray() {
        int[] tmp = null;
        System.arraycopy(iarray, 0, tmp, 0, iarray.length);
        return tmp;
    }
}
