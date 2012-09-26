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

/**
 * Definition of constants used throughout FOP.
 * There are sets of constants describing:
 * <ul>
 * <li>Input and output formats</li>
 * <li>Formatting objects (<em>FO_XXX</em>)</li>
 * <li>Formatting properties (<em>PR_XXX</em>)</li>
 * <li>Enumerated values used in formatting properties and traits (<em>EN_XXX</em>)</li>
 * </ul>
 */
public interface Constants {

    /** not set */
    int NOT_SET = 0;

    // element constants
    /** FObj base class */
    int FO_UNKNOWN_NODE = 0;
    /** FO element constant */
    int FO_BASIC_LINK = 1;
    /** FO element constant */
    int FO_BIDI_OVERRIDE = 2;
    /** FO element constant */
    int FO_BLOCK = 3;
    /** FO element constant */
    int FO_BLOCK_CONTAINER = 4;
    /** FO element constant - XSL 1.1 */
    int FO_BOOKMARK_TREE = 5;
    /** FO element constant - XSL 1.1 */
    int FO_BOOKMARK = 6;
    /** FO element constant - XSL 1.1 */
    int FO_BOOKMARK_TITLE = 7;
    /** FO element constant - XSL 1.1 */
    int FO_CHANGE_BAR_BEGIN = 8;
    /** FO element constant - XSL 1.1 */
    int FO_CHANGE_BAR_END = 9;
    /** FO element constant */
    int FO_CHARACTER = 10;
    /** FO element constant */
    int FO_COLOR_PROFILE = 11;
    /** FO element constant */
    int FO_CONDITIONAL_PAGE_MASTER_REFERENCE = 12;
    /** FO element constant */
    int FO_DECLARATIONS = 13;
    /** FO element constant */
    int FO_EXTERNAL_GRAPHIC = 14;
    /** FO element constant */
    int FO_FLOAT = 15;
    /** FO element constant */
    int FO_FLOW = 16;
    /** FO element constant - XSL 1.1 */
    int FO_FLOW_ASSIGNMENT = 17;
    /** FO element constant - XSL 1.1 */
    int FO_FLOW_MAP = 18;
    /** FO element constant - XSL 1.1 */
    int FO_FLOW_NAME_SPECIFIER = 19;
    /** FO element constant - XSL 1.1 */
    int FO_FLOW_SOURCE_LIST = 20;
    /** FO element constant - XSL 1.1 */
    int FO_FLOW_TARGET_LIST = 21;
    /** FO element constant - XSL 1.1 */
    int FO_FOLIO_PREFIX = 22;
    /** FO element constant - XSL 1.1 */
    int FO_FOLIO_SUFFIX = 23;
    /** FO element constant */
    int FO_FOOTNOTE = 24;
    /** FO element constant */
    int FO_FOOTNOTE_BODY = 25;
    /** FO element constant - XSL 1.1 */
    int FO_INDEX_KEY_REFERENCE = 26;
    /** FO element constant - XSL 1.1 */
    int FO_INDEX_PAGE_NUMBER_PREFIX = 27;
    /** FO element constant - XSL 1.1 */
    int FO_INDEX_PAGE_NUMBER_SUFFIX = 28;
    /** FO element constant - XSL 1.1 */
    int FO_INDEX_PAGE_CITATION_LIST = 29;
    /** FO element constant - XSL 1.1 */
    int FO_INDEX_PAGE_CITATION_LIST_SEPARATOR = 30;
    /** FO element constant - XSL 1.1 */
    int FO_INDEX_PAGE_CITATION_RANGE_SEPARATOR = 31;
    /** FO element constant - XSL 1.1 */
    int FO_INDEX_RANGE_BEGIN = 32;
    /** FO element constant - XSL 1.1 */
    int FO_INDEX_RANGE_END = 33;
    /** FO element constant */
    int FO_INITIAL_PROPERTY_SET = 34;
    /** FO element constant */
    int FO_INLINE = 35;
    /** FO element constant */
    int FO_INLINE_CONTAINER = 36;
    /** FO element constant */
    int FO_INSTREAM_FOREIGN_OBJECT = 37;
    /** FO element constant */
    int FO_LAYOUT_MASTER_SET = 38;
    /** FO element constant */
    int FO_LEADER = 39;
    /** FO element constant */
    int FO_LIST_BLOCK = 40;
    /** FO element constant */
    int FO_LIST_ITEM = 41;
    /** FO element constant */
    int FO_LIST_ITEM_BODY = 42;
    /** FO element constant */
    int FO_LIST_ITEM_LABEL = 43;
    /** FO element constant */
    int FO_MARKER = 44;
    /** FO element constant */
    int FO_MULTI_CASE = 45;
    /** FO element constant */
    int FO_MULTI_PROPERTIES = 46;
    /** FO element constant */
    int FO_MULTI_PROPERTY_SET = 47;
    /** FO element constant */
    int FO_MULTI_SWITCH = 48;
    /** FO element constant */
    int FO_MULTI_TOGGLE = 49;
    /** FO element constant */
    int FO_PAGE_NUMBER = 50;
    /** FO element constant */
    int FO_PAGE_NUMBER_CITATION = 51;
    /** FO element constant - XSL 1.1 */
    int FO_PAGE_NUMBER_CITATION_LAST = 52;
    /** FO element constant */
    int FO_PAGE_SEQUENCE = 53;
    /** FO element constant */
    int FO_PAGE_SEQUENCE_MASTER = 54;
    /** FO element constant - XSL 1.1 */
    int FO_PAGE_SEQUENCE_WRAPPER = 55;
    /** FO element constant */
    int FO_REGION_AFTER = 56;
    /** FO element constant */
    int FO_REGION_BEFORE = 57;
    /** FO element constant */
    int FO_REGION_BODY = 58;
    /** FO element constant */
    int FO_REGION_END = 59;
    /** FO element constant - XSL 1.1 */
    int FO_REGION_NAME_SPECIFIER = 60;
    /** FO element constant */
    int FO_REGION_START = 61;
    /** FO element constant */
    int FO_REPEATABLE_PAGE_MASTER_ALTERNATIVES = 62;
    /** FO element constant */
    int FO_REPEATABLE_PAGE_MASTER_REFERENCE = 63;
    /** FO element constant */
    int FO_RETRIEVE_MARKER = 64;
    /** FO element constant - XSL 1.1 */
    int FO_RETRIEVE_TABLE_MARKER = 65;
    /** FO element constant */
    int FO_ROOT = 66;
    /** FO element constant - XSL 1.1 */
    int FO_SCALING_VALUE_CITATION = 67;
    /** FO element constant */
    int FO_SIMPLE_PAGE_MASTER = 68;
    /** FO element constant */
    int FO_SINGLE_PAGE_MASTER_REFERENCE = 69;
    /** FO element constant */
    int FO_STATIC_CONTENT = 70;
    /** FO element constant */
    int FO_TABLE = 71;
    /** FO element constant */
    int FO_TABLE_AND_CAPTION = 72;
    /** FO element constant */
    int FO_TABLE_BODY = 73;
    /** FO element constant */
    int FO_TABLE_CAPTION = 74;
    /** FO element constant */
    int FO_TABLE_CELL = 75;
    /** FO element constant */
    int FO_TABLE_COLUMN = 76;
    /** FO element constant */
    int FO_TABLE_FOOTER = 77;
    /** FO element constant */
    int FO_TABLE_HEADER = 78;
    /** FO element constant */
    int FO_TABLE_ROW = 79;
    /** FO element constant */
    int FO_TITLE = 80;
    /** FO element constant */
    int FO_WRAPPER = 81;
    /** Number of FO element constants defined */
    int FRM_OBJ_COUNT = 81;

    // Masks
    /**
     * For compound properties the property constant value is shifted by this amount.
     * The low order bits hold the constant for the component property.
     */
    int COMPOUND_SHIFT = 9;
    /**
     * Mask that when applied to a compound property returns the constant of
     * the component property.
     */
    int PROPERTY_MASK = (1 << COMPOUND_SHIFT) - 1;
    /**
     * Mask that when applied to a compound property returns the constant of
     * the compound property.
     */
    int COMPOUND_MASK = ~PROPERTY_MASK;
    /** Number of compund properties defined */
    int COMPOUND_COUNT = 11;

    // property constants
    /** Property constant */
    int PR_ABSOLUTE_POSITION = 1;
    /** Property constant */
    int PR_ACTIVE_STATE = 2;
    /** Property constant */
    int PR_ALIGNMENT_ADJUST = 3;
    /** Property constant */
    int PR_ALIGNMENT_BASELINE = 4;
    /** Property constant */
    int PR_AUTO_RESTORE = 5;
    /** Property constant */
    int PR_AZIMUTH = 6;
    /** Property constant */
    int PR_BACKGROUND = 7;
    /** Property constant */
    int PR_BACKGROUND_ATTACHMENT = 8;
    /** Property constant */
    int PR_BACKGROUND_COLOR = 9;
    /** Property constant */
    int PR_BACKGROUND_IMAGE = 10;
    /** Property constant */
    int PR_BACKGROUND_POSITION = 11;
    /** Property constant */
    int PR_BACKGROUND_POSITION_HORIZONTAL = 12;
    /** Property constant */
    int PR_BACKGROUND_POSITION_VERTICAL = 13;
    /** Property constant */
    int PR_BACKGROUND_REPEAT = 14;
    /** Property constant */
    int PR_BASELINE_SHIFT = 15;
    /** Property constant */
    int PR_BLANK_OR_NOT_BLANK = 16;
    /** Property constant */
    int PR_BLOCK_PROGRESSION_DIMENSION = 17;
    /** Property constant */
    int PR_BORDER = 18;
    /** Property constant */
    int PR_BORDER_AFTER_COLOR = 19;
    /** Property constant */
    int PR_BORDER_AFTER_PRECEDENCE = 20;
    /** Property constant */
    int PR_BORDER_AFTER_STYLE = 21;
    /** Property constant */
    int PR_BORDER_AFTER_WIDTH = 22;
    /** Property constant */
    int PR_BORDER_BEFORE_COLOR = 23;
    /** Property constant */
    int PR_BORDER_BEFORE_PRECEDENCE = 24;
    /** Property constant */
    int PR_BORDER_BEFORE_STYLE = 25;
    /** Property constant */
    int PR_BORDER_BEFORE_WIDTH = 26;
    /** Property constant */
    int PR_BORDER_BOTTOM = 27;
    /** Property constant */
    int PR_BORDER_BOTTOM_COLOR = 28;
    /** Property constant */
    int PR_BORDER_BOTTOM_STYLE = 29;
    /** Property constant */
    int PR_BORDER_BOTTOM_WIDTH = 30;
    /** Property constant */
    int PR_BORDER_COLLAPSE = 31;
    /** Property constant */
    int PR_BORDER_COLOR = 32;
    /** Property constant */
    int PR_BORDER_END_COLOR = 33;
    /** Property constant */
    int PR_BORDER_END_PRECEDENCE = 34;
    /** Property constant */
    int PR_BORDER_END_STYLE = 35;
    /** Property constant */
    int PR_BORDER_END_WIDTH = 36;
    /** Property constant */
    int PR_BORDER_LEFT = 37;
    /** Property constant */
    int PR_BORDER_LEFT_COLOR = 38;
    /** Property constant */
    int PR_BORDER_LEFT_STYLE = 39;
    /** Property constant */
    int PR_BORDER_LEFT_WIDTH = 40;
    /** Property constant */
    int PR_BORDER_RIGHT = 41;
    /** Property constant */
    int PR_BORDER_RIGHT_COLOR = 42;
    /** Property constant */
    int PR_BORDER_RIGHT_STYLE = 43;
    /** Property constant */
    int PR_BORDER_RIGHT_WIDTH = 44;
    /** Property constant */
    int PR_BORDER_SEPARATION = 45;
    /** Property constant */
    int PR_BORDER_SPACING = 46;
    /** Property constant */
    int PR_BORDER_START_COLOR = 47;
    /** Property constant */
    int PR_BORDER_START_PRECEDENCE = 48;
    /** Property constant */
    int PR_BORDER_START_STYLE = 49;
    /** Property constant */
    int PR_BORDER_START_WIDTH = 50;
    /** Property constant */
    int PR_BORDER_STYLE = 51;
    /** Property constant */
    int PR_BORDER_TOP = 52;
    /** Property constant */
    int PR_BORDER_TOP_COLOR = 53;
    /** Property constant */
    int PR_BORDER_TOP_STYLE = 54;
    /** Property constant */
    int PR_BORDER_TOP_WIDTH = 55;
    /** Property constant */
    int PR_BORDER_WIDTH = 56;
    /** Property constant */
    int PR_BOTTOM = 57;
    /** Property constant */
    int PR_BREAK_AFTER = 58;
    /** Property constant */
    int PR_BREAK_BEFORE = 59;
    /** Property constant */
    int PR_CAPTION_SIDE = 60;
    /** Property constant */
    int PR_CASE_NAME = 61;
    /** Property constant */
    int PR_CASE_TITLE = 62;
    /** Property constant - XSL 1.1 */
    int PR_CHANGE_BAR_CLASS = 63;
    /** Property constant - XSL 1.1 */
    int PR_CHANGE_BAR_COLOR = 64;
    /** Property constant - XSL 1.1 */
    int PR_CHANGE_BAR_OFFSET = 65;
    /** Property constant - XSL 1.1 */
    int PR_CHANGE_BAR_PLACEMENT = 66;
    /** Property constant - XSL 1.1 */
    int PR_CHANGE_BAR_STYLE = 67;
    /** Property constant - XSL 1.1 */
    int PR_CHANGE_BAR_WIDTH = 68;
    /** Property constant */
    int PR_CHARACTER = 69;
    /** Property constant */
    int PR_CLEAR = 70;
    /** Property constant */
    int PR_CLIP = 71;
    /** Property constant */
    int PR_COLOR = 72;
    /** Property constant */
    int PR_COLOR_PROFILE_NAME = 73;
    /** Property constant */
    int PR_COLUMN_COUNT = 74;
    /** Property constant */
    int PR_COLUMN_GAP = 75;
    /** Property constant */
    int PR_COLUMN_NUMBER = 76;
    /** Property constant */
    int PR_COLUMN_WIDTH = 77;
    /** Property constant */
    int PR_CONTENT_HEIGHT = 78;
    /** Property constant */
    int PR_CONTENT_TYPE = 79;
    /** Property constant */
    int PR_CONTENT_WIDTH = 80;
    /** Property constant */
    int PR_COUNTRY = 81;
    /** Property constant */
    int PR_CUE = 82;
    /** Property constant */
    int PR_CUE_AFTER = 83;
    /** Property constant */
    int PR_CUE_BEFORE = 84;
    /** Property constant */
    int PR_DESTINATION_PLACEMENT_OFFSET = 85;
    /** Property constant */
    int PR_DIRECTION = 86;
    /** Property constant */
    int PR_DISPLAY_ALIGN = 87;
    /** Property constant */
    int PR_DOMINANT_BASELINE = 88;
    /** Property constant */
    int PR_ELEVATION = 89;
    /** Property constant */
    int PR_EMPTY_CELLS = 90;
    /** Property constant */
    int PR_END_INDENT = 91;
    /** Property constant */
    int PR_ENDS_ROW = 92;
    /** Property constant */
    int PR_EXTENT = 93;
    /** Property constant */
    int PR_EXTERNAL_DESTINATION = 94;
    /** Property constant */
    int PR_FLOAT = 95;
    /** Property constant -- XSL 1.1 */
    int PR_FLOW_MAP_NAME = 96;
    /** Property constant -- XSL 1.1 */
    int PR_FLOW_MAP_REFERENCE = 97;
    /** Property constant */
    int PR_FLOW_NAME = 98;
    /** Property constant -- XSL 1.1 */
    int PR_FLOW_NAME_REFERENCE = 99;
    /** Property constant */
    int PR_FONT = 100;
    /** Property constant */
    int PR_FONT_FAMILY = 101;
    /** Property constant */
    int PR_FONT_SELECTION_STRATEGY = 102;
    /** Property constant */
    int PR_FONT_SIZE = 103;
    /** Property constant */
    int PR_FONT_SIZE_ADJUST = 104;
    /** Property constant */
    int PR_FONT_STRETCH = 105;
    /** Property constant */
    int PR_FONT_STYLE = 106;
    /** Property constant */
    int PR_FONT_VARIANT = 107;
    /** Property constant */
    int PR_FONT_WEIGHT = 108;
    /** Property constant */
    int PR_FORCE_PAGE_COUNT = 109;
    /** Property constant */
    int PR_FORMAT = 110;
    /** Property constant */
    int PR_GLYPH_ORIENTATION_HORIZONTAL = 111;
    /** Property constant */
    int PR_GLYPH_ORIENTATION_VERTICAL = 112;
    /** Property constant */
    int PR_GROUPING_SEPARATOR = 113;
    /** Property constant */
    int PR_GROUPING_SIZE = 114;
    /** Property constant */
    int PR_HEIGHT = 115;
    /** Property constant */
    int PR_HYPHENATE = 116;
    /** Property constant */
    int PR_HYPHENATION_CHARACTER = 117;
    /** Property constant */
    int PR_HYPHENATION_KEEP = 118;
    /** Property constant */
    int PR_HYPHENATION_LADDER_COUNT = 119;
    /** Property constant */
    int PR_HYPHENATION_PUSH_CHARACTER_COUNT = 120;
    /** Property constant */
    int PR_HYPHENATION_REMAIN_CHARACTER_COUNT = 121;
    /** Property constant */
    int PR_ID = 122;
    /** Property constant */
    int PR_INDICATE_DESTINATION = 123;
    /** Property constant - XSL 1.1 */
    int PR_INDEX_CLASS = 124;
    /** Property constant - XSL 1.1 */
    int PR_INDEX_KEY = 125;
    /** Property constant */
    int PR_INITIAL_PAGE_NUMBER = 126;
    /** Property constant */
    int PR_INLINE_PROGRESSION_DIMENSION = 127;
    /** Property constant */
    int PR_INTERNAL_DESTINATION = 128;
    /** Property constant - XSL 1.1 */
    int PR_INTRINSIC_SCALE_VALUE = 129;
    /** Property constant */
    int PR_INTRUSION_DISPLACE = 130;
    /** Property constant */
    int PR_KEEP_TOGETHER = 131;
    /** Property constant */
    int PR_KEEP_WITH_NEXT = 132;
    /** Property constant */
    int PR_KEEP_WITH_PREVIOUS = 133;
    /** Property constant */
    int PR_LANGUAGE = 134;
    /** Property constant */
    int PR_LAST_LINE_END_INDENT = 135;
    /** Property constant */
    int PR_LEADER_ALIGNMENT = 136;
    /** Property constant */
    int PR_LEADER_LENGTH = 137;
    /** Property constant */
    int PR_LEADER_PATTERN = 138;
    /** Property constant */
    int PR_LEADER_PATTERN_WIDTH = 139;
    /** Property constant */
    int PR_LEFT = 140;
    /** Property constant */
    int PR_LETTER_SPACING = 141;
    /** Property constant */
    int PR_LETTER_VALUE = 142;
    /** Property constant */
    int PR_LINEFEED_TREATMENT = 143;
    /** Property constant */
    int PR_LINE_HEIGHT = 144;
    /** Property constant */
    int PR_LINE_HEIGHT_SHIFT_ADJUSTMENT = 145;
    /** Property constant */
    int PR_LINE_STACKING_STRATEGY = 146;
    /** Property constant */
    int PR_MARGIN = 147;
    /** Property constant */
    int PR_MARGIN_BOTTOM = 148;
    /** Property constant */
    int PR_MARGIN_LEFT = 149;
    /** Property constant */
    int PR_MARGIN_RIGHT = 150;
    /** Property constant */
    int PR_MARGIN_TOP = 151;
    /** Property constant */
    int PR_MARKER_CLASS_NAME = 152;
    /** Property constant */
    int PR_MASTER_NAME = 153;
    /** Property constant */
    int PR_MASTER_REFERENCE = 154;
    /** Property constant */
    int PR_MAX_HEIGHT = 155;
    /** Property constant */
    int PR_MAXIMUM_REPEATS = 156;
    /** Property constant */
    int PR_MAX_WIDTH = 157;
    /** Property constant - XSL 1.1 */
    int PR_MERGE_PAGES_ACROSS_INDEX_KEY_REFERENCES = 158;
    /** Property constant - XSL 1.1 */
    int PR_MERGE_RANGES_ACROSS_INDEX_KEY_REFERENCES = 159;
    /** Property constant - XSL 1.1 */
    int PR_MERGE_SEQUENTIAL_PAGE_NUMBERS = 160;
    /** Property constant */
    int PR_MEDIA_USAGE = 161;
    /** Property constant */
    int PR_MIN_HEIGHT = 162;
    /** Property constant */
    int PR_MIN_WIDTH = 163;
    /** Property constant */
    int PR_NUMBER_COLUMNS_REPEATED = 164;
    /** Property constant */
    int PR_NUMBER_COLUMNS_SPANNED = 165;
    /** Property constant */
    int PR_NUMBER_ROWS_SPANNED = 166;
    /** Property constant */
    int PR_ODD_OR_EVEN = 167;
    /** Property constant */
    int PR_ORPHANS = 168;
    /** Property constant */
    int PR_OVERFLOW = 169;
    /** Property constant */
    int PR_PADDING = 170;
    /** Property constant */
    int PR_PADDING_AFTER = 171;
    /** Property constant */
    int PR_PADDING_BEFORE = 172;
    /** Property constant */
    int PR_PADDING_BOTTOM = 173;
    /** Property constant */
    int PR_PADDING_END = 174;
    /** Property constant */
    int PR_PADDING_LEFT = 175;
    /** Property constant */
    int PR_PADDING_RIGHT = 176;
    /** Property constant */
    int PR_PADDING_START = 177;
    /** Property constant */
    int PR_PADDING_TOP = 178;
    /** Property constant */
    int PR_PAGE_BREAK_AFTER = 179;
    /** Property constant */
    int PR_PAGE_BREAK_BEFORE = 180;
    /** Property constant */
    int PR_PAGE_BREAK_INSIDE = 181;
    /** Property constant - XSL 1.1 */
    int PR_PAGE_CITATION_STRATEGY = 182;
    /** Property constant */
    int PR_PAGE_HEIGHT = 183;
    /** Property constant - XSL 1.1 */
    int PR_PAGE_NUMBER_TREATMENT = 184;
    /** Property constant */
    int PR_PAGE_POSITION = 185;
    /** Property constant */
    int PR_PAGE_WIDTH = 186;
    /** Property constant */
    int PR_PAUSE = 187;
    /** Property constant */
    int PR_PAUSE_AFTER = 188;
    /** Property constant */
    int PR_PAUSE_BEFORE = 189;
    /** Property constant */
    int PR_PITCH = 190;
    /** Property constant */
    int PR_PITCH_RANGE = 191;
    /** Property constant */
    int PR_PLAY_DURING = 192;
    /** Property constant */
    int PR_POSITION = 193;
    /** Property constant */
    int PR_PRECEDENCE = 194;
    /** Property constant */
    int PR_PROVISIONAL_DISTANCE_BETWEEN_STARTS = 195;
    /** Property constant */
    int PR_PROVISIONAL_LABEL_SEPARATION = 196;
    /** Property constant */
    int PR_REFERENCE_ORIENTATION = 197;
    /** Property constant */
    int PR_REF_ID = 198;
    /** Property constant */
    int PR_REGION_NAME = 199;
    /** Property constant - XSL 1.1 */
    int PR_REGION_NAME_REFERENCE = 200;
    /** Property constant - XSL 1.1 */
    int PR_REF_INDEX_KEY = 201;
    /** Property constant */
    int PR_RELATIVE_ALIGN = 202;
    /** Property constant */
    int PR_RELATIVE_POSITION = 203;
    /** Property constant */
    int PR_RENDERING_INTENT = 204;
    /** Property constant */
    int PR_RETRIEVE_BOUNDARY = 205;
    /** Property constant - XSL 1.1 */
    int PR_RETRIEVE_BOUNDARY_WITHIN_TABLE = 206;
    /** Property constant */
    int PR_RETRIEVE_CLASS_NAME = 207;
    /** Property constant */
    int PR_RETRIEVE_POSITION = 208;
    /** Property constant - XSL 1.1 */
    int PR_RETRIEVE_POSITION_WITHIN_TABLE = 209;
    /** Property constant */
    int PR_RICHNESS = 210;
    /** Property constant */
    int PR_RIGHT = 211;
    /** Property constant */
    int PR_ROLE = 212;
    /** Property constant */
    int PR_RULE_STYLE = 213;
    /** Property constant */
    int PR_RULE_THICKNESS = 214;
    /** Property constant */
    int PR_SCALING = 215;
    /** Property constant */
    int PR_SCALING_METHOD = 216;
    /** Property constant */
    int PR_SCORE_SPACES = 217;
    /** Property constant */
    int PR_SCRIPT = 218;
    /** Property constant */
    int PR_SHOW_DESTINATION = 219;
    /** Property constant */
    int PR_SIZE = 220;
    /** Property constant */
    int PR_SOURCE_DOCUMENT = 221;
    /** Property constant */
    int PR_SPACE_AFTER = 222;
    /** Property constant */
    int PR_SPACE_BEFORE = 223;
    /** Property constant */
    int PR_SPACE_END = 224;
    /** Property constant */
    int PR_SPACE_START = 225;
    /** Property constant */
    int PR_SPAN = 226;
    /** Property constant */
    int PR_SPEAK = 227;
    /** Property constant */
    int PR_SPEAK_HEADER = 228;
    /** Property constant */
    int PR_SPEAK_NUMERAL = 229;
    /** Property constant */
    int PR_SPEAK_PUNCTUATION = 230;
    /** Property constant */
    int PR_SPEECH_RATE = 231;
    /** Property constant */
    int PR_SRC = 232;
    /** Property constant */
    int PR_START_INDENT = 233;
    /** Property constant */
    int PR_STARTING_STATE = 234;
    /** Property constant */
    int PR_STARTS_ROW = 235;
    /** Property constant */
    int PR_STRESS = 236;
    /** Property constant */
    int PR_SUPPRESS_AT_LINE_BREAK = 237;
    /** Property constant */
    int PR_SWITCH_TO = 238;
    /** Property constant */
    int PR_TABLE_LAYOUT = 239;
    /** Property constant */
    int PR_TABLE_OMIT_FOOTER_AT_BREAK = 240;
    /** Property constant */
    int PR_TABLE_OMIT_HEADER_AT_BREAK = 241;
    /** Property constant */
    int PR_TARGET_PRESENTATION_CONTEXT = 242;
    /** Property constant */
    int PR_TARGET_PROCESSING_CONTEXT = 243;
    /** Property constant */
    int PR_TARGET_STYLESHEET = 244;
    /** Property constant */
    int PR_TEXT_ALIGN = 245;
    /** Property constant */
    int PR_TEXT_ALIGN_LAST = 246;
    /** Property constant */
    int PR_TEXT_ALTITUDE = 247;
    /** Property constant */
    int PR_TEXT_DECORATION = 248;
    /** Property constant */
    int PR_TEXT_DEPTH = 249;
    /** Property constant */
    int PR_TEXT_INDENT = 250;
    /** Property constant */
    int PR_TEXT_SHADOW = 251;
    /** Property constant */
    int PR_TEXT_TRANSFORM = 252;
    /** Property constant */
    int PR_TOP = 253;
    /** Property constant */
    int PR_TREAT_AS_WORD_SPACE = 254;
    /** Property constant */
    int PR_UNICODE_BIDI = 255;
    /** Property constant */
    int PR_VERTICAL_ALIGN = 256;
    /** Property constant */
    int PR_VISIBILITY = 257;
    /** Property constant */
    int PR_VOICE_FAMILY = 258;
    /** Property constant */
    int PR_VOLUME = 259;
    /** Property constant */
    int PR_WHITE_SPACE = 260;
    /** Property constant */
    int PR_WHITE_SPACE_COLLAPSE = 261;
    /** Property constant */
    int PR_WHITE_SPACE_TREATMENT = 262;
    /** Property constant */
    int PR_WIDOWS = 263;
    /** Property constant */
    int PR_WIDTH = 264;
    /** Property constant */
    int PR_WORD_SPACING = 265;
    /** Property constant */
    int PR_WRAP_OPTION = 266;
    /** Property constant */
    int PR_WRITING_MODE = 267;
    /** Property constant */
    int PR_XML_LANG = 268;
    /** Property constant */
    int PR_Z_INDEX = 269;
    /** Property constant - FOP proprietary: Custom extension for line alignment */
    int PR_X_BLOCK_PROGRESSION_UNIT = 270;
    /** Property constant - FOP proprietary: limit for widow content in lists and tables */
    int PR_X_WIDOW_CONTENT_LIMIT = 271;
    /** Property constant - FOP proprietary: limit for orphan content in lists and tables */
    int PR_X_ORPHAN_CONTENT_LIMIT = 272;
    /**
     * Property constant - FOP proprietary: disable balancing of columns in
     * multi-column layouts.
     */
    int PR_X_DISABLE_COLUMN_BALANCING = 273;
    /**
     * Property constant - FOP proprietary: alternative text for e-g and i-f-o.
     * Used for accessibility.
     */
    int PR_X_ALT_TEXT = 274;
    /** Property constant - FOP proprietary prototype (in XSL-FO 2.0 Requirements) */
    int PR_X_XML_BASE = 275;
    /**
     * Property constant - FOP proprietary extension (see NumberConverter) used
     * to perform additional control over number conversion when generating page
     * numbers.
     */
    int PR_X_NUMBER_CONVERSION_FEATURES = 276;

    /** Scope for table header */
    int PR_X_HEADER_COLUMN = 277;

    /** Number of property constants defined */
    int PROPERTY_COUNT = 277;

    // compound property constants

    /** Property constant for compound property */
    int CP_BLOCK_PROGRESSION_DIRECTION = 1 << COMPOUND_SHIFT;
    /** Property constant for compound property */
    int CP_CONDITIONALITY = 2 << COMPOUND_SHIFT;
    /** Property constant for compound property */
    int CP_INLINE_PROGRESSION_DIRECTION = 3 << COMPOUND_SHIFT;
    /** Property constant for compound property */
    int CP_LENGTH = 4 << COMPOUND_SHIFT;
    /** Property constant for compound property */
    int CP_MAXIMUM = 5 << COMPOUND_SHIFT;
    /** Property constant for compound property */
    int CP_MINIMUM = 6 << COMPOUND_SHIFT;
    /** Property constant for compound property */
    int CP_OPTIMUM = 7 << COMPOUND_SHIFT;
    /** Property constant for compound property */
    int CP_PRECEDENCE = 8 << COMPOUND_SHIFT;
    /** Property constant for compound property */
    int CP_WITHIN_COLUMN = 9 << COMPOUND_SHIFT;
    /** Property constant for compound property */
    int CP_WITHIN_LINE = 10 << COMPOUND_SHIFT;
    /** Property constant for compound property */
    int CP_WITHIN_PAGE = 11 << COMPOUND_SHIFT;

    // Enumeration constants
    /** Enumeration constant */
    int EN_ABSOLUTE = 1;
    /** Enumeration constant */
    int EN_ABSOLUTE_COLORMETRIC = 2;
    /** Enumeration constant */
    int EN_AFTER = 3;
    /** Enumeration constant */
    int EN_AFTER_EDGE = 4;
    /** Enumeration constant */
    int EN_ALL = 5;
    /** Enumeration constant */
    int EN_ALPHABETIC = 6;
    /** Enumeration constant */
    int EN_ALWAYS = 7;
    /** Enumeration constant */
    int EN_ANY = 8;
    /** Enumeration constant */
    int EN_AUTO = 9;
    /** Enumeration constant */
    int EN_AUTO_EVEN = 10;
    /** Enumeration constant */
    int EN_AUTO_ODD = 11;
    /** Enumeration constant */
    int EN_BASELINE = 12;
    /** Enumeration constant */
    int EN_BEFORE = 13;
    /** Enumeration constant */
    int EN_BEFORE_EDGE = 14;
    /** Enumeration constant */
    int EN_BIDI_OVERRIDE = 15;
    /** Enumeration constant */
    int EN_BLANK = 16;
    /** Enumeration constant */
    int EN_BLINK = 17;
    /** Enumeration constant */
    int EN_BLOCK = 18;
    /** Enumeration constant */
    int EN_BOTH = 19;
    /** Enumeration constant */
    int EN_BOTTOM = 20;
    /** Enumeration constant */
    int EN_BOUNDED_IN_ONE_DIMENSION = 21;
    /** Enumeration constant */
    int EN_CAPITALIZE = 22;
    /** Enumeration constant */
    int EN_CENTER = 23;
    /** Enumeration constant */
    int EN_CENTRAL = 24;
    /** Enumeration constant */
    int EN_CHARACTER_BY_CHARACTER = 25;
    /** Enumeration constant */
    int EN_COLLAPSE = 26;
    /** Enumeration constant */
    int EN_COLLAPSE_WITH_PRECEDENCE = 27;
    /** Enumeration constant */
    int EN_COLUMN = 28;
    /** Enumeration constant */
    int EN_CONDENSED = 29;
    /** Enumeration constant */
    int EN_CONSIDER_SHIFTS = 30;
    /** Enumeration constant */
    int EN_DASHED = 31;
    /** Enumeration constant */
    int EN_DISCARD = 32;
    /** Enumeration constant */
    int EN_DISREGARD_SHIFTS = 33;
    /** Enumeration constant */
    int EN_DOCUMENT = 34;
    /** Enumeration constant */
    int EN_DOTS = 35;
    /** Enumeration constant */
    int EN_DOTTED = 36;
    /** Enumeration constant */
    int EN_DOUBLE = 37;
    /** Enumeration constant */
    int EN_EMBED = 38;
    /** Enumeration constant */
    int EN_END = 39;
    /** Enumeration constant */
    int EN_END_ON_EVEN = 40;
    /** Enumeration constant */
    int EN_END_ON_ODD = 41;
    /** Enumeration constant */
    int EN_ERROR_IF_OVERFLOW = 42;
    /** Enumeration constant */
    int EN_EVEN = 43;
    /** Enumeration constant */
    int EN_EVEN_PAGE = 44;
    /** Enumeration constant */
    int EN_EXPANDED = 45;
    /** Enumeration constant */
    int EN_EXTRA_CONDENSED = 46;
    /** Enumeration constant */
    int EN_EXTRA_EXPANDED = 47;
    /** Enumeration constant */
    int EN_FALSE = 48;
    /** Enumeration constant */
    int EN_FIC = 49;
    /** Enumeration constant */
    int EN_FIRST = 50;
    /** Enumeration constant */
    int EN_FIXED = 51;
    /** Enumeration constant */
    int EN_FONT_HEIGHT = 52;
    /** Enumeration constant */
    int EN_FORCE = 53;
    /** Enumeration constant */
    int EN_FSWP = 54;
    /** Enumeration constant */
    int EN_GROOVE = 55;
    /** Enumeration constant */
    int EN_HANGING = 56;
    /** Enumeration constant */
    int EN_HIDDEN = 57;
    /** Enumeration constant */
    int EN_HIDE = 58;
    /** Enumeration constant */
    int EN_IDEOGRAPHIC = 59;
    /** Enumeration constant */
    int EN_IGNORE = 60;
    /** Enumeration constant */
    int EN_IGNORE_IF_AFTER_LINEFEED = 61;
    /** Enumeration constant */
    int EN_IGNORE_IF_BEFORE_LINEFEED = 62;
    /** Enumeration constant */
    int EN_IGNORE_IF_SURROUNDING_LINEFEED = 63;
    /** Enumeration constant */
    int EN_INDEFINITE = 64;
    /** Enumeration constant */
    int EN_INDENT = 65;
    /** Enumeration constant */
    int EN_INHERIT = 66;
    /** Enumeration constant */
    int EN_INSET = 67;
    /** Enumeration constant */
    int EN_INSIDE = 68;
    /** Enumeration constant */
    int EN_INTEGER_PIXELS = 69;
    /** Enumeration constant */
    int EN_JUSTIFY = 70;
    /** Enumeration constant */
    int EN_LARGER = 71;
    /** Enumeration constant */
    int EN_LAST = 72;
    /** Enumeration constant */
    int EN_LEFT = 73;
    /** Enumeration constant */
    int EN_LEWP = 74;
    /** Enumeration constant */
    int EN_LINE = 75;
    /** Enumeration constant */
    int EN_LINE_HEIGHT = 76;
    /** Enumeration constant */
    int EN_LINE_THROUGH = 77;
    /** Enumeration constant */
    int EN_LOWERCASE = 78;
    /** Enumeration constant */
    int EN_LR_TB = 79;
    /** Enumeration constant */
    int EN_LTR = 80;
    /** Enumeration constant */
    int EN_LSWP = 81;
    /** Enumeration constant */
    int EN_MATHEMATICAL = 82;
    /** Enumeration constant */
    int EN_MAX_HEIGHT = 83;
    /** Enumeration constant */
    int EN_MIDDLE = 84;
    /** Enumeration constant */
    int EN_NARROWER = 85;
    /** Enumeration constant */
    int EN_NO_BLINK = 86;
    /** Enumeration constant */
    int EN_NO_CHANGE = 87;
    /** Enumeration constant */
    int EN_NO_FORCE = 88;
    /** Enumeration constant */
    int EN_NO_LIMIT = 89;
    /** Enumeration constant */
    int EN_NO_LINE_THROUGH = 90;
    /** Enumeration constant */
    int EN_NO_OVERLINE = 91;
    /** Enumeration constant */
    int EN_NO_UNDERLINE = 92;
    /** Enumeration constant */
    int EN_NO_WRAP = 93;
    /** Enumeration constant */
    int EN_NON_UNIFORM = 94;
    /** Enumeration constant */
    int EN_NONE = 95;
    /** Enumeration constant */
    int EN_NOREPEAT = 96;
    /** Enumeration constant */
    int EN_NORMAL = 97;
    /** Enumeration constant */
    int EN_NOT_BLANK = 98;
    /** Enumeration constant */
    int EN_ODD = 99;
    /** Enumeration constant */
    int EN_ODD_PAGE = 100;
    /** Enumeration constant */
    int EN_OUTSET = 101;
    /** Enumeration constant */
    int EN_OUTSIDE = 102;
    /** Enumeration constant */
    int EN_OVERLINE = 103;
    /** Enumeration constant */
    int EN_PAGE = 104;
    /** Enumeration constant */
    int EN_PAGE_SEQUENCE = 105;
    /** Enumeration constant */
    int EN_PAGINATE = 106;
    /** Enumeration constant */
    int EN_PERCEPTUAL = 107;
    /** Enumeration constant */
    int EN_PRESERVE = 108;
    /** Enumeration constant */
    int EN_REFERENCE_AREA = 109;
    /** Enumeration constant */
    int EN_RELATIVE = 110;
    /** Enumeration constant */
    int EN_RELATIVE_COLOMETRIC = 111;
    /** Enumeration constant */
    int EN_REPEAT = 112;
    /** Enumeration constant */
    int EN_REPEATX = 113;
    /** Enumeration constant */
    int EN_REPEATY = 114;
    /** Enumeration constant */
    int EN_RESAMPLE_ANY_METHOD = 115;
    /** Enumeration constant */
    int EN_RESET_SIZE = 116;
    /** Enumeration constant */
    int EN_REST = 117;
    /** Enumeration constant */
    int EN_RETAIN = 118;
    /** Enumeration constant */
    int EN_RIDGE = 119;
    /** Enumeration constant */
    int EN_RIGHT = 120;
    /** Enumeration constant */
    int EN_RL_TB = 121;
    /** Enumeration constant */
    int EN_RTL = 122;
    /** Enumeration constant */
    int EN_RULE = 123;
    /** Enumeration constant */
    int EN_SATURATION = 124;
    /** Enumeration constant */
    int EN_SCALE_TO_FIT = 125;
    /** Enumeration constant */
    int EN_SCROLL = 126;
    /** Enumeration constant */
    int EN_SEMI_CONDENSED = 127;
    /** Enumeration constant */
    int EN_SEMI_EXPANDED = 128;
    /** Enumeration constant */
    int EN_SEPARATE = 129;
    /** Enumeration constant */
    int EN_SHOW = 130;
    /** Enumeration constant */
    int EN_SMALL_CAPS = 131;
    /** Enumeration constant */
    int EN_SMALLER = 132;
    /** Enumeration constant */
    int EN_SOLID = 133;
    /** Enumeration constant */
    int EN_SPACE = 134;
    /** Enumeration constant */
    int EN_START = 135;
    /** Enumeration constant */
    int EN_STATIC = 136;
    /** Enumeration constant */
    int EN_SUB = 137;
    /** Enumeration constant */
    int EN_SUPER = 138;
    /** Enumeration constant */
    int EN_SUPPRESS = 139;
    /** Enumeration constant */
    int EN_TB_RL = 140;
    /** Enumeration constant */
    int EN_TEXT_AFTER_EDGE = 141;
    /** Enumeration constant */
    int EN_TEXT_BEFORE_EDGE = 142;
    /** Enumeration constant */
    int EN_TEXT_BOTTOM = 143;
    /** Enumeration constant */
    int EN_TEXT_TOP = 144;
    /** Enumeration constant */
    int EN_TOP = 145;
    /** Enumeration constant */
    int EN_TRADITIONAL = 146;
    /** Enumeration constant */
    int EN_TREAT_AS_SPACE = 147;
    /** Enumeration constant */
    int EN_TREAT_AS_ZERO_WIDTH_SPACE = 148;
    /** Enumeration constant */
    int EN_TRUE = 149;
    /** Enumeration constant */
    int EN_ULTRA_CONDENSED = 150;
    /** Enumeration constant */
    int EN_ULTRA_EXPANDED = 151;
    /** Enumeration constant */
    int EN_UNBOUNDED = 152;
    /** Enumeration constant */
    int EN_UNDERLINE = 153;
    /** Enumeration constant */
    int EN_UNIFORM = 154;
    /** Enumeration constant */
    int EN_UPPERCASE = 155;
    /** Enumeration constant */
    int EN_USE_FONT_METRICS = 156;
    /** Enumeration constant */
    int EN_USE_SCRIPT = 157;
    /** Enumeration constant */
    int EN_USECONTENT = 158;
    /** Enumeration constant */
    int EN_VISIBLE = 159;
    /** Enumeration constant */
    int EN_WIDER = 160;
    /** Enumeration constant */
    int EN_WRAP = 161;
    /** Enumeration constant - non-standard for display-align */
    int EN_X_FILL = 162;
    /** Enumeration constant - non-standard for display-align */
    int EN_X_DISTRIBUTE = 163;
    /** Enumeration constant */
    int EN_ITALIC = 164;
    /** Enumeration constant */
    int EN_OBLIQUE = 165;
    /** Enumeration constant */
    int EN_BACKSLANT = 166;
    /** Enumeration constant */
    int EN_BOLDER = 167;
    /** Enumeration constant */
    int EN_LIGHTER = 168;
    /** Enumeration constant */
    int EN_100 = 169;
    /** Enumeration constant */
    int EN_200 = 170;
    /** Enumeration constant */
    int EN_300 = 171;
    /** Enumeration constant */
    int EN_400 = 172;
    /** Enumeration constant */
    int EN_500 = 173;
    /** Enumeration constant */
    int EN_600 = 174;
    /** Enumeration constant */
    int EN_700 = 175;
    /** Enumeration constant */
    int EN_800 = 176;
    /** Enumeration constant */
    int EN_900 = 177;
    /** Enumeration constant -- page-break-shorthand */
    int EN_AVOID = 178;
    /** Enumeration constant -- white-space shorthand */
    int EN_PRE = 179;
    /** Enumeration constant -- font shorthand */
    int EN_CAPTION = 180;
    /** Enumeration constant -- font shorthand */
    int EN_ICON = 181;
    /** Enumeration constant -- font shorthand */
    int EN_MENU = 182;
    /** Enumeration constant -- font shorthand */
    int EN_MESSAGE_BOX = 183;
    /** Enumeration constant -- font shorthand */
    int EN_SMALL_CAPTION = 184;
    /** Enumeration constant -- font shorthand */
    int EN_STATUS_BAR = 185;
    /** Enumeration constant -- for page-position, XSL 1.1 */
    int EN_ONLY = 186;
    /** Enumeration constant -- for instream-foreign-object and external-graphic, XSL 1.1 */
    int EN_SCALE_DOWN_TO_FIT = 187;
    /** Enumeration constant -- for instream-foreign-object and external-graphic, XSL 1.1 */
    int EN_SCALE_UP_TO_FIT = 188;
    /** Enumeration constant -- for fo:basic-link show-destination */
    int EN_REPLACE = 189;
    /** Enumeration constant -- for fo:basic-link show-destination */
    int EN_NEW = 190;
    /** Enumeration constant -- for fo:retrieve-table-marker */
    int EN_FIRST_STARTING = 191;
    /** Enumeration constant -- for fo:retrieve-table-marker */
    int EN_LAST_STARTING = 192;
    /** Enumeration constant -- for fo:retrieve-table-marker */
    int EN_LAST_ENDING = 193;
    /** Enumeration constant -- for fo:retrieve-table-marker */
    int EN_TABLE = 194;
    /** Enumeration constant -- for fo:retrieve-table-marker */
    int EN_TABLE_FRAGMENT = 195;
    /** Enumeration constant -- XSL 1.1 */
    int EN_MERGE = 196;
    /** Enumeration constant -- XSL 1.1 */
    int EN_LEAVE_SEPARATE = 197;
    /** Enumeration constant -- XSL 1.1 */
    int EN_LINK = 198;
    /** Enumeration constant -- XSL 1.1 */
    int EN_NO_LINK = 199;
    /** Enumeration constant -- XSL 1.1 */
    int EN_ALTERNATE = 200;
    /** Enumeration constant -- for *-direction traits */
    int EN_LR = 201; // left to right
    /** Enumeration constant -- for *-direction traits */
    int EN_RL = 202; // right to left
    /** Enumeration constant -- for *-direction traits */
    int EN_TB = 203; // top to bottom
    /** Enumeration constant -- for *-direction traits */
    int EN_BT = 204; // bottom to top
    /** Enumeration constant */
    int EN_TB_LR = 205; // for top-to-bottom, left-to-right writing mode
    /** Number of enumeration constants defined */
    int ENUM_COUNT = 205;
}
