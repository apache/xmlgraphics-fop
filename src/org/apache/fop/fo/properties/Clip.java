package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.AbstractPropertyValue;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.FONode;

import java.util.Iterator;

public class Clip extends Property  {
    public static final int dataTypes = AUTO | COMPLEX | INHERIT;
    public static final int traitMapping = RENDERING;
    public static final int initialValueType = AUTO_IT;
    public static final int inherited = NO;

    /*
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> being built
     * @param value <tt>PropertyValue</tt> returned by the parser
     * @return <tt>PropertyValue</tt> the verified value
     */
    public /*static*/ PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
                    throws PropertyException
    {
        int type = value.getType();
        if (type == PropertyValue.INHERIT || type == PropertyValue.AUTO)
            return value;
        if (type != PropertyValue.LIST)
            throw new PropertyException
                ("clip: <shape> requires 4 <length> or <auto> args");
        PropertyValueList list = (PropertyValueList) value;
        if (list.size() != 4) throw new PropertyException
                ("clip: <shape> requires 4 lengths");
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if ( obj instanceof AbstractPropertyValue)  {
                AbstractPropertyValue pv = (AbstractPropertyValue)obj;
                if (pv.type == PropertyValue.AUTO ||
                    (pv.type == PropertyValue.NUMERIC &&
                        ((Numeric)pv).isLength())
                ) continue;
            }
            throw new PropertyException
                    ("clip: <shape> requires 4 <length> or <auto> args");
        }
        return value;
    }
}

