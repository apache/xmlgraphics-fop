/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

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
            // Properties setting font-size first
            // Shorthand first
                                           FONT = 1,
                                      FONT_SIZE = 2,
            // Writing mode early for handling of corresponding values
                                   WRITING_MODE = 3,
            // All other shorthands
                                     BACKGROUND = 4,
                            BACKGROUND_POSITION = 5,
                                         BORDER = 6,
                                   BORDER_COLOR = 7,
                                   BORDER_STYLE = 8,
                                   BORDER_WIDTH = 9,
                                  BORDER_BOTTOM = 10,
                                    BORDER_LEFT = 11,
                                   BORDER_RIGHT = 12,
                                     BORDER_TOP = 13,
                                 BORDER_SPACING = 14,
                                            CUE = 15,
                                         MARGIN = 16,
                                        PADDING = 17,
                               PAGE_BREAK_AFTER = 18,
                              PAGE_BREAK_BEFORE = 19,
                              PAGE_BREAK_INSIDE = 20,
                                          PAUSE = 21,
                                       POSITION = 22,
                                           SIZE = 23,
                                 VERTICAL_ALIGN = 24,
                                    WHITE_SPACE = 25,
                                       XML_LANG = 26,
            // Non-shorthand properties
            // Within these, compounds precede their components
            // and corresponding relative properties
            // precede corresponding absolute properties
                              ABSOLUTE_POSITION = 27,
                                   ACTIVE_STATE = 28,
                               ALIGNMENT_ADJUST = 29,
                             ALIGNMENT_BASELINE = 30,
                                   AUTO_RESTORE = 31,
                                        AZIMUTH = 32,
                          BACKGROUND_ATTACHMENT = 33,
                               BACKGROUND_COLOR = 34,
                               BACKGROUND_IMAGE = 35,
                 BACKGROUND_POSITION_HORIZONTAL = 36,
                   BACKGROUND_POSITION_VERTICAL = 37,
                              BACKGROUND_REPEAT = 38,
                                 BASELINE_SHIFT = 39,
                             BLANK_OR_NOT_BLANK = 40,
                    BLOCK_PROGRESSION_DIMENSION = 41,
            BLOCK_PROGRESSION_DIMENSION_MINIMUM = 42,
            BLOCK_PROGRESSION_DIMENSION_OPTIMUM = 43,
            BLOCK_PROGRESSION_DIMENSION_MAXIMUM = 44,

        // Border corresponding properties
                             BORDER_AFTER_COLOR = 45,
                        BORDER_AFTER_PRECEDENCE = 46,
                             BORDER_AFTER_STYLE = 47,
                             BORDER_AFTER_WIDTH = 48,
                      BORDER_AFTER_WIDTH_LENGTH = 49,
              BORDER_AFTER_WIDTH_CONDITIONALITY = 50,
                            BORDER_BEFORE_COLOR = 51,
                       BORDER_BEFORE_PRECEDENCE = 52,
                            BORDER_BEFORE_STYLE = 53,
                            BORDER_BEFORE_WIDTH = 54,
                     BORDER_BEFORE_WIDTH_LENGTH = 55,
             BORDER_BEFORE_WIDTH_CONDITIONALITY = 56,
                               BORDER_END_COLOR = 57,
                          BORDER_END_PRECEDENCE = 58,
                               BORDER_END_STYLE = 59,
                               BORDER_END_WIDTH = 60,
                        BORDER_END_WIDTH_LENGTH = 61,
                BORDER_END_WIDTH_CONDITIONALITY = 62,
                             BORDER_START_COLOR = 63,
                        BORDER_START_PRECEDENCE = 64,
                             BORDER_START_STYLE = 65,
                             BORDER_START_WIDTH = 66,
                      BORDER_START_WIDTH_LENGTH = 67,
              BORDER_START_WIDTH_CONDITIONALITY = 68,

                            BORDER_BOTTOM_COLOR = 69,
                            BORDER_BOTTOM_STYLE = 70,
                            BORDER_BOTTOM_WIDTH = 71,
                              BORDER_LEFT_COLOR = 72,
                              BORDER_LEFT_STYLE = 73,
                              BORDER_LEFT_WIDTH = 74,
                             BORDER_RIGHT_COLOR = 75,
                             BORDER_RIGHT_STYLE = 76,
                             BORDER_RIGHT_WIDTH = 77,
                               BORDER_TOP_COLOR = 78,
                               BORDER_TOP_STYLE = 79,
                               BORDER_TOP_WIDTH = 80,

                                BORDER_COLLAPSE = 81,
                              BORDER_SEPARATION = 82,
  BORDER_SEPARATION_BLOCK_PROGRESSION_DIRECTION = 83,
 BORDER_SEPARATION_INLINE_PROGRESSION_DIRECTION = 84,
                                         BOTTOM = 85,
                                    BREAK_AFTER = 86,
                                   BREAK_BEFORE = 87,
                                   CAPTION_SIDE = 88,
                                      CASE_NAME = 89,
                                     CASE_TITLE = 90,
                                      CHARACTER = 91,
                                          CLEAR = 92,
                                           CLIP = 93,
                                          COLOR = 94,
                             COLOR_PROFILE_NAME = 95,
                                   COLUMN_COUNT = 96,
                                     COLUMN_GAP = 97,
                                  COLUMN_NUMBER = 98,
                                   COLUMN_WIDTH = 99,
                                 CONTENT_HEIGHT = 100,
                                   CONTENT_TYPE = 101,
                                  CONTENT_WIDTH = 102,
                                        COUNTRY = 103,
                                      CUE_AFTER = 104,
                                     CUE_BEFORE = 105,
                   DESTINATION_PLACEMENT_OFFSET = 106,
                                      DIRECTION = 107,
                                  DISPLAY_ALIGN = 108,
                              DOMINANT_BASELINE = 109,
                                      ELEVATION = 110,
                                    EMPTY_CELLS = 111,
                                     END_INDENT = 112,
                                       ENDS_ROW = 113,
                                         EXTENT = 114,
                           EXTERNAL_DESTINATION = 115,
                                          FLOAT = 116,
                                      FLOW_NAME = 117,
                                    FONT_FAMILY = 118,
                        FONT_SELECTION_STRATEGY = 119,
                               FONT_SIZE_ADJUST = 120,
                                   FONT_STRETCH = 121,
                                     FONT_STYLE = 122,
                                   FONT_VARIANT = 123,
                                    FONT_WEIGHT = 124,
                               FORCE_PAGE_COUNT = 125,
                                         FORMAT = 126,
                   GLYPH_ORIENTATION_HORIZONTAL = 127,
                     GLYPH_ORIENTATION_VERTICAL = 128,
                             GROUPING_SEPARATOR = 129,
                                  GROUPING_SIZE = 130,
                                         HEIGHT = 131,
                                      HYPHENATE = 132,
                          HYPHENATION_CHARACTER = 133,
                               HYPHENATION_KEEP = 134,
                       HYPHENATION_LADDER_COUNT = 135,
               HYPHENATION_PUSH_CHARACTER_COUNT = 136,
             HYPHENATION_REMAIN_CHARACTER_COUNT = 137,
                                             ID = 138,
                           INDICATE_DESTINATION = 139,
                            INITIAL_PAGE_NUMBER = 140,
                   INLINE_PROGRESSION_DIMENSION = 141,
           INLINE_PROGRESSION_DIMENSION_MINIMUM = 142,
           INLINE_PROGRESSION_DIMENSION_OPTIMUM = 143,
           INLINE_PROGRESSION_DIMENSION_MAXIMUM = 144,
                           INTERNAL_DESTINATION = 145,
                             INTRUSION_DISPLACE = 146,
                                  KEEP_TOGETHER = 147,
                      KEEP_TOGETHER_WITHIN_LINE = 148,
                      KEEP_TOGETHER_WITHIN_PAGE = 149,
                    KEEP_TOGETHER_WITHIN_COLUMN = 150,
                                 KEEP_WITH_NEXT = 151,
                     KEEP_WITH_NEXT_WITHIN_LINE = 152,
                     KEEP_WITH_NEXT_WITHIN_PAGE = 153,
                   KEEP_WITH_NEXT_WITHIN_COLUMN = 154,
                             KEEP_WITH_PREVIOUS = 155,
                 KEEP_WITH_PREVIOUS_WITHIN_LINE = 156,
                 KEEP_WITH_PREVIOUS_WITHIN_PAGE = 157,
               KEEP_WITH_PREVIOUS_WITHIN_COLUMN = 158,
                                       LANGUAGE = 159,
                           LAST_LINE_END_INDENT = 160,
                               LEADER_ALIGNMENT = 161,
                                  LEADER_LENGTH = 162,
                          LEADER_LENGTH_MINIMUM = 163,
                          LEADER_LENGTH_OPTIMUM = 164,
                          LEADER_LENGTH_MAXIMUM = 165,
                                 LEADER_PATTERN = 166,
                           LEADER_PATTERN_WIDTH = 167,
                                           LEFT = 168,
                                 LETTER_SPACING = 169,
                         LETTER_SPACING_MINIMUM = 170,
                         LETTER_SPACING_OPTIMUM = 171,
                         LETTER_SPACING_MAXIMUM = 172,
                  LETTER_SPACING_CONDITIONALITY = 173,
                      LETTER_SPACING_PRECEDENCE = 174,
                                   LETTER_VALUE = 175,
                             LINEFEED_TREATMENT = 176,
                                    LINE_HEIGHT = 177,
                            LINE_HEIGHT_MINIMUM = 178,
                            LINE_HEIGHT_OPTIMUM = 179,
                            LINE_HEIGHT_MAXIMUM = 180,
                     LINE_HEIGHT_CONDITIONALITY = 181,
                         LINE_HEIGHT_PRECEDENCE = 182,
                   LINE_HEIGHT_SHIFT_ADJUSTMENT = 183,
                         LINE_STACKING_STRATEGY = 184,

                              MARKER_CLASS_NAME = 185,
                                    MASTER_NAME = 186,
                               MASTER_REFERENCE = 187,
                                     MAX_HEIGHT = 188,
                                MAXIMUM_REPEATS = 189,
                                      MAX_WIDTH = 190,
                                    MEDIA_USAGE = 191,
                                     MIN_HEIGHT = 192,
                                      MIN_WIDTH = 193,
                        NUMBER_COLUMNS_REPEATED = 194,
                         NUMBER_COLUMNS_SPANNED = 195,
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
                                     VISIBILITY = 305,
                                   VOICE_FAMILY = 306,
                                         VOLUME = 307,
                           WHITE_SPACE_COLLAPSE = 308,
                          WHITE_SPACE_TREATMENT = 309,
                                         WIDOWS = 310,
                                          WIDTH = 311,
                                   WORD_SPACING = 312,
                           WORD_SPACING_MINIMUM = 313,
                           WORD_SPACING_OPTIMUM = 314,
                           WORD_SPACING_MAXIMUM = 315,
                    WORD_SPACING_CONDITIONALITY = 316,
                        WORD_SPACING_PRECEDENCE = 317,
                                    WRAP_OPTION = 318,
                                        Z_INDEX = 319,
        
                            LAST_PROPERTY_INDEX = Z_INDEX;


    /**
     * A String[] array containing the names of all of the FO properties.
     * The array is effectively 1-based, as the first element is null.
     * The list of int constants referring to the properties must be manually
     * kept in sync with the names in this array, as the constants can be
     * used to index into this, and the other property arrays.
     */

    private static final String[] propertyNames = {
        "no-property"                                          // 0
        ,"font"                                                // 1
        ,"font-size"                                           // 2

        ,"writing-mode"                                        // 3

        ,"background"                                          // 4
        ,"background-position"                                 // 5
        ,"border"                                              // 6
        ,"border-color"                                        // 7
        ,"border-style"                                        // 8
        ,"border-width"                                        // 9
        ,"border-bottom"                                       // 10
        ,"border-left"                                         // 11
        ,"border-right"                                        // 12
        ,"border-top"                                          // 13
        ,"border-spacing"                                      // 14
        ,"cue"                                                 // 15
        ,"margin"                                              // 16
        ,"padding"                                             // 17
        ,"page-break-after"                                    // 18
        ,"page-break-before"                                   // 19
        ,"page-break-inside"                                   // 20
        ,"pause"                                               // 21
        ,"position"                                            // 22
        ,"size"                                                // 23
        ,"vertical-align"                                      // 24
        ,"white-space"                                         // 25
        ,"xml:lang"                                            // 26

        ,"absolute-position"                                   // 27
        ,"active-state"                                        // 28
        ,"alignment-adjust"                                    // 29
        ,"alignment-baseline"                                  // 30
        ,"auto-restore"                                        // 31
        ,"azimuth"                                             // 32
        ,"background-attachment"                               // 33
        ,"background-color"                                    // 34
        ,"background-image"                                    // 35
        ,"background-position-horizontal"                      // 36
        ,"background-position-vertical"                        // 37
        ,"background-repeat"                                   // 38
        ,"baseline-shift"                                      // 39
        ,"blank-or-not-blank"                                  // 40
        ,"block-progression-dimension"                         // 41
        ,"block-progression-dimension.minimum"                 // 42
        ,"block-progression-dimension.optimum"                 // 43
        ,"block-progression-dimension.maximum"                 // 44

        ,"border-after-color"                                  // 45
        ,"border-after-precedence"                             // 46
        ,"border-after-style"                                  // 47
        ,"border-after-width"                                  // 48
        ,"border-after-width.length"                           // 49
        ,"border-after-width.conditionality"                   // 50
        ,"border-before-color"                                 // 51
        ,"border-before-precedence"                            // 52
        ,"border-before-style"                                 // 53
        ,"border-before-width"                                 // 54
        ,"border-before-width.length"                          // 55
        ,"border-before-width.conditionality"                  // 56
        ,"border-end-color"                                    // 57
        ,"border-end-precedence"                               // 58
        ,"border-end-style"                                    // 59
        ,"border-end-width"                                    // 60
        ,"border-end-width.length"                             // 61
        ,"border-end-width.conditionality"                     // 62
        ,"border-start-color"                                  // 63
        ,"border-start-precedence"                             // 64
        ,"border-start-style"                                  // 65
        ,"border-start-width"                                  // 66
        ,"border-start-width.length"                           // 67
        ,"border-start-width.conditionality"                   // 68

        ,"border-bottom-color"                                 // 69
        ,"border-bottom-style"                                 // 70
        ,"border-bottom-width"                                 // 71
        ,"border-left-color"                                   // 72
        ,"border-left-style"                                   // 73
        ,"border-left-width"                                   // 74
        ,"border-right-color"                                  // 75
        ,"border-right-style"                                  // 76
        ,"border-right-width"                                  // 77
        ,"border-top-color"                                    // 78
        ,"border-top-style"                                    // 79
        ,"border-top-width"                                    // 80

        ,"border-collapse"                                     // 81
        ,"border-separation"                                   // 82
        ,"border-separation.block-progression-direction"       // 83
        ,"border-separation.inline-progression-direction"      // 84
        ,"bottom"                                              // 85
        ,"break-after"                                         // 86
        ,"break-before"                                        // 87
        ,"caption-side"                                        // 88
        ,"case-name"                                           // 89
        ,"case-title"                                          // 90
        ,"character"                                           // 91
        ,"clear"                                               // 92
        ,"clip"                                                // 93
        ,"color"                                               // 94
        ,"color-profile-name"                                  // 95
        ,"column-count"                                        // 96
        ,"column-gap"                                          // 97
        ,"column-number"                                       // 98
        ,"column-width"                                        // 99
        ,"content-height"                                      // 100
        ,"content-type"                                        // 101
        ,"content-width"                                       // 102
        ,"country"                                             // 103
        ,"cue-after"                                           // 104
        ,"cue-before"                                          // 105
        ,"destination-placement-offset"                        // 106
        ,"direction"                                           // 107
        ,"display-align"                                       // 108
        ,"dominant-baseline"                                   // 109
        ,"elevation"                                           // 110
        ,"empty-cells"                                         // 111
        ,"end-indent"                                          // 112
        ,"ends-row"                                            // 113
        ,"extent"                                              // 114
        ,"external-destination"                                // 115
        ,"float"                                               // 116
        ,"flow-name"                                           // 117
        ,"font-family"                                         // 118
        ,"font-selection-strategy"                             // 119
        ,"font-size-adjust"                                    // 120
        ,"font-stretch"                                        // 121
        ,"font-style"                                          // 122
        ,"font-variant"                                        // 123
        ,"font-weight"                                         // 124
        ,"force-page-count"                                    // 125
        ,"format"                                              // 126
        ,"glyph-orientation-horizontal"                        // 127
        ,"glyph-orientation-vertical"                          // 128
        ,"grouping-separator"                                  // 129
        ,"grouping-size"                                       // 130
        ,"height"                                              // 131
        ,"hyphenate"                                           // 132
        ,"hyphenation-character"                               // 133
        ,"hyphenation-keep"                                    // 134
        ,"hyphenation-ladder-count"                            // 135
        ,"hyphenation-push-character-count"                    // 136
        ,"hyphenation-remain-character-count"                  // 137
        ,"id"                                                  // 138
        ,"indicate-destination"                                // 139
        ,"initial-page-number"                                 // 140
        ,"inline-progression-dimension"                        // 141
        ,"inline-progression-dimension.minimum"                // 142
        ,"inline-progression-dimension.optimum"                // 143
        ,"inline-progression-dimension.maximum"                // 144
        ,"internal-destination"                                // 145
        ,"intrusion-displace"                                  // 146
        ,"keep-together"                                       // 147
        ,"keep-together.within-line"                           // 148
        ,"keep-together.within-column"                         // 149
        ,"keep-together.within-page"                           // 150
        ,"keep-with-next"                                      // 151
        ,"keep-with-next.within-line"                          // 152
        ,"keep-with-next.within-column"                        // 153
        ,"keep-with-next.within-page"                          // 154
        ,"keep-with-previous"                                  // 155
        ,"keep-with-previous.within-line"                      // 156
        ,"keep-with-previous.within-column"                    // 157
        ,"keep-with-previous.within-page"                      // 158
        ,"language"                                            // 159
        ,"last-line-end-indent"                                // 160
        ,"leader-alignment"                                    // 161
        ,"leader-length"                                       // 162
        ,"leader-length.minimum"                               // 163
        ,"leader-length.optimum"                               // 164
        ,"leader-length.maximum"                               // 165
        ,"leader-pattern"                                      // 166
        ,"leader-pattern-width"                                // 167
        ,"left"                                                // 168
        ,"letter-spacing"                                      // 169
        ,"letter-spacing.minimum"                              // 170
        ,"letter-spacing.optimum"                              // 171
        ,"letter-spacing.maximum"                              // 172
        ,"letter-spacing.conditionality"                       // 173
        ,"letter-spacing.precedence"                           // 174
        ,"letter-value"                                        // 175
        ,"linefeed-treatment"                                  // 176
        ,"line-height"                                         // 177
        ,"line-height.minimum"                                 // 178
        ,"line-height.optimum"                                 // 179
        ,"line-height.maximum"                                 // 180
        ,"line-height.conditionality"                          // 181
        ,"line-height.precedence"                              // 182
        ,"line-height-shift-adjustment"                        // 183
        ,"line-stacking-strategy"                              // 184

        ,"marker-class-name"                                   // 185
        ,"master-name"                                         // 186
        ,"master-reference"                                    // 187
        ,"max-height"                                          // 188
        ,"maximum-repeats"                                     // 189
        ,"max-width"                                           // 190
        ,"media-usage"                                         // 191
        ,"min-height"                                          // 192
        ,"min-width"                                           // 193
        ,"number-columns-repeated"                             // 194
        ,"number-columns-spanned"                              // 195
        ,"number-rows-spanned"                                 // 196
        ,"odd-or-even"                                         // 197
        ,"orphans"                                             // 198
        ,"overflow"                                            // 199

        ,"padding-after"                                       // 200
        ,"padding-after.length"                                // 201
        ,"padding-after.conditionality"                        // 202
        ,"padding-before"                                      // 203
        ,"padding-before.length"                               // 204
        ,"padding-before.conditionality"                       // 205
        ,"padding-end"                                         // 206
        ,"padding-end.length"                                  // 207
        ,"padding-end.conditionality"                          // 208
        ,"padding-start"                                       // 209
        ,"padding-start.length"                                // 210
        ,"padding-start.conditionality"                        // 211

        ,"padding-bottom"                                      // 212
        ,"padding-left"                                        // 213
        ,"padding-right"                                       // 214
        ,"padding-top"                                         // 215

        ,"page-height"                                         // 216
        ,"page-position"                                       // 217
        ,"page-width"                                          // 218
        ,"pause-after"                                         // 219
        ,"pause-before"                                        // 220
        ,"pitch"                                               // 221
        ,"pitch-range"                                         // 222
        ,"play-during"                                         // 223
        ,"precedence"                                          // 224
        ,"provisional-distance-between-starts"                 // 225
        ,"provisional-label-separation"                        // 226
        ,"reference-orientation"                               // 227
        ,"ref-id"                                              // 228
        ,"region-name"                                         // 229
        ,"relative-align"                                      // 230
        ,"relative-position"                                   // 231
        ,"rendering-intent"                                    // 232
        ,"retrieve-boundary"                                   // 233
        ,"retrieve-class-name"                                 // 234
        ,"retrieve-position"                                   // 235
        ,"richness"                                            // 236
        ,"right"                                               // 237
        ,"role"                                                // 238
        ,"rule-style"                                          // 239
        ,"rule-thickness"                                      // 240
        ,"scaling"                                             // 241
        ,"scaling-method"                                      // 242
        ,"score-spaces"                                        // 243
        ,"script"                                              // 244
        ,"show-destination"                                    // 245
        ,"source-document"                                     // 246

        ,"space-after"                                         // 247
        ,"space-after.minimum"                                 // 248
        ,"space-after.optimum"                                 // 249
        ,"space-after.maximum"                                 // 250
        ,"space-after.conditionality"                          // 251
        ,"space-after.precedence"                              // 252
        ,"space-before"                                        // 253
        ,"space-before.minimum"                                // 254
        ,"space-before.optimum"                                // 255
        ,"space-before.maximum"                                // 256
        ,"space-before.conditionality"                         // 257
        ,"space-before.precedence"                             // 258
        ,"space-end"                                           // 259
        ,"space-end.minimum"                                   // 260
        ,"space-end.optimum"                                   // 261
        ,"space-end.maximum"                                   // 262
        ,"space-end.conditionality"                            // 263
        ,"space-end.precedence"                                // 264
        ,"space-start"                                         // 265
        ,"space-start.minimum"                                 // 266
        ,"space-start.optimum"                                 // 267
        ,"space-start.maximum"                                 // 268
        ,"space-start.conditionality"                          // 269
        ,"space-start.precedence"                              // 270

        ,"margin-bottom"                                       // 271
        ,"margin-left"                                         // 272
        ,"margin-right"                                        // 273
        ,"margin-top"                                          // 274

        ,"span"                                                // 275
        ,"speak"                                               // 276
        ,"speak-header"                                        // 277
        ,"speak-numeral"                                       // 278
        ,"speak-punctuation"                                   // 279
        ,"speech-rate"                                         // 280
        ,"src"                                                 // 281
        ,"start-indent"                                        // 282
        ,"starting-state"                                      // 283
        ,"starts-row"                                          // 284
        ,"stress"                                              // 285
        ,"suppress-at-line-break"                              // 286
        ,"switch-to"                                           // 287
        ,"table-layout"                                        // 288
        ,"table-omit-footer-at-break"                          // 289
        ,"table-omit-header-at-break"                          // 290
        ,"target-presentation-context"                         // 291
        ,"target-processing-context"                           // 292
        ,"target-stylesheet"                                   // 293
        ,"text-align"                                          // 294
        ,"text-align-last"                                     // 295
        ,"text-altitude"                                       // 296
        ,"text-decoration"                                     // 297
        ,"text-depth"                                          // 298
        ,"text-indent"                                         // 299
        ,"text-shadow"                                         // 300
        ,"text-transform"                                      // 301
        ,"top"                                                 // 302
        ,"treat-as-word-space"                                 // 303
        ,"unicode-bidi"                                        // 304
        ,"visibility"                                          // 305
        ,"voice-family"                                        // 306
        ,"volume"                                              // 307
        ,"white-space-collapse"                                // 308
        ,"white-space-treatment"                               // 309
        ,"widows"                                              // 310
        ,"width"                                               // 311
        ,"word-spacing"                                        // 312
        ,"word-spacing-minimum"                                // 313
        ,"word-spacing-optimum"                                // 314
        ,"word-spacing-maximum"                                // 315
        ,"word-spacing-conditionality"                         // 316
        ,"word-spacing-precedence"                             // 317
        ,"wrap-option"                                         // 318
        ,"z-index"                                             // 319
    };

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

}
