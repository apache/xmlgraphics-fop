/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */

package org.apache.fop.fo;

import java.util.HashMap;
import java.util.Set;
import org.apache.fop.fo.properties.*;

//import org.apache.fop.svg.*;

public class FOPropertyMapping implements Constants {

  private static Property.Maker[] s_htGeneric = new Property.Maker[PROPERTY_COUNT+1];
  /* s_htElementLists not currently used; apparently for specifying element-specific
   * property makers (instead of the default maker for a particular property); see
   * former org.apache.fop.fo.PropertyListBuilder 
   */
  private static HashMap s_htElementLists = new HashMap();
  private static HashMap s_htSubPropNames = new HashMap();
  private static HashMap s_htPropNames = new HashMap();
  private static HashMap s_htPropIds = new HashMap();
  
  /* PROPCLASS = ColorTypeProperty */

  /* PROPCLASS = EnumProperty */

  /* PROPCLASS = KeepProperty */

  /* PROPCLASS = CondLengthProperty */

  /* PROPCLASS = CondLengthProperty */

  /* PROPCLASS = LengthProperty */

  /* PROPCLASS = CondLengthProperty */

  /* PROPCLASS = LengthProperty */

  /* PROPCLASS = EnumProperty */

  /* PROPCLASS = EnumProperty */

  /* PROPCLASS = SpaceProperty */

  static {
    // Generate the generic mapping

  /* PROPCLASS = ColorTypeProperty */

  /* PROPCLASS = EnumProperty */

  /* PROPCLASS = KeepProperty */

  /* PROPCLASS = CondLengthProperty */

  /* PROPCLASS = CondLengthProperty */

  /* PROPCLASS = LengthProperty */

  /* PROPCLASS = CondLengthProperty */

  /* PROPCLASS = LengthProperty */

  /* PROPCLASS = EnumProperty */

  /* PROPCLASS = EnumProperty */

  /* PROPCLASS = SpaceProperty */
    addPropertyName("source-document", PR_SOURCE_DOCUMENT);
    s_htGeneric[PR_SOURCE_DOCUMENT] =SourceDocumentMaker.maker(PR_SOURCE_DOCUMENT);
    addPropertyName("role", PR_ROLE);
    s_htGeneric[PR_ROLE] =RoleMaker.maker(PR_ROLE);
    addPropertyName("absolute-position", PR_ABSOLUTE_POSITION);
    s_htGeneric[PR_ABSOLUTE_POSITION] =AbsolutePositionMaker.maker(PR_ABSOLUTE_POSITION);
    addPropertyName("top", PR_TOP);
    s_htGeneric[PR_TOP] =TopMaker.maker(PR_TOP);
    addPropertyName("right", PR_RIGHT);
    s_htGeneric[PR_RIGHT] =RightMaker.maker(PR_RIGHT);
    addPropertyName("bottom", PR_BOTTOM);
    s_htGeneric[PR_BOTTOM] =BottomMaker.maker(PR_BOTTOM);
    addPropertyName("left", PR_LEFT);
    s_htGeneric[PR_LEFT] =LeftMaker.maker(PR_LEFT);
    addPropertyName("azimuth", PR_AZIMUTH);
    s_htGeneric[PR_AZIMUTH] =AzimuthMaker.maker(PR_AZIMUTH);
    addPropertyName("cue-after", PR_CUE_AFTER);
    s_htGeneric[PR_CUE_AFTER] =CueAfterMaker.maker(PR_CUE_AFTER);
    addPropertyName("cue-before", PR_CUE_BEFORE);
    s_htGeneric[PR_CUE_BEFORE] =CueBeforeMaker.maker(PR_CUE_BEFORE);
    addPropertyName("elevation", PR_ELEVATION);
    s_htGeneric[PR_ELEVATION] =ElevationMaker.maker(PR_ELEVATION);
    addPropertyName("pause-after", PR_PAUSE_AFTER);
    s_htGeneric[PR_PAUSE_AFTER] =PauseAfterMaker.maker(PR_PAUSE_AFTER);
    addPropertyName("pause-before", PR_PAUSE_BEFORE);
    s_htGeneric[PR_PAUSE_BEFORE] =PauseBeforeMaker.maker(PR_PAUSE_BEFORE);
    addPropertyName("pitch", PR_PITCH);
    s_htGeneric[PR_PITCH] =PitchMaker.maker(PR_PITCH);
    addPropertyName("pitch-range", PR_PITCH_RANGE);
    s_htGeneric[PR_PITCH_RANGE] =PitchRangeMaker.maker(PR_PITCH_RANGE);
    addPropertyName("play-during", PR_PLAY_DURING);
    s_htGeneric[PR_PLAY_DURING] =PlayDuringMaker.maker(PR_PLAY_DURING);
    addPropertyName("richness", PR_RICHNESS);
    s_htGeneric[PR_RICHNESS] =RichnessMaker.maker(PR_RICHNESS);
    addPropertyName("speak", PR_SPEAK);
    s_htGeneric[PR_SPEAK] =SpeakMaker.maker(PR_SPEAK);
    addPropertyName("speak-header", PR_SPEAK_HEADER);
    s_htGeneric[PR_SPEAK_HEADER] =SpeakHeaderMaker.maker(PR_SPEAK_HEADER);
    addPropertyName("speak-numeral", PR_SPEAK_NUMERAL);
    s_htGeneric[PR_SPEAK_NUMERAL] =SpeakNumeralMaker.maker(PR_SPEAK_NUMERAL);
    addPropertyName("speak-punctuation", PR_SPEAK_PUNCTUATION);
    s_htGeneric[PR_SPEAK_PUNCTUATION] =SpeakPunctuationMaker.maker(PR_SPEAK_PUNCTUATION);
    addPropertyName("speech-rate", PR_SPEECH_RATE);
    s_htGeneric[PR_SPEECH_RATE] =SpeechRateMaker.maker(PR_SPEECH_RATE);
    addPropertyName("stress", PR_STRESS);
    s_htGeneric[PR_STRESS] =StressMaker.maker(PR_STRESS);
    addPropertyName("voice-family", PR_VOICE_FAMILY);
    s_htGeneric[PR_VOICE_FAMILY] =VoiceFamilyMaker.maker(PR_VOICE_FAMILY);
    addPropertyName("volume", PR_VOLUME);
    s_htGeneric[PR_VOLUME] =VolumeMaker.maker(PR_VOLUME);
    addPropertyName("background-attachment", PR_BACKGROUND_ATTACHMENT);
    s_htGeneric[PR_BACKGROUND_ATTACHMENT] =BackgroundAttachmentMaker.maker(PR_BACKGROUND_ATTACHMENT);
    addPropertyName("background-color", PR_BACKGROUND_COLOR);
    s_htGeneric[PR_BACKGROUND_COLOR] =BackgroundColorMaker.maker(PR_BACKGROUND_COLOR);
    addPropertyName("background-image", PR_BACKGROUND_IMAGE);
    s_htGeneric[PR_BACKGROUND_IMAGE] =BackgroundImageMaker.maker(PR_BACKGROUND_IMAGE);
    addPropertyName("background-repeat", PR_BACKGROUND_REPEAT);
    s_htGeneric[PR_BACKGROUND_REPEAT] =BackgroundRepeatMaker.maker(PR_BACKGROUND_REPEAT);
    addPropertyName("background-position-horizontal", PR_BACKGROUND_POSITION_HORIZONTAL);
    s_htGeneric[PR_BACKGROUND_POSITION_HORIZONTAL] =BackgroundPositionHorizontalMaker.maker(PR_BACKGROUND_POSITION_HORIZONTAL);
    addPropertyName("background-position-vertical", PR_BACKGROUND_POSITION_VERTICAL);
    s_htGeneric[PR_BACKGROUND_POSITION_VERTICAL] =BackgroundPositionVerticalMaker.maker(PR_BACKGROUND_POSITION_VERTICAL);
    addPropertyName("border-before-color", PR_BORDER_BEFORE_COLOR);
    s_htGeneric[PR_BORDER_BEFORE_COLOR] =BorderBeforeColorMaker.maker(PR_BORDER_BEFORE_COLOR);
    addPropertyName("border-before-style", PR_BORDER_BEFORE_STYLE);
    s_htGeneric[PR_BORDER_BEFORE_STYLE] =BorderBeforeStyleMaker.maker(PR_BORDER_BEFORE_STYLE);
    addPropertyName("border-before-width", PR_BORDER_BEFORE_WIDTH);
    s_htGeneric[PR_BORDER_BEFORE_WIDTH] =BorderBeforeWidthMaker.maker(PR_BORDER_BEFORE_WIDTH);
    addPropertyName("border-after-color", PR_BORDER_AFTER_COLOR);
    s_htGeneric[PR_BORDER_AFTER_COLOR] =BorderAfterColorMaker.maker(PR_BORDER_AFTER_COLOR);
    addPropertyName("border-after-style", PR_BORDER_AFTER_STYLE);
    s_htGeneric[PR_BORDER_AFTER_STYLE] =BorderAfterStyleMaker.maker(PR_BORDER_AFTER_STYLE);
    addPropertyName("border-after-width", PR_BORDER_AFTER_WIDTH);
    s_htGeneric[PR_BORDER_AFTER_WIDTH] =BorderAfterWidthMaker.maker(PR_BORDER_AFTER_WIDTH);
    addPropertyName("border-start-color", PR_BORDER_START_COLOR);
    s_htGeneric[PR_BORDER_START_COLOR] =BorderStartColorMaker.maker(PR_BORDER_START_COLOR);
    addPropertyName("border-start-style", PR_BORDER_START_STYLE);
    s_htGeneric[PR_BORDER_START_STYLE] =BorderStartStyleMaker.maker(PR_BORDER_START_STYLE);
    addPropertyName("border-start-width", PR_BORDER_START_WIDTH);
    s_htGeneric[PR_BORDER_START_WIDTH] =BorderStartWidthMaker.maker(PR_BORDER_START_WIDTH);
    addPropertyName("border-end-color", PR_BORDER_END_COLOR);
    s_htGeneric[PR_BORDER_END_COLOR] =BorderEndColorMaker.maker(PR_BORDER_END_COLOR);
    addPropertyName("border-end-style", PR_BORDER_END_STYLE);
    s_htGeneric[PR_BORDER_END_STYLE] =BorderEndStyleMaker.maker(PR_BORDER_END_STYLE);
    addPropertyName("border-end-width", PR_BORDER_END_WIDTH);
    s_htGeneric[PR_BORDER_END_WIDTH] =BorderEndWidthMaker.maker(PR_BORDER_END_WIDTH);
    addPropertyName("border-top-color", PR_BORDER_TOP_COLOR);
    s_htGeneric[PR_BORDER_TOP_COLOR] =BorderTopColorMaker.maker(PR_BORDER_TOP_COLOR);
    addPropertyName("border-top-style", PR_BORDER_TOP_STYLE);
    s_htGeneric[PR_BORDER_TOP_STYLE] =BorderTopStyleMaker.maker(PR_BORDER_TOP_STYLE);
    addPropertyName("border-top-width", PR_BORDER_TOP_WIDTH);
    s_htGeneric[PR_BORDER_TOP_WIDTH] =BorderTopWidthMaker.maker(PR_BORDER_TOP_WIDTH);
    addPropertyName("border-bottom-color", PR_BORDER_BOTTOM_COLOR);
    s_htGeneric[PR_BORDER_BOTTOM_COLOR] =BorderBottomColorMaker.maker(PR_BORDER_BOTTOM_COLOR);
    addPropertyName("border-bottom-style", PR_BORDER_BOTTOM_STYLE);
    s_htGeneric[PR_BORDER_BOTTOM_STYLE] =BorderBottomStyleMaker.maker(PR_BORDER_BOTTOM_STYLE);
    addPropertyName("border-bottom-width", PR_BORDER_BOTTOM_WIDTH);
    s_htGeneric[PR_BORDER_BOTTOM_WIDTH] =BorderBottomWidthMaker.maker(PR_BORDER_BOTTOM_WIDTH);
    addPropertyName("border-left-color", PR_BORDER_LEFT_COLOR);
    s_htGeneric[PR_BORDER_LEFT_COLOR] =BorderLeftColorMaker.maker(PR_BORDER_LEFT_COLOR);
    addPropertyName("border-left-style", PR_BORDER_LEFT_STYLE);
    s_htGeneric[PR_BORDER_LEFT_STYLE] =BorderLeftStyleMaker.maker(PR_BORDER_LEFT_STYLE);
    addPropertyName("border-left-width", PR_BORDER_LEFT_WIDTH);
    s_htGeneric[PR_BORDER_LEFT_WIDTH] =BorderLeftWidthMaker.maker(PR_BORDER_LEFT_WIDTH);
    addPropertyName("border-right-color", PR_BORDER_RIGHT_COLOR);
    s_htGeneric[PR_BORDER_RIGHT_COLOR] =BorderRightColorMaker.maker(PR_BORDER_RIGHT_COLOR);
    addPropertyName("border-right-style", PR_BORDER_RIGHT_STYLE);
    s_htGeneric[PR_BORDER_RIGHT_STYLE] =BorderRightStyleMaker.maker(PR_BORDER_RIGHT_STYLE);
    addPropertyName("border-right-width", PR_BORDER_RIGHT_WIDTH);
    s_htGeneric[PR_BORDER_RIGHT_WIDTH] =BorderRightWidthMaker.maker(PR_BORDER_RIGHT_WIDTH);
    addPropertyName("padding-before", PR_PADDING_BEFORE);
    s_htGeneric[PR_PADDING_BEFORE] =PaddingBeforeMaker.maker(PR_PADDING_BEFORE);
    addPropertyName("padding-after", PR_PADDING_AFTER);
    s_htGeneric[PR_PADDING_AFTER] =PaddingAfterMaker.maker(PR_PADDING_AFTER);
    addPropertyName("padding-start", PR_PADDING_START);
    s_htGeneric[PR_PADDING_START] =PaddingStartMaker.maker(PR_PADDING_START);
    addPropertyName("padding-end", PR_PADDING_END);
    s_htGeneric[PR_PADDING_END] =PaddingEndMaker.maker(PR_PADDING_END);
    addPropertyName("padding-top", PR_PADDING_TOP);
    s_htGeneric[PR_PADDING_TOP] =PaddingTopMaker.maker(PR_PADDING_TOP);
    addPropertyName("padding-bottom", PR_PADDING_BOTTOM);
    s_htGeneric[PR_PADDING_BOTTOM] =PaddingBottomMaker.maker(PR_PADDING_BOTTOM);
    addPropertyName("padding-left", PR_PADDING_LEFT);
    s_htGeneric[PR_PADDING_LEFT] =PaddingLeftMaker.maker(PR_PADDING_LEFT);
    addPropertyName("padding-right", PR_PADDING_RIGHT);
    s_htGeneric[PR_PADDING_RIGHT] =PaddingRightMaker.maker(PR_PADDING_RIGHT);
    addPropertyName("font-family", PR_FONT_FAMILY);
    s_htGeneric[PR_FONT_FAMILY] =FontFamilyMaker.maker(PR_FONT_FAMILY);
    addPropertyName("font-selection-strategy", PR_FONT_SELECTION_STRATEGY);
    s_htGeneric[PR_FONT_SELECTION_STRATEGY] =FontSelectionStrategyMaker.maker(PR_FONT_SELECTION_STRATEGY);
    addPropertyName("font-size", PR_FONT_SIZE);
    s_htGeneric[PR_FONT_SIZE] =FontSizeMaker.maker(PR_FONT_SIZE);
    addPropertyName("font-stretch", PR_FONT_STRETCH);
    s_htGeneric[PR_FONT_STRETCH] =FontStretchMaker.maker(PR_FONT_STRETCH);
    addPropertyName("font-size-adjust", PR_FONT_SIZE_ADJUST);
    s_htGeneric[PR_FONT_SIZE_ADJUST] =FontSizeAdjustMaker.maker(PR_FONT_SIZE_ADJUST);
    addPropertyName("font-style", PR_FONT_STYLE);
    s_htGeneric[PR_FONT_STYLE] =FontStyleMaker.maker(PR_FONT_STYLE);
    addPropertyName("font-variant", PR_FONT_VARIANT);
    s_htGeneric[PR_FONT_VARIANT] =FontVariantMaker.maker(PR_FONT_VARIANT);
    addPropertyName("font-weight", PR_FONT_WEIGHT);
    s_htGeneric[PR_FONT_WEIGHT] =FontWeightMaker.maker(PR_FONT_WEIGHT);
    addPropertyName("country", PR_COUNTRY);
    s_htGeneric[PR_COUNTRY] =CountryMaker.maker(PR_COUNTRY);
    addPropertyName("language", PR_LANGUAGE);
    s_htGeneric[PR_LANGUAGE] =LanguageMaker.maker(PR_LANGUAGE);
    addPropertyName("script", PR_SCRIPT);
    s_htGeneric[PR_SCRIPT] =ScriptMaker.maker(PR_SCRIPT);
    addPropertyName("hyphenate", PR_HYPHENATE);
    s_htGeneric[PR_HYPHENATE] =HyphenateMaker.maker(PR_HYPHENATE);
    addPropertyName("hyphenation-character", PR_HYPHENATION_CHARACTER);
    s_htGeneric[PR_HYPHENATION_CHARACTER] =HyphenationCharacterMaker.maker(PR_HYPHENATION_CHARACTER);
    addPropertyName("hyphenation-push-character-count", PR_HYPHENATION_PUSH_CHARACTER_COUNT);
    s_htGeneric[PR_HYPHENATION_PUSH_CHARACTER_COUNT] =HyphenationPushCharacterCountMaker.maker(PR_HYPHENATION_PUSH_CHARACTER_COUNT);
    addPropertyName("hyphenation-remain-character-count", PR_HYPHENATION_REMAIN_CHARACTER_COUNT);
    s_htGeneric[PR_HYPHENATION_REMAIN_CHARACTER_COUNT] =HyphenationRemainCharacterCountMaker.maker(PR_HYPHENATION_REMAIN_CHARACTER_COUNT);
    addPropertyName("margin-top", PR_MARGIN_TOP);
    s_htGeneric[PR_MARGIN_TOP] =MarginTopMaker.maker(PR_MARGIN_TOP);
    addPropertyName("margin-bottom", PR_MARGIN_BOTTOM);
    s_htGeneric[PR_MARGIN_BOTTOM] =MarginBottomMaker.maker(PR_MARGIN_BOTTOM);
    addPropertyName("margin-left", PR_MARGIN_LEFT);
    s_htGeneric[PR_MARGIN_LEFT] =MarginLeftMaker.maker(PR_MARGIN_LEFT);
    addPropertyName("margin-right", PR_MARGIN_RIGHT);
    s_htGeneric[PR_MARGIN_RIGHT] =MarginRightMaker.maker(PR_MARGIN_RIGHT);
    addPropertyName("space-before", PR_SPACE_BEFORE);
    s_htGeneric[PR_SPACE_BEFORE] =SpaceBeforeMaker.maker(PR_SPACE_BEFORE);
    addPropertyName("space-after", PR_SPACE_AFTER);
    s_htGeneric[PR_SPACE_AFTER] =SpaceAfterMaker.maker(PR_SPACE_AFTER);
    addPropertyName("start-indent", PR_START_INDENT);
    s_htGeneric[PR_START_INDENT] =StartIndentMaker.maker(PR_START_INDENT);
    addPropertyName("end-indent", PR_END_INDENT);
    s_htGeneric[PR_END_INDENT] =EndIndentMaker.maker(PR_END_INDENT);
    addPropertyName("space-end", PR_SPACE_END);
    s_htGeneric[PR_SPACE_END] =GenericSpace.maker(PR_SPACE_END);
    addPropertyName("space-start", PR_SPACE_START);
    s_htGeneric[PR_SPACE_START] =GenericSpace.maker(PR_SPACE_START);
    addPropertyName("relative-position", PR_RELATIVE_POSITION);
    s_htGeneric[PR_RELATIVE_POSITION] =RelativePositionMaker.maker(PR_RELATIVE_POSITION);
    addPropertyName("alignment-adjust", PR_ALIGNMENT_ADJUST);
    s_htGeneric[PR_ALIGNMENT_ADJUST] =AlignmentAdjustMaker.maker(PR_ALIGNMENT_ADJUST);
    addPropertyName("alignment-baseline", PR_ALIGNMENT_BASELINE);
    s_htGeneric[PR_ALIGNMENT_BASELINE] =AlignmentBaselineMaker.maker(PR_ALIGNMENT_BASELINE);
    addPropertyName("baseline-shift", PR_BASELINE_SHIFT);
    s_htGeneric[PR_BASELINE_SHIFT] =BaselineShiftMaker.maker(PR_BASELINE_SHIFT);
    addPropertyName("display-align", PR_DISPLAY_ALIGN);
    s_htGeneric[PR_DISPLAY_ALIGN] =DisplayAlignMaker.maker(PR_DISPLAY_ALIGN);
    addPropertyName("dominant-baseline", PR_DOMINANT_BASELINE);
    s_htGeneric[PR_DOMINANT_BASELINE] =DominantBaselineMaker.maker(PR_DOMINANT_BASELINE);
    addPropertyName("relative-align", PR_RELATIVE_ALIGN);
    s_htGeneric[PR_RELATIVE_ALIGN] =RelativeAlignMaker.maker(PR_RELATIVE_ALIGN);
    addPropertyName("block-progression-dimension", PR_BLOCK_PROGRESSION_DIMENSION);
    s_htGeneric[PR_BLOCK_PROGRESSION_DIMENSION] =BlockProgressionDimensionMaker.maker(PR_BLOCK_PROGRESSION_DIMENSION);
    addPropertyName("content-height", PR_CONTENT_HEIGHT);
    s_htGeneric[PR_CONTENT_HEIGHT] =ContentHeightMaker.maker(PR_CONTENT_HEIGHT);
    addPropertyName("content-width", PR_CONTENT_WIDTH);
    s_htGeneric[PR_CONTENT_WIDTH] =ContentWidthMaker.maker(PR_CONTENT_WIDTH);
    addPropertyName("height", PR_HEIGHT);
    s_htGeneric[PR_HEIGHT] =HeightMaker.maker(PR_HEIGHT);
    addPropertyName("inline-progression-dimension", PR_INLINE_PROGRESSION_DIMENSION);
    s_htGeneric[PR_INLINE_PROGRESSION_DIMENSION] =InlineProgressionDimensionMaker.maker(PR_INLINE_PROGRESSION_DIMENSION);
    addPropertyName("max-height", PR_MAX_HEIGHT);
    s_htGeneric[PR_MAX_HEIGHT] =MaxHeightMaker.maker(PR_MAX_HEIGHT);
    addPropertyName("max-width", PR_MAX_WIDTH);
    s_htGeneric[PR_MAX_WIDTH] =MaxWidthMaker.maker(PR_MAX_WIDTH);
    addPropertyName("min-height", PR_MIN_HEIGHT);
    s_htGeneric[PR_MIN_HEIGHT] =MinHeightMaker.maker(PR_MIN_HEIGHT);
    addPropertyName("min-width", PR_MIN_WIDTH);
    s_htGeneric[PR_MIN_WIDTH] =MinWidthMaker.maker(PR_MIN_WIDTH);
    addPropertyName("scaling", PR_SCALING);
    s_htGeneric[PR_SCALING] =ScalingMaker.maker(PR_SCALING);
    addPropertyName("scaling-method", PR_SCALING_METHOD);
    s_htGeneric[PR_SCALING_METHOD] =ScalingMethodMaker.maker(PR_SCALING_METHOD);
    addPropertyName("width", PR_WIDTH);
    s_htGeneric[PR_WIDTH] =WidthMaker.maker(PR_WIDTH);
    addPropertyName("hyphenation-keep", PR_HYPHENATION_KEEP);
    s_htGeneric[PR_HYPHENATION_KEEP] =HyphenationKeepMaker.maker(PR_HYPHENATION_KEEP);
    addPropertyName("hyphenation-ladder-count", PR_HYPHENATION_LADDER_COUNT);
    s_htGeneric[PR_HYPHENATION_LADDER_COUNT] =HyphenationLadderCountMaker.maker(PR_HYPHENATION_LADDER_COUNT);
    addPropertyName("last-line-end-indent", PR_LAST_LINE_END_INDENT);
    s_htGeneric[PR_LAST_LINE_END_INDENT] =LastLineEndIndentMaker.maker(PR_LAST_LINE_END_INDENT);
    addPropertyName("line-height", PR_LINE_HEIGHT);
    s_htGeneric[PR_LINE_HEIGHT] =LineHeightMaker.maker(PR_LINE_HEIGHT);
    addPropertyName("line-height-shift-adjustment", PR_LINE_HEIGHT_SHIFT_ADJUSTMENT);
    s_htGeneric[PR_LINE_HEIGHT_SHIFT_ADJUSTMENT] =LineHeightShiftAdjustmentMaker.maker(PR_LINE_HEIGHT_SHIFT_ADJUSTMENT);
    addPropertyName("line-stacking-strategy", PR_LINE_STACKING_STRATEGY);
    s_htGeneric[PR_LINE_STACKING_STRATEGY] =LineStackingStrategyMaker.maker(PR_LINE_STACKING_STRATEGY);
    addPropertyName("linefeed-treatment", PR_LINEFEED_TREATMENT);
    s_htGeneric[PR_LINEFEED_TREATMENT] =LinefeedTreatmentMaker.maker(PR_LINEFEED_TREATMENT);
    addPropertyName("space-treatment", PR_SPACE_TREATMENT);
    s_htGeneric[PR_SPACE_TREATMENT] =SpaceTreatmentMaker.maker(PR_SPACE_TREATMENT);
    addPropertyName("text-align", PR_TEXT_ALIGN);
    s_htGeneric[PR_TEXT_ALIGN] =TextAlignMaker.maker(PR_TEXT_ALIGN);
    addPropertyName("text-align-last", PR_TEXT_ALIGN_LAST);
    s_htGeneric[PR_TEXT_ALIGN_LAST] =TextAlignLastMaker.maker(PR_TEXT_ALIGN_LAST);
    addPropertyName("text-indent", PR_TEXT_INDENT);
    s_htGeneric[PR_TEXT_INDENT] =TextIndentMaker.maker(PR_TEXT_INDENT);
    addPropertyName("white-space-collapse", PR_WHITE_SPACE_COLLAPSE);
    s_htGeneric[PR_WHITE_SPACE_COLLAPSE] =WhiteSpaceCollapseMaker.maker(PR_WHITE_SPACE_COLLAPSE);
    addPropertyName("wrap-option", PR_WRAP_OPTION);
    s_htGeneric[PR_WRAP_OPTION] =WrapOptionMaker.maker(PR_WRAP_OPTION);
    addPropertyName("character", PR_CHARACTER);
    s_htGeneric[PR_CHARACTER] =CharacterMaker.maker(PR_CHARACTER);
    addPropertyName("letter-spacing", PR_LETTER_SPACING);
    s_htGeneric[PR_LETTER_SPACING] =LetterSpacingMaker.maker(PR_LETTER_SPACING);
    addPropertyName("suppress-at-line-break", PR_SUPPRESS_AT_LINE_BREAK);
    s_htGeneric[PR_SUPPRESS_AT_LINE_BREAK] =SuppressAtLineBreakMaker.maker(PR_SUPPRESS_AT_LINE_BREAK);
    addPropertyName("text-decoration", PR_TEXT_DECORATION);
    s_htGeneric[PR_TEXT_DECORATION] =TextDecorationMaker.maker(PR_TEXT_DECORATION);
    addPropertyName("text-shadow", PR_TEXT_SHADOW);
    s_htGeneric[PR_TEXT_SHADOW] =TextShadowMaker.maker(PR_TEXT_SHADOW);
    addPropertyName("text-transform", PR_TEXT_TRANSFORM);
    s_htGeneric[PR_TEXT_TRANSFORM] =TextTransformMaker.maker(PR_TEXT_TRANSFORM);
    addPropertyName("treat-as-word-space", PR_TREAT_AS_WORD_SPACE);
    s_htGeneric[PR_TREAT_AS_WORD_SPACE] =TreatAsWordSpaceMaker.maker(PR_TREAT_AS_WORD_SPACE);
    addPropertyName("word-spacing", PR_WORD_SPACING);
    s_htGeneric[PR_WORD_SPACING] =WordSpacingMaker.maker(PR_WORD_SPACING);
    addPropertyName("color", PR_COLOR);
    s_htGeneric[PR_COLOR] =ColorMaker.maker(PR_COLOR);
    addPropertyName("color-profile-name", PR_COLOR_PROFILE_NAME);
    s_htGeneric[PR_COLOR_PROFILE_NAME] =ColorProfileNameMaker.maker(PR_COLOR_PROFILE_NAME);
    addPropertyName("rendering-intent", PR_RENDERING_INTENT);
    s_htGeneric[PR_RENDERING_INTENT] =RenderingIntentMaker.maker(PR_RENDERING_INTENT);
    addPropertyName("clear", PR_CLEAR);
    s_htGeneric[PR_CLEAR] =ClearMaker.maker(PR_CLEAR);
    addPropertyName("float", PR_FLOAT);
    s_htGeneric[PR_FLOAT] =FloatMaker.maker(PR_FLOAT);
    addPropertyName("break-after", PR_BREAK_AFTER);
    s_htGeneric[PR_BREAK_AFTER] =GenericBreak.maker(PR_BREAK_AFTER);
    addPropertyName("break-before", PR_BREAK_BEFORE);
    s_htGeneric[PR_BREAK_BEFORE] =GenericBreak.maker(PR_BREAK_BEFORE);
    addPropertyName("keep-together", PR_KEEP_TOGETHER);
    s_htGeneric[PR_KEEP_TOGETHER] =KeepTogetherMaker.maker(PR_KEEP_TOGETHER);
    addPropertyName("keep-with-next", PR_KEEP_WITH_NEXT);
    s_htGeneric[PR_KEEP_WITH_NEXT] =KeepWithNextMaker.maker(PR_KEEP_WITH_NEXT);
    addPropertyName("keep-with-previous", PR_KEEP_WITH_PREVIOUS);
    s_htGeneric[PR_KEEP_WITH_PREVIOUS] =KeepWithPreviousMaker.maker(PR_KEEP_WITH_PREVIOUS);
    addPropertyName("orphans", PR_ORPHANS);
    s_htGeneric[PR_ORPHANS] =OrphansMaker.maker(PR_ORPHANS);
    addPropertyName("widows", PR_WIDOWS);
    s_htGeneric[PR_WIDOWS] =WidowsMaker.maker(PR_WIDOWS);
    addPropertyName("clip", PR_CLIP);
    s_htGeneric[PR_CLIP] =ClipMaker.maker(PR_CLIP);
    addPropertyName("overflow", PR_OVERFLOW);
    s_htGeneric[PR_OVERFLOW] =OverflowMaker.maker(PR_OVERFLOW);
    addPropertyName("reference-orientation", PR_REFERENCE_ORIENTATION);
    s_htGeneric[PR_REFERENCE_ORIENTATION] =ReferenceOrientationMaker.maker(PR_REFERENCE_ORIENTATION);
    addPropertyName("span", PR_SPAN);
    s_htGeneric[PR_SPAN] =SpanMaker.maker(PR_SPAN);
    addPropertyName("leader-alignment", PR_LEADER_ALIGNMENT);
    s_htGeneric[PR_LEADER_ALIGNMENT] =LeaderAlignmentMaker.maker(PR_LEADER_ALIGNMENT);
    addPropertyName("leader-pattern", PR_LEADER_PATTERN);
    s_htGeneric[PR_LEADER_PATTERN] =LeaderPatternMaker.maker(PR_LEADER_PATTERN);
    addPropertyName("leader-pattern-width", PR_LEADER_PATTERN_WIDTH);
    s_htGeneric[PR_LEADER_PATTERN_WIDTH] =LeaderPatternWidthMaker.maker(PR_LEADER_PATTERN_WIDTH);
    addPropertyName("leader-length", PR_LEADER_LENGTH);
    s_htGeneric[PR_LEADER_LENGTH] =LeaderLengthMaker.maker(PR_LEADER_LENGTH);
    addPropertyName("rule-style", PR_RULE_STYLE);
    s_htGeneric[PR_RULE_STYLE] =RuleStyleMaker.maker(PR_RULE_STYLE);
    addPropertyName("rule-thickness", PR_RULE_THICKNESS);
    s_htGeneric[PR_RULE_THICKNESS] =RuleThicknessMaker.maker(PR_RULE_THICKNESS);
    addPropertyName("active-state", PR_ACTIVE_STATE);
    s_htGeneric[PR_ACTIVE_STATE] =ActiveStateMaker.maker(PR_ACTIVE_STATE);
    addPropertyName("auto-restore", PR_AUTO_RESTORE);
    s_htGeneric[PR_AUTO_RESTORE] =AutoRestoreMaker.maker(PR_AUTO_RESTORE);
    addPropertyName("case-name", PR_CASE_NAME);
    s_htGeneric[PR_CASE_NAME] =CaseNameMaker.maker(PR_CASE_NAME);
    addPropertyName("case-title", PR_CASE_TITLE);
    s_htGeneric[PR_CASE_TITLE] =CaseTitleMaker.maker(PR_CASE_TITLE);
    addPropertyName("destination-placement-offset", PR_DESTINATION_PLACEMENT_OFFSET);
    s_htGeneric[PR_DESTINATION_PLACEMENT_OFFSET] =DestinationPlacementOffsetMaker.maker(PR_DESTINATION_PLACEMENT_OFFSET);
    addPropertyName("external-destination", PR_EXTERNAL_DESTINATION);
    s_htGeneric[PR_EXTERNAL_DESTINATION] =ExternalDestinationMaker.maker(PR_EXTERNAL_DESTINATION);
    addPropertyName("indicate-destination", PR_INDICATE_DESTINATION);
    s_htGeneric[PR_INDICATE_DESTINATION] =IndicateDestinationMaker.maker(PR_INDICATE_DESTINATION);
    addPropertyName("internal-destination", PR_INTERNAL_DESTINATION);
    s_htGeneric[PR_INTERNAL_DESTINATION] =InternalDestinationMaker.maker(PR_INTERNAL_DESTINATION);
    addPropertyName("show-destination", PR_SHOW_DESTINATION);
    s_htGeneric[PR_SHOW_DESTINATION] =ShowDestinationMaker.maker(PR_SHOW_DESTINATION);
    addPropertyName("starting-state", PR_STARTING_STATE);
    s_htGeneric[PR_STARTING_STATE] =StartingStateMaker.maker(PR_STARTING_STATE);
    addPropertyName("switch-to", PR_SWITCH_TO);
    s_htGeneric[PR_SWITCH_TO] =SwitchToMaker.maker(PR_SWITCH_TO);
    addPropertyName("target-presentation-context", PR_TARGET_PRESENTATION_CONTEXT);
    s_htGeneric[PR_TARGET_PRESENTATION_CONTEXT] =TargetPresentationContextMaker.maker(PR_TARGET_PRESENTATION_CONTEXT);
    addPropertyName("target-processing-context", PR_TARGET_PROCESSING_CONTEXT);
    s_htGeneric[PR_TARGET_PROCESSING_CONTEXT] =TargetProcessingContextMaker.maker(PR_TARGET_PROCESSING_CONTEXT);
    addPropertyName("target-stylesheet", PR_TARGET_STYLESHEET);
    s_htGeneric[PR_TARGET_STYLESHEET] =TargetStylesheetMaker.maker(PR_TARGET_STYLESHEET);
    addPropertyName("marker-class-name", PR_MARKER_CLASS_NAME);
    s_htGeneric[PR_MARKER_CLASS_NAME] =MarkerClassNameMaker.maker(PR_MARKER_CLASS_NAME);
    addPropertyName("retrieve-class-name", PR_RETRIEVE_CLASS_NAME);
    s_htGeneric[PR_RETRIEVE_CLASS_NAME] =RetrieveClassNameMaker.maker(PR_RETRIEVE_CLASS_NAME);
    addPropertyName("retrieve-position", PR_RETRIEVE_POSITION);
    s_htGeneric[PR_RETRIEVE_POSITION] =RetrievePositionMaker.maker(PR_RETRIEVE_POSITION);
    addPropertyName("retrieve-boundary", PR_RETRIEVE_BOUNDARY);
    s_htGeneric[PR_RETRIEVE_BOUNDARY] =RetrieveBoundaryMaker.maker(PR_RETRIEVE_BOUNDARY);
    addPropertyName("format", PR_FORMAT);
    s_htGeneric[PR_FORMAT] =FormatMaker.maker(PR_FORMAT);
    addPropertyName("grouping-separator", PR_GROUPING_SEPARATOR);
    s_htGeneric[PR_GROUPING_SEPARATOR] =GroupingSeparatorMaker.maker(PR_GROUPING_SEPARATOR);
    addPropertyName("grouping-size", PR_GROUPING_SIZE);
    s_htGeneric[PR_GROUPING_SIZE] =GroupingSizeMaker.maker(PR_GROUPING_SIZE);
    addPropertyName("letter-value", PR_LETTER_VALUE);
    s_htGeneric[PR_LETTER_VALUE] =LetterValueMaker.maker(PR_LETTER_VALUE);
    addPropertyName("blank-or-not-blank", PR_BLANK_OR_NOT_BLANK);
    s_htGeneric[PR_BLANK_OR_NOT_BLANK] =BlankOrNotBlankMaker.maker(PR_BLANK_OR_NOT_BLANK);
    addPropertyName("column-count", PR_COLUMN_COUNT);
    s_htGeneric[PR_COLUMN_COUNT] =ColumnCountMaker.maker(PR_COLUMN_COUNT);
    addPropertyName("column-gap", PR_COLUMN_GAP);
    s_htGeneric[PR_COLUMN_GAP] =ColumnGapMaker.maker(PR_COLUMN_GAP);
    addPropertyName("extent", PR_EXTENT);
    s_htGeneric[PR_EXTENT] =ExtentMaker.maker(PR_EXTENT);
    addPropertyName("flow-name", PR_FLOW_NAME);
    s_htGeneric[PR_FLOW_NAME] =FlowNameMaker.maker(PR_FLOW_NAME);
    addPropertyName("force-page-count", PR_FORCE_PAGE_COUNT);
    s_htGeneric[PR_FORCE_PAGE_COUNT] =ForcePageCountMaker.maker(PR_FORCE_PAGE_COUNT);
    addPropertyName("initial-page-number", PR_INITIAL_PAGE_NUMBER);
    s_htGeneric[PR_INITIAL_PAGE_NUMBER] =InitialPageNumberMaker.maker(PR_INITIAL_PAGE_NUMBER);
    addPropertyName("master-name", PR_MASTER_NAME);
    s_htGeneric[PR_MASTER_NAME] =MasterNameMaker.maker(PR_MASTER_NAME);
    addPropertyName("master-reference", PR_MASTER_REFERENCE);
    s_htGeneric[PR_MASTER_REFERENCE] =MasterReferenceMaker.maker(PR_MASTER_REFERENCE);
    addPropertyName("maximum-repeats", PR_MAXIMUM_REPEATS);
    s_htGeneric[PR_MAXIMUM_REPEATS] =MaximumRepeatsMaker.maker(PR_MAXIMUM_REPEATS);
    addPropertyName("media-usage", PR_MEDIA_USAGE);
    s_htGeneric[PR_MEDIA_USAGE] =MediaUsageMaker.maker(PR_MEDIA_USAGE);
    addPropertyName("odd-or-even", PR_ODD_OR_EVEN);
    s_htGeneric[PR_ODD_OR_EVEN] =OddOrEvenMaker.maker(PR_ODD_OR_EVEN);
    addPropertyName("page-height", PR_PAGE_HEIGHT);
    s_htGeneric[PR_PAGE_HEIGHT] =PageHeightMaker.maker(PR_PAGE_HEIGHT);
    addPropertyName("page-position", PR_PAGE_POSITION);
    s_htGeneric[PR_PAGE_POSITION] =PagePositionMaker.maker(PR_PAGE_POSITION);
    addPropertyName("page-width", PR_PAGE_WIDTH);
    s_htGeneric[PR_PAGE_WIDTH] =PageWidthMaker.maker(PR_PAGE_WIDTH);
    addPropertyName("precedence", PR_PRECEDENCE);
    s_htGeneric[PR_PRECEDENCE] =PrecedenceMaker.maker(PR_PRECEDENCE);
    addPropertyName("region-name", PR_REGION_NAME);
    s_htGeneric[PR_REGION_NAME] =RegionNameMaker.maker(PR_REGION_NAME);
    addPropertyName("border-after-precedence", PR_BORDER_AFTER_PRECEDENCE);
    s_htGeneric[PR_BORDER_AFTER_PRECEDENCE] =BorderAfterPrecedenceMaker.maker(PR_BORDER_AFTER_PRECEDENCE);
    addPropertyName("border-before-precedence", PR_BORDER_BEFORE_PRECEDENCE);
    s_htGeneric[PR_BORDER_BEFORE_PRECEDENCE] =BorderBeforePrecedenceMaker.maker(PR_BORDER_BEFORE_PRECEDENCE);
    addPropertyName("border-collapse", PR_BORDER_COLLAPSE);
    s_htGeneric[PR_BORDER_COLLAPSE] =BorderCollapseMaker.maker(PR_BORDER_COLLAPSE);
    addPropertyName("border-end-precedence", PR_BORDER_END_PRECEDENCE);
    s_htGeneric[PR_BORDER_END_PRECEDENCE] =BorderEndPrecedenceMaker.maker(PR_BORDER_END_PRECEDENCE);
    addPropertyName("border-separation", PR_BORDER_SEPARATION);
    s_htGeneric[PR_BORDER_SEPARATION] =BorderSeparationMaker.maker(PR_BORDER_SEPARATION);
    addPropertyName("border-start-precedence", PR_BORDER_START_PRECEDENCE);
    s_htGeneric[PR_BORDER_START_PRECEDENCE] =BorderStartPrecedenceMaker.maker(PR_BORDER_START_PRECEDENCE);
    addPropertyName("caption-side", PR_CAPTION_SIDE);
    s_htGeneric[PR_CAPTION_SIDE] =CaptionSideMaker.maker(PR_CAPTION_SIDE);
    addPropertyName("column-number", PR_COLUMN_NUMBER);
    s_htGeneric[PR_COLUMN_NUMBER] =ColumnNumberMaker.maker(PR_COLUMN_NUMBER);
    addPropertyName("column-width", PR_COLUMN_WIDTH);
    s_htGeneric[PR_COLUMN_WIDTH] =ColumnWidthMaker.maker(PR_COLUMN_WIDTH);
    addPropertyName("empty-cells", PR_EMPTY_CELLS);
    s_htGeneric[PR_EMPTY_CELLS] =EmptyCellsMaker.maker(PR_EMPTY_CELLS);
    addPropertyName("ends-row", PR_ENDS_ROW);
    s_htGeneric[PR_ENDS_ROW] =EndsRowMaker.maker(PR_ENDS_ROW);
    addPropertyName("number-columns-repeated", PR_NUMBER_COLUMNS_REPEATED);
    s_htGeneric[PR_NUMBER_COLUMNS_REPEATED] =NumberColumnsRepeatedMaker.maker(PR_NUMBER_COLUMNS_REPEATED);
    addPropertyName("number-columns-spanned", PR_NUMBER_COLUMNS_SPANNED);
    s_htGeneric[PR_NUMBER_COLUMNS_SPANNED] =NumberColumnsSpannedMaker.maker(PR_NUMBER_COLUMNS_SPANNED);
    addPropertyName("number-rows-spanned", PR_NUMBER_ROWS_SPANNED);
    s_htGeneric[PR_NUMBER_ROWS_SPANNED] =NumberRowsSpannedMaker.maker(PR_NUMBER_ROWS_SPANNED);
    addPropertyName("starts-row", PR_STARTS_ROW);
    s_htGeneric[PR_STARTS_ROW] =StartsRowMaker.maker(PR_STARTS_ROW);
    addPropertyName("table-layout", PR_TABLE_LAYOUT);
    s_htGeneric[PR_TABLE_LAYOUT] =TableLayoutMaker.maker(PR_TABLE_LAYOUT);
    addPropertyName("table-omit-footer-at-break", PR_TABLE_OMIT_FOOTER_AT_BREAK);
    s_htGeneric[PR_TABLE_OMIT_FOOTER_AT_BREAK] =TableOmitFooterAtBreakMaker.maker(PR_TABLE_OMIT_FOOTER_AT_BREAK);
    addPropertyName("table-omit-header-at-break", PR_TABLE_OMIT_HEADER_AT_BREAK);
    s_htGeneric[PR_TABLE_OMIT_HEADER_AT_BREAK] =TableOmitHeaderAtBreakMaker.maker(PR_TABLE_OMIT_HEADER_AT_BREAK);
    addPropertyName("direction", PR_DIRECTION);
    s_htGeneric[PR_DIRECTION] =DirectionMaker.maker(PR_DIRECTION);
    addPropertyName("glyph-orientation-horizontal", PR_GLYPH_ORIENTATION_HORIZONTAL);
    s_htGeneric[PR_GLYPH_ORIENTATION_HORIZONTAL] =GlyphOrientationHorizontalMaker.maker(PR_GLYPH_ORIENTATION_HORIZONTAL);
    addPropertyName("glyph-orientation-vertical", PR_GLYPH_ORIENTATION_VERTICAL);
    s_htGeneric[PR_GLYPH_ORIENTATION_VERTICAL] =GlyphOrientationVerticalMaker.maker(PR_GLYPH_ORIENTATION_VERTICAL);
    addPropertyName("text-altitude", PR_TEXT_ALTITUDE);
    s_htGeneric[PR_TEXT_ALTITUDE] =TextAltitudeMaker.maker(PR_TEXT_ALTITUDE);
    addPropertyName("text-depth", PR_TEXT_DEPTH);
    s_htGeneric[PR_TEXT_DEPTH] =TextDepthMaker.maker(PR_TEXT_DEPTH);
    addPropertyName("unicode-bidi", PR_UNICODE_BIDI);
    s_htGeneric[PR_UNICODE_BIDI] =UnicodeBidiMaker.maker(PR_UNICODE_BIDI);
    addPropertyName("writing-mode", PR_WRITING_MODE);
    s_htGeneric[PR_WRITING_MODE] =WritingModeMaker.maker(PR_WRITING_MODE);
    addPropertyName("content-type", PR_CONTENT_TYPE);
    s_htGeneric[PR_CONTENT_TYPE] =ContentTypeMaker.maker(PR_CONTENT_TYPE);
    addPropertyName("id", PR_ID);
    s_htGeneric[PR_ID] =IdMaker.maker(PR_ID);
    addPropertyName("provisional-label-separation", PR_PROVISIONAL_LABEL_SEPARATION);
    s_htGeneric[PR_PROVISIONAL_LABEL_SEPARATION] =ProvisionalLabelSeparationMaker.maker(PR_PROVISIONAL_LABEL_SEPARATION);
    addPropertyName("provisional-distance-between-starts", PR_PROVISIONAL_DISTANCE_BETWEEN_STARTS);
    s_htGeneric[PR_PROVISIONAL_DISTANCE_BETWEEN_STARTS] =ProvisionalDistanceBetweenStartsMaker.maker(PR_PROVISIONAL_DISTANCE_BETWEEN_STARTS);
    addPropertyName("ref-id", PR_REF_ID);
    s_htGeneric[PR_REF_ID] =RefIdMaker.maker(PR_REF_ID);
    addPropertyName("score-spaces", PR_SCORE_SPACES);
    s_htGeneric[PR_SCORE_SPACES] =ScoreSpacesMaker.maker(PR_SCORE_SPACES);
    addPropertyName("src", PR_SRC);
    s_htGeneric[PR_SRC] =SrcMaker.maker(PR_SRC);
    addPropertyName("visibility", PR_VISIBILITY);
    s_htGeneric[PR_VISIBILITY] =VisibilityMaker.maker(PR_VISIBILITY);
    addPropertyName("z-index", PR_Z_INDEX);
    s_htGeneric[PR_Z_INDEX] =ZIndexMaker.maker(PR_Z_INDEX);
    addPropertyName("background", PR_BACKGROUND);
    s_htGeneric[PR_BACKGROUND] =BackgroundMaker.maker(PR_BACKGROUND);
    addPropertyName("background-position", PR_BACKGROUND_POSITION);
    s_htGeneric[PR_BACKGROUND_POSITION] =BackgroundPositionMaker.maker(PR_BACKGROUND_POSITION);
    addPropertyName("border", PR_BORDER);
    s_htGeneric[PR_BORDER] =BorderMaker.maker(PR_BORDER);
    addPropertyName("border-bottom", PR_BORDER_BOTTOM);
    s_htGeneric[PR_BORDER_BOTTOM] =BorderBottomMaker.maker(PR_BORDER_BOTTOM);
    addPropertyName("border-color", PR_BORDER_COLOR);
    s_htGeneric[PR_BORDER_COLOR] =BorderColorMaker.maker(PR_BORDER_COLOR);
    addPropertyName("border-left", PR_BORDER_LEFT);
    s_htGeneric[PR_BORDER_LEFT] =BorderLeftMaker.maker(PR_BORDER_LEFT);
    addPropertyName("border-right", PR_BORDER_RIGHT);
    s_htGeneric[PR_BORDER_RIGHT] =BorderRightMaker.maker(PR_BORDER_RIGHT);
    addPropertyName("border-style", PR_BORDER_STYLE);
    s_htGeneric[PR_BORDER_STYLE] =BorderStyleMaker.maker(PR_BORDER_STYLE);
    addPropertyName("border-spacing", PR_BORDER_SPACING);
    s_htGeneric[PR_BORDER_SPACING] =BorderSpacingMaker.maker(PR_BORDER_SPACING);
    addPropertyName("border-top", PR_BORDER_TOP);
    s_htGeneric[PR_BORDER_TOP] =BorderTopMaker.maker(PR_BORDER_TOP);
    addPropertyName("border-width", PR_BORDER_WIDTH);
    s_htGeneric[PR_BORDER_WIDTH] =BorderWidthMaker.maker(PR_BORDER_WIDTH);
    addPropertyName("cue", PR_CUE);
    s_htGeneric[PR_CUE] =CueMaker.maker(PR_CUE);
    addPropertyName("font", PR_FONT);
    s_htGeneric[PR_FONT] =FontMaker.maker(PR_FONT);
    addPropertyName("margin", PR_MARGIN);
    s_htGeneric[PR_MARGIN] =MarginMaker.maker(PR_MARGIN);
    addPropertyName("padding", PR_PADDING);
    s_htGeneric[PR_PADDING] =PaddingMaker.maker(PR_PADDING);
    addPropertyName("page-break-after", PR_PAGE_BREAK_AFTER);
    s_htGeneric[PR_PAGE_BREAK_AFTER] =PageBreakAfterMaker.maker(PR_PAGE_BREAK_AFTER);
    addPropertyName("page-break-before", PR_PAGE_BREAK_BEFORE);
    s_htGeneric[PR_PAGE_BREAK_BEFORE] =PageBreakBeforeMaker.maker(PR_PAGE_BREAK_BEFORE);
    addPropertyName("page-break-inside", PR_PAGE_BREAK_INSIDE);
    s_htGeneric[PR_PAGE_BREAK_INSIDE] =PageBreakInsideMaker.maker(PR_PAGE_BREAK_INSIDE);
    addPropertyName("pause", PR_PAUSE);
    s_htGeneric[PR_PAUSE] =PauseMaker.maker(PR_PAUSE);
    addPropertyName("position", PR_POSITION);
    s_htGeneric[PR_POSITION] =PositionMaker.maker(PR_POSITION);
    addPropertyName("size", PR_SIZE);
    s_htGeneric[PR_SIZE] =SizeMaker.maker(PR_SIZE);
    addPropertyName("vertical-align", PR_VERTICAL_ALIGN);
    s_htGeneric[PR_VERTICAL_ALIGN] =VerticalAlignMaker.maker(PR_VERTICAL_ALIGN);
    addPropertyName("white-space-treatment", PR_WHITE_SPACE_TREATMENT);
    s_htGeneric[PR_WHITE_SPACE_TREATMENT] =WhiteSpaceTreatmentMaker.maker(PR_WHITE_SPACE_TREATMENT);
    addPropertyName("xml:lang", PR_XML_LANG);
    s_htGeneric[PR_XML_LANG] =XMLLangMaker.maker(PR_XML_LANG);

  }


  public static Property.Maker[] getGenericMappings() {
    return s_htGeneric;
  }

  public static Set getElementMappings() {
    return s_htElementLists.keySet();
  }

  public static Property.Maker[] getElementMapping(int elemName) {
    return (Property.Maker[])s_htElementLists.get(new Integer(elemName));
  }

  public static int getPropertyId(String name) {
    // check to see if base.compound or just base property
    int sepchar = name.indexOf('.');

    if (sepchar > -1) {
        Integer baseId = (Integer) s_htPropNames.get(name.substring(0, sepchar));
        if (baseId == null) {
            return -1;
        } else {
            int cmpdId = getSubPropertyId(name.substring(sepchar + 1));
            if (cmpdId == -1) {
                return -1;
            } else {
                return baseId.intValue() + cmpdId;
            }
        }
    } else {
        Integer baseId = (Integer) s_htPropNames.get(name);
        if (baseId == null)
            return -1;
        return baseId.intValue();
    }
  }

  public static int getSubPropertyId(String name) {
  	Integer i = (Integer) s_htSubPropNames.get(name);
  	if (i == null)
  		return -1;
    return i.intValue();
  }
  
  // returns a property, compound, or property.compound name
  public static String getPropertyName(int id) {
    if (((id & Constants.COMPOUND_MASK) == 0) 
        || ((id & Constants.PROPERTY_MASK) == 0)) {
        return (String) s_htPropIds.get(new Integer(id));
    } else {
        return (String) s_htPropIds.get(new Integer(
            id & Constants.PROPERTY_MASK)) + "." + s_htPropIds.get(
            new Integer(id & Constants.COMPOUND_MASK));
    }
  }

  static {
    addSubPropertyName("length", CP_LENGTH);
    addSubPropertyName("conditionality", CP_CONDITIONALITY);
    addSubPropertyName("block-progression-direction", CP_BLOCK_PROGRESSION_DIRECTION);
    addSubPropertyName("inline-progression-direction", CP_INLINE_PROGRESSION_DIRECTION);
    addSubPropertyName("within-line", CP_WITHIN_LINE);
    addSubPropertyName("within-column", CP_WITHIN_COLUMN);
    addSubPropertyName("within-page", CP_WITHIN_PAGE);
    addSubPropertyName("minimum", CP_MINIMUM);
    addSubPropertyName("maximum", CP_MAXIMUM);
    addSubPropertyName("optimum", CP_OPTIMUM);
    addSubPropertyName("precedence", CP_PRECEDENCE);
  
  }
  
  public static void addPropertyName(String name, int id) {
    s_htPropNames.put(name, new Integer(id));
    s_htPropIds.put(new Integer(id), name);
  }

  public static void addSubPropertyName(String name, int id) {
    s_htSubPropNames.put(name, new Integer(id));
    s_htPropIds.put(new Integer(id), name);
  }
}
