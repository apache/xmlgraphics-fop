/*
 * $Id: MutableFont.java,v 1.2 2003/03/06 17:43:05 jeremias Exp $
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
package org.apache.fop.fonts;

import java.util.Map;


/**
 * This interface is used to set the values of a font during configuration time.
 */
public interface MutableFont {

    /**
     * Sets the font name.
     * @param name font name
     */
    void setFontName(String name);
    
    /**
     * Sets the path to the embeddable font file.
     * @param path URI to the file
     */
    void setEmbedFileName(String path);

    /**
     * Sets the resource name of the embeddable font file.
     * @param name resource name
     */
    void setEmbedResourceName(String name);

    /**
     * Sets the capital height value.
     * @param capHeight capital height
     */
    void setCapHeight(int capHeight);
    
    /**
     * Sets the ascent value.
     * @param ascender ascent height
     */
    void setAscender(int ascender);
    
    /**
     * Sets the descent value.
     * @param descender descent value
     */
    void setDescender(int descender);

    /**
     * Sets the font's bounding box
     * @param bbox bounding box
     */
    void setFontBBox(int[] bbox);

    /**
     * Sets the font's flags
     * @param flags flags
     */
    void setFlags(int flags);
    
    /**
     * Sets the font's StemV value.
     * @param stemV StemV
     */
    void setStemV(int stemV);
    
    /**
     * Sets the font's italic angle.
     * @param italicAngle italic angle
     */
    void setItalicAngle(int italicAngle);
    
    /**
     * Sets the font's default width
     * @param width default width
     */
    void setMissingWidth(int width);
 
    /**
     * Sets the font type.
     * @param fontType font type
     */
    void setFontType(FontType fontType);
 
    /**
     * Sets the index of the first character in the character table.
     * @param index index of first character
     */
    void setFirstChar(int index);
    
    /**
     * Sets the index of the last character in the character table.
     * @param index index of the last character
     */
    void setLastChar(int index);
    
    /**
     * Enables/disabled kerning.
     * @param enabled True if kerning should be enabled if available
     */
    void setKerningEnabled(boolean enabled);
    
    /**
     * Adds an entry to the kerning table.
     * @param key Kerning key
     * @param value Kerning value
     */
    void putKerningEntry(Integer key, Map value);
       
}
