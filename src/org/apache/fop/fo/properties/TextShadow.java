/*
 * $Id$
 *
 *  ============================================================================
 *                    The Apache Software License, Version 1.1
 *  ============================================================================
 *  
 *  Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without modifica-
 *  tion, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of  source code must  retain the above copyright  notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include  the following  acknowledgment:  "This product includes  software
 *     developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *     Alternately, this  acknowledgment may  appear in the software itself,  if
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *     endorse  or promote  products derived  from this  software without  prior
 *     written permission. For written permission, please contact
 *     apache@apache.org.
 *  
 *  5. Products  derived from this software may not  be called "Apache", nor may
 *     "Apache" appear  in their name,  without prior written permission  of the
 *     Apache Software Foundation.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 *  APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 *  DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 *  ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 *  (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  This software  consists of voluntary contributions made  by many individuals
 *  on  behalf of the Apache Software  Foundation and was  originally created by
 *  James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 *  Software Foundation, please see <http://www.apache.org/>.
 *  
 */
package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.ShadowEffect;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.ColorNonTransparent;

import java.util.Iterator;

public class TextShadow extends ColorNonTransparent  {
    public static final int dataTypes = COMPLEX | NONE | INHERIT;
    public static final int traitMapping = RENDERING;
    public static final int initialValueType = NONE_IT;
    public static final int inherited = COMPUTED;


    /**
     * Refine list of lists of individual shadow effects.
     * 'list' is a PropertyValueList containing, at the top level,
     * a sequence of PropertyValueLists, each representing a single
     * shadow effect.  A shadow effect must contain, at a minimum, an
     * inline-progression offset and a block-progression offset.  It may
     * also optionally contain a blur radius.  This set of two or three
     * <tt>Length</tt>s may be preceded or followed by a color
     * specifier.
     */
    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue list)
                    throws PropertyException
    {
        int property = list.getProperty();
        if ( ! (list instanceof PropertyValueList)) {
            return super.refineParsing(PropNames.TEXT_SHADOW, foNode, list);
        }
        if (((PropertyValueList)list).size() == 0)
            throw new PropertyException
                ("text-shadow requires PropertyValueList of effects");
        PropertyValueList newlist = new PropertyValueList(property);
        Iterator effects = ((PropertyValueList)list).iterator();
        while (effects.hasNext()) {
            newlist.add(new ShadowEffect(PropNames.TEXT_SHADOW,
                        (PropertyValueList)(effects.next())));
        }
        return newlist;
    }

}

