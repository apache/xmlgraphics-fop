

package org.apache.fop.render.mif.fonts;

import org.apache.fop.render.mif.Font;

public class TimesItalic extends Font {
    private final static String fontName = "Times-Italic";
    private final static String encoding = "WinAnsiEncoding";
    private final static int capHeight = 653;
    private final static int xHeight = 441;
    private final static int ascender = 683;
    private final static int descender = -205;
    private final static int firstChar = 32;
    private final static int lastChar = 255;
    private final static int[] width;

    static {
        width = new int[256];
        width[0x0041] = 611;
        width[0x00C6] = 889;
        width[0x00C1] = 611;
        width[0x00C2] = 611;
        width[0x00C4] = 611;
        width[0x00C0] = 611;
        width[0x00C5] = 611;
        width[0x00C3] = 611;
        width[0x0042] = 611;
        width[0x0043] = 667;
        width[0x00C7] = 667;
        width[0x0044] = 722;
        width[0x0045] = 611;
        width[0x00C9] = 611;
        width[0x00CA] = 611;
        width[0x00CB] = 611;
        width[0x00C8] = 611;
        width[0x00D0] = 722;
        width[0x0046] = 611;
        width[0x0047] = 722;
        width[0x0048] = 722;
        width[0x0049] = 333;
        width[0x00CD] = 333;
        width[0x00CE] = 333;
        width[0x00CF] = 333;
        width[0x00CC] = 333;
        width[0x004A] = 444;
        width[0x004B] = 667;
        width[0x004C] = 556;
        width[0x004D] = 833;
        width[0x004E] = 667;
        width[0x00D1] = 667;
        width[0x004F] = 722;
        width[0x008C] = 944;
        width[0x00D3] = 722;
        width[0x00D4] = 722;
        width[0x00D6] = 722;
        width[0x00D2] = 722;
        width[0x00D8] = 722;
        width[0x00D5] = 722;
        width[0x0050] = 611;
        width[0x0051] = 722;
        width[0x0052] = 611;
        width[0x0053] = 500;
        width[0x008A] = 500;
        width[0x0054] = 556;
        width[0x00DE] = 611;
        width[0x0055] = 722;
        width[0x00DA] = 722;
        width[0x00DB] = 722;
        width[0x00DC] = 722;
        width[0x00D9] = 722;
        width[0x0056] = 611;
        width[0x0057] = 833;
        width[0x0058] = 611;
        width[0x0059] = 556;
        width[0x00DD] = 556;
        width[0x009F] = 556;
        width[0x005A] = 556;
        width[0x0061] = 500;
        width[0x00E1] = 500;
        width[0x00E2] = 500;
        width[0x00B4] = 333;
        width[0x00E4] = 500;
        width[0x00E6] = 667;
        width[0x00E0] = 500;
        width[0x0026] = 778;
        width[0x00E5] = 500;
        width[0x005E] = 422;
        width[0x007E] = 541;
        width[0x002A] = 500;
        width[0x0040] = 920;
        width[0x00E3] = 500;
        width[0x0062] = 500;
        width[0x005C] = 278;
        width[0x007C] = 275;
        width[0x007B] = 400;
        width[0x007D] = 400;
        width[0x005B] = 389;
        width[0x005D] = 389;
        width[0x00A6] = 275;
        width[0x0095] = 350;
        width[0x0063] = 444;
        width[0x00E7] = 444;
        width[0x00B8] = 333;
        width[0x00A2] = 500;
        width[0x0088] = 333;
        width[0x003A] = 333;
        width[0x002C] = 250;
        width[0x00A9] = 760;
        width[0x00A4] = 500;
        width[0x0064] = 500;
        width[0x0086] = 500;
        width[0x0087] = 500;
        width[0x00B0] = 400;
        width[0x00A8] = 333;
        width[0x00F7] = 675;
        width[0x0024] = 500;
        width[0x0065] = 444;
        width[0x00E9] = 444;
        width[0x00EA] = 444;
        width[0x00EB] = 444;
        width[0x00E8] = 444;
        width[0x0038] = 500;
        width[0x0085] = 889;
        width[0x0097] = 889;
        width[0x0096] = 500;
        width[0x003D] = 675;
        width[0x00F0] = 500;
        width[0x0021] = 333;
        width[0x00A1] = 389;
        width[0x0066] = 278;
        width[0x0035] = 500;
        width[0x0083] = 500;
        width[0x0034] = 500;
        width[0xA4] = 167;
        width[0x0067] = 500;
        width[0x00DF] = 500;
        width[0x0060] = 333;
        width[0x003E] = 675;
        width[0x00AB] = 500;
        width[0x00BB] = 500;
        width[0x008B] = 333;
        width[0x009B] = 333;
        width[0x0068] = 500;
        width[0x002D] = 333;
        width[0x0069] = 278;
        width[0x00ED] = 278;
        width[0x00EE] = 278;
        width[0x00EF] = 278;
        width[0x00EC] = 278;
        width[0x006A] = 278;
        width[0x006B] = 444;
        width[0x006C] = 278;
        width[0x003C] = 675;
        width[0x00AC] = 675;
        width[0x006D] = 722;
        width[0x00AF] = 333;
        width[0x2D] = 675;
        width[0x00B5] = 500;
        width[0x00D7] = 675;
        width[0x006E] = 500;
        width[0x0039] = 500;
        width[0x00F1] = 500;
        width[0x0023] = 500;
        width[0x006F] = 500;
        width[0x00F3] = 500;
        width[0x00F4] = 500;
        width[0x00F6] = 500;
        width[0x009C] = 667;
        width[0x00F2] = 500;
        width[0x0031] = 500;
        width[0x00BD] = 750;
        width[0x00BC] = 750;
        width[0x00B9] = 300;
        width[0x00AA] = 276;
        width[0x00BA] = 310;
        width[0x00F8] = 500;
        width[0x00F5] = 500;
        width[0x0070] = 500;
        width[0x00B6] = 523;
        width[0x0028] = 333;
        width[0x0029] = 333;
        width[0x0025] = 833;
        width[0x002E] = 250;
        width[0x00B7] = 250;
        width[0x0089] = 1000;
        width[0x002B] = 675;
        width[0x00B1] = 675;
        width[0x0071] = 500;
        width[0x003F] = 500;
        width[0x00BF] = 500;
        width[0x0022] = 420;
        width[0x0084] = 556;
        width[0x0093] = 556;
        width[0x0094] = 556;
        width[0x0091] = 333;
        width[0x0092] = 333;
        width[0x0082] = 333;
        width[0x0027] = 214;
        width[0x0072] = 389;
        width[0x00AE] = 760;
        width[0x00B0] = 333;
        width[0x0073] = 389;
        width[0x009A] = 389;
        width[0x00A7] = 500;
        width[0x003B] = 333;
        width[0x0037] = 500;
        width[0x0036] = 500;
        width[0x002F] = 278;
        width[0x0020] = 250;
        width[0x00A0] = 250;
        width[0x00A3] = 500;
        width[0x0074] = 278;
        width[0x00FE] = 500;
        width[0x0033] = 500;
        width[0x00BE] = 750;
        width[0x00B3] = 300;
        width[0x0098] = 333;
        width[0x0099] = 980;
        width[0x0032] = 500;
        width[0x00B2] = 300;
        width[0x0075] = 500;
        width[0x00FA] = 500;
        width[0x00FB] = 500;
        width[0x00FC] = 500;
        width[0x00F9] = 500;
        width[0x005F] = 500;
        width[0x0076] = 444;
        width[0x0077] = 667;
        width[0x0078] = 444;
        width[0x0079] = 444;
        width[0x00FD] = 444;
        width[0x00FF] = 444;
        width[0x00A5] = 500;
        width[0x007A] = 389;
        width[0x0030] = 500;

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

