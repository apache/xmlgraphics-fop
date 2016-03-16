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

package org.apache.fop.render.pcl.fonts;

/**
 * Table C-1 from http://www.lprng.com/DISTRIB/RESOURCES/DOCS/pcl5comp.pdf
 */
public enum PCLSymbolSet {
    // Unbound font containing > 256 characters
    Unbound("1X", 56),

    // Other symbol sets to use in bound fonts
    Bound_Generic("0Q", 17),
    GW_3212("18C", 597),
    ISO_60_Danish_Norwegian("0D", 4),
    Devanagari("2D", 68),
    ISO_4_United_Kingdom("1E", 37),
    Windows_3_1_Latin2("9E", 293),
    ISO_69_French("1F", 38),
    ISO_21_German("1G", 39),
    Greek_8("8G", 283),
    Windows_3_1_Latin_Greek("9G", 295),
    PC_851_Latin_Greek("10G", 327),
    PC_8_Latin_Greek("12G", 391),
    Hebrew_7("0H", 8),
    ISO_8859_8_Latin_Hebrew("7H", 232),
    Hebrew_8("8H", 264),
    PC_862_Latin_Hebrew("15H", 488),
    ISO_15_Italian("0I", 9),
    Microsoft_Publishing("6J", 202),
    DeskTop("7J", 234),
    Document("8J", 266),
    PC_1004("9J", 298),
    PS_Text("10J", 330),
    PS_ISO_Latin1("11J", 362),
    MC_Text("12J", 394),
    Ventura_International3("13J", 426),
    Ventura_US3("14J", 458),
    Swash_Characters("16J", 522),
    Small_Caps_Old_Style_Figures("17J", 554),
    Old_Style_Figures("18J", 586),
    Fractions("19J", 618),
    Lining_Figures("21J", 682),
    Small_Caps_and_Lining_Figures("22J", 714),
    Alternate_Caps("23J", 746),
    Kana_8_JIS_210("8K", 267),
    Korean_8("9K", 299),

    Line_Draw_7("0L", 12),
    HP_Block_Characters("1L", 44),
    Tax_Line_Draw("2L", 76),
    Line_Draw_8("8L", 268),
    Ventura_ITC_Zapf_Dingbats3("9L", 300),
    PS_ITC_Zapf_Dingbats("10L", 332),
    ITC_Zapf_Dingbats_Series_100("11L", 364),
    ITC_Zapf_Dingbats_Series_200("12L", 396),
    ITC_Zapf_Dingbats_Series_300("13L", 428),
    Windows_Baltic("19L", 620),
    Carta("20L", 652),
    Ornaments("21L", 684),
    Universal_News_Commercial_Pi("22L", 716),
    Chess("23L", 748),
    Astrology_1("24L", 780),
    Pi_Set_1("31L", 1004),
    Pi_Set_2("32L", 1036),
    Pi_Set_3("33L", 1068),
    Pi_Set_4("34L", 1100),
    Pi_Set_5("35L", 1132),
    Pi_Set_6("36L", 1164),
    Wingdings("579L", 18540),
    Math_7("0M", 13),
    Tech_7("1M", 45),
    PS_Math("5M", 173),
    Ventura_Math3("6M", 205),
    Math_8("8M", 269),
    Universal_Greek_Math_Pi("10M", 333),
    TeX_Math_Extension("11M", 365),
    TeX_Math_Symbol("12M", 397),
    TeX_Math_Italic("13M", 429),
    Symbol("19M", 621),
    ISO_8859_1_Latin_1("0N", 14),
    ISO_8859_2_Latin_2("2N", 78),

    ISO_8859_3_Latin_3("3N", 110),
    ISO_8859_4_Latin_4("4N", 142),
    ISO_8859_9_Latin_5("5N", 174),
    ISO_8859_10_Latin_6("6N", 206),
    ISO_8859_5_Latin_Cyrillic("10N", 334),
    ISO_8859_6_Latin_Arabic("11N", 366),
    ISO_8859_7_Latin_Greek("12N", 398),
    OCR_A("0O", 15),
    OCR_B("1O", 47),
    OCR_M("2O", 79),
    MICR_E13B("10O", 335),
    Typewriter_Paired_APL("0P", 16),
    Bit_Paired_APL("1P", 48),
    Expert("10P", 336),
    Alternate("11P", 368),
    Fraktur("12P", 400),
    Cyrillic_ASCII_8859_5_1986("0R", 18),
    Cyrillic("1R", 50),
    PC_Cyrillic("3R", 114),
    Windows_3_1_Latin_Cyrillic("9R", 306),
    ISO_11_Swedish("0S", 19),
    ISO_17_Spanish3("2S", 83),
    HP_European_Spanish("7S", 243),
    HP_Latin_Spanish("8S", 275),
    HP_GL_Download("16S", 531),
    HP_GL_Drafting("17S", 563),
    HP_GL_Special_Symbols("18S", 595),
    Sonata("20S", 659),
    Thai_8("0T", 20),
    TISI_620_2533_Thai("1T", 52),
    Windows_3_1_Latin_5("5T", 180),
    Turkish_8("8T", 276),

    PC_8_Turkish("9T", 308),
    Teletex("10T", 340),
    ISO_6_ASCII("0U", 21),
    Legal("1U", 53),
    HPL("5U", 181),
    OEM_1("7U", 245),
    Roman_8("8U", 277),
    Windows_3_0_Latin_1("9U", 309),
    PC_8_Code_Page_437("10U", 341),
    PC_8_D_N_Danish_Norwegian("11U", 373),
    PC_850_Multilingual("12U", 405),
    Pi_Font("15U", 501),
    PC_857("16U", 533),
    PC_852_Latin_2("17U", 565),
    Windows_3_1_Latin_1("19U", 629),
    PC_860_Portugal("20U", 661),
    PC_861_Iceland("21U", 693),
    PC_863_Canada_French("23U", 757),
    PC_865_Norway("25U", 821),
    PC_775("26U", 853),
    Arabic_8("8V", 278),
    Windows_3_1_Latin_Arabic("9V", 310),
    Code_Page_864_Latin_Arabic("10V", 342),
    Barcode_3of9("0Y", 25),
    Industrial_2_of_5_Barcode("1Y", 57),
    Matrix_2_of_5_Barcode("2Y", 89),
    Interleaved_2_of_5_Barcode("4Y", 153),
    CODABAR_Barcode("5Y", 185),
    MSI_Plessey_Barcode("6Y", 217),
    Code_11_Barcode("7Y", 249),
    UPC_EAN_Barcode("8Y", 281),
    MICR_CMC_7("14Y", 473),
    USPS_ZIP("5Y", 505),

    Math_7_2("0A", 1),
    Line_Draw_7_2("0B", 2),
    HP_Large_Characters("0C", 3),
    ISO_61_Norwegian_Version_2("1D", 36),
    Roman_Extension("0E", 5),
    ISO_25_French("0F", 6),
    HP_German("0G", 7),
    ISO_14_JIS_ASCII("0K", 11),
    ISO_13_Katakana("1K", 43),
    ISO_57_Chinese("2K", 75),
    HP_Spanish("1S", 51),
    ISO_10_Swedish("3S", 115),
    ISO_16_Portuguese("4S", 147),
    ISO_84_Portuguese("5S", 179),
    ISO_85_Spanish("6S", 211),
    ISO_2_International_Reference("2U", 85),
    Arabic("0V", 22);

    private String symbolSetID;
    private int kind1;

    PCLSymbolSet(String symbolSetID, int kind1) {
        this.kind1 = kind1;
    }

    public String getSymbolSetID() {
        return symbolSetID;
    }

    public int getKind1() {
        return kind1;
    }
}
