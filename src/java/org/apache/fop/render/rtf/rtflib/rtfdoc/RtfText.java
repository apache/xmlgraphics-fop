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

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.IOException;
import java.io.Writer;

/**  Model of a text run (a piece of text with attributes) in an RTF document
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public class RtfText extends RtfElement {
        // char code for non-breakable space
        private static final int CHAR_NBSP = 160;
        private static final int CHAR_TAB = 137;
        private static final int CHAR_NEW_LINE = 141;
        /* these next two variables are used to encode bold formating in the
         * raw xml text. Usefull when specific words or phrases are to be bolded
         * but their placement and length change.  Thus the bold formatting becomes
         * part of the data.  The same method can be used for implementing other types
         * of raw text formatting.
         */
        private static final int CHAR_BOLD_START = 130;
        private static final int CHAR_BOLD_END = 131;

    /** members */
    private String text;
    private final RtfAttributes attr;


    /** RtfText attributes: attribute names are RTF control word names to avoid
     *  additional mapping */
    /** constant for bold */
    public static final String ATTR_BOLD = "b";
    /** constant for italic */
    public static final String ATTR_ITALIC = "i";
    /** constant for underline */
    public static final String ATTR_UNDERLINE = "ul";
    /** constant for font size */
    public static final String ATTR_FONT_SIZE = "fs";
    /** constant for font family */
    public static final String ATTR_FONT_FAMILY = "f";
    /** constant for font color */
    public static final String ATTR_FONT_COLOR = "cf";
    /** constant for background color */
    public static final String ATTR_BACKGROUND_COLOR = "chcbpat"; // Added by Boris on 06/25//02

    /** RtfText attributes: alignment attributes */
    /** constant for align center */
    public static final String ALIGN_CENTER = "qc";
    /** constant for align left */
    public static final String ALIGN_LEFT = "ql";
    /** constant for align right */
    public static final String ALIGN_RIGHT = "qr";
    /** constant for align justified */
    public static final String ALIGN_JUSTIFIED = "qj";
    /** constant for align distributed */
    public static final String ALIGN_DISTRIBUTED = "qd";

    /** RtfText attributes: border attributes */
    //added by Chris Scott
    /** constant for bottom single border */
    public static final String BDR_BOTTOM_SINGLE = "brdrb\\brsp40\\brdrs";
    /** constant for bottom double border */
    public static final String BDR_BOTTOM_DOUBLE = "brdrb\\brsp40\\brdrdb";
    /** constant for bottom embossed border */
    public static final String BDR_BOTTOM_EMBOSS = "brdrb\\brsp40\\brdremboss";
    /** constant for bottom dotted border */
    public static final String BDR_BOTTOM_DOTTED = "brdrb\\brsp40\\brdrdot";
    /** constant for bottom dashed border */
    public static final String BDR_BOTTOM_DASH = "brdrb\\brsp40\\brdrdash";

    /** RtfText attributes: fields */
    //must be carefull of group markings and star control
    //ie page field:
    //  "{\field {\*\fldinst {PAGE}} {\fldrslt}}"
    /** constant for field */
    public static final String RTF_FIELD = "field";
    /** constant for field page */
    public static final String RTF_FIELD_PAGE = "fldinst { PAGE }";
    /** constant for field result */
    public static final String RTF_FIELD_RESULT = "fldrslt";

    /**RtfText attributes: indentation attributes */
    //added by Chris Scott
    /** constant for left indent body */
    public static final String LEFT_INDENT_BODY = "li";
    /** constant for left indent first */
    public static final String LEFT_INDENT_FIRST = "fi-";
    /** constant for right indent body */
    public static final String RIGHT_INDENT_BODY = "ri";

    /** constant for center tab */
    public static final String TAB_CENTER = "tqc\\tx";
    /** constant for right tab */
    public static final String TAB_RIGHT = "tqr\\tx";
    /** constant for tab leader dots */
    public static final String TAB_LEADER_DOTS = "tldot";
    /** constant for tab leader hyphens */
    public static final String TAB_LEADER_HYPHEN = "tlhyph";
    /** constant for tab leader underscores */
    public static final String TAB_LEADER_UNDER = "tlul";
    /** constant for tab leader thick */
    public static final String TAB_LEADER_THICK = "tlth";
    /** constant for tab leader equals */
    public static final String TAB_LEADER_EQUALS = "tleq";

    /** Space before/after a paragraph */
    //these lines were added by Boris Pouderous
    public static final String SPACE_BEFORE = "sb";
    /** Space after a paragraph */
    public static final String SPACE_AFTER = "sa";

    /** RtfText attributes: this must contain all allignment attributes names */
    public static final String[] ALIGNMENT = new String []
    {
        ALIGN_CENTER, ALIGN_LEFT, ALIGN_RIGHT, ALIGN_JUSTIFIED, ALIGN_DISTRIBUTED
    };

    /** RtfText attributes:: this must contain all border attribute names*/
    //this line added by Chris Scott, Westinghouse
    public static final String[] BORDER = new String []
    {
        BDR_BOTTOM_SINGLE, BDR_BOTTOM_DOUBLE, BDR_BOTTOM_EMBOSS, BDR_BOTTOM_DOTTED,
        BDR_BOTTOM_DASH
    };

    /** String array of indent constants */
    public static final String[] INDENT = new String []
    {
        LEFT_INDENT_BODY, LEFT_INDENT_FIRST
    };

    /** String array of tab constants */
    public static final String[] TABS = new String []
    {
        TAB_CENTER, TAB_RIGHT, TAB_LEADER_DOTS, TAB_LEADER_HYPHEN, TAB_LEADER_UNDER,
        TAB_LEADER_THICK, TAB_LEADER_EQUALS
    };


    /** RtfText attributes: this must contain all attribute names */
    public static final String [] ATTR_NAMES = {
        ATTR_BOLD,
        ATTR_ITALIC,
        ATTR_UNDERLINE,
        ATTR_FONT_SIZE,
        ATTR_FONT_FAMILY,
        ATTR_FONT_COLOR,
        ATTR_BACKGROUND_COLOR
    };

    /** Create an RtfText in given IRtfTextContainer.
     *  @param str optional initial text content
     */
    RtfText(IRtfTextContainer parent, Writer w, String str, RtfAttributes attr)
           throws IOException {
        super((RtfContainer)parent, w);
        text = str;
        this.attr = attr;
    }

    /**
     * Write our text to the RTF stream
     * @throws IOException for I/O problems
     */
    public void writeRtfContent() throws IOException {
            writeChars: {

                //these lines were added by Boris Pouderous
                  if (attr != null) {
                  writeAttributes(attr, new String[] {RtfText.SPACE_BEFORE});
                  writeAttributes(attr, new String[] {RtfText.SPACE_AFTER});
                }

                if (isTab()) {
                    writeControlWord("tab");
                } else if (isNewLine()) {
                    break writeChars;
                } else if (isBold(true)) {
                    writeControlWord("b");
                } else if (isBold(false)) {
                    writeControlWord("b0");
                // TODO not optimal, consecutive RtfText with same attributes
                // could be written without group marks
                } else {
                    writeGroupMark(true);
                    if (attr != null && mustWriteAttributes()) {
                        writeAttributes(attr, RtfText.ATTR_NAMES);
                    }
                    RtfStringConverter.getInstance().writeRtfString(writer, text);
                    writeGroupMark(false);
                }

          }
    }

        /** true if our text attributes must be written */
        private boolean mustWriteAttributes() {
            return !isEmpty() && !isNbsp();
        }

    /** IRtfTextContainer requirement:
     * @return a copy of our attributes */
    public RtfAttributes getTextContainerAttributes() {
        if (attrib == null) {
            return null;
        }
        return (RtfAttributes)this.attrib.clone();
    }

    /** direct access to our text */
    String getText() {
        return text;
    }

    /** direct access to our text */
    void setText(String str) {
        text = str;
    }

    /**
     * Checks whether the text is empty.
     *
     * @return true    If m_text is null\n
     *         false   m_text is set
     */
    public boolean isEmpty () {
        return text == null || text.trim().length() == 0;
    }

    /**
     *  True if text contains a single non-breaking space (#160).
     *  TODO make this more general and/or merge with isEmpty? -- what happen
     *       with empty paragraphs, if they will be removed, than NO, else ok
     *
     * @return true    If m_text is character 160\n
     *         false   m_text is not a nbsp
     */
    public boolean isNbsp () {
        if (!isEmpty ()) {
            if (text.trim ().length () == 1 && text.charAt (0) == CHAR_NBSP) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the text is a tab character
     */
    public boolean isTab() {
        if (text.trim().length() == 1 && text.charAt(0) == CHAR_TAB) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if text is a newline character
     */
    public boolean isNewLine() {
        if (text.trim().length() == 1 && text.charAt(0) == CHAR_NEW_LINE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param isStart set to true if processing the start of the text (??)
     * @return true if text is bold
     */
    public boolean isBold(boolean isStart) {
        if (isStart) {
            if (text.trim().length() == 1 && text.charAt(0) == CHAR_BOLD_START) {
                return true;
            }
        } else {
            if (text.trim().length() == 1 && text.charAt(0) == CHAR_BOLD_END) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /** @return the attributes of our text */
    public RtfAttributes getTextAttributes() {
        return attr;
    }
}
