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
package org.apache.fop.render.rtf;

import java.util.Map;
import java.util.HashMap;

//FOP
import org.apache.fop.apps.FOPException;


/**  Converts XSL-FO units to RTF units
 *
 *  @author Bertrand Delacretaz <bdelacretaz@codeconsult.ch>
 *  @author putzi
 *  @author Peter Herweg <pherweg@web.de>
 *
 *  This class was originally developed by Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  for the JFOR project and is now integrated into FOP.
 */

class FoUnitsConverter {
    private static final FoUnitsConverter INSTANCE = new FoUnitsConverter();

    /** points to twips: 1 twip is 1/20 of a point */
    public static final float POINT_TO_TWIPS = 20f;

    /** millimeters and centimeters to twips: , one point is 1/72 of an inch, one inch is 25.4 mm */
    public static final float IN_TO_TWIPS = 72f * POINT_TO_TWIPS;
    public static final float MM_TO_TWIPS = IN_TO_TWIPS / 25.4f;
    public static final float CM_TO_TWIPS = 10 * MM_TO_TWIPS;


    /** conversion factors keyed by xsl:fo units names */
    private static final Map TWIP_FACTORS = new HashMap();
    static {
        TWIP_FACTORS.put("mm", new Float(MM_TO_TWIPS));
        TWIP_FACTORS.put("cm", new Float(CM_TO_TWIPS));
        TWIP_FACTORS.put("pt", new Float(POINT_TO_TWIPS));
        TWIP_FACTORS.put("in", new Float(IN_TO_TWIPS));
    }

    /** singleton pattern */
    private FoUnitsConverter() {
    }

    /** singleton pattern */
    static FoUnitsConverter getInstance() {
        return INSTANCE;
    }

    /** convert given value to RTF units
     *  @param foValue a value like "12mm"
     *  TODO: tested with "mm" units only, needs work to comply with FO spec
     *  Why does it search for period instead of simply breaking last two
     *  Characters into another units string? - Chris
     */
    float convertToTwips(String foValue)
            throws FOPException {
        foValue = foValue.trim();

        // break value into number and units
        final StringBuffer number = new StringBuffer();
        final StringBuffer units = new StringBuffer();

        for (int i = 0; i < foValue.length(); i++) {
            final char c = foValue.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                number.append(c);
            } else {
                // found the end of the digits
                units.append(foValue.substring(i).trim());
                break;
            }
        }

        return numberToTwips(number.toString(), units.toString());
    }


    /** convert given value to twips according to given units */
    private float numberToTwips(String number, String units)
            throws FOPException {
        float result = 0;

        // convert number to integer
        try {
            if (number != null && number.trim().length() > 0) {
                result = Float.valueOf(number).floatValue();
            }
        } catch (Exception e) {
            throw new FOPException("number format error: cannot convert '"
                                   + number + "' to float value");
        }

        // find conversion factor
        if (units != null && units.trim().length() > 0) {
            final Float factor = (Float)TWIP_FACTORS.get(units.toLowerCase());
            if (factor == null) {
                throw new FOPException("conversion factor not found for '" + units + "' units");
            }
            result *= factor.floatValue();
        }

        return result;
    }

    /** convert a font size given in points like "12pt" */
    int convertFontSize(String size) throws FOPException {
        size = size.trim();
        final String FONT_SUFFIX = "pt";
        if (!size.endsWith(FONT_SUFFIX)) {
            throw new FOPException("Invalid font size '" + size + "', must end with '"
                                   + FONT_SUFFIX + "'");
        }

        float result = 0;
        size = size.substring(0, size.length() - FONT_SUFFIX.length());
        try {
            result = (Float.valueOf(size).floatValue());
        } catch (Exception e) {
            throw new FOPException("Invalid font size value '" + size + "'");
        }

        // RTF font size units are in half-points
        return (int)(result * 2.0);
    }
}
