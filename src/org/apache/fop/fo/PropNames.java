/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

import java.util.HashMap;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.Ints;

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
                                    FONT_FAMILY = 119,
                        FONT_SELECTION_STRATEGY = 120,
                               FONT_SIZE_ADJUST = 121,
                                   FONT_STRETCH = 122,
                                     FONT_STYLE = 123,
                                   FONT_VARIANT = 124,
                                    FONT_WEIGHT = 125,
                               FORCE_PAGE_COUNT = 126,
                                         FORMAT = 127,
                   GLYPH_ORIENTATION_HORIZONTAL = 128,
                     GLYPH_ORIENTATION_VERTICAL = 129,
                             GROUPING_SEPARATOR = 130,
                                  GROUPING_SIZE = 131,
                                         HEIGHT = 132,
                                      HYPHENATE = 133,
                          HYPHENATION_CHARACTER = 134,
                               HYPHENATION_KEEP = 135,
                       HYPHENATION_LADDER_COUNT = 136,
               HYPHENATION_PUSH_CHARACTER_COUNT = 137,
             HYPHENATION_REMAIN_CHARACTER_COUNT = 138,
                                             ID = 139,
                           INDICATE_DESTINATION = 140,
                            INITIAL_PAGE_NUMBER = 141,
                   INLINE_PROGRESSION_DIMENSION = 142,
           INLINE_PROGRESSION_DIMENSION_MINIMUM = 143,
           INLINE_PROGRESSION_DIMENSION_OPTIMUM = 144,
           INLINE_PROGRESSION_DIMENSION_MAXIMUM = 145,
                           INTERNAL_DESTINATION = 146,
                             INTRUSION_DISPLACE = 147,
                                  KEEP_TOGETHER = 148,
                      KEEP_TOGETHER_WITHIN_LINE = 149,
                      KEEP_TOGETHER_WITHIN_PAGE = 150,
                    KEEP_TOGETHER_WITHIN_COLUMN = 151,
                                 KEEP_WITH_NEXT = 152,
                     KEEP_WITH_NEXT_WITHIN_LINE = 153,
                     KEEP_WITH_NEXT_WITHIN_PAGE = 154,
                   KEEP_WITH_NEXT_WITHIN_COLUMN = 155,
                             KEEP_WITH_PREVIOUS = 156,
                 KEEP_WITH_PREVIOUS_WITHIN_LINE = 157,
                 KEEP_WITH_PREVIOUS_WITHIN_PAGE = 158,
               KEEP_WITH_PREVIOUS_WITHIN_COLUMN = 159,
                                       LANGUAGE = 160,
                           LAST_LINE_END_INDENT = 161,
                               LEADER_ALIGNMENT = 162,
                                  LEADER_LENGTH = 163,
                          LEADER_LENGTH_MINIMUM = 164,
                          LEADER_LENGTH_OPTIMUM = 165,
                          LEADER_LENGTH_MAXIMUM = 166,
                                 LEADER_PATTERN = 167,
                           LEADER_PATTERN_WIDTH = 168,
                                           LEFT = 169,
                                 LETTER_SPACING = 170,
                         LETTER_SPACING_MINIMUM = 171,
                         LETTER_SPACING_OPTIMUM = 172,
                         LETTER_SPACING_MAXIMUM = 173,
                  LETTER_SPACING_CONDITIONALITY = 174,
                      LETTER_SPACING_PRECEDENCE = 175,
                                   LETTER_VALUE = 176,
                             LINEFEED_TREATMENT = 177,
                                    LINE_HEIGHT = 178,
                            LINE_HEIGHT_MINIMUM = 179,
                            LINE_HEIGHT_OPTIMUM = 180,
                            LINE_HEIGHT_MAXIMUM = 181,
                     LINE_HEIGHT_CONDITIONALITY = 182,
                         LINE_HEIGHT_PRECEDENCE = 183,
                   LINE_HEIGHT_SHIFT_ADJUSTMENT = 184,
                         LINE_STACKING_STRATEGY = 185,

                              MARKER_CLASS_NAME = 186,
                                    MASTER_NAME = 187,
                               MASTER_REFERENCE = 188,
                                     MAX_HEIGHT = 189,
                                MAXIMUM_REPEATS = 190,
                                      MAX_WIDTH = 191,
                                    MEDIA_USAGE = 192,
                                     MIN_HEIGHT = 193,
                                      MIN_WIDTH = 194,
                        NUMBER_COLUMNS_REPEATED = 195,
                            NUMBER_ROWS_SPANNED = 196,
                                    ODD_OR_EVEN = 197,
                                        ORPHANS = 198,
                                       OVERFLOW = 199,

        // Padding corresponding properties
                                  PADDING_AFTER = 200,
                           PADDING_AFTER_LENGTH = 201,
                   PADDING_AFTER_CONDITIONALITY = 202,
                                 PADDING_BEFORE = 203,
                          PADDING_BEFORE_LENGTH = 204,
                  PADDING_BEFORE_CONDITIONALITY = 205,
                                    PADDING_END = 206,
                             PADDING_END_LENGTH = 207,
                     PADDING_END_CONDITIONALITY = 208,
                                  PADDING_START = 209,
                           PADDING_START_LENGTH = 210,
                   PADDING_START_CONDITIONALITY = 211,

                                 PADDING_BOTTOM = 212,
                                   PADDING_LEFT = 213,
                                  PADDING_RIGHT = 214,
                                    PADDING_TOP = 215,

                                    PAGE_HEIGHT = 216,
                                  PAGE_POSITION = 217,
                                     PAGE_WIDTH = 218,
                                    PAUSE_AFTER = 219,
                                   PAUSE_BEFORE = 220,
                                          PITCH = 221,
                                    PITCH_RANGE = 222,
                                    PLAY_DURING = 223,
                                     PRECEDENCE = 224,
            PROVISIONAL_DISTANCE_BETWEEN_STARTS = 225,
                   PROVISIONAL_LABEL_SEPARATION = 226,
                          REFERENCE_ORIENTATION = 227,
                                         REF_ID = 228,
                                    REGION_NAME = 229,
                                 RELATIVE_ALIGN = 230,
                              RELATIVE_POSITION = 231,
                               RENDERING_INTENT = 232,
                              RETRIEVE_BOUNDARY = 233,
                            RETRIEVE_CLASS_NAME = 234,
                              RETRIEVE_POSITION = 235,
                                       RICHNESS = 236,
                                          RIGHT = 237,
                                           ROLE = 238,
                                     RULE_STYLE = 239,
                                 RULE_THICKNESS = 240,
                                        SCALING = 241,
                                 SCALING_METHOD = 242,
                                   SCORE_SPACES = 243,
                                         SCRIPT = 244,
                               SHOW_DESTINATION = 245,
                                SOURCE_DOCUMENT = 246,

        // Space/margin corresponding properties
                                    SPACE_AFTER = 247,
                            SPACE_AFTER_MINIMUM = 248,
                            SPACE_AFTER_OPTIMUM = 249,
                            SPACE_AFTER_MAXIMUM = 250,
                     SPACE_AFTER_CONDITIONALITY = 251,
                         SPACE_AFTER_PRECEDENCE = 252,
                                   SPACE_BEFORE = 253,
                           SPACE_BEFORE_MINIMUM = 254,
                           SPACE_BEFORE_OPTIMUM = 255,
                           SPACE_BEFORE_MAXIMUM = 256,
                    SPACE_BEFORE_CONDITIONALITY = 257,
                        SPACE_BEFORE_PRECEDENCE = 258,
                                      SPACE_END = 259,
                              SPACE_END_MINIMUM = 260,
                              SPACE_END_OPTIMUM = 261,
                              SPACE_END_MAXIMUM = 262,
                       SPACE_END_CONDITIONALITY = 263,
                           SPACE_END_PRECEDENCE = 264,
                                    SPACE_START = 265,
                            SPACE_START_MINIMUM = 266,
                            SPACE_START_OPTIMUM = 267,
                            SPACE_START_MAXIMUM = 268,
                     SPACE_START_CONDITIONALITY = 269,
                         SPACE_START_PRECEDENCE = 270,

                                  MARGIN_BOTTOM = 271,
                                    MARGIN_LEFT = 272,
                                   MARGIN_RIGHT = 273,
                                     MARGIN_TOP = 274,

                                           SPAN = 275,
                                          SPEAK = 276,
                                   SPEAK_HEADER = 277,
                                  SPEAK_NUMERAL = 278,
                              SPEAK_PUNCTUATION = 279,
                                    SPEECH_RATE = 280,
                                            SRC = 281,
                                   START_INDENT = 282,
                                 STARTING_STATE = 283,
                                     STARTS_ROW = 284,
                                         STRESS = 285,
                         SUPPRESS_AT_LINE_BREAK = 286,
                                      SWITCH_TO = 287,
                                   TABLE_LAYOUT = 288,
                     TABLE_OMIT_FOOTER_AT_BREAK = 289,
                     TABLE_OMIT_HEADER_AT_BREAK = 290,
                    TARGET_PRESENTATION_CONTEXT = 291,
                      TARGET_PROCESSING_CONTEXT = 292,
                              TARGET_STYLESHEET = 293,
                                     TEXT_ALIGN = 294,
                                TEXT_ALIGN_LAST = 295,
                                  TEXT_ALTITUDE = 296,
                                TEXT_DECORATION = 297,
                                     TEXT_DEPTH = 298,
                                    TEXT_INDENT = 299,
                                    TEXT_SHADOW = 300,
                                 TEXT_TRANSFORM = 301,
                                            TOP = 302,
                            TREAT_AS_WORD_SPACE = 303,
                                   UNICODE_BIDI = 304,
        USAGE_CONTEXT_OF_SUPPRESS_AT_LINE_BREAK = 305,
                                     VISIBILITY = 306,
                                   VOICE_FAMILY = 307,
                                         VOLUME = 308,
                           WHITE_SPACE_COLLAPSE = 309,
                          WHITE_SPACE_TREATMENT = 310,
                                         WIDOWS = 311,
                                          WIDTH = 312,
                                   WORD_SPACING = 313,
                           WORD_SPACING_MINIMUM = 314,
                           WORD_SPACING_OPTIMUM = 315,
                           WORD_SPACING_MAXIMUM = 316,
                    WORD_SPACING_CONDITIONALITY = 317,
                        WORD_SPACING_PRECEDENCE = 318,
                                    WRAP_OPTION = 319,
                                        Z_INDEX = 320,
        
                            LAST_PROPERTY_INDEX = Z_INDEX;


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
                                       ,"font-family"  // 119
                           ,"font-selection-strategy"  // 120
                                  ,"font-size-adjust"  // 121
                                      ,"font-stretch"  // 122
                                        ,"font-style"  // 123
                                      ,"font-variant"  // 124
                                       ,"font-weight"  // 125
                                  ,"force-page-count"  // 126
                                            ,"format"  // 127
                      ,"glyph-orientation-horizontal"  // 128
                        ,"glyph-orientation-vertical"  // 129
                                ,"grouping-separator"  // 130
                                     ,"grouping-size"  // 131
                                            ,"height"  // 132
                                         ,"hyphenate"  // 133
                             ,"hyphenation-character"  // 134
                                  ,"hyphenation-keep"  // 135
                          ,"hyphenation-ladder-count"  // 136
                  ,"hyphenation-push-character-count"  // 137
                ,"hyphenation-remain-character-count"  // 138
                                                ,"id"  // 139
                              ,"indicate-destination"  // 140
                               ,"initial-page-number"  // 141
                      ,"inline-progression-dimension"  // 142
              ,"inline-progression-dimension.minimum"  // 143
              ,"inline-progression-dimension.optimum"  // 144
              ,"inline-progression-dimension.maximum"  // 145
                              ,"internal-destination"  // 146
                                ,"intrusion-displace"  // 147
                                     ,"keep-together"  // 148
                         ,"keep-together.within-line"  // 149
                       ,"keep-together.within-column"  // 150
                         ,"keep-together.within-page"  // 151
                                    ,"keep-with-next"  // 152
                        ,"keep-with-next.within-line"  // 153
                      ,"keep-with-next.within-column"  // 154
                        ,"keep-with-next.within-page"  // 155
                                ,"keep-with-previous"  // 156
                    ,"keep-with-previous.within-line"  // 157
                  ,"keep-with-previous.within-column"  // 158
                    ,"keep-with-previous.within-page"  // 159
                                          ,"language"  // 160
                              ,"last-line-end-indent"  // 161
                                  ,"leader-alignment"  // 162
                                     ,"leader-length"  // 163
                             ,"leader-length.minimum"  // 164
                             ,"leader-length.optimum"  // 165
                             ,"leader-length.maximum"  // 166
                                    ,"leader-pattern"  // 167
                              ,"leader-pattern-width"  // 168
                                              ,"left"  // 169
                                    ,"letter-spacing"  // 170
                            ,"letter-spacing.minimum"  // 171
                            ,"letter-spacing.optimum"  // 172
                            ,"letter-spacing.maximum"  // 173
                     ,"letter-spacing.conditionality"  // 174
                         ,"letter-spacing.precedence"  // 175
                                      ,"letter-value"  // 176
                                ,"linefeed-treatment"  // 177
                                       ,"line-height"  // 178
                               ,"line-height.minimum"  // 179
                               ,"line-height.optimum"  // 180
                               ,"line-height.maximum"  // 181
                        ,"line-height.conditionality"  // 182
                            ,"line-height.precedence"  // 183
                      ,"line-height-shift-adjustment"  // 184
                            ,"line-stacking-strategy"  // 185

                                 ,"marker-class-name"  // 186
                                       ,"master-name"  // 187
                                  ,"master-reference"  // 188
                                        ,"max-height"  // 189
                                   ,"maximum-repeats"  // 190
                                         ,"max-width"  // 191
                                       ,"media-usage"  // 192
                                        ,"min-height"  // 193
                                         ,"min-width"  // 194
                           ,"number-columns-repeated"  // 195
                               ,"number-rows-spanned"  // 196
                                       ,"odd-or-even"  // 197
                                           ,"orphans"  // 198
                                          ,"overflow"  // 199

                                     ,"padding-after"  // 200
                              ,"padding-after.length"  // 201
                      ,"padding-after.conditionality"  // 202
                                    ,"padding-before"  // 203
                             ,"padding-before.length"  // 204
                     ,"padding-before.conditionality"  // 205
                                       ,"padding-end"  // 206
                                ,"padding-end.length"  // 207
                        ,"padding-end.conditionality"  // 208
                                     ,"padding-start"  // 209
                              ,"padding-start.length"  // 210
                      ,"padding-start.conditionality"  // 211

                                    ,"padding-bottom"  // 212
                                      ,"padding-left"  // 213
                                     ,"padding-right"  // 214
                                       ,"padding-top"  // 215

                                       ,"page-height"  // 216
                                     ,"page-position"  // 217
                                        ,"page-width"  // 218
                                       ,"pause-after"  // 219
                                      ,"pause-before"  // 220
                                             ,"pitch"  // 221
                                       ,"pitch-range"  // 222
                                       ,"play-during"  // 223
                                        ,"precedence"  // 224
               ,"provisional-distance-between-starts"  // 225
                      ,"provisional-label-separation"  // 226
                             ,"reference-orientation"  // 227
                                            ,"ref-id"  // 228
                                       ,"region-name"  // 229
                                    ,"relative-align"  // 230
                                 ,"relative-position"  // 231
                                  ,"rendering-intent"  // 232
                                 ,"retrieve-boundary"  // 233
                               ,"retrieve-class-name"  // 234
                                 ,"retrieve-position"  // 235
                                          ,"richness"  // 236
                                             ,"right"  // 237
                                              ,"role"  // 238
                                        ,"rule-style"  // 239
                                    ,"rule-thickness"  // 240
                                           ,"scaling"  // 241
                                    ,"scaling-method"  // 242
                                      ,"score-spaces"  // 243
                                            ,"script"  // 244
                                  ,"show-destination"  // 245
                                   ,"source-document"  // 246

                                       ,"space-after"  // 247
                               ,"space-after.minimum"  // 248
                               ,"space-after.optimum"  // 249
                               ,"space-after.maximum"  // 250
                        ,"space-after.conditionality"  // 251
                            ,"space-after.precedence"  // 252
                                      ,"space-before"  // 253
                              ,"space-before.minimum"  // 254
                              ,"space-before.optimum"  // 255
                              ,"space-before.maximum"  // 256
                       ,"space-before.conditionality"  // 257
                           ,"space-before.precedence"  // 258
                                         ,"space-end"  // 259
                                 ,"space-end.minimum"  // 260
                                 ,"space-end.optimum"  // 261
                                 ,"space-end.maximum"  // 262
                          ,"space-end.conditionality"  // 263
                              ,"space-end.precedence"  // 264
                                       ,"space-start"  // 265
                               ,"space-start.minimum"  // 266
                               ,"space-start.optimum"  // 267
                               ,"space-start.maximum"  // 268
                        ,"space-start.conditionality"  // 269
                            ,"space-start.precedence"  // 270

                                     ,"margin-bottom"  // 271
                                       ,"margin-left"  // 272
                                      ,"margin-right"  // 273
                                        ,"margin-top"  // 274

                                              ,"span"  // 275
                                             ,"speak"  // 276
                                      ,"speak-header"  // 277
                                     ,"speak-numeral"  // 278
                                 ,"speak-punctuation"  // 279
                                       ,"speech-rate"  // 280
                                               ,"src"  // 281
                                      ,"start-indent"  // 282
                                    ,"starting-state"  // 283
                                        ,"starts-row"  // 284
                                            ,"stress"  // 285
                            ,"suppress-at-line-break"  // 286
                                         ,"switch-to"  // 287
                                      ,"table-layout"  // 288
                        ,"table-omit-footer-at-break"  // 289
                        ,"table-omit-header-at-break"  // 290
                       ,"target-presentation-context"  // 291
                         ,"target-processing-context"  // 292
                                 ,"target-stylesheet"  // 293
                                        ,"text-align"  // 294
                                   ,"text-align-last"  // 295
                                     ,"text-altitude"  // 296
                                   ,"text-decoration"  // 297
                                        ,"text-depth"  // 298
                                       ,"text-indent"  // 299
                                       ,"text-shadow"  // 300
                                    ,"text-transform"  // 301
                                               ,"top"  // 302
                               ,"treat-as-word-space"  // 303
                                      ,"unicode-bidi"  // 304
           ,"usage-context-of-suppress-at-line-break"  // 305
                                        ,"visibility"  // 306
                                      ,"voice-family"  // 307
                                            ,"volume"  // 308
                              ,"white-space-collapse"  // 309
                             ,"white-space-treatment"  // 310
                                            ,"widows"  // 311
                                             ,"width"  // 312
                                      ,"word-spacing"  // 313
                              ,"word-spacing-minimum"  // 314
                              ,"word-spacing-optimum"  // 315
                              ,"word-spacing-maximum"  // 316
                       ,"word-spacing-conditionality"  // 317
                           ,"word-spacing-precedence"  // 318
                                       ,"wrap-option"  // 319
                                           ,"z-index"  // 320
    };

    /**
     * A <tt>hashMap</tt> mapping property names (the keys) to
     * property integer indices.
     */
    private static final HashMap toIndex;
    static {
        toIndex = new HashMap(LAST_PROPERTY_INDEX + 1);
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
     * @param propindex <tt>int</tt> index of the FO property.
     * @return <tt>String</tt> name of the indexd FO property.
     * @exception PropertyException if the property index is invalid.
     */
    public static int getPropertyIndex(String name)
    {
        return ((Integer)(toIndex.get(name))).intValue();
    }

}
