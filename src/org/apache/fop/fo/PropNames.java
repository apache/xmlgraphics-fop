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
            // All other shorthands
                                     BACKGROUND = 3,
                            BACKGROUND_POSITION = 4,
                                         BORDER = 5,
                                   BORDER_COLOR = 6,
                                   BORDER_STYLE = 7,
                                   BORDER_WIDTH = 8,
                                  BORDER_BOTTOM = 9,
                                    BORDER_LEFT = 10,
                                   BORDER_RIGHT = 11,
                                     BORDER_TOP = 12,
                                 BORDER_SPACING = 13,
                                            CUE = 14,
                                         MARGIN = 15,
                                        PADDING = 16,
                               PAGE_BREAK_AFTER = 17,
                              PAGE_BREAK_BEFORE = 18,
                              PAGE_BREAK_INSIDE = 19,
                                          PAUSE = 20,
                                       POSITION = 21,
                                           SIZE = 22,
                                 VERTICAL_ALIGN = 23,
                                    WHITE_SPACE = 24,
                                       XML_LANG = 25,
            // Non-shorthand properties
            // Within these, compounds precede their components
            // and corresponding relative properties
            // precede corresponding absolute properties
                              ABSOLUTE_POSITION = 26,
                                   ACTIVE_STATE = 27,
                               ALIGNMENT_ADJUST = 28,
                             ALIGNMENT_BASELINE = 29,
                                   AUTO_RESTORE = 30,
                                        AZIMUTH = 31,
                          BACKGROUND_ATTACHMENT = 32,
                               BACKGROUND_COLOR = 33,
                               BACKGROUND_IMAGE = 34,
                 BACKGROUND_POSITION_HORIZONTAL = 35,
                   BACKGROUND_POSITION_VERTICAL = 36,
                              BACKGROUND_REPEAT = 37,
                                 BASELINE_SHIFT = 38,
                             BLANK_OR_NOT_BLANK = 39,
                    BLOCK_PROGRESSION_DIMENSION = 40,
            BLOCK_PROGRESSION_DIMENSION_MINIMUM = 41,
            BLOCK_PROGRESSION_DIMENSION_OPTIMUM = 42,
            BLOCK_PROGRESSION_DIMENSION_MAXIMUM = 43,

        // Border corresponding properties
                             BORDER_AFTER_COLOR = 44,
                        BORDER_AFTER_PRECEDENCE = 45,
                             BORDER_AFTER_STYLE = 46,
                             BORDER_AFTER_WIDTH = 47,
                      BORDER_AFTER_WIDTH_LENGTH = 48,
              BORDER_AFTER_WIDTH_CONDITIONALITY = 49,
                            BORDER_BEFORE_COLOR = 50,
                       BORDER_BEFORE_PRECEDENCE = 51,
                            BORDER_BEFORE_STYLE = 52,
                            BORDER_BEFORE_WIDTH = 53,
                     BORDER_BEFORE_WIDTH_LENGTH = 54,
             BORDER_BEFORE_WIDTH_CONDITIONALITY = 55,
                               BORDER_END_COLOR = 56,
                          BORDER_END_PRECEDENCE = 57,
                               BORDER_END_STYLE = 58,
                               BORDER_END_WIDTH = 59,
                        BORDER_END_WIDTH_LENGTH = 60,
                BORDER_END_WIDTH_CONDITIONALITY = 61,
                             BORDER_START_COLOR = 62,
                        BORDER_START_PRECEDENCE = 63,
                             BORDER_START_STYLE = 64,
                             BORDER_START_WIDTH = 65,
                      BORDER_START_WIDTH_LENGTH = 66,
              BORDER_START_WIDTH_CONDITIONALITY = 67,

                            BORDER_BOTTOM_COLOR = 68,
                            BORDER_BOTTOM_STYLE = 69,
                            BORDER_BOTTOM_WIDTH = 70,
                              BORDER_LEFT_COLOR = 71,
                              BORDER_LEFT_STYLE = 72,
                              BORDER_LEFT_WIDTH = 73,
                             BORDER_RIGHT_COLOR = 74,
                             BORDER_RIGHT_STYLE = 75,
                             BORDER_RIGHT_WIDTH = 76,
                               BORDER_TOP_COLOR = 77,
                               BORDER_TOP_STYLE = 78,
                               BORDER_TOP_WIDTH = 79,

                                BORDER_COLLAPSE = 80,
                              BORDER_SEPARATION = 81,
  BORDER_SEPARATION_BLOCK_PROGRESSION_DIRECTION = 82,
 BORDER_SEPARATION_INLINE_PROGRESSION_DIRECTION = 83,
                                         BOTTOM = 84,
                                    BREAK_AFTER = 85,
                                   BREAK_BEFORE = 86,
                                   CAPTION_SIDE = 87,
                                      CASE_NAME = 88,
                                     CASE_TITLE = 89,
                                      CHARACTER = 90,
                                          CLEAR = 91,
                                           CLIP = 92,
                                          COLOR = 93,
                             COLOR_PROFILE_NAME = 94,
                                   COLUMN_COUNT = 95,
                                     COLUMN_GAP = 96,
                                  COLUMN_NUMBER = 97,
                                   COLUMN_WIDTH = 98,
                                 CONTENT_HEIGHT = 99,
                                   CONTENT_TYPE = 100,
                                  CONTENT_WIDTH = 101,
                                        COUNTRY = 102,
                                      CUE_AFTER = 103,
                                     CUE_BEFORE = 104,
                   DESTINATION_PLACEMENT_OFFSET = 105,
                                      DIRECTION = 106,
                                  DISPLAY_ALIGN = 107,
                              DOMINANT_BASELINE = 108,
                                      ELEVATION = 109,
                                    EMPTY_CELLS = 110,
                                     END_INDENT = 111,
                                       ENDS_ROW = 112,
                                         EXTENT = 113,
                           EXTERNAL_DESTINATION = 114,
                                          FLOAT = 115,
                                      FLOW_NAME = 116,
                                    FONT_FAMILY = 117,
                        FONT_SELECTION_STRATEGY = 118,
                               FONT_SIZE_ADJUST = 119,
                                   FONT_STRETCH = 120,
                                     FONT_STYLE = 121,
                                   FONT_VARIANT = 122,
                                    FONT_WEIGHT = 123,
                               FORCE_PAGE_COUNT = 124,
                                         FORMAT = 125,
                   GLYPH_ORIENTATION_HORIZONTAL = 126,
                     GLYPH_ORIENTATION_VERTICAL = 127,
                             GROUPING_SEPARATOR = 128,
                                  GROUPING_SIZE = 129,
                                         HEIGHT = 130,
                                      HYPHENATE = 131,
                          HYPHENATION_CHARACTER = 132,
                               HYPHENATION_KEEP = 133,
                       HYPHENATION_LADDER_COUNT = 134,
               HYPHENATION_PUSH_CHARACTER_COUNT = 135,
             HYPHENATION_REMAIN_CHARACTER_COUNT = 136,
                                             ID = 137,
                           INDICATE_DESTINATION = 138,
                            INITIAL_PAGE_NUMBER = 139,
                   INLINE_PROGRESSION_DIMENSION = 140,
           INLINE_PROGRESSION_DIMENSION_MINIMUM = 141,
           INLINE_PROGRESSION_DIMENSION_OPTIMUM = 142,
           INLINE_PROGRESSION_DIMENSION_MAXIMUM = 143,
                           INTERNAL_DESTINATION = 144,
                             INTRUSION_DISPLACE = 145,
                                  KEEP_TOGETHER = 146,
                      KEEP_TOGETHER_WITHIN_LINE = 147,
                      KEEP_TOGETHER_WITHIN_PAGE = 148,
                    KEEP_TOGETHER_WITHIN_COLUMN = 149,
                                 KEEP_WITH_NEXT = 150,
                     KEEP_WITH_NEXT_WITHIN_LINE = 151,
                     KEEP_WITH_NEXT_WITHIN_PAGE = 152,
                   KEEP_WITH_NEXT_WITHIN_COLUMN = 153,
                             KEEP_WITH_PREVIOUS = 154,
                 KEEP_WITH_PREVIOUS_WITHIN_LINE = 155,
                 KEEP_WITH_PREVIOUS_WITHIN_PAGE = 156,
               KEEP_WITH_PREVIOUS_WITHIN_COLUMN = 157,
                                       LANGUAGE = 158,
                           LAST_LINE_END_INDENT = 159,
                               LEADER_ALIGNMENT = 160,
                                  LEADER_LENGTH = 161,
                          LEADER_LENGTH_MINIMUM = 162,
                          LEADER_LENGTH_OPTIMUM = 163,
                          LEADER_LENGTH_MAXIMUM = 164,
                                 LEADER_PATTERN = 165,
                           LEADER_PATTERN_WIDTH = 166,
                                           LEFT = 167,
                                 LETTER_SPACING = 168,
                         LETTER_SPACING_MINIMUM = 169,
                         LETTER_SPACING_OPTIMUM = 170,
                         LETTER_SPACING_MAXIMUM = 171,
                  LETTER_SPACING_CONDITIONALITY = 172,
                      LETTER_SPACING_PRECEDENCE = 173,
                                   LETTER_VALUE = 174,
                             LINEFEED_TREATMENT = 175,
                                    LINE_HEIGHT = 176,
                            LINE_HEIGHT_MINIMUM = 177,
                            LINE_HEIGHT_OPTIMUM = 178,
                            LINE_HEIGHT_MAXIMUM = 179,
                     LINE_HEIGHT_CONDITIONALITY = 180,
                         LINE_HEIGHT_PRECEDENCE = 181,
                   LINE_HEIGHT_SHIFT_ADJUSTMENT = 182,
                         LINE_STACKING_STRATEGY = 183,

                              MARKER_CLASS_NAME = 184,
                                    MASTER_NAME = 185,
                               MASTER_REFERENCE = 186,
                                     MAX_HEIGHT = 187,
                                MAXIMUM_REPEATS = 188,
                                      MAX_WIDTH = 189,
                                    MEDIA_USAGE = 190,
                                     MIN_HEIGHT = 191,
                                      MIN_WIDTH = 192,
                        NUMBER_COLUMNS_REPEATED = 193,
                         NUMBER_COLUMNS_SPANNED = 194,
                            NUMBER_ROWS_SPANNED = 195,
                                    ODD_OR_EVEN = 196,
                                        ORPHANS = 197,
                                       OVERFLOW = 198,

        // Padding corresponding properties
                                  PADDING_AFTER = 199,
                           PADDING_AFTER_LENGTH = 200,
                   PADDING_AFTER_CONDITIONALITY = 201,
                                 PADDING_BEFORE = 202,
                          PADDING_BEFORE_LENGTH = 203,
                  PADDING_BEFORE_CONDITIONALITY = 204,
                                    PADDING_END = 205,
                             PADDING_END_LENGTH = 206,
                     PADDING_END_CONDITIONALITY = 207,
                                  PADDING_START = 208,
                           PADDING_START_LENGTH = 209,
                   PADDING_START_CONDITIONALITY = 210,

                                 PADDING_BOTTOM = 211,
                                   PADDING_LEFT = 212,
                                  PADDING_RIGHT = 213,
                                    PADDING_TOP = 214,

                                    PAGE_HEIGHT = 215,
                                  PAGE_POSITION = 216,
                                     PAGE_WIDTH = 217,
                                    PAUSE_AFTER = 218,
                                   PAUSE_BEFORE = 219,
                                          PITCH = 220,
                                    PITCH_RANGE = 221,
                                    PLAY_DURING = 222,
                                     PRECEDENCE = 223,
            PROVISIONAL_DISTANCE_BETWEEN_STARTS = 224,
                   PROVISIONAL_LABEL_SEPARATION = 225,
                          REFERENCE_ORIENTATION = 226,
                                         REF_ID = 227,
                                    REGION_NAME = 228,
                                 RELATIVE_ALIGN = 229,
                              RELATIVE_POSITION = 230,
                               RENDERING_INTENT = 231,
                              RETRIEVE_BOUNDARY = 232,
                            RETRIEVE_CLASS_NAME = 233,
                              RETRIEVE_POSITION = 234,
                                       RICHNESS = 235,
                                          RIGHT = 236,
                                           ROLE = 237,
                                     RULE_STYLE = 238,
                                 RULE_THICKNESS = 239,
                                        SCALING = 240,
                                 SCALING_METHOD = 241,
                                   SCORE_SPACES = 242,
                                         SCRIPT = 243,
                               SHOW_DESTINATION = 244,
                                SOURCE_DOCUMENT = 245,

        // Space/margin corresponding properties
                                    SPACE_AFTER = 246,
                            SPACE_AFTER_MINIMUM = 247,
                            SPACE_AFTER_OPTIMUM = 248,
                            SPACE_AFTER_MAXIMUM = 249,
                     SPACE_AFTER_CONDITIONALITY = 250,
                         SPACE_AFTER_PRECEDENCE = 251,
                                   SPACE_BEFORE = 252,
                           SPACE_BEFORE_MINIMUM = 253,
                           SPACE_BEFORE_OPTIMUM = 254,
                           SPACE_BEFORE_MAXIMUM = 255,
                    SPACE_BEFORE_CONDITIONALITY = 256,
                        SPACE_BEFORE_PRECEDENCE = 257,
                                      SPACE_END = 258,
                              SPACE_END_MINIMUM = 259,
                              SPACE_END_OPTIMUM = 260,
                              SPACE_END_MAXIMUM = 261,
                       SPACE_END_CONDITIONALITY = 262,
                           SPACE_END_PRECEDENCE = 263,
                                    SPACE_START = 264,
                            SPACE_START_MINIMUM = 265,
                            SPACE_START_OPTIMUM = 266,
                            SPACE_START_MAXIMUM = 267,
                     SPACE_START_CONDITIONALITY = 268,
                         SPACE_START_PRECEDENCE = 269,

                                  MARGIN_BOTTOM = 270,
                                    MARGIN_LEFT = 271,
                                   MARGIN_RIGHT = 272,
                                     MARGIN_TOP = 273,

                                           SPAN = 274,
                                          SPEAK = 275,
                                   SPEAK_HEADER = 276,
                                  SPEAK_NUMERAL = 277,
                              SPEAK_PUNCTUATION = 278,
                                    SPEECH_RATE = 279,
                                            SRC = 280,
                                   START_INDENT = 281,
                                 STARTING_STATE = 282,
                                     STARTS_ROW = 283,
                                         STRESS = 284,
                         SUPPRESS_AT_LINE_BREAK = 285,
                                      SWITCH_TO = 286,
                                   TABLE_LAYOUT = 287,
                     TABLE_OMIT_FOOTER_AT_BREAK = 288,
                     TABLE_OMIT_HEADER_AT_BREAK = 289,
                    TARGET_PRESENTATION_CONTEXT = 290,
                      TARGET_PROCESSING_CONTEXT = 291,
                              TARGET_STYLESHEET = 292,
                                     TEXT_ALIGN = 293,
                                TEXT_ALIGN_LAST = 294,
                                  TEXT_ALTITUDE = 295,
                                TEXT_DECORATION = 296,
                                     TEXT_DEPTH = 297,
                                    TEXT_INDENT = 298,
                                    TEXT_SHADOW = 299,
                                 TEXT_TRANSFORM = 300,
                                            TOP = 301,
                            TREAT_AS_WORD_SPACE = 302,
                                   UNICODE_BIDI = 303,
                                     VISIBILITY = 304,
                                   VOICE_FAMILY = 305,
                                         VOLUME = 306,
                           WHITE_SPACE_COLLAPSE = 307,
                          WHITE_SPACE_TREATMENT = 308,
                                         WIDOWS = 309,
                                          WIDTH = 310,
                                   WORD_SPACING = 311,
                           WORD_SPACING_MINIMUM = 312,
                           WORD_SPACING_OPTIMUM = 313,
                           WORD_SPACING_MAXIMUM = 314,
                    WORD_SPACING_CONDITIONALITY = 315,
                        WORD_SPACING_PRECEDENCE = 316,
                                    WRAP_OPTION = 317,
                                   WRITING_MODE = 318,
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

        ,"background"                                          // 3
        ,"background-position"                                 // 4
        ,"border"                                              // 5
        ,"border-color"                                        // 6
        ,"border-style"                                        // 7
        ,"border-width"                                        // 8
        ,"border-bottom"                                       // 9
        ,"border-left"                                         // 10
        ,"border-right"                                        // 11
        ,"border-top"                                          // 12
        ,"border-spacing"                                      // 13
        ,"cue"                                                 // 14
        ,"margin"                                              // 15
        ,"padding"                                             // 16
        ,"page-break-after"                                    // 17
        ,"page-break-before"                                   // 18
        ,"page-break-inside"                                   // 19
        ,"pause"                                               // 20
        ,"position"                                            // 21
        ,"size"                                                // 22
        ,"vertical-align"                                      // 23
        ,"white-space"                                         // 24
        ,"xml:lang"                                            // 25

        ,"absolute-position"                                   // 26
        ,"active-state"                                        // 27
        ,"alignment-adjust"                                    // 28
        ,"alignment-baseline"                                  // 29
        ,"auto-restore"                                        // 30
        ,"azimuth"                                             // 31
        ,"background-attachment"                               // 32
        ,"background-color"                                    // 33
        ,"background-image"                                    // 34
        ,"background-position-horizontal"                      // 35
        ,"background-position-vertical"                        // 36
        ,"background-repeat"                                   // 37
        ,"baseline-shift"                                      // 38
        ,"blank-or-not-blank"                                  // 39
        ,"block-progression-dimension"                         // 40
        ,"block-progression-dimension.minimum"                 // 41
        ,"block-progression-dimension.optimum"                 // 42
        ,"block-progression-dimension.maximum"                 // 43

        ,"border-after-color"                                  // 44
        ,"border-after-precedence"                             // 45
        ,"border-after-style"                                  // 46
        ,"border-after-width"                                  // 47
        ,"border-after-width.length"                           // 48
        ,"border-after-width.conditionality"                   // 49
        ,"border-before-color"                                 // 50
        ,"border-before-precedence"                            // 51
        ,"border-before-style"                                 // 52
        ,"border-before-width"                                 // 53
        ,"border-before-width.length"                          // 54
        ,"border-before-width.conditionality"                  // 55
        ,"border-end-color"                                    // 56
        ,"border-end-precedence"                               // 57
        ,"border-end-style"                                    // 58
        ,"border-end-width"                                    // 59
        ,"border-end-width.length"                             // 60
        ,"border-end-width.conditionality"                     // 61
        ,"border-start-color"                                  // 62
        ,"border-start-precedence"                             // 63
        ,"border-start-style"                                  // 64
        ,"border-start-width"                                  // 65
        ,"border-start-width.length"                           // 66
        ,"border-start-width.conditionality"                   // 67

        ,"border-bottom-color"                                 // 68
        ,"border-bottom-style"                                 // 69
        ,"border-bottom-width"                                 // 70
        ,"border-left-color"                                   // 71
        ,"border-left-style"                                   // 72
        ,"border-left-width"                                   // 73
        ,"border-right-color"                                  // 74
        ,"border-right-style"                                  // 75
        ,"border-right-width"                                  // 76
        ,"border-top-color"                                    // 77
        ,"border-top-style"                                    // 78
        ,"border-top-width"                                    // 79

        ,"border-collapse"                                     // 80
        ,"border-separation"                                   // 81
        ,"border-separation.block-progression-direction"       // 82
        ,"border-separation.inline-progression-direction"      // 83
        ,"bottom"                                              // 84
        ,"break-after"                                         // 85
        ,"break-before"                                        // 86
        ,"caption-side"                                        // 87
        ,"case-name"                                           // 88
        ,"case-title"                                          // 89
        ,"character"                                           // 90
        ,"clear"                                               // 91
        ,"clip"                                                // 92
        ,"color"                                               // 93
        ,"color-profile-name"                                  // 94
        ,"column-count"                                        // 95
        ,"column-gap"                                          // 96
        ,"column-number"                                       // 97
        ,"column-width"                                        // 98
        ,"content-height"                                      // 99
        ,"content-type"                                        // 100
        ,"content-width"                                       // 101
        ,"country"                                             // 102
        ,"cue-after"                                           // 103
        ,"cue-before"                                          // 104
        ,"destination-placement-offset"                        // 105
        ,"direction"                                           // 106
        ,"display-align"                                       // 107
        ,"dominant-baseline"                                   // 108
        ,"elevation"                                           // 109
        ,"empty-cells"                                         // 110
        ,"end-indent"                                          // 111
        ,"ends-row"                                            // 112
        ,"extent"                                              // 113
        ,"external-destination"                                // 114
        ,"float"                                               // 115
        ,"flow-name"                                           // 116
        ,"font-family"                                         // 117
        ,"font-selection-strategy"                             // 118
        ,"font-size-adjust"                                    // 119
        ,"font-stretch"                                        // 120
        ,"font-style"                                          // 121
        ,"font-variant"                                        // 122
        ,"font-weight"                                         // 123
        ,"force-page-count"                                    // 124
        ,"format"                                              // 125
        ,"glyph-orientation-horizontal"                        // 126
        ,"glyph-orientation-vertical"                          // 127
        ,"grouping-separator"                                  // 128
        ,"grouping-size"                                       // 129
        ,"height"                                              // 130
        ,"hyphenate"                                           // 131
        ,"hyphenation-character"                               // 132
        ,"hyphenation-keep"                                    // 133
        ,"hyphenation-ladder-count"                            // 134
        ,"hyphenation-push-character-count"                    // 135
        ,"hyphenation-remain-character-count"                  // 136
        ,"id"                                                  // 137
        ,"indicate-destination"                                // 138
        ,"initial-page-number"                                 // 139
        ,"inline-progression-dimension"                        // 140
        ,"inline-progression-dimension.minimum"                // 141
        ,"inline-progression-dimension.optimum"                // 142
        ,"inline-progression-dimension.maximum"                // 143
        ,"internal-destination"                                // 144
        ,"intrusion-displace"                                  // 145
        ,"keep-together"                                       // 146
        ,"keep-together.within-line"                           // 147
        ,"keep-together.within-column"                         // 148
        ,"keep-together.within-page"                           // 149
        ,"keep-with-next"                                      // 150
        ,"keep-with-next.within-line"                          // 151
        ,"keep-with-next.within-column"                        // 152
        ,"keep-with-next.within-page"                          // 153
        ,"keep-with-previous"                                  // 154
        ,"keep-with-previous.within-line"                      // 155
        ,"keep-with-previous.within-column"                    // 156
        ,"keep-with-previous.within-page"                      // 157
        ,"language"                                            // 158
        ,"last-line-end-indent"                                // 159
        ,"leader-alignment"                                    // 160
        ,"leader-length"                                       // 161
        ,"leader-length.minimum"                               // 162
        ,"leader-length.optimum"                               // 163
        ,"leader-length.maximum"                               // 164
        ,"leader-pattern"                                      // 165
        ,"leader-pattern-width"                                // 166
        ,"left"                                                // 167
        ,"letter-spacing"                                      // 168
        ,"letter-spacing.minimum"                              // 169
        ,"letter-spacing.optimum"                              // 170
        ,"letter-spacing.maximum"                              // 171
        ,"letter-spacing.conditionality"                       // 172
        ,"letter-spacing.precedence"                           // 173
        ,"letter-value"                                        // 174
        ,"linefeed-treatment"                                  // 175
        ,"line-height"                                         // 176
        ,"line-height.minimum"                                 // 177
        ,"line-height.optimum"                                 // 178
        ,"line-height.maximum"                                 // 179
        ,"line-height.conditionality"                          // 180
        ,"line-height.precedence"                              // 181
        ,"line-height-shift-adjustment"                        // 182
        ,"line-stacking-strategy"                              // 183

        ,"marker-class-name"                                   // 184
        ,"master-name"                                         // 185
        ,"master-reference"                                    // 186
        ,"max-height"                                          // 187
        ,"maximum-repeats"                                     // 188
        ,"max-width"                                           // 189
        ,"media-usage"                                         // 190
        ,"min-height"                                          // 191
        ,"min-width"                                           // 192
        ,"number-columns-repeated"                             // 193
        ,"number-columns-spanned"                              // 194
        ,"number-rows-spanned"                                 // 195
        ,"odd-or-even"                                         // 196
        ,"orphans"                                             // 197
        ,"overflow"                                            // 198

        ,"padding-after"                                       // 199
        ,"padding-after.length"                                // 200
        ,"padding-after.conditionality"                        // 201
        ,"padding-before"                                      // 202
        ,"padding-before.length"                               // 203
        ,"padding-before.conditionality"                       // 204
        ,"padding-end"                                         // 205
        ,"padding-end.length"                                  // 206
        ,"padding-end.conditionality"                          // 207
        ,"padding-start"                                       // 208
        ,"padding-start.length"                                // 209
        ,"padding-start.conditionality"                        // 210

        ,"padding-bottom"                                      // 211
        ,"padding-left"                                        // 212
        ,"padding-right"                                       // 213
        ,"padding-top"                                         // 214

        ,"page-height"                                         // 215
        ,"page-position"                                       // 216
        ,"page-width"                                          // 217
        ,"pause-after"                                         // 218
        ,"pause-before"                                        // 219
        ,"pitch"                                               // 220
        ,"pitch-range"                                         // 221
        ,"play-during"                                         // 222
        ,"precedence"                                          // 223
        ,"provisional-distance-between-starts"                 // 224
        ,"provisional-label-separation"                        // 225
        ,"reference-orientation"                               // 226
        ,"ref-id"                                              // 227
        ,"region-name"                                         // 228
        ,"relative-align"                                      // 229
        ,"relative-position"                                   // 230
        ,"rendering-intent"                                    // 231
        ,"retrieve-boundary"                                   // 232
        ,"retrieve-class-name"                                 // 233
        ,"retrieve-position"                                   // 234
        ,"richness"                                            // 235
        ,"right"                                               // 236
        ,"role"                                                // 237
        ,"rule-style"                                          // 238
        ,"rule-thickness"                                      // 239
        ,"scaling"                                             // 240
        ,"scaling-method"                                      // 241
        ,"score-spaces"                                        // 242
        ,"script"                                              // 243
        ,"show-destination"                                    // 244
        ,"source-document"                                     // 245

        ,"space-after"                                         // 246
        ,"space-after.minimum"                                 // 247
        ,"space-after.optimum"                                 // 248
        ,"space-after.maximum"                                 // 249
        ,"space-after.conditionality"                          // 250
        ,"space-after.precedence"                              // 251
        ,"space-before"                                        // 252
        ,"space-before.minimum"                                // 253
        ,"space-before.optimum"                                // 254
        ,"space-before.maximum"                                // 255
        ,"space-before.conditionality"                         // 256
        ,"space-before.precedence"                             // 257
        ,"space-end"                                           // 258
        ,"space-end.minimum"                                   // 259
        ,"space-end.optimum"                                   // 260
        ,"space-end.maximum"                                   // 261
        ,"space-end.conditionality"                            // 262
        ,"space-end.precedence"                                // 263
        ,"space-start"                                         // 264
        ,"space-start.minimum"                                 // 265
        ,"space-start.optimum"                                 // 266
        ,"space-start.maximum"                                 // 267
        ,"space-start.conditionality"                          // 268
        ,"space-start.precedence"                              // 269

        ,"margin-bottom"                                       // 270
        ,"margin-left"                                         // 271
        ,"margin-right"                                        // 272
        ,"margin-top"                                          // 273

        ,"span"                                                // 274
        ,"speak"                                               // 275
        ,"speak-header"                                        // 276
        ,"speak-numeral"                                       // 277
        ,"speak-punctuation"                                   // 278
        ,"speech-rate"                                         // 279
        ,"src"                                                 // 280
        ,"start-indent"                                        // 281
        ,"starting-state"                                      // 282
        ,"starts-row"                                          // 283
        ,"stress"                                              // 284
        ,"suppress-at-line-break"                              // 285
        ,"switch-to"                                           // 286
        ,"table-layout"                                        // 287
        ,"table-omit-footer-at-break"                          // 288
        ,"table-omit-header-at-break"                          // 289
        ,"target-presentation-context"                         // 290
        ,"target-processing-context"                           // 291
        ,"target-stylesheet"                                   // 292
        ,"text-align"                                          // 293
        ,"text-align-last"                                     // 294
        ,"text-altitude"                                       // 295
        ,"text-decoration"                                     // 296
        ,"text-depth"                                          // 297
        ,"text-indent"                                         // 298
        ,"text-shadow"                                         // 299
        ,"text-transform"                                      // 300
        ,"top"                                                 // 301
        ,"treat-as-word-space"                                 // 302
        ,"unicode-bidi"                                        // 303
        ,"visibility"                                          // 304
        ,"voice-family"                                        // 305
        ,"volume"                                              // 306
        ,"white-space-collapse"                                // 307
        ,"white-space-treatment"                               // 308
        ,"widows"                                              // 309
        ,"width"                                               // 310
        ,"word-spacing"                                        // 311
        ,"word-spacing-minimum"                                // 312
        ,"word-spacing-optimum"                                // 313
        ,"word-spacing-maximum"                                // 314
        ,"word-spacing-conditionality"                         // 315
        ,"word-spacing-precedence"                             // 316
        ,"wrap-option"                                         // 317
        ,"writing-mode"                                        // 318
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
