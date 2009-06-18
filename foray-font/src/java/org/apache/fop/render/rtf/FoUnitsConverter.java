/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

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

final class FoUnitsConverter {
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
        final String sFONTSUFFIX = "pt";
        if (!size.endsWith(sFONTSUFFIX)) {
            throw new FOPException("Invalid font size '" + size + "', must end with '"
                                   + sFONTSUFFIX + "'");
        }

        float result = 0;
        size = size.substring(0, size.length() - sFONTSUFFIX.length());
        try {
            result = (Float.valueOf(size).floatValue());
        } catch (Exception e) {
            throw new FOPException("Invalid font size value '" + size + "'");
        }

        // RTF font size units are in half-points
        return (int)(result * 2.0);
    }
}
