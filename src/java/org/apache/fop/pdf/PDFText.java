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
package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * This class represents a simple number object. It also contains contains some 
 * utility methods for outputing numbers to PDF.
 */
public class PDFText extends PDFObject {

    private static final char[] DIGITS = 
                                 {'0', '1', '2', '3', '4', '5', '6', '7',
                                  '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
                                  
    private String text;

    /**
     * Returns the text.
     * @return the text
     */
    public String getText() {
        return this.text;
    }
    
    /**
     * Sets the text.
     * @param text the text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    protected String toPDFString() {
        if (getText() == null) {
            throw new IllegalArgumentException(
                "The text of this PDFText must not be empty");
        }
        StringBuffer sb = new StringBuffer(64);
        sb.append(getObjectID());
        sb.append("(");
        sb.append(escapeText(getText()));
        sb.append(")");
        sb.append("\nendobj\n");
        return sb.toString();
    }

    /**
     * Escape text (see 4.4.1 in PDF 1.3 specs)
     * @param text the text to encode
     * @return encoded text
     */
    public static final String escapeText(final String text) {
        return escapeText(text, true);
    }
    /**
     * Escape text (see 4.4.1 in PDF 1.3 specs)
     * @param text the text to encode
     * @param hexMode true if the output should follow the hex encoding rules
     * @return encoded text
     */
    public static final String escapeText(final String text, boolean hexMode) {
        if (text != null && text.length() > 0) {
            if (hexMode) {
                final byte[] uniBytes;
                try {
                    uniBytes = text.getBytes("UnicodeBig");
                } catch (java.io.UnsupportedEncodingException uee) {
                    throw new CascadingRuntimeException("Incompatible VM", uee);
                }
                return toHex(uniBytes);
            } else {
                final StringBuffer result = new StringBuffer(text.length() * 2);
                result.append("(");
                final int l = text.length();

                // byte order marker (0xfeff)
                result.append("\\376\\377");
                
                for (int i = 0; i < l; i++) {
                    final char ch = text.charAt(i);
                    //if (ch < 128) {
                    //    result.append('\u0000');
                    //    result.append(ch);
                    //} else {
                        final int high = (ch & 0xff00) >>> 8;
                        final int low = ch & 0xff;
                        result.append("\\");
                        result.append(Integer.toOctalString(high));
                        result.append("\\");
                        result.append(Integer.toOctalString(low));
                    //}
                }
                result.append(")");
                return result.toString();
            }
        }
        return "()";
    }

    /**
     * Converts a byte array to a Hexadecimal String (3.2.3 in PDF 1.4 specs)
     * @param data the data to encode
     * @return String the resulting string
     */
    public static final String toHex(byte[] data) {
        final StringBuffer sb = new StringBuffer(data.length * 2);
        sb.append("<");
        for (int i = 0; i < data.length; i++) {
            sb.append(DIGITS[(data[i] >>> 4) & 0x0F]);
            sb.append(DIGITS[data[i] & 0x0F]);
        }
        sb.append(">");
        return sb.toString();
    }
    
    /**
     * Converts a String to UTF-16 (big endian).
     * @param text text to convert
     * @return byte[] UTF-17 stream
     */
    public static final byte[] toUTF16(String text) {
        try {
            return text.getBytes("UnicodeBig");
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new CascadingRuntimeException("Incompatible VM", uee);
        }
    }

    /**
     * Convert a char to a multibyte hex representation
     * @param c character to encode
     * @return the encoded character
     */
    public static final String toUnicodeHex(char c) {
        final StringBuffer buf = new StringBuffer(4);
        final byte[] uniBytes;
        try {
            final char[] a = {c};
            uniBytes = new String(a).getBytes("UnicodeBigUnmarked");
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new CascadingRuntimeException("Incompatible VM", uee);
        }

        for (int i = 0; i < uniBytes.length; i++) {
            buf.append(DIGITS[(uniBytes[i] >>> 4) & 0x0F]);
            buf.append(DIGITS[uniBytes[i] & 0x0F]);
        }
        return buf.toString();
    }
    
    /**
     * Escaped a String as described in section 4.4 in the PDF 1.3 specs.
     * @param s String to escape
     * @return String the escaped String
     */
    public static final String escapeString(final String s) {
        if (s == null || s.length() == 0) {
            return "()";
        } else {
            final StringBuffer sb = new StringBuffer(64);
            sb.append("(");
            for (int i = 0; i < s.length(); i++) {
                final char c = s.charAt(i);
                escapeStringChar(c, sb);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    /**
     * Escapes a character conforming to the rules established in the PostScript
     * Language Reference (Search for "Literal Text Strings").
     * @param c character to escape
     * @param target target StringBuffer to write the escaped character to
     */
    public static final void escapeStringChar(final char c, final StringBuffer target) {
        if (c > 127) {
            target.append("\\");
            target.append(Integer.toOctalString(c));
        } else {
            switch (c) {
                case '\n':
                    target.append("\\n");
                    break;
                case '\r':
                    target.append("\\r");
                    break;
                case '\t':
                    target.append("\\t");
                    break;
                case '\b':
                    target.append("\\b");
                    break;
                case '\f':
                    target.append("\\f");
                    break;
                case '\\':
                    target.append("\\\\");
                    break;
                case '(':
                    target.append("\\(");
                    break;
                case ')':
                    target.append("\\)");
                    break;
                default:
                    target.append(c);
            }
        }
    }

    /**
     * Escape a byte array for output to PDF (Used for encrypted strings)
     * @param data data to encode
     * @return byte[] encoded data
     */
    public static final byte[] escapeByteArray(byte[] data) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(data.length);
        bout.write((int)'(');
        for (int i = 0; i < data.length; i++) {
            final int b = data[i];
            switch (b) {
                case '\n':
                    bout.write('\\');
                    bout.write('n');
                    break;
                case '\r':
                    bout.write('\\');
                    bout.write('r');
                    break;
                case '\t':
                    bout.write('\\');
                    bout.write('t');
                    break;
                case '\b':
                    bout.write('\\');
                    bout.write('b');
                    break;
                case '\f':
                    bout.write('\\');
                    bout.write('f');
                    break;
                case '\\':
                    bout.write('\\');
                    bout.write('\\');
                    break;
                case '(':
                    bout.write('\\');
                    bout.write('(');
                    break;
                case ')':
                    bout.write('\\');
                    bout.write(')');
                    break;
                default:
                    bout.write(b);
            }
        }
        bout.write((int)')');
        return bout.toByteArray();
    }

}

