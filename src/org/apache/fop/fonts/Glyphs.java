/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts;

public class Glyphs {
    static String notdef = ".notdef";

    static String mac_glyph_names[] = {
        /* 0x00 */
        notdef, ".null", "CR", "space", "exclam", "quotedbl", "numbersign",
                "dollar", "percent", "ampersand", "quotesingle", "parenleft",
                "parenright", "asterisk", "plus", "comma", /* 0x10 */
        "hyphen", "period", "slash", "zero", "one", "two", "three", "four",
                  "five", "six", "seven", "eight", "nine", "colon",
                  "semicolon", "less", /* 0x20 */
        "equal", "greater", "question", "at", "A", "B", "C", "D", "E", "F",
                 "G", "H", "I", "J", "K", "L", /* 0x30 */
        "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
             "bracketleft", "backslash", /* 0x40 */
        "bracketright", "asciicircum", "underscore", "grave", "a", "b", "c",
                        "d", "e", "f", "g", "h", "i", "j", "k", "l",
                        /* 0x50 */
        "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
             "braceleft", "bar", /* 0x60 */
        "braceright", "asciitilde", "Adieresis", "Aring", "Ccedilla",
                      "Eacute", "Ntilde", "Odieresis", "Udieresis", "aacute",
                      "agrave", "acircumflex", "adieresis", "atilde",
                      "aring", "ccedilla", /* 0x70 */
        "eacute", "egrave", "ecircumflex", "edieresis", "iacute", "igrave",
                  "icircumflex", "idieresis", "ntilde", "oacute", "ograve",
                  "ocircumflex", "odieresis", "otilde", "uacute", "ugrave",
                  /* 0x80 */
        "ucircumflex", "udieresis", "dagger", "degree", "cent", "sterling",
                       "section", "bullet", "paragraph", "germandbls",
                       "registered", "copyright", "trademark", "acute",
                       "dieresis", "notequal", /* 0x90 */
        "AE", "Oslash", "infinity", "plusminus", "lessequal", "greaterequal",
              "yen", "mu", "partialdiff", "Sigma", "Pi", "pi", "integral",
              "ordfeminine", "ordmasculine", "Omega", /* 0xa0 */
        "ae", "oslash", "questiondown", "exclamdown", "logicalnot",
              "radical", "florin", "approxequal", "Delta", "guillemotleft",
              "guillemotright", "ellipsis", "nbspace", "Agrave", "Atilde",
              "Otilde", /* 0xb0 */
        "OE", "oe", "endash", "emdash", "quotedblleft", "quotedblright",
              "quoteleft", "quoteright", "divide", "lozenge", "ydieresis",
              "Ydieresis", "fraction", "currency", "guilsinglleft",
              "guilsinglright", /* 0xc0 */
        "fi", "fl", "daggerdbl", "periodcentered", "quotesinglbase",
              "quotedblbase", "perthousand", "Acircumflex", "Ecircumflex",
              "Aacute", "Edieresis", "Egrave", "Iacute", "Icircumflex",
              "Idieresis", "Igrave", /* 0xd0 */
        "Oacute", "Ocircumflex", "applelogo", "Ograve", "Uacute",
                  "Ucircumflex", "Ugrave", "dotlessi", "circumflex", "tilde",
                  "macron", "breve", "dotaccent", "ring", "cedilla",
                  "hungarumlaut", /* 0xe0 */
        "ogonek", "caron", "Lslash", "lslash", "Scaron", "scaron", "Zcaron",
                  "zcaron", "brokenbar", "Eth", "eth", "Yacute", "yacute",
                  "Thorn", "thorn", "minus", /* 0xf0 */
        "multiply", "onesuperior", "twosuperior", "threesuperior", "onehalf",
                    "onequarter", "threequarters", "franc", "Gbreve",
                    "gbreve", "Idot", "Scedilla", "scedilla", "Cacute",
                    "cacute", "Ccaron", /* 0x100 */
        "ccaron", "dmacron"
    };

    static String[] tex8r = {
        // 0x00
        ".notdef", "dotaccent", "fi", "fl", "fraction", "hungarumlaut",
                   "Lslash", "lslash", "ogonek", "ring", ".notdef", "breve",
                   "minus", ".notdef", "Zcaron", "zcaron", // 0x10
        "caron", "dotlessi", "dotlessj", "ff", "ffi", "ffl", ".notdef",
                 ".notdef", ".notdef", ".notdef", ".notdef", ".notdef",
                 ".notdef", ".notdef", "grave", "quotesingle", // 0x20
        "space", "exclam", "quotedbl", "numbersign", "dollar", "percent",
                 "ampersand", "quoteright", "parenleft", "parenright",
                 "asterisk", "plus", "comma", "hyphen", "period", "slash",
                 // 0x30
        "zero", "one", "two", "three", "four", "five", "six", "seven",
                "eight", "nine", "colon", "semicolon", "less", "equal",
                "greater", "question", // 0x40
        "at", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
              "M", "N", "O", // 0x50
        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "bracketleft",
             "backslash", "bracketright", "asciicircum", "underscore", // 0x60
        "quoteleft", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
                     "l", "m", "n", "o", // 0x70
        "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "braceleft",
             "bar", "braceright", "asciitilde", ".notdef", // 0x80
        "Euro", ".notdef", "quotesinglbase", "florin", "quotedblbase",
                "ellipsis", "dagger", "daggerdbl", "circumflex",
                "perthousand", "Scaron", "guilsinglleft", "OE", ".notdef",
                ".notdef", ".notdef", // 0x90
        ".notdef", ".notdef", ".notdef", "quotedblleft", "quotedblright",
                   "bullet", "endash", "emdash", "tilde", "trademark",
                   "scaron", "guilsinglright", "oe", ".notdef", ".notdef",
                   "Ydieresis", // 0xA0
        ".notdef", "exclamdown", "cent", "sterling", "currency", "yen",
                   "brokenbar", "section", "dieresis", "copyright",
                   "ordfeminine", "guillemotleft", "logicalnot", "hyphen",
                   "registered", "macron", // 0xB0
        "degree", "plusminus", "twosuperior", "threesuperior", "acute", "mu",
                  "paragraph", "periodcentered", "cedilla", "onesuperior",
                  "ordmasculine", "guillemotright", "onequarter", "onehalf",
                  "threequarters", "questiondown", // 0xC0
        "Agrave", "Aacute", "Acircumflex", "Atilde", "Adieresis", "Aring",
                  "AE", "Ccedilla", "Egrave", "Eacute", "Ecircumflex",
                  "Edieresis", "Igrave", "Iacute", "Icircumflex",
                  "Idieresis", // 0xD0
        "Eth", "Ntilde", "Ograve", "Oacute", "Ocircumflex", "Otilde",
               "Odieresis", "multiply", "Oslash", "Ugrave", "Uacute",
               "Ucircumflex", "Udieresis", "Yacute", "Thorn", "germandbls",
               // 0xE0
        "agrave", "aacute", "acircumflex", "atilde", "adieresis", "aring",
                  "ae", "ccedilla", "egrave", "eacute", "ecircumflex",
                  "edieresis", "igrave", "iacute", "icircumflex",
                  "idieresis", // 0xF0
        "eth", "ntilde", "ograve", "oacute", "ocircumflex", "otilde",
               "odieresis", "divide", "oslash", "ugrave", "uacute",
               "ucircumflex", "udieresis", "yacute", "thorn", "ydieresis"
    };

    /**
     * The characters in WinAnsiEncoding
     */
    public static char[] winAnsiEncoding = {
        // not used until char 32
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
           0, 0, 0, 0, 0, 0, 0, 0, 0, // 0x20
        ' ', '!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',',
             '-', '.', '/', // 0x30
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=',
             '>', '?', '@', // 0x40
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
             'O', // 0x50
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '[', '\\',
             ']', '^', '_', // 0x60
        '?', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
             'n', 'o', // 0x70
        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '{', '|', '}',
             '~', '?', // 0x80
        '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?',
             '?', '?', // 0x90
        '?', '?',                                       // quoteleft
        '?',                                            // quoteright
        '?',                                            // quotedblleft
        '?',                                            // quotedblright
        '?',                                            // bullet
        '?',                                            // endash
        '?',                                            // emdash
        '~', '?',                                       // bullet
        '?', '?', '?', '?', '?', '?', // 0xA0
        ' ', '¡', '¢', '£', '¤', '¥', '¦', '§', '¨', '©', 'ª', '«', '¬', '­',
             '®', '¯', // 0xb0
        '°', '±', '²', '³', '´',
             'µ',                                       // This is hand-coded, the rest is assumption
        '¶',                                            // and *might* not be correct...
        '·', '¸', '¹', 'º', '»', '¼', '½', '¾', '¿', // 0xc0
        'À', 'Á', 'Â', 'Ã', 'Ä', 'Å',                   // Aring
        'Æ',                                            // AE
        'Ç', 'È', 'É', 'Ê', 'Ë', 'Ì', 'Í', 'Î', 'Ï', // 0xd0
        'Ð', 'Ñ', 'Ò', 'Ó', 'Ô', 'Õ', 'Ö', '×', 'Ø',    // Oslash
        'Ù', 'Ú', 'Û', 'Ü', 'Ý', 'Þ', 'ß', // 0xe0
        'à', 'á', 'â', 'ã', 'ä', 'å',                   // aring
        'æ',                                            // ae
        'ç', 'è', 'é', 'ê', 'ë', 'ì', 'í', 'î', 'ï', // 0xf0
        'ð', 'ñ', 'ò', 'ó', 'ô', 'õ', 'ö', '÷', 'ø', 'ù', 'ú', 'û', 'ü', 'ý',
             'þ', 'ÿ'
    };

    static String[] unicode_glyphs = {
        "A", "A", "Æ", "AE", "?", "AEacute", "?", "AEsmall", "Á", "Aacute",
        "?", "Aacutesmall", "?", "Abreve", "Â", "Acircumflex", "?",
        "Acircumflexsmall", "?", "Acute", "?", "Acutesmall", "Ä",
        "Adieresis", "?", "Adieresissmall", "À", "Agrave", "?",
        "Agravesmall", "?", "Alpha", "?", "Alphatonos", "?", "Amacron", "?",
        "Aogonek", "Å", "Aring", "?", "Aringacute", "?", "Aringsmall", "?",
        "Asmall", "Ã", "Atilde", "?", "Atildesmall", "B", "B", "?", "Beta",
        "?", "Brevesmall", "?", "Bsmall", "C", "C", "?", "Cacute", "?",
        "Caron", "?", "Caronsmall", "?", "Ccaron", "Ç", "Ccedilla", "?",
        "Ccedillasmall", "?", "Ccircumflex", "?", "Cdotaccent", "?",
        "Cedillasmall", "?", "Chi", "?", "Circumflexsmall", "?", "Csmall",
        "D", "D", "?", "Dcaron", "?", "Dcroat", "?", "Delta", "?", "Delta",
        "?", "Dieresis", "?", "DieresisAcute", "?", "DieresisGrave", "?",
        "Dieresissmall", "?", "Dotaccentsmall", "?", "Dsmall", "E", "E", "É",
        "Eacute", "?", "Eacutesmall", "?", "Ebreve", "?", "Ecaron", "Ê",
        "Ecircumflex", "?", "Ecircumflexsmall", "Ë", "Edieresis", "?",
        "Edieresissmall", "?", "Edotaccent", "È", "Egrave", "?",
        "Egravesmall", "?", "Emacron", "?", "Eng", "?", "Eogonek", "?",
        "Epsilon", "?", "Epsilontonos", "?", "Esmall", "?", "Eta", "?",
        "Etatonos", "Ð", "Eth", "?", "Ethsmall", "?", "Euro", "F", "F", "?",
        "Fsmall", "G", "G", "?", "Gamma", "?", "Gbreve", "?", "Gcaron", "?",
        "Gcircumflex", "?", "Gcommaaccent", "?", "Gdotaccent", "?", "Grave",
        "?", "Gravesmall", "?", "Gsmall", "H", "H", "?", "H18533", "?",
        "H18543", "?", "H18551", "?", "H22073", "?", "Hbar", "?",
        "Hcircumflex", "?", "Hsmall", "?", "Hungarumlaut", "?",
        "Hungarumlautsmall", "I", "I", "?", "IJ", "Í", "Iacute", "?",
        "Iacutesmall", "?", "Ibreve", "Î", "Icircumflex", "?",
        "Icircumflexsmall", "Ï", "Idieresis", "?", "Idieresissmall", "?",
        "Idotaccent", "?", "Ifraktur", "Ì", "Igrave", "?", "Igravesmall",
        "?", "Imacron", "?", "Iogonek", "?", "Iota", "?", "Iotadieresis",
        "?", "Iotatonos", "?", "Ismall", "?", "Itilde", "J", "J", "?",
        "Jcircumflex", "?", "Jsmall", "K", "K", "?", "Kappa", "?",
        "Kcommaaccent", "?", "Ksmall", "L", "L", "?", "LL", "?", "Lacute",
        "?", "Lambda", "?", "Lcaron", "?", "Lcommaaccent", "?", "Ldot", "?",
        "Lslash", "?", "Lslashsmall", "?", "Lsmall", "M", "M", "?", "Macron",
        "?", "Macronsmall", "?", "Msmall", "?", "Mu", "N", "N", "?",
        "Nacute", "?", "Ncaron", "?", "Ncommaaccent", "?", "Nsmall", "Ñ",
        "Ntilde", "?", "Ntildesmall", "?", "Nu", "O", "O", "?", "OE", "?",
        "OEsmall", "Ó", "Oacute", "?", "Oacutesmall", "?", "Obreve", "Ô",
        "Ocircumflex", "?", "Ocircumflexsmall", "Ö", "Odieresis", "?",
        "Odieresissmall", "?", "Ogoneksmall", "Ò", "Ograve", "?",
        "Ogravesmall", "?", "Ohorn", "?", "Ohungarumlaut", "?", "Omacron",
        "?", "Omega", "?", "Omega", "?", "Omegatonos", "?", "Omicron", "?",
        "Omicrontonos", "Ø", "Oslash", "?", "Oslashacute", "?",
        "Oslashsmall", "?", "Osmall", "Õ", "Otilde", "?", "Otildesmall", "P",
        "P", "?", "Phi", "?", "Pi", "?", "Psi", "?", "Psmall", "Q", "Q", "?",
        "Qsmall", "R", "R", "?", "Racute", "?", "Rcaron", "?",
        "Rcommaaccent", "?", "Rfraktur", "?", "Rho", "?", "Ringsmall", "?",
        "Rsmall", "S", "S", "?", "SF010000", "?", "SF020000", "?",
        "SF030000", "?", "SF040000", "?", "SF050000", "?", "SF060000", "?",
        "SF070000", "?", "SF080000", "?", "SF090000", "?", "SF100000", "?",
        "SF110000", "?", "SF190000", "?", "SF200000", "?", "SF210000", "?",
        "SF220000", "?", "SF230000", "?", "SF240000", "?", "SF250000", "?",
        "SF260000", "?", "SF270000", "?", "SF280000", "?", "SF360000", "?",
        "SF370000", "?", "SF380000", "?", "SF390000", "?", "SF400000", "?",
        "SF410000", "?", "SF420000", "?", "SF430000", "?", "SF440000", "?",
        "SF450000", "?", "SF460000", "?", "SF470000", "?", "SF480000", "?",
        "SF490000", "?", "SF500000", "?", "SF510000", "?", "SF520000", "?",
        "SF530000", "?", "SF540000", "?", "Sacute", "?", "Scaron", "?",
        "Scaronsmall", "?", "Scedilla", "?", "Scedilla", "?", "Scircumflex",
        "?", "Scommaaccent", "?", "Sigma", "?", "Ssmall", "T", "T", "?",
        "Tau", "?", "Tbar", "?", "Tcaron", "?", "Tcommaaccent", "?",
        "Tcommaaccent", "?", "Theta", "Þ", "Thorn", "?", "Thornsmall", "?",
        "Tildesmall", "?", "Tsmall", "U", "U", "Ú", "Uacute", "?",
        "Uacutesmall", "?", "Ubreve", "Û", "Ucircumflex", "?",
        "Ucircumflexsmall", "Ü", "Udieresis", "?", "Udieresissmall", "Ù",
        "Ugrave", "?", "Ugravesmall", "?", "Uhorn", "?", "Uhungarumlaut",
        "?", "Umacron", "?", "Uogonek", "?", "Upsilon", "?", "Upsilon1", "?",
        "Upsilondieresis", "?", "Upsilontonos", "?", "Uring", "?", "Usmall",
        "?", "Utilde", "V", "V", "?", "Vsmall", "W", "W", "?", "Wacute", "?",
        "Wcircumflex", "?", "Wdieresis", "?", "Wgrave", "?", "Wsmall", "X",
        "X", "?", "Xi", "?", "Xsmall", "Y", "Y", "Ý", "Yacute", "?",
        "Yacutesmall", "?", "Ycircumflex", "?", "Ydieresis", "?",
        "Ydieresissmall", "?", "Ygrave", "?", "Ysmall", "Z", "Z", "?",
        "Zacute", "?", "Zcaron", "?", "Zcaronsmall", "?", "Zdotaccent", "?",
        "Zeta", "?", "Zsmall", "a", "a", "á", "aacute", "?", "abreve", "â",
        "acircumflex", "´", "acute", "?", "acutecomb", "ä", "adieresis", "æ",
        "ae", "?", "aeacute", "?", "afii00208", "?", "afii10017", "?",
        "afii10018", "?", "afii10019", "?", "afii10020", "?", "afii10021",
        "?", "afii10022", "?", "afii10023", "?", "afii10024", "?",
        "afii10025", "?", "afii10026", "?", "afii10027", "?", "afii10028",
        "?", "afii10029", "?", "afii10030", "?", "afii10031", "?",
        "afii10032", "?", "afii10033", "?", "afii10034", "?", "afii10035",
        "?", "afii10036", "?", "afii10037", "?", "afii10038", "?",
        "afii10039", "?", "afii10040", "?", "afii10041", "?", "afii10042",
        "?", "afii10043", "?", "afii10044", "?", "afii10045", "?",
        "afii10046", "?", "afii10047", "?", "afii10048", "?", "afii10049",
        "?", "afii10050", "?", "afii10051", "?", "afii10052", "?",
        "afii10053", "?", "afii10054", "?", "afii10055", "?", "afii10056",
        "?", "afii10057", "?", "afii10058", "?", "afii10059", "?",
        "afii10060", "?", "afii10061", "?", "afii10062", "?", "afii10063",
        "?", "afii10064", "?", "afii10065", "?", "afii10066", "?",
        "afii10067", "?", "afii10068", "?", "afii10069", "?", "afii10070",
        "?", "afii10071", "?", "afii10072", "?", "afii10073", "?",
        "afii10074", "?", "afii10075", "?", "afii10076", "?", "afii10077",
        "?", "afii10078", "?", "afii10079", "?", "afii10080", "?",
        "afii10081", "?", "afii10082", "?", "afii10083", "?", "afii10084",
        "?", "afii10085", "?", "afii10086", "?", "afii10087", "?",
        "afii10088", "?", "afii10089", "?", "afii10090", "?", "afii10091",
        "?", "afii10092", "?", "afii10093", "?", "afii10094", "?",
        "afii10095", "?", "afii10096", "?", "afii10097", "?", "afii10098",
        "?", "afii10099", "?", "afii10100", "?", "afii10101", "?",
        "afii10102", "?", "afii10103", "?", "afii10104", "?", "afii10105",
        "?", "afii10106", "?", "afii10107", "?", "afii10108", "?",
        "afii10109", "?", "afii10110", "?", "afii10145", "?", "afii10146",
        "?", "afii10147", "?", "afii10148", "?", "afii10192", "?",
        "afii10193", "?", "afii10194", "?", "afii10195", "?", "afii10196",
        "?", "afii10831", "?", "afii10832", "?", "afii10846", "?", "afii299",
        "?", "afii300", "?", "afii301", "?", "afii57381", "?", "afii57388",
        "?", "afii57392", "?", "afii57393", "?", "afii57394", "?",
        "afii57395", "?", "afii57396", "?", "afii57397", "?", "afii57398",
        "?", "afii57399", "?", "afii57400", "?", "afii57401", "?",
        "afii57403", "?", "afii57407", "?", "afii57409", "?", "afii57410",
        "?", "afii57411", "?", "afii57412", "?", "afii57413", "?",
        "afii57414", "?", "afii57415", "?", "afii57416", "?", "afii57417",
        "?", "afii57418", "?", "afii57419", "?", "afii57420", "?",
        "afii57421", "?", "afii57422", "?", "afii57423", "?", "afii57424",
        "?", "afii57425", "?", "afii57426", "?", "afii57427", "?",
        "afii57428", "?", "afii57429", "?", "afii57430", "?", "afii57431",
        "?", "afii57432", "?", "afii57433", "?", "afii57434", "?",
        "afii57440", "?", "afii57441", "?", "afii57442", "?", "afii57443",
        "?", "afii57444", "?", "afii57445", "?", "afii57446", "?",
        "afii57448", "?", "afii57449", "?", "afii57450", "?", "afii57451",
        "?", "afii57452", "?", "afii57453", "?", "afii57454", "?",
        "afii57455", "?", "afii57456", "?", "afii57457", "?", "afii57458",
        "?", "afii57470", "?", "afii57505", "?", "afii57506", "?",
        "afii57507", "?", "afii57508", "?", "afii57509", "?", "afii57511",
        "?", "afii57512", "?", "afii57513", "?", "afii57514", "?",
        "afii57519", "?", "afii57534", "?", "afii57636", "?", "afii57645",
        "?", "afii57658", "?", "afii57664", "?", "afii57665", "?",
        "afii57666", "?", "afii57667", "?", "afii57668", "?", "afii57669",
        "?", "afii57670", "?", "afii57671", "?", "afii57672", "?",
        "afii57673", "?", "afii57674", "?", "afii57675", "?", "afii57676",
        "?", "afii57677", "?", "afii57678", "?", "afii57679", "?",
        "afii57680", "?", "afii57681", "?", "afii57682", "?", "afii57683",
        "?", "afii57684", "?", "afii57685", "?", "afii57686", "?",
        "afii57687", "?", "afii57688", "?", "afii57689", "?", "afii57690",
        "?", "afii57694", "?", "afii57695", "?", "afii57700", "?",
        "afii57705", "?", "afii57716", "?", "afii57717", "?", "afii57718",
        "?", "afii57723", "?", "afii57793", "?", "afii57794", "?",
        "afii57795", "?", "afii57796", "?", "afii57797", "?", "afii57798",
        "?", "afii57799", "?", "afii57800", "?", "afii57801", "?",
        "afii57802", "?", "afii57803", "?", "afii57804", "?", "afii57806",
        "?", "afii57807", "?", "afii57839", "?", "afii57841", "?",
        "afii57842", "?", "afii57929", "?", "afii61248", "?", "afii61289",
        "?", "afii61352", "?", "afii61573", "?", "afii61574", "?",
        "afii61575", "?", "afii61664", "?", "afii63167", "?", "afii64937",
        "à", "agrave", "?", "aleph", "?", "alpha", "?", "alphatonos", "?",
        "amacron", "&", "ampersand", "?", "ampersandsmall", "?", "angle",
        "?", "angleleft", "?", "angleright", "?", "anoteleia", "?",
        "aogonek", "?", "approxequal", "å", "aring", "?", "aringacute", "?",
        "arrowboth", "?", "arrowdblboth", "?", "arrowdbldown", "?",
        "arrowdblleft", "?", "arrowdblright", "?", "arrowdblup", "?",
        "arrowdown", "?", "arrowhorizex", "?", "arrowleft", "?",
        "arrowright", "?", "arrowup", "?", "arrowupdn", "?", "arrowupdnbse",
        "?", "arrowvertex", "^", "asciicircum", "~", "asciitilde", "*",
        "asterisk", "?", "asteriskmath", "?", "asuperior", "@", "at", "ã",
        "atilde", "b", "b", // "\", "backslash",
        "\\", "backslash", "|", "bar", "?", "beta", "?", "block", "?",
              "braceex", "{", "braceleft", "?", "braceleftbt", "?",
              "braceleftmid", "?", "bracelefttp", "}", "braceright", "?",
              "bracerightbt", "?", "bracerightmid", "?", "bracerighttp", "[",
              "bracketleft", "?", "bracketleftbt", "?", "bracketleftex", "?",
              "bracketlefttp", "]", "bracketright", "?", "bracketrightbt",
              "?", "bracketrightex", "?", "bracketrighttp", "?", "breve",
              "¦", "brokenbar", "?", "bsuperior", "?", "bullet", "c", "c",
              "?", "cacute", "?", "caron", "?", "carriagereturn", "?",
              "ccaron", "ç", "ccedilla", "?", "ccircumflex", "?",
              "cdotaccent", "¸", "cedilla", "¢", "cent", "?", "centinferior",
              "?", "centoldstyle", "?", "centsuperior", "?", "chi", "?",
              "circle", "?", "circlemultiply", "?", "circleplus", "?",
              "circumflex", "?", "club", ":", "colon", "?", "colonmonetary",
              ",", "comma", "?", "commaaccent", "?", "commainferior", "?",
              "commasuperior", "?", "congruent", "©", "copyright", "?",
              "copyrightsans", "?", "copyrightserif", "¤", "currency", "?",
              "cyrBreve", "?", "cyrFlex", "?", "cyrbreve", "?", "cyrflex",
              "d", "d", "?", "dagger", "?", "daggerdbl", "?", "dblGrave",
              "?", "dblgrave", "?", "dcaron", "?", "dcroat", "°", "degree",
              "?", "delta", "?", "diamond", "¨", "dieresis", "?",
              "dieresisacute", "?", "dieresisgrave", "?", "dieresistonos",
              "÷", "divide", "?", "dkshade", "?", "dnblock", "$", "dollar",
              "?", "dollarinferior", "?", "dollaroldstyle", "?",
              "dollarsuperior", "?", "dong", "?", "dotaccent", "?",
              "dotbelowcomb", "?", "dotlessi", "?", "dotlessj", "?",
              "dotmath", "?", "dsuperior", "e", "e", "é", "eacute", "?",
              "ebreve", "?", "ecaron", "ê", "ecircumflex", "ë", "edieresis",
              "?", "edotaccent", "è", "egrave", "8", "eight", "?",
              "eightinferior", "?", "eightoldstyle", "?", "eightsuperior",
              "?", "element", "?", "ellipsis", "?", "emacron", "?", "emdash",
              "?", "emptyset", "?", "endash", "?", "eng", "?", "eogonek",
              "?", "epsilon", "?", "epsilontonos", "=", "equal", "?",
              "equivalence", "?", "estimated", "?", "esuperior", "?", "eta",
              "?", "etatonos", "ð", "eth", "!", "exclam", "?", "exclamdbl",
              "¡", "exclamdown", "?", "exclamdownsmall", "?", "exclamsmall",
              "?", "existential", "f", "f", "?", "female", "?", "ff", "?",
              "ffi", "?", "ffl", "?", "fi", "?", "figuredash", "?",
              "filledbox", "?", "filledrect", "5", "five", "?",
              "fiveeighths", "?", "fiveinferior", "?", "fiveoldstyle", "?",
              "fivesuperior", "?", "fl", "?", "florin", "4", "four", "?",
              "fourinferior", "?", "fouroldstyle", "?", "foursuperior", "?",
              "fraction", "?", "fraction", "?", "franc", "g", "g", "?",
              "gamma", "?", "gbreve", "?", "gcaron", "?", "gcircumflex", "?",
              "gcommaaccent", "?", "gdotaccent", "ß", "germandbls", "?",
              "gradient", "`", "grave", "?", "gravecomb", ">", "greater",
              "?", "greaterequal", "«", "guillemotleft", "»",
              "guillemotright", "?", "guilsinglleft", "?", "guilsinglright",
              "h", "h", "?", "hbar", "?", "hcircumflex", "?", "heart", "?",
              "hookabovecomb", "?", "house", "?", "hungarumlaut", "-",
              "hyphen", "­", "hyphen", "?", "hypheninferior", "?",
              "hyphensuperior", "i", "i", "í", "iacute", "?", "ibreve", "î",
              "icircumflex", "ï", "idieresis", "ì", "igrave", "?", "ij", "?",
              "imacron", "?", "infinity", "?", "integral", "?", "integralbt",
              "?", "integralex", "?", "integraltp", "?", "intersection", "?",
              "invbullet", "?", "invcircle", "?", "invsmileface", "?",
              "iogonek", "?", "iota", "?", "iotadieresis", "?",
              "iotadieresistonos", "?", "iotatonos", "?", "isuperior", "?",
              "itilde", "j", "j", "?", "jcircumflex", "k", "k", "?", "kappa",
              "?", "kcommaaccent", "?", "kgreenlandic", "l", "l", "?",
              "lacute", "?", "lambda", "?", "lcaron", "?", "lcommaaccent",
              "?", "ldot", "<", "less", "?", "lessequal", "?", "lfblock",
              "?", "lira", "?", "ll", "?", "logicaland", "¬", "logicalnot",
              "?", "logicalor", "?", "longs", "?", "lozenge", "?", "lslash",
              "?", "lsuperior", "?", "ltshade", "m", "m", "¯", "macron", "?",
              "macron", "?", "male", "?", "minus", "?", "minute", "?",
              "msuperior", "µ", "mu", "?", "mu", "×", "multiply", "?",
              "musicalnote", "?", "musicalnotedbl", "n", "n", "?", "nacute",
              "?", "napostrophe", "?", "ncaron", "?", "ncommaaccent", "9",
              "nine", "?", "nineinferior", "?", "nineoldstyle", "?",
              "ninesuperior", "?", "notelement", "?", "notequal", "?",
              "notsubset", "?", "nsuperior", "ñ", "ntilde", "?", "nu", "#",
              "numbersign", "o", "o", "ó", "oacute", "?", "obreve", "ô",
              "ocircumflex", "ö", "odieresis", "?", "oe", "?", "ogonek", "ò",
              "ograve", "?", "ohorn", "?", "ohungarumlaut", "?", "omacron",
              "?", "omega", "?", "omega1", "?", "omegatonos", "?", "omicron",
              "?", "omicrontonos", "1", "one", "?", "onedotenleader", "?",
              "oneeighth", "?", "onefitted", "½", "onehalf", "?",
              "oneinferior", "?", "oneoldstyle", "¼", "onequarter", "¹",
              "onesuperior", "?", "onethird", "?", "openbullet", "ª",
              "ordfeminine", "º", "ordmasculine", "?", "orthogonal", "ø",
              "oslash", "?", "oslashacute", "?", "osuperior", "õ", "otilde",
              "p", "p", "¶", "paragraph", "(", "parenleft", "?",
              "parenleftbt", "?", "parenleftex", "?", "parenleftinferior",
              "?", "parenleftsuperior", "?", "parenlefttp", ")",
              "parenright", "?", "parenrightbt", "?", "parenrightex", "?",
              "parenrightinferior", "?", "parenrightsuperior", "?",
              "parenrighttp", "?", "partialdiff", "%", "percent", ".",
              "period", "·", "periodcentered", "?", "periodcentered", "?",
              "periodinferior", "?", "periodsuperior", "?", "perpendicular",
              "?", "perthousand", "?", "peseta", "?", "phi", "?", "phi1",
              "?", "pi", "+", "plus", "±", "plusminus", "?", "prescription",
              "?", "product", "?", "propersubset", "?", "propersuperset",
              "?", "proportional", "?", "psi", "q", "q", "?", "question",
              "¿", "questiondown", "?", "questiondownsmall", "?",
              "questionsmall", "\"", "quotedbl", // """, "quotedbl",
        "?", "quotedblbase", "?", "quotedblleft", "?", "quotedblright", "?",
             "quoteleft", "?", "quotereversed", "?", "quoteright", "?",
             "quotesinglbase", "'", "quotesingle", "r", "r", "?", "racute",
             "?", "radical", "?", "radicalex", "?", "rcaron", "?",
             "rcommaaccent", "?", "reflexsubset", "?", "reflexsuperset", "®",
             "registered", "?", "registersans", "?", "registerserif", "?",
             "revlogicalnot", "?", "rho", "?", "ring", "?", "rsuperior", "?",
             "rtblock", "?", "rupiah", "s", "s", "?", "sacute", "?",
             "scaron", "?", "scedilla", "?", "scedilla", "?", "scircumflex",
             "?", "scommaaccent", "?", "second", "§", "section", ";",
             "semicolon", "7", "seven", "?", "seveneighths", "?",
             "seveninferior", "?", "sevenoldstyle", "?", "sevensuperior",
             "?", "shade", "?", "sigma", "?", "sigma1", "?", "similar", "6",
             "six", "?", "sixinferior", "?", "sixoldstyle", "?",
             "sixsuperior", "/", "slash", "?", "smileface", " ", "space",
             " ", "space", "?", "spade", "?", "ssuperior", "£", "sterling",
             "?", "suchthat", "?", "summation", "?", "sun", "t", "t", "?",
             "tau", "?", "tbar", "?", "tcaron", "?", "tcommaaccent", "?",
             "tcommaaccent", "?", "therefore", "?", "theta", "?", "theta1",
             "þ", "thorn", "3", "three", "?", "threeeighths", "?",
             "threeinferior", "?", "threeoldstyle", "¾", "threequarters",
             "?", "threequartersemdash", "³", "threesuperior", "?", "tilde",
             "?", "tildecomb", "?", "tonos", "?", "trademark", "?",
             "trademarksans", "?", "trademarkserif", "?", "triagdn", "?",
             "triaglf", "?", "triagrt", "?", "triagup", "?", "tsuperior",
             "2", "two", "?", "twodotenleader", "?", "twoinferior", "?",
             "twooldstyle", "²", "twosuperior", "?", "twothirds", "u", "u",
             "ú", "uacute", "?", "ubreve", "û", "ucircumflex", "ü",
             "udieresis", "ù", "ugrave", "?", "uhorn", "?", "uhungarumlaut",
             "?", "umacron", "_", "underscore", "?", "underscoredbl", "?",
             "union", "?", "universal", "?", "uogonek", "?", "upblock", "?",
             "upsilon", "?", "upsilondieresis", "?", "upsilondieresistonos",
             "?", "upsilontonos", "?", "uring", "?", "utilde", "v", "v", "w",
             "w", "?", "wacute", "?", "wcircumflex", "?", "wdieresis", "?",
             "weierstrass", "?", "wgrave", "x", "x", "?", "xi", "y", "y",
             "ý", "yacute", "?", "ycircumflex", "ÿ", "ydieresis", "¥", "yen",
             "?", "ygrave", "z", "z", "?", "zacute", "?", "zcaron", "?",
             "zdotaccent", "0", "zero", "?", "zeroinferior", "?",
             "zerooldstyle", "?", "zerosuperior", "?", "zeta"
    };

    /**
     * Return the glyphname from a string,
     * eg, glyphToString("\\") returns "backslash"
     */
    public static String glyphToString(String name) {
        String ret = "";
        int i = unicode_glyphs.length;
        for (int j = 0; j < i; j += 2) {
            if (unicode_glyphs[j + 1].equals(name)) {
                ret = unicode_glyphs[j];
                j = i;
            }
        }
        return ret;
    }

    /**
     * Return the string representation of a glyphname,
     * eg stringToGlyph("backslash") returns "\\"
     */
    public static String stringToGlyph(String name) {
        String ret = "";
        int i = unicode_glyphs.length;
        for (int j = 0; j < i; j += 2) {
            if (unicode_glyphs[j].equals(name)) {
                ret = unicode_glyphs[j + 1];
                j = i;
            }
        }
        return ret;
    }

}

