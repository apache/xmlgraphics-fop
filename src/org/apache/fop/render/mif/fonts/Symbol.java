

package org.apache.fop.render.mif.fonts;

import org.apache.fop.render.mif.Font;

public class Symbol extends Font {
    private final static String fontName = "Symbol";
    private final static String encoding = "StandardEncoding";
    private final static int capHeight = 1010;
    private final static int xHeight = 520;
    private final static int ascender = 1010;
    private final static int descender = -293;
    private final static int firstChar = 32;
    private final static int lastChar = 255;
    private final static int[] width;

    static {
        width = new int[256];
        width[0x0020] = 250;
        width[0x0021] = 333;
        width[0x22] = 713;
        width[0x0023] = 500;
        width[0x24] = 549;
        width[0x0025] = 833;
        width[0x0026] = 778;
        width[0x27] = 439;
        width[0x0028] = 333;
        width[0x0029] = 333;
        width[0x2A] = 500;
        width[0x002B] = 549;
        width[0x002C] = 250;
        width[0x2D] = 549;
        width[0x002E] = 250;
        width[0x002F] = 278;
        width[0x0030] = 500;
        width[0x0031] = 500;
        width[0x0032] = 500;
        width[0x0033] = 500;
        width[0x0034] = 500;
        width[0x0035] = 500;
        width[0x0036] = 500;
        width[0x0037] = 500;
        width[0x0038] = 500;
        width[0x0039] = 500;
        width[0x003A] = 278;
        width[0x003B] = 278;
        width[0x003C] = 549;
        width[0x003D] = 549;
        width[0x003E] = 549;
        width[0x003F] = 444;
        width[0x40] = 549;
        width[0x41] = 722;
        width[0x42] = 667;
        width[0x43] = 722;
        width[0x44] = 612;
        width[0x45] = 611;
        width[0x46] = 763;
        width[0x47] = 603;
        width[0x48] = 722;
        width[0x49] = 333;
        width[0x4A] = 631;
        width[0x4B] = 722;
        width[0x4C] = 686;
        width[0x4D] = 889;
        width[0x4E] = 722;
        width[0x4F] = 722;
        width[0x50] = 768;
        width[0x51] = 741;
        width[0x52] = 556;
        width[0x53] = 592;
        width[0x54] = 611;
        width[0x55] = 690;
        width[0x56] = 439;
        width[0x57] = 768;
        width[0x58] = 645;
        width[0x59] = 795;
        width[0x5A] = 611;
        width[0x005B] = 333;
        width[0x5C] = 863;
        width[0x005D] = 333;
        width[0x5E] = 658;
        width[0x005F] = 500;
        width[0x60] = 500;
        width[0x61] = 631;
        width[0x62] = 549;
        width[0x63] = 549;
        width[0x64] = 494;
        width[0x65] = 439;
        width[0x66] = 521;
        width[0x67] = 411;
        width[0x68] = 603;
        width[0x69] = 329;
        width[0x6A] = 603;
        width[0x6B] = 549;
        width[0x6C] = 549;
        width[0x006D] = 576;
        width[0x00B5] = 576;
        width[0x6E] = 521;
        width[0x6F] = 549;
        width[0x70] = 549;
        width[0x71] = 521;
        width[0x72] = 549;
        width[0x73] = 603;
        width[0x74] = 439;
        width[0x75] = 576;
        width[0x76] = 713;
        width[0x77] = 686;
        width[0x78] = 493;
        width[0x79] = 686;
        width[0x7A] = 494;
        width[0x007B] = 480;
        width[0x007C] = 200;
        width[0x007D] = 480;
        width[0x7E] = 549;
        width[0xA1] = 620;
        width[0xA2] = 247;
        width[0xA3] = 549;
        width[0xA4] = 167;
        width[0xA5] = 713;
        width[0x0083] = 500;
        width[0xA7] = 753;
        width[0xA8] = 753;
        width[0xA9] = 753;
        width[0xAA] = 753;
        width[0xAB] = 1042;
        width[0xAC] = 987;
        width[0xAD] = 603;
        width[0xAE] = 987;
        width[0xAF] = 603;
        width[0x00B0] = 400;
        width[0x00B1] = 549;
        width[0xB2] = 411;
        width[0xB3] = 549;
        width[0x00D7] = 549;
        width[0xB5] = 713;
        width[0xB6] = 494;
        width[0x0095] = 460;
        width[0x00F7] = 549;
        width[0xB9] = 549;
        width[0xBA] = 549;
        width[0xBB] = 549;
        width[0x0085] = 1000;
        width[0xBD] = 603;
        width[0xBE] = 1000;
        width[0xBF] = 658;
        width[0xC0] = 823;
        width[0xC1] = 686;
        width[0xC2] = 795;
        width[0xC3] = 987;
        width[0xC4] = 768;
        width[0xC5] = 768;
        width[0xC6] = 823;
        width[0xC7] = 768;
        width[0xC8] = 768;
        width[0xC9] = 713;
        width[0xCA] = 713;
        width[0xCB] = 713;
        width[0xCC] = 713;
        width[0xCD] = 713;
        width[0xCE] = 713;
        width[0xCF] = 713;
        width[0xD0] = 768;
        width[0xD1] = 713;
        width[0xD2] = 790;
        width[0xD3] = 790;
        width[0xD4] = 890;
        width[0xD5] = 823;
        width[0xD6] = 549;
        width[0xD7] = 250;
        width[0x00AC] = 713;
        width[0xD9] = 603;
        width[0xDA] = 603;
        width[0xDB] = 1042;
        width[0xDC] = 987;
        width[0xDD] = 603;
        width[0xDE] = 987;
        width[0xDF] = 603;
        width[0xE0] = 494;
        width[0xE1] = 329;
        width[0xE2] = 790;
        width[0xE3] = 790;
        width[0xE4] = 786;
        width[0xE5] = 713;
        width[0xE6] = 384;
        width[0xE7] = 384;
        width[0xE8] = 384;
        width[0xE9] = 384;
        width[0xEA] = 384;
        width[0xEB] = 384;
        width[0xEC] = 494;
        width[0xED] = 494;
        width[0xEE] = 494;
        width[0xEF] = 494;
        width[0xF1] = 329;
        width[0xF2] = 274;
        width[0xF3] = 686;
        width[0xF4] = 686;
        width[0xF5] = 686;
        width[0xF6] = 384;
        width[0xF7] = 384;
        width[0xF8] = 384;
        width[0xF9] = 384;
        width[0xFA] = 384;
        width[0xFB] = 384;
        width[0xFC] = 494;
        width[0xFD] = 494;
        width[0xFE] = 494;

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

    public int width(int i,int size) {
        return size * width[i];
    }

    public int[] getWidths(int size) {
        int[] arr = new int[getLastChar()-getFirstChar()+1];
        System.arraycopy(width, getFirstChar(), arr, 0, getLastChar()-getFirstChar()+1);
        for( int i = 0; i < arr.length; i++) arr[i] *= size;
        return arr;
    }
}

