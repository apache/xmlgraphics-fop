/*
 * $Id$
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
 */
package org.apache.fop.fo.properties;

import java.util.HashMap;

import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.expr.PropertyException;

public class DominantBaseline extends Property  {
    public static final int dataTypes = AUTO | ENUM | INHERIT;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = FORMATTING;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = AUTO_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int USE_SCRIPT = 1;
    public static final int NO_CHANGE = 2;
    public static final int RESET_SIZE = 3;
    public static final int IDEOGRAPHIC = 4;
    public static final int ALPHABETIC = 5;
    public static final int HANGING = 6;
    public static final int MATHEMATICAL = 7;
    public static final int CENTRAL = 8;
    public static final int MIDDLE = 9;
    public static final int TEXT_AFTER_EDGE = 10;
    public static final int TEXT_BEFORE_EDGE = 11;
    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }


    private static final String[] rwEnums = {
        null
        ,"use-script"
        ,"no-change"
        ,"reset-size"
        ,"ideographic"
        ,"alphabetic"
        ,"hanging"
        ,"mathematical"
        ,"central"
        ,"middle"
        ,"test-after-edge"
        ,"text-before-edge"
    };
    private static final HashMap rwEnumHash;
    static {
        rwEnumHash = new HashMap((int)(rwEnums.length / 0.75) + 1);
        for (int i = 1; i < rwEnums.length; i++ ) {
            rwEnumHash.put((Object)rwEnums[i],
                                (Object) Ints.consts.get(i));
        }
    }
    public int getEnumIndex(String enum)
        throws PropertyException
    {
        Integer ii = (Integer)(rwEnumHash.get(enum));
        if (ii == null)
            throw new PropertyException("Unknown enum value: " + enum);
        return ii.intValue();
    }
    public String getEnumText(int index)
        throws PropertyException
    {
        if (index < 1 || index >= rwEnums.length)
            throw new PropertyException("index out of range: " + index);
        return rwEnums[index];
    }
}

