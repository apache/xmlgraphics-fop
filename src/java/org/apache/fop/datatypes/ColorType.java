/*
 * $Id: ColorType.java,v 1.15.2.10 2003/06/12 18:19:34 pbwest Exp $
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */

package org.apache.fop.datatypes;

import java.util.HashMap;

import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;

/**
 * A base datatype class; colour in XSL
 */
public class ColorType extends AbstractPropertyValue {

    private static final String tag = "$Name:  $";
    private static final String revision = "$Revision: 1.15.2.10 $";

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
        super(property, PropertyValue.COLOR_TYPE);
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
        this(PropNames.getPropertyIndex(propertyName),
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
        super(property, PropertyValue.COLOR_TYPE);
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
        this(PropNames.getPropertyIndex(propertyName),
                red, green, blue);
    }

    /**
     * @param property an <tt>int</tt> containing the property index.
     * @param color a <tt>float[]</tt> of four components containing
     * the normalized (0 <= n <= 1) red, green, blue and alpha components.
     */
    public ColorType(int property, float[] color) throws PropertyException {
        super(property, PropertyValue.COLOR_TYPE);
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
        this(PropNames.getPropertyIndex(propertyName), color);
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
        super(property, PropertyValue.COLOR_TYPE);
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
        this(PropNames.getPropertyIndex(propertyName), value);
    }

    /**
     * Construct a colour given the index of a standard XSL/HTML enumerated
     * colour.
     * @param property an <tt>int</tt>; the property index.
     * @param colorEnum an <tt>int</tt>; the enumeration index. 
     */
    public ColorType(int property, int colorEnum) throws PropertyException {
        super(property, PropertyValue.COLOR_TYPE);
        String enumText = propertyConsts.getEnumText(property, colorEnum);
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
        this(PropNames.getPropertyIndex(propertyName), colorEnum);
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
    public static float[] getStandardColor(String name) {
        return (float[])standardColors.get(name);
    }

    /**
     * Retrieve the components of a standard XSL/HTML color from the
     * <i>systemColors</i> and <i>systemColors</i>
     * <tt>HashMap</tt>s.
     * @param name a <tt>String</tt>; the name of the standard color.
     * @return a <tt>float[4]</tt> containing the RGB information for
     * that color.
     */
    public static float[] getSystemColor(String name)
        throws PropertyException
    {
        float syscolor[];
        if ((syscolor = (float[])systemColors.get(name)) != null)
            return syscolor;
        else throw new PropertyException("Unknown system color: " + name);
    }

    /**
     * A <tt>HashMap</tt> containing four-element arrays of <tt>float</tt>
     * with the normalized RGB values of standard colours, indexed by the
     * name of the color.  Individual values are <tt>float[4]</tt> arrays
     * representing the normalized color.
     */
    private static final HashMap standardColors;
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
     * a reference to the array is returned.  Individual values are
     * <tt>float[4]</tt> arrays representing the normalized color.
     */
    private static final HashMap systemColors;
    static {
        systemColors = new HashMap();
        systemColors.put("aliceblue",
                            new float[] {240/255f, 248/255f, 255/255f, 0f});
        systemColors.put("antiquewhite1",
                            new float[] {255/255f, 239/255f, 219/255f, 0f});
        systemColors.put("antiquewhite2",
                            new float[] {238/255f, 223/255f, 204/255f, 0f});
        systemColors.put("antiquewhite3",
                            new float[] {205/255f, 192/255f, 176/255f, 0f});
        systemColors.put("antiquewhite4",
                            new float[] {139/255f, 131/255f, 120/255f, 0f});
        systemColors.put("antiquewhite",
                            new float[] {250/255f, 235/255f, 215/255f, 0f});
        systemColors.put("aquamarine1",
                            new float[] {127/255f, 255/255f, 212/255f, 0f});
        systemColors.put("aquamarine2",
                            new float[] {118/255f, 238/255f, 198/255f, 0f});
        systemColors.put("aquamarine3",
                            new float[] {102/255f, 205/255f, 170/255f, 0f});
        systemColors.put("aquamarine4",
                            new float[] {69/255f, 139/255f, 116/255f, 0f});
        systemColors.put("aquamarine",
                            new float[] {127/255f, 255/255f, 212/255f, 0f});
        systemColors.put("azure1",
                            new float[] {240/255f, 255/255f, 255/255f, 0f});
        systemColors.put("azure2",
                            new float[] {224/255f, 238/255f, 238/255f, 0f});
        systemColors.put("azure3",
                            new float[] {193/255f, 205/255f, 205/255f, 0f});
        systemColors.put("azure4",
                            new float[] {131/255f, 139/255f, 139/255f, 0f});
        systemColors.put("azure",
                            new float[] {240/255f, 255/255f, 255/255f, 0f});
        systemColors.put("beige",
                            new float[] {245/255f, 245/255f, 220/255f, 0f});
        systemColors.put("bisque1",
                            new float[] {255/255f, 228/255f, 196/255f, 0f});
        systemColors.put("bisque2",
                            new float[] {238/255f, 213/255f, 183/255f, 0f});
        systemColors.put("bisque3",
                            new float[] {205/255f, 183/255f, 158/255f, 0f});
        systemColors.put("bisque4",
                            new float[] {139/255f, 125/255f, 107/255f, 0f});
        systemColors.put("bisque",
                            new float[] {255/255f, 228/255f, 196/255f, 0f});
        systemColors.put("black",
                            new float[] {0/255f, 0/255f, 0/255f, 0f});
        systemColors.put("blanchedalmond",
                            new float[] {255/255f, 235/255f, 205/255f, 0f});
        systemColors.put("blue1",
                            new float[] {0/255f, 0/255f, 255/255f, 0f});
        systemColors.put("blue2",
                            new float[] {0/255f, 0/255f, 238/255f, 0f});
        systemColors.put("blue3",
                            new float[] {0/255f, 0/255f, 205/255f, 0f});
        systemColors.put("blue4",
                            new float[] {0/255f, 0/255f, 139/255f, 0f});
        systemColors.put("blue",
                            new float[] {0/255f, 0/255f, 255/255f, 0f});
        systemColors.put("blueviolet",
                            new float[] {138/255f, 43/255f, 226/255f, 0f});
        systemColors.put("brown1",
                            new float[] {255/255f, 64/255f, 64/255f, 0f});
        systemColors.put("brown2",
                            new float[] {238/255f, 59/255f, 59/255f, 0f});
        systemColors.put("brown3",
                            new float[] {205/255f, 51/255f, 51/255f, 0f});
        systemColors.put("brown4",
                            new float[] {139/255f, 35/255f, 35/255f, 0f});
        systemColors.put("brown",
                            new float[] {165/255f, 42/255f, 42/255f, 0f});
        systemColors.put("burlywood1",
                            new float[] {255/255f, 211/255f, 155/255f, 0f});
        systemColors.put("burlywood2",
                            new float[] {238/255f, 197/255f, 145/255f, 0f});
        systemColors.put("burlywood3",
                            new float[] {205/255f, 170/255f, 125/255f, 0f});
        systemColors.put("burlywood4",
                            new float[] {139/255f, 115/255f, 85/255f, 0f});
        systemColors.put("burlywood",
                            new float[] {222/255f, 184/255f, 135/255f, 0f});
        systemColors.put("cadetblue1",
                            new float[] {152/255f, 245/255f, 255/255f, 0f});
        systemColors.put("cadetblue2",
                            new float[] {142/255f, 229/255f, 238/255f, 0f});
        systemColors.put("cadetblue3",
                            new float[] {122/255f, 197/255f, 205/255f, 0f});
        systemColors.put("cadetblue4",
                            new float[] {83/255f, 134/255f, 139/255f, 0f});
        systemColors.put("cadetblue",
                            new float[] {95/255f, 158/255f, 160/255f, 0f});
        systemColors.put("chartreuse1",
                            new float[] {127/255f, 255/255f, 0/255f, 0f});
        systemColors.put("chartreuse2",
                            new float[] {118/255f, 238/255f, 0/255f, 0f});
        systemColors.put("chartreuse3",
                            new float[] {102/255f, 205/255f, 0/255f, 0f});
        systemColors.put("chartreuse4",
                            new float[] {69/255f, 139/255f, 0/255f, 0f});
        systemColors.put("chartreuse",
                            new float[] {127/255f, 255/255f, 0/255f, 0f});
        systemColors.put("chocolate1",
                            new float[] {255/255f, 127/255f, 36/255f, 0f});
        systemColors.put("chocolate2",
                            new float[] {238/255f, 118/255f, 33/255f, 0f});
        systemColors.put("chocolate3",
                            new float[] {205/255f, 102/255f, 29/255f, 0f});
        systemColors.put("chocolate4",
                            new float[] {139/255f, 69/255f, 19/255f, 0f});
        systemColors.put("chocolate",
                            new float[] {210/255f, 105/255f, 30/255f, 0f});
        systemColors.put("coral1",
                            new float[] {255/255f, 114/255f, 86/255f, 0f});
        systemColors.put("coral2",
                            new float[] {238/255f, 106/255f, 80/255f, 0f});
        systemColors.put("coral3",
                            new float[] {205/255f, 91/255f, 69/255f, 0f});
        systemColors.put("coral4",
                            new float[] {139/255f, 62/255f, 47/255f, 0f});
        systemColors.put("coral",
                            new float[] {255/255f, 127/255f, 80/255f, 0f});
        systemColors.put("cornflowerblue",
                            new float[] {100/255f, 149/255f, 237/255f, 0f});
        systemColors.put("cornsilk1",
                            new float[] {255/255f, 248/255f, 220/255f, 0f});
        systemColors.put("cornsilk2",
                            new float[] {238/255f, 232/255f, 205/255f, 0f});
        systemColors.put("cornsilk3",
                            new float[] {205/255f, 200/255f, 177/255f, 0f});
        systemColors.put("cornsilk4",
                            new float[] {139/255f, 136/255f, 120/255f, 0f});
        systemColors.put("cornsilk",
                            new float[] {255/255f, 248/255f, 220/255f, 0f});
        systemColors.put("cyan1",
                            new float[] {0/255f, 255/255f, 255/255f, 0f});
        systemColors.put("cyan2",
                            new float[] {0/255f, 238/255f, 238/255f, 0f});
        systemColors.put("cyan3",
                            new float[] {0/255f, 205/255f, 205/255f, 0f});
        systemColors.put("cyan4",
                            new float[] {0/255f, 139/255f, 139/255f, 0f});
        systemColors.put("cyan",
                            new float[] {0/255f, 255/255f, 255/255f, 0f});
        systemColors.put("darkblue",
                            new float[] {0/255f, 0/255f, 139/255f, 0f});
        systemColors.put("darkcyan",
                            new float[] {0/255f, 139/255f, 139/255f, 0f});
        systemColors.put("darkgoldenrod1",
                            new float[] {255/255f, 185/255f, 15/255f, 0f});
        systemColors.put("darkgoldenrod2",
                            new float[] {238/255f, 173/255f, 14/255f, 0f});
        systemColors.put("darkgoldenrod3",
                            new float[] {205/255f, 149/255f, 12/255f, 0f});
        systemColors.put("darkgoldenrod4",
                            new float[] {139/255f, 101/255f, 8/255f, 0f});
        systemColors.put("darkgoldenrod",
                            new float[] {184/255f, 134/255f, 11/255f, 0f});
        systemColors.put("darkgray",
                            new float[] {169/255f, 169/255f, 169/255f, 0f});
        systemColors.put("darkgreen",
                            new float[] {0/255f, 100/255f, 0/255f, 0f});
        systemColors.put("darkgrey",
                            new float[] {169/255f, 169/255f, 169/255f, 0f});
        systemColors.put("darkkhaki",
                            new float[] {189/255f, 183/255f, 107/255f, 0f});
        systemColors.put("darkmagenta",
                            new float[] {139/255f, 0/255f, 139/255f, 0f});
        systemColors.put("darkolivegreen1",
                            new float[] {202/255f, 255/255f, 112/255f, 0f});
        systemColors.put("darkolivegreen2",
                            new float[] {188/255f, 238/255f, 104/255f, 0f});
        systemColors.put("darkolivegreen3",
                            new float[] {162/255f, 205/255f, 90/255f, 0f});
        systemColors.put("darkolivegreen4",
                            new float[] {110/255f, 139/255f, 61/255f, 0f});
        systemColors.put("darkolivegreen",
                            new float[] {85/255f, 107/255f, 47/255f, 0f});
        systemColors.put("darkorange1",
                            new float[] {255/255f, 127/255f, 0/255f, 0f});
        systemColors.put("darkorange2",
                            new float[] {238/255f, 118/255f, 0/255f, 0f});
        systemColors.put("darkorange3",
                            new float[] {205/255f, 102/255f, 0/255f, 0f});
        systemColors.put("darkorange4",
                            new float[] {139/255f, 69/255f, 0/255f, 0f});
        systemColors.put("darkorange",
                            new float[] {255/255f, 140/255f, 0/255f, 0f});
        systemColors.put("darkorchid1",
                            new float[] {191/255f, 62/255f, 255/255f, 0f});
        systemColors.put("darkorchid2",
                            new float[] {178/255f, 58/255f, 238/255f, 0f});
        systemColors.put("darkorchid3",
                            new float[] {154/255f, 50/255f, 205/255f, 0f});
        systemColors.put("darkorchid4",
                            new float[] {104/255f, 34/255f, 139/255f, 0f});
        systemColors.put("darkorchid",
                            new float[] {153/255f, 50/255f, 204/255f, 0f});
        systemColors.put("darkred",
                            new float[] {139/255f, 0/255f, 0/255f, 0f});
        systemColors.put("darksalmon",
                            new float[] {233/255f, 150/255f, 122/255f, 0f});
        systemColors.put("darkseagreen1",
                            new float[] {193/255f, 255/255f, 193/255f, 0f});
        systemColors.put("darkseagreen2",
                            new float[] {180/255f, 238/255f, 180/255f, 0f});
        systemColors.put("darkseagreen3",
                            new float[] {155/255f, 205/255f, 155/255f, 0f});
        systemColors.put("darkseagreen4",
                            new float[] {105/255f, 139/255f, 105/255f, 0f});
        systemColors.put("darkseagreen",
                            new float[] {143/255f, 188/255f, 143/255f, 0f});
        systemColors.put("darkslateblue",
                            new float[] {72/255f, 61/255f, 139/255f, 0f});
        systemColors.put("darkslategray1",
                            new float[] {151/255f, 255/255f, 255/255f, 0f});
        systemColors.put("darkslategray2",
                            new float[] {141/255f, 238/255f, 238/255f, 0f});
        systemColors.put("darkslategray3",
                            new float[] {121/255f, 205/255f, 205/255f, 0f});
        systemColors.put("darkslategray4",
                            new float[] {82/255f, 139/255f, 139/255f, 0f});
        systemColors.put("darkslategray",
                            new float[] {47/255f, 79/255f, 79/255f, 0f});
        systemColors.put("darkslategrey",
                            new float[] {47/255f, 79/255f, 79/255f, 0f});
        systemColors.put("darkturquoise",
                            new float[] {0/255f, 206/255f, 209/255f, 0f});
        systemColors.put("darkviolet",
                            new float[] {148/255f, 0/255f, 211/255f, 0f});
        systemColors.put("deeppink1",
                            new float[] {255/255f, 20/255f, 147/255f, 0f});
        systemColors.put("deeppink2",
                            new float[] {238/255f, 18/255f, 137/255f, 0f});
        systemColors.put("deeppink3",
                            new float[] {205/255f, 16/255f, 118/255f, 0f});
        systemColors.put("deeppink4",
                            new float[] {139/255f, 10/255f, 80/255f, 0f});
        systemColors.put("deeppink",
                            new float[] {255/255f, 20/255f, 147/255f, 0f});
        systemColors.put("deepskyblue1",
                            new float[] {0/255f, 191/255f, 255/255f, 0f});
        systemColors.put("deepskyblue2",
                            new float[] {0/255f, 178/255f, 238/255f, 0f});
        systemColors.put("deepskyblue3",
                            new float[] {0/255f, 154/255f, 205/255f, 0f});
        systemColors.put("deepskyblue4",
                            new float[] {0/255f, 104/255f, 139/255f, 0f});
        systemColors.put("deepskyblue",
                            new float[] {0/255f, 191/255f, 255/255f, 0f});
        systemColors.put("dimgray",
                            new float[] {105/255f, 105/255f, 105/255f, 0f});
        systemColors.put("dimgrey",
                            new float[] {105/255f, 105/255f, 105/255f, 0f});
        systemColors.put("dodgerblue1",
                            new float[] {30/255f, 144/255f, 255/255f, 0f});
        systemColors.put("dodgerblue2",
                            new float[] {28/255f, 134/255f, 238/255f, 0f});
        systemColors.put("dodgerblue3",
                            new float[] {24/255f, 116/255f, 205/255f, 0f});
        systemColors.put("dodgerblue4",
                            new float[] {16/255f, 78/255f, 139/255f, 0f});
        systemColors.put("dodgerblue",
                            new float[] {30/255f, 144/255f, 255/255f, 0f});
        systemColors.put("firebrick1",
                            new float[] {255/255f, 48/255f, 48/255f, 0f});
        systemColors.put("firebrick2",
                            new float[] {238/255f, 44/255f, 44/255f, 0f});
        systemColors.put("firebrick3",
                            new float[] {205/255f, 38/255f, 38/255f, 0f});
        systemColors.put("firebrick4",
                            new float[] {139/255f, 26/255f, 26/255f, 0f});
        systemColors.put("firebrick",
                            new float[] {178/255f, 34/255f, 34/255f, 0f});
        systemColors.put("floralwhite",
                            new float[] {255/255f, 250/255f, 240/255f, 0f});
        systemColors.put("forestgreen",
                            new float[] {34/255f, 139/255f, 34/255f, 0f});
        systemColors.put("gainsboro",
                            new float[] {220/255f, 220/255f, 220/255f, 0f});
        systemColors.put("ghostwhite",
                            new float[] {248/255f, 248/255f, 255/255f, 0f});
        systemColors.put("gold1",
                            new float[] {255/255f, 215/255f, 0/255f, 0f});
        systemColors.put("gold2",
                            new float[] {238/255f, 201/255f, 0/255f, 0f});
        systemColors.put("gold3",
                            new float[] {205/255f, 173/255f, 0/255f, 0f});
        systemColors.put("gold4",
                            new float[] {139/255f, 117/255f, 0/255f, 0f});
        systemColors.put("goldenrod1",
                            new float[] {255/255f, 193/255f, 37/255f, 0f});
        systemColors.put("goldenrod2",
                            new float[] {238/255f, 180/255f, 34/255f, 0f});
        systemColors.put("goldenrod3",
                            new float[] {205/255f, 155/255f, 29/255f, 0f});
        systemColors.put("goldenrod4",
                            new float[] {139/255f, 105/255f, 20/255f, 0f});
        systemColors.put("goldenrod",
                            new float[] {218/255f, 165/255f, 32/255f, 0f});
        systemColors.put("gold",
                            new float[] {255/255f, 215/255f, 0/255f, 0f});
        systemColors.put("gray0",
                            new float[] {0/255f, 0/255f, 0/255f, 0f});
        systemColors.put("gray100",
                            new float[] {255/255f, 255/255f, 255/255f, 0f});
        systemColors.put("gray10",
                            new float[] {26/255f, 26/255f, 26/255f, 0f});
        systemColors.put("gray11",
                            new float[] {28/255f, 28/255f, 28/255f, 0f});
        systemColors.put("gray12",
                            new float[] {31/255f, 31/255f, 31/255f, 0f});
        systemColors.put("gray13",
                            new float[] {33/255f, 33/255f, 33/255f, 0f});
        systemColors.put("gray14",
                            new float[] {36/255f, 36/255f, 36/255f, 0f});
        systemColors.put("gray15",
                            new float[] {38/255f, 38/255f, 38/255f, 0f});
        systemColors.put("gray16",
                            new float[] {41/255f, 41/255f, 41/255f, 0f});
        systemColors.put("gray17",
                            new float[] {43/255f, 43/255f, 43/255f, 0f});
        systemColors.put("gray18",
                            new float[] {46/255f, 46/255f, 46/255f, 0f});
        systemColors.put("gray19",
                            new float[] {48/255f, 48/255f, 48/255f, 0f});
        systemColors.put("gray1",
                            new float[] {3/255f, 3/255f, 3/255f, 0f});
        systemColors.put("gray20",
                            new float[] {51/255f, 51/255f, 51/255f, 0f});
        systemColors.put("gray21",
                            new float[] {54/255f, 54/255f, 54/255f, 0f});
        systemColors.put("gray22",
                            new float[] {56/255f, 56/255f, 56/255f, 0f});
        systemColors.put("gray23",
                            new float[] {59/255f, 59/255f, 59/255f, 0f});
        systemColors.put("gray24",
                            new float[] {61/255f, 61/255f, 61/255f, 0f});
        systemColors.put("gray25",
                            new float[] {64/255f, 64/255f, 64/255f, 0f});
        systemColors.put("gray26",
                            new float[] {66/255f, 66/255f, 66/255f, 0f});
        systemColors.put("gray27",
                            new float[] {69/255f, 69/255f, 69/255f, 0f});
        systemColors.put("gray28",
                            new float[] {71/255f, 71/255f, 71/255f, 0f});
        systemColors.put("gray29",
                            new float[] {74/255f, 74/255f, 74/255f, 0f});
        systemColors.put("gray2",
                            new float[] {5/255f, 5/255f, 5/255f, 0f});
        systemColors.put("gray30",
                            new float[] {77/255f, 77/255f, 77/255f, 0f});
        systemColors.put("gray31",
                            new float[] {79/255f, 79/255f, 79/255f, 0f});
        systemColors.put("gray32",
                            new float[] {82/255f, 82/255f, 82/255f, 0f});
        systemColors.put("gray33",
                            new float[] {84/255f, 84/255f, 84/255f, 0f});
        systemColors.put("gray34",
                            new float[] {87/255f, 87/255f, 87/255f, 0f});
        systemColors.put("gray35",
                            new float[] {89/255f, 89/255f, 89/255f, 0f});
        systemColors.put("gray36",
                            new float[] {92/255f, 92/255f, 92/255f, 0f});
        systemColors.put("gray37",
                            new float[] {94/255f, 94/255f, 94/255f, 0f});
        systemColors.put("gray38",
                            new float[] {97/255f, 97/255f, 97/255f, 0f});
        systemColors.put("gray39",
                            new float[] {99/255f, 99/255f, 99/255f, 0f});
        systemColors.put("gray3",
                            new float[] {8/255f, 8/255f, 8/255f, 0f});
        systemColors.put("gray40",
                            new float[] {102/255f, 102/255f, 102/255f, 0f});
        systemColors.put("gray41",
                            new float[] {105/255f, 105/255f, 105/255f, 0f});
        systemColors.put("gray42",
                            new float[] {107/255f, 107/255f, 107/255f, 0f});
        systemColors.put("gray43",
                            new float[] {110/255f, 110/255f, 110/255f, 0f});
        systemColors.put("gray44",
                            new float[] {112/255f, 112/255f, 112/255f, 0f});
        systemColors.put("gray45",
                            new float[] {115/255f, 115/255f, 115/255f, 0f});
        systemColors.put("gray46",
                            new float[] {117/255f, 117/255f, 117/255f, 0f});
        systemColors.put("gray47",
                            new float[] {120/255f, 120/255f, 120/255f, 0f});
        systemColors.put("gray48",
                            new float[] {122/255f, 122/255f, 122/255f, 0f});
        systemColors.put("gray49",
                            new float[] {125/255f, 125/255f, 125/255f, 0f});
        systemColors.put("gray4",
                            new float[] {10/255f, 10/255f, 10/255f, 0f});
        systemColors.put("gray50",
                            new float[] {127/255f, 127/255f, 127/255f, 0f});
        systemColors.put("gray51",
                            new float[] {130/255f, 130/255f, 130/255f, 0f});
        systemColors.put("gray52",
                            new float[] {133/255f, 133/255f, 133/255f, 0f});
        systemColors.put("gray53",
                            new float[] {135/255f, 135/255f, 135/255f, 0f});
        systemColors.put("gray54",
                            new float[] {138/255f, 138/255f, 138/255f, 0f});
        systemColors.put("gray55",
                            new float[] {140/255f, 140/255f, 140/255f, 0f});
        systemColors.put("gray56",
                            new float[] {143/255f, 143/255f, 143/255f, 0f});
        systemColors.put("gray57",
                            new float[] {145/255f, 145/255f, 145/255f, 0f});
        systemColors.put("gray58",
                            new float[] {148/255f, 148/255f, 148/255f, 0f});
        systemColors.put("gray59",
                            new float[] {150/255f, 150/255f, 150/255f, 0f});
        systemColors.put("gray5",
                            new float[] {13/255f, 13/255f, 13/255f, 0f});
        systemColors.put("gray60",
                            new float[] {153/255f, 153/255f, 153/255f, 0f});
        systemColors.put("gray61",
                            new float[] {156/255f, 156/255f, 156/255f, 0f});
        systemColors.put("gray62",
                            new float[] {158/255f, 158/255f, 158/255f, 0f});
        systemColors.put("gray63",
                            new float[] {161/255f, 161/255f, 161/255f, 0f});
        systemColors.put("gray64",
                            new float[] {163/255f, 163/255f, 163/255f, 0f});
        systemColors.put("gray65",
                            new float[] {166/255f, 166/255f, 166/255f, 0f});
        systemColors.put("gray66",
                            new float[] {168/255f, 168/255f, 168/255f, 0f});
        systemColors.put("gray67",
                            new float[] {171/255f, 171/255f, 171/255f, 0f});
        systemColors.put("gray68",
                            new float[] {173/255f, 173/255f, 173/255f, 0f});
        systemColors.put("gray69",
                            new float[] {176/255f, 176/255f, 176/255f, 0f});
        systemColors.put("gray6",
                            new float[] {15/255f, 15/255f, 15/255f, 0f});
        systemColors.put("gray70",
                            new float[] {179/255f, 179/255f, 179/255f, 0f});
        systemColors.put("gray71",
                            new float[] {181/255f, 181/255f, 181/255f, 0f});
        systemColors.put("gray72",
                            new float[] {184/255f, 184/255f, 184/255f, 0f});
        systemColors.put("gray73",
                            new float[] {186/255f, 186/255f, 186/255f, 0f});
        systemColors.put("gray74",
                            new float[] {189/255f, 189/255f, 189/255f, 0f});
        systemColors.put("gray75",
                            new float[] {191/255f, 191/255f, 191/255f, 0f});
        systemColors.put("gray76",
                            new float[] {194/255f, 194/255f, 194/255f, 0f});
        systemColors.put("gray77",
                            new float[] {196/255f, 196/255f, 196/255f, 0f});
        systemColors.put("gray78",
                            new float[] {199/255f, 199/255f, 199/255f, 0f});
        systemColors.put("gray79",
                            new float[] {201/255f, 201/255f, 201/255f, 0f});
        systemColors.put("gray7",
                            new float[] {18/255f, 18/255f, 18/255f, 0f});
        systemColors.put("gray80",
                            new float[] {204/255f, 204/255f, 204/255f, 0f});
        systemColors.put("gray81",
                            new float[] {207/255f, 207/255f, 207/255f, 0f});
        systemColors.put("gray82",
                            new float[] {209/255f, 209/255f, 209/255f, 0f});
        systemColors.put("gray83",
                            new float[] {212/255f, 212/255f, 212/255f, 0f});
        systemColors.put("gray84",
                            new float[] {214/255f, 214/255f, 214/255f, 0f});
        systemColors.put("gray85",
                            new float[] {217/255f, 217/255f, 217/255f, 0f});
        systemColors.put("gray86",
                            new float[] {219/255f, 219/255f, 219/255f, 0f});
        systemColors.put("gray87",
                            new float[] {222/255f, 222/255f, 222/255f, 0f});
        systemColors.put("gray88",
                            new float[] {224/255f, 224/255f, 224/255f, 0f});
        systemColors.put("gray89",
                            new float[] {227/255f, 227/255f, 227/255f, 0f});
        systemColors.put("gray8",
                            new float[] {20/255f, 20/255f, 20/255f, 0f});
        systemColors.put("gray90",
                            new float[] {229/255f, 229/255f, 229/255f, 0f});
        systemColors.put("gray91",
                            new float[] {232/255f, 232/255f, 232/255f, 0f});
        systemColors.put("gray92",
                            new float[] {235/255f, 235/255f, 235/255f, 0f});
        systemColors.put("gray93",
                            new float[] {237/255f, 237/255f, 237/255f, 0f});
        systemColors.put("gray94",
                            new float[] {240/255f, 240/255f, 240/255f, 0f});
        systemColors.put("gray95",
                            new float[] {242/255f, 242/255f, 242/255f, 0f});
        systemColors.put("gray96",
                            new float[] {245/255f, 245/255f, 245/255f, 0f});
        systemColors.put("gray97",
                            new float[] {247/255f, 247/255f, 247/255f, 0f});
        systemColors.put("gray98",
                            new float[] {250/255f, 250/255f, 250/255f, 0f});
        systemColors.put("gray99",
                            new float[] {252/255f, 252/255f, 252/255f, 0f});
        systemColors.put("gray9",
                            new float[] {23/255f, 23/255f, 23/255f, 0f});
        systemColors.put("gray",
                            new float[] {190/255f, 190/255f, 190/255f, 0f});
        systemColors.put("green1",
                            new float[] {0/255f, 255/255f, 0/255f, 0f});
        systemColors.put("green2",
                            new float[] {0/255f, 238/255f, 0/255f, 0f});
        systemColors.put("green3",
                            new float[] {0/255f, 205/255f, 0/255f, 0f});
        systemColors.put("green4",
                            new float[] {0/255f, 139/255f, 0/255f, 0f});
        systemColors.put("green",
                            new float[] {0/255f, 255/255f, 0/255f, 0f});
        systemColors.put("greenyellow",
                            new float[] {173/255f, 255/255f, 47/255f, 0f});
        systemColors.put("grey0",
                            new float[] {0/255f, 0/255f, 0/255f, 0f});
        systemColors.put("grey100",
                            new float[] {255/255f, 255/255f, 255/255f, 0f});
        systemColors.put("grey10",
                            new float[] {26/255f, 26/255f, 26/255f, 0f});
        systemColors.put("grey11",
                            new float[] {28/255f, 28/255f, 28/255f, 0f});
        systemColors.put("grey12",
                            new float[] {31/255f, 31/255f, 31/255f, 0f});
        systemColors.put("grey13",
                            new float[] {33/255f, 33/255f, 33/255f, 0f});
        systemColors.put("grey14",
                            new float[] {36/255f, 36/255f, 36/255f, 0f});
        systemColors.put("grey15",
                            new float[] {38/255f, 38/255f, 38/255f, 0f});
        systemColors.put("grey16",
                            new float[] {41/255f, 41/255f, 41/255f, 0f});
        systemColors.put("grey17",
                            new float[] {43/255f, 43/255f, 43/255f, 0f});
        systemColors.put("grey18",
                            new float[] {46/255f, 46/255f, 46/255f, 0f});
        systemColors.put("grey19",
                            new float[] {48/255f, 48/255f, 48/255f, 0f});
        systemColors.put("grey1",
                            new float[] {3/255f, 3/255f, 3/255f, 0f});
        systemColors.put("grey20",
                            new float[] {51/255f, 51/255f, 51/255f, 0f});
        systemColors.put("grey21",
                            new float[] {54/255f, 54/255f, 54/255f, 0f});
        systemColors.put("grey22",
                            new float[] {56/255f, 56/255f, 56/255f, 0f});
        systemColors.put("grey23",
                            new float[] {59/255f, 59/255f, 59/255f, 0f});
        systemColors.put("grey24",
                            new float[] {61/255f, 61/255f, 61/255f, 0f});
        systemColors.put("grey25",
                            new float[] {64/255f, 64/255f, 64/255f, 0f});
        systemColors.put("grey26",
                            new float[] {66/255f, 66/255f, 66/255f, 0f});
        systemColors.put("grey27",
                            new float[] {69/255f, 69/255f, 69/255f, 0f});
        systemColors.put("grey28",
                            new float[] {71/255f, 71/255f, 71/255f, 0f});
        systemColors.put("grey29",
                            new float[] {74/255f, 74/255f, 74/255f, 0f});
        systemColors.put("grey2",
                            new float[] {5/255f, 5/255f, 5/255f, 0f});
        systemColors.put("grey30",
                            new float[] {77/255f, 77/255f, 77/255f, 0f});
        systemColors.put("grey31",
                            new float[] {79/255f, 79/255f, 79/255f, 0f});
        systemColors.put("grey32",
                            new float[] {82/255f, 82/255f, 82/255f, 0f});
        systemColors.put("grey33",
                            new float[] {84/255f, 84/255f, 84/255f, 0f});
        systemColors.put("grey34",
                            new float[] {87/255f, 87/255f, 87/255f, 0f});
        systemColors.put("grey35",
                            new float[] {89/255f, 89/255f, 89/255f, 0f});
        systemColors.put("grey36",
                            new float[] {92/255f, 92/255f, 92/255f, 0f});
        systemColors.put("grey37",
                            new float[] {94/255f, 94/255f, 94/255f, 0f});
        systemColors.put("grey38",
                            new float[] {97/255f, 97/255f, 97/255f, 0f});
        systemColors.put("grey39",
                            new float[] {99/255f, 99/255f, 99/255f, 0f});
        systemColors.put("grey3",
                            new float[] {8/255f, 8/255f, 8/255f, 0f});
        systemColors.put("grey40",
                            new float[] {102/255f, 102/255f, 102/255f, 0f});
        systemColors.put("grey41",
                            new float[] {105/255f, 105/255f, 105/255f, 0f});
        systemColors.put("grey42",
                            new float[] {107/255f, 107/255f, 107/255f, 0f});
        systemColors.put("grey43",
                            new float[] {110/255f, 110/255f, 110/255f, 0f});
        systemColors.put("grey44",
                            new float[] {112/255f, 112/255f, 112/255f, 0f});
        systemColors.put("grey45",
                            new float[] {115/255f, 115/255f, 115/255f, 0f});
        systemColors.put("grey46",
                            new float[] {117/255f, 117/255f, 117/255f, 0f});
        systemColors.put("grey47",
                            new float[] {120/255f, 120/255f, 120/255f, 0f});
        systemColors.put("grey48",
                            new float[] {122/255f, 122/255f, 122/255f, 0f});
        systemColors.put("grey49",
                            new float[] {125/255f, 125/255f, 125/255f, 0f});
        systemColors.put("grey4",
                            new float[] {10/255f, 10/255f, 10/255f, 0f});
        systemColors.put("grey50",
                            new float[] {127/255f, 127/255f, 127/255f, 0f});
        systemColors.put("grey51",
                            new float[] {130/255f, 130/255f, 130/255f, 0f});
        systemColors.put("grey52",
                            new float[] {133/255f, 133/255f, 133/255f, 0f});
        systemColors.put("grey53",
                            new float[] {135/255f, 135/255f, 135/255f, 0f});
        systemColors.put("grey54",
                            new float[] {138/255f, 138/255f, 138/255f, 0f});
        systemColors.put("grey55",
                            new float[] {140/255f, 140/255f, 140/255f, 0f});
        systemColors.put("grey56",
                            new float[] {143/255f, 143/255f, 143/255f, 0f});
        systemColors.put("grey57",
                            new float[] {145/255f, 145/255f, 145/255f, 0f});
        systemColors.put("grey58",
                            new float[] {148/255f, 148/255f, 148/255f, 0f});
        systemColors.put("grey59",
                            new float[] {150/255f, 150/255f, 150/255f, 0f});
        systemColors.put("grey5",
                            new float[] {13/255f, 13/255f, 13/255f, 0f});
        systemColors.put("grey60",
                            new float[] {153/255f, 153/255f, 153/255f, 0f});
        systemColors.put("grey61",
                            new float[] {156/255f, 156/255f, 156/255f, 0f});
        systemColors.put("grey62",
                            new float[] {158/255f, 158/255f, 158/255f, 0f});
        systemColors.put("grey63",
                            new float[] {161/255f, 161/255f, 161/255f, 0f});
        systemColors.put("grey64",
                            new float[] {163/255f, 163/255f, 163/255f, 0f});
        systemColors.put("grey65",
                            new float[] {166/255f, 166/255f, 166/255f, 0f});
        systemColors.put("grey66",
                            new float[] {168/255f, 168/255f, 168/255f, 0f});
        systemColors.put("grey67",
                            new float[] {171/255f, 171/255f, 171/255f, 0f});
        systemColors.put("grey68",
                            new float[] {173/255f, 173/255f, 173/255f, 0f});
        systemColors.put("grey69",
                            new float[] {176/255f, 176/255f, 176/255f, 0f});
        systemColors.put("grey6",
                            new float[] {15/255f, 15/255f, 15/255f, 0f});
        systemColors.put("grey70",
                            new float[] {179/255f, 179/255f, 179/255f, 0f});
        systemColors.put("grey71",
                            new float[] {181/255f, 181/255f, 181/255f, 0f});
        systemColors.put("grey72",
                            new float[] {184/255f, 184/255f, 184/255f, 0f});
        systemColors.put("grey73",
                            new float[] {186/255f, 186/255f, 186/255f, 0f});
        systemColors.put("grey74",
                            new float[] {189/255f, 189/255f, 189/255f, 0f});
        systemColors.put("grey75",
                            new float[] {191/255f, 191/255f, 191/255f, 0f});
        systemColors.put("grey76",
                            new float[] {194/255f, 194/255f, 194/255f, 0f});
        systemColors.put("grey77",
                            new float[] {196/255f, 196/255f, 196/255f, 0f});
        systemColors.put("grey78",
                            new float[] {199/255f, 199/255f, 199/255f, 0f});
        systemColors.put("grey79",
                            new float[] {201/255f, 201/255f, 201/255f, 0f});
        systemColors.put("grey7",
                            new float[] {18/255f, 18/255f, 18/255f, 0f});
        systemColors.put("grey80",
                            new float[] {204/255f, 204/255f, 204/255f, 0f});
        systemColors.put("grey81",
                            new float[] {207/255f, 207/255f, 207/255f, 0f});
        systemColors.put("grey82",
                            new float[] {209/255f, 209/255f, 209/255f, 0f});
        systemColors.put("grey83",
                            new float[] {212/255f, 212/255f, 212/255f, 0f});
        systemColors.put("grey84",
                            new float[] {214/255f, 214/255f, 214/255f, 0f});
        systemColors.put("grey85",
                            new float[] {217/255f, 217/255f, 217/255f, 0f});
        systemColors.put("grey86",
                            new float[] {219/255f, 219/255f, 219/255f, 0f});
        systemColors.put("grey87",
                            new float[] {222/255f, 222/255f, 222/255f, 0f});
        systemColors.put("grey88",
                            new float[] {224/255f, 224/255f, 224/255f, 0f});
        systemColors.put("grey89",
                            new float[] {227/255f, 227/255f, 227/255f, 0f});
        systemColors.put("grey8",
                            new float[] {20/255f, 20/255f, 20/255f, 0f});
        systemColors.put("grey90",
                            new float[] {229/255f, 229/255f, 229/255f, 0f});
        systemColors.put("grey91",
                            new float[] {232/255f, 232/255f, 232/255f, 0f});
        systemColors.put("grey92",
                            new float[] {235/255f, 235/255f, 235/255f, 0f});
        systemColors.put("grey93",
                            new float[] {237/255f, 237/255f, 237/255f, 0f});
        systemColors.put("grey94",
                            new float[] {240/255f, 240/255f, 240/255f, 0f});
        systemColors.put("grey95",
                            new float[] {242/255f, 242/255f, 242/255f, 0f});
        systemColors.put("grey96",
                            new float[] {245/255f, 245/255f, 245/255f, 0f});
        systemColors.put("grey97",
                            new float[] {247/255f, 247/255f, 247/255f, 0f});
        systemColors.put("grey98",
                            new float[] {250/255f, 250/255f, 250/255f, 0f});
        systemColors.put("grey99",
                            new float[] {252/255f, 252/255f, 252/255f, 0f});
        systemColors.put("grey9",
                            new float[] {23/255f, 23/255f, 23/255f, 0f});
        systemColors.put("grey",
                            new float[] {190/255f, 190/255f, 190/255f, 0f});
        systemColors.put("honeydew1",
                            new float[] {240/255f, 255/255f, 240/255f, 0f});
        systemColors.put("honeydew2",
                            new float[] {224/255f, 238/255f, 224/255f, 0f});
        systemColors.put("honeydew3",
                            new float[] {193/255f, 205/255f, 193/255f, 0f});
        systemColors.put("honeydew4",
                            new float[] {131/255f, 139/255f, 131/255f, 0f});
        systemColors.put("honeydew",
                            new float[] {240/255f, 255/255f, 240/255f, 0f});
        systemColors.put("hotpink1",
                            new float[] {255/255f, 110/255f, 180/255f, 0f});
        systemColors.put("hotpink2",
                            new float[] {238/255f, 106/255f, 167/255f, 0f});
        systemColors.put("hotpink3",
                            new float[] {205/255f, 96/255f, 144/255f, 0f});
        systemColors.put("hotpink4",
                            new float[] {139/255f, 58/255f, 98/255f, 0f});
        systemColors.put("hotpink",
                            new float[] {255/255f, 105/255f, 180/255f, 0f});
        systemColors.put("indianred1",
                            new float[] {255/255f, 106/255f, 106/255f, 0f});
        systemColors.put("indianred2",
                            new float[] {238/255f, 99/255f, 99/255f, 0f});
        systemColors.put("indianred3",
                            new float[] {205/255f, 85/255f, 85/255f, 0f});
        systemColors.put("indianred4",
                            new float[] {139/255f, 58/255f, 58/255f, 0f});
        systemColors.put("indianred",
                            new float[] {205/255f, 92/255f, 92/255f, 0f});
        systemColors.put("ivory1",
                            new float[] {255/255f, 255/255f, 240/255f, 0f});
        systemColors.put("ivory2",
                            new float[] {238/255f, 238/255f, 224/255f, 0f});
        systemColors.put("ivory3",
                            new float[] {205/255f, 205/255f, 193/255f, 0f});
        systemColors.put("ivory4",
                            new float[] {139/255f, 139/255f, 131/255f, 0f});
        systemColors.put("ivory",
                            new float[] {255/255f, 255/255f, 240/255f, 0f});
        systemColors.put("khaki1",
                            new float[] {255/255f, 246/255f, 143/255f, 0f});
        systemColors.put("khaki2",
                            new float[] {238/255f, 230/255f, 133/255f, 0f});
        systemColors.put("khaki3",
                            new float[] {205/255f, 198/255f, 115/255f, 0f});
        systemColors.put("khaki4",
                            new float[] {139/255f, 134/255f, 78/255f, 0f});
        systemColors.put("khaki",
                            new float[] {240/255f, 230/255f, 140/255f, 0f});
        systemColors.put("lavenderblush1",
                            new float[] {255/255f, 240/255f, 245/255f, 0f});
        systemColors.put("lavenderblush2",
                            new float[] {238/255f, 224/255f, 229/255f, 0f});
        systemColors.put("lavenderblush3",
                            new float[] {205/255f, 193/255f, 197/255f, 0f});
        systemColors.put("lavenderblush4",
                            new float[] {139/255f, 131/255f, 134/255f, 0f});
        systemColors.put("lavenderblush",
                            new float[] {255/255f, 240/255f, 245/255f, 0f});
        systemColors.put("lavender",
                            new float[] {230/255f, 230/255f, 250/255f, 0f});
        systemColors.put("lawngreen",
                            new float[] {124/255f, 252/255f, 0/255f, 0f});
        systemColors.put("lemonchiffon1",
                            new float[] {255/255f, 250/255f, 205/255f, 0f});
        systemColors.put("lemonchiffon2",
                            new float[] {238/255f, 233/255f, 191/255f, 0f});
        systemColors.put("lemonchiffon3",
                            new float[] {205/255f, 201/255f, 165/255f, 0f});
        systemColors.put("lemonchiffon4",
                            new float[] {139/255f, 137/255f, 112/255f, 0f});
        systemColors.put("lemonchiffon",
                            new float[] {255/255f, 250/255f, 205/255f, 0f});
        systemColors.put("lightblue1",
                            new float[] {191/255f, 239/255f, 255/255f, 0f});
        systemColors.put("lightblue2",
                            new float[] {178/255f, 223/255f, 238/255f, 0f});
        systemColors.put("lightblue3",
                            new float[] {154/255f, 192/255f, 205/255f, 0f});
        systemColors.put("lightblue4",
                            new float[] {104/255f, 131/255f, 139/255f, 0f});
        systemColors.put("lightblue",
                            new float[] {173/255f, 216/255f, 230/255f, 0f});
        systemColors.put("lightcoral",
                            new float[] {240/255f, 128/255f, 128/255f, 0f});
        systemColors.put("lightcyan1",
                            new float[] {224/255f, 255/255f, 255/255f, 0f});
        systemColors.put("lightcyan2",
                            new float[] {209/255f, 238/255f, 238/255f, 0f});
        systemColors.put("lightcyan3",
                            new float[] {180/255f, 205/255f, 205/255f, 0f});
        systemColors.put("lightcyan4",
                            new float[] {122/255f, 139/255f, 139/255f, 0f});
        systemColors.put("lightcyan",
                            new float[] {224/255f, 255/255f, 255/255f, 0f});
        systemColors.put("lightgoldenrod1",
                            new float[] {255/255f, 236/255f, 139/255f, 0f});
        systemColors.put("lightgoldenrod2",
                            new float[] {238/255f, 220/255f, 130/255f, 0f});
        systemColors.put("lightgoldenrod3",
                            new float[] {205/255f, 190/255f, 112/255f, 0f});
        systemColors.put("lightgoldenrod4",
                            new float[] {139/255f, 129/255f, 76/255f, 0f});
        systemColors.put("lightgoldenrod",
                            new float[] {238/255f, 221/255f, 130/255f, 0f});
        systemColors.put("lightgoldenrodyellow",
                            new float[] {250/255f, 250/255f, 210/255f, 0f});
        systemColors.put("lightgray",
                            new float[] {211/255f, 211/255f, 211/255f, 0f});
        systemColors.put("lightgreen",
                            new float[] {144/255f, 238/255f, 144/255f, 0f});
        systemColors.put("lightgrey",
                            new float[] {211/255f, 211/255f, 211/255f, 0f});
        systemColors.put("lightpink1",
                            new float[] {255/255f, 174/255f, 185/255f, 0f});
        systemColors.put("lightpink2",
                            new float[] {238/255f, 162/255f, 173/255f, 0f});
        systemColors.put("lightpink3",
                            new float[] {205/255f, 140/255f, 149/255f, 0f});
        systemColors.put("lightpink4",
                            new float[] {139/255f, 95/255f, 101/255f, 0f});
        systemColors.put("lightpink",
                            new float[] {255/255f, 182/255f, 193/255f, 0f});
        systemColors.put("lightsalmon1",
                            new float[] {255/255f, 160/255f, 122/255f, 0f});
        systemColors.put("lightsalmon2",
                            new float[] {238/255f, 149/255f, 114/255f, 0f});
        systemColors.put("lightsalmon3",
                            new float[] {205/255f, 129/255f, 98/255f, 0f});
        systemColors.put("lightsalmon4",
                            new float[] {139/255f, 87/255f, 66/255f, 0f});
        systemColors.put("lightsalmon",
                            new float[] {255/255f, 160/255f, 122/255f, 0f});
        systemColors.put("lightseagreen",
                            new float[] {32/255f, 178/255f, 170/255f, 0f});
        systemColors.put("lightskyblue1",
                            new float[] {176/255f, 226/255f, 255/255f, 0f});
        systemColors.put("lightskyblue2",
                            new float[] {164/255f, 211/255f, 238/255f, 0f});
        systemColors.put("lightskyblue3",
                            new float[] {141/255f, 182/255f, 205/255f, 0f});
        systemColors.put("lightskyblue4",
                            new float[] {96/255f, 123/255f, 139/255f, 0f});
        systemColors.put("lightskyblue",
                            new float[] {135/255f, 206/255f, 250/255f, 0f});
        systemColors.put("lightslateblue",
                            new float[] {132/255f, 112/255f, 255/255f, 0f});
        systemColors.put("lightslategray",
                            new float[] {119/255f, 136/255f, 153/255f, 0f});
        systemColors.put("lightslategrey",
                            new float[] {119/255f, 136/255f, 153/255f, 0f});
        systemColors.put("lightsteelblue1",
                            new float[] {202/255f, 225/255f, 255/255f, 0f});
        systemColors.put("lightsteelblue2",
                            new float[] {188/255f, 210/255f, 238/255f, 0f});
        systemColors.put("lightsteelblue3",
                            new float[] {162/255f, 181/255f, 205/255f, 0f});
        systemColors.put("lightsteelblue4",
                            new float[] {110/255f, 123/255f, 139/255f, 0f});
        systemColors.put("lightsteelblue",
                            new float[] {176/255f, 196/255f, 222/255f, 0f});
        systemColors.put("lightyellow1",
                            new float[] {255/255f, 255/255f, 224/255f, 0f});
        systemColors.put("lightyellow2",
                            new float[] {238/255f, 238/255f, 209/255f, 0f});
        systemColors.put("lightyellow3",
                            new float[] {205/255f, 205/255f, 180/255f, 0f});
        systemColors.put("lightyellow4",
                            new float[] {139/255f, 139/255f, 122/255f, 0f});
        systemColors.put("lightyellow",
                            new float[] {255/255f, 255/255f, 224/255f, 0f});
        systemColors.put("limegreen",
                            new float[] {50/255f, 205/255f, 50/255f, 0f});
        systemColors.put("linen",
                            new float[] {250/255f, 240/255f, 230/255f, 0f});
        systemColors.put("magenta1",
                            new float[] {255/255f, 0/255f, 255/255f, 0f});
        systemColors.put("magenta2",
                            new float[] {238/255f, 0/255f, 238/255f, 0f});
        systemColors.put("magenta3",
                            new float[] {205/255f, 0/255f, 205/255f, 0f});
        systemColors.put("magenta4",
                            new float[] {139/255f, 0/255f, 139/255f, 0f});
        systemColors.put("magenta",
                            new float[] {255/255f, 0/255f, 255/255f, 0f});
        systemColors.put("maroon1",
                            new float[] {255/255f, 52/255f, 179/255f, 0f});
        systemColors.put("maroon2",
                            new float[] {238/255f, 48/255f, 167/255f, 0f});
        systemColors.put("maroon3",
                            new float[] {205/255f, 41/255f, 144/255f, 0f});
        systemColors.put("maroon4",
                            new float[] {139/255f, 28/255f, 98/255f, 0f});
        systemColors.put("maroon",
                            new float[] {176/255f, 48/255f, 96/255f, 0f});
        systemColors.put("mediumaquamarine",
                            new float[] {102/255f, 205/255f, 170/255f, 0f});
        systemColors.put("mediumblue",
                            new float[] {0/255f, 0/255f, 205/255f, 0f});
        systemColors.put("mediumorchid1",
                            new float[] {224/255f, 102/255f, 255/255f, 0f});
        systemColors.put("mediumorchid2",
                            new float[] {209/255f, 95/255f, 238/255f, 0f});
        systemColors.put("mediumorchid3",
                            new float[] {180/255f, 82/255f, 205/255f, 0f});
        systemColors.put("mediumorchid4",
                            new float[] {122/255f, 55/255f, 139/255f, 0f});
        systemColors.put("mediumorchid",
                            new float[] {186/255f, 85/255f, 211/255f, 0f});
        systemColors.put("mediumpurple1",
                            new float[] {171/255f, 130/255f, 255/255f, 0f});
        systemColors.put("mediumpurple2",
                            new float[] {159/255f, 121/255f, 238/255f, 0f});
        systemColors.put("mediumpurple3",
                            new float[] {137/255f, 104/255f, 205/255f, 0f});
        systemColors.put("mediumpurple4",
                            new float[] {93/255f, 71/255f, 139/255f, 0f});
        systemColors.put("mediumpurple",
                            new float[] {147/255f, 112/255f, 219/255f, 0f});
        systemColors.put("mediumseagreen",
                            new float[] {60/255f, 179/255f, 113/255f, 0f});
        systemColors.put("mediumslateblue",
                            new float[] {123/255f, 104/255f, 238/255f, 0f});
        systemColors.put("mediumspringgreen",
                            new float[] {0/255f, 250/255f, 154/255f, 0f});
        systemColors.put("mediumturquoise",
                            new float[] {72/255f, 209/255f, 204/255f, 0f});
        systemColors.put("mediumvioletred",
                            new float[] {199/255f, 21/255f, 133/255f, 0f});
        systemColors.put("midnightblue",
                            new float[] {25/255f, 25/255f, 112/255f, 0f});
        systemColors.put("mintcream",
                            new float[] {245/255f, 255/255f, 250/255f, 0f});
        systemColors.put("mistyrose1",
                            new float[] {255/255f, 228/255f, 225/255f, 0f});
        systemColors.put("mistyrose2",
                            new float[] {238/255f, 213/255f, 210/255f, 0f});
        systemColors.put("mistyrose3",
                            new float[] {205/255f, 183/255f, 181/255f, 0f});
        systemColors.put("mistyrose4",
                            new float[] {139/255f, 125/255f, 123/255f, 0f});
        systemColors.put("mistyrose",
                            new float[] {255/255f, 228/255f, 225/255f, 0f});
        systemColors.put("moccasin",
                            new float[] {255/255f, 228/255f, 181/255f, 0f});
        systemColors.put("navajowhite1",
                            new float[] {255/255f, 222/255f, 173/255f, 0f});
        systemColors.put("navajowhite2",
                            new float[] {238/255f, 207/255f, 161/255f, 0f});
        systemColors.put("navajowhite3",
                            new float[] {205/255f, 179/255f, 139/255f, 0f});
        systemColors.put("navajowhite4",
                            new float[] {139/255f, 121/255f, 94/255f, 0f});
        systemColors.put("navajowhite",
                            new float[] {255/255f, 222/255f, 173/255f, 0f});
        systemColors.put("navyblue",
                            new float[] {0/255f, 0/255f, 128/255f, 0f});
        systemColors.put("navy",
                            new float[] {0/255f, 0/255f, 128/255f, 0f});
        systemColors.put("oldlace",
                            new float[] {253/255f, 245/255f, 230/255f, 0f});
        systemColors.put("olivedrab1",
                            new float[] {192/255f, 255/255f, 62/255f, 0f});
        systemColors.put("olivedrab2",
                            new float[] {179/255f, 238/255f, 58/255f, 0f});
        systemColors.put("olivedrab3",
                            new float[] {154/255f, 205/255f, 50/255f, 0f});
        systemColors.put("olivedrab4",
                            new float[] {105/255f, 139/255f, 34/255f, 0f});
        systemColors.put("olivedrab",
                            new float[] {107/255f, 142/255f, 35/255f, 0f});
        systemColors.put("orange1",
                            new float[] {255/255f, 165/255f, 0/255f, 0f});
        systemColors.put("orange2",
                            new float[] {238/255f, 154/255f, 0/255f, 0f});
        systemColors.put("orange3",
                            new float[] {205/255f, 133/255f, 0/255f, 0f});
        systemColors.put("orange4",
                            new float[] {139/255f, 90/255f, 0/255f, 0f});
        systemColors.put("orange",
                            new float[] {255/255f, 165/255f, 0/255f, 0f});
        systemColors.put("orangered1",
                            new float[] {255/255f, 69/255f, 0/255f, 0f});
        systemColors.put("orangered2",
                            new float[] {238/255f, 64/255f, 0/255f, 0f});
        systemColors.put("orangered3",
                            new float[] {205/255f, 55/255f, 0/255f, 0f});
        systemColors.put("orangered4",
                            new float[] {139/255f, 37/255f, 0/255f, 0f});
        systemColors.put("orangered",
                            new float[] {255/255f, 69/255f, 0/255f, 0f});
        systemColors.put("orchid1",
                            new float[] {255/255f, 131/255f, 250/255f, 0f});
        systemColors.put("orchid2",
                            new float[] {238/255f, 122/255f, 233/255f, 0f});
        systemColors.put("orchid3",
                            new float[] {205/255f, 105/255f, 201/255f, 0f});
        systemColors.put("orchid4",
                            new float[] {139/255f, 71/255f, 137/255f, 0f});
        systemColors.put("orchid",
                            new float[] {218/255f, 112/255f, 214/255f, 0f});
        systemColors.put("palegoldenrod",
                            new float[] {238/255f, 232/255f, 170/255f, 0f});
        systemColors.put("palegreen1",
                            new float[] {154/255f, 255/255f, 154/255f, 0f});
        systemColors.put("palegreen2",
                            new float[] {144/255f, 238/255f, 144/255f, 0f});
        systemColors.put("palegreen3",
                            new float[] {124/255f, 205/255f, 124/255f, 0f});
        systemColors.put("palegreen4",
                            new float[] {84/255f, 139/255f, 84/255f, 0f});
        systemColors.put("palegreen",
                            new float[] {152/255f, 251/255f, 152/255f, 0f});
        systemColors.put("paleturquoise1",
                            new float[] {187/255f, 255/255f, 255/255f, 0f});
        systemColors.put("paleturquoise2",
                            new float[] {174/255f, 238/255f, 238/255f, 0f});
        systemColors.put("paleturquoise3",
                            new float[] {150/255f, 205/255f, 205/255f, 0f});
        systemColors.put("paleturquoise4",
                            new float[] {102/255f, 139/255f, 139/255f, 0f});
        systemColors.put("paleturquoise",
                            new float[] {175/255f, 238/255f, 238/255f, 0f});
        systemColors.put("palevioletred1",
                            new float[] {255/255f, 130/255f, 171/255f, 0f});
        systemColors.put("palevioletred2",
                            new float[] {238/255f, 121/255f, 159/255f, 0f});
        systemColors.put("palevioletred3",
                            new float[] {205/255f, 104/255f, 137/255f, 0f});
        systemColors.put("palevioletred4",
                            new float[] {139/255f, 71/255f, 93/255f, 0f});
        systemColors.put("palevioletred",
                            new float[] {219/255f, 112/255f, 147/255f, 0f});
        systemColors.put("papayawhip",
                            new float[] {255/255f, 239/255f, 213/255f, 0f});
        systemColors.put("peachpuff1",
                            new float[] {255/255f, 218/255f, 185/255f, 0f});
        systemColors.put("peachpuff2",
                            new float[] {238/255f, 203/255f, 173/255f, 0f});
        systemColors.put("peachpuff3",
                            new float[] {205/255f, 175/255f, 149/255f, 0f});
        systemColors.put("peachpuff4",
                            new float[] {139/255f, 119/255f, 101/255f, 0f});
        systemColors.put("peachpuff",
                            new float[] {255/255f, 218/255f, 185/255f, 0f});
        systemColors.put("peru",
                            new float[] {205/255f, 133/255f, 63/255f, 0f});
        systemColors.put("pink1",
                            new float[] {255/255f, 181/255f, 197/255f, 0f});
        systemColors.put("pink2",
                            new float[] {238/255f, 169/255f, 184/255f, 0f});
        systemColors.put("pink3",
                            new float[] {205/255f, 145/255f, 158/255f, 0f});
        systemColors.put("pink4",
                            new float[] {139/255f, 99/255f, 108/255f, 0f});
        systemColors.put("pink",
                            new float[] {255/255f, 192/255f, 203/255f, 0f});
        systemColors.put("plum1",
                            new float[] {255/255f, 187/255f, 255/255f, 0f});
        systemColors.put("plum2",
                            new float[] {238/255f, 174/255f, 238/255f, 0f});
        systemColors.put("plum3",
                            new float[] {205/255f, 150/255f, 205/255f, 0f});
        systemColors.put("plum4",
                            new float[] {139/255f, 102/255f, 139/255f, 0f});
        systemColors.put("plum",
                            new float[] {221/255f, 160/255f, 221/255f, 0f});
        systemColors.put("powderblue",
                            new float[] {176/255f, 224/255f, 230/255f, 0f});
        systemColors.put("purple1",
                            new float[] {155/255f, 48/255f, 255/255f, 0f});
        systemColors.put("purple2",
                            new float[] {145/255f, 44/255f, 238/255f, 0f});
        systemColors.put("purple3",
                            new float[] {125/255f, 38/255f, 205/255f, 0f});
        systemColors.put("purple4",
                            new float[] {85/255f, 26/255f, 139/255f, 0f});
        systemColors.put("purple",
                            new float[] {160/255f, 32/255f, 240/255f, 0f});
        systemColors.put("red1",
                            new float[] {255/255f, 0/255f, 0/255f, 0f});
        systemColors.put("red2",
                            new float[] {238/255f, 0/255f, 0/255f, 0f});
        systemColors.put("red3",
                            new float[] {205/255f, 0/255f, 0/255f, 0f});
        systemColors.put("red4",
                            new float[] {139/255f, 0/255f, 0/255f, 0f});
        systemColors.put("red",
                            new float[] {255/255f, 0/255f, 0/255f, 0f});
        systemColors.put("rosybrown1",
                            new float[] {255/255f, 193/255f, 193/255f, 0f});
        systemColors.put("rosybrown2",
                            new float[] {238/255f, 180/255f, 180/255f, 0f});
        systemColors.put("rosybrown3",
                            new float[] {205/255f, 155/255f, 155/255f, 0f});
        systemColors.put("rosybrown4",
                            new float[] {139/255f, 105/255f, 105/255f, 0f});
        systemColors.put("rosybrown",
                            new float[] {188/255f, 143/255f, 143/255f, 0f});
        systemColors.put("royalblue1",
                            new float[] {72/255f, 118/255f, 255/255f, 0f});
        systemColors.put("royalblue2",
                            new float[] {67/255f, 110/255f, 238/255f, 0f});
        systemColors.put("royalblue3",
                            new float[] {58/255f, 95/255f, 205/255f, 0f});
        systemColors.put("royalblue4",
                            new float[] {39/255f, 64/255f, 139/255f, 0f});
        systemColors.put("royalblue",
                            new float[] {65/255f, 105/255f, 225/255f, 0f});
        systemColors.put("saddlebrown",
                            new float[] {139/255f, 69/255f, 19/255f, 0f});
        systemColors.put("salmon1",
                            new float[] {255/255f, 140/255f, 105/255f, 0f});
        systemColors.put("salmon2",
                            new float[] {238/255f, 130/255f, 98/255f, 0f});
        systemColors.put("salmon3",
                            new float[] {205/255f, 112/255f, 84/255f, 0f});
        systemColors.put("salmon4",
                            new float[] {139/255f, 76/255f, 57/255f, 0f});
        systemColors.put("salmon",
                            new float[] {250/255f, 128/255f, 114/255f, 0f});
        systemColors.put("sandybrown",
                            new float[] {244/255f, 164/255f, 96/255f, 0f});
        systemColors.put("seagreen1",
                            new float[] {84/255f, 255/255f, 159/255f, 0f});
        systemColors.put("seagreen2",
                            new float[] {78/255f, 238/255f, 148/255f, 0f});
        systemColors.put("seagreen3",
                            new float[] {67/255f, 205/255f, 128/255f, 0f});
        systemColors.put("seagreen4",
                            new float[] {46/255f, 139/255f, 87/255f, 0f});
        systemColors.put("seagreen",
                            new float[] {46/255f, 139/255f, 87/255f, 0f});
        systemColors.put("seashell1",
                            new float[] {255/255f, 245/255f, 238/255f, 0f});
        systemColors.put("seashell2",
                            new float[] {238/255f, 229/255f, 222/255f, 0f});
        systemColors.put("seashell3",
                            new float[] {205/255f, 197/255f, 191/255f, 0f});
        systemColors.put("seashell4",
                            new float[] {139/255f, 134/255f, 130/255f, 0f});
        systemColors.put("seashell",
                            new float[] {255/255f, 245/255f, 238/255f, 0f});
        systemColors.put("sienna1",
                            new float[] {255/255f, 130/255f, 71/255f, 0f});
        systemColors.put("sienna2",
                            new float[] {238/255f, 121/255f, 66/255f, 0f});
        systemColors.put("sienna3",
                            new float[] {205/255f, 104/255f, 57/255f, 0f});
        systemColors.put("sienna4",
                            new float[] {139/255f, 71/255f, 38/255f, 0f});
        systemColors.put("sienna",
                            new float[] {160/255f, 82/255f, 45/255f, 0f});
        systemColors.put("skyblue1",
                            new float[] {135/255f, 206/255f, 255/255f, 0f});
        systemColors.put("skyblue2",
                            new float[] {126/255f, 192/255f, 238/255f, 0f});
        systemColors.put("skyblue3",
                            new float[] {108/255f, 166/255f, 205/255f, 0f});
        systemColors.put("skyblue4",
                            new float[] {74/255f, 112/255f, 139/255f, 0f});
        systemColors.put("skyblue",
                            new float[] {135/255f, 206/255f, 235/255f, 0f});
        systemColors.put("slateblue1",
                            new float[] {131/255f, 111/255f, 255/255f, 0f});
        systemColors.put("slateblue2",
                            new float[] {122/255f, 103/255f, 238/255f, 0f});
        systemColors.put("slateblue3",
                            new float[] {105/255f, 89/255f, 205/255f, 0f});
        systemColors.put("slateblue4",
                            new float[] {71/255f, 60/255f, 139/255f, 0f});
        systemColors.put("slateblue",
                            new float[] {106/255f, 90/255f, 205/255f, 0f});
        systemColors.put("slategray1",
                            new float[] {198/255f, 226/255f, 255/255f, 0f});
        systemColors.put("slategray2",
                            new float[] {185/255f, 211/255f, 238/255f, 0f});
        systemColors.put("slategray3",
                            new float[] {159/255f, 182/255f, 205/255f, 0f});
        systemColors.put("slategray4",
                            new float[] {108/255f, 123/255f, 139/255f, 0f});
        systemColors.put("slategray",
                            new float[] {112/255f, 128/255f, 144/255f, 0f});
        systemColors.put("slategrey",
                            new float[] {112/255f, 128/255f, 144/255f, 0f});
        systemColors.put("snow1",
                            new float[] {255/255f, 250/255f, 250/255f, 0f});
        systemColors.put("snow2",
                            new float[] {238/255f, 233/255f, 233/255f, 0f});
        systemColors.put("snow3",
                            new float[] {205/255f, 201/255f, 201/255f, 0f});
        systemColors.put("snow4",
                            new float[] {139/255f, 137/255f, 137/255f, 0f});
        systemColors.put("snow",
                            new float[] {255/255f, 250/255f, 250/255f, 0f});
        systemColors.put("springgreen1",
                            new float[] {0/255f, 255/255f, 127/255f, 0f});
        systemColors.put("springgreen2",
                            new float[] {0/255f, 238/255f, 118/255f, 0f});
        systemColors.put("springgreen3",
                            new float[] {0/255f, 205/255f, 102/255f, 0f});
        systemColors.put("springgreen4",
                            new float[] {0/255f, 139/255f, 69/255f, 0f});
        systemColors.put("springgreen",
                            new float[] {0/255f, 255/255f, 127/255f, 0f});
        systemColors.put("steelblue1",
                            new float[] {99/255f, 184/255f, 255/255f, 0f});
        systemColors.put("steelblue2",
                            new float[] {92/255f, 172/255f, 238/255f, 0f});
        systemColors.put("steelblue3",
                            new float[] {79/255f, 148/255f, 205/255f, 0f});
        systemColors.put("steelblue4",
                            new float[] {54/255f, 100/255f, 139/255f, 0f});
        systemColors.put("steelblue",
                            new float[] {70/255f, 130/255f, 180/255f, 0f});
        systemColors.put("tan1",
                            new float[] {255/255f, 165/255f, 79/255f, 0f});
        systemColors.put("tan2",
                            new float[] {238/255f, 154/255f, 73/255f, 0f});
        systemColors.put("tan3",
                            new float[] {205/255f, 133/255f, 63/255f, 0f});
        systemColors.put("tan4",
                            new float[] {139/255f, 90/255f, 43/255f, 0f});
        systemColors.put("tan",
                            new float[] {210/255f, 180/255f, 140/255f, 0f});
        systemColors.put("thistle1",
                            new float[] {255/255f, 225/255f, 255/255f, 0f});
        systemColors.put("thistle2",
                            new float[] {238/255f, 210/255f, 238/255f, 0f});
        systemColors.put("thistle3",
                            new float[] {205/255f, 181/255f, 205/255f, 0f});
        systemColors.put("thistle4",
                            new float[] {139/255f, 123/255f, 139/255f, 0f});
        systemColors.put("thistle",
                            new float[] {216/255f, 191/255f, 216/255f, 0f});
        systemColors.put("tomato1",
                            new float[] {255/255f, 99/255f, 71/255f, 0f});
        systemColors.put("tomato2",
                            new float[] {238/255f, 92/255f, 66/255f, 0f});
        systemColors.put("tomato3",
                            new float[] {205/255f, 79/255f, 57/255f, 0f});
        systemColors.put("tomato4",
                            new float[] {139/255f, 54/255f, 38/255f, 0f});
        systemColors.put("tomato",
                            new float[] {255/255f, 99/255f, 71/255f, 0f});
        systemColors.put("turquoise1",
                            new float[] {0/255f, 245/255f, 255/255f, 0f});
        systemColors.put("turquoise2",
                            new float[] {0/255f, 229/255f, 238/255f, 0f});
        systemColors.put("turquoise3",
                            new float[] {0/255f, 197/255f, 205/255f, 0f});
        systemColors.put("turquoise4",
                            new float[] {0/255f, 134/255f, 139/255f, 0f});
        systemColors.put("turquoise",
                            new float[] {64/255f, 224/255f, 208/255f, 0f});
        systemColors.put("violet",
                            new float[] {238/255f, 130/255f, 238/255f, 0f});
        systemColors.put("violetred1",
                            new float[] {255/255f, 62/255f, 150/255f, 0f});
        systemColors.put("violetred2",
                            new float[] {238/255f, 58/255f, 140/255f, 0f});
        systemColors.put("violetred3",
                            new float[] {205/255f, 50/255f, 120/255f, 0f});
        systemColors.put("violetred4",
                            new float[] {139/255f, 34/255f, 82/255f, 0f});
        systemColors.put("violetred",
                            new float[] {208/255f, 32/255f, 144/255f, 0f});
        systemColors.put("wheat1",
                            new float[] {255/255f, 231/255f, 186/255f, 0f});
        systemColors.put("wheat2",
                            new float[] {238/255f, 216/255f, 174/255f, 0f});
        systemColors.put("wheat3",
                            new float[] {205/255f, 186/255f, 150/255f, 0f});
        systemColors.put("wheat4",
                            new float[] {139/255f, 126/255f, 102/255f, 0f});
        systemColors.put("wheat",
                            new float[] {245/255f, 222/255f, 179/255f, 0f});
        systemColors.put("white",
                            new float[] {255/255f, 255/255f, 255/255f, 0f});
        systemColors.put("whitesmoke",
                            new float[] {245/255f, 245/255f, 245/255f, 0f});
        systemColors.put("yellow1",
                            new float[] {255/255f, 255/255f, 0/255f, 0f});
        systemColors.put("yellow2",
                            new float[] {238/255f, 238/255f, 0/255f, 0f});
        systemColors.put("yellow3",
                            new float[] {205/255f, 205/255f, 0/255f, 0f});
        systemColors.put("yellow4",
                            new float[] {139/255f, 139/255f, 0/255f, 0f});
        systemColors.put("yellowgreen",
                            new float[] {154/255f, 205/255f, 50/255f, 0f});
        systemColors.put("yellow",
                            new float[] {255/255f, 255/255f, 0/255f, 0f});
    }
}
