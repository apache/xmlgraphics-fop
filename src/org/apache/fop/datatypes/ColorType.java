/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

import java.util.HashMap;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.expr.AbstractPropertyValue;

/**
 * A base datatype class; colour in XSL
 */
public class ColorType extends AbstractPropertyValue {

    public static final int
        RED = 0
     ,GREEN = 1
      ,BLUE = 2
     ,ALPHA = 3
            ;

    /**
     * A four-element array of float indexed by the <tt>static final int</tt>
     * constants RED, GREEN, BLUE and ALPHA.  It contains the normalized
     * RGB-ALPHA values of the current colour; i.e. all of the values are
     * between 0 and 1 inclusive.
     */
    protected float color[] = {0f, 0f, 0f, 0f};

    /**
     * @param property an <tt>int</tt> containing the property index.
     * @param red a <tt>float</tt> containing the normalized
     * (0 <= red <= 1) red component.
     * @param green a <tt>float</tt> containing the normalized
     * (0 <= green <= 1) green component.
     * @param blue a <tt>float</tt> containing the normalized
     * (0 <= blue <= 1) blue component.
     */
    public ColorType(int property, float red, float green, float blue)
        throws PropertyException
    {
        super(property);
        color[RED] = red;
        color[GREEN] = green;
        color[BLUE] = blue;
    }

    /**
     * @param propertyName a <tt>String</tt> containing the property name.
     * @param red a <tt>float</tt> containing the normalized
     * (0 <= red <= 1) red component.
     * @param green a <tt>float</tt> containing the normalized
     * (0 <= green <= 1) green component.
     * @param blue a <tt>float</tt> containing the normalized
     * (0 <= blue <= 1) blue component.
     */
    public ColorType(String propertyName, float red, float green, float blue)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName),
                red, green, blue);
    }

    /**
     * @param property an <tt>int</tt> containing the property index.
     * @param red an <tt>int</tt> containing the un-normalized
     * red component.
     * @param green an <tt>int</tt> containing the un-normalized
     * green component.
     * @param blue an <tt>int</tt> containing the un-normalized
     * blue component.
     */
    public ColorType(int property, int red, int green, int blue)
        throws PropertyException
    {
        super(property);
        if (red > 255) red = 255;
        if (green > 255) green = 255;
        if (blue > 255) blue = 255;
        if (red < 0) red = 0;
        if (green < 0) green = 0;
        if (blue < 0) blue = 0;
        color[RED] = red/255f;
        color[GREEN] = green/255f;
        color[BLUE] = blue/255f;
    }

    /**
     * @param propertyName a <tt>String</tt> containing the property name.
     * @param red an <tt>int</tt> containing the un-normalized
     * red component.
     * @param green an <tt>int</tt> containing the un-normalized
     * green component.
     * @param blue an <tt>int</tt> containing the un-normalized
     * blue component.
     */
    public ColorType(String propertyName, int red, int green, int blue)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName),
                red, green, blue);
    }

    /**
     * @param property an <tt>int</tt> containing the property index.
     * @param color a <tt>float[]</tt> of four components containing
     * the normalized (0 <= n <= 1) red, green, blue and alpha components.
     */
    public ColorType(int property, float[] color) throws PropertyException {
        super(property);
        if (color.length != 4) throw new PropertyException
                ("Attempt to construct a ColorType with array of "
                    + color.length + " elements.");
        this.color = (float[])(color.clone());
    }

    /**
     * @param propertyName a <tt>String</tt> containing the property name.
     * @param color a <tt>float[]</tt> of four components containing
     * the normalized (0 <= n <= 1) red, green, blue and alpha components.
     */
    public ColorType(String propertyName, float[] color)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName), color);
    }

    /**
     * Construct a colour given a particular String specifying either a
     * #RGB or #RRGGBB three-component hexadecimal colour specifier or
     * a system color name.  N.B. To disambiguate color named generated
     * by the system-color() core function and the standard enumerated
     * colors, the <tt>String</tt> argument form is reserved for
     * system-color() and the <tt>int</tt> for is reserved for the enumeration
     * index.
     * @param property an <tt>int</tt>; the property index.
     * @param value a <tt>String</tt>; the system color name.
     */
    public ColorType(int property, String value) throws PropertyException {
        super(property);
        if (value.startsWith("#")) {
            try {
                if (value.length() == 4) {
                    // Range is 0 - F.  Convert to a float in the range
                    // 0 - 1 by dividing by 15
                    color[RED] = Integer.parseInt(value.substring(1, 2), 16)
                               / 15f;
                    color[GREEN] = Integer.parseInt(value.substring(2, 3), 16)
                                 / 15f;
                    color[BLUE] = Integer.parseInt(value.substring(3), 16)
                                / 15f;
                } else if (value.length() == 7) {
                    // Range is 0 - FF.  Convert to a float in the range
                    // 0 - 1 by dividing by 255
                    color[RED] = Integer.parseInt(value.substring(1, 3), 16)
                               / 255f;
                    color[GREEN] = Integer.parseInt(value.substring(3, 5), 16)
                                 / 255f;
                    color[BLUE] = Integer.parseInt(value.substring(5), 16)
                                / 255f;
                } else {
                    throw new PropertyException
                        ("Unknown colour format. Must be #RGB or #RRGGBB");
                }
            } catch (Exception e) {
                throw new PropertyException
                        ("Unknown colour format. Must be #RGB or #RRGGBB");
            }
        } else {
            String colorName = value.toLowerCase();
            if (colorName.equals("transparent")) {
                color[ALPHA] = 1;
            } else {
                color = (float[])(getSystemColor(colorName).clone());
            }
        }
    }

    /**
     * Construct a colour given a particular String specifying either a
     * #RGB or #RRGGBB three-component hexadecimal colour specifier or
     * a system color name.  N.B. To disambiguate color named generated
     * by the system-color() core function and the standard enumerated
     * colors, the <tt>String</tt> argument form is reserved for
     * system-color() and the <tt>int</tt> for is reserved for the enumeration
     * index.
     * @param propertyName a <tt>String</tt>; the property name.
     * @param value a <tt>String</tt>; the system color name.
     */
    public ColorType(String propertyName, String value)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName), value);
    }

    /**
     * Construct a colour given the index of a standard XSL/HTML enumerated
     * colour.
     * @param property an <tt>int</tt>; the property index.
     * @param colorEnum an <tt>int</tt>; the enumeration index. 
     */
    public ColorType(int property, int colorEnum) throws PropertyException {
        super(property);
        String enumText = PropertyConsts.getEnumText(property, colorEnum);
        color = (float[])(getStandardColor(enumText).clone());
    }

    /**
     * Construct a colour given the index of a standard XSL/HTML enumerated
     * colour.
     * @param propertyName a <tt>String</tt>; the property name.
     * @param colorEnum an <tt>int</tt>; the enumeration index. 
     */
    public ColorType(String propertyName, int colorEnum)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName), colorEnum);
    }

    /**
     * @return a <tt>float[4]</tt> containing the red, green, blue and alpha
     * components of this color.
     */
    public float[] color() {
        return color;
    }

    public float blue() {
        return color[BLUE];
    }

    public float green() {
        return color[GREEN];
    }

    public float red() {
        return color[RED];
    }

    public float alpha() {
        return color[ALPHA];
    }

    /**
     * Retrieve the components of a standard XSL/HTML color from the
     * <i>standardColors</i> <tt>HashMap</tt>.
     * @param name a <tt>String</tt>; the name of the standard color.
     * @return a <tt>float[4]</tt> containing the RGB information for
     * that color.
     */
    public static float[] getStandardColor(String name)
        throws PropertyException
    {
        return (float[])standardColors.get(name);
    }

    /**
     * Retrieve the components of a standard XSL/HTML color from the
     * <i>systemColors</i> and <i>normalizedSystemColors</i>
     * <tt>HashMap</tt>s.
     * @param name a <tt>String</tt>; the name of the standard color.
     * @return a <tt>float[4]</tt> containing the RGB information for
     * that color.
     */
    public static synchronized float[] getSystemColor(String name)
        throws PropertyException
    {
        float syscolor[];
        int rgbcolor[];
        if ((syscolor = (float[])normalizedSystemColors.get(name)) != null)
            return syscolor;
        if ((rgbcolor = (int[])systemColors.get(name)) != null) {
            syscolor = new float[] {
                rgbcolor[RED]/255f,
                rgbcolor[GREEN]/255f,
                rgbcolor[BLUE]/255f,
                0f
            };
            normalizedSystemColors.put(name, syscolor);
            return syscolor;
        }
        else throw new PropertyException("Unknown system color: " + name);
    }

    /**
     * A <tt>HashMap</tt> containing four-element arrays of <tt>float</tt>
     * with the normalized RGB values of standard colours, indexed by the
     * name of the color.
     * @return a <tt>float[4]</tt> array representing the normalized color.
     */
    private static HashMap standardColors;
    static {
        standardColors = new HashMap(16);
        standardColors.put
                ("black",   new float[] {0f, 0f, 0f, 0f});
        standardColors.put
                ("silver",  new float[] {0xC0/255f, 0xC0/255f, 0xC0/255f, 0f});
        standardColors.put
                ("gray",    new float[] {0x80/255f, 0x80/255f, 0x80/255f, 0f});
        standardColors.put
                ("white",   new float[] {1f, 1f, 1f, 0f});
        standardColors.put
                ("maroon",  new float[] {0x80/255f, 0f, 0f, 0f});
        standardColors.put
                ("red",     new float[] {1f, 0f, 0f, 0f});
        standardColors.put
                ("purple",  new float[] {0x80/255f, 0f, 0x80/255f, 0f});
        standardColors.put
                ("fuchsia", new float[] {1f, 0f, 1f, 0f});
        standardColors.put
                ("green",   new float[] {0f, 0x80/255f, 0f, 0f});
        standardColors.put
                ("lime",    new float[] {0f, 1f, 0f, 0f});
        standardColors.put
                ("olive",   new float[] {0x80/255f, 0x80/255f, 0f, 0f});
        standardColors.put
                ("yellow",  new float[] {1f, 1f, 0f, 0f});
        standardColors.put
                ("navy",    new float[] {0f, 0f, 0x80/255f, 0f});
        standardColors.put
                ("blue",    new float[] {0f, 0f, 1f, 0f});
        standardColors.put
                ("teal",    new float[] {0f, 0x80/255f, 0x80/255f, 0f});
        standardColors.put
                ("aqua",    new float[] {0f, 1f, 1f, 0f});
    }

    /**
     * A <tt>HashMap</tt> containing four-element arrays of <tt>float</tt>
     * with the normalized RGB values of system colours, indexed by the
     * system name of the color.  This array is referred to when system color
     * references are encountered.  If the color is in the HashMap,
     * a reference to the array is returned.  If not, the <i>systemColors</i>
     * HashMap is checked.  If the color exists there, it is normailized,
     * added to this HashMap, and a reference to the new array is returned.
     * @return a <tt>float[4]</tt> array representing the normalized color.
     */
    private static HashMap normalizedSystemColors = new HashMap();

    public static final HashMap systemColors;
    static {
        systemColors = new HashMap();
        systemColors.put("aliceblue", new int[] {240, 248, 255});
        systemColors.put("antiquewhite1", new int[] {255, 239, 219});
        systemColors.put("antiquewhite2", new int[] {238, 223, 204});
        systemColors.put("antiquewhite3", new int[] {205, 192, 176});
        systemColors.put("antiquewhite4", new int[] {139, 131, 120});
        systemColors.put("antiquewhite", new int[] {250, 235, 215});
        systemColors.put("aquamarine1", new int[] {127, 255, 212});
        systemColors.put("aquamarine2", new int[] {118, 238, 198});
        systemColors.put("aquamarine3", new int[] {102, 205, 170});
        systemColors.put("aquamarine4", new int[] {69, 139, 116});
        systemColors.put("aquamarine", new int[] {127, 255, 212});
        systemColors.put("azure1", new int[] {240, 255, 255});
        systemColors.put("azure2", new int[] {224, 238, 238});
        systemColors.put("azure3", new int[] {193, 205, 205});
        systemColors.put("azure4", new int[] {131, 139, 139});
        systemColors.put("azure", new int[] {240, 255, 255});
        systemColors.put("beige", new int[] {245, 245, 220});
        systemColors.put("bisque1", new int[] {255, 228, 196});
        systemColors.put("bisque2", new int[] {238, 213, 183});
        systemColors.put("bisque3", new int[] {205, 183, 158});
        systemColors.put("bisque4", new int[] {139, 125, 107});
        systemColors.put("bisque", new int[] {255, 228, 196});
        systemColors.put("black", new int[] {0, 0, 0});
        systemColors.put("blanchedalmond", new int[] {255, 235, 205});
        systemColors.put("blue1", new int[] {0, 0, 255});
        systemColors.put("blue2", new int[] {0, 0, 238});
        systemColors.put("blue3", new int[] {0, 0, 205});
        systemColors.put("blue4", new int[] {0, 0, 139});
        systemColors.put("blue", new int[] {0, 0, 255});
        systemColors.put("blueviolet", new int[] {138, 43, 226});
        systemColors.put("brown1", new int[] {255, 64, 64});
        systemColors.put("brown2", new int[] {238, 59, 59});
        systemColors.put("brown3", new int[] {205, 51, 51});
        systemColors.put("brown4", new int[] {139, 35, 35});
        systemColors.put("brown", new int[] {165, 42, 42});
        systemColors.put("burlywood1", new int[] {255, 211, 155});
        systemColors.put("burlywood2", new int[] {238, 197, 145});
        systemColors.put("burlywood3", new int[] {205, 170, 125});
        systemColors.put("burlywood4", new int[] {139, 115, 85});
        systemColors.put("burlywood", new int[] {222, 184, 135});
        systemColors.put("cadetblue1", new int[] {152, 245, 255});
        systemColors.put("cadetblue2", new int[] {142, 229, 238});
        systemColors.put("cadetblue3", new int[] {122, 197, 205});
        systemColors.put("cadetblue4", new int[] {83, 134, 139});
        systemColors.put("cadetblue", new int[] {95, 158, 160});
        systemColors.put("chartreuse1", new int[] {127, 255, 0});
        systemColors.put("chartreuse2", new int[] {118, 238, 0});
        systemColors.put("chartreuse3", new int[] {102, 205, 0});
        systemColors.put("chartreuse4", new int[] {69, 139, 0});
        systemColors.put("chartreuse", new int[] {127, 255, 0});
        systemColors.put("chocolate1", new int[] {255, 127, 36});
        systemColors.put("chocolate2", new int[] {238, 118, 33});
        systemColors.put("chocolate3", new int[] {205, 102, 29});
        systemColors.put("chocolate4", new int[] {139, 69, 19});
        systemColors.put("chocolate", new int[] {210, 105, 30});
        systemColors.put("coral1", new int[] {255, 114, 86});
        systemColors.put("coral2", new int[] {238, 106, 80});
        systemColors.put("coral3", new int[] {205, 91, 69});
        systemColors.put("coral4", new int[] {139, 62, 47});
        systemColors.put("coral", new int[] {255, 127, 80});
        systemColors.put("cornflowerblue", new int[] {100, 149, 237});
        systemColors.put("cornsilk1", new int[] {255, 248, 220});
        systemColors.put("cornsilk2", new int[] {238, 232, 205});
        systemColors.put("cornsilk3", new int[] {205, 200, 177});
        systemColors.put("cornsilk4", new int[] {139, 136, 120});
        systemColors.put("cornsilk", new int[] {255, 248, 220});
        systemColors.put("cyan1", new int[] {0, 255, 255});
        systemColors.put("cyan2", new int[] {0, 238, 238});
        systemColors.put("cyan3", new int[] {0, 205, 205});
        systemColors.put("cyan4", new int[] {0, 139, 139});
        systemColors.put("cyan", new int[] {0, 255, 255});
        systemColors.put("darkblue", new int[] {0, 0, 139});
        systemColors.put("darkcyan", new int[] {0, 139, 139});
        systemColors.put("darkgoldenrod1", new int[] {255, 185, 15});
        systemColors.put("darkgoldenrod2", new int[] {238, 173, 14});
        systemColors.put("darkgoldenrod3", new int[] {205, 149, 12});
        systemColors.put("darkgoldenrod4", new int[] {139, 101, 8});
        systemColors.put("darkgoldenrod", new int[] {184, 134, 11});
        systemColors.put("darkgray", new int[] {169, 169, 169});
        systemColors.put("darkgreen", new int[] {0, 100, 0});
        systemColors.put("darkgrey", new int[] {169, 169, 169});
        systemColors.put("darkkhaki", new int[] {189, 183, 107});
        systemColors.put("darkmagenta", new int[] {139, 0, 139});
        systemColors.put("darkolivegreen1", new int[] {202, 255, 112});
        systemColors.put("darkolivegreen2", new int[] {188, 238, 104});
        systemColors.put("darkolivegreen3", new int[] {162, 205, 90});
        systemColors.put("darkolivegreen4", new int[] {110, 139, 61});
        systemColors.put("darkolivegreen", new int[] {85, 107, 47});
        systemColors.put("darkorange1", new int[] {255, 127, 0});
        systemColors.put("darkorange2", new int[] {238, 118, 0});
        systemColors.put("darkorange3", new int[] {205, 102, 0});
        systemColors.put("darkorange4", new int[] {139, 69, 0});
        systemColors.put("darkorange", new int[] {255, 140, 0});
        systemColors.put("darkorchid1", new int[] {191, 62, 255});
        systemColors.put("darkorchid2", new int[] {178, 58, 238});
        systemColors.put("darkorchid3", new int[] {154, 50, 205});
        systemColors.put("darkorchid4", new int[] {104, 34, 139});
        systemColors.put("darkorchid", new int[] {153, 50, 204});
        systemColors.put("darkred", new int[] {139, 0, 0});
        systemColors.put("darksalmon", new int[] {233, 150, 122});
        systemColors.put("darkseagreen1", new int[] {193, 255, 193});
        systemColors.put("darkseagreen2", new int[] {180, 238, 180});
        systemColors.put("darkseagreen3", new int[] {155, 205, 155});
        systemColors.put("darkseagreen4", new int[] {105, 139, 105});
        systemColors.put("darkseagreen", new int[] {143, 188, 143});
        systemColors.put("darkslateblue", new int[] {72, 61, 139});
        systemColors.put("darkslategray1", new int[] {151, 255, 255});
        systemColors.put("darkslategray2", new int[] {141, 238, 238});
        systemColors.put("darkslategray3", new int[] {121, 205, 205});
        systemColors.put("darkslategray4", new int[] {82, 139, 139});
        systemColors.put("darkslategray", new int[] {47, 79, 79});
        systemColors.put("darkslategrey", new int[] {47, 79, 79});
        systemColors.put("darkturquoise", new int[] {0, 206, 209});
        systemColors.put("darkviolet", new int[] {148, 0, 211});
        systemColors.put("deeppink1", new int[] {255, 20, 147});
        systemColors.put("deeppink2", new int[] {238, 18, 137});
        systemColors.put("deeppink3", new int[] {205, 16, 118});
        systemColors.put("deeppink4", new int[] {139, 10, 80});
        systemColors.put("deeppink", new int[] {255, 20, 147});
        systemColors.put("deepskyblue1", new int[] {0, 191, 255});
        systemColors.put("deepskyblue2", new int[] {0, 178, 238});
        systemColors.put("deepskyblue3", new int[] {0, 154, 205});
        systemColors.put("deepskyblue4", new int[] {0, 104, 139});
        systemColors.put("deepskyblue", new int[] {0, 191, 255});
        systemColors.put("dimgray", new int[] {105, 105, 105});
        systemColors.put("dimgrey", new int[] {105, 105, 105});
        systemColors.put("dodgerblue1", new int[] {30, 144, 255});
        systemColors.put("dodgerblue2", new int[] {28, 134, 238});
        systemColors.put("dodgerblue3", new int[] {24, 116, 205});
        systemColors.put("dodgerblue4", new int[] {16, 78, 139});
        systemColors.put("dodgerblue", new int[] {30, 144, 255});
        systemColors.put("firebrick1", new int[] {255, 48, 48});
        systemColors.put("firebrick2", new int[] {238, 44, 44});
        systemColors.put("firebrick3", new int[] {205, 38, 38});
        systemColors.put("firebrick4", new int[] {139, 26, 26});
        systemColors.put("firebrick", new int[] {178, 34, 34});
        systemColors.put("floralwhite", new int[] {255, 250, 240});
        systemColors.put("forestgreen", new int[] {34, 139, 34});
        systemColors.put("gainsboro", new int[] {220, 220, 220});
        systemColors.put("ghostwhite", new int[] {248, 248, 255});
        systemColors.put("gold1", new int[] {255, 215, 0});
        systemColors.put("gold2", new int[] {238, 201, 0});
        systemColors.put("gold3", new int[] {205, 173, 0});
        systemColors.put("gold4", new int[] {139, 117, 0});
        systemColors.put("goldenrod1", new int[] {255, 193, 37});
        systemColors.put("goldenrod2", new int[] {238, 180, 34});
        systemColors.put("goldenrod3", new int[] {205, 155, 29});
        systemColors.put("goldenrod4", new int[] {139, 105, 20});
        systemColors.put("goldenrod", new int[] {218, 165, 32});
        systemColors.put("gold", new int[] {255, 215, 0});
        systemColors.put("gray0", new int[] {0, 0, 0});
        systemColors.put("gray100", new int[] {255, 255, 255});
        systemColors.put("gray10", new int[] {26, 26, 26});
        systemColors.put("gray11", new int[] {28, 28, 28});
        systemColors.put("gray12", new int[] {31, 31, 31});
        systemColors.put("gray13", new int[] {33, 33, 33});
        systemColors.put("gray14", new int[] {36, 36, 36});
        systemColors.put("gray15", new int[] {38, 38, 38});
        systemColors.put("gray16", new int[] {41, 41, 41});
        systemColors.put("gray17", new int[] {43, 43, 43});
        systemColors.put("gray18", new int[] {46, 46, 46});
        systemColors.put("gray19", new int[] {48, 48, 48});
        systemColors.put("gray1", new int[] {3, 3, 3});
        systemColors.put("gray20", new int[] {51, 51, 51});
        systemColors.put("gray21", new int[] {54, 54, 54});
        systemColors.put("gray22", new int[] {56, 56, 56});
        systemColors.put("gray23", new int[] {59, 59, 59});
        systemColors.put("gray24", new int[] {61, 61, 61});
        systemColors.put("gray25", new int[] {64, 64, 64});
        systemColors.put("gray26", new int[] {66, 66, 66});
        systemColors.put("gray27", new int[] {69, 69, 69});
        systemColors.put("gray28", new int[] {71, 71, 71});
        systemColors.put("gray29", new int[] {74, 74, 74});
        systemColors.put("gray2", new int[] {5, 5, 5});
        systemColors.put("gray30", new int[] {77, 77, 77});
        systemColors.put("gray31", new int[] {79, 79, 79});
        systemColors.put("gray32", new int[] {82, 82, 82});
        systemColors.put("gray33", new int[] {84, 84, 84});
        systemColors.put("gray34", new int[] {87, 87, 87});
        systemColors.put("gray35", new int[] {89, 89, 89});
        systemColors.put("gray36", new int[] {92, 92, 92});
        systemColors.put("gray37", new int[] {94, 94, 94});
        systemColors.put("gray38", new int[] {97, 97, 97});
        systemColors.put("gray39", new int[] {99, 99, 99});
        systemColors.put("gray3", new int[] {8, 8, 8});
        systemColors.put("gray40", new int[] {102, 102, 102});
        systemColors.put("gray41", new int[] {105, 105, 105});
        systemColors.put("gray42", new int[] {107, 107, 107});
        systemColors.put("gray43", new int[] {110, 110, 110});
        systemColors.put("gray44", new int[] {112, 112, 112});
        systemColors.put("gray45", new int[] {115, 115, 115});
        systemColors.put("gray46", new int[] {117, 117, 117});
        systemColors.put("gray47", new int[] {120, 120, 120});
        systemColors.put("gray48", new int[] {122, 122, 122});
        systemColors.put("gray49", new int[] {125, 125, 125});
        systemColors.put("gray4", new int[] {10, 10, 10});
        systemColors.put("gray50", new int[] {127, 127, 127});
        systemColors.put("gray51", new int[] {130, 130, 130});
        systemColors.put("gray52", new int[] {133, 133, 133});
        systemColors.put("gray53", new int[] {135, 135, 135});
        systemColors.put("gray54", new int[] {138, 138, 138});
        systemColors.put("gray55", new int[] {140, 140, 140});
        systemColors.put("gray56", new int[] {143, 143, 143});
        systemColors.put("gray57", new int[] {145, 145, 145});
        systemColors.put("gray58", new int[] {148, 148, 148});
        systemColors.put("gray59", new int[] {150, 150, 150});
        systemColors.put("gray5", new int[] {13, 13, 13});
        systemColors.put("gray60", new int[] {153, 153, 153});
        systemColors.put("gray61", new int[] {156, 156, 156});
        systemColors.put("gray62", new int[] {158, 158, 158});
        systemColors.put("gray63", new int[] {161, 161, 161});
        systemColors.put("gray64", new int[] {163, 163, 163});
        systemColors.put("gray65", new int[] {166, 166, 166});
        systemColors.put("gray66", new int[] {168, 168, 168});
        systemColors.put("gray67", new int[] {171, 171, 171});
        systemColors.put("gray68", new int[] {173, 173, 173});
        systemColors.put("gray69", new int[] {176, 176, 176});
        systemColors.put("gray6", new int[] {15, 15, 15});
        systemColors.put("gray70", new int[] {179, 179, 179});
        systemColors.put("gray71", new int[] {181, 181, 181});
        systemColors.put("gray72", new int[] {184, 184, 184});
        systemColors.put("gray73", new int[] {186, 186, 186});
        systemColors.put("gray74", new int[] {189, 189, 189});
        systemColors.put("gray75", new int[] {191, 191, 191});
        systemColors.put("gray76", new int[] {194, 194, 194});
        systemColors.put("gray77", new int[] {196, 196, 196});
        systemColors.put("gray78", new int[] {199, 199, 199});
        systemColors.put("gray79", new int[] {201, 201, 201});
        systemColors.put("gray7", new int[] {18, 18, 18});
        systemColors.put("gray80", new int[] {204, 204, 204});
        systemColors.put("gray81", new int[] {207, 207, 207});
        systemColors.put("gray82", new int[] {209, 209, 209});
        systemColors.put("gray83", new int[] {212, 212, 212});
        systemColors.put("gray84", new int[] {214, 214, 214});
        systemColors.put("gray85", new int[] {217, 217, 217});
        systemColors.put("gray86", new int[] {219, 219, 219});
        systemColors.put("gray87", new int[] {222, 222, 222});
        systemColors.put("gray88", new int[] {224, 224, 224});
        systemColors.put("gray89", new int[] {227, 227, 227});
        systemColors.put("gray8", new int[] {20, 20, 20});
        systemColors.put("gray90", new int[] {229, 229, 229});
        systemColors.put("gray91", new int[] {232, 232, 232});
        systemColors.put("gray92", new int[] {235, 235, 235});
        systemColors.put("gray93", new int[] {237, 237, 237});
        systemColors.put("gray94", new int[] {240, 240, 240});
        systemColors.put("gray95", new int[] {242, 242, 242});
        systemColors.put("gray96", new int[] {245, 245, 245});
        systemColors.put("gray97", new int[] {247, 247, 247});
        systemColors.put("gray98", new int[] {250, 250, 250});
        systemColors.put("gray99", new int[] {252, 252, 252});
        systemColors.put("gray9", new int[] {23, 23, 23});
        systemColors.put("gray", new int[] {190, 190, 190});
        systemColors.put("green1", new int[] {0, 255, 0});
        systemColors.put("green2", new int[] {0, 238, 0});
        systemColors.put("green3", new int[] {0, 205, 0});
        systemColors.put("green4", new int[] {0, 139, 0});
        systemColors.put("green", new int[] {0, 255, 0});
        systemColors.put("greenyellow", new int[] {173, 255, 47});
        systemColors.put("grey0", new int[] {0, 0, 0});
        systemColors.put("grey100", new int[] {255, 255, 255});
        systemColors.put("grey10", new int[] {26, 26, 26});
        systemColors.put("grey11", new int[] {28, 28, 28});
        systemColors.put("grey12", new int[] {31, 31, 31});
        systemColors.put("grey13", new int[] {33, 33, 33});
        systemColors.put("grey14", new int[] {36, 36, 36});
        systemColors.put("grey15", new int[] {38, 38, 38});
        systemColors.put("grey16", new int[] {41, 41, 41});
        systemColors.put("grey17", new int[] {43, 43, 43});
        systemColors.put("grey18", new int[] {46, 46, 46});
        systemColors.put("grey19", new int[] {48, 48, 48});
        systemColors.put("grey1", new int[] {3, 3, 3});
        systemColors.put("grey20", new int[] {51, 51, 51});
        systemColors.put("grey21", new int[] {54, 54, 54});
        systemColors.put("grey22", new int[] {56, 56, 56});
        systemColors.put("grey23", new int[] {59, 59, 59});
        systemColors.put("grey24", new int[] {61, 61, 61});
        systemColors.put("grey25", new int[] {64, 64, 64});
        systemColors.put("grey26", new int[] {66, 66, 66});
        systemColors.put("grey27", new int[] {69, 69, 69});
        systemColors.put("grey28", new int[] {71, 71, 71});
        systemColors.put("grey29", new int[] {74, 74, 74});
        systemColors.put("grey2", new int[] {5, 5, 5});
        systemColors.put("grey30", new int[] {77, 77, 77});
        systemColors.put("grey31", new int[] {79, 79, 79});
        systemColors.put("grey32", new int[] {82, 82, 82});
        systemColors.put("grey33", new int[] {84, 84, 84});
        systemColors.put("grey34", new int[] {87, 87, 87});
        systemColors.put("grey35", new int[] {89, 89, 89});
        systemColors.put("grey36", new int[] {92, 92, 92});
        systemColors.put("grey37", new int[] {94, 94, 94});
        systemColors.put("grey38", new int[] {97, 97, 97});
        systemColors.put("grey39", new int[] {99, 99, 99});
        systemColors.put("grey3", new int[] {8, 8, 8});
        systemColors.put("grey40", new int[] {102, 102, 102});
        systemColors.put("grey41", new int[] {105, 105, 105});
        systemColors.put("grey42", new int[] {107, 107, 107});
        systemColors.put("grey43", new int[] {110, 110, 110});
        systemColors.put("grey44", new int[] {112, 112, 112});
        systemColors.put("grey45", new int[] {115, 115, 115});
        systemColors.put("grey46", new int[] {117, 117, 117});
        systemColors.put("grey47", new int[] {120, 120, 120});
        systemColors.put("grey48", new int[] {122, 122, 122});
        systemColors.put("grey49", new int[] {125, 125, 125});
        systemColors.put("grey4", new int[] {10, 10, 10});
        systemColors.put("grey50", new int[] {127, 127, 127});
        systemColors.put("grey51", new int[] {130, 130, 130});
        systemColors.put("grey52", new int[] {133, 133, 133});
        systemColors.put("grey53", new int[] {135, 135, 135});
        systemColors.put("grey54", new int[] {138, 138, 138});
        systemColors.put("grey55", new int[] {140, 140, 140});
        systemColors.put("grey56", new int[] {143, 143, 143});
        systemColors.put("grey57", new int[] {145, 145, 145});
        systemColors.put("grey58", new int[] {148, 148, 148});
        systemColors.put("grey59", new int[] {150, 150, 150});
        systemColors.put("grey5", new int[] {13, 13, 13});
        systemColors.put("grey60", new int[] {153, 153, 153});
        systemColors.put("grey61", new int[] {156, 156, 156});
        systemColors.put("grey62", new int[] {158, 158, 158});
        systemColors.put("grey63", new int[] {161, 161, 161});
        systemColors.put("grey64", new int[] {163, 163, 163});
        systemColors.put("grey65", new int[] {166, 166, 166});
        systemColors.put("grey66", new int[] {168, 168, 168});
        systemColors.put("grey67", new int[] {171, 171, 171});
        systemColors.put("grey68", new int[] {173, 173, 173});
        systemColors.put("grey69", new int[] {176, 176, 176});
        systemColors.put("grey6", new int[] {15, 15, 15});
        systemColors.put("grey70", new int[] {179, 179, 179});
        systemColors.put("grey71", new int[] {181, 181, 181});
        systemColors.put("grey72", new int[] {184, 184, 184});
        systemColors.put("grey73", new int[] {186, 186, 186});
        systemColors.put("grey74", new int[] {189, 189, 189});
        systemColors.put("grey75", new int[] {191, 191, 191});
        systemColors.put("grey76", new int[] {194, 194, 194});
        systemColors.put("grey77", new int[] {196, 196, 196});
        systemColors.put("grey78", new int[] {199, 199, 199});
        systemColors.put("grey79", new int[] {201, 201, 201});
        systemColors.put("grey7", new int[] {18, 18, 18});
        systemColors.put("grey80", new int[] {204, 204, 204});
        systemColors.put("grey81", new int[] {207, 207, 207});
        systemColors.put("grey82", new int[] {209, 209, 209});
        systemColors.put("grey83", new int[] {212, 212, 212});
        systemColors.put("grey84", new int[] {214, 214, 214});
        systemColors.put("grey85", new int[] {217, 217, 217});
        systemColors.put("grey86", new int[] {219, 219, 219});
        systemColors.put("grey87", new int[] {222, 222, 222});
        systemColors.put("grey88", new int[] {224, 224, 224});
        systemColors.put("grey89", new int[] {227, 227, 227});
        systemColors.put("grey8", new int[] {20, 20, 20});
        systemColors.put("grey90", new int[] {229, 229, 229});
        systemColors.put("grey91", new int[] {232, 232, 232});
        systemColors.put("grey92", new int[] {235, 235, 235});
        systemColors.put("grey93", new int[] {237, 237, 237});
        systemColors.put("grey94", new int[] {240, 240, 240});
        systemColors.put("grey95", new int[] {242, 242, 242});
        systemColors.put("grey96", new int[] {245, 245, 245});
        systemColors.put("grey97", new int[] {247, 247, 247});
        systemColors.put("grey98", new int[] {250, 250, 250});
        systemColors.put("grey99", new int[] {252, 252, 252});
        systemColors.put("grey9", new int[] {23, 23, 23});
        systemColors.put("grey", new int[] {190, 190, 190});
        systemColors.put("honeydew1", new int[] {240, 255, 240});
        systemColors.put("honeydew2", new int[] {224, 238, 224});
        systemColors.put("honeydew3", new int[] {193, 205, 193});
        systemColors.put("honeydew4", new int[] {131, 139, 131});
        systemColors.put("honeydew", new int[] {240, 255, 240});
        systemColors.put("hotpink1", new int[] {255, 110, 180});
        systemColors.put("hotpink2", new int[] {238, 106, 167});
        systemColors.put("hotpink3", new int[] {205, 96, 144});
        systemColors.put("hotpink4", new int[] {139, 58, 98});
        systemColors.put("hotpink", new int[] {255, 105, 180});
        systemColors.put("indianred1", new int[] {255, 106, 106});
        systemColors.put("indianred2", new int[] {238, 99, 99});
        systemColors.put("indianred3", new int[] {205, 85, 85});
        systemColors.put("indianred4", new int[] {139, 58, 58});
        systemColors.put("indianred", new int[] {205, 92, 92});
        systemColors.put("ivory1", new int[] {255, 255, 240});
        systemColors.put("ivory2", new int[] {238, 238, 224});
        systemColors.put("ivory3", new int[] {205, 205, 193});
        systemColors.put("ivory4", new int[] {139, 139, 131});
        systemColors.put("ivory", new int[] {255, 255, 240});
        systemColors.put("khaki1", new int[] {255, 246, 143});
        systemColors.put("khaki2", new int[] {238, 230, 133});
        systemColors.put("khaki3", new int[] {205, 198, 115});
        systemColors.put("khaki4", new int[] {139, 134, 78});
        systemColors.put("khaki", new int[] {240, 230, 140});
        systemColors.put("lavenderblush1", new int[] {255, 240, 245});
        systemColors.put("lavenderblush2", new int[] {238, 224, 229});
        systemColors.put("lavenderblush3", new int[] {205, 193, 197});
        systemColors.put("lavenderblush4", new int[] {139, 131, 134});
        systemColors.put("lavenderblush", new int[] {255, 240, 245});
        systemColors.put("lavender", new int[] {230, 230, 250});
        systemColors.put("lawngreen", new int[] {124, 252, 0});
        systemColors.put("lemonchiffon1", new int[] {255, 250, 205});
        systemColors.put("lemonchiffon2", new int[] {238, 233, 191});
        systemColors.put("lemonchiffon3", new int[] {205, 201, 165});
        systemColors.put("lemonchiffon4", new int[] {139, 137, 112});
        systemColors.put("lemonchiffon", new int[] {255, 250, 205});
        systemColors.put("lightblue1", new int[] {191, 239, 255});
        systemColors.put("lightblue2", new int[] {178, 223, 238});
        systemColors.put("lightblue3", new int[] {154, 192, 205});
        systemColors.put("lightblue4", new int[] {104, 131, 139});
        systemColors.put("lightblue", new int[] {173, 216, 230});
        systemColors.put("lightcoral", new int[] {240, 128, 128});
        systemColors.put("lightcyan1", new int[] {224, 255, 255});
        systemColors.put("lightcyan2", new int[] {209, 238, 238});
        systemColors.put("lightcyan3", new int[] {180, 205, 205});
        systemColors.put("lightcyan4", new int[] {122, 139, 139});
        systemColors.put("lightcyan", new int[] {224, 255, 255});
        systemColors.put("lightgoldenrod1", new int[] {255, 236, 139});
        systemColors.put("lightgoldenrod2", new int[] {238, 220, 130});
        systemColors.put("lightgoldenrod3", new int[] {205, 190, 112});
        systemColors.put("lightgoldenrod4", new int[] {139, 129, 76});
        systemColors.put("lightgoldenrod", new int[] {238, 221, 130});
        systemColors.put("lightgoldenrodyellow", new int[] {250, 250, 210});
        systemColors.put("lightgray", new int[] {211, 211, 211});
        systemColors.put("lightgreen", new int[] {144, 238, 144});
        systemColors.put("lightgrey", new int[] {211, 211, 211});
        systemColors.put("lightpink1", new int[] {255, 174, 185});
        systemColors.put("lightpink2", new int[] {238, 162, 173});
        systemColors.put("lightpink3", new int[] {205, 140, 149});
        systemColors.put("lightpink4", new int[] {139, 95, 101});
        systemColors.put("lightpink", new int[] {255, 182, 193});
        systemColors.put("lightsalmon1", new int[] {255, 160, 122});
        systemColors.put("lightsalmon2", new int[] {238, 149, 114});
        systemColors.put("lightsalmon3", new int[] {205, 129, 98});
        systemColors.put("lightsalmon4", new int[] {139, 87, 66});
        systemColors.put("lightsalmon", new int[] {255, 160, 122});
        systemColors.put("lightseagreen", new int[] {32, 178, 170});
        systemColors.put("lightskyblue1", new int[] {176, 226, 255});
        systemColors.put("lightskyblue2", new int[] {164, 211, 238});
        systemColors.put("lightskyblue3", new int[] {141, 182, 205});
        systemColors.put("lightskyblue4", new int[] {96, 123, 139});
        systemColors.put("lightskyblue", new int[] {135, 206, 250});
        systemColors.put("lightslateblue", new int[] {132, 112, 255});
        systemColors.put("lightslategray", new int[] {119, 136, 153});
        systemColors.put("lightslategrey", new int[] {119, 136, 153});
        systemColors.put("lightsteelblue1", new int[] {202, 225, 255});
        systemColors.put("lightsteelblue2", new int[] {188, 210, 238});
        systemColors.put("lightsteelblue3", new int[] {162, 181, 205});
        systemColors.put("lightsteelblue4", new int[] {110, 123, 139});
        systemColors.put("lightsteelblue", new int[] {176, 196, 222});
        systemColors.put("lightyellow1", new int[] {255, 255, 224});
        systemColors.put("lightyellow2", new int[] {238, 238, 209});
        systemColors.put("lightyellow3", new int[] {205, 205, 180});
        systemColors.put("lightyellow4", new int[] {139, 139, 122});
        systemColors.put("lightyellow", new int[] {255, 255, 224});
        systemColors.put("limegreen", new int[] {50, 205, 50});
        systemColors.put("linen", new int[] {250, 240, 230});
        systemColors.put("magenta1", new int[] {255, 0, 255});
        systemColors.put("magenta2", new int[] {238, 0, 238});
        systemColors.put("magenta3", new int[] {205, 0, 205});
        systemColors.put("magenta4", new int[] {139, 0, 139});
        systemColors.put("magenta", new int[] {255, 0, 255});
        systemColors.put("maroon1", new int[] {255, 52, 179});
        systemColors.put("maroon2", new int[] {238, 48, 167});
        systemColors.put("maroon3", new int[] {205, 41, 144});
        systemColors.put("maroon4", new int[] {139, 28, 98});
        systemColors.put("maroon", new int[] {176, 48, 96});
        systemColors.put("mediumaquamarine", new int[] {102, 205, 170});
        systemColors.put("mediumblue", new int[] {0, 0, 205});
        systemColors.put("mediumorchid1", new int[] {224, 102, 255});
        systemColors.put("mediumorchid2", new int[] {209, 95, 238});
        systemColors.put("mediumorchid3", new int[] {180, 82, 205});
        systemColors.put("mediumorchid4", new int[] {122, 55, 139});
        systemColors.put("mediumorchid", new int[] {186, 85, 211});
        systemColors.put("mediumpurple1", new int[] {171, 130, 255});
        systemColors.put("mediumpurple2", new int[] {159, 121, 238});
        systemColors.put("mediumpurple3", new int[] {137, 104, 205});
        systemColors.put("mediumpurple4", new int[] {93, 71, 139});
        systemColors.put("mediumpurple", new int[] {147, 112, 219});
        systemColors.put("mediumseagreen", new int[] {60, 179, 113});
        systemColors.put("mediumslateblue", new int[] {123, 104, 238});
        systemColors.put("mediumspringgreen", new int[] {0, 250, 154});
        systemColors.put("mediumturquoise", new int[] {72, 209, 204});
        systemColors.put("mediumvioletred", new int[] {199, 21, 133});
        systemColors.put("midnightblue", new int[] {25, 25, 112});
        systemColors.put("mintcream", new int[] {245, 255, 250});
        systemColors.put("mistyrose1", new int[] {255, 228, 225});
        systemColors.put("mistyrose2", new int[] {238, 213, 210});
        systemColors.put("mistyrose3", new int[] {205, 183, 181});
        systemColors.put("mistyrose4", new int[] {139, 125, 123});
        systemColors.put("mistyrose", new int[] {255, 228, 225});
        systemColors.put("moccasin", new int[] {255, 228, 181});
        systemColors.put("navajowhite1", new int[] {255, 222, 173});
        systemColors.put("navajowhite2", new int[] {238, 207, 161});
        systemColors.put("navajowhite3", new int[] {205, 179, 139});
        systemColors.put("navajowhite4", new int[] {139, 121, 94});
        systemColors.put("navajowhite", new int[] {255, 222, 173});
        systemColors.put("navyblue", new int[] {0, 0, 128});
        systemColors.put("navy", new int[] {0, 0, 128});
        systemColors.put("oldlace", new int[] {253, 245, 230});
        systemColors.put("olivedrab1", new int[] {192, 255, 62});
        systemColors.put("olivedrab2", new int[] {179, 238, 58});
        systemColors.put("olivedrab3", new int[] {154, 205, 50});
        systemColors.put("olivedrab4", new int[] {105, 139, 34});
        systemColors.put("olivedrab", new int[] {107, 142, 35});
        systemColors.put("orange1", new int[] {255, 165, 0});
        systemColors.put("orange2", new int[] {238, 154, 0});
        systemColors.put("orange3", new int[] {205, 133, 0});
        systemColors.put("orange4", new int[] {139, 90, 0});
        systemColors.put("orange", new int[] {255, 165, 0});
        systemColors.put("orangered1", new int[] {255, 69, 0});
        systemColors.put("orangered2", new int[] {238, 64, 0});
        systemColors.put("orangered3", new int[] {205, 55, 0});
        systemColors.put("orangered4", new int[] {139, 37, 0});
        systemColors.put("orangered", new int[] {255, 69, 0});
        systemColors.put("orchid1", new int[] {255, 131, 250});
        systemColors.put("orchid2", new int[] {238, 122, 233});
        systemColors.put("orchid3", new int[] {205, 105, 201});
        systemColors.put("orchid4", new int[] {139, 71, 137});
        systemColors.put("orchid", new int[] {218, 112, 214});
        systemColors.put("palegoldenrod", new int[] {238, 232, 170});
        systemColors.put("palegreen1", new int[] {154, 255, 154});
        systemColors.put("palegreen2", new int[] {144, 238, 144});
        systemColors.put("palegreen3", new int[] {124, 205, 124});
        systemColors.put("palegreen4", new int[] {84, 139, 84});
        systemColors.put("palegreen", new int[] {152, 251, 152});
        systemColors.put("paleturquoise1", new int[] {187, 255, 255});
        systemColors.put("paleturquoise2", new int[] {174, 238, 238});
        systemColors.put("paleturquoise3", new int[] {150, 205, 205});
        systemColors.put("paleturquoise4", new int[] {102, 139, 139});
        systemColors.put("paleturquoise", new int[] {175, 238, 238});
        systemColors.put("palevioletred1", new int[] {255, 130, 171});
        systemColors.put("palevioletred2", new int[] {238, 121, 159});
        systemColors.put("palevioletred3", new int[] {205, 104, 137});
        systemColors.put("palevioletred4", new int[] {139, 71, 93});
        systemColors.put("palevioletred", new int[] {219, 112, 147});
        systemColors.put("papayawhip", new int[] {255, 239, 213});
        systemColors.put("peachpuff1", new int[] {255, 218, 185});
        systemColors.put("peachpuff2", new int[] {238, 203, 173});
        systemColors.put("peachpuff3", new int[] {205, 175, 149});
        systemColors.put("peachpuff4", new int[] {139, 119, 101});
        systemColors.put("peachpuff", new int[] {255, 218, 185});
        systemColors.put("peru", new int[] {205, 133, 63});
        systemColors.put("pink1", new int[] {255, 181, 197});
        systemColors.put("pink2", new int[] {238, 169, 184});
        systemColors.put("pink3", new int[] {205, 145, 158});
        systemColors.put("pink4", new int[] {139, 99, 108});
        systemColors.put("pink", new int[] {255, 192, 203});
        systemColors.put("plum1", new int[] {255, 187, 255});
        systemColors.put("plum2", new int[] {238, 174, 238});
        systemColors.put("plum3", new int[] {205, 150, 205});
        systemColors.put("plum4", new int[] {139, 102, 139});
        systemColors.put("plum", new int[] {221, 160, 221});
        systemColors.put("powderblue", new int[] {176, 224, 230});
        systemColors.put("purple1", new int[] {155, 48, 255});
        systemColors.put("purple2", new int[] {145, 44, 238});
        systemColors.put("purple3", new int[] {125, 38, 205});
        systemColors.put("purple4", new int[] {85, 26, 139});
        systemColors.put("purple", new int[] {160, 32, 240});
        systemColors.put("red1", new int[] {255, 0, 0});
        systemColors.put("red2", new int[] {238, 0, 0});
        systemColors.put("red3", new int[] {205, 0, 0});
        systemColors.put("red4", new int[] {139, 0, 0});
        systemColors.put("red", new int[] {255, 0, 0});
        systemColors.put("rosybrown1", new int[] {255, 193, 193});
        systemColors.put("rosybrown2", new int[] {238, 180, 180});
        systemColors.put("rosybrown3", new int[] {205, 155, 155});
        systemColors.put("rosybrown4", new int[] {139, 105, 105});
        systemColors.put("rosybrown", new int[] {188, 143, 143});
        systemColors.put("royalblue1", new int[] {72, 118, 255});
        systemColors.put("royalblue2", new int[] {67, 110, 238});
        systemColors.put("royalblue3", new int[] {58, 95, 205});
        systemColors.put("royalblue4", new int[] {39, 64, 139});
        systemColors.put("royalblue", new int[] {65, 105, 225});
        systemColors.put("saddlebrown", new int[] {139, 69, 19});
        systemColors.put("salmon1", new int[] {255, 140, 105});
        systemColors.put("salmon2", new int[] {238, 130, 98});
        systemColors.put("salmon3", new int[] {205, 112, 84});
        systemColors.put("salmon4", new int[] {139, 76, 57});
        systemColors.put("salmon", new int[] {250, 128, 114});
        systemColors.put("sandybrown", new int[] {244, 164, 96});
        systemColors.put("seagreen1", new int[] {84, 255, 159});
        systemColors.put("seagreen2", new int[] {78, 238, 148});
        systemColors.put("seagreen3", new int[] {67, 205, 128});
        systemColors.put("seagreen4", new int[] {46, 139, 87});
        systemColors.put("seagreen", new int[] {46, 139, 87});
        systemColors.put("seashell1", new int[] {255, 245, 238});
        systemColors.put("seashell2", new int[] {238, 229, 222});
        systemColors.put("seashell3", new int[] {205, 197, 191});
        systemColors.put("seashell4", new int[] {139, 134, 130});
        systemColors.put("seashell", new int[] {255, 245, 238});
        systemColors.put("sienna1", new int[] {255, 130, 71});
        systemColors.put("sienna2", new int[] {238, 121, 66});
        systemColors.put("sienna3", new int[] {205, 104, 57});
        systemColors.put("sienna4", new int[] {139, 71, 38});
        systemColors.put("sienna", new int[] {160, 82, 45});
        systemColors.put("skyblue1", new int[] {135, 206, 255});
        systemColors.put("skyblue2", new int[] {126, 192, 238});
        systemColors.put("skyblue3", new int[] {108, 166, 205});
        systemColors.put("skyblue4", new int[] {74, 112, 139});
        systemColors.put("skyblue", new int[] {135, 206, 235});
        systemColors.put("slateblue1", new int[] {131, 111, 255});
        systemColors.put("slateblue2", new int[] {122, 103, 238});
        systemColors.put("slateblue3", new int[] {105, 89, 205});
        systemColors.put("slateblue4", new int[] {71, 60, 139});
        systemColors.put("slateblue", new int[] {106, 90, 205});
        systemColors.put("slategray1", new int[] {198, 226, 255});
        systemColors.put("slategray2", new int[] {185, 211, 238});
        systemColors.put("slategray3", new int[] {159, 182, 205});
        systemColors.put("slategray4", new int[] {108, 123, 139});
        systemColors.put("slategray", new int[] {112, 128, 144});
        systemColors.put("slategrey", new int[] {112, 128, 144});
        systemColors.put("snow1", new int[] {255, 250, 250});
        systemColors.put("snow2", new int[] {238, 233, 233});
        systemColors.put("snow3", new int[] {205, 201, 201});
        systemColors.put("snow4", new int[] {139, 137, 137});
        systemColors.put("snow", new int[] {255, 250, 250});
        systemColors.put("springgreen1", new int[] {0, 255, 127});
        systemColors.put("springgreen2", new int[] {0, 238, 118});
        systemColors.put("springgreen3", new int[] {0, 205, 102});
        systemColors.put("springgreen4", new int[] {0, 139, 69});
        systemColors.put("springgreen", new int[] {0, 255, 127});
        systemColors.put("steelblue1", new int[] {99, 184, 255});
        systemColors.put("steelblue2", new int[] {92, 172, 238});
        systemColors.put("steelblue3", new int[] {79, 148, 205});
        systemColors.put("steelblue4", new int[] {54, 100, 139});
        systemColors.put("steelblue", new int[] {70, 130, 180});
        systemColors.put("tan1", new int[] {255, 165, 79});
        systemColors.put("tan2", new int[] {238, 154, 73});
        systemColors.put("tan3", new int[] {205, 133, 63});
        systemColors.put("tan4", new int[] {139, 90, 43});
        systemColors.put("tan", new int[] {210, 180, 140});
        systemColors.put("thistle1", new int[] {255, 225, 255});
        systemColors.put("thistle2", new int[] {238, 210, 238});
        systemColors.put("thistle3", new int[] {205, 181, 205});
        systemColors.put("thistle4", new int[] {139, 123, 139});
        systemColors.put("thistle", new int[] {216, 191, 216});
        systemColors.put("tomato1", new int[] {255, 99, 71});
        systemColors.put("tomato2", new int[] {238, 92, 66});
        systemColors.put("tomato3", new int[] {205, 79, 57});
        systemColors.put("tomato4", new int[] {139, 54, 38});
        systemColors.put("tomato", new int[] {255, 99, 71});
        systemColors.put("turquoise1", new int[] {0, 245, 255});
        systemColors.put("turquoise2", new int[] {0, 229, 238});
        systemColors.put("turquoise3", new int[] {0, 197, 205});
        systemColors.put("turquoise4", new int[] {0, 134, 139});
        systemColors.put("turquoise", new int[] {64, 224, 208});
        systemColors.put("violet", new int[] {238, 130, 238});
        systemColors.put("violetred1", new int[] {255, 62, 150});
        systemColors.put("violetred2", new int[] {238, 58, 140});
        systemColors.put("violetred3", new int[] {205, 50, 120});
        systemColors.put("violetred4", new int[] {139, 34, 82});
        systemColors.put("violetred", new int[] {208, 32, 144});
        systemColors.put("wheat1", new int[] {255, 231, 186});
        systemColors.put("wheat2", new int[] {238, 216, 174});
        systemColors.put("wheat3", new int[] {205, 186, 150});
        systemColors.put("wheat4", new int[] {139, 126, 102});
        systemColors.put("wheat", new int[] {245, 222, 179});
        systemColors.put("white", new int[] {255, 255, 255});
        systemColors.put("whitesmoke", new int[] {245, 245, 245});
        systemColors.put("yellow1", new int[] {255, 255, 0});
        systemColors.put("yellow2", new int[] {238, 238, 0});
        systemColors.put("yellow3", new int[] {205, 205, 0});
        systemColors.put("yellow4", new int[] {139, 139, 0});
        systemColors.put("yellowgreen", new int[] {154, 205, 50});
        systemColors.put("yellow", new int[] {255, 255, 0});
    }
}
