/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

/* $Id$ */


package org.apache.fop.fo;

public interface Constants {

    /* These constants are used by apps.CommandLineOptions and
       apps.Fop to describe the input (either .FO or .XML/.XSL)
       and desired output (PDF, PS, AWT, etc.) of the document */
       
    /** render constants for bounds checking */
    int RENDER_MIN_CONST = 1;
    int RENDER_MAX_CONST = 10;
    /** input / output not set */
    int NOT_SET = 0;
    /** input: fo file */
    int FO_INPUT = 1;
    /** input: xml+xsl file */
    int XSLT_INPUT = 2;
    /** output: pdf file */
    int RENDER_PDF = 1;
    /** output: screen using swing */
    int RENDER_AWT = 2;
    /** output: mif file */
    int RENDER_MIF = 3;
    /** output: sent swing rendered file to printer */
    int RENDER_PRINT = 4;
    /** output: pcl file */
    int RENDER_PCL = 5;
    /** output: postscript file */
    int RENDER_PS = 6;
    /** output: text file */
    int RENDER_TXT = 7;
    /** output: svg file */
    int RENDER_SVG = 8;
    /** output: XML area tree */
    int RENDER_XML = 9;
    /** output: RTF file */
    int RENDER_RTF = 10;
    
    // element constants
    int FO_UNKNOWN_NODE = 0;  // FObj base class
    int FO_BASIC_LINK = 1;
    int FO_BIDI_OVERRIDE = 2;
    int FO_BLOCK = 3;
    int FO_BLOCK_CONTAINER = 4;
    int FO_CHARACTER = 5;
    int FO_COLOR_PROFILE = 6;
    int FO_CONDITIONAL_PAGE_MASTER_REFERENCE = 7;
    int FO_DECLARATIONS = 8;
    int FO_EXTERNAL_GRAPHIC = 9;
    int FO_FLOAT = 10;
    int FO_FLOW = 11;
    int FO_FOOTNOTE = 12;
    int FO_FOOTNOTE_BODY = 13;
    int FO_INITIAL_PROPERTY_SET = 14;
    int FO_INLINE = 15;
    int FO_INLINE_CONTAINER = 16;
    int FO_INSTREAM_FOREIGN_OBJECT = 17;
    int FO_LAYOUT_MASTER_SET = 18;
    int FO_LEADER = 19;
    int FO_LIST_BLOCK = 20;
    int FO_LIST_ITEM = 21;
    int FO_LIST_ITEM_BODY = 22;
    int FO_LIST_ITEM_LABEL = 23;
    int FO_MARKER = 24;
    int FO_MULTI_CASE = 25;
    int FO_MULTI_PROPERTIES = 26;
    int FO_MULTI_PROPERTY_SET = 27;
    int FO_MULTI_SWITCH = 28;
    int FO_MULTI_TOGGLE = 29;
    int FO_PAGE_NUMBER = 30;
    int FO_PAGE_NUMBER_CITATION = 31;
    int FO_PAGE_SEQUENCE = 32;
    int FO_PAGE_SEQUENCE_MASTER = 33;
    int FO_REGION_AFTER = 34;
    int FO_REGION_BEFORE = 35;
    int FO_REGION_BODY = 36;
    int FO_REGION_END = 37;
    int FO_REGION_START = 38;
    int FO_REPEATABLE_PAGE_MASTER_ALTERNATIVES = 39;
    int FO_REPEATABLE_PAGE_MASTER_REFERENCE = 40;
    int FO_RETRIEVE_MARKER = 41;
    int FO_ROOT = 42;
    int FO_SIMPLE_PAGE_MASTER = 43;
    int FO_SINGLE_PAGE_MASTER_REFERENCE = 44;
    int FO_STATIC_CONTENT = 45;
    int FO_TABLE = 46;
    int FO_TABLE_AND_CAPTION = 47;
    int FO_TABLE_BODY = 48;
    int FO_TABLE_CAPTION = 49;
    int FO_TABLE_CELL = 50;
    int FO_TABLE_COLUMN = 51;
    int FO_TABLE_FOOTER = 52;
    int FO_TABLE_HEADER = 53;
    int FO_TABLE_ROW = 54;
    int FO_TITLE = 55;
    int FO_WRAPPER = 56;
    int ELEMENT_COUNT = 56;
    
    // Masks
    int COMPOUND_SHIFT = 9;
    int PROPERTY_MASK = (1 << COMPOUND_SHIFT)-1;
    int COMPOUND_MASK = ~PROPERTY_MASK;
    int COMPOUND_COUNT = 11;
    
    // property constants

    int PR_ABSOLUTE_POSITION = 1;
    int PR_ACTIVE_STATE = 2;
    int PR_ALIGNMENT_ADJUST = 3;
    int PR_ALIGNMENT_BASELINE = 4;
    int PR_AUTO_RESTORE = 5;
    int PR_AZIMUTH = 6;
    int PR_BACKGROUND = 7;
    int PR_BACKGROUND_ATTACHMENT = 8;
    int PR_BACKGROUND_COLOR = 9;
    int PR_BACKGROUND_IMAGE = 10;
    int PR_BACKGROUND_POSITION = 11;
    int PR_BACKGROUND_POSITION_HORIZONTAL = 12;
    int PR_BACKGROUND_POSITION_VERTICAL = 13;
    int PR_BACKGROUND_REPEAT = 14;
    int PR_BASELINE_SHIFT = 15;
    int PR_BLANK_OR_NOT_BLANK = 16;
    int PR_BLOCK_PROGRESSION_DIMENSION = 17;
    int PR_BORDER = 18;
    int PR_BORDER_AFTER_COLOR = 19;
    int PR_BORDER_AFTER_PRECEDENCE = 20;
    int PR_BORDER_AFTER_STYLE = 21;
    int PR_BORDER_AFTER_WIDTH = 22;
    int PR_BORDER_BEFORE_COLOR = 23;
    int PR_BORDER_BEFORE_PRECEDENCE = 24;
    int PR_BORDER_BEFORE_STYLE = 25;
    int PR_BORDER_BEFORE_WIDTH = 26;
    int PR_BORDER_BOTTOM = 27;
    int PR_BORDER_BOTTOM_COLOR = 28;
    int PR_BORDER_BOTTOM_STYLE = 29;
    int PR_BORDER_BOTTOM_WIDTH = 30;
    int PR_BORDER_COLLAPSE = 31;
    int PR_BORDER_COLOR = 32;
    int PR_BORDER_END_COLOR = 33;
    int PR_BORDER_END_PRECEDENCE = 34;
    int PR_BORDER_END_STYLE = 35;
    int PR_BORDER_END_WIDTH = 36;
    int PR_BORDER_LEFT = 37;
    int PR_BORDER_LEFT_COLOR = 38;
    int PR_BORDER_LEFT_STYLE = 39;
    int PR_BORDER_LEFT_WIDTH = 40;
    int PR_BORDER_RIGHT = 41;
    int PR_BORDER_RIGHT_COLOR = 42;
    int PR_BORDER_RIGHT_STYLE = 43;
    int PR_BORDER_RIGHT_WIDTH = 44;
    int PR_BORDER_SEPARATION = 45;
    int PR_BORDER_SPACING = 46;
    int PR_BORDER_START_COLOR = 47;
    int PR_BORDER_START_PRECEDENCE = 48;
    int PR_BORDER_START_STYLE = 49;
    int PR_BORDER_START_WIDTH = 50;
    int PR_BORDER_STYLE = 51;
    int PR_BORDER_TOP = 52;
    int PR_BORDER_TOP_COLOR = 53;
    int PR_BORDER_TOP_STYLE = 54;
    int PR_BORDER_TOP_WIDTH = 55;
    int PR_BORDER_WIDTH = 56;
    int PR_BOTTOM = 57;
    int PR_BREAK_AFTER = 58;
    int PR_BREAK_BEFORE = 59;
    int PR_CAPTION_SIDE = 60;
    int PR_CASE_NAME = 61;
    int PR_CASE_TITLE = 62;
    int PR_CHARACTER = 63;
    int PR_CLEAR = 64;
    int PR_CLIP = 65;
    int PR_COLOR = 66;
    int PR_COLOR_PROFILE_NAME = 67;
    int PR_COLUMN_COUNT = 68;
    int PR_COLUMN_GAP = 69;
    int PR_COLUMN_NUMBER = 70;
    int PR_COLUMN_WIDTH = 71;
    int PR_CONTENT_HEIGHT = 72;
    int PR_CONTENT_TYPE = 73;
    int PR_CONTENT_WIDTH = 74;
    int PR_COUNTRY = 75;
    int PR_CUE = 76;
    int PR_CUE_AFTER = 77;
    int PR_CUE_BEFORE = 78;
    int PR_DESTINATION_PLACEMENT_OFFSET = 79;
    int PR_DIRECTION = 80;
    int PR_DISPLAY_ALIGN = 81;
    int PR_DOMINANT_BASELINE = 82;
    int PR_ELEVATION = 83;
    int PR_EMPTY_CELLS = 84;
    int PR_END_INDENT = 85;
    int PR_ENDS_ROW = 86;
    int PR_EXTENT = 87;
    int PR_EXTERNAL_DESTINATION = 88;
    int PR_FLOAT = 89;
    int PR_FLOW_NAME = 90;
    int PR_FONT = 91;
    int PR_FONT_FAMILY = 92;
    int PR_FONT_SELECTION_STRATEGY = 93;
    int PR_FONT_SIZE = 94;
    int PR_FONT_SIZE_ADJUST = 95;
    int PR_FONT_STRETCH = 96;
    int PR_FONT_STYLE = 97;
    int PR_FONT_VARIANT = 98;
    int PR_FONT_WEIGHT = 99;
    int PR_FORCE_PAGE_COUNT = 100;
    int PR_FORMAT = 101;
    int PR_GLYPH_ORIENTATION_HORIZONTAL = 102;
    int PR_GLYPH_ORIENTATION_VERTICAL = 103;
    int PR_GROUPING_SEPARATOR = 104;
    int PR_GROUPING_SIZE = 105;
    int PR_HEIGHT = 106;
    int PR_HYPHENATE = 107;
    int PR_HYPHENATION_CHARACTER = 108;
    int PR_HYPHENATION_KEEP = 109;
    int PR_HYPHENATION_LADDER_COUNT = 110;
    int PR_HYPHENATION_PUSH_CHARACTER_COUNT = 111;
    int PR_HYPHENATION_REMAIN_CHARACTER_COUNT = 112;
    int PR_ID = 113;
    int PR_INDICATE_DESTINATION = 114;
    int PR_INITIAL_PAGE_NUMBER = 115;
    int PR_INLINE_PROGRESSION_DIMENSION = 116;
    int PR_INTERNAL_DESTINATION = 117;
    int PR_KEEP_TOGETHER = 118;
    int PR_KEEP_WITH_NEXT = 119;
    int PR_KEEP_WITH_PREVIOUS = 120;
    int PR_LANGUAGE = 121;
    int PR_LAST_LINE_END_INDENT = 122;
    int PR_LEADER_ALIGNMENT = 123;
    int PR_LEADER_LENGTH = 124;
    int PR_LEADER_PATTERN = 125;
    int PR_LEADER_PATTERN_WIDTH = 126;
    int PR_LEFT = 127;
    int PR_LETTER_SPACING = 128;
    int PR_LETTER_VALUE = 129;
    int PR_LINEFEED_TREATMENT = 130;
    int PR_LINE_HEIGHT = 131;
    int PR_LINE_HEIGHT_SHIFT_ADJUSTMENT = 132;
    int PR_LINE_STACKING_STRATEGY = 133;
    int PR_MARGIN = 134;
    int PR_MARGIN_BOTTOM = 135;
    int PR_MARGIN_LEFT = 136;
    int PR_MARGIN_RIGHT = 137;
    int PR_MARGIN_TOP = 138;
    int PR_MARKER_CLASS_NAME = 139;
    int PR_MASTER_NAME = 140;
    int PR_MASTER_REFERENCE = 141;
    int PR_MAX_HEIGHT = 142;
    int PR_MAXIMUM_REPEATS = 143;
    int PR_MAX_WIDTH = 144;
    int PR_MEDIA_USAGE = 145;
    int PR_MIN_HEIGHT = 146;
    int PR_MIN_WIDTH = 147;
    int PR_NUMBER_COLUMNS_REPEATED = 148;
    int PR_NUMBER_COLUMNS_SPANNED = 149;
    int PR_NUMBER_ROWS_SPANNED = 150;
    int PR_ODD_OR_EVEN = 151;
    int PR_ORPHANS = 152;
    int PR_OVERFLOW = 153;
    int PR_PADDING = 154;
    int PR_PADDING_AFTER = 155;
    int PR_PADDING_BEFORE = 156;
    int PR_PADDING_BOTTOM = 157;
    int PR_PADDING_END = 158;
    int PR_PADDING_LEFT = 159;
    int PR_PADDING_RIGHT = 160;
    int PR_PADDING_START = 161;
    int PR_PADDING_TOP = 162;
    int PR_PAGE_BREAK_AFTER = 163;
    int PR_PAGE_BREAK_BEFORE = 164;
    int PR_PAGE_BREAK_INSIDE = 165;
    int PR_PAGE_HEIGHT = 166;
    int PR_PAGE_POSITION = 167;
    int PR_PAGE_WIDTH = 168;
    int PR_PAUSE = 169;
    int PR_PAUSE_AFTER = 170;
    int PR_PAUSE_BEFORE = 171;
    int PR_PITCH = 172;
    int PR_PITCH_RANGE = 173;
    int PR_PLAY_DURING = 174;
    int PR_POSITION = 175;
    int PR_PRECEDENCE = 176;
    int PR_PROVISIONAL_DISTANCE_BETWEEN_STARTS = 177;
    int PR_PROVISIONAL_LABEL_SEPARATION = 178;
    int PR_REFERENCE_ORIENTATION = 179;
    int PR_REF_ID = 180;
    int PR_REGION_NAME = 181;
    int PR_RELATIVE_ALIGN = 182;
    int PR_RELATIVE_POSITION = 183;
    int PR_RENDERING_INTENT = 184;
    int PR_RETRIEVE_BOUNDARY = 185;
    int PR_RETRIEVE_CLASS_NAME = 186;
    int PR_RETRIEVE_POSITION = 187;
    int PR_RICHNESS = 188;
    int PR_RIGHT = 189;
    int PR_ROLE = 190;
    int PR_RULE_STYLE = 191;
    int PR_RULE_THICKNESS = 192;
    int PR_SCALING = 193;
    int PR_SCALING_METHOD = 194;
    int PR_SCORE_SPACES = 195;
    int PR_SCRIPT = 196;
    int PR_SHOW_DESTINATION = 197;
    int PR_SIZE = 198;
    int PR_SOURCE_DOCUMENT = 199;
    int PR_SPACE_AFTER = 200;
    int PR_SPACE_BEFORE = 201;
    int PR_SPACE_END = 202;
    int PR_SPACE_START = 203;
    int PR_SPACE_TREATMENT = 204;
    int PR_SPAN = 205;
    int PR_SPEAK = 206;
    int PR_SPEAK_HEADER = 207;
    int PR_SPEAK_NUMERAL = 208;
    int PR_SPEAK_PUNCTUATION = 209;
    int PR_SPEECH_RATE = 210;
    int PR_SRC = 211;
    int PR_START_INDENT = 212;
    int PR_STARTING_STATE = 213;
    int PR_STARTS_ROW = 214;
    int PR_STRESS = 215;
    int PR_SUPPRESS_AT_LINE_BREAK = 216;
    int PR_SWITCH_TO = 217;
    int PR_TABLE_LAYOUT = 218;
    int PR_TABLE_OMIT_FOOTER_AT_BREAK = 219;
    int PR_TABLE_OMIT_HEADER_AT_BREAK = 220;
    int PR_TARGET_PRESENTATION_CONTEXT = 221;
    int PR_TARGET_PROCESSING_CONTEXT = 222;
    int PR_TARGET_STYLESHEET = 223;
    int PR_TEXT_ALIGN = 224;
    int PR_TEXT_ALIGN_LAST = 225;
    int PR_TEXT_ALTITUDE = 226;
    int PR_TEXT_DECORATION = 227;
    int PR_TEXT_DEPTH = 228;
    int PR_TEXT_INDENT = 229;
    int PR_TEXT_SHADOW = 230;
    int PR_TEXT_TRANSFORM = 231;
    int PR_TOP = 232;
    int PR_TREAT_AS_WORD_SPACE = 233;
    int PR_UNICODE_BIDI = 234;
    int PR_VERTICAL_ALIGN = 235;
    int PR_VISIBILITY = 236;
    int PR_VOICE_FAMILY = 237;
    int PR_VOLUME = 238;
    int PR_WHITE_SPACE_COLLAPSE = 239;
    int PR_WHITE_SPACE_TREATMENT = 240;
    int PR_WIDOWS = 241;
    int PR_WIDTH = 242;
    int PR_WORD_SPACING = 243;
    int PR_WRAP_OPTION = 244;
    int PR_WRITING_MODE = 245;
    int PR_XML_LANG = 246;
    int PR_Z_INDEX = 247;
    int PROPERTY_COUNT = 247;

    // compound property constants

    int CP_BLOCK_PROGRESSION_DIRECTION = 1 << COMPOUND_SHIFT;
    int CP_CONDITIONALITY = 2 << COMPOUND_SHIFT;
    int CP_INLINE_PROGRESSION_DIRECTION = 3 << COMPOUND_SHIFT;
    int CP_LENGTH = 4 << COMPOUND_SHIFT;
    int CP_MAXIMUM = 5 << COMPOUND_SHIFT;
    int CP_MINIMUM = 6 << COMPOUND_SHIFT;
    int CP_OPTIMUM = 7 << COMPOUND_SHIFT;
    int CP_PRECEDENCE = 8 << COMPOUND_SHIFT;
    int CP_WITHIN_COLUMN = 9 << COMPOUND_SHIFT;
    int CP_WITHIN_LINE = 10 << COMPOUND_SHIFT;
    int CP_WITHIN_PAGE = 11 << COMPOUND_SHIFT;

    // Enumeration constants

    int ABSOLUTE = 1;
    int ABSOLUTE_COLORMETRIC = 2;
    int AFTER = 3;
    int ALL = 4;
    int ALPHABETIC = 5;
    int ALWAYS = 6;
    int ANY = 7;
    int AUTO = 8;
    int BASELINE = 9;
    int BEFORE = 10;
    int BLANK = 11;
    int BLINK = 12;
    int BOTTOM = 13;
    int CAPITALIZE = 14;
    int CENTER = 15;
    int COLLAPSE = 16;
    int COLUMN = 17;
    int DASHED = 18;
    int DISCARD = 19;
    int DOCUMENT = 20;
    int DOTS = 21;
    int DOTTED = 22;
    int DOUBLE = 23;
    int END = 24;
    int END_ON_EVEN = 25;
    int END_ON_ODD = 26;
    int ERROR_IF_OVERFLOW = 27;
    int EVEN = 28;
    int EVEN_PAGE = 29;
    int FALSE = 30;
    int FIC = 31;
    int FIRST = 32;
    int FIXED = 33;
    int FORCE = 34;
    int FSWP = 35;
    int GROOVE = 36;
    int HIDDEN = 37;
    int IGNORE = 38;
    int IGNORE_IF_AFTER_LINEFEED = 39;
    int IGNORE_IF_BEFORE_LINEFEED = 40;
    int IGNORE_IF_SURROUNDING_LINEFEED = 41;
    int INSET = 42;
    int JUSTIFY = 43;
    int LAST = 44;
    int LEFT = 45;
    int LEWP = 46;
    int LINE_THROUGH = 47;
    int LOWERCASE = 48;
    int LR_TB = 49;
    int LSWP = 50;
    int MIDDLE = 51;
    int NO_BLINK = 52;
    int NO_FORCE = 53;
    int NO_LINE_THROUGH = 54;
    int NO_OVERLINE = 55;
    int NO_UNDERLINE = 56;
    int NO_WRAP = 57;
    int NON_UNIFORM = 58;
    int NONE = 59;
    int NOREPEAT = 60;
    int NORMAL = 61;
    int NOT_BLANK = 62;
    int ODD = 63;
    int ODD_PAGE = 64;
    int OUTSET = 65;
    int OVERLINE = 66;
    int PAGE = 67;
    int PAGE_SEQUENCE = 68;
    int PERCEPTUAL = 69;
    int PRESERVE = 70;
    int REFERENCE_AREA = 71;
    int RELATIVE = 72;
    int RELATIVE_COLOMETRIC = 73;
    int REPEAT = 74;
    int REPEATX = 75;
    int REPEATY = 76;
    int REST = 77;
    int RETAIN = 78;
    int RIDGE = 79;
    int RIGHT = 80;
    int RL_TB = 81;
    int RULE = 82;
    int SATURATION = 83;
    int SCROLL = 84;
    int SEPARATE = 85;
    int SMALL_CAPS = 86;
    int SOLID = 87;
    int SPACE = 88;
    int START = 89;
    int STATIC = 90;
    int SUB = 91;
    int SUPER = 92;
    int TB_RL = 93;
    int TEXT_BOTTOM = 94;
    int TEXT_TOP = 95;
    int TOP = 96;
    int TRADITIONAL = 97;
    int TREAT_AS_SPACE = 98;
    int TREAT_AS_ZERO_WIDTH_SPACE = 99;
    int TRUE = 100;
    int UNDERLINE = 101;
    int UNIFORM = 102;
    int UPPERCASE = 103;
    int USECONTENT = 104;
    int VISIBLE = 105;
    int WRAP = 106;
    int ENUM_COUNT = 106;

   // Enumeration Interfaces
   
    public interface GenericBooleanInterface {
        int TRUE =  Constants.TRUE;
        int FALSE =  Constants.FALSE;
    }
     
    public interface GenericBorderStyleInterface {
        int NONE =  Constants.NONE;
        int HIDDEN =  Constants.HIDDEN;
        int DOTTED =  Constants.DOTTED;
        int DASHED =  Constants.DASHED;
        int SOLID =  Constants.SOLID;
        int DOUBLE =  Constants.DOUBLE;
        int GROOVE =  Constants.GROOVE;
        int RIDGE =  Constants.RIDGE;
        int INSET =  Constants.INSET;
        int OUTSET =  Constants.OUTSET;
    }
    
    public interface GenericBreakInterface {
        int AUTO =  Constants.AUTO;
        int COLUMN =  Constants.COLUMN;
        int PAGE =  Constants.PAGE;
        int EVEN_PAGE =  Constants.EVEN_PAGE;
        int ODD_PAGE =  Constants.ODD_PAGE;
    }
    
    public interface GenericCondBorderWidthInterface {
        public interface Conditionality {
            int DISCARD = Constants.DISCARD;
            int RETAIN = Constants.RETAIN;
        }
    }
    
    public interface GenericCondPaddingInterface {
        public interface Conditionality {
            int DISCARD = Constants.DISCARD;
            int RETAIN = Constants.RETAIN;
        }
    }
        
    public interface GenericKeepInterface {
        public interface WithinPage {
            int AUTO = Constants.AUTO;
            int ALWAYS = Constants.ALWAYS;
        }
        public interface WithinLine {
            int AUTO = Constants.AUTO;
            int ALWAYS = Constants.ALWAYS;
        }
        public interface WithinColumn {
            int AUTO = Constants.AUTO;
            int ALWAYS = Constants.ALWAYS;
        }
    }
    
    public interface GenericSpaceInterface {
        public interface Precedence {
            int FORCE = Constants.FORCE;
        }
        public interface Conditionality {
            int DISCARD = Constants.DISCARD;
            int RETAIN = Constants.RETAIN;
        }
    }
   

    public interface AbsolutePosition {
        int AUTO = Constants.AUTO;
        int FIXED = Constants.FIXED;
        int ABSOLUTE = Constants.ABSOLUTE; }

    public interface BackgroundRepeat {
        int REPEAT = Constants.REPEAT;
        int REPEATX = Constants.REPEATX;
        int REPEATY = Constants.REPEATY;
        int NOREPEAT = Constants.NOREPEAT; }

    public interface BaselineShift {
        int BASELINE = Constants.BASELINE;
        int SUB = Constants.SUB;
        int SUPER = Constants.SUPER; }

    public interface BlankOrNotBlank {
        int BLANK = Constants.BLANK;
        int NOT_BLANK = Constants.NOT_BLANK;
        int ANY = Constants.ANY; }

    public interface BorderAfterStyle extends GenericBorderStyleInterface { }

    public interface BorderAfterWidth extends GenericCondBorderWidthInterface { }

    public interface BorderBeforeStyle extends GenericBorderStyleInterface { }

    public interface BorderBeforeWidth extends GenericCondBorderWidthInterface { }

    public interface BorderBottomStyle extends GenericBorderStyleInterface { }

    public interface BorderCollapse {
        int SEPARATE = Constants.SEPARATE;
        int COLLAPSE = Constants.COLLAPSE; }

    public interface BorderEndStyle extends GenericBorderStyleInterface { }

    public interface BorderEndWidth extends GenericCondBorderWidthInterface { }

    public interface BorderLeftStyle extends GenericBorderStyleInterface { }

    public interface BorderRightStyle extends GenericBorderStyleInterface { }

    public interface BorderStartStyle extends GenericBorderStyleInterface { }

    public interface BorderStartWidth extends GenericCondBorderWidthInterface { }

    public interface BorderTopStyle extends GenericBorderStyleInterface { }

    public interface BreakAfter extends GenericBreakInterface { }

    public interface BreakBefore extends GenericBreakInterface { }

    public interface CaptionSide {
        int BEFORE = Constants.BEFORE;
        int AFTER = Constants.AFTER;
        int START = Constants.START;
        int END = Constants.END;
        int TOP = Constants.TOP;
        int BOTTOM = Constants.BOTTOM;
        int LEFT = Constants.LEFT;
        int RIGHT = Constants.RIGHT; }

    public interface DisplayAlign {
        int BEFORE = Constants.BEFORE;
        int AFTER = Constants.AFTER;
        int CENTER = Constants.CENTER;
        int AUTO = Constants.AUTO; }

    public interface FontVariant {
        int NORMAL = Constants.NORMAL;
        int SMALL_CAPS = Constants.SMALL_CAPS; }

    public interface ForcePageCount {
        int EVEN = Constants.EVEN;
        int ODD = Constants.ODD;
        int END_ON_EVEN = Constants.END_ON_EVEN;
        int END_ON_ODD = Constants.END_ON_ODD;
        int NO_FORCE = Constants.NO_FORCE;
        int AUTO = Constants.AUTO; }

    public interface Hyphenate {
        int TRUE = Constants.TRUE;
        int FALSE = Constants.FALSE; }

    public interface KeepTogether extends GenericKeepInterface { }

    public interface KeepWithNext extends GenericKeepInterface { }

    public interface KeepWithPrevious extends GenericKeepInterface { }

    public interface LeaderAlignment {
        int NONE = Constants.NONE;
        int REFERENCE_AREA = Constants.REFERENCE_AREA;
        int PAGE = Constants.PAGE; }

    public interface LeaderPattern {
        int SPACE = Constants.SPACE;
        int RULE = Constants.RULE;
        int DOTS = Constants.DOTS;
        int USECONTENT = Constants.USECONTENT; }

    public interface LetterValue {
        int ALPHABETIC = Constants.ALPHABETIC;
        int TRADITIONAL = Constants.TRADITIONAL;
        int AUTO = Constants.AUTO; }

    public interface LinefeedTreatment {
        int IGNORE = Constants.IGNORE;
        int PRESERVE = Constants.PRESERVE;
        int TREAT_AS_SPACE = Constants.TREAT_AS_SPACE;
        int TREAT_AS_ZERO_WIDTH_SPACE = Constants.TREAT_AS_ZERO_WIDTH_SPACE; }

    public interface OddOrEven {
        int ODD = Constants.ODD;
        int EVEN = Constants.EVEN;
        int ANY = Constants.ANY; }

    public interface Overflow {
        int VISIBLE = Constants.VISIBLE;
        int HIDDEN = Constants.HIDDEN;
        int SCROLL = Constants.SCROLL;
        int ERROR_IF_OVERFLOW = Constants.ERROR_IF_OVERFLOW;
        int AUTO = Constants.AUTO; }

    public interface PaddingAfter extends GenericCondPaddingInterface { }

    public interface PaddingBefore extends GenericCondPaddingInterface { }

    public interface PaddingEnd extends GenericCondPaddingInterface { }

    public interface PaddingStart extends GenericCondPaddingInterface { }

    public interface PagePosition {
        int FIRST = Constants.FIRST;
        int LAST = Constants.LAST;
        int REST = Constants.REST;
        int ANY = Constants.ANY; }

/*    public interface Position {   conflicts with layoutmgr.Position
        int STATIC = Constants.STATIC;
        int RELATIVE = Constants.RELATIVE;
        int ABSOLUTE = Constants.ABSOLUTE;
        int FIXED = Constants.FIXED; }  */

    public interface Precedence {
        int TRUE = Constants.TRUE;
        int FALSE = Constants.FALSE; }

    public interface RelativeAlign {
        int BEFORE = Constants.BEFORE;
        int BASELINE = Constants.BASELINE; }

    public interface RenderingIntent {
        int AUTO = Constants.AUTO;
        int PERCEPTUAL = Constants.PERCEPTUAL;
        int RELATIVE_COLOMETRIC = Constants.RELATIVE_COLOMETRIC;
        int SATURATION = Constants.SATURATION;
        int ABSOLUTE_COLORMETRIC = Constants.ABSOLUTE_COLORMETRIC; }

    public interface RetrieveBoundary {
        int PAGE = Constants.PAGE;
        int PAGE_SEQUENCE = Constants.PAGE_SEQUENCE;
        int DOCUMENT = Constants.DOCUMENT; }

    public interface RetrievePosition {
        int FSWP = Constants.FSWP;
        int FIC = Constants.FIC;
        int LSWP = Constants.LSWP;
        int LEWP = Constants.LEWP; }

    public interface RuleStyle {
        int NONE = Constants.NONE;
        int DOTTED = Constants.DOTTED;
        int DASHED = Constants.DASHED;
        int SOLID = Constants.SOLID;
        int DOUBLE = Constants.DOUBLE;
        int GROOVE = Constants.GROOVE;
        int RIDGE = Constants.RIDGE; }

    public interface Scaling {
        int UNIFORM = Constants.UNIFORM;
        int NON_UNIFORM = Constants.NON_UNIFORM; }

    public interface SpaceAfter extends GenericSpaceInterface { }

    public interface SpaceBefore extends GenericSpaceInterface { }

    public interface SpaceEnd extends GenericSpaceInterface { }

    public interface SpaceStart extends GenericSpaceInterface { }

/*    public interface Span {   conflicts with Area.Span 
        int NONE = Constants.NONE;
        int ALL = Constants.ALL; }  */

    public interface TableLayout {
        int AUTO = Constants.AUTO;
        int FIXED = Constants.FIXED; }

    public interface TableOmitFooterAtBreak extends GenericBooleanInterface { }

    public interface TableOmitHeaderAtBreak extends GenericBooleanInterface { }

    public interface TextAlign {
        int CENTER = Constants.CENTER;
        int END = Constants.END;
        int START = Constants.START;
        int JUSTIFY = Constants.JUSTIFY; }

    public interface TextAlignLast {
        int CENTER = Constants.CENTER;
        int END = Constants.END;
        int START = Constants.START;
        int JUSTIFY = Constants.JUSTIFY; }

    public interface TextDecoration {
        int NONE = Constants.NONE;
        int UNDERLINE = Constants.UNDERLINE;
        int OVERLINE = Constants.OVERLINE;
        int LINE_THROUGH = Constants.LINE_THROUGH;
        int BLINK = Constants.BLINK;
        int NO_UNDERLINE = Constants.NO_UNDERLINE;
        int NO_OVERLINE = Constants.NO_OVERLINE;
        int NO_LINE_THROUGH = Constants.NO_LINE_THROUGH;
        int NO_BLINK = Constants.NO_BLINK; }

    public interface TextTransform {
        int NONE = Constants.NONE;
        int CAPITALIZE = Constants.CAPITALIZE;
        int UPPERCASE = Constants.UPPERCASE;
        int LOWERCASE = Constants.LOWERCASE; }

    public interface VerticalAlign {
        int BASELINE = Constants.BASELINE;
        int MIDDLE = Constants.MIDDLE;
        int SUB = Constants.SUB;
        int SUPER = Constants.SUPER;
        int TEXT_TOP = Constants.TEXT_TOP;
        int TEXT_BOTTOM = Constants.TEXT_BOTTOM;
        int TOP = Constants.TOP;
        int BOTTOM = Constants.BOTTOM; }

    public interface WhiteSpaceCollapse extends GenericBooleanInterface { }

    public interface WhiteSpaceTreatment {
        int IGNORE = Constants.IGNORE;
        int PRESERVE = Constants.PRESERVE;
        int IGNORE_IF_BEFORE_LINEFEED = Constants.IGNORE_IF_BEFORE_LINEFEED;
        int IGNORE_IF_AFTER_LINEFEED = Constants.IGNORE_IF_AFTER_LINEFEED;
        int IGNORE_IF_SURROUNDING_LINEFEED = Constants.IGNORE_IF_SURROUNDING_LINEFEED; }

    public interface WordSpacing extends GenericSpaceInterface { }

    public interface WrapOption {
        int WRAP = Constants.WRAP;
        int NO_WRAP = Constants.NO_WRAP; }

    public interface WritingMode {
        int LR_TB = Constants.LR_TB;
        int RL_TB = Constants.RL_TB;
        int TB_RL = Constants.TB_RL; }

}
