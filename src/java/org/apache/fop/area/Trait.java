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

package org.apache.fop.area;

import java.awt.Color;
import java.io.Serializable;

import org.apache.xmlgraphics.image.loader.ImageInfo;

import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.util.ColorUtil;

// properties should be serialized by the holder
/**
 * Area traits used for rendering.
 * This class represents an area trait that specifies a value for rendering.
 */
public final class Trait implements Serializable {

    private Trait() {
    }

    /**
     * Id reference line, not resolved.
     * not sure if this is needed.
     */
    //public static final Integer ID_LINK = new Integer(0);

    /**
     * Internal link trait.
     * Contains the PageViewport key and the PROD_ID of the target area
     */
    public static final Integer INTERNAL_LINK = new Integer(1);

    /**
     * External link. A URL link to an external resource.
     */
    public static final Integer EXTERNAL_LINK = new Integer(2);

    /**
     * The font triplet for the current font.
     */
    public static final Integer FONT = new Integer(3);

    /**
     * Font size for the current font.
     */
    public static final Integer FONT_SIZE = new Integer(4);

    /**
     * The current color.
     */
    public static final Integer COLOR = new Integer(7);

    /**
     * The ID of the FO that produced an area.
     */
    public static final Integer PROD_ID = new Integer(8);

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
    //public static final Integer OFFSET = new Integer(13);

    /**
     * The shadow for text.
     */
    //public static final Integer SHADOW = new Integer(14);

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
    //public static final Integer BREAK_BEFORE = new Integer(25);

    /**
     * break after
     */
    //public static final Integer BREAK_AFTER = new Integer(26);

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

    /** The ptr trait. Used for accessibility   */
    public static final Integer PTR = new Integer(37);


    /** Maximum value used by trait keys */
    public static final int MAX_TRAIT_KEY = 38;

    private static final TraitInfo[] TRAIT_INFO = new TraitInfo[MAX_TRAIT_KEY + 1];

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

    private static void put(Integer key, TraitInfo info) {
        TRAIT_INFO[key.intValue()] = info;
    }

    static {
        // Create a hashmap mapping trait code to name for external representation
        //put(ID_LINK, new TraitInfo("id-link", String.class));
        put(PTR, new TraitInfo("ptr", String.class));
        put(INTERNAL_LINK, new TraitInfo("internal-link", InternalLink.class));
        put(EXTERNAL_LINK, new TraitInfo("external-link", ExternalLink.class));
        put(FONT,         new TraitInfo("font", FontTriplet.class));
        put(FONT_SIZE,    new TraitInfo("font-size", Integer.class));
        put(COLOR, new TraitInfo("color", Color.class));
        put(PROD_ID, new TraitInfo("prod-id", String.class));
        put(BACKGROUND,   new TraitInfo("background", Background.class));
        put(UNDERLINE,    new TraitInfo("underline-score", Boolean.class));
        put(UNDERLINE_COLOR, new TraitInfo("underline-score-color", Color.class));
        put(OVERLINE,     new TraitInfo("overline-score", Boolean.class));
        put(OVERLINE_COLOR, new TraitInfo("overline-score-color", Color.class));
        put(LINETHROUGH,  new TraitInfo("through-score", Boolean.class));
        put(LINETHROUGH_COLOR, new TraitInfo("through-score-color", Color.class));
        put(BLINK,        new TraitInfo("blink", Boolean.class));
        //put(OFFSET, new TraitInfo("offset", Integer.class));
        //put(SHADOW, new TraitInfo("shadow", Integer.class));
        put(BORDER_START,
                          new TraitInfo("border-start", BorderProps.class));
        put(BORDER_END,
                          new TraitInfo("border-end", BorderProps.class));
        put(BORDER_BEFORE,
                          new TraitInfo("border-before", BorderProps.class));
        put(BORDER_AFTER,
                          new TraitInfo("border-after", BorderProps.class));
        put(PADDING_START,
                          new TraitInfo("padding-start", Integer.class));
        put(PADDING_END,
                          new TraitInfo("padding-end", Integer.class));
        put(PADDING_BEFORE,
                          new TraitInfo("padding-before", Integer.class));
        put(PADDING_AFTER,
                          new TraitInfo("padding-after", Integer.class));
        put(SPACE_START,
                          new TraitInfo("space-start", Integer.class));
        put(SPACE_END,
                          new TraitInfo("space-end", Integer.class));
        //put(BREAK_BEFORE,
        //                  new TraitInfo("break-before", Integer.class));
        //put(BREAK_AFTER,
        //                  new TraitInfo("break-after", Integer.class));
        put(START_INDENT,
                new TraitInfo("start-indent", Integer.class));
        put(END_INDENT,
                new TraitInfo("end-indent", Integer.class));
        put(SPACE_BEFORE,
                new TraitInfo("space-before", Integer.class));
        put(SPACE_AFTER,
                new TraitInfo("space-after", Integer.class));
        put(IS_REFERENCE_AREA,
                new TraitInfo("is-reference-area", Boolean.class));
        put(IS_VIEWPORT_AREA,
                new TraitInfo("is-viewport-area", Boolean.class));

    }

    /**
     * Get the trait name for a trait code.
     *
     * @param traitCode the trait code to get the name for
     * @return the trait name
     */
    public static String getTraitName(Object traitCode) {
        return TRAIT_INFO[((Integer)traitCode).intValue()].getName();
    }

    /**
     * Get the data storage class for the trait.
     *
     * @param traitCode the trait code to lookup
     * @return the class type for the trait
     */
    public static Class getTraitClass(Object traitCode) {
        return TRAIT_INFO[((Integer)traitCode).intValue()].getClazz();
    }

    /**
     * Class for internal link traits.
     * Stores PageViewport key and producer ID
     */
    public static class InternalLink implements Serializable {

        /** The unique key of the PageViewport. */
        private String pvKey;

        /** The PROD_ID of the link target */
        private String idRef;

        /**
         * Create an InternalLink to the given PageViewport and target ID
         *
         * @param pvKey the PageViewport key
         * @param idRef the target ID
         */
        public InternalLink(String pvKey, String idRef) {
            setPVKey(pvKey);
            setIDRef(idRef);
        }

        /**
         * Create an InternalLink based on the given XML attribute value.
         * This is typically called when data are read from an XML area tree.
         *
         * @param attrValue attribute value to be parsed by InternalLink.parseXMLAttribute
         */
        public InternalLink(String attrValue) {
            String[] values = parseXMLAttribute(attrValue);
            setPVKey(values[0]);
            setIDRef(values[1]);
        }

        /**
         * Sets the key of the targeted PageViewport.
         *
         * @param pvKey the PageViewport key
         */
        public void setPVKey(String pvKey) {
            this.pvKey = pvKey;
        }

        /**
         * Returns the key of the targeted PageViewport.
         *
         * @return the PageViewport key
         */
        public String getPVKey() {
            return pvKey;
        }

        /**
         * Sets the target ID.
         *
         * @param idRef the target ID
         */
        public void setIDRef(String idRef) {
            this.idRef = idRef;
        }

        /**
         * Returns the target ID.
         *
         * @return the target ID
         */
        public String getIDRef() {
            return idRef;
        }

       /**
        * Returns the attribute value for this object as
        * used in the area tree XML.
        *
        * @return a string of the type "(thisPVKey,thisIDRef)"
        */
       public String xmlAttribute() {
           return makeXMLAttribute(pvKey, idRef);
       }

       /**
        * Returns the XML attribute value for the given PV key and ID ref.
        * This value is used in the area tree XML.
        *
        * @param pvKey the PageViewport key of the link target
        * @param idRef the ID of the link target
        * @return a string of the type "(thisPVKey,thisIDRef)"
        */
       public static String makeXMLAttribute(String pvKey, String idRef) {
           return "(" + (pvKey == null ? "" : pvKey) + ","
                      + (idRef == null ? "" : idRef) + ")";
       }

       /**
        * Parses XML attribute value from the area tree into
        * PageViewport key + IDRef strings. If the attribute value is
        * formatted like "(s1,s2)", then s1 and s2 are considered to be
        * the PV key and the IDRef, respectively.
        * Otherwise, the entire string is the PV key and the IDRef is null.
        *
        * @param attrValue the atribute value (PV key and possibly IDRef)
        * @return a 2-String array containing the PV key and the IDRef.
        * Both may be null.
        */
       public static String[] parseXMLAttribute(String attrValue) {
           String[] result = {null, null};
           if (attrValue != null) {
              int len = attrValue.length();
              if (len >= 2 && attrValue.charAt(0) == '(' && attrValue.charAt(len - 1) == ')'
                      && attrValue.indexOf(',') != -1) {
                  String value = attrValue.substring(1, len - 1); // remove brackets
                  int delimIndex = value.indexOf(',');
                  result[0] = value.substring(0, delimIndex).trim(); // PV key
                  result[1] = value.substring(delimIndex + 1, value.length()).trim(); // IDRef
              } else {
                  // PV key only, e.g. from old area tree XML:
                  result[0] = attrValue;
              }
           }
           return result;
       }

        /**
         * Return the human-friendly string for debugging.
         * {@inheritDoc}
         */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("pvKey=").append(pvKey);
            sb.append(",idRef=").append(idRef);
            return sb.toString();
        }
    }

    /**
     * External Link trait structure
     */
    public static class ExternalLink implements Serializable {

        private String destination;
        private boolean newWindow;

        /**
         * Constructs an ExternalLink object with the given destination
         *
         * @param destination   target of the link
         * @param newWindow     true if the target should be opened in a new window
         */
        public ExternalLink(String destination, boolean newWindow) {
            this.destination = destination;
            this.newWindow = newWindow;
        }

        /**
         * Create an <code>ExternalLink</code> from a trait value/attribute value in the
         * area tree
         * @param traitValue    the value to use (should match the result of {@link #toString()}
         * @return an <code>ExternalLink</code> instance corresponding to the given value
         */
        protected static ExternalLink makeFromTraitValue(String traitValue) {
            String dest = null;
            boolean newWindow = false;
            String[] values = traitValue.split(",");
            for (int i = 0, c = values.length; i < c; i++) {
                String v = values[i];
                if (v.startsWith("dest=")) {
                    dest = v.substring(5);
                } else if (v.startsWith("newWindow=")) {
                    newWindow = Boolean.valueOf(v.substring(10)).booleanValue();
                } else {
                    throw new IllegalArgumentException(
                            "Malformed trait value for Trait.ExternalLink: " + traitValue);
                }
            }
            return new ExternalLink(dest, newWindow);
        }

        /**
         * Get the target/destination of the link
         * @return  the destination of the link
         */
        public String getDestination() {
            return this.destination;
        }

        /**
         * Check if the target has to be displayed in a new window
         * @return  <code>true</code> if the target has to be displayed in a new window
         */
        public boolean newWindow() {
            return this.newWindow;
        }

        /**
         * Return a String representation of the object.
         * @return  a <code>String</code> of the form
         *          "org.apache.fop.area.Trait.ExternalLink[dest=someURL,newWindow=false]"
         */
        public String toString() {
            StringBuffer sb = new StringBuffer(64);
            sb.append("newWindow=").append(newWindow);
            sb.append(",dest=").append(this.destination);
            return sb.toString();
        }
    }

    /**
     * Background trait structure.
     * Used for storing back trait information which are related.
     */
    public static class Background implements Serializable {

        /** The background color if any. */
        private Color color = null;

        /** The background image url if any. */
        private String url = null;

        /** The background image if any. */
        private ImageInfo imageInfo = null;

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
        public Color getColor() {
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
         * Returns the ImageInfo object representing the background image
         * @return the background image, null if n/a
         */
        public ImageInfo getImageInfo() {
            return imageInfo;
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
        public void setColor(Color color) {
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
         * Sets the image repetition behaviour for images.
         * @param repeat The image repetition behaviour to set
         */
        public void setRepeat(String repeat) {
            setRepeat(getConstantForRepeat(repeat));
        }

        /**
         * Sets the URL to the background image.
         * @param url The URL to set
         */
        public void setURL(String url) {
            this.url = url;
        }

        /**
         * Sets the ImageInfo of the image to use as the background image.
         * @param info The background image's info object
         */
        public void setImageInfo(ImageInfo info) {
            this.imageInfo = info;
        }

        /**
         * Sets the vertical offset for images.
         * @param vertical The vertical offset to set
         */
        public void setVertical(int vertical) {
            this.vertical = vertical;
        }

        private String getRepeatString() {
            switch (getRepeat()) {
            case Constants.EN_REPEAT: return "repeat";
            case Constants.EN_REPEATX: return "repeat-x";
            case Constants.EN_REPEATY: return "repeat-y";
            case Constants.EN_NOREPEAT: return "no-repeat";
            default: throw new IllegalStateException("Illegal repeat style: " + getRepeat());
            }
        }

        private static int getConstantForRepeat(String repeat) {
            if ("repeat".equalsIgnoreCase(repeat)) {
                return Constants.EN_REPEAT;
            } else if ("repeat-x".equalsIgnoreCase(repeat)) {
                return Constants.EN_REPEATX;
            } else if ("repeat-y".equalsIgnoreCase(repeat)) {
                return Constants.EN_REPEATY;
            } else if ("no-repeat".equalsIgnoreCase(repeat)) {
                return Constants.EN_NOREPEAT;
            } else {
                throw new IllegalStateException("Illegal repeat style: " + repeat);
            }
        }

        /**
         * Return the string for debugging.
         * {@inheritDoc}
         */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (color != null) {
                sb.append("color=").append(ColorUtil.colorToString(color));
            }
            if (url != null) {
                if (color != null) {
                    sb.append(",");
                }
                sb.append("url=").append(url);
                sb.append(",repeat=").append(getRepeatString());
                sb.append(",horiz=").append(horiz);
                sb.append(",vertical=").append(vertical);
            }
            return sb.toString();
        }

    }
}

