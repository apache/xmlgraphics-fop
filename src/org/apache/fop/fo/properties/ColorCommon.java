package org.apache.fop.fo.properties;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.datatypes.Ints;

/**
 * Pseudo-property class for common color values occurring in a
 * number of classes.
 */
public class ColorCommon extends Property  {
    public static final int AQUA = 1;
    public static final int BLACK = 2;
    public static final int BLUE = 3;
    public static final int FUSCHIA = 4;
    public static final int GRAY = 5;
    public static final int GREEN = 6;
    public static final int LIME = 7;
    public static final int MAROON = 8;
    public static final int NAVY = 9;
    public static final int OLIVE = 10;
    public static final int PURPLE = 11;
    public static final int RED = 12;
    public static final int SILVER = 13;
    public static final int TEAL = 14;
    public static final int WHITE = 15;
    public static final int YELLOW = 16;
    public static final int TRANSPARENT = 17;

    protected static final String[] rwEnums = {
	null
	,"aqua"
	,"black"
	,"blue"
	,"fuschia"
	,"gray"
	,"green"
	,"lime"
	,"maroon"
	,"navy"
	,"olive"
	,"purple"
	,"red"
	,"silver"
	,"teal"
	,"white"
	,"yellow"
	,"transparent"
    };

}

