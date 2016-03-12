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

package org.apache.fop.render.pcl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class hold code for selecting a set of hard-coded fonts available in practically all
 * PCL implementations. We hope this can be improved in the future.
 */
final class HardcodedFonts {

    private HardcodedFonts() {
    }

    /** logging instance */
    private static final Log LOG = LogFactory.getLog(HardcodedFonts.class);

    /**
     * Sets the current font (NOTE: Hard-coded font mappings ATM!)
     * @param name the font name (internal F* names for now)
     * @param size the font size (in millipoints)
     * @param text the text to be rendered (used to determine if there are non-printable chars)
     * @return true if the font can be mapped to PCL
     * @throws IOException if an I/O problem occurs
     */
    public static boolean setFont(PCLGenerator gen, String name, int size, String text)
                throws IOException {
        byte[] encoded = text.getBytes("ISO-8859-1");
        for (int i = 0, c = encoded.length; i < c; i++) {
            if (encoded[i] == 0x3F && text.charAt(i) != '?') {
                return false;
            }
        }
        return selectFont(gen, name, size);
    }

    protected static boolean selectFont(PCLGenerator gen, String name, int size) throws IOException {
        int fontcode = 0;
        if (name.length() > 1 && name.charAt(0) == 'F') {
            try {
                fontcode = Integer.parseInt(name.substring(1));
            } catch (Exception e) {
                LOG.error(e);
            }
        }
        //Note "(ON" selects ISO 8859-1 symbol set as used by PCLGenerator
        String formattedSize = gen.formatDouble2(size / 1000.0);
        switch (fontcode) {
        case 1:     // F1 = Helvetica
            // gen.writeCommand("(8U");
            // gen.writeCommand("(s1p" + formattedSize + "v0s0b24580T");
            // Arial is more common among PCL5 printers than Helvetica - so use Arial

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s0b16602T");
            break;
        case 2:     // F2 = Helvetica Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s0b16602T");
            break;
        case 3:     // F3 = Helvetica Bold

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s3b16602T");
            break;
        case 4:     // F4 = Helvetica Bold Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s3b16602T");
            break;
        case 5:     // F5 = Times Roman
            // gen.writeCommand("(8U");
            // gen.writeCommand("(s1p" + formattedSize + "v0s0b25093T");
            // Times New is more common among PCL5 printers than Times - so use Times New

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s0b16901T");
            break;
        case 6:     // F6 = Times Italic

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s0b16901T");
            break;
        case 7:     // F7 = Times Bold

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v0s3b16901T");
            break;
        case 8:     // F8 = Times Bold Italic

            gen.writeCommand("(0N");
            gen.writeCommand("(s1p" + formattedSize + "v1s3b16901T");
            break;
        case 9:     // F9 = Courier

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f))
                    + "h0s0b4099T");
            break;
        case 10:    // F10 = Courier Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f))
                    + "h1s0b4099T");
            break;
        case 11:    // F11 = Courier Bold

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f))
                    + "h0s3b4099T");
            break;
        case 12:    // F12 = Courier Bold Oblique

            gen.writeCommand("(0N");
            gen.writeCommand("(s0p" + gen.formatDouble2(120.01f / (size / 1000.00f))
                    + "h1s3b4099T");
            break;
        case 13:    // F13 = Symbol

            return false;
            //gen.writeCommand("(19M");
            //gen.writeCommand("(s1p" + formattedSize + "v0s0b16686T");
            // ECMA Latin 1 Symbol Set in Times Roman???
            // gen.writeCommand("(9U");
            // gen.writeCommand("(s1p" + formattedSize + "v0s0b25093T");
            //break;
        case 14:    // F14 = Zapf Dingbats

            return false;
            //gen.writeCommand("(14L");
            //gen.writeCommand("(s1p" + formattedSize + "v0s0b45101T");
            //break;
        default:
            //gen.writeCommand("(0N");
            //gen.writeCommand("(s" + formattedSize + "V");
            return false;
        }
        return true;
    }

}
