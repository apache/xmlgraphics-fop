package org.apache.fop.datatypes;

import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.AbstractPropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.PropNames;

import java.util.Iterator;

/*
 * ShadowEffect.java
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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Class to represent ShadowEffect values.  This class is a holder for a
 * set of PropertyValue objects, and will be placed in a PropertyValueList,
 * as text shadow effects are specified in a list. See 7.16.5.
 */

public class ShadowEffect extends AbstractPropertyValue {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * The shadow's "horizontal distance to the right of the text" (mandatory).
     */
    private Numeric inlineOffset;
    
    /**
     * The shadow's "vertical distance below the text" (mandatory).
     */
    private Numeric blockOffset;

    /**
     * The shadow's blur radius (optional)
     */
    private Numeric blurRadius;

    /**
     * The shadow's color (optional)
     */
    private ColorType color;

    /**
     * Construct a <i>ShadowEffect</i> from a given <tt>PropertyValueList</tt>.
     * An individual shadow effect is specified as a list comprising a
     * mandatory pair of <tt>Length</tt>s, the inline-progression offset and
     * the block-progression offset, with an otpional third <tt>Length</tt>
     * for the blur radius.  The shadow effect may optionally include a
     * color value, specified as a <tt>ColorType</tt>.  The <tt>ColorType</tt>
     * may precede or follow the <tt>Length</tt> specifiers.
     * @param property the index of the property with which this value
     * is associated.
     * @param list the <tt>PropertyValueList</tt> containing details of one
     * shadow effect
     */
    public ShadowEffect(int property, PropertyValueList list)
        throws PropertyException
    {
        super(property, PropertyValue.SHADOW_EFFECT);
        Object entry;
        Iterator entries = list.iterator();
        switch (list.size()) {
        case 2:
            // Must be the inline and block offsets
            setInlineAndBlock(entries);
            break;
        case 3:
            // Must have inline and block offsets; may have blur radius or
            //  a colour specifier
            if (list.getFirst() instanceof Numeric) {
                if (list.getLast() instanceof Numeric) {
                    setInlineBlockAndBlur(entries);
                } else { // last element must be a color
                    setInlineAndBlock(entries);
                    setColor(entries);
                }
            }
            else { // First entry is not Numeric; has to be color
                setColor(entries);
                setInlineAndBlock(entries);
            }
            break;
        case 4:
            // Must have inline and block offsets, blur radius and colour
            //  specifier
            if (list.getFirst() instanceof Numeric) {
                setInlineBlockAndBlur(entries);
                setColor(entries);
            }
            else { // First entry is not Numeric; has to be color
                setColor(entries);
                setInlineBlockAndBlur(entries);
            }
            break;
        default:
            throw new PropertyException
                    ("Invalid number of elements in ShadowEffect: "
                     + list.size());
        }
    }

    /**
     * Construct a <i>ShadowEffect</i> from a given <tt>PropertyValueList</tt>.
     * @param propertyName the name of the property with which this value
     * is associated.
     * @param list the <tt>PropertyValueList</tt> containing details of one
     * shadow effect
     */
    public ShadowEffect(String propertyName, PropertyValueList list)
        throws PropertyException
    {
        this(PropNames.getPropertyIndex(propertyName), list);
    }

    /**
     * Pick up two <tt>Numeric</tt> entries through the <tt>Iterator</tt>
     * and assign them to the inlineOffset and blockOffset
     * @param entries an <tt>Iterator</tt> already initialised elsewhere
     */
    private void setInlineAndBlock(Iterator entries)
            throws PropertyException
    {
        Object entry;
        entry = entries.next();
        if (! (entry instanceof Numeric))
            throw new PropertyException
                    ("Numeric value expected for text-shadow");
        inlineOffset = (Numeric)entry;
        entry = entries.next();
        if (! (entry instanceof Numeric))
            throw new PropertyException
                    ("Numeric value expected for text-shadow");
        blockOffset = (Numeric)entry;
    }

    /**
     * Pick up three <tt>Numeric</tt> entries through the <tt>Iterator</tt>
     * and assign them to the inlineOffset, blockOffset and blurRadius
     * @param entries an <tt>Iterator</tt> already initialised elsewhere
     */
    private void setInlineBlockAndBlur(Iterator entries)
            throws PropertyException
    {
        Object entry;
        setInlineAndBlock(entries);
        entry = entries.next();
        if (! (entry instanceof Numeric))
            throw new PropertyException
                    ("Numeric blur radius value expected for text-shadow");
        blurRadius = (Numeric)entry;
    }

    /**
     * Set the shadow color from the next entry returned by the entries
     * iterator.  A color entry must be either a <tt>ColorType</tt> already,
     * or an <tt>NCName</tt> containing one of the standard XSL color
     * keywords.
     * @param entries an <tt>Iterator</tt>.
     */
    private void setColor(Iterator entries) throws PropertyException {
        Object entry;
        entry = entries.next();
        if (entry instanceof ColorType) {
            color = (ColorType)entry;
        } else if (entry instanceof NCName) {
            color = new ColorType
                    (property,
                     propertyConsts.getEnumIndex
                     (PropNames.TEXT_SHADOW, ((NCName)entry).getNCName()));
        }
    }

    /**
     * Validate this <i>ShadowEffect</i>.  Check that it is allowed on the
     * associated property.  A <i>ShadowEffect</i> may also encode a single
     * character; i.e. a <tt>&lt;character&gt;</tt> type.  If the
     * validation against <i>LITERAL</i> fails, try <i>CHARACTER_T</i>.
     */
    public void validate() throws PropertyException {
        if (property != PropNames.TEXT_SHADOW)
            throw new PropertyException
                    ("ShadowEffects only valid for text-shadow'");
    }

}
