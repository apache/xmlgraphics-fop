package org.apache.fop.datatypes;

import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.expr.PropertyValueList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.PropNames;

import java.util.Iterator;

/*
 * ShadowEffect.java
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Class to represent ShadowEffect values.  This class is a holder for a
 * set of PropertyValue objects, and will be placed in a PropertyValueList,
 * as text shadow effects are specified in a list. See 7.16.5.
 */

public class ShadowEffect extends AbstractPropertyValue {

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
        super(property);
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
        this(PropertyConsts.getPropertyIndex(propertyName), list);
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
                     PropertyConsts.getEnumIndex
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
