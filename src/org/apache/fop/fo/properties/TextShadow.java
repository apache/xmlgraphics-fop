package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.ShadowEffect;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.properties.ColorNonTransparent;

import java.util.Iterator;

public class TextShadow extends ColorNonTransparent  {
    public static final int dataTypes = COMPLEX | NONE | INHERIT;
    public static final int traitMapping = RENDERING;
    public static final int initialValueType = NONE_IT;
    public static final int inherited = NO;


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
    public /**/static/**/ PropertyValue refineParsing
                                    (FONode foNode, PropertyValue list)
                    throws PropertyException
    {
        int property = list.getProperty();
        if ( ! (list instanceof PropertyValueList)) {
            return Property.refineParsing(foNode, list);
        }
        if (((PropertyValueList)list).size() == 0)
            throw new PropertyException
                ("text-shadow requires PropertyValueList of effects");
        PropertyValueList newlist = new PropertyValueList(property);
        Iterator effects = ((PropertyValueList)list).iterator();
        while (effects.hasNext()) {
            newlist.add(new ShadowEffect(property,
                        (PropertyValueList)(effects.next())));
        }
        return newlist;
    }

}

