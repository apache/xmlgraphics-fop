/*
 * $Id$
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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

import java.util.HashMap;

import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.expr.PropertyException;

/**
 * A class of constants; an array of all property names and the constants
 * by which to refer to them.
 */

public class PropNames {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /*
     * List of property constants in property processing order -
     * FONT, FONT_SIZE first
     * Shorthands must precede any of their expansion elements.
     * Compounds must precede any of their components.
     * The list of property constants can be regenerated in XEmacs by setting
     * the region on the list of constants. (C-Space at the beginning,
     * move to last line, C-x C-x to exchange mark and point.)  Then run
     * a shell command on the region with replacement (M-1 M-|).  Use
     * the perl command:
     * perl -p -e 'BEGIN{$n=0};$n++ if s/= [0-9]+,/= $n,/'
     * Alternatively, start at a given point in the list by setting the
     * appropriate start value for $n.
     *
     * in vi, set mark `a' at the last line and
     * !'aperl... etc
     */
     /** Constant for matching property defined in <i>XSLFO</i>. */

    public static final int
                                    NO_PROPERTY = 0,
            // Properties setting up environment for from-table-column(),
            // e.g. font-size = from-table-column()
                                  COLUMN_NUMBER = 1,
                         NUMBER_COLUMNS_SPANNED = 2,
            // Properties setting font-size first
            // Shorthand first
                                           FONT = 3,
                                      FONT_SIZE = 4,
            // Writing mode early for handling of corresponding values
                                   WRITING_MODE = 5,
            // All other shorthands
                                     BACKGROUND = 6,
                            BACKGROUND_POSITION = 7,
                                         BORDER = 8,
                                   BORDER_COLOR = 9,
                                   BORDER_STYLE = 10,
                                   BORDER_WIDTH = 11,
                                  BORDER_BOTTOM = 12,
                                    BORDER_LEFT = 13,
                                   BORDER_RIGHT = 14,
                                     BORDER_TOP = 15,
                                 BORDER_SPACING = 16,
                                            CUE = 17,
                                         MARGIN = 18,
                                        PADDING = 19,
                               PAGE_BREAK_AFTER = 20,
                              PAGE_BREAK_BEFORE = 21,
                              PAGE_BREAK_INSIDE = 22,
                                          PAUSE = 23,
                                       POSITION = 24,
                                           SIZE = 25,
                                 VERTICAL_ALIGN = 26,
                                    WHITE_SPACE = 27,
                                       XML_LANG = 28,
            // Non-shorthand properties
            // Within these, compounds precede their components
            // and corresponding relative properties
            // precede corresponding absolute properties
                              ABSOLUTE_POSITION = 29,
                                   ACTIVE_STATE = 30,
                               ALIGNMENT_ADJUST = 31,
                             ALIGNMENT_BASELINE = 32,
                                   AUTO_RESTORE = 33,
                                        AZIMUTH = 34,
                          BACKGROUND_ATTACHMENT = 35,
                               BACKGROUND_COLOR = 36,
                               BACKGROUND_IMAGE = 37,
                 BACKGROUND_POSITION_HORIZONTAL = 38,
                   BACKGROUND_POSITION_VERTICAL = 39,
                              BACKGROUND_REPEAT = 40,
                                 BASELINE_SHIFT = 41,
                             BLANK_OR_NOT_BLANK = 42,
                    BLOCK_PROGRESSION_DIMENSION = 43,
            BLOCK_PROGRESSION_DIMENSION_MINIMUM = 44,
            BLOCK_PROGRESSION_DIMENSION_OPTIMUM = 45,
            BLOCK_PROGRESSION_DIMENSION_MAXIMUM = 46,

        // Border corresponding properties
                             BORDER_AFTER_COLOR = 47,
                        BORDER_AFTER_PRECEDENCE = 48,
                             BORDER_AFTER_STYLE = 49,
                             BORDER_AFTER_WIDTH = 50,
                      BORDER_AFTER_WIDTH_LENGTH = 51,
              BORDER_AFTER_WIDTH_CONDITIONALITY = 52,
                            BORDER_BEFORE_COLOR = 53,
                       BORDER_BEFORE_PRECEDENCE = 54,
                            BORDER_BEFORE_STYLE = 55,
                            BORDER_BEFORE_WIDTH = 56,
                     BORDER_BEFORE_WIDTH_LENGTH = 57,
             BORDER_BEFORE_WIDTH_CONDITIONALITY = 58,
                               BORDER_END_COLOR = 59,
                          BORDER_END_PRECEDENCE = 60,
                               BORDER_END_STYLE = 61,
                               BORDER_END_WIDTH = 62,
                        BORDER_END_WIDTH_LENGTH = 63,
                BORDER_END_WIDTH_CONDITIONALITY = 64,
                             BORDER_START_COLOR = 65,
                        BORDER_START_PRECEDENCE = 66,
                             BORDER_START_STYLE = 67,
                             BORDER_START_WIDTH = 68,
                      BORDER_START_WIDTH_LENGTH = 69,
              BORDER_START_WIDTH_CONDITIONALITY = 70,

                            BORDER_BOTTOM_COLOR = 71,
                            BORDER_BOTTOM_STYLE = 72,
                            BORDER_BOTTOM_WIDTH = 73,
                              BORDER_LEFT_COLOR = 74,
                              BORDER_LEFT_STYLE = 75,
                              BORDER_LEFT_WIDTH = 76,
                             BORDER_RIGHT_COLOR = 77,
                             BORDER_RIGHT_STYLE = 78,
                             BORDER_RIGHT_WIDTH = 79,
                               BORDER_TOP_COLOR = 80,
                               BORDER_TOP_STYLE = 81,
                               BORDER_TOP_WIDTH = 82,

                                BORDER_COLLAPSE = 83,
                              BORDER_SEPARATION = 84,
  BORDER_SEPARATION_BLOCK_PROGRESSION_DIRECTION = 85,
 BORDER_SEPARATION_INLINE_PROGRESSION_DIRECTION = 86,
                                         BOTTOM = 87,
                                    BREAK_AFTER = 88,
                                   BREAK_BEFORE = 89,
                                   CAPTION_SIDE = 90,
                                      CASE_NAME = 91,
                                     CASE_TITLE = 92,
                                      CHARACTER = 93,
                                          CLEAR = 94,
                                           CLIP = 95,
                                          COLOR = 96,
                             COLOR_PROFILE_NAME = 97,
                                   COLUMN_COUNT = 98,
                                     COLUMN_GAP = 99,
                                   COLUMN_WIDTH = 100,
                                 CONTENT_HEIGHT = 101,
                                   CONTENT_TYPE = 102,
                                  CONTENT_WIDTH = 103,
                                        COUNTRY = 104,
                                      CUE_AFTER = 105,
                                     CUE_BEFORE = 106,
                   DESTINATION_PLACEMENT_OFFSET = 107,
                                      DIRECTION = 108,
                                  DISPLAY_ALIGN = 109,
                              DOMINANT_BASELINE = 110,
                                      ELEVATION = 111,
                                    EMPTY_CELLS = 112,
                                     END_INDENT = 113,
                                       ENDS_ROW = 114,
                                         EXTENT = 115,
                           EXTERNAL_DESTINATION = 116,
                                          FLOAT = 117,
                                      FLOW_NAME = 118,
                            FLOW_NAME_REFERENCE = 119,
                                  FLOW_MAP_NAME = 120,
                             FLOW_MAP_REFERENCE = 121,
                                    FONT_FAMILY = 122,
                        FONT_SELECTION_STRATEGY = 123,
                               FONT_SIZE_ADJUST = 124,
                                   FONT_STRETCH = 125,
                                     FONT_STYLE = 126,
                                   FONT_VARIANT = 127,
                                    FONT_WEIGHT = 128,
                               FORCE_PAGE_COUNT = 129,
                                         FORMAT = 130,
                   GLYPH_ORIENTATION_HORIZONTAL = 131,
                     GLYPH_ORIENTATION_VERTICAL = 132,
                             GROUPING_SEPARATOR = 133,
                                  GROUPING_SIZE = 134,
                                         HEIGHT = 135,
                                      HYPHENATE = 136,
                          HYPHENATION_CHARACTER = 137,
                               HYPHENATION_KEEP = 138,
                       HYPHENATION_LADDER_COUNT = 139,
               HYPHENATION_PUSH_CHARACTER_COUNT = 140,
             HYPHENATION_REMAIN_CHARACTER_COUNT = 141,
                                             ID = 142,
                           INDICATE_DESTINATION = 143,
                            INITIAL_PAGE_NUMBER = 144,
                   INLINE_PROGRESSION_DIMENSION = 145,
           INLINE_PROGRESSION_DIMENSION_MINIMUM = 146,
           INLINE_PROGRESSION_DIMENSION_OPTIMUM = 147,
           INLINE_PROGRESSION_DIMENSION_MAXIMUM = 148,
                           INTERNAL_DESTINATION = 149,
                             INTRUSION_DISPLACE = 150,
                                  KEEP_TOGETHER = 151,
                      KEEP_TOGETHER_WITHIN_LINE = 152,
                      KEEP_TOGETHER_WITHIN_PAGE = 153,
                    KEEP_TOGETHER_WITHIN_COLUMN = 154,
                                 KEEP_WITH_NEXT = 155,
                     KEEP_WITH_NEXT_WITHIN_LINE = 156,
                     KEEP_WITH_NEXT_WITHIN_PAGE = 157,
                   KEEP_WITH_NEXT_WITHIN_COLUMN = 158,
                             KEEP_WITH_PREVIOUS = 159,
                 KEEP_WITH_PREVIOUS_WITHIN_LINE = 160,
                 KEEP_WITH_PREVIOUS_WITHIN_PAGE = 161,
               KEEP_WITH_PREVIOUS_WITHIN_COLUMN = 162,
                                       LANGUAGE = 163,
                           LAST_LINE_END_INDENT = 164,
                               LEADER_ALIGNMENT = 165,
                                  LEADER_LENGTH = 166,
                          LEADER_LENGTH_MINIMUM = 167,
                          LEADER_LENGTH_OPTIMUM = 168,
                          LEADER_LENGTH_MAXIMUM = 169,
                                 LEADER_PATTERN = 170,
                           LEADER_PATTERN_WIDTH = 171,
                                           LEFT = 172,
                                 LETTER_SPACING = 173,
                         LETTER_SPACING_MINIMUM = 174,
                         LETTER_SPACING_OPTIMUM = 175,
                         LETTER_SPACING_MAXIMUM = 176,
                  LETTER_SPACING_CONDITIONALITY = 177,
                      LETTER_SPACING_PRECEDENCE = 178,
                                   LETTER_VALUE = 179,
                             LINEFEED_TREATMENT = 180,
                                    LINE_HEIGHT = 181,
                            LINE_HEIGHT_MINIMUM = 182,
                            LINE_HEIGHT_OPTIMUM = 183,
                            LINE_HEIGHT_MAXIMUM = 184,
                     LINE_HEIGHT_CONDITIONALITY = 185,
                         LINE_HEIGHT_PRECEDENCE = 186,
                   LINE_HEIGHT_SHIFT_ADJUSTMENT = 187,
                         LINE_STACKING_STRATEGY = 188,

                              MARKER_CLASS_NAME = 189,
                                    MASTER_NAME = 190,
                               MASTER_REFERENCE = 191,
                                     MAX_HEIGHT = 192,
                                MAXIMUM_REPEATS = 193,
                                      MAX_WIDTH = 194,
                                    MEDIA_USAGE = 195,
                                     MIN_HEIGHT = 196,
                                      MIN_WIDTH = 197,
                        NUMBER_COLUMNS_REPEATED = 198,
                            NUMBER_ROWS_SPANNED = 199,
                                    ODD_OR_EVEN = 200,
                                        ORPHANS = 201,
                                       OVERFLOW = 202,

        // Padding corresponding properties
                                  PADDING_AFTER = 203,
                           PADDING_AFTER_LENGTH = 204,
                   PADDING_AFTER_CONDITIONALITY = 205,
                                 PADDING_BEFORE = 206,
                          PADDING_BEFORE_LENGTH = 207,
                  PADDING_BEFORE_CONDITIONALITY = 208,
                                    PADDING_END = 209,
                             PADDING_END_LENGTH = 210,
                     PADDING_END_CONDITIONALITY = 211,
                                  PADDING_START = 212,
                           PADDING_START_LENGTH = 213,
                   PADDING_START_CONDITIONALITY = 214,

                                 PADDING_BOTTOM = 215,
                                   PADDING_LEFT = 216,
                                  PADDING_RIGHT = 217,
                                    PADDING_TOP = 218,

                                    PAGE_HEIGHT = 219,
                                  PAGE_POSITION = 220,
                                     PAGE_WIDTH = 221,
                                    PAUSE_AFTER = 222,
                                   PAUSE_BEFORE = 223,
                                          PITCH = 224,
                                    PITCH_RANGE = 225,
                                    PLAY_DURING = 226,
                                     PRECEDENCE = 227,
            PROVISIONAL_DISTANCE_BETWEEN_STARTS = 228,
                   PROVISIONAL_LABEL_SEPARATION = 229,
                          REFERENCE_ORIENTATION = 230,
                                         REF_ID = 231,
                                    REGION_NAME = 232,
                          REGION_NAME_REFERENCE = 233,
                                 RELATIVE_ALIGN = 234,
                              RELATIVE_POSITION = 235,
                               RENDERING_INTENT = 236,
                              RETRIEVE_BOUNDARY = 237,
                            RETRIEVE_CLASS_NAME = 238,
                              RETRIEVE_POSITION = 239,
                                       RICHNESS = 240,
                                          RIGHT = 241,
                                           ROLE = 242,
                                     RULE_STYLE = 243,
                                 RULE_THICKNESS = 244,
                                        SCALING = 245,
                                 SCALING_METHOD = 246,
                                   SCORE_SPACES = 247,
                                         SCRIPT = 248,
                               SHOW_DESTINATION = 249,
                                SOURCE_DOCUMENT = 250,

        // Space/margin corresponding properties
                                    SPACE_AFTER = 251,
                            SPACE_AFTER_MINIMUM = 252,
                            SPACE_AFTER_OPTIMUM = 253,
                            SPACE_AFTER_MAXIMUM = 254,
                     SPACE_AFTER_CONDITIONALITY = 255,
                         SPACE_AFTER_PRECEDENCE = 256,
                                   SPACE_BEFORE = 257,
                           SPACE_BEFORE_MINIMUM = 258,
                           SPACE_BEFORE_OPTIMUM = 259,
                           SPACE_BEFORE_MAXIMUM = 260,
                    SPACE_BEFORE_CONDITIONALITY = 261,
                        SPACE_BEFORE_PRECEDENCE = 262,
                                      SPACE_END = 263,
                              SPACE_END_MINIMUM = 264,
                              SPACE_END_OPTIMUM = 265,
                              SPACE_END_MAXIMUM = 266,
                       SPACE_END_CONDITIONALITY = 267,
                           SPACE_END_PRECEDENCE = 268,
                                    SPACE_START = 269,
                            SPACE_START_MINIMUM = 270,
                            SPACE_START_OPTIMUM = 271,
                            SPACE_START_MAXIMUM = 272,
                     SPACE_START_CONDITIONALITY = 273,
                         SPACE_START_PRECEDENCE = 274,

                                  MARGIN_BOTTOM = 275,
                                    MARGIN_LEFT = 276,
                                   MARGIN_RIGHT = 277,
                                     MARGIN_TOP = 278,

                                           SPAN = 279,
                                          SPEAK = 280,
                                   SPEAK_HEADER = 281,
                                  SPEAK_NUMERAL = 282,
                              SPEAK_PUNCTUATION = 283,
                                    SPEECH_RATE = 284,
                                            SRC = 285,
                                   START_INDENT = 286,
                                 STARTING_STATE = 287,
                                     STARTS_ROW = 288,
                                         STRESS = 289,
                         SUPPRESS_AT_LINE_BREAK = 290,
                                      SWITCH_TO = 291,
                                   TABLE_LAYOUT = 292,
                     TABLE_OMIT_FOOTER_AT_BREAK = 293,
                     TABLE_OMIT_HEADER_AT_BREAK = 294,
                    TARGET_PRESENTATION_CONTEXT = 295,
                      TARGET_PROCESSING_CONTEXT = 296,
                              TARGET_STYLESHEET = 297,
                                     TEXT_ALIGN = 298,
                                TEXT_ALIGN_LAST = 299,
                                  TEXT_ALTITUDE = 300,
                                TEXT_DECORATION = 301,
                                     TEXT_DEPTH = 302,
                                    TEXT_INDENT = 303,
                                    TEXT_SHADOW = 304,
                                 TEXT_TRANSFORM = 305,
                                            TOP = 306,
                            TREAT_AS_WORD_SPACE = 307,
                                   UNICODE_BIDI = 308,
                                     VISIBILITY = 309,
                                   VOICE_FAMILY = 310,
                                         VOLUME = 311,
                           WHITE_SPACE_COLLAPSE = 312,
                          WHITE_SPACE_TREATMENT = 313,
                                         WIDOWS = 314,
                                          WIDTH = 315,
                                   WORD_SPACING = 316,
                           WORD_SPACING_MINIMUM = 317,
                           WORD_SPACING_OPTIMUM = 318,
                           WORD_SPACING_MAXIMUM = 319,
                    WORD_SPACING_CONDITIONALITY = 320,
                        WORD_SPACING_PRECEDENCE = 321,
                                    WRAP_OPTION = 322,
                                        Z_INDEX = 323,
        
                            LAST_PROPERTY_INDEX = Z_INDEX;
    // TODO specify last corresponding accurately and possibly re-organize to
    // group all corresponding properties early in the list
    public static final int
                       LAST_CORRESPONDING_INDEX = Z_INDEX;


    /**
     * A String[] array containing the names of all of the FO properties.
     * The array is effectively 1-based, as the first element is null.
     * The list of int constants referring to the properties must be manually
     * kept in sync with the names in this array, as the constants can be
     * used to index into this, and the other property arrays.
     */

    private static final String[] propertyNames = {
                                        "no-property"  // 0

                                     ,"column-number"  // 1
                            ,"number-columns-spanned"  // 2

                                              ,"font"  // 3
                                         ,"font-size"  // 4

                                      ,"writing-mode"  // 5

                                        ,"background"  // 6
                               ,"background-position"  // 7
                                            ,"border"  // 8
                                      ,"border-color"  // 9
                                      ,"border-style"  // 10
                                      ,"border-width"  // 11
                                     ,"border-bottom"  // 12
                                       ,"border-left"  // 13
                                      ,"border-right"  // 14
                                        ,"border-top"  // 15
                                    ,"border-spacing"  // 16
                                               ,"cue"  // 17
                                            ,"margin"  // 18
                                           ,"padding"  // 19
                                  ,"page-break-after"  // 20
                                 ,"page-break-before"  // 21
                                 ,"page-break-inside"  // 22
                                             ,"pause"  // 23
                                          ,"position"  // 24
                                              ,"size"  // 25
                                    ,"vertical-align"  // 26
                                       ,"white-space"  // 27
                                          ,"xml:lang"  // 28

                                 ,"absolute-position"  // 29
                                      ,"active-state"  // 30
                                  ,"alignment-adjust"  // 31
                                ,"alignment-baseline"  // 32
                                      ,"auto-restore"  // 33
                                           ,"azimuth"  // 34
                             ,"background-attachment"  // 35
                                  ,"background-color"  // 36
                                  ,"background-image"  // 37
                    ,"background-position-horizontal"  // 38
                      ,"background-position-vertical"  // 39
                                 ,"background-repeat"  // 40
                                    ,"baseline-shift"  // 41
                                ,"blank-or-not-blank"  // 42
                       ,"block-progression-dimension"  // 43
               ,"block-progression-dimension.minimum"  // 44
               ,"block-progression-dimension.optimum"  // 45
               ,"block-progression-dimension.maximum"  // 46

                                ,"border-after-color"  // 47
                           ,"border-after-precedence"  // 48
                                ,"border-after-style"  // 49
                                ,"border-after-width"  // 50
                         ,"border-after-width.length"  // 51
                 ,"border-after-width.conditionality"  // 52
                               ,"border-before-color"  // 53
                          ,"border-before-precedence"  // 54
                               ,"border-before-style"  // 55
                               ,"border-before-width"  // 56
                        ,"border-before-width.length"  // 57
                ,"border-before-width.conditionality"  // 58
                                  ,"border-end-color"  // 59
                             ,"border-end-precedence"  // 60
                                  ,"border-end-style"  // 61
                                  ,"border-end-width"  // 62
                           ,"border-end-width.length"  // 63
                   ,"border-end-width.conditionality"  // 64
                                ,"border-start-color"  // 65
                           ,"border-start-precedence"  // 66
                                ,"border-start-style"  // 67
                                ,"border-start-width"  // 68
                         ,"border-start-width.length"  // 69
                 ,"border-start-width.conditionality"  // 70

                               ,"border-bottom-color"  // 71
                               ,"border-bottom-style"  // 72
                               ,"border-bottom-width"  // 73
                                 ,"border-left-color"  // 74
                                 ,"border-left-style"  // 75
                                 ,"border-left-width"  // 76
                                ,"border-right-color"  // 77
                                ,"border-right-style"  // 78
                                ,"border-right-width"  // 79
                                  ,"border-top-color"  // 80
                                  ,"border-top-style"  // 81
                                  ,"border-top-width"  // 82

                                   ,"border-collapse"  // 83
                                 ,"border-separation"  // 84
     ,"border-separation.block-progression-direction"  // 85
    ,"border-separation.inline-progression-direction"  // 86
                                            ,"bottom"  // 87
                                       ,"break-after"  // 88
                                      ,"break-before"  // 89
                                      ,"caption-side"  // 90
                                         ,"case-name"  // 91
                                        ,"case-title"  // 92
                                         ,"character"  // 93
                                             ,"clear"  // 94
                                              ,"clip"  // 95
                                             ,"color"  // 96
                                ,"color-profile-name"  // 97
                                      ,"column-count"  // 98
                                        ,"column-gap"  // 99
                                      ,"column-width"  // 100
                                    ,"content-height"  // 101
                                      ,"content-type"  // 102
                                     ,"content-width"  // 103
                                           ,"country"  // 104
                                         ,"cue-after"  // 105
                                        ,"cue-before"  // 106
                      ,"destination-placement-offset"  // 107
                                         ,"direction"  // 108
                                     ,"display-align"  // 109
                                 ,"dominant-baseline"  // 110
                                         ,"elevation"  // 111
                                       ,"empty-cells"  // 112
                                        ,"end-indent"  // 113
                                          ,"ends-row"  // 114
                                            ,"extent"  // 115
                              ,"external-destination"  // 116
                                             ,"float"  // 117
                                         ,"flow-name"  // 118
                               ,"flow-name-reference"  // 119
                                     ,"flow-map-name"  // 120
                                ,"flow-map-reference"  // 121
                                       ,"font-family"  // 122
                           ,"font-selection-strategy"  // 123
                                  ,"font-size-adjust"  // 124
                                      ,"font-stretch"  // 125
                                        ,"font-style"  // 126
                                      ,"font-variant"  // 127
                                       ,"font-weight"  // 128
                                  ,"force-page-count"  // 129
                                            ,"format"  // 130
                      ,"glyph-orientation-horizontal"  // 131
                        ,"glyph-orientation-vertical"  // 132
                                ,"grouping-separator"  // 133
                                     ,"grouping-size"  // 134
                                            ,"height"  // 135
                                         ,"hyphenate"  // 136
                             ,"hyphenation-character"  // 137
                                  ,"hyphenation-keep"  // 138
                          ,"hyphenation-ladder-count"  // 139
                  ,"hyphenation-push-character-count"  // 140
                ,"hyphenation-remain-character-count"  // 141
                                                ,"id"  // 142
                              ,"indicate-destination"  // 143
                               ,"initial-page-number"  // 144
                      ,"inline-progression-dimension"  // 145
              ,"inline-progression-dimension.minimum"  // 146
              ,"inline-progression-dimension.optimum"  // 147
              ,"inline-progression-dimension.maximum"  // 148
                              ,"internal-destination"  // 149
                                ,"intrusion-displace"  // 150
                                     ,"keep-together"  // 151
                         ,"keep-together.within-line"  // 152
                       ,"keep-together.within-column"  // 153
                         ,"keep-together.within-page"  // 154
                                    ,"keep-with-next"  // 155
                        ,"keep-with-next.within-line"  // 156
                      ,"keep-with-next.within-column"  // 157
                        ,"keep-with-next.within-page"  // 158
                                ,"keep-with-previous"  // 159
                    ,"keep-with-previous.within-line"  // 160
                  ,"keep-with-previous.within-column"  // 161
                    ,"keep-with-previous.within-page"  // 162
                                          ,"language"  // 163
                              ,"last-line-end-indent"  // 164
                                  ,"leader-alignment"  // 165
                                     ,"leader-length"  // 166
                             ,"leader-length.minimum"  // 167
                             ,"leader-length.optimum"  // 168
                             ,"leader-length.maximum"  // 169
                                    ,"leader-pattern"  // 170
                              ,"leader-pattern-width"  // 171
                                              ,"left"  // 172
                                    ,"letter-spacing"  // 173
                            ,"letter-spacing.minimum"  // 174
                            ,"letter-spacing.optimum"  // 175
                            ,"letter-spacing.maximum"  // 176
                     ,"letter-spacing.conditionality"  // 177
                         ,"letter-spacing.precedence"  // 178
                                      ,"letter-value"  // 179
                                ,"linefeed-treatment"  // 180
                                       ,"line-height"  // 181
                               ,"line-height.minimum"  // 182
                               ,"line-height.optimum"  // 183
                               ,"line-height.maximum"  // 184
                        ,"line-height.conditionality"  // 185
                            ,"line-height.precedence"  // 186
                      ,"line-height-shift-adjustment"  // 187
                            ,"line-stacking-strategy"  // 188

                                 ,"marker-class-name"  // 189
                                       ,"master-name"  // 190
                                  ,"master-reference"  // 191
                                        ,"max-height"  // 192
                                   ,"maximum-repeats"  // 193
                                         ,"max-width"  // 194
                                       ,"media-usage"  // 195
                                        ,"min-height"  // 196
                                         ,"min-width"  // 197
                           ,"number-columns-repeated"  // 198
                               ,"number-rows-spanned"  // 199
                                       ,"odd-or-even"  // 200
                                           ,"orphans"  // 201
                                          ,"overflow"  // 202

                                     ,"padding-after"  // 203
                              ,"padding-after.length"  // 204
                      ,"padding-after.conditionality"  // 205
                                    ,"padding-before"  // 206
                             ,"padding-before.length"  // 207
                     ,"padding-before.conditionality"  // 208
                                       ,"padding-end"  // 209
                                ,"padding-end.length"  // 210
                        ,"padding-end.conditionality"  // 211
                                     ,"padding-start"  // 212
                              ,"padding-start.length"  // 213
                      ,"padding-start.conditionality"  // 214

                                    ,"padding-bottom"  // 215
                                      ,"padding-left"  // 216
                                     ,"padding-right"  // 217
                                       ,"padding-top"  // 218

                                       ,"page-height"  // 219
                                     ,"page-position"  // 220
                                        ,"page-width"  // 221
                                       ,"pause-after"  // 222
                                      ,"pause-before"  // 223
                                             ,"pitch"  // 224
                                       ,"pitch-range"  // 225
                                       ,"play-during"  // 226
                                        ,"precedence"  // 227
               ,"provisional-distance-between-starts"  // 228
                      ,"provisional-label-separation"  // 229
                             ,"reference-orientation"  // 230
                                            ,"ref-id"  // 231
                                       ,"region-name"  // 232
                             ,"region-name-reference"  // 233
                                    ,"relative-align"  // 234
                                 ,"relative-position"  // 235
                                  ,"rendering-intent"  // 236
                                 ,"retrieve-boundary"  // 237
                               ,"retrieve-class-name"  // 238
                                 ,"retrieve-position"  // 239
                                          ,"richness"  // 240
                                             ,"right"  // 241
                                              ,"role"  // 242
                                        ,"rule-style"  // 243
                                    ,"rule-thickness"  // 244
                                           ,"scaling"  // 245
                                    ,"scaling-method"  // 246
                                      ,"score-spaces"  // 247
                                            ,"script"  // 248
                                  ,"show-destination"  // 249
                                   ,"source-document"  // 250

                                       ,"space-after"  // 251
                               ,"space-after.minimum"  // 252
                               ,"space-after.optimum"  // 253
                               ,"space-after.maximum"  // 254
                        ,"space-after.conditionality"  // 255
                            ,"space-after.precedence"  // 256
                                      ,"space-before"  // 257
                              ,"space-before.minimum"  // 258
                              ,"space-before.optimum"  // 259
                              ,"space-before.maximum"  // 260
                       ,"space-before.conditionality"  // 261
                           ,"space-before.precedence"  // 262
                                         ,"space-end"  // 263
                                 ,"space-end.minimum"  // 264
                                 ,"space-end.optimum"  // 265
                                 ,"space-end.maximum"  // 266
                          ,"space-end.conditionality"  // 267
                              ,"space-end.precedence"  // 268
                                       ,"space-start"  // 269
                               ,"space-start.minimum"  // 270
                               ,"space-start.optimum"  // 271
                               ,"space-start.maximum"  // 272
                        ,"space-start.conditionality"  // 273
                            ,"space-start.precedence"  // 274

                                     ,"margin-bottom"  // 275
                                       ,"margin-left"  // 276
                                      ,"margin-right"  // 277
                                        ,"margin-top"  // 278

                                              ,"span"  // 279
                                             ,"speak"  // 280
                                      ,"speak-header"  // 281
                                     ,"speak-numeral"  // 282
                                 ,"speak-punctuation"  // 283
                                       ,"speech-rate"  // 284
                                               ,"src"  // 285
                                      ,"start-indent"  // 286
                                    ,"starting-state"  // 287
                                        ,"starts-row"  // 288
                                            ,"stress"  // 289
                            ,"suppress-at-line-break"  // 290
                                         ,"switch-to"  // 291
                                      ,"table-layout"  // 292
                        ,"table-omit-footer-at-break"  // 293
                        ,"table-omit-header-at-break"  // 294
                       ,"target-presentation-context"  // 295
                         ,"target-processing-context"  // 296
                                 ,"target-stylesheet"  // 297
                                        ,"text-align"  // 298
                                   ,"text-align-last"  // 299
                                     ,"text-altitude"  // 300
                                   ,"text-decoration"  // 301
                                        ,"text-depth"  // 302
                                       ,"text-indent"  // 303
                                       ,"text-shadow"  // 304
                                    ,"text-transform"  // 305
                                               ,"top"  // 306
                               ,"treat-as-word-space"  // 307
                                      ,"unicode-bidi"  // 308
                                        ,"visibility"  // 309
                                      ,"voice-family"  // 310
                                            ,"volume"  // 311
                              ,"white-space-collapse"  // 312
                             ,"white-space-treatment"  // 313
                                            ,"widows"  // 314
                                             ,"width"  // 315
                                      ,"word-spacing"  // 316
                              ,"word-spacing-minimum"  // 317
                              ,"word-spacing-optimum"  // 318
                              ,"word-spacing-maximum"  // 319
                       ,"word-spacing-conditionality"  // 320
                           ,"word-spacing-precedence"  // 321
                                       ,"wrap-option"  // 322
                                           ,"z-index"  // 323
    };

    /**
     * A <tt>hashMap</tt> mapping property names (the keys) to
     * property integer indices.
     */
    private static final HashMap toIndex;
    static {
        toIndex = new HashMap(
                (int)((LAST_PROPERTY_INDEX + 1) / 0.75) + 1);
        // Set up the toIndex Hashmap with the name of the
        // property as a key, and the integer index as a value
        for (int i = 0; i <= LAST_PROPERTY_INDEX; i++) {
            if (toIndex.put(propertyNames[i],
                                    Ints.consts.get(i)) != null) {
                throw new RuntimeException(
                    "Duplicate values in toIndex for key " +
                    propertyNames[i]);
            }
        }
    }

    /**
     * @param propindex <tt>int</tt> index of the FO property.
     * @return <tt>String</tt> name of the indexd FO property.
     * @exception PropertyException if the property index is invalid.
     */
    public static String getPropertyName(int propindex)
                throws PropertyException
    {
        if (propindex < 0 || propindex > LAST_PROPERTY_INDEX)
                throw new PropertyException
                        ("getPropertyName: index is invalid: " + propindex);
        return propertyNames[propindex];
    }

    /**
     * Get the property index of a property name.
     * @param name  of the FO property.
     * @return <tt>String</tt> name of the indexd FO property.
     * @exception PropertyException if the property index is invalid.
     */
    public static int getPropertyIndex(String name)
        throws PropertyException
    {
        Integer intg = (Integer)(toIndex.get(name));
        if (intg == null)
            throw new PropertyException
                                ("Property name '" + name + "' is unknown.");
        return intg.intValue();
    }

}
