/*
 * Ints.java
 * $Id$
 *
 * Created: Sun Nov  4 13:24:25 2001
 * 
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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.datatypes;

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.datastructs.ROIntegerArray;

/**
 * Data class of pre-initialised Integer objects.
 */

public class Ints {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    /**
     * An <tt>ROIntegerArray</tt> of Integer object constants corresponding
     * to the set of property index values.
     * @see org.apache.fop.fo.PropNames
     */
    public static final ROIntegerArray consts;
    /**
     * An <tt>ROIntegerArray</tt> of Integer object constants corresponding
     * to the set of flow object index values.
     * @see org.apache.fop.fo.FObjectNames
     */
    public static final ROIntegerArray foconsts;
    private static final Integer[] constsAr;
    private static final Integer[] foconstsAr;
    static {
        int range = PropNames.LAST_PROPERTY_INDEX >= FObjectNames.LAST_FO ?
                PropNames.LAST_PROPERTY_INDEX : FObjectNames.LAST_FO;
        constsAr = new Integer[PropNames.LAST_PROPERTY_INDEX + 1];
        for (int i = 0; i <= PropNames.LAST_PROPERTY_INDEX; i++) {
            constsAr[i] = new Integer(i);
        }
        consts = new ROIntegerArray(constsAr);
        foconstsAr = new Integer[FObjectNames.LAST_FO + 1];
        if (PropNames.LAST_PROPERTY_INDEX >= FObjectNames.LAST_FO) {
            System.arraycopy(constsAr, 0, foconstsAr, 0,
                             FObjectNames.LAST_FO + 1);
        }
        else {
            System.arraycopy(constsAr, 0, foconstsAr, 0,
                             PropNames.LAST_PROPERTY_INDEX + 1);
            for (int i = PropNames.LAST_PROPERTY_INDEX + 1;
                 i <= FObjectNames.LAST_FO; i++) {
                foconstsAr[i] = new Integer(i);
            }
        }
        foconsts = new ROIntegerArray(foconstsAr);
    }
    
    private Ints (){}
}
