/*
 * Copyright 1999-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: Trait.java,v 1.4 2004/02/27 17:41:26 jeremias Exp $ */

package org.apache.fop.area;

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.image.FopImage;
import org.apache.fop.traits.BorderProps;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

// properties should be serialized by the holder
/**
 * Area traits used for rendering.
 * This class represents an area trait that specifies a value for rendering.
 */
public class Trait implements Serializable {
    /**
     * Id reference line, not resolved.
     * not sure if this is needed.
     */
    public static final Integer ID_LINK = new Integer(0);

    /**
     * Internal link trait.
     * This is resolved and provides a link to an internal area.
     */
    public static final Integer INTERNAL_LINK = new Integer(1); //resolved

    /**
     * External link. A URL link to an external resource.
     */
    public static final Integer EXTERNAL_LINK = new Integer(2);

    /**
     * The font name from the font setup.
     */
    public static final Integer FONT_NAME = new Integer(3);

    /**
     * Font size for the current font.
     */
    public static final Integer FONT_SIZE = new Integer(4);

    /**
     * The current colour.
     */
    public static final Integer COLOR = new Integer(7);

    /**
     * Don't think this is necessary.
     */
    public static final Integer ID_AREA = new Integer(8);

    /**
     * Background trait for an area.
     */
    public static final Integer BACKGROUND = new Integer(9);

    /**
     * Underline trait used when rendering inline parent.
     */
    public static final Integer UNDERLINE = new Integer(10);

    /**
     * Overline trait used when rendering inline parent.
     */
    public static final Integer OVERLINE = new Integer(11);

    /**
     * Linethrough trait used when rendering inline parent.
     */
    public static final Integer LINETHROUGH = new Integer(12);

    /**
     * Shadow offset.
     */
    public static final Integer OFFSET = new Integer(13);

    /**
     * The shadow for text.
     */
    public static final Integer SHADOW = new Integer(14);

    /**
     * The border start.
     */
    public static final Integer BORDER_START = new Integer(15);

    /**
     * The border end.
     */
    public static final Integer BORDER_END = new Integer(16);

    /**
     * The border before.
     */
    public static final Integer BORDER_BEFORE = new Integer(17);

    /**
     * The border after.
     */
    public static final Integer BORDER_AFTER = new Integer(18);

    /**
     * The padding start.
     */
    public static final Integer PADDING_START = new Integer(19);

    /**
     * The padding end.
     */
    public static final Integer PADDING_END = new Integer(20);

    /**
     * The padding before.
     */
    public static final Integer PADDING_BEFORE = new Integer(21);

    /**
     * The padding after.
     */
    public static final Integer PADDING_AFTER = new Integer(22);

    /**
     * The space start.
     */
    public static final Integer SPACE_START = new Integer(23);

    /**
     * The space end.
     */
    public static final Integer SPACE_END  = new Integer(24);

    /**
     * break before
     */
    public static final Integer BREAK_BEFORE = new Integer(25);

    /**
     * break after
     */
    public static final Integer BREAK_AFTER = new Integer(26);

    /**
     * The start-indent trait.
     */
    public static final Integer START_INDENT = new Integer(27);

    /**
     * The end-indent trait.
     */
    public static final Integer END_INDENT  = new Integer(28);

    /** The space-before trait. */
    public static final Integer SPACE_BEFORE  = new Integer(29);
    
    /** The space-after trait. */
    public static final Integer SPACE_AFTER  = new Integer(30);
    
    /** The is-reference-area trait. */
    public static final Integer IS_REFERENCE_AREA = new Integer(31);
    
    /** The is-viewport-area trait. */
    public static final Integer IS_VIEWPORT_AREA = new Integer(32);
    
    /** Blinking trait used when rendering inline parent. */
    public static final Integer BLINK = new Integer(33);
    
    /** Trait for color of underline decorations when rendering inline parent. */
    public static final Integer UNDERLINE_COLOR = new Integer(34);
    /** Trait for color of overline decorations when rendering inline parent. */
    public static final Integer OVERLINE_COLOR = new Integer(35);
    /** Trait for color of linethrough decorations when rendering inline parent. */
    public static final Integer LINETHROUGH_COLOR = new Integer(36);
    
    private static final Map TRAIT_INFO = new HashMap();

    private static class TraitInfo {
        private String name;
        private Class clazz; // Class of trait data

        public TraitInfo(String name, Class clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public String getName() {
            return this.name;
        }

        public Class getClazz() {
            return this.clazz;
        }
    }

    static {
        // Create a hashmap mapping trait code to name for external representation
        TRAIT_INFO.put(ID_LINK, new TraitInfo("id-link", String.class));
        TRAIT_INFO.put(INTERNAL_LINK,
                          new TraitInfo("internal-link", String.class));
        TRAIT_INFO.put(EXTERNAL_LINK,
                          new TraitInfo("external-link", String.class));
        TRAIT_INFO.put(FONT_NAME,
                          new TraitInfo("font-family", String.class));
        TRAIT_INFO.put(FONT_SIZE,
                          new TraitInfo("font-size", Integer.class));
        TRAIT_INFO.put(COLOR, new TraitInfo("color", String.class));
        TRAIT_INFO.put(ID_AREA, new TraitInfo("id-area", String.class));
        TRAIT_INFO.put(BACKGROUND,
                          new TraitInfo("background", Background.class));
        TRAIT_INFO.put(UNDERLINE,
                          new TraitInfo("underline-score", Boolean.class));
        TRAIT_INFO.put(UNDERLINE_COLOR, new TraitInfo("underline-score-color", String.class));
        TRAIT_INFO.put(OVERLINE,
                          new TraitInfo("overline-score", Boolean.class));
        TRAIT_INFO.put(OVERLINE_COLOR, new TraitInfo("overline-score-color", String.class));
        TRAIT_INFO.put(LINETHROUGH,
                          new TraitInfo("through-score", Boolean.class));
        TRAIT_INFO.put(LINETHROUGH_COLOR, new TraitInfo("through-score-color", String.class));
        TRAIT_INFO.put(BLINK,
                          new TraitInfo("blink", Boolean.class));
        TRAIT_INFO.put(OFFSET, new TraitInfo("offset", Integer.class));
        TRAIT_INFO.put(SHADOW, new TraitInfo("shadow", Integer.class));
        TRAIT_INFO.put(BORDER_START,
                          new TraitInfo("border-start", BorderProps.class));
        TRAIT_INFO.put(BORDER_END,
                          new TraitInfo("border-end", BorderProps.class));
        TRAIT_INFO.put(BORDER_BEFORE,
                          new TraitInfo("border-before", BorderProps.class));
        TRAIT_INFO.put(BORDER_AFTER,
                          new TraitInfo("border-after", BorderProps.class));
        TRAIT_INFO.put(PADDING_START,
                          new TraitInfo("padding-start", Integer.class));
        TRAIT_INFO.put(PADDING_END,
                          new TraitInfo("padding-end", Integer.class));
        TRAIT_INFO.put(PADDING_BEFORE,
                          new TraitInfo("padding-before", Integer.class));
        TRAIT_INFO.put(PADDING_AFTER,
                          new TraitInfo("padding-after", Integer.class));
        TRAIT_INFO.put(SPACE_START,
                          new TraitInfo("space-start", Integer.class));
        TRAIT_INFO.put(SPACE_END,
                          new TraitInfo("space-end", Integer.class));
        TRAIT_INFO.put(BREAK_BEFORE,
                          new TraitInfo("break-before", Integer.class));
        TRAIT_INFO.put(BREAK_AFTER,
                          new TraitInfo("break-after", Integer.class));
        TRAIT_INFO.put(START_INDENT,
                new TraitInfo("start-indent", Integer.class));
        TRAIT_INFO.put(END_INDENT,
                new TraitInfo("end-indent", Integer.class));
        TRAIT_INFO.put(SPACE_BEFORE,
                new TraitInfo("space-before", Integer.class));
        TRAIT_INFO.put(SPACE_AFTER,
                new TraitInfo("space-after", Integer.class));
        TRAIT_INFO.put(IS_REFERENCE_AREA,
                new TraitInfo("is-reference-area", Boolean.class));
        TRAIT_INFO.put(IS_VIEWPORT_AREA,
                new TraitInfo("is-viewport-area", Boolean.class));
    }

    /**
     * Get the trait name for a trait code.
     *
     * @param traitCode the trait code to get the name for
     * @return the trait name
     */
    public static String getTraitName(Object traitCode) {
        Object obj = TRAIT_INFO.get(traitCode);
        if (obj != null) {
            return ((TraitInfo) obj).getName();
        } else {
            return "unknown-trait-" + traitCode.toString();
        }
    }

    /**
     * Get the trait code for a trait name.
     *
     * @param sTraitName the name of the trait to find
     * @return the trait code object
     */
    public static Object getTraitCode(String sTraitName) {
        Iterator iter = TRAIT_INFO.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            TraitInfo ti = (TraitInfo) entry.getValue();
            if (ti != null && ti.getName().equals(sTraitName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get the data storage class for the trait.
     *
     * @param oTraitCode the trait code to lookup
     * @return the class type for the trait
     */
    private static Class getTraitClass(Object oTraitCode) {
        TraitInfo ti = (TraitInfo) TRAIT_INFO.get(oTraitCode);
        return (ti != null ? ti.getClazz() : null);
    }

    /**
     * The type of trait for an area.
     */
    private Object propType;

    /**
     * The data value of the trait.
     */
    private Object data;

    /**
     * Create a new empty trait.
     */
    public Trait() {
        this.propType = null;
        this.data = null;
    }

    /**
     * Create a trait with the value and type.
     *
     * @param propType the type of trait
     * @param data the data value
     */
    public Trait(Object propType, Object data) {
        this.propType = propType;
        this.data = data;
    }

    /**
     * Returns the trait data value.
     * @return the trait data value
     */
    public Object getData() {
        return this.data;
    }

    /**
     * Returns the property type.
     * @return the property type
     */
    public Object getPropType() {
        return this.propType;
    }

    /**
     * Return the string for debugging.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return data.toString();
    }

    /**
     * Make a trait value.
     *
     * @param oCode trait code
     * @param sTraitValue trait value as String
     * @return the trait value as object
     */
    public static Object makeTraitValue(Object oCode, String sTraitValue) {
        // Get the code from the name
        // See what type of object it is
        // Convert string value to an object of that type
        Class tclass = getTraitClass(oCode);
        if (tclass == null) {
            return null;
        }
        if (tclass.equals(String.class)) {
            return sTraitValue;
        }
        if (tclass.equals(Integer.class)) {
            return new Integer(sTraitValue);
        }
        // See if the class has a constructor from string or can read from a string
        try {
            Object o = tclass.newInstance();
            //return o.fromString(sTraitValue);
        } catch (IllegalAccessException e1) {
            System.err.println("Can't create instance of "
                               + tclass.getName());
            return null;
        } catch (InstantiationException e2) {
            System.err.println("Can't create instance of "
                               + tclass.getName());
            return null;
        }


        return null;
    }

    /**
     * Background trait structure.
     * Used for storing back trait information which are related.
     */
    public static class Background implements Serializable {

        /** The background color if any. */
        private ColorType color = null;

        /** The background image url if any. */
        private String url = null;
        
        /** The background image if any. */
        private FopImage fopimage = null;

        /** Background repeat enum for images. */
        private int repeat;

        /** Background horizontal offset for images. */
        private int horiz;

        /** Background vertical offset for images. */
        private int vertical;

        /**
         * Returns the background color.
         * @return background color, null if n/a
         */
        public ColorType getColor() {
            return color;
        }

        /**
         * Returns the horizontal offset for images.
         * @return the horizontal offset
         */
        public int getHoriz() {
            return horiz;
        }

        /**
         * Returns the image repetition behaviour for images.
         * @return the image repetition behaviour
         */
        public int getRepeat() {
            return repeat;
        }

        /**
         * Returns the URL to the background image
         * @return URL to the background image, null if n/a
         */
        public String getURL() {
            return url;
        }

        /**
         * Returns the FopImage representing the background image
         * @return the background image, null if n/a
         */
        public FopImage getFopImage() {
            return fopimage;
        }

        /**
         * Returns the vertical offset for images.
         * @return the vertical offset
         */
        public int getVertical() {
            return vertical;
        }

        /**
         * Sets the color.
         * @param color The color to set
         */
        public void setColor(ColorType color) {
            this.color = color;
        }

        /**
         * Sets the horizontal offset.
         * @param horiz The horizontal offset to set
         */
        public void setHoriz(int horiz) {
            this.horiz = horiz;
        }

        /**
         * Sets the image repetition behaviour for images.
         * @param repeat The image repetition behaviour to set
         */
        public void setRepeat(int repeat) {
            this.repeat = repeat;
        }

        /**
         * Sets the URL to the background image.
         * @param url The URL to set
         */
        public void setURL(String url) {
            this.url = url;
        }

        /**
         * Sets the FopImage to use as the background image.
         * @param fopimage The FopImage to use
         */
        public void setFopImage(FopImage fopimage) {
            this.fopimage = fopimage;
        }

        /**
         * Sets the vertical offset for images.
         * @param vertical The vertical offset to set
         */
        public void setVertical(int vertical) {
            this.vertical = vertical;
        }

       /**
         * Return the string for debugging.
         * @see java.lang.Object#toString()
         */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("color=" + color);
            if (url != null) {
                sb.append(",url=");
                sb.append(url);
            }
            sb.append(",repeat=" + repeat);
            sb.append(",horiz=" + horiz);
            sb.append(",vertical=" + vertical);
            return sb.toString();
        }

    }
}

