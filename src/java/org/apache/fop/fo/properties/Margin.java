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

import java.util.Iterator;

import org.apache.fop.datatypes.Auto;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.indirect.FromNearestSpecified;
import org.apache.fop.datatypes.indirect.FromParent;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.fo.expr.PropertyException;

public class Margin extends Property  {
    public static final int dataTypes = SHORTHAND;

    public int getDataTypes() {
        return dataTypes;
    }

    public static final int traitMapping = SHORTHAND_MAP;

    public int getTraitMapping() {
        return traitMapping;
    }

    public static final int initialValueType = NOTYPE_IT;

    public int getInitialValueType() {
        return initialValueType;
    }

    public static final int inherited = NO;

    public int getInherited() {
        return inherited;
    }


    /**
     * 'value' is a PropertyValueList or an individual PropertyValue.
     *
     * <p>If 'value' is an individual PropertyValue, it must contain
     * either
     *   a FromParent value,
     *   a FromNearestSpecified value,
     *   an Inherit value,
     *   an Auto value,
     *   a Numeric value which is a distance, rather than a number.
     *
     * <p>If 'value' is a PropertyValueList, it contains a list of
     * 2 to 4 length, percentage or auto values representing margin
     * dimensions.
     *
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.
     * The first element is a value for margin-top,
     * the second element is a value for margin-right,
     * the third element is a value for margin-bottom,
     * the fourth element is a value for margin-left.
     *
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @return <tt>PropertyValue</tt> the verified value
     */
    public PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
                throws PropertyException
    {
        if ( ! (value instanceof PropertyValueList)) {
            if (value instanceof Inherit
                || value instanceof FromParent
                || value instanceof FromNearestSpecified
                )
                return refineExpansionList(PropNames.MARGIN, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));
            // N.B.  Does this require further refinement?
            // Where is Auto expanded?
            return refineExpansionList(PropNames.MARGIN, foNode,
                        ShorthandPropSets.expandAndCopySHand
                                                (autoOrDistance(value)));
        } else {
            PropertyValueList list =
                            spaceSeparatedList((PropertyValueList)value);
            PropertyValue top, left, bottom, right;
            int count = list.size();
            if (count < 2 || count > 4)
                throw new PropertyException
                    ("margin list contains " + count + " items");

            Iterator margins = list.iterator();

            // There must be at least two
            top = autoOrDistance
                ((PropertyValue)(margins.next()), PropNames.MARGIN_TOP);
            right = autoOrDistance
                ((PropertyValue)(margins.next()), PropNames.MARGIN_RIGHT);
            try {
                bottom = (PropertyValue)(top.clone());
                bottom.setProperty(PropNames.MARGIN_BOTTOM);
                left = (PropertyValue)(right.clone());
                left.setProperty(PropNames.MARGIN_LEFT);
            } catch (CloneNotSupportedException cnse) {
                throw new PropertyException
                            (cnse.getMessage());
            }

            if (margins.hasNext())
                bottom = autoOrDistance((PropertyValue)(margins.next()),
                                                PropNames.MARGIN_BOTTOM);
            if (margins.hasNext())
                left = autoOrDistance((PropertyValue)(margins.next()),
                                                PropNames.MARGIN_LEFT);

            list = new PropertyValueList(PropNames.MARGIN);
            list.add(top);
            list.add(right);
            list.add(bottom);
            list.add(left);
            return list;
        }
    }

    /**
     * @param value <tt>PropertyValue</tt> the value being tested
     * @param property <tt>int</tt> property index of returned value
     * @return <tt>PropertyValue</t> the same value, with its property set
     *  to the <i>property</i> argument, if it is an Auto or a
     * <tt>Numeric</tt> distance
     * @exception PropertyException if the conditions are not met
     */
    private static PropertyValue autoOrDistance
                                    (PropertyValue value, int property)
        throws PropertyException
    {
        if (value instanceof Auto ||
            value instanceof Numeric && ((Numeric)value).isDistance()) {
            value.setProperty(property);
            return value;
        }
        else throw new PropertyException
            ("Value not 'Auto' or a distance for "
                + PropNames.getPropertyName(value.getProperty()));
    }

    /**
     * @param value <tt>PropertyValue</tt> the value being tested
     * @return <tt>PropertyValue</t> the same value if it is an Auto or a
     * <tt>Numeric</tt> distance
     * @exception PropertyException if the conditions are not met
     */
    private static PropertyValue autoOrDistance(PropertyValue value)
        throws PropertyException
    {
        return autoOrDistance(value, value.getProperty());
    }
}

