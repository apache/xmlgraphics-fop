

package org.apache.fop.render.mif.fonts;

import org.apache.fop.render.mif.Font;

public class ZapfDingbats extends Font {
    private final static String fontName = "ZapfDingbats";
    private final static String encoding = "StandardEncoding";
    private final static int capHeight = 820;
    private final static int xHeight = 426;
    private final static int ascender = 820;
    private final static int descender = -143;
    private final static int firstChar = 32;
    private final static int lastChar = 255;
    private final static int[] width;

    static {
        width = new int[256];
        width[0x0020] = 278;
        width[0x0021] = 974;
        width[0x0022] = 961;
        width[0x0023] = 974;
        width[0x0024] = 980;
        width[0x0025] = 719;
        width[0x0026] = 789;
        width[0x0027] = 790;
        width[0x0028] = 791;
        width[0x0029] = 690;
        width[0x002A] = 960;
        width[0x002B] = 939;
        width[0x002C] = 549;
        width[0x002D] = 855;
        width[0x002E] = 911;
        width[0x002F] = 933;
        width[0x0030] = 911;
        width[0x0031] = 945;
        width[0x0032] = 974;
        width[0x0033] = 755;
        width[0x0034] = 846;
        width[0x0035] = 762;
        width[0x0036] = 761;
        width[0x0037] = 571;
        width[0x0038] = 677;
        width[0x0039] = 763;
        width[0x003A] = 760;
        width[0x003B] = 759;
        width[0x003C] = 754;
        width[0x003D] = 494;
        width[0x003E] = 552;
        width[0x003F] = 537;
        width[0x0040] = 577;
        width[0x0041] = 692;
        width[0x0042] = 786;
        width[0x0043] = 788;
        width[0x0044] = 788;
        width[0x0045] = 790;
        width[0x0046] = 793;
        width[0x0047] = 794;
        width[0x0048] = 816;
        width[0x0049] = 823;
        width[0x004A] = 789;
        width[0x004B] = 841;
        width[0x004C] = 823;
        width[0x004D] = 833;
        width[0x004E] = 816;
        width[0x004F] = 831;
        width[0x0050] = 923;
        width[0x0051] = 744;
        width[0x0052] = 723;
        width[0x0053] = 749;
        width[0x0054] = 790;
        width[0x0055] = 792;
        width[0x0056] = 695;
        width[0x0057] = 776;
        width[0x0058] = 768;
        width[0x0059] = 792;
        width[0x005A] = 759;
        width[0x005B] = 707;
        width[0x005C] = 708;
        width[0x005D] = 682;
        width[0x005E] = 701;
        width[0x005F] = 826;
        width[0x0060] = 815;
        width[0x0061] = 789;
        width[0x0062] = 789;
        width[0x0063] = 707;
        width[0x0064] = 687;
        width[0x0065] = 696;
        width[0x0066] = 689;
        width[0x0067] = 786;
        width[0x0068] = 787;
        width[0x0069] = 713;
        width[0x006A] = 791;
        width[0x006B] = 785;
        width[0x006C] = 791;
        width[0x006D] = 873;
        width[0x006E] = 761;
        width[0x006F] = 762;
        width[0x0070] = 762;
        width[0x0071] = 759;
        width[0x0072] = 759;
        width[0x0073] = 892;
        width[0x0074] = 892;
        width[0x0075] = 788;
        width[0x0076] = 784;
        width[0x0077] = 438;
        width[0x0078] = 138;
        width[0x0079] = 277;
        width[0x007A] = 415;
        width[0x007B] = 392;
        width[0x007C] = 392;
        width[0x007D] = 668;
        width[0x007E] = 668;
        width[0x00A1] = 732;
        width[0x00A2] = 544;
        width[0x00A3] = 544;
        width[0x00A4] = 910;
        width[0x00A5] = 667;
        width[0x00A6] = 760;
        width[0x00A7] = 760;
        width[0x00A8] = 776;
        width[0x00A9] = 595;
        width[0x00AA] = 694;
        width[0x00AB] = 626;
        width[0x00AC] = 788;
        width[0x00AD] = 788;
        width[0x00AE] = 788;
        width[0x00AF] = 788;
        width[0x00B0] = 788;
        width[0x00B1] = 788;
        width[0x00B2] = 788;
        width[0x00B3] = 788;
        width[0x00B4] = 788;
        width[0x00B5] = 788;
        width[0x00B6] = 788;
        width[0x00B7] = 788;
        width[0x00B8] = 788;
        width[0x00B9] = 788;
        width[0x00BA] = 788;
        width[0x00BB] = 788;
        width[0x00BC] = 788;
        width[0x00BD] = 788;
        width[0x00BE] = 788;
        width[0x00BF] = 788;
        width[0x00C0] = 788;
        width[0x00C1] = 788;
        width[0x00C2] = 788;
        width[0x00C3] = 788;
        width[0x00C4] = 788;
        width[0x00C5] = 788;
        width[0x00C6] = 788;
        width[0x00C7] = 788;
        width[0x00C8] = 788;
        width[0x00C9] = 788;
        width[0x00CA] = 788;
        width[0x00CB] = 788;
        width[0x00CC] = 788;
        width[0x00CD] = 788;
        width[0x00CE] = 788;
        width[0x00CF] = 788;
        width[0x00D0] = 788;
        width[0x00D1] = 788;
        width[0x00D2] = 788;
        width[0x00D3] = 788;
        width[0x00D4] = 894;
        width[0x00D5] = 838;
        width[0x00D6] = 1016;
        width[0x00D7] = 458;
        width[0x00D8] = 748;
        width[0x00D9] = 924;
        width[0x00DA] = 748;
        width[0x00DB] = 918;
        width[0x00DC] = 927;
        width[0x00DD] = 928;
        width[0x00DE] = 928;
        width[0x00DF] = 834;
        width[0x00E0] = 873;
        width[0x00E1] = 828;
        width[0x00E2] = 924;
        width[0x00E3] = 924;
        width[0x00E4] = 917;
        width[0x00E5] = 930;
        width[0x00E6] = 931;
        width[0x00E7] = 463;
        width[0x00E8] = 883;
        width[0x00E9] = 836;
        width[0x00EA] = 836;
        width[0x00EB] = 867;
        width[0x00EC] = 867;
        width[0x00ED] = 696;
        width[0x00EE] = 696;
        width[0x00EF] = 874;
        width[0x00F1] = 874;
        width[0x00F2] = 760;
        width[0x00F3] = 946;
        width[0x00F4] = 771;
        width[0x00F5] = 865;
        width[0x00F6] = 771;
        width[0x00F7] = 888;
        width[0x00F8] = 967;
        width[0x00F9] = 888;
        width[0x00FA] = 831;
        width[0x00FB] = 873;
        width[0x00FC] = 927;
        width[0x00FD] = 970;
        width[0x00FE] = 918;
        width[0x0089] = 410;
        width[0x0087] = 509;
        width[0x008C] = 334;
        width[0x0086] = 509;
        width[0x0080] = 390;
        width[0x008A] = 234;
        width[0x0084] = 276;
        width[0x0081] = 390;
        width[0x0088] = 410;
        width[0x0083] = 317;
        width[0x0082] = 317;
        width[0x0085] = 276;
        width[0x008D] = 334;
        width[0x008B] = 234;

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

