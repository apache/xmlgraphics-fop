/*
 * $Id: FontType.java,v 1.2 2003/03/06 17:43:05 jeremias Exp $
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

import org.apache.avalon.framework.ValuedEnum;

/**
 * This class enumerates all supported font types.
 */
public class FontType extends ValuedEnum {

    /**
     * Collective identifier for "other" font types
     */
    public static final FontType OTHER       = new FontType("Other", 0);
    /**
     * Adobe Type 0 fonts
     */
    public static final FontType TYPE0       = new FontType("Type0", 1);
    /**
     * Adobe Type 1 fonts
     */
    public static final FontType TYPE1       = new FontType("Type1", 2);
    /**
     * Adobe Multiple Master Type 1 fonts
     */
    public static final FontType MMTYPE1     = new FontType("MMType1", 3);
    /**
     * Adobe Type 3 fonts ("user-defined" fonts)
     */
    public static final FontType TYPE3       = new FontType("Type3", 4);
    /**
     * TrueType fonts
     */
    public static final FontType TRUETYPE    = new FontType("TrueType", 5);


    /**
     * @see org.apache.avalon.framework.Enum#Enum(String)
     */
    protected FontType(String name, int value) {
        super(name, value);
    }


    /**
     * Returns the FontType by name.
     * @param name Name of the font type to look up
     * @return the font type
     */
    public static FontType byName(String name) {
        if (name.equalsIgnoreCase(FontType.OTHER.getName())) {
            return FontType.OTHER;
        } else if (name.equalsIgnoreCase(FontType.TYPE0.getName())) {
            return FontType.TYPE0;
        } else if (name.equalsIgnoreCase(FontType.TYPE1.getName())) {
            return FontType.TYPE1;
        } else if (name.equalsIgnoreCase(FontType.MMTYPE1.getName())) {
            return FontType.MMTYPE1;
        } else if (name.equalsIgnoreCase(FontType.TYPE3.getName())) {
            return FontType.TYPE3;
        } else if (name.equalsIgnoreCase(FontType.TRUETYPE.getName())) {
            return FontType.TRUETYPE;
        } else {
            throw new IllegalArgumentException("Invalid font type: " + name);
        }
    }
    
    
    /**
     * Returns the FontType by value.
     * @param value Value of the font type to look up
     * @return the font type
     */
    public static FontType byValue(int value) {
        if (value == FontType.OTHER.getValue()) {
            return FontType.OTHER;
        } else if (value == FontType.TYPE0.getValue()) {
            return FontType.TYPE0;
        } else if (value == FontType.TYPE1.getValue()) {
            return FontType.TYPE1;
        } else if (value == FontType.MMTYPE1.getValue()) {
            return FontType.MMTYPE1;
        } else if (value == FontType.TYPE3.getValue()) {
            return FontType.TYPE3;
        } else if (value == FontType.TRUETYPE.getValue()) {
            return FontType.TRUETYPE;
        } else {
            throw new IllegalArgumentException("Invalid font type: " + value);
        }
    }
    
}
