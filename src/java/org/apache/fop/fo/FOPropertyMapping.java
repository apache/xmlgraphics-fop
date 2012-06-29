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

package org.apache.fop.fo;

import java.util.HashMap;
import java.util.Map;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.table.TableFObj.ColumnNumberPropertyMaker;
import org.apache.fop.fo.properties.BackgroundPositionShorthand;
import org.apache.fop.fo.properties.BorderSpacingShorthandParser;
import org.apache.fop.fo.properties.BorderWidthPropertyMaker;
import org.apache.fop.fo.properties.BoxCornerPropShorthandParser;
import org.apache.fop.fo.properties.BoxPropShorthandParser;
import org.apache.fop.fo.properties.CharacterProperty;
import org.apache.fop.fo.properties.ColorProperty;
import org.apache.fop.fo.properties.CondLengthProperty;
import org.apache.fop.fo.properties.CorrespondingPropertyMaker;
import org.apache.fop.fo.properties.DimensionPropertyMaker;
import org.apache.fop.fo.properties.EnumProperty;
import org.apache.fop.fo.properties.FontFamilyProperty;
import org.apache.fop.fo.properties.FontShorthandParser;
import org.apache.fop.fo.properties.FontShorthandProperty;
import org.apache.fop.fo.properties.FontSizePropertyMaker;
import org.apache.fop.fo.properties.FontStretchPropertyMaker;
import org.apache.fop.fo.properties.FontWeightPropertyMaker;
import org.apache.fop.fo.properties.GenericShorthandParser;
import org.apache.fop.fo.properties.IndentPropertyMaker;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthPairProperty;
import org.apache.fop.fo.properties.LengthProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.LineHeightPropertyMaker;
import org.apache.fop.fo.properties.ListProperty;
import org.apache.fop.fo.properties.NumberProperty;
import org.apache.fop.fo.properties.PageBreakShorthandParser;
import org.apache.fop.fo.properties.PageDimensionMaker;
import org.apache.fop.fo.properties.PositionShorthandParser;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.PropertyMaker;
import org.apache.fop.fo.properties.ReferenceOrientationMaker;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.fo.properties.SpacePropertyMaker;
import org.apache.fop.fo.properties.SpacingPropertyMaker;
import org.apache.fop.fo.properties.StringProperty;
import org.apache.fop.fo.properties.TableBorderPrecedence;
import org.apache.fop.fo.properties.TextDecorationMaker;
import org.apache.fop.fo.properties.ToBeImplementedProperty;
import org.apache.fop.fo.properties.URIProperty;
import org.apache.fop.fo.properties.VerticalAlignShorthandParser;
import org.apache.fop.fo.properties.WhiteSpaceShorthandParser;
import org.apache.fop.fo.properties.XMLLangShorthandParser;

/**
 * This class creates and returns an array of Property.Maker instances
 * indexed by the PR_* propId from Constants.java.
 *
 * TODO Check multi-threading safety of the statics below
 */
public final class FOPropertyMapping implements Constants {

    private FOPropertyMapping() {
    }

    private static Map<String, Integer> propNames = new HashMap<String, Integer>();
    private static Map<String, Integer> subPropNames = new HashMap<String, Integer>();
    private static Map<Integer, String> propIds = new HashMap<Integer, String>();

    private static PropertyMaker[] generics = null;

    // The rest is only used during the building of the generics array.
    private Property[] enums = null;

    private PropertyMaker genericColor = null;
    private PropertyMaker genericBoolean = null;
    private PropertyMaker genericKeep = null;
    private PropertyMaker genericCondLength = null;
    private PropertyMaker genericCondPadding = null;
    private PropertyMaker genericPadding = null;
    private PropertyMaker genericCondBorderWidth = null;
    private PropertyMaker genericBorderWidth = null;
    private PropertyMaker genericBorderStyle = null;
    private PropertyMaker genericCondCornerRadius = null;
    private PropertyMaker genericBreak = null;
    private PropertyMaker genericSpace = null;

    /**
     * Create the generic property maker templates. These templates
     * are used be the actual makers as a parameter to .useGeneric(...).
     */
    private void createGenerics() {

        PropertyMaker sub;

        genericColor = new ColorProperty.Maker(0);

        // GenericBoolean
        genericBoolean = new EnumProperty.Maker(0);
        genericBoolean.addEnum("true", getEnumProperty(EN_TRUE, "TRUE"));
        genericBoolean.addEnum("false", getEnumProperty(EN_FALSE, "FALSE"));

        // GenericKeep
        genericKeep = new KeepProperty.Maker(0);
        sub = new NumberProperty.Maker(CP_WITHIN_PAGE);
        sub.setByShorthand(true);
        sub.setDefault("auto");
        sub.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        sub.addEnum("always", getEnumProperty(EN_ALWAYS, "ALWAYS"));
        genericKeep.addSubpropMaker(sub);
        sub = new NumberProperty.Maker(CP_WITHIN_LINE);
        sub.setByShorthand(true);
        sub.setDefault("auto");
        sub.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        sub.addEnum("always", getEnumProperty(EN_ALWAYS, "ALWAYS"));
        genericKeep.addSubpropMaker(sub);
        sub = new NumberProperty.Maker(CP_WITHIN_COLUMN);
        sub.setByShorthand(true);
        sub.setDefault("auto");
        sub.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        sub.addEnum("always", getEnumProperty(EN_ALWAYS, "ALWAYS"));
        genericKeep.addSubpropMaker(sub);

        // GenericCondLength
        genericCondLength = new CondLengthProperty.Maker(0);
        sub = new LengthProperty.Maker(CP_LENGTH);
        sub.setByShorthand(true);
        genericCondLength.addSubpropMaker(sub);
        sub = new EnumProperty.Maker(CP_CONDITIONALITY);
        sub.addEnum("discard", getEnumProperty(EN_DISCARD, "DISCARD"));
        sub.addEnum("retain", getEnumProperty(EN_RETAIN, "RETAIN"));
        genericCondLength.addSubpropMaker(sub);

        // GenericCondPadding
        genericCondPadding = new CondLengthProperty.Maker(0);
        genericCondPadding.useGeneric(genericCondLength);
        genericCondPadding.setInherited(false);
        genericCondPadding.getSubpropMaker(CP_LENGTH).setDefault("0pt");
        genericCondPadding.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);

        // GenericPadding
        genericPadding = new LengthProperty.Maker(0);
        genericPadding.setInherited(false);
        genericPadding.setDefault("0pt");
        genericPadding.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        genericPadding.addShorthand(generics[PR_PADDING]);

        // GenericCondBorderWidth
        genericCondBorderWidth = new CondLengthProperty.Maker(0);
        genericCondBorderWidth.setInherited(false);
        genericCondBorderWidth.addKeyword("thin", "0.5pt");
        genericCondBorderWidth.addKeyword("medium", "1pt");
        genericCondBorderWidth.addKeyword("thick", "2pt");
        sub = new LengthProperty.Maker(CP_LENGTH);
        sub.setByShorthand(true);
        sub.addKeyword("thin", "0.5pt");
        sub.addKeyword("medium", "1pt");
        sub.addKeyword("thick", "2pt");
        sub.setDefault("medium");
        genericCondBorderWidth.addSubpropMaker(sub);
        sub = new EnumProperty.Maker(CP_CONDITIONALITY);
        sub.addEnum("discard", getEnumProperty(EN_DISCARD, "DISCARD"));
        sub.addEnum("retain", getEnumProperty(EN_RETAIN, "RETAIN"));
        genericCondBorderWidth.addSubpropMaker(sub);

        // GenericBorderWidth
        genericBorderWidth = new LengthProperty.Maker(0);
        genericBorderWidth.setInherited(false);
        genericBorderWidth.addKeyword("thin", "0.5pt");
        genericBorderWidth.addKeyword("medium", "1pt");
        genericBorderWidth.addKeyword("thick", "2pt");
        genericBorderWidth.setDefault("medium");

        // GenericBorderStyle
        genericBorderStyle = new EnumProperty.Maker(0);
        genericBorderStyle.setInherited(false);
        genericBorderStyle.addEnum("none", getEnumProperty(EN_NONE, "NONE"));
        genericBorderStyle.addEnum("hidden", getEnumProperty(EN_HIDDEN, "HIDDEN"));
        genericBorderStyle.addEnum("dotted", getEnumProperty(EN_DOTTED, "DOTTED"));
        genericBorderStyle.addEnum("dashed", getEnumProperty(EN_DASHED, "DASHED"));
        genericBorderStyle.addEnum("solid", getEnumProperty(EN_SOLID, "SOLID"));
        genericBorderStyle.addEnum("double", getEnumProperty(EN_DOUBLE, "DOUBLE"));
        genericBorderStyle.addEnum("groove", getEnumProperty(EN_GROOVE, "GROOVE"));
        genericBorderStyle.addEnum("ridge", getEnumProperty(EN_RIDGE, "RIDGE"));
        genericBorderStyle.addEnum("inset", getEnumProperty(EN_INSET, "INSET"));
        genericBorderStyle.addEnum("outset", getEnumProperty(EN_OUTSET, "OUTSET"));
        genericBorderStyle.setDefault("none");

        // GenericCondCornerRadius
        genericCondCornerRadius =  new CondLengthProperty.Maker(0);
        genericCondCornerRadius.useGeneric(genericCondLength);
        genericCondCornerRadius.setInherited(false);
        genericCondCornerRadius.getSubpropMaker(CP_LENGTH).setDefault("0pt");
        genericCondCornerRadius.setPercentBase(LengthBase.CONTAINING_BLOCK_HEIGHT);
        genericCondCornerRadius.addShorthand(generics[PR_X_BORDER_RADIUS]);


        // GenericBreak
        genericBreak = new EnumProperty.Maker(0);
        genericBreak.setInherited(false);
        genericBreak.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        genericBreak.addEnum("column", getEnumProperty(EN_COLUMN, "COLUMN"));
        genericBreak.addEnum("page", getEnumProperty(EN_PAGE, "PAGE"));
        genericBreak.addEnum("even-page", getEnumProperty(EN_EVEN_PAGE, "EVEN_PAGE"));
        genericBreak.addEnum("odd-page", getEnumProperty(EN_ODD_PAGE, "ODD_PAGE"));
        genericBreak.setDefault("auto");

        // GenericSpace
        genericSpace = new SpaceProperty.Maker(0);
        genericSpace.setInherited(false);
        sub = new LengthProperty.Maker(CP_MINIMUM);
        sub.setDefault("0pt");
        sub.setByShorthand(true);
        genericSpace.addSubpropMaker(sub);
        sub = new LengthProperty.Maker(CP_OPTIMUM);
        sub.setDefault("0pt");
        sub.setByShorthand(true);
        genericSpace.addSubpropMaker(sub);
        sub = new LengthProperty.Maker(CP_MAXIMUM);
        sub.setDefault("0pt");
        sub.setByShorthand(true);
        genericSpace.addSubpropMaker(sub);
        sub = new NumberProperty.Maker(CP_PRECEDENCE);
        sub.addEnum("force", getEnumProperty(EN_FORCE, "FORCE"));
        sub.setDefault("0");
        genericSpace.addSubpropMaker(sub);
        sub = new EnumProperty.Maker(CP_CONDITIONALITY);
        sub.addEnum("discard", getEnumProperty(EN_DISCARD, "DISCARD"));
        sub.addEnum("retain", getEnumProperty(EN_RETAIN, "RETAIN"));
        sub.setDefault("discard");
        genericSpace.addSubpropMaker(sub);
    }

    /**
     * Add a property maker to the generics array.
     * Also creates the name <-> id mapping in propNames and propIds.
     *
     * @param name  the name of the property maker.
     * @param maker the maker.
     */
    private static void addPropertyMaker(String name, PropertyMaker maker) {
        generics[maker.getPropId()] = maker;
        propNames.put(name, maker.getPropId());
        propIds.put(maker.getPropId(), name);
    }

    /**
     * Create the name<->id mapping for the subproperty names.
     * @param name name of the subproperty.
     * @param id   Id for the subproperty from CP_* in Constants.java.
     */
    private static void addSubpropMakerName(String name, int id) {
        subPropNames.put(name, id);
        propIds.put(id, name);
    }

    /**
     * Return a (possibly cached) enum property based in the enum value.
     * @param enumValue A enum value from Constants.java.
     * @param text the text value by which this enum property is known
     * @return An EnumProperty instance.
     */
    private Property getEnumProperty(int enumValue, String text) {
        if (enums == null) {
            enums = new Property[ENUM_COUNT + 1];
        }
        if (enums[enumValue] == null) {
            enums[enumValue] = EnumProperty.getInstance(enumValue, text);
        }
        return enums[enumValue];
    }

    /**
     * Return the array of Makers.
     * @return the maker array.
     */
    public static PropertyMaker[] getGenericMappings() {

        if (generics == null) {
            /* this method was never called before */
            generics = new PropertyMaker[PROPERTY_COUNT + 1];
            FOPropertyMapping gp = new FOPropertyMapping();

            /* Create the shorthand first. They are
             * referenced by the real properties.
             */
            gp.createShorthandProperties();
            gp.createGenerics();
            gp.createAccessibilityProperties();
            gp.createAbsolutePositionProperties();
            gp.createAuralProperties();
            gp.createBorderPaddingBackgroundProperties();
            gp.createFontProperties();
            gp.createHyphenationProperties();
            gp.createMarginBlockProperties();
            gp.createMarginInlineProperties();
            gp.createRelativePosProperties();
            gp.createAreaAlignmentProperties();
            gp.createAreaDimensionProperties();
            gp.createBlockAndLineProperties();
            gp.createCharacterProperties();
            gp.createColorProperties();
            gp.createFloatProperties();
            gp.createKeepsAndBreaksProperties();
            gp.createLayoutProperties();
            gp.createLeaderAndRuleProperties();
            gp.createDynamicProperties();
            gp.createMarkersProperties();
            gp.createNumberToStringProperties();
            gp.createPaginationAndLayoutProperties();
            gp.createTableProperties();
            gp.createWritingModeProperties();
            gp.createMiscProperties();

            // Hardcode the subproperties.
            addSubpropMakerName("length", CP_LENGTH);
            addSubpropMakerName("conditionality", CP_CONDITIONALITY);
            addSubpropMakerName("block-progression-direction", CP_BLOCK_PROGRESSION_DIRECTION);
            addSubpropMakerName("inline-progression-direction", CP_INLINE_PROGRESSION_DIRECTION);
            addSubpropMakerName("within-line", CP_WITHIN_LINE);
            addSubpropMakerName("within-column", CP_WITHIN_COLUMN);
            addSubpropMakerName("within-page", CP_WITHIN_PAGE);
            addSubpropMakerName("minimum", CP_MINIMUM);
            addSubpropMakerName("maximum", CP_MAXIMUM);
            addSubpropMakerName("optimum", CP_OPTIMUM);
            addSubpropMakerName("precedence", CP_PRECEDENCE);
        }

        return generics;
    }

    /**
     * Return the propId for a property name.
     * @param name the property name
     * @return a propId that matches the property name.
     */
    public static int getPropertyId(String name) {
        if (name != null) {
            Integer i = propNames.get(name);
            if (i != null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return the subpropId for a subproperty name.
     * @param name the subproperty name.
     * @return a subpropId that matches the subproperty name.
     */
    public static int getSubPropertyId(String name) {
        if (name != null) {
            Integer i = subPropNames.get(name);
            if (i != null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the property name corresponding to the PR_* id
     * @param id   the property id in Constants
     * @return  the property name
     */
    public static String getPropertyName(int id) {
        if (((id & Constants.COMPOUND_MASK) == 0)
                || ((id & Constants.PROPERTY_MASK) == 0)) {
            return propIds.get(id);
        } else {
            return propIds.get(id & Constants.PROPERTY_MASK)
                    + "." + propIds.get(id & Constants.COMPOUND_MASK);
        }
    }

    private void createAccessibilityProperties() {
        PropertyMaker m;

        // source-document
        m  = new StringProperty.Maker(PR_SOURCE_DOCUMENT);
        m.setInherited(false);
        m.setDefault("none");
        addPropertyMaker("source-document", m);

        // role
        m  = new StringProperty.Maker(PR_ROLE);
        m.setInherited(false);
        m.setDefault("none");
        addPropertyMaker("role", m);
    }

    private void createAbsolutePositionProperties() {
        PropertyMaker m;
        LengthProperty.Maker l;

        // absolute-position
        m  = new EnumProperty.Maker(PR_ABSOLUTE_POSITION);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("fixed", getEnumProperty(EN_FIXED, "FIXED"));
        m.addEnum("absolute", getEnumProperty(EN_ABSOLUTE, "ABSOLUTE"));
        m.setDefault("auto");
        m.addShorthand(generics[PR_POSITION]);
        addPropertyMaker("absolute-position", m);

        // top
        l  = new LengthProperty.Maker(PR_TOP);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setDefault("auto");
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_HEIGHT);
        addPropertyMaker("top", l);

        // right
        l  = new LengthProperty.Maker(PR_RIGHT);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setDefault("auto");
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("right", l);

        // bottom
        l  = new LengthProperty.Maker(PR_BOTTOM);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setDefault("auto");
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_HEIGHT);
        addPropertyMaker("bottom", l);

        // left
        l  = new LengthProperty.Maker(PR_LEFT);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setDefault("auto");
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("left", l);
    }

    private void createAuralProperties() {
        PropertyMaker m;

        // azimuth
        m  = new ToBeImplementedProperty.Maker(PR_AZIMUTH);
        m.setInherited(true);
        m.setDefault("center");
        addPropertyMaker("azimuth", m);

        // cue-after
        m  = new ToBeImplementedProperty.Maker(PR_CUE_AFTER);
        m.setInherited(false);
        m.setDefault("none");
        addPropertyMaker("cue-after", m);

        // cue-before
        m  = new ToBeImplementedProperty.Maker(PR_CUE_BEFORE);
        m.setInherited(false);
        m.setDefault("none");
        addPropertyMaker("cue-before", m);

        // elevation
        m  = new ToBeImplementedProperty.Maker(PR_ELEVATION);
        m.setInherited(true);
        m.setDefault("level");
        addPropertyMaker("elevation", m);

        // pause-after
        m  = new ToBeImplementedProperty.Maker(PR_PAUSE_AFTER);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("pause-after", m);

        // pause-before
        m  = new ToBeImplementedProperty.Maker(PR_PAUSE_BEFORE);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("pause-before", m);

        // pitch
        m  = new ToBeImplementedProperty.Maker(PR_PITCH);
        m.setInherited(true);
        m.setDefault("medium");
        addPropertyMaker("pitch", m);

        // pitch-range
        m  = new ToBeImplementedProperty.Maker(PR_PITCH_RANGE);
        m.setInherited(true);
        m.setDefault("50");
        addPropertyMaker("pitch-range", m);

        // play-during
        m  = new ToBeImplementedProperty.Maker(PR_PLAY_DURING);
        m.setInherited(false);
        m.setDefault("auto");
        addPropertyMaker("play-during", m);

        // richness
        m  = new ToBeImplementedProperty.Maker(PR_RICHNESS);
        m.setInherited(true);
        m.setDefault("50");
        addPropertyMaker("richness", m);

        // speak
        m  = new ToBeImplementedProperty.Maker(PR_SPEAK);
        m.setInherited(true);
        m.setDefault("normal");
        addPropertyMaker("speak", m);

        // speak-header
        m  = new ToBeImplementedProperty.Maker(PR_SPEAK_HEADER);
        m.setInherited(true);
        m.setDefault("once");
        addPropertyMaker("speak-header", m);

        // speak-numeral
        m  = new ToBeImplementedProperty.Maker(PR_SPEAK_NUMERAL);
        m.setInherited(true);
        m.setDefault("continuous");
        addPropertyMaker("speak-numeral", m);

        // speak-punctuation
        m  = new ToBeImplementedProperty.Maker(PR_SPEAK_PUNCTUATION);
        m.setInherited(true);
        m.setDefault("none");
        addPropertyMaker("speak-punctuation", m);

        // speech-rate
        m  = new ToBeImplementedProperty.Maker(PR_SPEECH_RATE);
        m.setInherited(true);
        m.setDefault("medium");
        addPropertyMaker("speech-rate", m);

        // stress
        m  = new ToBeImplementedProperty.Maker(PR_STRESS);
        m.setInherited(true);
        m.setDefault("50");
        addPropertyMaker("stress", m);

        // voice-family
        m  = new ToBeImplementedProperty.Maker(PR_VOICE_FAMILY);
        m.setInherited(true);
        m.setDefault("");
        addPropertyMaker("voice-family", m);

        // volume
        m  = new ToBeImplementedProperty.Maker(PR_VOLUME);
        m.setInherited(true);
        m.setDefault("medium");
        addPropertyMaker("volume", m);
    }

    private void createBorderPaddingBackgroundProperties() {    // CSOK: MethodLength
        PropertyMaker m;
        BorderWidthPropertyMaker bwm;
        CorrespondingPropertyMaker corr;

        // background-attachment
        m  = new EnumProperty.Maker(PR_BACKGROUND_ATTACHMENT);
        m.setInherited(false);
        m.addEnum("scroll", getEnumProperty(EN_SCROLL, "SCROLL"));
        m.addEnum("fixed", getEnumProperty(EN_FIXED, "FIXED"));
        m.setDefault("scroll");
        addPropertyMaker("background-attachment", m);

        // background-color
        m  = new ColorProperty.Maker(PR_BACKGROUND_COLOR) {
            protected Property convertPropertyDatatype(
                    Property p, PropertyList propertyList, FObj fo) throws PropertyException {
                String nameval = p.getNCname();
                if (nameval != null) {
                    FObj fobj = (fo == null ? propertyList.getFObj() : fo);
                    FOUserAgent ua = (fobj == null ? null : fobj.getUserAgent());
                    return ColorProperty.getInstance(ua, nameval);
                }
                return super.convertPropertyDatatype(p, propertyList, fo);
            }
        };
        m.useGeneric(genericColor);
        m.setInherited(false);
        m.setDefault("transparent");
        addPropertyMaker("background-color", m);

        // background-image
        m  = new StringProperty.Maker(PR_BACKGROUND_IMAGE);
        m.setInherited(false);
        m.setDefault("none");
        addPropertyMaker("background-image", m);

        // background-repeat
        m  = new EnumProperty.Maker(PR_BACKGROUND_REPEAT);
        m.setInherited(false);
        m.addEnum("repeat", getEnumProperty(EN_REPEAT, "REPEAT"));
        m.addEnum("repeat-x", getEnumProperty(EN_REPEATX, "REPEATX"));
        m.addEnum("repeat-y", getEnumProperty(EN_REPEATY, "REPEATY"));
        m.addEnum("no-repeat", getEnumProperty(EN_NOREPEAT, "NOREPEAT"));
        m.setDefault("repeat");
        addPropertyMaker("background-repeat", m);

        // background-position-horizontal
        m  = new LengthProperty.Maker(PR_BACKGROUND_POSITION_HORIZONTAL);
        m.setInherited(false);
        m.setDefault("0pt");
        m.addKeyword("left", "0pt");
        m.addKeyword("center", "50%");
        m.addKeyword("right", "100%");
        m.setPercentBase(LengthBase.IMAGE_BACKGROUND_POSITION_HORIZONTAL);
        m.addShorthand(generics[PR_BACKGROUND_POSITION]);
        addPropertyMaker("background-position-horizontal", m);

        // background-position-vertical
        m  = new LengthProperty.Maker(PR_BACKGROUND_POSITION_VERTICAL);
        m.setInherited(false);
        m.setDefault("0pt");
        m.addKeyword("top", "0pt");
        m.addKeyword("center", "50%");
        m.addKeyword("bottom", "100%");
        m.setPercentBase(LengthBase.IMAGE_BACKGROUND_POSITION_VERTICAL);
        m.addShorthand(generics[PR_BACKGROUND_POSITION]);
        addPropertyMaker("background-position-vertical", m);

        // border-before-color
        m  = new ColorProperty.Maker(PR_BORDER_BEFORE_COLOR);
        m.useGeneric(genericColor);
        m.setInherited(false);
        m.setDefault("black");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_TOP_COLOR, PR_BORDER_TOP_COLOR,
                              PR_BORDER_RIGHT_COLOR, PR_BORDER_LEFT_COLOR);
        corr.setRelative(true);
        addPropertyMaker("border-before-color", m);

        // border-before-style
        m  = new EnumProperty.Maker(PR_BORDER_BEFORE_STYLE);
        m.useGeneric(genericBorderStyle);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_TOP_STYLE, PR_BORDER_TOP_STYLE,
                              PR_BORDER_RIGHT_STYLE, PR_BORDER_LEFT_STYLE);
        corr.setRelative(true);
        addPropertyMaker("border-before-style", m);

        // border-before-width
        m  = new CondLengthProperty.Maker(PR_BORDER_BEFORE_WIDTH);
        m.useGeneric(genericCondBorderWidth);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_TOP_WIDTH, PR_BORDER_TOP_WIDTH,
                              PR_BORDER_RIGHT_WIDTH, PR_BORDER_LEFT_WIDTH);
        corr.setRelative(true);
        addPropertyMaker("border-before-width", m);

        // border-after-color
        m  = new ColorProperty.Maker(PR_BORDER_AFTER_COLOR);
        m.useGeneric(genericColor);
        m.setInherited(false);
        m.setDefault("black");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_BOTTOM_COLOR, PR_BORDER_BOTTOM_COLOR,
                              PR_BORDER_LEFT_COLOR, PR_BORDER_RIGHT_COLOR);
        corr.setRelative(true);
        addPropertyMaker("border-after-color", m);

        // border-after-style
        m  = new EnumProperty.Maker(PR_BORDER_AFTER_STYLE);
        m.useGeneric(genericBorderStyle);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_BOTTOM_STYLE, PR_BORDER_BOTTOM_STYLE,
                              PR_BORDER_LEFT_STYLE, PR_BORDER_RIGHT_STYLE);
        corr.setRelative(true);
        addPropertyMaker("border-after-style", m);

        // border-after-width
        m  = new CondLengthProperty.Maker(PR_BORDER_AFTER_WIDTH);
        m.useGeneric(genericCondBorderWidth);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_BOTTOM_WIDTH, PR_BORDER_BOTTOM_WIDTH,
                              PR_BORDER_LEFT_WIDTH, PR_BORDER_LEFT_WIDTH);
        corr.setRelative(true);
        addPropertyMaker("border-after-width", m);

        // border-start-color
        m  = new ColorProperty.Maker(PR_BORDER_START_COLOR);
        m.useGeneric(genericColor);
        m.setInherited(false);
        m.setDefault("black");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_LEFT_COLOR, PR_BORDER_RIGHT_COLOR,
                              PR_BORDER_TOP_COLOR, PR_BORDER_TOP_COLOR);
        corr.setRelative(true);
        addPropertyMaker("border-start-color", m);

        // border-start-style
        m  = new EnumProperty.Maker(PR_BORDER_START_STYLE);
        m.useGeneric(genericBorderStyle);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_LEFT_STYLE, PR_BORDER_RIGHT_STYLE,
                              PR_BORDER_TOP_STYLE, PR_BORDER_TOP_STYLE);
        corr.setRelative(true);
        addPropertyMaker("border-start-style", m);

        // border-start-width
        m  = new CondLengthProperty.Maker(PR_BORDER_START_WIDTH);
        m.useGeneric(genericCondBorderWidth);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_LEFT_WIDTH, PR_BORDER_RIGHT_WIDTH,
                              PR_BORDER_TOP_WIDTH, PR_BORDER_TOP_WIDTH);
        corr.setRelative(true);
        addPropertyMaker("border-start-width", m);

        // border-end-color
        m  = new ColorProperty.Maker(PR_BORDER_END_COLOR);
        m.useGeneric(genericColor);
        m.setInherited(false);
        m.setDefault("black");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_RIGHT_COLOR, PR_BORDER_LEFT_COLOR,
                              PR_BORDER_BOTTOM_COLOR, PR_BORDER_BOTTOM_COLOR);
        corr.setRelative(true);
        addPropertyMaker("border-end-color", m);

        // border-end-style
        m  = new EnumProperty.Maker(PR_BORDER_END_STYLE);
        m.useGeneric(genericBorderStyle);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_RIGHT_STYLE, PR_BORDER_LEFT_STYLE,
                              PR_BORDER_BOTTOM_STYLE, PR_BORDER_BOTTOM_STYLE);
        corr.setRelative(true);
        addPropertyMaker("border-end-style", m);

        // border-end-width
        m  = new CondLengthProperty.Maker(PR_BORDER_END_WIDTH);
        m.useGeneric(genericCondBorderWidth);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_RIGHT_WIDTH, PR_BORDER_LEFT_WIDTH,
                              PR_BORDER_BOTTOM_WIDTH, PR_BORDER_BOTTOM_WIDTH);
        corr.setRelative(true);
        addPropertyMaker("border-end-width", m);

        // border-top-color
        m  = new ColorProperty.Maker(PR_BORDER_TOP_COLOR);
        m.useGeneric(genericColor);
        m.setInherited(false);
        m.setDefault("black");
        m.addShorthand(generics[PR_BORDER_TOP]);
        m.addShorthand(generics[PR_BORDER_COLOR]);
        m.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_BEFORE_COLOR, PR_BORDER_BEFORE_COLOR,
                              PR_BORDER_START_COLOR, PR_BORDER_START_COLOR);
        addPropertyMaker("border-top-color", m);

        // border-top-style
        m  = new EnumProperty.Maker(PR_BORDER_TOP_STYLE);
        m.useGeneric(genericBorderStyle);
        m.addShorthand(generics[PR_BORDER_TOP]);
        m.addShorthand(generics[PR_BORDER_STYLE]);
        m.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_BEFORE_STYLE, PR_BORDER_BEFORE_STYLE,
                              PR_BORDER_START_STYLE, PR_BORDER_START_STYLE);
        addPropertyMaker("border-top-style", m);

        // border-top-width
        bwm  = new BorderWidthPropertyMaker(PR_BORDER_TOP_WIDTH);
        bwm.useGeneric(genericBorderWidth);
        bwm.setBorderStyleId(PR_BORDER_TOP_STYLE);
        bwm.addShorthand(generics[PR_BORDER_TOP]);
        bwm.addShorthand(generics[PR_BORDER_WIDTH]);
        bwm.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(bwm);
        corr.setCorresponding(PR_BORDER_BEFORE_WIDTH, PR_BORDER_BEFORE_WIDTH,
                              PR_BORDER_START_WIDTH, PR_BORDER_START_WIDTH);
        addPropertyMaker("border-top-width", bwm);

        // border-bottom-color
        m  = new ColorProperty.Maker(PR_BORDER_BOTTOM_COLOR);
        m.useGeneric(genericColor);
        m.setInherited(false);
        m.setDefault("black");
        m.addShorthand(generics[PR_BORDER_BOTTOM]);
        m.addShorthand(generics[PR_BORDER_COLOR]);
        m.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_AFTER_COLOR, PR_BORDER_AFTER_COLOR,
                              PR_BORDER_END_COLOR, PR_BORDER_END_COLOR);
        addPropertyMaker("border-bottom-color", m);

        // border-bottom-style
        m  = new EnumProperty.Maker(PR_BORDER_BOTTOM_STYLE);
        m.useGeneric(genericBorderStyle);
        m.addShorthand(generics[PR_BORDER_BOTTOM]);
        m.addShorthand(generics[PR_BORDER_STYLE]);
        m.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_AFTER_STYLE, PR_BORDER_AFTER_STYLE,
                              PR_BORDER_END_STYLE, PR_BORDER_END_STYLE);
        addPropertyMaker("border-bottom-style", m);

        // border-bottom-width
        bwm  = new BorderWidthPropertyMaker(PR_BORDER_BOTTOM_WIDTH);
        bwm.useGeneric(genericBorderWidth);
        bwm.setBorderStyleId(PR_BORDER_BOTTOM_STYLE);
        bwm.addShorthand(generics[PR_BORDER_BOTTOM]);
        bwm.addShorthand(generics[PR_BORDER_WIDTH]);
        bwm.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(bwm);
        corr.setCorresponding(PR_BORDER_AFTER_WIDTH, PR_BORDER_AFTER_WIDTH,
                              PR_BORDER_END_WIDTH, PR_BORDER_END_WIDTH);
        addPropertyMaker("border-bottom-width", bwm);

        // border-left-color
        m  = new ColorProperty.Maker(PR_BORDER_LEFT_COLOR);
        m.useGeneric(genericColor);
        m.setInherited(false);
        m.setDefault("black");
        m.addShorthand(generics[PR_BORDER_LEFT]);
        m.addShorthand(generics[PR_BORDER_COLOR]);
        m.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_START_COLOR, PR_BORDER_END_COLOR,
                              PR_BORDER_AFTER_COLOR, PR_BORDER_BEFORE_COLOR);
        addPropertyMaker("border-left-color", m);

        // border-left-style
        m  = new EnumProperty.Maker(PR_BORDER_LEFT_STYLE);
        m.useGeneric(genericBorderStyle);
        m.addShorthand(generics[PR_BORDER_LEFT]);
        m.addShorthand(generics[PR_BORDER_STYLE]);
        m.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_START_STYLE, PR_BORDER_END_STYLE,
                              PR_BORDER_AFTER_STYLE, PR_BORDER_BEFORE_STYLE);
        addPropertyMaker("border-left-style", m);

        // border-left-width
        bwm  = new BorderWidthPropertyMaker(PR_BORDER_LEFT_WIDTH);
        bwm.useGeneric(genericBorderWidth);
        bwm.setBorderStyleId(PR_BORDER_LEFT_STYLE);
        bwm.addShorthand(generics[PR_BORDER_LEFT]);
        bwm.addShorthand(generics[PR_BORDER_WIDTH]);
        bwm.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(bwm);
        corr.setCorresponding(PR_BORDER_START_WIDTH, PR_BORDER_END_WIDTH,
                              PR_BORDER_AFTER_WIDTH, PR_BORDER_BEFORE_WIDTH);
        addPropertyMaker("border-left-width", bwm);

        // border-right-color
        m  = new ColorProperty.Maker(PR_BORDER_RIGHT_COLOR);
        m.useGeneric(genericColor);
        m.setInherited(false);
        m.setDefault("black");
        m.addShorthand(generics[PR_BORDER_RIGHT]);
        m.addShorthand(generics[PR_BORDER_COLOR]);
        m.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_END_COLOR, PR_BORDER_START_COLOR,
                              PR_BORDER_BEFORE_COLOR, PR_BORDER_AFTER_COLOR);
        addPropertyMaker("border-right-color", m);

        // border-right-style
        m  = new EnumProperty.Maker(PR_BORDER_RIGHT_STYLE);
        m.useGeneric(genericBorderStyle);
        m.addShorthand(generics[PR_BORDER_RIGHT]);
        m.addShorthand(generics[PR_BORDER_STYLE]);
        m.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_BORDER_END_STYLE, PR_BORDER_START_STYLE,
                              PR_BORDER_BEFORE_STYLE, PR_BORDER_AFTER_STYLE);
        addPropertyMaker("border-right-style", m);

        // border-right-width
        bwm  = new BorderWidthPropertyMaker(PR_BORDER_RIGHT_WIDTH);
        bwm.useGeneric(genericBorderWidth);
        bwm.setBorderStyleId(PR_BORDER_RIGHT_STYLE);
        bwm.addShorthand(generics[PR_BORDER_RIGHT]);
        bwm.addShorthand(generics[PR_BORDER_WIDTH]);
        bwm.addShorthand(generics[PR_BORDER]);
        corr = new CorrespondingPropertyMaker(bwm);
        corr.setCorresponding(PR_BORDER_END_WIDTH, PR_BORDER_START_WIDTH,
                              PR_BORDER_BEFORE_WIDTH, PR_BORDER_AFTER_WIDTH);
        addPropertyMaker("border-right-width", bwm);

        // padding-before
        m  = new CondLengthProperty.Maker(PR_PADDING_BEFORE);
        m.useGeneric(genericCondPadding);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_PADDING_TOP, PR_PADDING_TOP,
                              PR_PADDING_RIGHT, PR_PADDING_LEFT);
        corr.setRelative(true);
        addPropertyMaker("padding-before", m);

        // padding-after
        m  = new CondLengthProperty.Maker(PR_PADDING_AFTER);
        m.useGeneric(genericCondPadding);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_PADDING_BOTTOM, PR_PADDING_BOTTOM,
                              PR_PADDING_LEFT, PR_PADDING_RIGHT);
        corr.setRelative(true);
        addPropertyMaker("padding-after", m);

        // padding-start
        m  = new CondLengthProperty.Maker(PR_PADDING_START);
        m.useGeneric(genericCondPadding);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_PADDING_LEFT, PR_PADDING_RIGHT,
                              PR_PADDING_TOP, PR_PADDING_TOP);
        corr.setRelative(true);
        addPropertyMaker("padding-start", m);

        // padding-end
        m  = new CondLengthProperty.Maker(PR_PADDING_END);
        m.useGeneric(genericCondPadding);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_PADDING_RIGHT, PR_PADDING_LEFT,
                              PR_PADDING_BOTTOM, PR_PADDING_BOTTOM);
        corr.setRelative(true);
        addPropertyMaker("padding-end", m);

        // padding-top
        m  = new LengthProperty.Maker(PR_PADDING_TOP);
        m.useGeneric(genericPadding);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_PADDING_BEFORE, PR_PADDING_BEFORE,
                              PR_PADDING_START, PR_PADDING_START);
        addPropertyMaker("padding-top", m);

        // padding-bottom
        m  = new LengthProperty.Maker(PR_PADDING_BOTTOM);
        m.useGeneric(genericPadding);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_PADDING_AFTER, PR_PADDING_AFTER,
                              PR_PADDING_END, PR_PADDING_END);
        addPropertyMaker("padding-bottom", m);

        // padding-left
        m  = new LengthProperty.Maker(PR_PADDING_LEFT);
        m.useGeneric(genericPadding);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_PADDING_START, PR_PADDING_END,
                              PR_PADDING_AFTER, PR_PADDING_BEFORE);
        addPropertyMaker("padding-left", m);

        // padding-right
        m  = new LengthProperty.Maker(PR_PADDING_RIGHT);
        m.useGeneric(genericPadding);
        corr = new CorrespondingPropertyMaker(m);
        corr.setCorresponding(PR_PADDING_END, PR_PADDING_START,
                              PR_PADDING_BEFORE, PR_PADDING_AFTER);
        addPropertyMaker("padding-right", m);
    }

    private void createFontProperties() {
        PropertyMaker m;

        // font-family
        m  = new FontFamilyProperty.Maker(PR_FONT_FAMILY);
        m.setInherited(true);
        m.setDefault("sans-serif,Symbol,ZapfDingbats");
        m.addShorthand(generics[PR_FONT]);
        addPropertyMaker("font-family", m);

        // font-selection-strategy
        m  = new EnumProperty.Maker(PR_FONT_SELECTION_STRATEGY);
        m.setInherited(true);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("character-by-character", getEnumProperty(
                EN_CHARACTER_BY_CHARACTER, "CHARACTER_BY_CHARACTER"));
        m.setDefault("auto");
        addPropertyMaker("font-selection-strategy", m);

        // font-size
        m  = new FontSizePropertyMaker(PR_FONT_SIZE);
        m.setInherited(true);
        m.setDefault("12pt");
        m.addKeyword("xx-small", "6.944pt");
        m.addKeyword("x-small", "8.333pt");
        m.addKeyword("small", "10pt");
        m.addKeyword("medium", "12pt");
        m.addKeyword("large", "14.4pt");
        m.addKeyword("x-large", "17.28pt");
        m.addKeyword("xx-large", "20.736pt");
        m.addEnum("larger", getEnumProperty(EN_LARGER, "LARGER"));
        m.addEnum("smaller", getEnumProperty(EN_SMALLER, "SMALLER"));
        m.setPercentBase(LengthBase.INH_FONTSIZE);
        m.addShorthand(generics[PR_FONT]);
        addPropertyMaker("font-size", m);

        // font-stretch
        m  = new FontStretchPropertyMaker(PR_FONT_STRETCH);
        m.addEnum("normal", getEnumProperty(EN_NORMAL, "NORMAL"));
        m.addEnum("wider", getEnumProperty(EN_WIDER, "WIDER"));
        m.addEnum("narrower", getEnumProperty(EN_NARROWER, "NARROWER"));
        m.addEnum("ultra-condensed", getEnumProperty(EN_ULTRA_CONDENSED, "ULTRA_CONDENSED"));
        m.addEnum("extra-condensed", getEnumProperty(EN_EXTRA_CONDENSED, "EXTRA_CONDENSED"));
        m.addEnum("condensed", getEnumProperty(EN_CONDENSED, "CONDENSED"));
        m.addEnum("semi-condensed", getEnumProperty(EN_SEMI_CONDENSED, "SEMI_CONDENSED"));
        m.addEnum("semi-expanded", getEnumProperty(EN_SEMI_EXPANDED, "SEMI_EXPANDED"));
        m.addEnum("expanded", getEnumProperty(EN_EXPANDED, "EXPANDED"));
        m.addEnum("extra-expanded", getEnumProperty(EN_EXTRA_EXPANDED, "EXTRA_EXPANDED"));
        m.addEnum("ultra-expanded", getEnumProperty(EN_ULTRA_EXPANDED, "ULTRA_EXPANDED"));
        m.setDefault("normal");
        addPropertyMaker("font-stretch", m);

        // font-size-adjust
        m  = new NumberProperty.Maker(PR_FONT_SIZE_ADJUST);
        m.setInherited(true);
        m.addEnum("none", getEnumProperty(EN_NONE, "NONE"));
        m.setDefault("none");
        addPropertyMaker("font-size-adjust", m);

        // font-style
        m  = new EnumProperty.Maker(PR_FONT_STYLE);
        m.setInherited(true);
        m.addEnum("normal", getEnumProperty(EN_NORMAL, "NORMAL"));
        m.addEnum("italic", getEnumProperty(EN_ITALIC, "ITALIC"));
        m.addEnum("oblique", getEnumProperty(EN_OBLIQUE, "OBLIQUE"));
        m.addEnum("backslant", getEnumProperty(EN_BACKSLANT, "BACKSLANT"));
        m.setDefault("normal");
        m.addShorthand(generics[PR_FONT]);
        addPropertyMaker("font-style", m);

        // font-variant
        m  = new EnumProperty.Maker(PR_FONT_VARIANT);
        m.setInherited(true);
        m.addEnum("normal", getEnumProperty(EN_NORMAL, "NORMAL"));
        m.addEnum("small-caps", getEnumProperty(EN_SMALL_CAPS, "SMALL_CAPS"));
        m.setDefault("normal");
        m.addShorthand(generics[PR_FONT]);
        addPropertyMaker("font-variant", m);

        // font-weight
        m  = new FontWeightPropertyMaker(PR_FONT_WEIGHT);
        m.setInherited(true);
        m.addKeyword("normal", "400");
        m.addKeyword("bold", "700");
        m.addEnum("bolder", getEnumProperty(EN_BOLDER, "BOLDER"));
        m.addEnum("lighter", getEnumProperty(EN_LIGHTER, "LIGHTER"));
        m.addEnum("100", getEnumProperty(EN_100, "100"));
        m.addEnum("200", getEnumProperty(EN_200, "200"));
        m.addEnum("300", getEnumProperty(EN_300, "300"));
        m.addEnum("400", getEnumProperty(EN_400, "400"));
        m.addEnum("500", getEnumProperty(EN_500, "500"));
        m.addEnum("600", getEnumProperty(EN_600, "600"));
        m.addEnum("700", getEnumProperty(EN_700, "700"));
        m.addEnum("800", getEnumProperty(EN_800, "800"));
        m.addEnum("900", getEnumProperty(EN_900, "900"));
        m.setDefault("400");
        m.addShorthand(generics[PR_FONT]);
        addPropertyMaker("font-weight", m);
    }

    private void createHyphenationProperties() {
        PropertyMaker m;

        // country
        m  = new StringProperty.Maker(PR_COUNTRY);
        m.setInherited(true);
        m.setDefault("none");
        m.addShorthand(generics[PR_XML_LANG]);
        addPropertyMaker("country", m);

        // language
        m  = new StringProperty.Maker(PR_LANGUAGE);
        m.setInherited(true);
        m.setDefault("none");
        m.addShorthand(generics[PR_XML_LANG]);
        addPropertyMaker("language", m);

        // script
        m  = new StringProperty.Maker(PR_SCRIPT);
        m.setInherited(true);
        m.setDefault("auto");
        addPropertyMaker("script", m);

        // hyphenate
        m  = new EnumProperty.Maker(PR_HYPHENATE);
        m.useGeneric(genericBoolean);
        m.setInherited(true);
        m.setDefault("false");
        addPropertyMaker("hyphenate", m);

        // hyphenation-character
        m  = new CharacterProperty.Maker(PR_HYPHENATION_CHARACTER);
        m.setInherited(true);
        m.setDefault("-");
        addPropertyMaker("hyphenation-character", m);

        // hyphenation-push-character-count
        m  = new NumberProperty.PositiveIntegerMaker(PR_HYPHENATION_PUSH_CHARACTER_COUNT);
        m.setInherited(true);
        m.setDefault("2");
        addPropertyMaker("hyphenation-push-character-count", m);

        // hyphenation-remain-character-count
        m  = new NumberProperty.PositiveIntegerMaker(PR_HYPHENATION_REMAIN_CHARACTER_COUNT);
        m.setInherited(true);
        m.setDefault("2");
        addPropertyMaker("hyphenation-remain-character-count", m);
    }

    private void createMarginBlockProperties() {
        PropertyMaker m;
        CorrespondingPropertyMaker corr;

        // margin-top
        m  = new LengthProperty.Maker(PR_MARGIN_TOP);
        m.setInherited(false);
        m.setDefault("0pt");
        m.addShorthand(generics[PR_MARGIN]);
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("margin-top", m);

        // margin-bottom
        m  = new LengthProperty.Maker(PR_MARGIN_BOTTOM);
        m.setInherited(false);
        m.setDefault("0pt");
        m.addShorthand(generics[PR_MARGIN]);
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("margin-bottom", m);

        // margin-left
        m  = new LengthProperty.Maker(PR_MARGIN_LEFT);
        m.setInherited(false);
        m.setDefault("0pt");
        m.addShorthand(generics[PR_MARGIN]);
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("margin-left", m);

        // margin-right
        m  = new LengthProperty.Maker(PR_MARGIN_RIGHT);
        m.setInherited(false);
        m.setDefault("0pt");
        m.addShorthand(generics[PR_MARGIN]);
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("margin-right", m);

        // space-before
        m  = new SpaceProperty.Maker(PR_SPACE_BEFORE);
        m.useGeneric(genericSpace);
        corr = new SpacePropertyMaker(m);
        corr.setCorresponding(PR_MARGIN_TOP, PR_MARGIN_TOP, PR_MARGIN_RIGHT, PR_MARGIN_LEFT);
        corr.setUseParent(false);
        corr.setRelative(true);
        addPropertyMaker("space-before", m);

        // space-after
        m  = new SpaceProperty.Maker(PR_SPACE_AFTER);
        m.useGeneric(genericSpace);
        corr = new SpacePropertyMaker(m);
        corr.setCorresponding(PR_MARGIN_BOTTOM, PR_MARGIN_BOTTOM, PR_MARGIN_LEFT, PR_MARGIN_RIGHT);
        corr.setUseParent(false);
        corr.setRelative(true);
        addPropertyMaker("space-after", m);

        // start-indent
        m = new LengthProperty.Maker(PR_START_INDENT);
        m.setInherited(true);
        m.setDefault("0pt");
        m.setPercentBase(LengthBase.CONTAINING_REFAREA_WIDTH);
        IndentPropertyMaker sCorr = new IndentPropertyMaker(m);
        sCorr.setCorresponding(PR_MARGIN_LEFT, PR_MARGIN_RIGHT, PR_MARGIN_TOP, PR_MARGIN_TOP);
        sCorr.setUseParent(false);
        sCorr.setRelative(true);
        sCorr.setPaddingCorresponding(new int[] {
             PR_PADDING_LEFT, PR_PADDING_RIGHT, PR_PADDING_TOP, PR_PADDING_TOP
        });
        sCorr.setBorderWidthCorresponding(new int[] {
            PR_BORDER_LEFT_WIDTH, PR_BORDER_RIGHT_WIDTH, PR_BORDER_TOP_WIDTH, PR_BORDER_TOP_WIDTH
        });
        addPropertyMaker("start-indent", m);

        // end-indent
        m = new LengthProperty.Maker(PR_END_INDENT);
        m.setInherited(true);
        m.setDefault("0pt");
        m.setPercentBase(LengthBase.CONTAINING_REFAREA_WIDTH);
        IndentPropertyMaker eCorr = new IndentPropertyMaker(m);
        eCorr.setCorresponding(PR_MARGIN_RIGHT, PR_MARGIN_LEFT,
                               PR_MARGIN_BOTTOM, PR_MARGIN_BOTTOM);
        eCorr.setUseParent(false);
        eCorr.setRelative(true);
        eCorr.setPaddingCorresponding(new int[] {
            PR_PADDING_RIGHT, PR_PADDING_LEFT, PR_PADDING_BOTTOM, PR_PADDING_BOTTOM
        });
        eCorr.setBorderWidthCorresponding(new int[] {
            PR_BORDER_RIGHT_WIDTH, PR_BORDER_LEFT_WIDTH,
            PR_BORDER_BOTTOM_WIDTH, PR_BORDER_BOTTOM_WIDTH
        });
        addPropertyMaker("end-indent", m);
    }

    private void createMarginInlineProperties() {
        PropertyMaker m;

        // space-end
        m  = new SpaceProperty.Maker(PR_SPACE_END);
        m.useGeneric(genericSpace);
        addPropertyMaker("space-end", m);

        // space-start
        m  = new SpaceProperty.Maker(PR_SPACE_START);
        m.useGeneric(genericSpace);
        addPropertyMaker("space-start", m);
    }

    private void createRelativePosProperties() {
        PropertyMaker m;

        // relative-position
        m  = new EnumProperty.Maker(PR_RELATIVE_POSITION);
        m.setInherited(false);
        m.addEnum("static", getEnumProperty(EN_STATIC, "STATIC"));
        m.addEnum("relative", getEnumProperty(EN_RELATIVE, "RELATIVE"));
        m.setDefault("static");
        m.addShorthand(generics[PR_POSITION]);
        addPropertyMaker("relative-position", m);
    }

    private void createAreaAlignmentProperties() {
        PropertyMaker m;

        // alignment-adjust
        m  = new LengthProperty.Maker(PR_ALIGNMENT_ADJUST);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("baseline", getEnumProperty(EN_BASELINE, "BASELINE"));
        m.addEnum("before-edge", getEnumProperty(EN_BEFORE_EDGE, "BEFORE_EDGE"));
        m.addEnum("text-before-edge", getEnumProperty(EN_TEXT_BEFORE_EDGE, "TEXT_BEFORE_EDGE"));
        m.addEnum("middle", getEnumProperty(EN_MIDDLE, "MIDDLE"));
        m.addEnum("central", getEnumProperty(EN_CENTRAL, "CENTRAL"));
        m.addEnum("after-edge", getEnumProperty(EN_AFTER_EDGE, "AFTER_EDGE"));
        m.addEnum("text-after-edge", getEnumProperty(EN_TEXT_AFTER_EDGE, "TEXT_AFTER_EDGE"));
        m.addEnum("ideographic", getEnumProperty(EN_IDEOGRAPHIC, "IDEOGRAPHIC"));
        m.addEnum("alphabetic", getEnumProperty(EN_ALPHABETIC, "ALPHABETIC"));
        m.addEnum("hanging", getEnumProperty(EN_HANGING, "HANGING"));
        m.addEnum("mathematical", getEnumProperty(EN_MATHEMATICAL, "MATHEMATICAL"));
        m.setDefault("auto");
        m.setPercentBase(LengthBase.ALIGNMENT_ADJUST);
        m.addShorthand(generics[PR_VERTICAL_ALIGN]);
        addPropertyMaker("alignment-adjust", m);

        // alignment-baseline
        m  = new EnumProperty.Maker(PR_ALIGNMENT_BASELINE);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("baseline", getEnumProperty(EN_BASELINE, "BASELINE"));
        m.addEnum("before-edge", getEnumProperty(EN_BEFORE_EDGE, "BEFORE_EDGE"));
        m.addEnum("text-before-edge", getEnumProperty(EN_TEXT_BEFORE_EDGE, "TEXT_BEFORE_EDGE"));
        m.addEnum("middle", getEnumProperty(EN_MIDDLE, "MIDDLE"));
        m.addEnum("central", getEnumProperty(EN_CENTRAL, "CENTRAL"));
        m.addEnum("after-edge", getEnumProperty(EN_AFTER_EDGE, "AFTER_EDGE"));
        m.addEnum("text-after-edge", getEnumProperty(EN_TEXT_AFTER_EDGE, "TEXT_AFTER_EDGE"));
        m.addEnum("ideographic", getEnumProperty(EN_IDEOGRAPHIC, "IDEOGRAPHIC"));
        m.addEnum("alphabetic", getEnumProperty(EN_ALPHABETIC, "ALPHABETIC"));
        m.addEnum("hanging", getEnumProperty(EN_HANGING, "HANGING"));
        m.addEnum("mathematical", getEnumProperty(EN_MATHEMATICAL, "MATHEMATICAL"));
        m.setDefault("auto");
        m.addShorthand(generics[PR_VERTICAL_ALIGN]);
        addPropertyMaker("alignment-baseline", m);

        // baseline-shift
        m  = new LengthProperty.Maker(PR_BASELINE_SHIFT);
        m.setInherited(false);
        m.addEnum("baseline", getEnumProperty(EN_BASELINE, "BASELINE"));
        m.addEnum("sub", getEnumProperty(EN_SUB, "SUB"));
        m.addEnum("super", getEnumProperty(EN_SUPER, "SUPER"));
        m.setDefault("baseline");
        m.addShorthand(generics[PR_VERTICAL_ALIGN]);
        m.setPercentBase(LengthBase.CUSTOM_BASE);
        addPropertyMaker("baseline-shift", m);

        // display-align
        m  = new EnumProperty.Maker(PR_DISPLAY_ALIGN);
        m.setInherited(true);
        m.addEnum("before", getEnumProperty(EN_BEFORE, "BEFORE"));
        m.addEnum("after", getEnumProperty(EN_AFTER, "AFTER"));
        m.addEnum("center", getEnumProperty(EN_CENTER, "CENTER"));
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
/*LF*/  m.addEnum("distribute", getEnumProperty(EN_X_DISTRIBUTE, "DISTRIBUTE"));
/*LF*/  m.addEnum("fill", getEnumProperty(EN_X_FILL, "FILL"));
        m.setDefault("auto");
        addPropertyMaker("display-align", m);

        // dominant-baseline
        m  = new EnumProperty.Maker(PR_DOMINANT_BASELINE);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("use-script", getEnumProperty(EN_USE_SCRIPT, "USE_SCRIPT"));
        m.addEnum("no-change", getEnumProperty(EN_NO_CHANGE, "NO_CHANGE"));
        m.addEnum("reset-size", getEnumProperty(EN_RESET_SIZE, "RESET_SIZE"));
        m.addEnum("ideographic", getEnumProperty(EN_IDEOGRAPHIC, "IDEOGRAPHIC"));
        m.addEnum("alphabetic", getEnumProperty(EN_ALPHABETIC, "ALPHABETIC"));
        m.addEnum("hanging", getEnumProperty(EN_HANGING, "HANGING"));
        m.addEnum("mathematical", getEnumProperty(EN_MATHEMATICAL, "MATHEMATICAL"));
        m.addEnum("central", getEnumProperty(EN_CENTRAL, "CENTRAL"));
        m.addEnum("middle", getEnumProperty(EN_MIDDLE, "MIDDLE"));
        m.addEnum("text-after-edge", getEnumProperty(EN_TEXT_AFTER_EDGE, "TEXT_AFTER_EDGE"));
        m.addEnum("text-before-edge", getEnumProperty(EN_TEXT_BEFORE_EDGE, "TEXT_BEFORE_EDGE"));
        m.setDefault("auto");
        m.addShorthand(generics[PR_VERTICAL_ALIGN]);
        addPropertyMaker("dominant-baseline", m);

        // relative-align
        m  = new EnumProperty.Maker(PR_RELATIVE_ALIGN);
        m.setInherited(true);
        m.addEnum("before", getEnumProperty(EN_BEFORE, "BEFORE"));
        m.addEnum("baseline", getEnumProperty(EN_BASELINE, "BASELINE"));
        m.setDefault("before");
        addPropertyMaker("relative-align", m);
    }

    private void createAreaDimensionProperties() {              // CSOK: MethodLength
        PropertyMaker m;
        LengthProperty.Maker l;
        DimensionPropertyMaker pdim;

        // block-progression-dimension
        m = new LengthRangeProperty.Maker(PR_BLOCK_PROGRESSION_DIMENSION);
        m.setInherited(false);
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_HEIGHT);

        l = new LengthProperty.Maker(CP_MINIMUM);
        l.setDefault("auto");
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_HEIGHT);
        l.setByShorthand(true);
        m.addSubpropMaker(l);

        l = new LengthProperty.Maker(CP_OPTIMUM);
        l.setDefault("auto");
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_HEIGHT);
        l.setByShorthand(true);
        m.addSubpropMaker(l);

        l = new LengthProperty.Maker(CP_MAXIMUM);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setDefault("auto");
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_HEIGHT);
        l.setByShorthand(true);
        m.addSubpropMaker(l);

        pdim = new DimensionPropertyMaker(m);
        pdim.setCorresponding(PR_HEIGHT, PR_HEIGHT, PR_WIDTH, PR_WIDTH);
        pdim.setExtraCorresponding(new int[][] {
             {PR_MIN_HEIGHT, PR_MIN_HEIGHT, PR_MIN_WIDTH, PR_MIN_WIDTH},
             {PR_MAX_HEIGHT, PR_MAX_HEIGHT, PR_MAX_WIDTH, PR_MAX_WIDTH}
        });
        pdim.setRelative(true);
        m.setCorresponding(pdim);
        addPropertyMaker("block-progression-dimension", m);

        // content-height
        l  = new LengthProperty.Maker(PR_CONTENT_HEIGHT);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.addEnum("scale-to-fit", getEnumProperty(EN_SCALE_TO_FIT, "SCALE_TO_FIT"));
        l.addEnum("scale-down-to-fit", getEnumProperty(EN_SCALE_DOWN_TO_FIT, "SCALE_DOWN_TO_FIT"));
        l.addEnum("scale-up-to-fit", getEnumProperty(EN_SCALE_UP_TO_FIT, "SCALE_UP_TO_FIT"));
        l.setDefault("auto");
        l.setPercentBase(LengthBase.IMAGE_INTRINSIC_HEIGHT);
        addPropertyMaker("content-height", l);

        // content-width
        l  = new LengthProperty.Maker(PR_CONTENT_WIDTH);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.addEnum("scale-to-fit", getEnumProperty(EN_SCALE_TO_FIT, "SCALE_TO_FIT"));
        l.addEnum("scale-down-to-fit", getEnumProperty(EN_SCALE_DOWN_TO_FIT, "SCALE_DOWN_TO_FIT"));
        l.addEnum("scale-up-to-fit", getEnumProperty(EN_SCALE_UP_TO_FIT, "SCALE_UP_TO_FIT"));
        l.setDefault("auto");
        l.setPercentBase(LengthBase.IMAGE_INTRINSIC_WIDTH);
        addPropertyMaker("content-width", l);

        // height
        l  = new LengthProperty.Maker(PR_HEIGHT);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_HEIGHT);
        l.setDefault("auto");
        addPropertyMaker("height", l);

        // inline-progression-dimension
        m = new LengthRangeProperty.Maker(PR_INLINE_PROGRESSION_DIMENSION);
        m.setInherited(false);
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);

        l = new LengthProperty.Maker(CP_MINIMUM);
        l.setDefault("auto");
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        l.setByShorthand(true);
        m.addSubpropMaker(l);

        l = new LengthProperty.Maker(CP_OPTIMUM);
        l.setDefault("auto");
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        l.setByShorthand(true);
        m.addSubpropMaker(l);

        l = new LengthProperty.Maker(CP_MAXIMUM);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setDefault("auto");
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        l.setByShorthand(true);
        m.addSubpropMaker(l);

        pdim = new DimensionPropertyMaker(m);
        pdim.setRelative(true);
        pdim.setCorresponding(PR_WIDTH, PR_WIDTH, PR_HEIGHT, PR_HEIGHT);
        pdim.setExtraCorresponding(new int[][] {
            {PR_MIN_WIDTH, PR_MIN_WIDTH, PR_MIN_HEIGHT, PR_MIN_HEIGHT },
            {PR_MAX_WIDTH, PR_MAX_WIDTH, PR_MAX_HEIGHT, PR_MIN_HEIGHT }
        });
        m.setCorresponding(pdim);
        addPropertyMaker("inline-progression-dimension", m);

        // max-height
        m  = new LengthProperty.Maker(PR_MAX_HEIGHT);
        m.addEnum("NONE", getEnumProperty(EN_NONE, "NONE"));
        m.setInherited(false);
        m.setDefault("0pt");
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_HEIGHT);
        addPropertyMaker("max-height", m);

        // max-width
        m  = new LengthProperty.Maker(PR_MAX_WIDTH);
        m.addEnum("NONE", getEnumProperty(EN_NONE, "NONE"));
        m.setInherited(false);
        m.setDefault("none");
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("max-width", m);

        // min-height
        m  = new LengthProperty.Maker(PR_MIN_HEIGHT);
        m.setInherited(false);
        m.setDefault("0pt");
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_HEIGHT);
        addPropertyMaker("min-height", m);

        // min-width
        m  = new LengthProperty.Maker(PR_MIN_WIDTH);
        m.setInherited(false);
        m.setDefault(""); //UA dependent
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("min-width", m);

        // scaling
        m  = new EnumProperty.Maker(PR_SCALING);
        m.setInherited(true);
        m.addEnum("uniform", getEnumProperty(EN_UNIFORM, "UNIFORM"));
        m.addEnum("non-uniform", getEnumProperty(EN_NON_UNIFORM, "NON_UNIFORM"));
        m.setDefault("uniform");
        addPropertyMaker("scaling", m);

        // scaling-method
        m  = new EnumProperty.Maker(PR_SCALING_METHOD);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("integer-pixels",
                  getEnumProperty(EN_INTEGER_PIXELS, "INTEGER_PIXELS"));
        m.addEnum("resample-any-method",
                  getEnumProperty(EN_RESAMPLE_ANY_METHOD, "RESAMPLE_ANY_METHOD"));
        m.setDefault("auto");
        addPropertyMaker("scaling-method", m);

        // width
        l  = new LengthProperty.Maker(PR_WIDTH);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        l.setDefault("auto");
        addPropertyMaker("width", l);

        // fox:block-progression-unit (**CUSTOM EXTENSION**)
        l  = new LengthProperty.Maker(PR_X_BLOCK_PROGRESSION_UNIT);
        l.setInherited(false);
        l.setDefault("0pt");
        addPropertyMaker("fox:block-progression-unit", l);
    }

    private Property calcWritingModeDependent ( int pv, int wm ) {
        if ( pv == EN_LEFT ) {
            if ( wm == Constants.EN_LR_TB ) {
                pv = EN_START;
            } else if ( wm == Constants.EN_RL_TB ) {
                pv = EN_END;
            } else {
                pv = EN_START;
            }
        } else if ( pv == EN_RIGHT ) {
            if ( wm == Constants.EN_LR_TB ) {
                pv = EN_END;
            } else if ( wm == Constants.EN_RL_TB ) {
                pv = EN_START;
            } else {
                pv = EN_END;
            }
        }
        return makeWritingModeDependentEnum ( pv );
    }

    private Property makeWritingModeDependentEnum ( int pv ) {
        if ( pv == EN_START ) {
            return getEnumProperty ( EN_START, "START" );
        } else if ( pv == EN_END ) {
            return getEnumProperty ( EN_END, "END" );
        } else {
            return null;
        }
    }

    private void createBlockAndLineProperties() {               // CSOK: MethodLength
        PropertyMaker m;

        // hyphenation-keep
        m  = new EnumProperty.Maker(PR_HYPHENATION_KEEP);
        m.setInherited(true);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("column", getEnumProperty(EN_COLUMN, "COLUMN"));
        m.addEnum("page", getEnumProperty(EN_PAGE, "PAGE"));
        m.setDefault("auto");
        addPropertyMaker("hyphenation-keep", m);

        // hyphenation-ladder-count
        m  = new NumberProperty.Maker(PR_HYPHENATION_LADDER_COUNT);
        m.setInherited(true);
        m.addEnum("no-limit", getEnumProperty(EN_NO_LIMIT, "NO_LIMIT"));
        m.setDefault("no-limit");
        addPropertyMaker("hyphenation-ladder-count", m);

        // last-line-end-indent
        m  = new LengthProperty.Maker(PR_LAST_LINE_END_INDENT);
        m.setInherited(true);
        m.setDefault("0pt");
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("last-line-end-indent", m);

        // line-height
        m  = new LineHeightPropertyMaker(PR_LINE_HEIGHT);
        m.useGeneric(genericSpace);
        m.setInherited(true);
        m.addKeyword("normal", "1.2");
        m.setPercentBase(LengthBase.FONTSIZE);
        m.setDefault("normal", true);
        m.addShorthand(generics[PR_FONT]);
        addPropertyMaker("line-height", m);

        // line-height-shift-adjustment
        m  = new EnumProperty.Maker(PR_LINE_HEIGHT_SHIFT_ADJUSTMENT);
        m.setInherited(true);
        m.addEnum("consider-shifts", getEnumProperty(EN_CONSIDER_SHIFTS, "CONSIDER_SHIFTS"));
        m.addEnum("disregard-shifts", getEnumProperty(EN_DISREGARD_SHIFTS, "DISREGARD_SHIFTS"));
        m.setDefault("consider-shifts");
        addPropertyMaker("line-height-shift-adjustment", m);

        // line-stacking-strategy
        m  = new EnumProperty.Maker(PR_LINE_STACKING_STRATEGY);
        m.setInherited(true);
        m.addEnum("line-height", getEnumProperty(EN_LINE_HEIGHT, "LINE_HEIGHT"));
        m.addEnum("font-height", getEnumProperty(EN_FONT_HEIGHT, "FONT_HEIGHT"));
        m.addEnum("max-height", getEnumProperty(EN_MAX_HEIGHT, "MAX_HEIGHT"));
        m.setDefault("max-height");
        addPropertyMaker("line-stacking-strategy", m);

        // linefeed-treatment
        m  = new EnumProperty.Maker(PR_LINEFEED_TREATMENT);
        m.setInherited(true);
        m.addEnum("ignore", getEnumProperty(EN_IGNORE, "IGNORE"));
        m.addEnum("preserve", getEnumProperty(EN_PRESERVE, "PRESERVE"));
        m.addEnum("treat-as-space", getEnumProperty(EN_TREAT_AS_SPACE, "TREAT_AS_SPACE"));
        m.addEnum("treat-as-zero-width-space",
                  getEnumProperty(EN_TREAT_AS_ZERO_WIDTH_SPACE,
                                  "TREAT_AS_ZERO_WIDTH_SPACE"));
        m.setDefault("treat-as-space");
        m.addShorthand(generics[PR_WHITE_SPACE]);
        addPropertyMaker("linefeed-treatment", m);

        // white-space-treatment
        m  = new EnumProperty.Maker(PR_WHITE_SPACE_TREATMENT);
        m.setInherited(true);
        m.addEnum("ignore", getEnumProperty(EN_IGNORE, "IGNORE"));
        m.addEnum("preserve", getEnumProperty(EN_PRESERVE, "PRESERVE"));
        m.addEnum("ignore-if-before-linefeed",
                  getEnumProperty(EN_IGNORE_IF_BEFORE_LINEFEED,
                                  "IGNORE_IF_BEFORE_LINEFEED"));
        m.addEnum("ignore-if-after-linefeed",
                  getEnumProperty(EN_IGNORE_IF_AFTER_LINEFEED,
                                  "IGNORE_IF_AFTER_LINEFEED"));
        m.addEnum("ignore-if-surrounding-linefeed",
                  getEnumProperty(EN_IGNORE_IF_SURROUNDING_LINEFEED,
                                  "IGNORE_IF_SURROUNDING_LINEFEED"));
        m.setDefault("ignore-if-surrounding-linefeed");
        m.addShorthand(generics[PR_WHITE_SPACE]);
        addPropertyMaker("white-space-treatment", m);

        // text-align TODO: make it a StringProperty with enums.
        m  = new EnumProperty.Maker(PR_TEXT_ALIGN) {
            public Property get(int subpropId, PropertyList propertyList,
                    boolean bTryInherit, boolean bTryDefault) throws PropertyException {
                Property p = super.get(subpropId, propertyList, bTryInherit, bTryDefault);
                if ( p != null ) {
                    int pv = p.getEnum();
                    if ( ( pv == EN_LEFT ) || ( pv == EN_RIGHT ) ) {
                        p = calcWritingModeDependent
                            ( pv, propertyList.get(Constants.PR_WRITING_MODE).getEnum() );
                    }
                }
                return p;
            }
        };
        m.setInherited(true);
        m.addEnum("center", getEnumProperty(EN_CENTER, "CENTER"));
        m.addEnum("end", getEnumProperty(EN_END, "END"));
        m.addEnum("start", getEnumProperty(EN_START, "START"));
        m.addEnum("justify", getEnumProperty(EN_JUSTIFY, "JUSTIFY"));
        // [GA] must defer writing-mode relative mapping of left/right
        m.addEnum("left", getEnumProperty(EN_LEFT, "LEFT"));
        m.addEnum("right", getEnumProperty(EN_RIGHT, "RIGHT"));
        // [GA] inside and outside are not correctly implemented by the following mapping
        m.addEnum("inside", getEnumProperty(EN_START, "START"));
        m.addEnum("outside", getEnumProperty(EN_END, "END"));
        m.setDefault("start");
        addPropertyMaker("text-align", m);

        // text-align-last
        m  = new EnumProperty.Maker(PR_TEXT_ALIGN_LAST) {
            public Property get(int subpropId, PropertyList propertyList,
                    boolean bTryInherit, boolean bTryDefault) throws PropertyException {
                Property p = super.get(subpropId, propertyList, bTryInherit, bTryDefault);
                if (p != null && p.getEnum() == EN_RELATIVE) {
                    //The default may have been returned, so check inherited value
                    p = propertyList.getNearestSpecified(PR_TEXT_ALIGN_LAST);
                    if (p.getEnum() == EN_RELATIVE) {
                        return calcRelative(propertyList);
                    }
                }
                return p;
            }
            private Property calcRelative(PropertyList propertyList) throws PropertyException {
                Property corresponding = propertyList.get(PR_TEXT_ALIGN);
                if (corresponding == null) {
                    return null;
                }
                int correspondingValue = corresponding.getEnum();
                if (correspondingValue == EN_JUSTIFY) {
                    return getEnumProperty(EN_START, "START");
                } else if (correspondingValue == EN_END) {
                    return getEnumProperty(EN_END, "END");
                } else if (correspondingValue == EN_START) {
                    return getEnumProperty(EN_START, "START");
                } else if (correspondingValue == EN_CENTER) {
                    return getEnumProperty(EN_CENTER, "CENTER");
                } else if (correspondingValue == EN_LEFT) {
                    return calcWritingModeDependent
                        ( EN_LEFT, propertyList.get(Constants.PR_WRITING_MODE).getEnum() );
                } else if (correspondingValue == EN_RIGHT) {
                    return calcWritingModeDependent
                        ( EN_RIGHT, propertyList.get(Constants.PR_WRITING_MODE).getEnum() );
                } else {
                    return null;
                }
            }
        };
        m.setInherited(false); //Actually it's "true" but the special PropertyMaker compensates
        // Note: both 'end', 'right' and 'outside' are mapped to END
        //       both 'start', 'left' and 'inside' are mapped to START
        m.addEnum("relative", getEnumProperty(EN_RELATIVE, "RELATIVE"));
        m.addEnum("center", getEnumProperty(EN_CENTER, "CENTER"));
        m.addEnum("end", getEnumProperty(EN_END, "END"));
        m.addEnum("right", getEnumProperty(EN_END, "END"));
        m.addEnum("start", getEnumProperty(EN_START, "START"));
        m.addEnum("left", getEnumProperty(EN_START, "START"));
        m.addEnum("justify", getEnumProperty(EN_JUSTIFY, "JUSTIFY"));
        m.addEnum("inside", getEnumProperty(EN_START, "START"));
        m.addEnum("outside", getEnumProperty(EN_END, "END"));
        m.setDefault("relative", true);
        addPropertyMaker("text-align-last", m);

        // text-indent
        m  = new LengthProperty.Maker(PR_TEXT_INDENT);
        m.setInherited(true);
        m.setDefault("0pt");
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("text-indent", m);

        // white-space-collapse
        m  = new EnumProperty.Maker(PR_WHITE_SPACE_COLLAPSE);
        m.useGeneric(genericBoolean);
        m.setInherited(true);
        m.setDefault("true");
        m.addShorthand(generics[PR_WHITE_SPACE]);
        addPropertyMaker("white-space-collapse", m);

        // wrap-option
        m  = new EnumProperty.Maker(PR_WRAP_OPTION);
        m.setInherited(true);
        m.addEnum("wrap", getEnumProperty(EN_WRAP, "WRAP"));
        m.addEnum("no-wrap", getEnumProperty(EN_NO_WRAP, "NO_WRAP"));
        m.setDefault("wrap");
        m.addShorthand(generics[PR_WHITE_SPACE]);
        addPropertyMaker("wrap-option", m);
    }

    private void createCharacterProperties() {
        PropertyMaker m;

        // character
        m  = new CharacterProperty.Maker(PR_CHARACTER);
        m.setInherited(false);
        m.setDefault("none");
        addPropertyMaker("character", m);

        // letter-spacing
        m  = new SpacingPropertyMaker(PR_LETTER_SPACING);
        m.useGeneric(genericSpace);
        m.setInherited(true);
        m.getSubpropMaker(CP_PRECEDENCE).setDefault("force");
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        m.setDefault("normal");
        m.addEnum("normal", getEnumProperty(EN_NORMAL, "NORMAL"));
        addPropertyMaker("letter-spacing", m);

        // suppress-at-line-break
        m  = new EnumProperty.Maker(PR_SUPPRESS_AT_LINE_BREAK);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("suppress", getEnumProperty(EN_SUPPRESS, "SUPPRESS"));
        m.addEnum("retain", getEnumProperty(EN_RETAIN, "RETAIN"));
        m.setDefault("auto");
        addPropertyMaker("suppress-at-line-break", m);

        // text-decoration
        //m  = new EnumProperty.Maker(PR_TEXT_DECORATION);
        m  = new TextDecorationMaker(PR_TEXT_DECORATION);
        m.setInherited(false);
        m.addEnum("none", getEnumProperty(EN_NONE, "NONE"));
        m.addEnum("underline", getEnumProperty(EN_UNDERLINE, "UNDERLINE"));
        m.addEnum("overline", getEnumProperty(EN_OVERLINE, "OVERLINE"));
        m.addEnum("line-through", getEnumProperty(EN_LINE_THROUGH, "LINE_THROUGH"));
        m.addEnum("blink", getEnumProperty(EN_BLINK, "BLINK"));
        m.addEnum("no-underline", getEnumProperty(EN_NO_UNDERLINE, "NO_UNDERLINE"));
        m.addEnum("no-overline", getEnumProperty(EN_NO_OVERLINE, "NO_OVERLINE"));
        m.addEnum("no-line-through", getEnumProperty(EN_NO_LINE_THROUGH, "NO_LINE_THROUGH"));
        m.addEnum("no-blink", getEnumProperty(EN_NO_BLINK, "NO_BLINK"));
        m.setDefault("none");
        addPropertyMaker("text-decoration", m);

        // text-shadow
        m  = new ToBeImplementedProperty.Maker(PR_TEXT_SHADOW);
        m.setInherited(false);
        m.setDefault("none");
        addPropertyMaker("text-shadow", m);

        // text-transform
        m  = new EnumProperty.Maker(PR_TEXT_TRANSFORM);
        m.setInherited(true);
        m.addEnum("none", getEnumProperty(EN_NONE, "NONE"));
        m.addEnum("capitalize", getEnumProperty(EN_CAPITALIZE, "CAPITALIZE"));
        m.addEnum("uppercase", getEnumProperty(EN_UPPERCASE, "UPPERCASE"));
        m.addEnum("lowercase", getEnumProperty(EN_LOWERCASE, "LOWERCASE"));
        m.setDefault("none");
        addPropertyMaker("text-transform", m);

        // treat-as-word-space
        m  = new EnumProperty.Maker(PR_TREAT_AS_WORD_SPACE);
        m.useGeneric(genericBoolean);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.setInherited(false);
        m.setDefault("auto");
        addPropertyMaker("treat-as-word-space", m);

        // word-spacing
        m  = new SpacingPropertyMaker(PR_WORD_SPACING);
        m.useGeneric(genericSpace);
        m.setInherited(true);
        m.getSubpropMaker(CP_PRECEDENCE).setDefault("force");
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        m.setDefault("normal");
        m.addEnum("normal", getEnumProperty(EN_NORMAL, "NORMAL"));
        addPropertyMaker("word-spacing", m);
    }

    private void createColorProperties() {
        PropertyMaker m;

        // color
        m  = new ColorProperty.Maker(PR_COLOR);
        m.useGeneric(genericColor);
        m.setInherited(true);
        m.setDefault("black");
        addPropertyMaker("color", m);

        // color-profile-name
        m  = new StringProperty.Maker(PR_COLOR_PROFILE_NAME);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("color-profile-name", m);

        // rendering-intent
        m  = new EnumProperty.Maker(PR_RENDERING_INTENT);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("perceptual", getEnumProperty(EN_PERCEPTUAL, "PERCEPTUAL"));
        m.addEnum("relative-colorimetric", getEnumProperty(EN_RELATIVE_COLOMETRIC,
                                                           "RELATIVE_COLOMETRIC"));
        m.addEnum("saturation", getEnumProperty(EN_SATURATION, "SATURATION"));
        m.addEnum("absolute-colorimetric", getEnumProperty(EN_ABSOLUTE_COLORMETRIC,
                                                           "ABSOLUTE_COLORMETRIC"));
        m.setDefault("auto");
        addPropertyMaker("rendering-intent", m);
    }

    private void createFloatProperties() {
        PropertyMaker m;

        // clear
        m  = new EnumProperty.Maker(PR_CLEAR);
        m.setInherited(false);
        // Note that left -> start and right -> end.
        m.addEnum("start", getEnumProperty(EN_START, "START"));
        m.addEnum("end", getEnumProperty(EN_END, "END"));
        m.addEnum("left", getEnumProperty(EN_START, "START"));
        m.addEnum("right", getEnumProperty(EN_END, "END"));
        m.addEnum("both", getEnumProperty(EN_BOTH, "BOTH"));
        m.addEnum("none", getEnumProperty(EN_NONE, "NONE"));
        m.setDefault("none");
        addPropertyMaker("clear", m);

        // float
        m  = new EnumProperty.Maker(PR_FLOAT);
        m.setInherited(false);
        // Note that left -> start and right -> end.
        m.addEnum("before", getEnumProperty(EN_BEFORE, "BEFORE"));
        m.addEnum("start", getEnumProperty(EN_START, "START"));
        m.addEnum("end", getEnumProperty(EN_END, "END"));
        m.addEnum("left", getEnumProperty(EN_START, "START"));
        m.addEnum("right", getEnumProperty(EN_END, "END"));
        m.addEnum("none", getEnumProperty(EN_NONE, "NONE"));
        m.setDefault("none");
        addPropertyMaker("float", m);

        // intrusion-displace
        m  = new EnumProperty.Maker(PR_INTRUSION_DISPLACE);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("none", getEnumProperty(EN_NONE, "NONE"));
        m.addEnum("line", getEnumProperty(EN_LINE, "LINE"));
        m.addEnum("indent", getEnumProperty(EN_INDENT, "INDENT"));
        m.addEnum("block", getEnumProperty(EN_BLOCK, "BLOCK"));
        m.setDefault("none");
        addPropertyMaker("intrusion-displace", m);
    }

    private void createKeepsAndBreaksProperties() {
        PropertyMaker m;

        // break-after
        m  = new EnumProperty.Maker(PR_BREAK_AFTER);
        m.useGeneric(genericBreak);
        m.addShorthand(generics[PR_PAGE_BREAK_AFTER]);
        addPropertyMaker("break-after", m);

        // break-before
        m  = new EnumProperty.Maker(PR_BREAK_BEFORE);
        m.useGeneric(genericBreak);
        m.addShorthand(generics[PR_PAGE_BREAK_BEFORE]);
        addPropertyMaker("break-before", m);

        // keep-together
        m  = new KeepProperty.Maker(PR_KEEP_TOGETHER);
        m.useGeneric(genericKeep);
        m.setInherited(true);
        m.setDefault("auto");
        m.addShorthand(generics[PR_PAGE_BREAK_INSIDE]);
        addPropertyMaker("keep-together", m);

        // keep-with-next
        m  = new KeepProperty.Maker(PR_KEEP_WITH_NEXT);
        m.useGeneric(genericKeep);
        m.setInherited(false);
        m.setDefault("auto");
        m.addShorthand(generics[PR_PAGE_BREAK_AFTER]);
        addPropertyMaker("keep-with-next", m);

        // keep-with-previous
        m  = new KeepProperty.Maker(PR_KEEP_WITH_PREVIOUS);
        m.useGeneric(genericKeep);
        m.setInherited(false);
        m.setDefault("auto");
        m.addShorthand(generics[PR_PAGE_BREAK_BEFORE]);
        addPropertyMaker("keep-with-previous", m);

        // orphans
        m  = new NumberProperty.Maker(PR_ORPHANS);
        m.setInherited(true);
        m.setDefault("2");
        addPropertyMaker("orphans", m);

        // widows
        m  = new NumberProperty.Maker(PR_WIDOWS);
        m.setInherited(true);
        m.setDefault("2");
        addPropertyMaker("widows", m);

        // fox:widow-content-limit
        m  = new LengthProperty.Maker(PR_X_WIDOW_CONTENT_LIMIT);
        m.setInherited(true);
        m.setDefault("0pt");
        addPropertyMaker("fox:widow-content-limit", m);

        // fox:orphan-content-limit
        m  = new LengthProperty.Maker(PR_X_ORPHAN_CONTENT_LIMIT);
        m.setInherited(true);
        m.setDefault("0pt");
        addPropertyMaker("fox:orphan-content-limit", m);
    }

    private void createLayoutProperties() {
        PropertyMaker m;

        // clip
        m  = new ToBeImplementedProperty.Maker(PR_CLIP);
        m.setInherited(false);
        m.setDefault("auto");
        addPropertyMaker("clip", m);

        // overflow
        m  = new EnumProperty.Maker(PR_OVERFLOW);
        m.setInherited(false);
        m.addEnum("visible", getEnumProperty(EN_VISIBLE, "VISIBLE"));
        m.addEnum("hidden", getEnumProperty(EN_HIDDEN, "HIDDEN"));
        m.addEnum("scroll", getEnumProperty(EN_SCROLL, "SCROLL"));
        m.addEnum("error-if-overflow", getEnumProperty(EN_ERROR_IF_OVERFLOW, "ERROR_IF_OVERFLOW"));
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.setDefault("auto");
        addPropertyMaker("overflow", m);

        // reference-orientation
        m  = new ReferenceOrientationMaker(PR_REFERENCE_ORIENTATION);
        m.setInherited(true);
        m.setDefault("0");
        addPropertyMaker("reference-orientation", m);

        // span
        m  = new EnumProperty.Maker(PR_SPAN);
        m.setInherited(false);
        m.addEnum("none", getEnumProperty(EN_NONE, "NONE"));
        m.addEnum("all", getEnumProperty(EN_ALL, "ALL"));
        m.setDefault("none");
        addPropertyMaker("span", m);

        // fox:disable-column-balancing
        m  = new EnumProperty.Maker(PR_X_DISABLE_COLUMN_BALANCING);
        m.useGeneric(genericBoolean);
        m.setInherited(true);
        m.setDefault("false");
        addPropertyMaker("fox:disable-column-balancing", m);
    }

    private void createLeaderAndRuleProperties() {
        PropertyMaker m;
        PropertyMaker sub;

        // leader-alignment
        m  = new EnumProperty.Maker(PR_LEADER_ALIGNMENT);
        m.setInherited(true);
        m.addEnum("none", getEnumProperty(EN_NONE, "NONE"));
        m.addEnum("reference-area", getEnumProperty(EN_REFERENCE_AREA, "REFERENCE_AREA"));
        m.addEnum("page", getEnumProperty(EN_PAGE, "PAGE"));
        m.setDefault("none");
        addPropertyMaker("leader-alignment", m);

        // leader-pattern
        m  = new EnumProperty.Maker(PR_LEADER_PATTERN);
        m.setInherited(true);
        m.addEnum("space", getEnumProperty(EN_SPACE, "SPACE"));
        m.addEnum("rule", getEnumProperty(EN_RULE, "RULE"));
        m.addEnum("dots", getEnumProperty(EN_DOTS, "DOTS"));
        m.addEnum("use-content", getEnumProperty(EN_USECONTENT, "USECONTENT"));
        m.setDefault("space");
        addPropertyMaker("leader-pattern", m);

        // leader-pattern-width
        m  = new LengthProperty.Maker(PR_LEADER_PATTERN_WIDTH);
        m.setInherited(true);
        m.setDefault("use-font-metrics", true);
        m.addKeyword("use-font-metrics", "0pt");
        m.setPercentBase(LengthBase.PARENT_AREA_WIDTH);
        addPropertyMaker("leader-pattern-width", m);

        // leader-length
        m  = new LengthRangeProperty.Maker(PR_LEADER_LENGTH);
        m.setInherited(true);
        m.setPercentBase(LengthBase.PARENT_AREA_WIDTH);

        sub = new LengthProperty.Maker(CP_MINIMUM);
        sub.setDefault("0pt");
        sub.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        sub.setByShorthand(true);
        m.addSubpropMaker(sub);

        sub = new LengthProperty.Maker(CP_OPTIMUM);
        sub.setDefault("12.0pt");
        sub.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        sub.setByShorthand(true);
        m.addSubpropMaker(sub);

        sub = new LengthProperty.Maker(CP_MAXIMUM);
        sub.setDefault("100%", true);
        sub.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        sub.setByShorthand(true);
        m.addSubpropMaker(sub);
        addPropertyMaker("leader-length", m);

        // rule-style
        m  = new EnumProperty.Maker(PR_RULE_STYLE);
        m.setInherited(true);
        m.addEnum("none", getEnumProperty(EN_NONE, "NONE"));
        m.addEnum("dotted", getEnumProperty(EN_DOTTED, "DOTTED"));
        m.addEnum("dashed", getEnumProperty(EN_DASHED, "DASHED"));
        m.addEnum("solid", getEnumProperty(EN_SOLID, "SOLID"));
        m.addEnum("double", getEnumProperty(EN_DOUBLE, "DOUBLE"));
        m.addEnum("groove", getEnumProperty(EN_GROOVE, "GROOVE"));
        m.addEnum("ridge", getEnumProperty(EN_RIDGE, "RIDGE"));
        m.setDefault("solid");
        addPropertyMaker("rule-style", m);

        // rule-thickness
        m  = new LengthProperty.Maker(PR_RULE_THICKNESS);
        m.setInherited(true);
        m.setDefault("1.0pt");
        addPropertyMaker("rule-thickness", m);
    }

    private void createDynamicProperties() {
        PropertyMaker m;

        // active-state
        m  = new ToBeImplementedProperty.Maker(PR_ACTIVE_STATE);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("active-state", m);

        // auto-restore
        m  = new ToBeImplementedProperty.Maker(PR_AUTO_RESTORE);
        m.setInherited(true);
        m.setDefault("false");
        addPropertyMaker("auto-restore", m);

        // case-name
        m  = new ToBeImplementedProperty.Maker(PR_CASE_NAME);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("case-name", m);

        // case-title
        m  = new ToBeImplementedProperty.Maker(PR_CASE_TITLE);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("case-title", m);

        // destination-placement-offset
        m  = new ToBeImplementedProperty.Maker(PR_DESTINATION_PLACEMENT_OFFSET);
        m.setInherited(false);
        m.setDefault("0pt");
        addPropertyMaker("destination-placement-offset", m);

        // external-destination
        m  = new StringProperty.Maker(PR_EXTERNAL_DESTINATION);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("external-destination", m);

        // indicate-destination
        m  = new ToBeImplementedProperty.Maker(PR_INDICATE_DESTINATION);
        m.setInherited(false);
        m.setDefault("false");
        addPropertyMaker("indicate-destination", m);

        // internal-destination
        m  = new StringProperty.Maker(PR_INTERNAL_DESTINATION);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("internal-destination", m);

        // show-destination
        m  = new EnumProperty.Maker(PR_SHOW_DESTINATION);
        m.setInherited(false);
        m.addEnum("new", getEnumProperty(EN_NEW, "NEW"));
        m.addEnum("replace", getEnumProperty(EN_REPLACE, "REPLACE"));
        m.setDefault("replace");
        addPropertyMaker("show-destination", m);

        // starting-state
        m  = new EnumProperty.Maker(PR_STARTING_STATE);
        m.setInherited(false);
        m.addEnum("show", getEnumProperty(EN_SHOW, "SHOW"));
        m.addEnum("hide", getEnumProperty(EN_HIDE, "HIDE"));
        m.setDefault("show");
        addPropertyMaker("starting-state", m);

        // switch-to
        m  = new ToBeImplementedProperty.Maker(PR_SWITCH_TO);
        m.setInherited(false);
        m.setDefault("xsl-any");
        addPropertyMaker("switch-to", m);

        // target-presentation-context
        m  = new ToBeImplementedProperty.Maker(PR_TARGET_PRESENTATION_CONTEXT);
        m.setInherited(false);
        m.setDefault("use-target-processing-context");
        addPropertyMaker("target-presentation-context", m);

        // target-processing-context
        m  = new ToBeImplementedProperty.Maker(PR_TARGET_PROCESSING_CONTEXT);
        m.setInherited(false);
        m.setDefault("document-root");
        addPropertyMaker("target-processing-context", m);

        // target-stylesheet
        m  = new ToBeImplementedProperty.Maker(PR_TARGET_STYLESHEET);
        m.setInherited(false);
        m.setDefault("use-normal-stylesheet");
        addPropertyMaker("target-stylesheet", m);
    }

    private void createMarkersProperties() {
        PropertyMaker m;

        // marker-class-name
        m  = new StringProperty.Maker(PR_MARKER_CLASS_NAME);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("marker-class-name", m);

        // retrieve-class-name
        m  = new StringProperty.Maker(PR_RETRIEVE_CLASS_NAME);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("retrieve-class-name", m);

        // retrieve-position
        m  = new EnumProperty.Maker(PR_RETRIEVE_POSITION);
        m.setInherited(false);
        m.addEnum("first-starting-within-page", getEnumProperty(EN_FSWP, "FSWP"));
        m.addEnum("first-including-carryover", getEnumProperty(EN_FIC, "FIC"));
        m.addEnum("last-starting-within-page", getEnumProperty(EN_LSWP, "LSWP"));
        m.addEnum("last-ending-within-page", getEnumProperty(EN_LEWP, "LEWP"));
        m.setDefault("first-starting-within-page");
        addPropertyMaker("retrieve-position", m);

        // retrieve-boundary
        m  = new EnumProperty.Maker(PR_RETRIEVE_BOUNDARY);
        m.setInherited(false);
        m.addEnum("page", getEnumProperty(EN_PAGE, "PAGE"));
        m.addEnum("page-sequence", getEnumProperty(EN_PAGE_SEQUENCE, "PAGE_SEQUENCE"));
        m.addEnum("document", getEnumProperty(EN_DOCUMENT, "DOCUMENT"));
        m.setDefault("page-sequence");
        addPropertyMaker("retrieve-boundary", m);

        // retrieve-position-within-table
        m  = new EnumProperty.Maker(PR_RETRIEVE_POSITION_WITHIN_TABLE);
        m.setInherited(false);
        m.addEnum("first-starting", getEnumProperty(EN_FIRST_STARTING, "FIRST_STARTING"));
        m.addEnum("first-including-carryover", getEnumProperty(EN_FIC, "FIC"));
        m.addEnum("last-starting", getEnumProperty(EN_LAST_STARTING, "LAST_STARTING"));
        m.addEnum("last-ending", getEnumProperty(EN_LAST_ENDING, "LAST_ENDING"));
        m.setDefault("first-starting");
        addPropertyMaker("retrieve-position-within-table", m);

        // retrieve-boundary-within-table
        m  = new EnumProperty.Maker(PR_RETRIEVE_BOUNDARY_WITHIN_TABLE);
        m.setInherited(false);
        m.addEnum("table", getEnumProperty(EN_TABLE, "TABLE"));
        m.addEnum("table-fragment", getEnumProperty(EN_TABLE_FRAGMENT, "TABLE_FRAGMENT"));
        m.addEnum("page", getEnumProperty(EN_DOCUMENT, "PAGE"));
        m.setDefault("table");
        addPropertyMaker("retrieve-boundary-within-table", m);
    }

    private void createNumberToStringProperties() {
        PropertyMaker m;

        // format
        m  = new StringProperty.Maker(PR_FORMAT);
        m.setInherited(false);
        m.setDefault("1");
        addPropertyMaker("format", m);

        // grouping-separator
        m  = new CharacterProperty.Maker(PR_GROUPING_SEPARATOR);
        m.setInherited(false);
        m.setDefault("none");
        addPropertyMaker("grouping-separator", m);

        // grouping-size
        m  = new NumberProperty.Maker(PR_GROUPING_SIZE);
        m.setInherited(false);
        m.setDefault("0");
        addPropertyMaker("grouping-size", m);

        // letter-value
        m  = new EnumProperty.Maker(PR_LETTER_VALUE);
        m.setInherited(false);
        m.addEnum("alphabetic", getEnumProperty(EN_ALPHABETIC, "ALPHABETIC"));
        m.addEnum("traditional", getEnumProperty(EN_TRADITIONAL, "TRADITIONAL"));
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.setDefault("auto");
        addPropertyMaker("letter-value", m);

        // fox:alt-text, used for accessibility
        m = new StringProperty.Maker(PR_X_NUMBER_CONVERSION_FEATURES);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("fox:number-conversion-features", m);
    }

    private void createPaginationAndLayoutProperties() {
        PropertyMaker m;
        LengthProperty.Maker l;

        // blank-or-not-blank
        m  = new EnumProperty.Maker(PR_BLANK_OR_NOT_BLANK);
        m.setInherited(false);
        m.addEnum("blank", getEnumProperty(EN_BLANK, "BLANK"));
        m.addEnum("not-blank", getEnumProperty(EN_NOT_BLANK, "NOT_BLANK"));
        m.addEnum("any", getEnumProperty(EN_ANY, "ANY"));
        m.setDefault("any");
        addPropertyMaker("blank-or-not-blank", m);

        // column-count
        m  = new NumberProperty.PositiveIntegerMaker(PR_COLUMN_COUNT);
        m.setInherited(false);
        m.setDefault("1");
        addPropertyMaker("column-count", m);

        // column-gap
        l  = new LengthProperty.Maker(PR_COLUMN_GAP);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.setDefault("0.25in");
        addPropertyMaker("column-gap", l);

        // extent
        m  = new LengthProperty.Maker(PR_EXTENT);
        m.setInherited(true);
        m.setDefault("0pt");
        m.setPercentBase(LengthBase.CUSTOM_BASE);
        addPropertyMaker("extent", m);

        // flow-name
        m  = new StringProperty.Maker(PR_FLOW_NAME);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("flow-name", m);

        // force-page-count
        m  = new EnumProperty.Maker(PR_FORCE_PAGE_COUNT);
        m.setInherited(false);
        m.addEnum("even", getEnumProperty(EN_EVEN, "EVEN"));
        m.addEnum("odd", getEnumProperty(EN_ODD, "ODD"));
        m.addEnum("end-on-even", getEnumProperty(EN_END_ON_EVEN, "END_ON_EVEN"));
        m.addEnum("end-on-odd", getEnumProperty(EN_END_ON_ODD, "END_ON_ODD"));
        m.addEnum("no-force", getEnumProperty(EN_NO_FORCE, "NO_FORCE"));
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.setDefault("auto");
        addPropertyMaker("force-page-count", m);

        // initial-page-number
        m  = new NumberProperty.PositiveIntegerMaker(PR_INITIAL_PAGE_NUMBER);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("auto-odd", getEnumProperty(EN_AUTO_ODD, "AUTO_ODD"));
        m.addEnum("auto-even", getEnumProperty(EN_AUTO_EVEN, "AUTO_EVEN"));
        m.setDefault("auto");
        addPropertyMaker("initial-page-number", m);

        // master-name
        m  = new StringProperty.Maker(PR_MASTER_NAME);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("master-name", m);

        // master-reference
        m  = new StringProperty.Maker(PR_MASTER_REFERENCE);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("master-reference", m);

        // maximum-repeats
        m  = new NumberProperty.Maker(PR_MAXIMUM_REPEATS);
        m.setInherited(false);
        m.addEnum("no-limit", getEnumProperty(EN_NO_LIMIT, "NO_LIMIT"));
        m.setDefault("no-limit");
        addPropertyMaker("maximum-repeats", m);

        // media-usage
        m  = new EnumProperty.Maker(PR_MEDIA_USAGE);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("paginate", getEnumProperty(EN_PAGINATE, "PAGINATE"));
        m.addEnum("bounded-in-one-dimension", getEnumProperty(EN_BOUNDED_IN_ONE_DIMENSION,
                                                              "BOUNDED_IN_ONE_DIMENSION"));
        m.addEnum("unbounded", getEnumProperty(EN_UNBOUNDED, "UNBOUNDED"));
        m.setDefault("auto");
        addPropertyMaker("media-usage", m);

        // odd-or-even
        m  = new EnumProperty.Maker(PR_ODD_OR_EVEN);
        m.setInherited(false);
        m.addEnum("odd", getEnumProperty(EN_ODD, "ODD"));
        m.addEnum("even", getEnumProperty(EN_EVEN, "EVEN"));
        m.addEnum("any", getEnumProperty(EN_ANY, "ANY"));
        m.setDefault("any");
        addPropertyMaker("odd-or-even", m);

        // page-height
        l  = new PageDimensionMaker(PR_PAGE_HEIGHT);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.addEnum("indefinite", getEnumProperty(EN_INDEFINITE, "INDEFINITE"));
        l.setDefault("auto");
        addPropertyMaker("page-height", l);

        // page-position
        m  = new EnumProperty.Maker(PR_PAGE_POSITION);
        m.setInherited(false);
        m.addEnum("first", getEnumProperty(EN_FIRST, "FIRST"));
        m.addEnum("last", getEnumProperty(EN_LAST, "LAST"));
        m.addEnum("rest", getEnumProperty(EN_REST, "REST"));
        m.addEnum("any", getEnumProperty(EN_ANY, "ANY"));
        m.addEnum("only", getEnumProperty(EN_ONLY, "ONLY")); //XSL 1.1
        m.setDefault("any");
        addPropertyMaker("page-position", m);

        // page-width
        l  = new PageDimensionMaker(PR_PAGE_WIDTH);
        l.setInherited(false);
        l.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        l.addEnum("indefinite", getEnumProperty(EN_INDEFINITE, "INDEFINITE"));
        l.setDefault("auto");
        addPropertyMaker("page-width", l);

        // precedence
        m  = new EnumProperty.Maker(PR_PRECEDENCE);
        m.useGeneric(genericBoolean);
        m.setInherited(false);
        m.setDefault("false");
        addPropertyMaker("precedence", m);

        // region-name
        m  = new StringProperty.Maker(PR_REGION_NAME);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("region-name", m);
    }

    private void createTableProperties() {
        PropertyMaker m;
        PropertyMaker sub;

        // border-after-precedence
        m  = new TableBorderPrecedence(PR_BORDER_AFTER_PRECEDENCE);
        m.setInherited(false);
        m.addEnum("force", getEnumProperty(EN_FORCE, "FORCE"));
        addPropertyMaker("border-after-precedence", m);

        // border-before-precedence
        m  = new TableBorderPrecedence(PR_BORDER_BEFORE_PRECEDENCE);
        m.setInherited(false);
        m.addEnum("force", getEnumProperty(EN_FORCE, "FORCE"));
        addPropertyMaker("border-before-precedence", m);

        // border-collapse
        m  = new EnumProperty.Maker(PR_BORDER_COLLAPSE);
        m.setInherited(true);
        m.setDefault("collapse");
        m.addEnum("separate", getEnumProperty(EN_SEPARATE, "SEPARATE"));
        m.addEnum("collapse-with-precedence", getEnumProperty(
                EN_COLLAPSE_WITH_PRECEDENCE, "COLLAPSE_WITH_PRECEDENCE"));
        m.addEnum("collapse", getEnumProperty(EN_COLLAPSE, "COLLAPSE"));
        addPropertyMaker("border-collapse", m);

        // border-end-precedence
        m  = new TableBorderPrecedence(PR_BORDER_END_PRECEDENCE);
        m.setInherited(false);
        m.addEnum("force", getEnumProperty(EN_FORCE, "FORCE"));
        addPropertyMaker("border-end-precedence", m);

        // border-separation
        m  = new LengthPairProperty.Maker(PR_BORDER_SEPARATION);
        m.setInherited(true);
        m.addShorthand(generics[PR_BORDER_SPACING]);

        sub = new LengthProperty.Maker(CP_BLOCK_PROGRESSION_DIRECTION);
        sub.setDefault("0pt");
        sub.setByShorthand(true);
        m.addSubpropMaker(sub);

        sub = new LengthProperty.Maker(CP_INLINE_PROGRESSION_DIRECTION);
        sub.setDefault("0pt");
        sub.setByShorthand(true);
        m.addSubpropMaker(sub);
        addPropertyMaker("border-separation", m);

        // border-start-precedence
        m  = new TableBorderPrecedence(PR_BORDER_START_PRECEDENCE);
        m.setInherited(false);
        m.addEnum("force", getEnumProperty(EN_FORCE, "FORCE"));
        addPropertyMaker("border-start-precedence", m);

        // caption-side
        m  = new EnumProperty.Maker(PR_CAPTION_SIDE);
        m.setInherited(true);
        m.addEnum("before", getEnumProperty(EN_BEFORE, "BEFORE"));
        m.addEnum("after", getEnumProperty(EN_AFTER, "AFTER"));
        m.addEnum("start", getEnumProperty(EN_START, "START"));
        m.addEnum("end", getEnumProperty(EN_END, "END"));
        m.addEnum("top", getEnumProperty(EN_TOP, "TOP"));
        m.addEnum("bottom", getEnumProperty(EN_BOTTOM, "BOTTOM"));
        m.addEnum("left", getEnumProperty(EN_LEFT, "LEFT"));
        m.addEnum("right", getEnumProperty(EN_RIGHT, "RIGHT"));
        m.setDefault("before");
        addPropertyMaker("caption-side", m);

        // column-number
        m  = new ColumnNumberPropertyMaker(PR_COLUMN_NUMBER);
        m.setInherited(false);
        addPropertyMaker("column-number", m);

        // column-width
        m  = new LengthProperty.Maker(PR_COLUMN_WIDTH);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.setDefault("auto");
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("column-width", m);

        // empty-cells
        m  = new EnumProperty.Maker(PR_EMPTY_CELLS);
        m.setInherited(true);
        m.addEnum("show", getEnumProperty(EN_SHOW, "SHOW"));
        m.addEnum("hide", getEnumProperty(EN_HIDE, "HIDE"));
        m.setDefault("show");
        addPropertyMaker("empty-cells", m);

        // ends-row
        m  = new EnumProperty.Maker(PR_ENDS_ROW);
        m.setInherited(false);
        m.useGeneric(genericBoolean);
        m.setDefault("false");
        addPropertyMaker("ends-row", m);

        // number-columns-repeated
        m  = new NumberProperty.PositiveIntegerMaker(PR_NUMBER_COLUMNS_REPEATED);
        m.setInherited(false);
        m.setDefault("1");
        addPropertyMaker("number-columns-repeated", m);

        // number-columns-spanned
        m  = new NumberProperty.PositiveIntegerMaker(PR_NUMBER_COLUMNS_SPANNED);
        m.setInherited(false);
        m.setDefault("1");
        addPropertyMaker("number-columns-spanned", m);

        // number-rows-spanned
        m  = new NumberProperty.PositiveIntegerMaker(PR_NUMBER_ROWS_SPANNED);
        m.setInherited(false);
        m.setDefault("1");
        addPropertyMaker("number-rows-spanned", m);

        // starts-row
        m  = new EnumProperty.Maker(PR_STARTS_ROW);
        m.useGeneric(genericBoolean);
        m.setInherited(false);
        m.setDefault("false");
        addPropertyMaker("starts-row", m);

        // table-layout
        m  = new EnumProperty.Maker(PR_TABLE_LAYOUT);
        m.setInherited(false);
        m.setDefault("auto");
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("fixed", getEnumProperty(EN_FIXED, "FIXED"));
        addPropertyMaker("table-layout", m);

        // table-omit-footer-at-break
        m  = new EnumProperty.Maker(PR_TABLE_OMIT_FOOTER_AT_BREAK);
        m.useGeneric(genericBoolean);
        m.setInherited(false);
        m.setDefault("false");
        addPropertyMaker("table-omit-footer-at-break", m);

        // table-omit-header-at-break
        m  = new EnumProperty.Maker(PR_TABLE_OMIT_HEADER_AT_BREAK);
        m.useGeneric(genericBoolean);
        m.setInherited(false);
        m.setDefault("false");
        addPropertyMaker("table-omit-header-at-break", m);
    }

    private void createWritingModeProperties() {
        PropertyMaker m;

        // direction
        m  = new EnumProperty.Maker(PR_DIRECTION);
        m.setInherited(true);
        m.addEnum("ltr", getEnumProperty(EN_LTR, "LTR"));
        m.addEnum("rtl", getEnumProperty(EN_RTL, "RTL"));
        m.setDefault("ltr");
        addPropertyMaker("direction", m);

        // glyph-orientation-horizontal
        m  = new ToBeImplementedProperty.Maker(PR_GLYPH_ORIENTATION_HORIZONTAL);
        m.setInherited(true);
        m.setDefault("0deg");
        addPropertyMaker("glyph-orientation-horizontal", m);

        // glyph-orientation-vertical
        m  = new ToBeImplementedProperty.Maker(PR_GLYPH_ORIENTATION_VERTICAL);
        m.setInherited(true);
        m.setDefault("auto");
        addPropertyMaker("glyph-orientation-vertical", m);

        // text-altitude
        m  = new LengthProperty.Maker(PR_TEXT_ALTITUDE);
        m.setInherited(false);
        m.addEnum("use-font-metrics", getEnumProperty(EN_USE_FONT_METRICS, "USE_FONT_METRICS"));
        m.setDefault("use-font-metrics");
        m.setPercentBase(LengthBase.FONTSIZE);
        addPropertyMaker("text-altitude", m);

        // text-depth
        m  = new LengthProperty.Maker(PR_TEXT_DEPTH);
        m.setInherited(false);
        m.addEnum("use-font-metrics", getEnumProperty(EN_USE_FONT_METRICS, "USE_FONT_METRICS"));
        m.setDefault("use-font-metrics");
        m.setPercentBase(LengthBase.FONTSIZE);
        addPropertyMaker("text-depth", m);

        // unicode-bidi
        m  = new EnumProperty.Maker(PR_UNICODE_BIDI);
        m.setInherited(false);
        m.addEnum("normal", getEnumProperty(EN_NORMAL, "NORMAL"));
        m.addEnum("embed", getEnumProperty(EN_EMBED, "EMBED"));
        m.addEnum("bidi-override", getEnumProperty(EN_BIDI_OVERRIDE, "BIDI_OVERRIDE"));
        m.setDefault("normal");
        addPropertyMaker("unicode-bidi", m);

        // writing-mode
        m  = new EnumProperty.Maker(PR_WRITING_MODE);
        m.setInherited(true);
        m.setDefault("lr-tb");
        m.addEnum("lr-tb", getEnumProperty(EN_LR_TB, "LR_TB"));
        m.addEnum("rl-tb", getEnumProperty(EN_RL_TB, "RL_TB"));
        m.addEnum("tb-rl", getEnumProperty(EN_TB_RL, "TB_RL"));
        m.addEnum("tb-lr", getEnumProperty(EN_TB_LR, "TB_LR"));
        m.addKeyword("lr", "lr-tb");
        m.addKeyword("rl", "rl-tb");
        m.addKeyword("tb", "tb-rl");
        addPropertyMaker("writing-mode", m);
    }

    private void createMiscProperties() {
        PropertyMaker m;

        // content-type
        m  = new StringProperty.Maker(PR_CONTENT_TYPE);
        m.setInherited(false);
        m.setDefault("auto");
        addPropertyMaker("content-type", m);

        // id
        m  = new StringProperty.Maker(PR_ID);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("id", m);

        // fox:alt-text, used for accessibility
        m = new StringProperty.Maker(PR_X_ALT_TEXT);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("fox:alt-text", m);


        // fox:border-*-radius-*

        // border-before-radius-start
        m  = new CondLengthProperty.Maker(PR_X_BORDER_BEFORE_RADIUS_START);
        m.useGeneric(genericCondCornerRadius);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");

        addPropertyMaker("fox:border-before-radius-start", m);

        // border-before-radius-end
        m  = new CondLengthProperty.Maker(PR_X_BORDER_BEFORE_RADIUS_END);
        m.useGeneric(genericCondCornerRadius);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        addPropertyMaker("fox:border-before-radius-end", m);

        // border-after-radius-start
        m  = new CondLengthProperty.Maker(PR_X_BORDER_AFTER_RADIUS_START);
        m.useGeneric(genericCondCornerRadius);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        addPropertyMaker("fox:border-after-radius-start", m);

        // border-after-radius-end
        m  = new CondLengthProperty.Maker(PR_X_BORDER_AFTER_RADIUS_END);
        m.useGeneric(genericCondCornerRadius);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        addPropertyMaker("fox:border-after-radius-end", m);

        // border-start-radius-before
        m  = new CondLengthProperty.Maker(PR_X_BORDER_START_RADIUS_START);
        m.useGeneric(genericCondCornerRadius);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        addPropertyMaker("fox:border-start-radius-before", m);

        // border-start-radius-after
        m  = new CondLengthProperty.Maker(PR_X_BORDER_START_RADIUS_END);
        m.useGeneric(genericCondCornerRadius);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        addPropertyMaker("fox:border-start-radius-after", m);

        // border-end-radius-before
        m  = new CondLengthProperty.Maker(PR_X_BORDER_END_RADIUS_START);
        m.useGeneric(genericCondCornerRadius);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        addPropertyMaker("fox:border-end-radius-before", m);

        // border-end-radius-after
        m  = new CondLengthProperty.Maker(PR_X_BORDER_END_RADIUS_END);
        m.useGeneric(genericCondCornerRadius);
        m.getSubpropMaker(CP_CONDITIONALITY).setDefault("discard");
        addPropertyMaker("fox:border-end-radius-after", m);


        // provisional-label-separation
        m  = new LengthProperty.Maker(PR_PROVISIONAL_LABEL_SEPARATION);
        m.setInherited(true);
        m.setDefault("6pt");
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("provisional-label-separation", m);

        // provisional-distance-between-starts
        m  = new LengthProperty.Maker(PR_PROVISIONAL_DISTANCE_BETWEEN_STARTS);
        m.setInherited(true);
        m.setDefault("24pt");
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("provisional-distance-between-starts", m);

        // ref-id
        m  = new StringProperty.Maker(PR_REF_ID);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("ref-id", m);

        // score-spaces
        m  = new EnumProperty.Maker(PR_SCORE_SPACES);
        m.useGeneric(genericBoolean);
        m.setInherited(true);
        m.setDefault("true");
        addPropertyMaker("score-spaces", m);

        // src
        m  = new URIProperty.Maker(PR_SRC);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("src", m);

        // visibility
        m  = new EnumProperty.Maker(PR_VISIBILITY);
        m.setInherited(false);
        m.addEnum("visible", getEnumProperty(EN_VISIBLE, "VISIBLE"));
        m.addEnum("hidden", getEnumProperty(EN_HIDDEN, "HIDDEN"));
        m.addEnum("collapse", getEnumProperty(EN_COLLAPSE, "COLLAPSE"));
        m.setDefault("visible");
        addPropertyMaker("visibility", m);

        // z-index
        m  = new NumberProperty.Maker(PR_Z_INDEX);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.setDefault("auto");
        addPropertyMaker("z-index", m);
    }

    private void createShorthandProperties() {                  // CSOK: MethodLength
        PropertyMaker m;

        // background
        m  = new ToBeImplementedProperty.Maker(PR_BACKGROUND);
        m.setInherited(false);
        m.setDefault("none");
        addPropertyMaker("background", m);

        // background-position
        m  = new BackgroundPositionShorthand.Maker(PR_BACKGROUND_POSITION);
        m.setInherited(false);
        m.addKeyword("left", "0pt 50%");
        m.addKeyword("left center", "0pt 50%");
        m.addKeyword("center left", "0pt 50%");
        m.addKeyword("right", "100% 50%");
        m.addKeyword("right center", "100% 50%");
        m.addKeyword("center right", "100% 50%");
        m.addKeyword("center", "50% 50%");
        m.addKeyword("center center", "50% 50%");
        m.addKeyword("top", "50% 0pt");
        m.addKeyword("top center", "50% 0pt");
        m.addKeyword("center top", "50% 0pt");
        m.addKeyword("bottom", "50% 100%");
        m.addKeyword("bottom center", "50% 100%");
        m.addKeyword("center bottom", "50% 100%");
        m.addKeyword("top left", "0pt 0pt");
        m.addKeyword("left top", "0pt 0pt");
        m.addKeyword("top right", "100% 0pt");
        m.addKeyword("right top", "100% 0pt");
        m.addKeyword("bottom left", "0pt 100%");
        m.addKeyword("left bottom", "0pt 100%");
        m.addKeyword("bottom right", "100% 100%");
        m.addKeyword("right bottom", "100% 100%");
        m.setDefault("0pt 0pt");
        m.setPercentBase(LengthBase.CUSTOM_BASE);
        m.setDatatypeParser(new BackgroundPositionShorthand.Parser());
        addPropertyMaker("background-position", m);

        // border
        m  = new ListProperty.Maker(PR_BORDER);
        m.setInherited(false);
        m.setDefault("");
        m.setDatatypeParser(new GenericShorthandParser());
        addPropertyMaker("border", m);

        // border-bottom
        m  = new ListProperty.Maker(PR_BORDER_BOTTOM);
        m.setInherited(false);
        m.setDefault("");
        m.setDatatypeParser(new GenericShorthandParser());
        addPropertyMaker("border-bottom", m);

        // border-color
        m  = new ListProperty.Maker(PR_BORDER_COLOR);
        m.setInherited(false);
        m.setDefault("");
        m.setDatatypeParser(new BoxPropShorthandParser());
        addPropertyMaker("border-color", m);

        // border-left
        m  = new ListProperty.Maker(PR_BORDER_LEFT);
        m.setInherited(false);
        m.setDefault("");
        m.setDatatypeParser(new GenericShorthandParser());
        addPropertyMaker("border-left", m);

        // border-right
        m  = new ListProperty.Maker(PR_BORDER_RIGHT);
        m.setInherited(false);
        m.setDefault("");
        m.setDatatypeParser(new GenericShorthandParser());
        addPropertyMaker("border-right", m);

        // border-style
        m  = new ListProperty.Maker(PR_BORDER_STYLE);
        m.setInherited(false);
        m.setDefault("");
        m.setDatatypeParser(new BoxPropShorthandParser());
        addPropertyMaker("border-style", m);

        // border-spacing
        m  = new ListProperty.Maker(PR_BORDER_SPACING);
        m.setInherited(true);
        m.setDefault("0pt");
        m.setDatatypeParser(new BorderSpacingShorthandParser());
        addPropertyMaker("border-spacing", m);

        // border-top
        m  = new ListProperty.Maker(PR_BORDER_TOP);
        m.setInherited(false);
        m.setDefault("");
        m.setDatatypeParser(new GenericShorthandParser());
        addPropertyMaker("border-top", m);

        // border-width
        m  = new ListProperty.Maker(PR_BORDER_WIDTH);
        m.setInherited(false);
        m.setDefault("");
        m.setDatatypeParser(new BoxPropShorthandParser());
        addPropertyMaker("border-width", m);

        // fox:border-radius
        m  = new ListProperty.Maker(PR_X_BORDER_RADIUS);
        m.setInherited(false);
        m.setDatatypeParser(new BoxCornerPropShorthandParser());
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("fox:border-radius", m);

        // cue
        m  = new ToBeImplementedProperty.Maker(PR_CUE);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("cue", m);

        // font
        m  = new FontShorthandProperty.Maker(PR_FONT);
        m.setInherited(true);
        m.addEnum("caption", getEnumProperty(EN_CAPTION, "CAPTION"));
        m.addEnum("icon", getEnumProperty(EN_ICON, "ICON"));
        m.addEnum("message-box", getEnumProperty(EN_MESSAGE_BOX, "MESSAGE_BOX"));
        m.addEnum("menu", getEnumProperty(EN_MENU, "MENU"));
        m.addEnum("small-caption", getEnumProperty(EN_SMALL_CAPTION, "SMALL_CAPTION"));
        m.addEnum("status-bar", getEnumProperty(EN_STATUS_BAR, "STATUS_BAR"));
        m.setDefault("");
        m.setDatatypeParser(new FontShorthandParser());
        addPropertyMaker("font", m);

        // margin
        m  = new ListProperty.Maker(PR_MARGIN);
        m.setInherited(false);
        m.setDefault("");
        m.setDatatypeParser(new BoxPropShorthandParser());
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("margin", m);

        // padding
        m  = new ListProperty.Maker(PR_PADDING);
        m.setInherited(false);
        m.setDatatypeParser(new BoxPropShorthandParser());
        m.setPercentBase(LengthBase.CONTAINING_BLOCK_WIDTH);
        addPropertyMaker("padding", m);

        // page-break-after
        m  = new EnumProperty.Maker(PR_PAGE_BREAK_AFTER);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("always", getEnumProperty(EN_ALWAYS, "ALWAYS"));
        m.addEnum("avoid", getEnumProperty(EN_AVOID, "AVOID"));
        m.addEnum("left", getEnumProperty(EN_LEFT, "LEFT"));
        m.addEnum("right", getEnumProperty(EN_RIGHT, "RIGHT"));
        m.setDefault("auto");
        m.setDatatypeParser(new PageBreakShorthandParser());
        addPropertyMaker("page-break-after", m);

        // page-break-before
        m  = new EnumProperty.Maker(PR_PAGE_BREAK_BEFORE);
        m.setInherited(false);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("always", getEnumProperty(EN_ALWAYS, "ALWAYS"));
        m.addEnum("avoid", getEnumProperty(EN_AVOID, "AVOID"));
        m.addEnum("left", getEnumProperty(EN_LEFT, "LEFT"));
        m.addEnum("right", getEnumProperty(EN_RIGHT, "RIGHT"));
        m.setDefault("auto");
        m.setDatatypeParser(new PageBreakShorthandParser());
        addPropertyMaker("page-break-before", m);

        // page-break-inside
        m  = new EnumProperty.Maker(PR_PAGE_BREAK_INSIDE);
        m.setInherited(true);
        m.addEnum("auto", getEnumProperty(EN_AUTO, "AUTO"));
        m.addEnum("avoid", getEnumProperty(EN_AVOID, "AVOID"));
        m.setDefault("auto");
        m.setDatatypeParser(new PageBreakShorthandParser());
        addPropertyMaker("page-break-inside", m);

        // pause
        m  = new ToBeImplementedProperty.Maker(PR_PAUSE);
        m.setInherited(false);
        m.setDefault("");
        addPropertyMaker("pause", m);

        // position
        m  = new EnumProperty.Maker(PR_POSITION);
        m.setInherited(false);
        m.addEnum("static", getEnumProperty(EN_STATIC, "STATIC"));
        m.addEnum("relative", getEnumProperty(EN_RELATIVE, "RELATIVE"));
        m.addEnum("absolute", getEnumProperty(EN_ABSOLUTE, "ABSOLUTE"));
        m.addEnum("fixed", getEnumProperty(EN_FIXED, "FIXED"));
        m.setDefault("static");
        m.setDatatypeParser(new PositionShorthandParser());
        addPropertyMaker("position", m);

        // size
        m  = new ToBeImplementedProperty.Maker(PR_SIZE);
        m.setInherited(false);
        m.setDefault("auto");
        addPropertyMaker("size", m);

        // vertical-align
        m  = new LengthProperty.Maker(PR_VERTICAL_ALIGN);
        m.setInherited(false);
        m.addEnum("baseline", getEnumProperty(EN_BASELINE, "BASELINE"));
        m.addEnum("middle", getEnumProperty(EN_MIDDLE, "MIDDLE"));
        m.addEnum("sub", getEnumProperty(EN_SUB, "SUB"));
        m.addEnum("super", getEnumProperty(EN_SUPER, "SUPER"));
        m.addEnum("text-top", getEnumProperty(EN_TEXT_TOP, "TEXT_TOP"));
        m.addEnum("text-bottom", getEnumProperty(EN_TEXT_BOTTOM, "TEXT_BOTTOM"));
        m.addEnum("top", getEnumProperty(EN_TOP, "TOP"));
        m.addEnum("bottom", getEnumProperty(EN_BOTTOM, "BOTTOM"));
        m.setDatatypeParser(new VerticalAlignShorthandParser());
        m.setDefault("baseline");
        m.setPercentBase(LengthBase.ALIGNMENT_ADJUST);
        addPropertyMaker("vertical-align", m);

        // white-space
        m = new EnumProperty.Maker(PR_WHITE_SPACE);
        m.setInherited(true);
        m.addEnum("normal", getEnumProperty(EN_NORMAL, "NORMAL"));
        m.addEnum("pre", getEnumProperty(EN_PRE, "PRE"));
        m.addEnum("nowrap", getEnumProperty(EN_NO_WRAP, "NO_WRAP"));
        m.setDefault("normal");
        m.setDatatypeParser(new WhiteSpaceShorthandParser());
        addPropertyMaker("white-space", m);

        // xml:lang
        m  = new StringProperty.Maker(PR_XML_LANG);
        m.setInherited(true);
        m.setDefault("");
        m.setDatatypeParser(new XMLLangShorthandParser());
        addPropertyMaker("xml:lang", m);

        // xml:base
        m  = new URIProperty.Maker(PR_X_XML_BASE);
        m.setInherited(true);
        m.setDefault("");
        addPropertyMaker("xml:base", m);

       }

}
