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
package org.apache.fop.fonts;

import java.util.Map;


/**
 * Main interface for access to font metrics.
 */
public interface FontMetrics {

    /**
     * Returns the font name.
     * @return the font name
     */
    String getFontName();
    
    
    /**
     * Returns the type of the font.
     * @return the font type
     */
    FontType getFontType();
    

    /**
     * Returns the ascent of the font described by this
     * FontMetrics object.
     * @param size font size
     * @return ascent in milliponts
     */
    int getAscender(int size);
    
    /**
     * Returns the size of a capital letter measured from the font's baseline.
     * @param size font size
     * @return height of capital characters
     */
    int getCapHeight(int size);
    
    
    /**
     * Returns the descent of the font described by this
     * FontMetrics object.
     * @param size font size
     * @return descent in milliponts
     */
    int getDescender(int size);
    
    
    /**
     * Determines the typical font height of this
     * FontMetrics object
     * @param size font size
     * @return font height in millipoints
     */
    int getXHeight(int size);

    /**
     * Return the width (in 1/1000ths of point size) of the character at
     * code point i.
     * @param i code point index
     * @param size font size
     * @return the width of the character
     */
    int getWidth(int i, int size);

    /**
     * Return the array of widths.
     * <p>
     * This is used to get an array for inserting in an output format.
     * It should not be used for lookup.
     * @return an array of widths
     */
    int[] getWidths();
    
    /**
     * Indicates if the font has kering information.
     * @return True, if kerning is available.
     */
    boolean hasKerningInfo();
        
    /**
     * Returns the kerning map for the font.
     * @return the kerning map
     */
    Map getKerningInfo();
    
}
