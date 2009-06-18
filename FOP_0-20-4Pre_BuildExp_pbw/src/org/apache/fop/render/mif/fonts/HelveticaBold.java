/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.mif.fonts;

import org.apache.fop.render.mif.Font;

public class HelveticaBold extends Font {
    private final static String fontName = "Helvetica-Bold";
    private final static String encoding = "WinAnsiEncoding";
    private final static int capHeight = 718;
    private final static int xHeight = 532;
    private final static int ascender = 718;
    private final static int descender = -207;
    private final static int firstChar = 32;
    private final static int lastChar = 255;
    private final static int[] width;

    static {
        width = new int[256];
        width[0x0041] = 722;
        width[0x00C6] = 1000;
        width[0x00C1] = 722;
        width[0x00C2] = 722;
        width[0x00C4] = 722;
        width[0x00C0] = 722;
        width[0x00C5] = 722;
        width[0x00C3] = 722;
        width[0x0042] = 722;
        width[0x0043] = 722;
        width[0x00C7] = 722;
        width[0x0044] = 722;
        width[0x0045] = 667;
        width[0x00C9] = 667;
        width[0x00CA] = 667;
        width[0x00CB] = 667;
        width[0x00C8] = 667;
        width[0x00D0] = 722;
        width[0x0046] = 611;
        width[0x0047] = 778;
        width[0x0048] = 722;
        width[0x0049] = 278;
        width[0x00CD] = 278;
        width[0x00CE] = 278;
        width[0x00CF] = 278;
        width[0x00CC] = 278;
        width[0x004A] = 556;
        width[0x004B] = 722;
        width[0x004C] = 611;
        width[0x004D] = 833;
        width[0x004E] = 722;
        width[0x00D1] = 722;
        width[0x004F] = 778;
        width[0x008C] = 1000;
        width[0x00D3] = 778;
        width[0x00D4] = 778;
        width[0x00D6] = 778;
        width[0x00D2] = 778;
        width[0x00D8] = 778;
        width[0x00D5] = 778;
        width[0x0050] = 667;
        width[0x0051] = 778;
        width[0x0052] = 722;
        width[0x0053] = 667;
        width[0x008A] = 667;
        width[0x0054] = 611;
        width[0x00DE] = 667;
        width[0x0055] = 722;
        width[0x00DA] = 722;
        width[0x00DB] = 722;
        width[0x00DC] = 722;
        width[0x00D9] = 722;
        width[0x0056] = 667;
        width[0x0057] = 944;
        width[0x0058] = 667;
        width[0x0059] = 667;
        width[0x00DD] = 667;
        width[0x009F] = 667;
        width[0x005A] = 611;
        width[0x0061] = 556;
        width[0x00E1] = 556;
        width[0x00E2] = 556;
        width[0x00B4] = 333;
        width[0x00E4] = 556;
        width[0x00E6] = 889;
        width[0x00E0] = 556;
        width[0x0026] = 722;
        width[0x00E5] = 556;
        width[0x005E] = 584;
        width[0x007E] = 584;
        width[0x002A] = 389;
        width[0x0040] = 975;
        width[0x00E3] = 556;
        width[0x0062] = 611;
        width[0x005C] = 278;
        width[0x007C] = 280;
        width[0x007B] = 389;
        width[0x007D] = 389;
        width[0x005B] = 333;
        width[0x005D] = 333;
        width[0x00A6] = 280;
        width[0x0095] = 350;
        width[0x0063] = 556;
        width[0x00E7] = 556;
        width[0x00B8] = 333;
        width[0x00A2] = 556;
        width[0x0088] = 333;
        width[0x003A] = 333;
        width[0x002C] = 278;
        width[0x00A9] = 737;
        width[0x00A4] = 556;
        width[0x0064] = 611;
        width[0x0086] = 556;
        width[0x0087] = 556;
        width[0x00B0] = 400;
        width[0x00A8] = 333;
        width[0x00F7] = 584;
        width[0x0024] = 556;
        width[0x0065] = 556;
        width[0x00E9] = 556;
        width[0x00EA] = 556;
        width[0x00EB] = 556;
        width[0x00E8] = 556;
        width[0x0038] = 556;
        width[0x0085] = 1000;
        width[0x0097] = 1000;
        width[0x0096] = 556;
        width[0x003D] = 584;
        width[0x00F0] = 611;
        width[0x0021] = 333;
        width[0x00A1] = 333;
        width[0x0066] = 333;
        width[0x0035] = 556;
        width[0x0083] = 556;
        width[0x0034] = 556;
        width[0xA4] = 167;
        width[0x0067] = 611;
        width[0x00DF] = 611;
        width[0x0060] = 333;
        width[0x003E] = 584;
        width[0x00AB] = 556;
        width[0x00BB] = 556;
        width[0x008B] = 333;
        width[0x009B] = 333;
        width[0x0068] = 611;
        width[0x002D] = 333;
        width[0x0069] = 278;
        width[0x00ED] = 278;
        width[0x00EE] = 278;
        width[0x00EF] = 278;
        width[0x00EC] = 278;
        width[0x006A] = 278;
        width[0x006B] = 556;
        width[0x006C] = 278;
        width[0x003C] = 584;
        width[0x00AC] = 584;
        width[0x006D] = 889;
        width[0x00AF] = 333;
        width[0x2D] = 584;
        width[0x00B5] = 611;
        width[0x00D7] = 584;
        width[0x006E] = 611;
        width[0x0039] = 556;
        width[0x00F1] = 611;
        width[0x0023] = 556;
        width[0x006F] = 611;
        width[0x00F3] = 611;
        width[0x00F4] = 611;
        width[0x00F6] = 611;
        width[0x009C] = 944;
        width[0x00F2] = 611;
        width[0x0031] = 556;
        width[0x00BD] = 834;
        width[0x00BC] = 834;
        width[0x00B9] = 333;
        width[0x00AA] = 370;
        width[0x00BA] = 365;
        width[0x00F8] = 611;
        width[0x00F5] = 611;
        width[0x0070] = 611;
        width[0x00B6] = 556;
        width[0x0028] = 333;
        width[0x0029] = 333;
        width[0x0025] = 889;
        width[0x002E] = 278;
        width[0x00B7] = 278;
        width[0x0089] = 1000;
        width[0x002B] = 584;
        width[0x00B1] = 584;
        width[0x0071] = 611;
        width[0x003F] = 611;
        width[0x00BF] = 611;
        width[0x0022] = 474;
        width[0x0084] = 500;
        width[0x0093] = 500;
        width[0x0094] = 500;
        width[0x0091] = 278;
        width[0x0092] = 278;
        width[0x0082] = 278;
        width[0x0027] = 238;
        width[0x0072] = 389;
        width[0x00AE] = 737;
        width[0x00B0] = 333;
        width[0x0073] = 556;
        width[0x009A] = 556;
        width[0x00A7] = 556;
        width[0x003B] = 333;
        width[0x0037] = 556;
        width[0x0036] = 556;
        width[0x002F] = 278;
        width[0x0020] = 278;
        width[0x00A0] = 278;
        width[0x00A3] = 556;
        width[0x0074] = 333;
        width[0x00FE] = 611;
        width[0x0033] = 556;
        width[0x00BE] = 834;
        width[0x00B3] = 333;
        width[0x0098] = 333;
        width[0x0099] = 1000;
        width[0x0032] = 556;
        width[0x00B2] = 333;
        width[0x0075] = 611;
        width[0x00FA] = 611;
        width[0x00FB] = 611;
        width[0x00FC] = 611;
        width[0x00F9] = 611;
        width[0x005F] = 556;
        width[0x0076] = 556;
        width[0x0077] = 778;
        width[0x0078] = 556;
        width[0x0079] = 556;
        width[0x00FD] = 556;
        width[0x00FF] = 556;
        width[0x00A5] = 556;
        width[0x007A] = 500;
        width[0x0030] = 556;

    }

    public String encoding() {
        return encoding;
    }

    public String fontName() {
        return fontName;
    }

    public int getAscender(int size) {
        return size * ascender;
    }

    public int getCapHeight(int size) {
        return size * capHeight;
    }

    public int getDescender(int size) {
        return size * descender;
    }

    public int getXHeight(int size) {
        return size * xHeight;
    }

    public int getFirstChar() {
        return firstChar;
    }

    public int getLastChar() {
        return lastChar;
    }

    public int width(int i, int size) {
        return size * width[i];
    }

    public int[] getWidths(int size) {
        int[] arr = new int[getLastChar() - getFirstChar() + 1];
        System.arraycopy(width, getFirstChar(), arr, 0,
                         getLastChar() - getFirstChar() + 1);
        for (int i = 0; i < arr.length; i++)
            arr[i] *= size;
        return arr;
    }

}

