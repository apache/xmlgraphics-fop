package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.FONode;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.UriType;
import org.apache.fop.fo.properties.Property;

import java.util.Iterator;

public class SourceDocument extends Property  {
    public static final int dataTypes =
                            COMPLEX | URI_SPECIFICATION | NONE | INHERIT;
    public static final int traitMapping = RENDERING;
    public static final int initialValueType = NONE_IT;
    public static final int inherited = NO;

    /*
     * @param foNode - the <tt>FONode</tt> being built
     * @param list <tt>PropertyValue</tt> returned by the parser
     * @return <tt>PropertyValue</tt> the verified value
     */
    public /**/static/**/ PropertyValue refineParsing
                                    (FONode foNode, PropertyValue list)
                    throws PropertyException
    {
        if ( ! (list instanceof PropertyValueList))
                            return Property.refineParsing(foNode, list);
        // Confirm that the list contains only UriType elements
        Iterator iter = ((PropertyValueList)list).iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if ( ! (obj instanceof UriType))
                throw new PropertyException
                    ("source-document requires a list of uris");
        }
        return list;
    }
}

