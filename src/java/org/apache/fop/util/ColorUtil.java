/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.util;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.java2d.color.DeviceCMYKColorSpace;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Generic Color helper class.
 * <p>
 * This class supports parsing string values into color values and creating
 * color values for strings. It provides a list of standard color names.
 */
public final class ColorUtil {

    /** The name for the uncalibrated CMYK pseudo-profile */
    public static final String CMYK_PSEUDO_PROFILE = "#CMYK";

    /**
     *
     * keeps all the predefined and parsed colors.
     * <p>
     * This map is used to predefine given colors, as well as speeding up
     * parsing of already parsed colors.
     */
    private static Map colorMap = null;

    /** Logger instance */
    protected static Log log = LogFactory.getLog(ColorUtil.class);

    static {
        initializeColorMap();
    }

    /**
     * Private constructor since this is an utility class.
     */
    private ColorUtil() {
    }

    /**
     * Creates a color from a given string.
     * <p>
     * This function supports a wide variety of inputs.
     * <ul>
     * <li>#RGB (hex 0..f)</li>
     * <li>#RGBA (hex 0..f)</li>
     * <li>#RRGGBB (hex 00..ff)</li>
     * <li>#RRGGBBAA (hex 00..ff)</li>
     * <li>rgb(r,g,b) (0..255 or 0%..100%)</li>
     * <li>java.awt.Color[r=r,g=g,b=b] (0..255)</li>
     * <li>system-color(colorname)</li>
     * <li>transparent</li>
     * <li>colorname</li>
     * <li>fop-rgb-icc(r,g,b,cs,cs-src,[num]+) (r/g/b: 0..1, num: 0..1)</li>
     * <li>cmyk(c,m,y,k) (0..1)</li>
     * </ul>
     *
     * @param foUserAgent FOUserAgent object
     * @param value
     *            the string to parse.
     * @return a Color representing the string if possible
     * @throws PropertyException
     *             if the string is not parsable or does not follow any of the
     *             given formats.
     */
    public static Color parseColorString(FOUserAgent foUserAgent, String value)
            throws PropertyException {
        if (value == null) {
            return null;
        }

        Color parsedColor = (Color) colorMap.get(value.toLowerCase());

        if (parsedColor == null) {
            if (value.startsWith("#")) {
                parsedColor = parseWithHash(value);
            } else if (value.startsWith("rgb(")) {
                parsedColor = parseAsRGB(value);
            } else if (value.startsWith("url(")) {
                throw new PropertyException(
                        "Colors starting with url( are not yet supported!");
            } else if (value.startsWith("java.awt.Color")) {
                parsedColor = parseAsJavaAWTColor(value);
            } else if (value.startsWith("system-color(")) {
                parsedColor = parseAsSystemColor(value);
            } else if (value.startsWith("fop-rgb-icc")) {
                parsedColor = parseAsFopRgbIcc(foUserAgent, value);
            } else if (value.startsWith("cmyk")) {
                parsedColor = parseAsCMYK(value);
            }

            if (parsedColor == null) {
                throw new PropertyException("Unknown Color: " + value);
            }

            colorMap.put(value, parsedColor);
        }

        // TODO - Returned Color object can be one from the static colorMap cache.
        //        That means it should be treated as read only for the rest of its lifetime.
        //        Not sure that is the case though.
        return parsedColor;
    }

    /**
     * Tries to parse a color given with the system-color() function.
     *
     * @param value
     *            the complete line
     * @return a color if possible
     * @throws PropertyException
     *             if the format is wrong.
     */
    private static Color parseAsSystemColor(String value)
            throws PropertyException {
        int poss = value.indexOf("(");
        int pose = value.indexOf(")");
        if (poss != -1 && pose != -1) {
            value = value.substring(poss + 1, pose);
        } else {
            throw new PropertyException("Unknown color format: " + value
                    + ". Must be system-color(x)");
        }
        return (Color) colorMap.get(value);
    }

    /**
     * Tries to parse the standard java.awt.Color toString output.
     *
     * @param value
     *            the complete line
     * @return a color if possible
     * @throws PropertyException
     *             if the format is wrong.
     * @see java.awt.Color#toString()
     */
    private static Color parseAsJavaAWTColor(String value)
            throws PropertyException {
        float red = 0.0f, green = 0.0f, blue = 0.0f;
        int poss = value.indexOf("[");
        int pose = value.indexOf("]");
        try {
            if (poss != -1 && pose != -1) {
                value = value.substring(poss + 1, pose);
                String[] args = value.split(",");
                if (args.length != 3) {
                    throw new PropertyException(
                            "Invalid number of arguments for a java.awt.Color: " + value);
                }

                red = Float.parseFloat(args[0].trim().substring(2)) / 255f;
                green = Float.parseFloat(args[1].trim().substring(2)) / 255f;
                blue = Float.parseFloat(args[2].trim().substring(2)) / 255f;
                if ((red < 0.0 || red > 1.0)
                        || (green < 0.0 || green > 1.0)
                        || (blue < 0.0 || blue > 1.0)) {
                    throw new PropertyException("Color values out of range");
                }
            } else {
                throw new IllegalArgumentException(
                            "Invalid format for a java.awt.Color: " + value);
            }
        } catch (PropertyException pe) {
            throw pe;
        } catch (Exception e) {
            throw new PropertyException(e);
        }
        return new Color(red, green, blue);
    }

    /**
     * Parse a color given with the rgb() function.
     *
     * @param value
     *            the complete line
     * @return a color if possible
     * @throws PropertyException
     *             if the format is wrong.
     */
    private static Color parseAsRGB(String value) throws PropertyException {
        Color parsedColor;
        int poss = value.indexOf("(");
        int pose = value.indexOf(")");
        if (poss != -1 && pose != -1) {
            value = value.substring(poss + 1, pose);
            try {
                String[] args = value.split(",");
                if (args.length != 3) {
                    throw new PropertyException(
                            "Invalid number of arguments: rgb(" + value + ")");
                }
                float red = 0.0f, green = 0.0f, blue = 0.0f;
                String str = args[0].trim();
                if (str.endsWith("%")) {
                    red = Float.parseFloat(str.substring(0,
                            str.length() - 1)) / 100f;
                } else {
                    red = Float.parseFloat(str) / 255f;
                }
                str = args[1].trim();
                if (str.endsWith("%")) {
                    green = Float.parseFloat(str.substring(0,
                            str.length() - 1)) / 100f;
                } else {
                    green = Float.parseFloat(str) / 255f;
                }
                str = args[2].trim();
                if (str.endsWith("%")) {
                    blue = Float.parseFloat(str.substring(0,
                            str.length() - 1)) / 100f;
                } else {
                    blue = Float.parseFloat(str) / 255f;
                }
                if ((red < 0.0 || red > 1.0)
                        || (green < 0.0 || green > 1.0)
                        || (blue < 0.0 || blue > 1.0)) {
                    throw new PropertyException("Color values out of range");
                }
                parsedColor = new Color(red, green, blue);
            } catch (PropertyException pe) {
                //simply re-throw
                throw pe;
            } catch (Exception e) {
                //wrap in a PropertyException
                throw new PropertyException(e);
            }
        } else {
            throw new PropertyException("Unknown color format: " + value
                    + ". Must be rgb(r,g,b)");
        }
        return parsedColor;
    }

    /**
     * parse a color given in the #.... format.
     *
     * @param value
     *            the complete line
     * @return a color if possible
     * @throws PropertyException
     *             if the format is wrong.
     */
    private static Color parseWithHash(String value) throws PropertyException {
        Color parsedColor = null;
        try {
            int len = value.length();
            int alpha;
            if (len == 5 || len == 9) {
                alpha = Integer.parseInt(
                        value.substring((len == 5) ? 3 : 7), 16);
            } else {
                alpha = 0xFF;
            }
            int red = 0, green = 0, blue = 0;
            if ((len == 4) || (len == 5)) {
                //multiply by 0x11 = 17 = 255/15
                red = Integer.parseInt(value.substring(1, 2), 16) * 0x11;
                green = Integer.parseInt(value.substring(2, 3), 16) * 0x11;
                blue = Integer.parseInt(value.substring(3, 4), 16) * 0X11;
            } else if ((len == 7) || (len == 9)) {
                red = Integer.parseInt(value.substring(1, 3), 16);
                green = Integer.parseInt(value.substring(3, 5), 16);
                blue = Integer.parseInt(value.substring(5, 7), 16);
            } else {
                throw new NumberFormatException();
            }
            parsedColor = new Color(red, green, blue, alpha);
        } catch (Exception e) {
            throw new PropertyException("Unknown color format: " + value
                    + ". Must be #RGB. #RGBA, #RRGGBB, or #RRGGBBAA");
        }
        return parsedColor;
    }

    /**
     * Parse a color specified using the fop-rgb-icc() function.
     *
     * @param value the function call
     * @return a color if possible
     * @throws PropertyException if the format is wrong.
     */
    private static Color parseAsFopRgbIcc(FOUserAgent foUserAgent, String value)
            throws PropertyException {
        Color parsedColor;
        int poss = value.indexOf("(");
        int pose = value.indexOf(")");
        if (poss != -1 && pose != -1) {
            String[] args = value.substring(poss + 1, pose).split(",");

            try {
                if (args.length < 5) {
                    throw new PropertyException("Too few arguments for rgb-icc() function");
                }
                /* Get and verify ICC profile name */
                String iccProfileName = args[3].trim();
                if (iccProfileName == null || "".equals(iccProfileName)) {
                    throw new PropertyException("ICC profile name missing");
                }
                ColorSpace colorSpace = null;
                String iccProfileSrc = null;
                if (isPseudoProfile(iccProfileName)) {
                    if (CMYK_PSEUDO_PROFILE.equalsIgnoreCase(iccProfileName)) {
                        colorSpace = DeviceCMYKColorSpace.getInstance();
                    } else {
                        assert false : "Incomplete implementation";
                    }
                } else {
                    /* Get and verify ICC profile source */
                    iccProfileSrc = args[4].trim();
                    if (iccProfileSrc == null || "".equals(iccProfileSrc)) {
                        throw new PropertyException("ICC profile source missing");
                    }
                    if (iccProfileSrc.startsWith("\"") || iccProfileSrc.startsWith("'")) {
                        iccProfileSrc = iccProfileSrc.substring(1);
                    }
                    if (iccProfileSrc.endsWith("\"") || iccProfileSrc.endsWith("'")) {
                        iccProfileSrc = iccProfileSrc.substring(0, iccProfileSrc.length() - 1);
                    }
                }
                /* ICC profile arguments */
                float[] iccComponents = new float[args.length - 5];
                for (int ix = 4; ++ix < args.length;) {
                    iccComponents[ix - 5] = Float.parseFloat(args[ix].trim());
                }

                float red = 0, green = 0, blue = 0;
                red = Float.parseFloat(args[0].trim());
                green = Float.parseFloat(args[1].trim());
                blue = Float.parseFloat(args[2].trim());
                /* Verify rgb replacement arguments */
                if ((red < 0 || red > 1)
                        || (green < 0 || green > 1)
                        || (blue < 0 || blue > 1)) {
                    throw new PropertyException("Color values out of range. "
                            + "Fallback RGB arguments to fop-rgb-icc() must be [0..1]");
                }

                /* Ask FOP factory to get ColorSpace for the specified ICC profile source */
                if (foUserAgent != null && iccProfileSrc != null) {
                    colorSpace = foUserAgent.getFactory().getColorSpace(
                            foUserAgent.getBaseURL(), iccProfileSrc);
                }
                if (colorSpace != null) {
                    // ColorSpace available - create ColorExt (keeps track of replacement rgb
                    // values for possible later colorTOsRGBString call
                    parsedColor = ColorExt.createFromFoRgbIcc(red, green, blue,
                            iccProfileName, iccProfileSrc, colorSpace, iccComponents);
                } else {
                    // ICC profile could not be loaded - use rgb replacement values */
                    log.warn("Color profile '" + iccProfileSrc
                            + "' not found. Using rgb replacement values.");
                    parsedColor = new Color(Math.round(red * 255),
                            Math.round(green * 255), Math.round(blue * 255));
                }
            } catch (PropertyException pe) {
                //simply re-throw
                throw pe;
            } catch (Exception e) {
                //wrap in a PropertyException
                throw new PropertyException(e);
            }
        } else {
            throw new PropertyException("Unknown color format: " + value
                    + ". Must be fop-rgb-icc(r,g,b,NCNAME,src,....)");
        }
        return parsedColor;
    }

    /**
     * Parse a color given with the cmyk() function.
     *
     * @param value
     *            the complete line
     * @return a color if possible
     * @throws PropertyException
     *             if the format is wrong.
     */
    private static Color parseAsCMYK(String value) throws PropertyException {
        Color parsedColor;
        int poss = value.indexOf("(");
        int pose = value.indexOf(")");
        if (poss != -1 && pose != -1) {
            value = value.substring(poss + 1, pose);
            String[] args = value.split(",");
            try {
                if (args.length != 4) {
                    throw new PropertyException(
                            "Invalid number of arguments: cmyk(" + value + ")");
                }
                float cyan = 0.0f, magenta = 0.0f, yellow = 0.0f, black = 0.0f;
                String str = args[0].trim();
                if (str.endsWith("%")) {
                  cyan  = Float.parseFloat(str.substring(0,
                            str.length() - 1)) / 100.0f;
                } else {
                  cyan  = Float.parseFloat(str);
                }
                str = args[1].trim();
                if (str.endsWith("%")) {
                  magenta = Float.parseFloat(str.substring(0,
                            str.length() - 1)) / 100.0f;
                } else {
                  magenta = Float.parseFloat(str);
                }
                str = args[2].trim();
                if (str.endsWith("%")) {
                  yellow = Float.parseFloat(str.substring(0,
                            str.length() - 1)) / 100.0f;
                } else {
                  yellow = Float.parseFloat(str);
                }
                str = args[3].trim();
                if (str.endsWith("%")) {
                  black = Float.parseFloat(str.substring(0,
                            str.length() - 1)) / 100.0f;
                } else {
                  black = Float.parseFloat(str);
                }

                if ((cyan < 0.0 || cyan > 1.0)
                        || (magenta < 0.0 || magenta > 1.0)
                        || (yellow < 0.0 || yellow > 1.0)
                        || (black < 0.0 || black > 1.0)) {
                    throw new PropertyException("Color values out of range"
                            + "Arguments to cmyk() must be in the range [0%-100%] or [0.0-1.0]");
                }
                float[] cmyk = new float[] {cyan, magenta, yellow, black};
                DeviceCMYKColorSpace cmykCs = DeviceCMYKColorSpace.getInstance();
                float[] rgb = cmykCs.toRGB(cmyk);
                parsedColor = ColorExt.createFromFoRgbIcc(rgb[0], rgb[1], rgb[2],
                        CMYK_PSEUDO_PROFILE, null, cmykCs, cmyk);
            } catch (PropertyException pe) {
                throw pe;
            } catch (Exception e) {
                throw new PropertyException(e);
            }
        } else {
            throw new PropertyException("Unknown color format: " + value
                    + ". Must be cmyk(c,m,y,k)");
        }
        return parsedColor;
    }

    /**
     * Creates a re-parsable string representation of the given color.
     * <p>
     * First, the color will be converted into the sRGB colorspace. It will then
     * be printed as #rrggbb, or as #rrrggbbaa if an alpha value is present.
     *
     * @param color
     *            the color to represent.
     * @return a re-parsable string representadion.
     */
    public static String colorToString(Color color) {
        ColorSpace cs = color.getColorSpace();
        if (color instanceof ColorExt) {
            return ((ColorExt)color).toFunctionCall();
        } else if (cs != null && cs.getType() == ColorSpace.TYPE_CMYK) {
            StringBuffer sbuf = new StringBuffer(24);
            float[] cmyk = color.getColorComponents(null);
            sbuf.append("cmyk(" + cmyk[0] + "," + cmyk[1] + "," + cmyk[2] + "," +  cmyk[3] + ")");
            return sbuf.toString();
        } else {
            StringBuffer sbuf = new StringBuffer();
            sbuf.append('#');
            String s = Integer.toHexString(color.getRed());
            if (s.length() == 1) {
                sbuf.append('0');
            }
            sbuf.append(s);
            s = Integer.toHexString(color.getGreen());
            if (s.length() == 1) {
                sbuf.append('0');
            }
            sbuf.append(s);
            s = Integer.toHexString(color.getBlue());
            if (s.length() == 1) {
                sbuf.append('0');
            }
            sbuf.append(s);
            if (color.getAlpha() != 255) {
                s = Integer.toHexString(color.getAlpha());
                if (s.length() == 1) {
                    sbuf.append('0');
                }
                sbuf.append(s);
            }
            return sbuf.toString();
        }
    }

    /**
     * Initializes the colorMap with some predefined values.
     */
    private static void initializeColorMap() {                  // CSOK: MethodLength
        colorMap = Collections.synchronizedMap(new java.util.HashMap());

        colorMap.put("aliceblue", new Color(240, 248, 255));
        colorMap.put("antiquewhite", new Color(250, 235, 215));
        colorMap.put("aqua", new Color(0, 255, 255));
        colorMap.put("aquamarine", new Color(127, 255, 212));
        colorMap.put("azure", new Color(240, 255, 255));
        colorMap.put("beige", new Color(245, 245, 220));
        colorMap.put("bisque", new Color(255, 228, 196));
        colorMap.put("black", new Color(0, 0, 0));
        colorMap.put("blanchedalmond", new Color(255, 235, 205));
        colorMap.put("blue", new Color(0, 0, 255));
        colorMap.put("blueviolet", new Color(138, 43, 226));
        colorMap.put("brown", new Color(165, 42, 42));
        colorMap.put("burlywood", new Color(222, 184, 135));
        colorMap.put("cadetblue", new Color(95, 158, 160));
        colorMap.put("chartreuse", new Color(127, 255, 0));
        colorMap.put("chocolate", new Color(210, 105, 30));
        colorMap.put("coral", new Color(255, 127, 80));
        colorMap.put("cornflowerblue", new Color(100, 149, 237));
        colorMap.put("cornsilk", new Color(255, 248, 220));
        colorMap.put("crimson", new Color(220, 20, 60));
        colorMap.put("cyan", new Color(0, 255, 255));
        colorMap.put("darkblue", new Color(0, 0, 139));
        colorMap.put("darkcyan", new Color(0, 139, 139));
        colorMap.put("darkgoldenrod", new Color(184, 134, 11));
        colorMap.put("darkgray", new Color(169, 169, 169));
        colorMap.put("darkgreen", new Color(0, 100, 0));
        colorMap.put("darkgrey", new Color(169, 169, 169));
        colorMap.put("darkkhaki", new Color(189, 183, 107));
        colorMap.put("darkmagenta", new Color(139, 0, 139));
        colorMap.put("darkolivegreen", new Color(85, 107, 47));
        colorMap.put("darkorange", new Color(255, 140, 0));
        colorMap.put("darkorchid", new Color(153, 50, 204));
        colorMap.put("darkred", new Color(139, 0, 0));
        colorMap.put("darksalmon", new Color(233, 150, 122));
        colorMap.put("darkseagreen", new Color(143, 188, 143));
        colorMap.put("darkslateblue", new Color(72, 61, 139));
        colorMap.put("darkslategray", new Color(47, 79, 79));
        colorMap.put("darkslategrey", new Color(47, 79, 79));
        colorMap.put("darkturquoise", new Color(0, 206, 209));
        colorMap.put("darkviolet", new Color(148, 0, 211));
        colorMap.put("deeppink", new Color(255, 20, 147));
        colorMap.put("deepskyblue", new Color(0, 191, 255));
        colorMap.put("dimgray", new Color(105, 105, 105));
        colorMap.put("dimgrey", new Color(105, 105, 105));
        colorMap.put("dodgerblue", new Color(30, 144, 255));
        colorMap.put("firebrick", new Color(178, 34, 34));
        colorMap.put("floralwhite", new Color(255, 250, 240));
        colorMap.put("forestgreen", new Color(34, 139, 34));
        colorMap.put("fuchsia", new Color(255, 0, 255));
        colorMap.put("gainsboro", new Color(220, 220, 220));
        colorMap.put("ghostwhite", new Color(248, 248, 255));
        colorMap.put("gold", new Color(255, 215, 0));
        colorMap.put("goldenrod", new Color(218, 165, 32));
        colorMap.put("gray", new Color(128, 128, 128));
        colorMap.put("green", new Color(0, 128, 0));
        colorMap.put("greenyellow", new Color(173, 255, 47));
        colorMap.put("grey", new Color(128, 128, 128));
        colorMap.put("honeydew", new Color(240, 255, 240));
        colorMap.put("hotpink", new Color(255, 105, 180));
        colorMap.put("indianred", new Color(205, 92, 92));
        colorMap.put("indigo", new Color(75, 0, 130));
        colorMap.put("ivory", new Color(255, 255, 240));
        colorMap.put("khaki", new Color(240, 230, 140));
        colorMap.put("lavender", new Color(230, 230, 250));
        colorMap.put("lavenderblush", new Color(255, 240, 245));
        colorMap.put("lawngreen", new Color(124, 252, 0));
        colorMap.put("lemonchiffon", new Color(255, 250, 205));
        colorMap.put("lightblue", new Color(173, 216, 230));
        colorMap.put("lightcoral", new Color(240, 128, 128));
        colorMap.put("lightcyan", new Color(224, 255, 255));
        colorMap.put("lightgoldenrodyellow", new Color(250, 250, 210));
        colorMap.put("lightgray", new Color(211, 211, 211));
        colorMap.put("lightgreen", new Color(144, 238, 144));
        colorMap.put("lightgrey", new Color(211, 211, 211));
        colorMap.put("lightpink", new Color(255, 182, 193));
        colorMap.put("lightsalmon", new Color(255, 160, 122));
        colorMap.put("lightseagreen", new Color(32, 178, 170));
        colorMap.put("lightskyblue", new Color(135, 206, 250));
        colorMap.put("lightslategray", new Color(119, 136, 153));
        colorMap.put("lightslategrey", new Color(119, 136, 153));
        colorMap.put("lightsteelblue", new Color(176, 196, 222));
        colorMap.put("lightyellow", new Color(255, 255, 224));
        colorMap.put("lime", new Color(0, 255, 0));
        colorMap.put("limegreen", new Color(50, 205, 50));
        colorMap.put("linen", new Color(250, 240, 230));
        colorMap.put("magenta", new Color(255, 0, 255));
        colorMap.put("maroon", new Color(128, 0, 0));
        colorMap.put("mediumaquamarine", new Color(102, 205, 170));
        colorMap.put("mediumblue", new Color(0, 0, 205));
        colorMap.put("mediumorchid", new Color(186, 85, 211));
        colorMap.put("mediumpurple", new Color(147, 112, 219));
        colorMap.put("mediumseagreen", new Color(60, 179, 113));
        colorMap.put("mediumslateblue", new Color(123, 104, 238));
        colorMap.put("mediumspringgreen", new Color(0, 250, 154));
        colorMap.put("mediumturquoise", new Color(72, 209, 204));
        colorMap.put("mediumvioletred", new Color(199, 21, 133));
        colorMap.put("midnightblue", new Color(25, 25, 112));
        colorMap.put("mintcream", new Color(245, 255, 250));
        colorMap.put("mistyrose", new Color(255, 228, 225));
        colorMap.put("moccasin", new Color(255, 228, 181));
        colorMap.put("navajowhite", new Color(255, 222, 173));
        colorMap.put("navy", new Color(0, 0, 128));
        colorMap.put("oldlace", new Color(253, 245, 230));
        colorMap.put("olive", new Color(128, 128, 0));
        colorMap.put("olivedrab", new Color(107, 142, 35));
        colorMap.put("orange", new Color(255, 165, 0));
        colorMap.put("orangered", new Color(255, 69, 0));
        colorMap.put("orchid", new Color(218, 112, 214));
        colorMap.put("palegoldenrod", new Color(238, 232, 170));
        colorMap.put("palegreen", new Color(152, 251, 152));
        colorMap.put("paleturquoise", new Color(175, 238, 238));
        colorMap.put("palevioletred", new Color(219, 112, 147));
        colorMap.put("papayawhip", new Color(255, 239, 213));
        colorMap.put("peachpuff", new Color(255, 218, 185));
        colorMap.put("peru", new Color(205, 133, 63));
        colorMap.put("pink", new Color(255, 192, 203));
        colorMap.put("plum ", new Color(221, 160, 221));
        colorMap.put("plum", new Color(221, 160, 221));
        colorMap.put("powderblue", new Color(176, 224, 230));
        colorMap.put("purple", new Color(128, 0, 128));
        colorMap.put("red", new Color(255, 0, 0));
        colorMap.put("rosybrown", new Color(188, 143, 143));
        colorMap.put("royalblue", new Color(65, 105, 225));
        colorMap.put("saddlebrown", new Color(139, 69, 19));
        colorMap.put("salmon", new Color(250, 128, 114));
        colorMap.put("sandybrown", new Color(244, 164, 96));
        colorMap.put("seagreen", new Color(46, 139, 87));
        colorMap.put("seashell", new Color(255, 245, 238));
        colorMap.put("sienna", new Color(160, 82, 45));
        colorMap.put("silver", new Color(192, 192, 192));
        colorMap.put("skyblue", new Color(135, 206, 235));
        colorMap.put("slateblue", new Color(106, 90, 205));
        colorMap.put("slategray", new Color(112, 128, 144));
        colorMap.put("slategrey", new Color(112, 128, 144));
        colorMap.put("snow", new Color(255, 250, 250));
        colorMap.put("springgreen", new Color(0, 255, 127));
        colorMap.put("steelblue", new Color(70, 130, 180));
        colorMap.put("tan", new Color(210, 180, 140));
        colorMap.put("teal", new Color(0, 128, 128));
        colorMap.put("thistle", new Color(216, 191, 216));
        colorMap.put("tomato", new Color(255, 99, 71));
        colorMap.put("turquoise", new Color(64, 224, 208));
        colorMap.put("violet", new Color(238, 130, 238));
        colorMap.put("wheat", new Color(245, 222, 179));
        colorMap.put("white", new Color(255, 255, 255));
        colorMap.put("whitesmoke", new Color(245, 245, 245));
        colorMap.put("yellow", new Color(255, 255, 0));
        colorMap.put("yellowgreen", new Color(154, 205, 50));
        colorMap.put("transparent", new Color(0, 0, 0, 0));
    }

    /**
     * Lightens up a color for groove, ridge, inset and outset border effects.
     * @param col the color to lighten up
     * @param factor factor by which to lighten up (negative values darken the color)
     * @return the modified color
     */
    public static Color lightenColor(Color col, float factor) {
        return org.apache.xmlgraphics.java2d.color.ColorUtil.lightenColor(col, factor);
    }

    /**
     * Indicates whether the given color profile name is one of the pseudo-profiles supported
     * by FOP (ex. #CMYK).
     * @param colorProfileName the color profile name to check
     * @return true if the color profile name is of a built-in pseudo-profile
     */
    public static boolean isPseudoProfile(String colorProfileName) {
        return CMYK_PSEUDO_PROFILE.equalsIgnoreCase(colorProfileName);
    }

    /**
     * Indicates whether the color is a gray value.
     * @param col the color
     * @return true if it is a gray value
     */
    public static boolean isGray(Color col) {
        return org.apache.xmlgraphics.java2d.color.ColorUtil.isGray(col);
    }

    /**
     * Creates an uncalibrated CMYK color with the given gray value.
     * @param black the gray component (0 - 1)
     * @return the CMYK color
     */
    public static Color toCMYKGrayColor(float black) {
        float[] cmyk = new float[] {0f, 0f, 0f, 1.0f - black};
        DeviceCMYKColorSpace cmykCs = DeviceCMYKColorSpace.getInstance();
        float[] rgb = cmykCs.toRGB(cmyk);
        return ColorExt.createFromFoRgbIcc(rgb[0], rgb[1], rgb[2],
                CMYK_PSEUDO_PROFILE, null, cmykCs, cmyk);
    }

}
