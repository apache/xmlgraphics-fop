/*
 * $Id$

   Copyright 1999-2003 The Apache Software Foundation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 *
 *!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 *   Automatically generated from xml-lang.xml by
 *    xml-lang.xsl $Revision$ $Name$.
 *                  DO NOT EDIT!
 *!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */ 

package org.apache.fop.datatypes;

import java.util.HashMap;

/**
 * A class for accessing and validating:
 * ISO 3166 country codes,
 * ISO 639-2T, 639-2B and 639-1 language codes,
 * and ISO 15924 script codes.
 * @see <a href="http://www.iso.ch/iso/en/prods-services/iso3166ma/02iso-3166-code-lists/index.html">iso3166</a>
 * @see <a href="http://www.loc.gov/standards/iso639-2/">iso639-2</a>
 * @see <a href="http://www.evertype.com/standards/iso15924/document/index.html">iso15924</a>
 */
public class CountryLanguageScript {

    /**
     * Map of English country names keyed on ISO 3166 country code.
     */
    private static final HashMap iso3166ToName;

    /**
     * Map of English language names keyed on ISO 639-2 terminology code.
     */
    private static final HashMap iso639_2T_ToENLang;

    /**
     * Map of French language names keyed on ISO 639-2 terminology code.
     */
    private static final HashMap iso639_2T_ToFRLang;

    /**
     * Map of ISO 639-2 terminology codes keyed on ISO 639-2 bibliographical
     * code.
     */
    private static final HashMap iso639_2B_To_639_2T;

    /**
     * Map of ISO 639-2 terminology codes keyed on ISO 639-1 2-letter code.
     */
    private static final HashMap iso639_1_To_639_2T;

    /**
     * Map of ISO 639-1 2-letter codes keyed on ISO 639-2 terminology code.
     */
    private static final HashMap iso639_2T_To_639_1;

    /**
     * Map of English script names keyed on ISO 15924 script code.
     */
    private static final HashMap iso15924ToName;

    static {
        iso3166ToName = new HashMap(239);

        iso639_2T_ToENLang = new HashMap((int)(466 * 1.4));
        iso639_2T_ToFRLang = new HashMap((int)(466 * 1.4));
        iso639_2B_To_639_2T = new HashMap((int)(466 * 1.4));
        iso639_1_To_639_2T = new HashMap((int)(466 * 1.4));
        iso639_2T_To_639_1 = new HashMap((int)(466 * 1.4));

        iso15924ToName = new HashMap(186);
    
        iso3166ToName.put("AF", "AFGHANISTAN");
  
        iso3166ToName.put("AL", "ALBANIA");
  
        iso3166ToName.put("DZ", "ALGERIA");
  
        iso3166ToName.put("AS", "AMERICAN SAMOA");
  
        iso3166ToName.put("AD", "ANDORRA");
  
        iso3166ToName.put("AO", "ANGOLA");
  
        iso3166ToName.put("AI", "ANGUILLA");
  
        iso3166ToName.put("AQ", "ANTARCTICA");
  
        iso3166ToName.put("AG", "ANTIGUA AND BARBUDA");
  
        iso3166ToName.put("AR", "ARGENTINA");
  
        iso3166ToName.put("AM", "ARMENIA");
  
        iso3166ToName.put("AW", "ARUBA");
  
        iso3166ToName.put("AU", "AUSTRALIA");
  
        iso3166ToName.put("AT", "AUSTRIA");
  
        iso3166ToName.put("AZ", "AZERBAIJAN");
  
        iso3166ToName.put("BS", "BAHAMAS");
  
        iso3166ToName.put("BH", "BAHRAIN");
  
        iso3166ToName.put("BD", "BANGLADESH");
  
        iso3166ToName.put("BB", "BARBADOS");
  
        iso3166ToName.put("BY", "BELARUS");
  
        iso3166ToName.put("BE", "BELGIUM");
  
        iso3166ToName.put("BZ", "BELIZE");
  
        iso3166ToName.put("BJ", "BENIN");
  
        iso3166ToName.put("BM", "BERMUDA");
  
        iso3166ToName.put("BT", "BHUTAN");
  
        iso3166ToName.put("BO", "BOLIVIA");
  
        iso3166ToName.put("BA", "BOSNIA AND HERZEGOVINA");
  
        iso3166ToName.put("BW", "BOTSWANA");
  
        iso3166ToName.put("BV", "BOUVET ISLAND");
  
        iso3166ToName.put("BR", "BRAZIL");
  
        iso3166ToName.put("IO", "BRITISH INDIAN OCEAN TERRITORY");
  
        iso3166ToName.put("BN", "BRUNEI DARUSSALAM");
  
        iso3166ToName.put("BG", "BULGARIA");
  
        iso3166ToName.put("BF", "BURKINA FASO");
  
        iso3166ToName.put("BI", "BURUNDI");
  
        iso3166ToName.put("KH", "CAMBODIA");
  
        iso3166ToName.put("CM", "CAMEROON");
  
        iso3166ToName.put("CA", "CANADA");
  
        iso3166ToName.put("CV", "CAPE VERDE");
  
        iso3166ToName.put("KY", "CAYMAN ISLANDS");
  
        iso3166ToName.put("CF", "CENTRAL AFRICAN REPUBLIC");
  
        iso3166ToName.put("TD", "CHAD");
  
        iso3166ToName.put("CL", "CHILE");
  
        iso3166ToName.put("CN", "CHINA");
  
        iso3166ToName.put("CX", "CHRISTMAS ISLAND");
  
        iso3166ToName.put("CC", "COCOS (KEELING) ISLANDS");
  
        iso3166ToName.put("CO", "COLOMBIA");
  
        iso3166ToName.put("KM", "COMOROS");
  
        iso3166ToName.put("CG", "CONGO");
  
        iso3166ToName.put("CD", "CONGO, THE DEMOCRATIC REPUBLIC OF THE");
  
        iso3166ToName.put("CK", "COOK ISLANDS");
  
        iso3166ToName.put("CR", "COSTA RICA");
  
        iso3166ToName.put("CI", "COTE D'IVOIRE");
  
        iso3166ToName.put("HR", "CROATIA");
  
        iso3166ToName.put("CU", "CUBA");
  
        iso3166ToName.put("CY", "CYPRUS");
  
        iso3166ToName.put("CZ", "CZECH REPUBLIC");
  
        iso3166ToName.put("DK", "DENMARK");
  
        iso3166ToName.put("DJ", "DJIBOUTI");
  
        iso3166ToName.put("DM", "DOMINICA");
  
        iso3166ToName.put("DO", "DOMINICAN REPUBLIC");
  
        iso3166ToName.put("TP", "EAST TIMOR");
  
        iso3166ToName.put("EC", "ECUADOR");
  
        iso3166ToName.put("EG", "EGYPT");
  
        iso3166ToName.put("SV", "EL SALVADOR");
  
        iso3166ToName.put("GQ", "EQUATORIAL GUINEA");
  
        iso3166ToName.put("ER", "ERITREA");
  
        iso3166ToName.put("EE", "ESTONIA");
  
        iso3166ToName.put("ET", "ETHIOPIA");
  
        iso3166ToName.put("FK", "FALKLAND ISLANDS (MALVINAS)");
  
        iso3166ToName.put("FO", "FAROE ISLANDS");
  
        iso3166ToName.put("FJ", "FIJI");
  
        iso3166ToName.put("FI", "FINLAND");
  
        iso3166ToName.put("FR", "FRANCE");
  
        iso3166ToName.put("GF", "FRENCH GUIANA");
  
        iso3166ToName.put("PF", "FRENCH POLYNESIA");
  
        iso3166ToName.put("TF", "FRENCH SOUTHERN TERRITORIES");
  
        iso3166ToName.put("GA", "GABON");
  
        iso3166ToName.put("GM", "GAMBIA");
  
        iso3166ToName.put("GE", "GEORGIA");
  
        iso3166ToName.put("DE", "GERMANY");
  
        iso3166ToName.put("GH", "GHANA");
  
        iso3166ToName.put("GI", "GIBRALTAR");
  
        iso3166ToName.put("GR", "GREECE");
  
        iso3166ToName.put("GL", "GREENLAND");
  
        iso3166ToName.put("GD", "GRENADA");
  
        iso3166ToName.put("GP", "GUADELOUPE");
  
        iso3166ToName.put("GU", "GUAM");
  
        iso3166ToName.put("GT", "GUATEMALA");
  
        iso3166ToName.put("GN", "GUINEA");
  
        iso3166ToName.put("GW", "GUINEA-BISSAU");
  
        iso3166ToName.put("GY", "GUYANA");
  
        iso3166ToName.put("HT", "HAITI");
  
        iso3166ToName.put("HM", "HEARD ISLAND AND MCDONALD ISLANDS");
  
        iso3166ToName.put("VA", "HOLY SEE (VATICAN CITY STATE)");
  
        iso3166ToName.put("HN", "HONDURAS");
  
        iso3166ToName.put("HK", "HONG KONG");
  
        iso3166ToName.put("HU", "HUNGARY");
  
        iso3166ToName.put("IS", "ICELAND");
  
        iso3166ToName.put("IN", "INDIA");
  
        iso3166ToName.put("ID", "INDONESIA");
  
        iso3166ToName.put("IR", "IRAN, ISLAMIC REPUBLIC OF");
  
        iso3166ToName.put("IQ", "IRAQ");
  
        iso3166ToName.put("IE", "IRELAND");
  
        iso3166ToName.put("IL", "ISRAEL");
  
        iso3166ToName.put("IT", "ITALY");
  
        iso3166ToName.put("JM", "JAMAICA");
  
        iso3166ToName.put("JP", "JAPAN");
  
        iso3166ToName.put("JO", "JORDAN");
  
        iso3166ToName.put("KZ", "KAZAKSTAN");
  
        iso3166ToName.put("KE", "KENYA");
  
        iso3166ToName.put("KI", "KIRIBATI");
  
        iso3166ToName.put("KP", "KOREA, DEMOCRATIC PEOPLE'S REPUBLIC OF");
  
        iso3166ToName.put("KR", "KOREA, REPUBLIC OF");
  
        iso3166ToName.put("KW", "KUWAIT");
  
        iso3166ToName.put("KG", "KYRGYZSTAN");
  
        iso3166ToName.put("LA", "LAO PEOPLE'S DEMOCRATIC REPUBLIC");
  
        iso3166ToName.put("LV", "LATVIA");
  
        iso3166ToName.put("LB", "LEBANON");
  
        iso3166ToName.put("LS", "LESOTHO");
  
        iso3166ToName.put("LR", "LIBERIA");
  
        iso3166ToName.put("LY", "LIBYAN ARAB JAMAHIRIYA");
  
        iso3166ToName.put("LI", "LIECHTENSTEIN");
  
        iso3166ToName.put("LT", "LITHUANIA");
  
        iso3166ToName.put("LU", "LUXEMBOURG");
  
        iso3166ToName.put("MO", "MACAU");
  
        iso3166ToName.put("MK", "MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF");
  
        iso3166ToName.put("MG", "MADAGASCAR");
  
        iso3166ToName.put("MW", "MALAWI");
  
        iso3166ToName.put("MY", "MALAYSIA");
  
        iso3166ToName.put("MV", "MALDIVES");
  
        iso3166ToName.put("ML", "MALI");
  
        iso3166ToName.put("MT", "MALTA");
  
        iso3166ToName.put("MH", "MARSHALL ISLANDS");
  
        iso3166ToName.put("MQ", "MARTINIQUE");
  
        iso3166ToName.put("MR", "MAURITANIA");
  
        iso3166ToName.put("MU", "MAURITIUS");
  
        iso3166ToName.put("YT", "MAYOTTE");
  
        iso3166ToName.put("MX", "MEXICO");
  
        iso3166ToName.put("FM", "MICRONESIA, FEDERATED STATES OF");
  
        iso3166ToName.put("MD", "MOLDOVA, REPUBLIC OF");
  
        iso3166ToName.put("MC", "MONACO");
  
        iso3166ToName.put("MN", "MONGOLIA");
  
        iso3166ToName.put("MS", "MONTSERRAT");
  
        iso3166ToName.put("MA", "MOROCCO");
  
        iso3166ToName.put("MZ", "MOZAMBIQUE");
  
        iso3166ToName.put("MM", "MYANMAR");
  
        iso3166ToName.put("NA", "NAMIBIA");
  
        iso3166ToName.put("NR", "NAURU");
  
        iso3166ToName.put("NP", "NEPAL");
  
        iso3166ToName.put("NL", "NETHERLANDS");
  
        iso3166ToName.put("AN", "NETHERLANDS ANTILLES");
  
        iso3166ToName.put("NC", "NEW CALEDONIA");
  
        iso3166ToName.put("NZ", "NEW ZEALAND");
  
        iso3166ToName.put("NI", "NICARAGUA");
  
        iso3166ToName.put("NE", "NIGER");
  
        iso3166ToName.put("NG", "NIGERIA");
  
        iso3166ToName.put("NU", "NIUE");
  
        iso3166ToName.put("NF", "NORFOLK ISLAND");
  
        iso3166ToName.put("MP", "NORTHERN MARIANA ISLANDS");
  
        iso3166ToName.put("NO", "NORWAY");
  
        iso3166ToName.put("OM", "OMAN");
  
        iso3166ToName.put("PK", "PAKISTAN");
  
        iso3166ToName.put("PW", "PALAU");
  
        iso3166ToName.put("PS", "PALESTINIAN TERRITORY, OCCUPIED");
  
        iso3166ToName.put("PA", "PANAMA");
  
        iso3166ToName.put("PG", "PAPUA NEW GUINEA");
  
        iso3166ToName.put("PY", "PARAGUAY");
  
        iso3166ToName.put("PE", "PERU");
  
        iso3166ToName.put("PH", "PHILIPPINES");
  
        iso3166ToName.put("PN", "PITCAIRN");
  
        iso3166ToName.put("PL", "POLAND");
  
        iso3166ToName.put("PT", "PORTUGAL");
  
        iso3166ToName.put("PR", "PUERTO RICO");
  
        iso3166ToName.put("QA", "QATAR");
  
        iso3166ToName.put("RE", "REUNION");
  
        iso3166ToName.put("RO", "ROMANIA");
  
        iso3166ToName.put("RU", "RUSSIAN FEDERATION");
  
        iso3166ToName.put("RW", "RWANDA");
  
        iso3166ToName.put("SH", "SAINT HELENA");
  
        iso3166ToName.put("KN", "SAINT KITTS AND NEVIS");
  
        iso3166ToName.put("LC", "SAINT LUCIA");
  
        iso3166ToName.put("PM", "SAINT PIERRE AND MIQUELON");
  
        iso3166ToName.put("VC", "SAINT VINCENT AND THE GRENADINES");
  
        iso3166ToName.put("WS", "SAMOA");
  
        iso3166ToName.put("SM", "SAN MARINO");
  
        iso3166ToName.put("ST", "SAO TOME AND PRINCIPE");
  
        iso3166ToName.put("SA", "SAUDI ARABIA");
  
        iso3166ToName.put("SN", "SENEGAL");
  
        iso3166ToName.put("SC", "SEYCHELLES");
  
        iso3166ToName.put("SL", "SIERRA LEONE");
  
        iso3166ToName.put("SG", "SINGAPORE");
  
        iso3166ToName.put("SK", "SLOVAKIA");
  
        iso3166ToName.put("SI", "SLOVENIA");
  
        iso3166ToName.put("SB", "SOLOMON ISLANDS");
  
        iso3166ToName.put("SO", "SOMALIA");
  
        iso3166ToName.put("ZA", "SOUTH AFRICA");
  
        iso3166ToName.put("GS", "SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS");
  
        iso3166ToName.put("ES", "SPAIN");
  
        iso3166ToName.put("LK", "SRI LANKA");
  
        iso3166ToName.put("SD", "SUDAN");
  
        iso3166ToName.put("SR", "SURINAME");
  
        iso3166ToName.put("SJ", "SVALBARD AND JAN MAYEN");
  
        iso3166ToName.put("SZ", "SWAZILAND");
  
        iso3166ToName.put("SE", "SWEDEN");
  
        iso3166ToName.put("CH", "SWITZERLAND");
  
        iso3166ToName.put("SY", "SYRIAN ARAB REPUBLIC");
  
        iso3166ToName.put("TW", "TAIWAN, PROVINCE OF CHINA");
  
        iso3166ToName.put("TJ", "TAJIKISTAN");
  
        iso3166ToName.put("TZ", "TANZANIA, UNITED REPUBLIC OF");
  
        iso3166ToName.put("TH", "THAILAND");
  
        iso3166ToName.put("TG", "TOGO");
  
        iso3166ToName.put("TK", "TOKELAU");
  
        iso3166ToName.put("TO", "TONGA");
  
        iso3166ToName.put("TT", "TRINIDAD AND TOBAGO");
  
        iso3166ToName.put("TN", "TUNISIA");
  
        iso3166ToName.put("TR", "TURKEY");
  
        iso3166ToName.put("TM", "TURKMENISTAN");
  
        iso3166ToName.put("TC", "TURKS AND CAICOS ISLANDS");
  
        iso3166ToName.put("TV", "TUVALU");
  
        iso3166ToName.put("UG", "UGANDA");
  
        iso3166ToName.put("UA", "UKRAINE");
  
        iso3166ToName.put("AE", "UNITED ARAB EMIRATES");
  
        iso3166ToName.put("GB", "UNITED KINGDOM");
  
        iso3166ToName.put("US", "UNITED STATES");
  
        iso3166ToName.put("UM", "UNITED STATES MINOR OUTLYING ISLANDS");
  
        iso3166ToName.put("UY", "URUGUAY");
  
        iso3166ToName.put("UZ", "UZBEKISTAN");
  
        iso3166ToName.put("VU", "VANUATU");
  
        iso3166ToName.put("VE", "VENEZUELA");
  
        iso3166ToName.put("VN", "VIET NAM");
  
        iso3166ToName.put("VG", "VIRGIN ISLANDS, BRITISH");
  
        iso3166ToName.put("VI", "VIRGIN ISLANDS, U.S.");
  
        iso3166ToName.put("WF", "WALLIS AND FUTUNA");
  
        iso3166ToName.put("EH", "WESTERN SAHARA");
  
        iso3166ToName.put("YE", "YEMEN");
  
        iso3166ToName.put("YU", "YUGOSLAVIA");
  
        iso3166ToName.put("ZM", "ZAMBIA");
  
        iso3166ToName.put("ZW", "ZIMBABWE");
  
        // Afar
        iso639_2T_ToENLang.put("aar", "Afar");
        iso639_2T_ToFRLang.put("aar", "afar");
    
        iso639_1_To_639_2T.put("aa", "aar");
        iso639_2T_To_639_1.put("aar", "aa");
  
        // Abkhazian
        iso639_2T_ToENLang.put("abk", "Abkhazian");
        iso639_2T_ToFRLang.put("abk", "abkhaze");
    
        iso639_1_To_639_2T.put("ab", "abk");
        iso639_2T_To_639_1.put("abk", "ab");
  
        // Achinese
        iso639_2T_ToENLang.put("ace", "Achinese");
        iso639_2T_ToFRLang.put("ace", "aceh");
    
        // Acoli
        iso639_2T_ToENLang.put("ach", "Acoli");
        iso639_2T_ToFRLang.put("ach", "acoli");
    
        // Adangme
        iso639_2T_ToENLang.put("ada", "Adangme");
        iso639_2T_ToFRLang.put("ada", "adangme");
    
        // Afro-Asiatic (Other)
        iso639_2T_ToENLang.put("afa", "Afro-Asiatic (Other)");
        iso639_2T_ToFRLang.put("afa", "afro-asiatiques, autres langues");
    
        // Afrihili
        iso639_2T_ToENLang.put("afh", "Afrihili");
        iso639_2T_ToFRLang.put("afh", "afrihili");
    
        // Afrikaans
        iso639_2T_ToENLang.put("afr", "Afrikaans");
        iso639_2T_ToFRLang.put("afr", "afrikaans");
    
        iso639_1_To_639_2T.put("af", "afr");
        iso639_2T_To_639_1.put("afr", "af");
  
        // Akan
        iso639_2T_ToENLang.put("aka", "Akan");
        iso639_2T_ToFRLang.put("aka", "akan");
    
        // Akkadian
        iso639_2T_ToENLang.put("akk", "Akkadian");
        iso639_2T_ToFRLang.put("akk", "akkadien");
    
        // Albanian
        iso639_2T_ToENLang.put("sqi", "Albanian");
        iso639_2T_ToFRLang.put("sqi", "albanais");
    
        iso639_2B_To_639_2T.put("alb", "sqi");
  
        iso639_1_To_639_2T.put("sq", "sqi");
        iso639_2T_To_639_1.put("sqi", "sq");
  
        // Aleut
        iso639_2T_ToENLang.put("ale", "Aleut");
        iso639_2T_ToFRLang.put("ale", "aléoute");
    
        // Algonquian languages
        iso639_2T_ToENLang.put("alg", "Algonquian languages");
        iso639_2T_ToFRLang.put("alg", "algonquines, langues");
    
        // Amharic
        iso639_2T_ToENLang.put("amh", "Amharic");
        iso639_2T_ToFRLang.put("amh", "amharique");
    
        iso639_1_To_639_2T.put("am", "amh");
        iso639_2T_To_639_1.put("amh", "am");
  
        // English, Old (ca.450-1100)
        iso639_2T_ToENLang.put("ang", "English, Old (ca.450-1100)");
        iso639_2T_ToFRLang.put("ang", "anglo-saxon (ca.450-1100)");
    
        // Apache languages
        iso639_2T_ToENLang.put("apa", "Apache languages");
        iso639_2T_ToFRLang.put("apa", "apache");
    
        // Arabic
        iso639_2T_ToENLang.put("ara", "Arabic");
        iso639_2T_ToFRLang.put("ara", "arabe");
    
        iso639_1_To_639_2T.put("ar", "ara");
        iso639_2T_To_639_1.put("ara", "ar");
  
        // Aramaic
        iso639_2T_ToENLang.put("arc", "Aramaic");
        iso639_2T_ToFRLang.put("arc", "araméen");
    
        // Armenian
        iso639_2T_ToENLang.put("hye", "Armenian");
        iso639_2T_ToFRLang.put("hye", "arménien");
    
        iso639_2B_To_639_2T.put("arm", "hye");
  
        iso639_1_To_639_2T.put("hy", "hye");
        iso639_2T_To_639_1.put("hye", "hy");
  
        // Araucanian
        iso639_2T_ToENLang.put("arn", "Araucanian");
        iso639_2T_ToFRLang.put("arn", "araucan");
    
        // Arapaho
        iso639_2T_ToENLang.put("arp", "Arapaho");
        iso639_2T_ToFRLang.put("arp", "arapaho");
    
        // Artificial (Other)
        iso639_2T_ToENLang.put("art", "Artificial (Other)");
        iso639_2T_ToFRLang.put("art", "artificielles, autres langues");
    
        // Arawak
        iso639_2T_ToENLang.put("arw", "Arawak");
        iso639_2T_ToFRLang.put("arw", "arawak");
    
        // Assamese
        iso639_2T_ToENLang.put("asm", "Assamese");
        iso639_2T_ToFRLang.put("asm", "assamais");
    
        iso639_1_To_639_2T.put("as", "asm");
        iso639_2T_To_639_1.put("asm", "as");
  
        // Asturian; Bable
        iso639_2T_ToENLang.put("ast", "Asturian; Bable");
        iso639_2T_ToFRLang.put("ast", "asturien; bable");
    
        // Athapascan languages
        iso639_2T_ToENLang.put("ath", "Athapascan languages");
        iso639_2T_ToFRLang.put("ath", "athapascanes, langues");
    
        // Australian languages
        iso639_2T_ToENLang.put("aus", "Australian languages");
        iso639_2T_ToFRLang.put("aus", "australiennes, langues");
    
        // Avaric
        iso639_2T_ToENLang.put("ava", "Avaric");
        iso639_2T_ToFRLang.put("ava", "avar");
    
        // Avestan
        iso639_2T_ToENLang.put("ave", "Avestan");
        iso639_2T_ToFRLang.put("ave", "avestique");
    
        iso639_1_To_639_2T.put("ae", "ave");
        iso639_2T_To_639_1.put("ave", "ae");
  
        // Awadhi
        iso639_2T_ToENLang.put("awa", "Awadhi");
        iso639_2T_ToFRLang.put("awa", "awadhi");
    
        // Aymara
        iso639_2T_ToENLang.put("aym", "Aymara");
        iso639_2T_ToFRLang.put("aym", "aymara");
    
        iso639_1_To_639_2T.put("ay", "aym");
        iso639_2T_To_639_1.put("aym", "ay");
  
        // Azerbaijani
        iso639_2T_ToENLang.put("aze", "Azerbaijani");
        iso639_2T_ToFRLang.put("aze", "azéri");
    
        iso639_1_To_639_2T.put("az", "aze");
        iso639_2T_To_639_1.put("aze", "az");
  
        // Banda
        iso639_2T_ToENLang.put("bad", "Banda");
        iso639_2T_ToFRLang.put("bad", "banda");
    
        // Bamileke languages
        iso639_2T_ToENLang.put("bai", "Bamileke languages");
        iso639_2T_ToFRLang.put("bai", "bamilékés, langues");
    
        // Bashkir
        iso639_2T_ToENLang.put("bak", "Bashkir");
        iso639_2T_ToFRLang.put("bak", "bachkir");
    
        iso639_1_To_639_2T.put("ba", "bak");
        iso639_2T_To_639_1.put("bak", "ba");
  
        // Baluchi
        iso639_2T_ToENLang.put("bal", "Baluchi");
        iso639_2T_ToFRLang.put("bal", "baloutchi");
    
        // Bambara
        iso639_2T_ToENLang.put("bam", "Bambara");
        iso639_2T_ToFRLang.put("bam", "bambara");
    
        // Balinese
        iso639_2T_ToENLang.put("ban", "Balinese");
        iso639_2T_ToFRLang.put("ban", "balinais");
    
        // Basque
        iso639_2T_ToENLang.put("eus", "Basque");
        iso639_2T_ToFRLang.put("eus", "basque");
    
        iso639_2B_To_639_2T.put("baq", "eus");
  
        iso639_1_To_639_2T.put("eu", "eus");
        iso639_2T_To_639_1.put("eus", "eu");
  
        // Basa
        iso639_2T_ToENLang.put("bas", "Basa");
        iso639_2T_ToFRLang.put("bas", "basa");
    
        // Baltic (Other)
        iso639_2T_ToENLang.put("bat", "Baltic (Other)");
        iso639_2T_ToFRLang.put("bat", "baltiques, autres langues");
    
        // Beja
        iso639_2T_ToENLang.put("bej", "Beja");
        iso639_2T_ToFRLang.put("bej", "bedja");
    
        // Belarusian
        iso639_2T_ToENLang.put("bel", "Belarusian");
        iso639_2T_ToFRLang.put("bel", "biélorusse");
    
        iso639_1_To_639_2T.put("be", "bel");
        iso639_2T_To_639_1.put("bel", "be");
  
        // Bemba
        iso639_2T_ToENLang.put("bem", "Bemba");
        iso639_2T_ToFRLang.put("bem", "bemba");
    
        // Bengali
        iso639_2T_ToENLang.put("ben", "Bengali");
        iso639_2T_ToFRLang.put("ben", "bengali");
    
        iso639_1_To_639_2T.put("bn", "ben");
        iso639_2T_To_639_1.put("ben", "bn");
  
        // Berber (Other)
        iso639_2T_ToENLang.put("ber", "Berber (Other)");
        iso639_2T_ToFRLang.put("ber", "berbères, autres langues");
    
        // Bhojpuri
        iso639_2T_ToENLang.put("bho", "Bhojpuri");
        iso639_2T_ToFRLang.put("bho", "bhojpuri");
    
        // Bihari
        iso639_2T_ToENLang.put("bih", "Bihari");
        iso639_2T_ToFRLang.put("bih", "bihari");
    
        iso639_1_To_639_2T.put("bh", "bih");
        iso639_2T_To_639_1.put("bih", "bh");
  
        // Bikol
        iso639_2T_ToENLang.put("bik", "Bikol");
        iso639_2T_ToFRLang.put("bik", "bikol");
    
        // Bini
        iso639_2T_ToENLang.put("bin", "Bini");
        iso639_2T_ToFRLang.put("bin", "bini");
    
        // Bislama
        iso639_2T_ToENLang.put("bis", "Bislama");
        iso639_2T_ToFRLang.put("bis", "bichlamar");
    
        iso639_1_To_639_2T.put("bi", "bis");
        iso639_2T_To_639_1.put("bis", "bi");
  
        // Siksika
        iso639_2T_ToENLang.put("bla", "Siksika");
        iso639_2T_ToFRLang.put("bla", "blackfoot");
    
        // Bantu (Other)
        iso639_2T_ToENLang.put("bnt", "Bantu (Other)");
        iso639_2T_ToFRLang.put("bnt", "bantoues, autres langues");
    
        // Tibetan
        iso639_2T_ToENLang.put("bod", "Tibetan");
        iso639_2T_ToFRLang.put("bod", "tibétain");
    
        iso639_2B_To_639_2T.put("tib", "bod");
  
        iso639_1_To_639_2T.put("bo", "bod");
        iso639_2T_To_639_1.put("bod", "bo");
  
        // Bosnian
        iso639_2T_ToENLang.put("bos", "Bosnian");
        iso639_2T_ToFRLang.put("bos", "bosniaque");
    
        iso639_1_To_639_2T.put("bs", "bos");
        iso639_2T_To_639_1.put("bos", "bs");
  
        // Braj
        iso639_2T_ToENLang.put("bra", "Braj");
        iso639_2T_ToFRLang.put("bra", "braj");
    
        // Breton
        iso639_2T_ToENLang.put("bre", "Breton");
        iso639_2T_ToFRLang.put("bre", "breton");
    
        iso639_1_To_639_2T.put("br", "bre");
        iso639_2T_To_639_1.put("bre", "br");
  
        // Batak (Indonesia)
        iso639_2T_ToENLang.put("btk", "Batak (Indonesia)");
        iso639_2T_ToFRLang.put("btk", "batak (Indonésie)");
    
        // Buriat
        iso639_2T_ToENLang.put("bua", "Buriat");
        iso639_2T_ToFRLang.put("bua", "bouriate");
    
        // Buginese
        iso639_2T_ToENLang.put("bug", "Buginese");
        iso639_2T_ToFRLang.put("bug", "bugi");
    
        // Bulgarian
        iso639_2T_ToENLang.put("bul", "Bulgarian");
        iso639_2T_ToFRLang.put("bul", "bulgare");
    
        iso639_1_To_639_2T.put("bg", "bul");
        iso639_2T_To_639_1.put("bul", "bg");
  
        // Burmese
        iso639_2T_ToENLang.put("mya", "Burmese");
        iso639_2T_ToFRLang.put("mya", "birman");
    
        iso639_2B_To_639_2T.put("bur", "mya");
  
        iso639_1_To_639_2T.put("my", "mya");
        iso639_2T_To_639_1.put("mya", "my");
  
        // Caddo
        iso639_2T_ToENLang.put("cad", "Caddo");
        iso639_2T_ToFRLang.put("cad", "caddo");
    
        // Central American Indian (Other)
        iso639_2T_ToENLang.put("cai", "Central American Indian (Other)");
        iso639_2T_ToFRLang.put("cai", "indiennes d'Amérique centrale, autres langues");
    
        // Carib
        iso639_2T_ToENLang.put("car", "Carib");
        iso639_2T_ToFRLang.put("car", "caribe");
    
        // Catalan
        iso639_2T_ToENLang.put("cat", "Catalan");
        iso639_2T_ToFRLang.put("cat", "catalan");
    
        iso639_1_To_639_2T.put("ca", "cat");
        iso639_2T_To_639_1.put("cat", "ca");
  
        // Caucasian (Other)
        iso639_2T_ToENLang.put("cau", "Caucasian (Other)");
        iso639_2T_ToFRLang.put("cau", "caucasiennes, autres langues");
    
        // Cebuano
        iso639_2T_ToENLang.put("ceb", "Cebuano");
        iso639_2T_ToFRLang.put("ceb", "cebuano");
    
        // Celtic (Other)
        iso639_2T_ToENLang.put("cel", "Celtic (Other)");
        iso639_2T_ToFRLang.put("cel", "celtiques, autres langues");
    
        // Czech
        iso639_2T_ToENLang.put("ces", "Czech");
        iso639_2T_ToFRLang.put("ces", "tchèque");
    
        iso639_2B_To_639_2T.put("cze", "ces");
  
        iso639_1_To_639_2T.put("cs", "ces");
        iso639_2T_To_639_1.put("ces", "cs");
  
        // Chamorro
        iso639_2T_ToENLang.put("cha", "Chamorro");
        iso639_2T_ToFRLang.put("cha", "chamorro");
    
        iso639_1_To_639_2T.put("ch", "cha");
        iso639_2T_To_639_1.put("cha", "ch");
  
        // Chibcha
        iso639_2T_ToENLang.put("chb", "Chibcha");
        iso639_2T_ToFRLang.put("chb", "chibcha");
    
        // Chechen 
        iso639_2T_ToENLang.put("che", "Chechen ");
        iso639_2T_ToFRLang.put("che", "tchétchène");
    
        iso639_1_To_639_2T.put("ce", "che");
        iso639_2T_To_639_1.put("che", "ce");
  
        // Chagatai
        iso639_2T_ToENLang.put("chg", "Chagatai");
        iso639_2T_ToFRLang.put("chg", "djaghataï");
    
        // Chinese
        iso639_2T_ToENLang.put("zho", "Chinese");
        iso639_2T_ToFRLang.put("zho", "chinois");
    
        iso639_2B_To_639_2T.put("chi", "zho");
  
        iso639_1_To_639_2T.put("zh", "zho");
        iso639_2T_To_639_1.put("zho", "zh");
  
        // Chuukese
        iso639_2T_ToENLang.put("chk", "Chuukese");
        iso639_2T_ToFRLang.put("chk", "chuuk");
    
        // Mari
        iso639_2T_ToENLang.put("chm", "Mari");
        iso639_2T_ToFRLang.put("chm", "mari");
    
        // Chinook jargon
        iso639_2T_ToENLang.put("chn", "Chinook jargon");
        iso639_2T_ToFRLang.put("chn", "chinook, jargon");
    
        // Choctaw
        iso639_2T_ToENLang.put("cho", "Choctaw");
        iso639_2T_ToFRLang.put("cho", "choctaw");
    
        // Chipewyan
        iso639_2T_ToENLang.put("chp", "Chipewyan");
        iso639_2T_ToFRLang.put("chp", "chipewyan");
    
        // Cherokee
        iso639_2T_ToENLang.put("chr", "Cherokee");
        iso639_2T_ToFRLang.put("chr", "cherokee");
    
        // Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic
        iso639_2T_ToENLang.put("chu", "Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic");
        iso639_2T_ToFRLang.put("chu", "slavon d'église; vieux slave; slavon liturgique; vieux bulgare");
    
        iso639_1_To_639_2T.put("cu", "chu");
        iso639_2T_To_639_1.put("chu", "cu");
  
        // Chuvash
        iso639_2T_ToENLang.put("chv", "Chuvash");
        iso639_2T_ToFRLang.put("chv", "tchouvache");
    
        iso639_1_To_639_2T.put("cv", "chv");
        iso639_2T_To_639_1.put("chv", "cv");
  
        // Cheyenne
        iso639_2T_ToENLang.put("chy", "Cheyenne");
        iso639_2T_ToFRLang.put("chy", "cheyenne");
    
        // Chamic languages
        iso639_2T_ToENLang.put("cmc", "Chamic languages");
        iso639_2T_ToFRLang.put("cmc", "chames, langues");
    
        // Coptic
        iso639_2T_ToENLang.put("cop", "Coptic");
        iso639_2T_ToFRLang.put("cop", "copte");
    
        // Cornish
        iso639_2T_ToENLang.put("cor", "Cornish");
        iso639_2T_ToFRLang.put("cor", "cornique");
    
        iso639_1_To_639_2T.put("kw", "cor");
        iso639_2T_To_639_1.put("cor", "kw");
  
        // Corsican
        iso639_2T_ToENLang.put("cos", "Corsican");
        iso639_2T_ToFRLang.put("cos", "corse");
    
        iso639_1_To_639_2T.put("co", "cos");
        iso639_2T_To_639_1.put("cos", "co");
  
        // Creoles and pidgins, English based (Other)
        iso639_2T_ToENLang.put("cpe", "Creoles and pidgins, English based (Other)");
        iso639_2T_ToFRLang.put("cpe", "créoles et pidgins anglais, autres");
    
        // Creoles and pidgins, French-based (Other)
        iso639_2T_ToENLang.put("cpf", "Creoles and pidgins, French-based (Other)");
        iso639_2T_ToFRLang.put("cpf", "créoles et pidgins français, autres");
    
        // Creoles and pidgins, Portuguese-based (Other)
        iso639_2T_ToENLang.put("cpp", "Creoles and pidgins, Portuguese-based (Other)");
        iso639_2T_ToFRLang.put("cpp", "créoles et pidgins portugais, autres");
    
        // Cree
        iso639_2T_ToENLang.put("cre", "Cree");
        iso639_2T_ToFRLang.put("cre", "cree");
    
        // Creoles and pidgins (Other)
        iso639_2T_ToENLang.put("crp", "Creoles and pidgins (Other)");
        iso639_2T_ToFRLang.put("crp", "créoles et pidgins divers");
    
        // Cushitic (Other)
        iso639_2T_ToENLang.put("cus", "Cushitic (Other)");
        iso639_2T_ToFRLang.put("cus", "couchitiques, autres langues");
    
        // Welsh
        iso639_2T_ToENLang.put("cym", "Welsh");
        iso639_2T_ToFRLang.put("cym", "gallois");
    
        iso639_2B_To_639_2T.put("wel", "cym");
  
        iso639_1_To_639_2T.put("cy", "cym");
        iso639_2T_To_639_1.put("cym", "cy");
  
        // Czech
        iso639_2T_ToENLang.put("ces", "Czech");
        iso639_2T_ToFRLang.put("ces", "tchèque");
    
        iso639_2B_To_639_2T.put("cze", "ces");
  
        iso639_1_To_639_2T.put("cs", "ces");
        iso639_2T_To_639_1.put("ces", "cs");
  
        // Dakota
        iso639_2T_ToENLang.put("dak", "Dakota");
        iso639_2T_ToFRLang.put("dak", "dakota");
    
        // Danish
        iso639_2T_ToENLang.put("dan", "Danish");
        iso639_2T_ToFRLang.put("dan", "danois");
    
        iso639_1_To_639_2T.put("da", "dan");
        iso639_2T_To_639_1.put("dan", "da");
  
        // Dayak
        iso639_2T_ToENLang.put("day", "Dayak");
        iso639_2T_ToFRLang.put("day", "dayak");
    
        // Delaware
        iso639_2T_ToENLang.put("del", "Delaware");
        iso639_2T_ToFRLang.put("del", "delaware");
    
        // Slave (Athapascan)
        iso639_2T_ToENLang.put("den", "Slave (Athapascan)");
        iso639_2T_ToFRLang.put("den", "esclave (athapascan)");
    
        // German
        iso639_2T_ToENLang.put("deu", "German");
        iso639_2T_ToFRLang.put("deu", "allemand");
    
        iso639_2B_To_639_2T.put("ger", "deu");
  
        iso639_1_To_639_2T.put("de", "deu");
        iso639_2T_To_639_1.put("deu", "de");
  
        // Dogrib
        iso639_2T_ToENLang.put("dgr", "Dogrib");
        iso639_2T_ToFRLang.put("dgr", "dogrib");
    
        // Dinka
        iso639_2T_ToENLang.put("din", "Dinka");
        iso639_2T_ToFRLang.put("din", "dinka");
    
        // Divehi
        iso639_2T_ToENLang.put("div", "Divehi");
        iso639_2T_ToFRLang.put("div", "maldivien");
    
        // Dogri
        iso639_2T_ToENLang.put("doi", "Dogri");
        iso639_2T_ToFRLang.put("doi", "dogri");
    
        // Dravidian (Other)
        iso639_2T_ToENLang.put("dra", "Dravidian (Other)");
        iso639_2T_ToFRLang.put("dra", "dravidiennes, autres langues");
    
        // Duala
        iso639_2T_ToENLang.put("dua", "Duala");
        iso639_2T_ToFRLang.put("dua", "douala");
    
        // Dutch, Middle (ca.1050-1350)
        iso639_2T_ToENLang.put("dum", "Dutch, Middle (ca.1050-1350)");
        iso639_2T_ToFRLang.put("dum", "néerlandais moyen (ca. 1050-1350)");
    
        // Dutch
        iso639_2T_ToENLang.put("nld", "Dutch");
        iso639_2T_ToFRLang.put("nld", "néerlandais");
    
        iso639_2B_To_639_2T.put("dut", "nld");
  
        iso639_1_To_639_2T.put("nl", "nld");
        iso639_2T_To_639_1.put("nld", "nl");
  
        // Dyula
        iso639_2T_ToENLang.put("dyu", "Dyula");
        iso639_2T_ToFRLang.put("dyu", "dioula");
    
        // Dzongkha
        iso639_2T_ToENLang.put("dzo", "Dzongkha");
        iso639_2T_ToFRLang.put("dzo", "dzongkha");
    
        iso639_1_To_639_2T.put("dz", "dzo");
        iso639_2T_To_639_1.put("dzo", "dz");
  
        // Efik
        iso639_2T_ToENLang.put("efi", "Efik");
        iso639_2T_ToFRLang.put("efi", "efik");
    
        // Egyptian (Ancient)
        iso639_2T_ToENLang.put("egy", "Egyptian (Ancient)");
        iso639_2T_ToFRLang.put("egy", "égyptien");
    
        // Ekajuk
        iso639_2T_ToENLang.put("eka", "Ekajuk");
        iso639_2T_ToFRLang.put("eka", "ekajuk");
    
        // Greek, Modern (1453-)
        iso639_2T_ToENLang.put("ell", "Greek, Modern (1453-)");
        iso639_2T_ToFRLang.put("ell", "grec moderne (après 1453)");
    
        iso639_2B_To_639_2T.put("gre", "ell");
  
        iso639_1_To_639_2T.put("el", "ell");
        iso639_2T_To_639_1.put("ell", "el");
  
        // Elamite
        iso639_2T_ToENLang.put("elx", "Elamite");
        iso639_2T_ToFRLang.put("elx", "élamite");
    
        // English
        iso639_2T_ToENLang.put("eng", "English");
        iso639_2T_ToFRLang.put("eng", "anglais");
    
        iso639_1_To_639_2T.put("en", "eng");
        iso639_2T_To_639_1.put("eng", "en");
  
        // English, Middle (1100-1500)
        iso639_2T_ToENLang.put("enm", "English, Middle (1100-1500)");
        iso639_2T_ToFRLang.put("enm", "anglais moyen (1100-1500)");
    
        // Esperanto
        iso639_2T_ToENLang.put("epo", "Esperanto");
        iso639_2T_ToFRLang.put("epo", "espéranto");
    
        iso639_1_To_639_2T.put("eo", "epo");
        iso639_2T_To_639_1.put("epo", "eo");
  
        // Estonian
        iso639_2T_ToENLang.put("est", "Estonian");
        iso639_2T_ToFRLang.put("est", "estonien");
    
        iso639_1_To_639_2T.put("et", "est");
        iso639_2T_To_639_1.put("est", "et");
  
        // Basque
        iso639_2T_ToENLang.put("eus", "Basque");
        iso639_2T_ToFRLang.put("eus", "basque");
    
        iso639_2B_To_639_2T.put("baq", "eus");
  
        iso639_1_To_639_2T.put("eu", "eus");
        iso639_2T_To_639_1.put("eus", "eu");
  
        // Ewe
        iso639_2T_ToENLang.put("ewe", "Ewe");
        iso639_2T_ToFRLang.put("ewe", "éwé");
    
        // Ewondo
        iso639_2T_ToENLang.put("ewo", "Ewondo");
        iso639_2T_ToFRLang.put("ewo", "éwondo");
    
        // Fang
        iso639_2T_ToENLang.put("fan", "Fang");
        iso639_2T_ToFRLang.put("fan", "fang");
    
        // Faroese
        iso639_2T_ToENLang.put("fao", "Faroese");
        iso639_2T_ToFRLang.put("fao", "féroïen");
    
        iso639_1_To_639_2T.put("fo", "fao");
        iso639_2T_To_639_1.put("fao", "fo");
  
        // Persian
        iso639_2T_ToENLang.put("fas", "Persian");
        iso639_2T_ToFRLang.put("fas", "persan");
    
        iso639_2B_To_639_2T.put("per", "fas");
  
        iso639_1_To_639_2T.put("fa", "fas");
        iso639_2T_To_639_1.put("fas", "fa");
  
        // Fanti
        iso639_2T_ToENLang.put("fat", "Fanti");
        iso639_2T_ToFRLang.put("fat", "fanti");
    
        // Fijian
        iso639_2T_ToENLang.put("fij", "Fijian");
        iso639_2T_ToFRLang.put("fij", "fidjien");
    
        iso639_1_To_639_2T.put("fj", "fij");
        iso639_2T_To_639_1.put("fij", "fj");
  
        // Finnish
        iso639_2T_ToENLang.put("fin", "Finnish");
        iso639_2T_ToFRLang.put("fin", "finnois");
    
        iso639_1_To_639_2T.put("fi", "fin");
        iso639_2T_To_639_1.put("fin", "fi");
  
        // Finno-Ugrian (Other)
        iso639_2T_ToENLang.put("fiu", "Finno-Ugrian (Other)");
        iso639_2T_ToFRLang.put("fiu", "finno-ougriennes, autres langues");
    
        // Fon
        iso639_2T_ToENLang.put("fon", "Fon");
        iso639_2T_ToFRLang.put("fon", "fon");
    
        // French
        iso639_2T_ToENLang.put("fra", "French");
        iso639_2T_ToFRLang.put("fra", "français");
    
        iso639_2B_To_639_2T.put("fre", "fra");
  
        iso639_1_To_639_2T.put("fr", "fra");
        iso639_2T_To_639_1.put("fra", "fr");
  
        // French, Middle (ca.1400-1800)
        iso639_2T_ToENLang.put("frm", "French, Middle (ca.1400-1800)");
        iso639_2T_ToFRLang.put("frm", "français moyen (1400-1800)");
    
        // French, Old (842-ca.1400)
        iso639_2T_ToENLang.put("fro", "French, Old (842-ca.1400)");
        iso639_2T_ToFRLang.put("fro", "français ancien (842-ca.1400)");
    
        // Frisian
        iso639_2T_ToENLang.put("fry", "Frisian");
        iso639_2T_ToFRLang.put("fry", "frison");
    
        iso639_1_To_639_2T.put("fy", "fry");
        iso639_2T_To_639_1.put("fry", "fy");
  
        // Fulah
        iso639_2T_ToENLang.put("ful", "Fulah");
        iso639_2T_ToFRLang.put("ful", "peul");
    
        // Friulian
        iso639_2T_ToENLang.put("fur", "Friulian");
        iso639_2T_ToFRLang.put("fur", "frioulan");
    
        // Ga
        iso639_2T_ToENLang.put("gaa", "Ga");
        iso639_2T_ToFRLang.put("gaa", "ga");
    
        // Gayo
        iso639_2T_ToENLang.put("gay", "Gayo");
        iso639_2T_ToFRLang.put("gay", "gayo");
    
        // Gbaya
        iso639_2T_ToENLang.put("gba", "Gbaya");
        iso639_2T_ToFRLang.put("gba", "gbaya");
    
        // Germanic (Other)
        iso639_2T_ToENLang.put("gem", "Germanic (Other)");
        iso639_2T_ToFRLang.put("gem", "germaniques, autres langues");
    
        // Georgian
        iso639_2T_ToENLang.put("kat", "Georgian");
        iso639_2T_ToFRLang.put("kat", "géorgien");
    
        iso639_2B_To_639_2T.put("geo", "kat");
  
        iso639_1_To_639_2T.put("ka", "kat");
        iso639_2T_To_639_1.put("kat", "ka");
  
        // German
        iso639_2T_ToENLang.put("deu", "German");
        iso639_2T_ToFRLang.put("deu", "allemand");
    
        iso639_2B_To_639_2T.put("ger", "deu");
  
        iso639_1_To_639_2T.put("de", "deu");
        iso639_2T_To_639_1.put("deu", "de");
  
        // Geez
        iso639_2T_ToENLang.put("gez", "Geez");
        iso639_2T_ToFRLang.put("gez", "guèze");
    
        // Gilbertese
        iso639_2T_ToENLang.put("gil", "Gilbertese");
        iso639_2T_ToFRLang.put("gil", "kiribati");
    
        // Gaelic; Scottish Gaelic
        iso639_2T_ToENLang.put("gla", "Gaelic; Scottish Gaelic");
        iso639_2T_ToFRLang.put("gla", "gaélique; gaélique écossais");
    
        iso639_1_To_639_2T.put("gd", "gla");
        iso639_2T_To_639_1.put("gla", "gd");
  
        // Irish
        iso639_2T_ToENLang.put("gle", "Irish");
        iso639_2T_ToFRLang.put("gle", "irlandais");
    
        iso639_1_To_639_2T.put("ga", "gle");
        iso639_2T_To_639_1.put("gle", "ga");
  
        // Gallegan
        iso639_2T_ToENLang.put("glg", "Gallegan");
        iso639_2T_ToFRLang.put("glg", "galicien");
    
        iso639_1_To_639_2T.put("gl", "glg");
        iso639_2T_To_639_1.put("glg", "gl");
  
        // Manx
        iso639_2T_ToENLang.put("glv", "Manx");
        iso639_2T_ToFRLang.put("glv", " manx; mannois");
    
        iso639_1_To_639_2T.put("gv", "glv");
        iso639_2T_To_639_1.put("glv", "gv");
  
        // German, Middle High (ca.1050-1500)
        iso639_2T_ToENLang.put("gmh", "German, Middle High (ca.1050-1500)");
        iso639_2T_ToFRLang.put("gmh", "allemand, moyen haut (ca. 1050-1500)");
    
        // German, Old High (ca.750-1050)
        iso639_2T_ToENLang.put("goh", "German, Old High (ca.750-1050)");
        iso639_2T_ToFRLang.put("goh", "allemand, vieux haut (ca. 750-1050)");
    
        // Gondi
        iso639_2T_ToENLang.put("gon", "Gondi");
        iso639_2T_ToFRLang.put("gon", "gond");
    
        // Gorontalo
        iso639_2T_ToENLang.put("gor", "Gorontalo");
        iso639_2T_ToFRLang.put("gor", "gorontalo");
    
        // Gothic
        iso639_2T_ToENLang.put("got", "Gothic");
        iso639_2T_ToFRLang.put("got", "gothique");
    
        // Grebo
        iso639_2T_ToENLang.put("grb", "Grebo");
        iso639_2T_ToFRLang.put("grb", "grebo");
    
        // Greek, Ancient (to 1453)
        iso639_2T_ToENLang.put("grc", "Greek, Ancient (to 1453)");
        iso639_2T_ToFRLang.put("grc", "grec ancien (jusqu'à 1453)");
    
        // Greek, Modern (1453-)
        iso639_2T_ToENLang.put("ell", "Greek, Modern (1453-)");
        iso639_2T_ToFRLang.put("ell", "grec moderne (après 1453)");
    
        iso639_2B_To_639_2T.put("gre", "ell");
  
        iso639_1_To_639_2T.put("el", "ell");
        iso639_2T_To_639_1.put("ell", "el");
  
        // Guarani
        iso639_2T_ToENLang.put("grn", "Guarani");
        iso639_2T_ToFRLang.put("grn", "guarani");
    
        iso639_1_To_639_2T.put("gn", "grn");
        iso639_2T_To_639_1.put("grn", "gn");
  
        // Gujarati
        iso639_2T_ToENLang.put("guj", "Gujarati");
        iso639_2T_ToFRLang.put("guj", "goudjrati");
    
        iso639_1_To_639_2T.put("gu", "guj");
        iso639_2T_To_639_1.put("guj", "gu");
  
        // Gwich´in
        iso639_2T_ToENLang.put("gwi", "Gwich´in");
        iso639_2T_ToFRLang.put("gwi", "gwich´in");
    
        // Haida
        iso639_2T_ToENLang.put("hai", "Haida");
        iso639_2T_ToFRLang.put("hai", "haida");
    
        // Hausa
        iso639_2T_ToENLang.put("hau", "Hausa");
        iso639_2T_ToFRLang.put("hau", "haoussa");
    
        iso639_1_To_639_2T.put("ha", "hau");
        iso639_2T_To_639_1.put("hau", "ha");
  
        // Hawaiian
        iso639_2T_ToENLang.put("haw", "Hawaiian");
        iso639_2T_ToFRLang.put("haw", "hawaïen");
    
        // Hebrew
        iso639_2T_ToENLang.put("heb", "Hebrew");
        iso639_2T_ToFRLang.put("heb", "hébreu");
    
        iso639_1_To_639_2T.put("he", "heb");
        iso639_2T_To_639_1.put("heb", "he");
  
        // Herero
        iso639_2T_ToENLang.put("her", "Herero");
        iso639_2T_ToFRLang.put("her", "herero");
    
        iso639_1_To_639_2T.put("hz", "her");
        iso639_2T_To_639_1.put("her", "hz");
  
        // Hiligaynon
        iso639_2T_ToENLang.put("hil", "Hiligaynon");
        iso639_2T_ToFRLang.put("hil", "hiligaynon");
    
        // Himachali
        iso639_2T_ToENLang.put("him", "Himachali");
        iso639_2T_ToFRLang.put("him", "himachali");
    
        // Hindi 
        iso639_2T_ToENLang.put("hin", "Hindi ");
        iso639_2T_ToFRLang.put("hin", "hindi");
    
        iso639_1_To_639_2T.put("hi", "hin");
        iso639_2T_To_639_1.put("hin", "hi");
  
        // Hittite
        iso639_2T_ToENLang.put("hit", "Hittite");
        iso639_2T_ToFRLang.put("hit", "hittite");
    
        // Hmong
        iso639_2T_ToENLang.put("hmn", "Hmong");
        iso639_2T_ToFRLang.put("hmn", "hmong");
    
        // Hiri Motu
        iso639_2T_ToENLang.put("hmo", "Hiri Motu");
        iso639_2T_ToFRLang.put("hmo", "hiri motu");
    
        iso639_1_To_639_2T.put("ho", "hmo");
        iso639_2T_To_639_1.put("hmo", "ho");
  
        // Croatian
        iso639_2T_ToENLang.put("hrv", "Croatian");
        iso639_2T_ToFRLang.put("hrv", "croate");
    
        iso639_2B_To_639_2T.put("scr", "hrv");
  
        iso639_1_To_639_2T.put("hr", "hrv");
        iso639_2T_To_639_1.put("hrv", "hr");
  
        // Hungarian
        iso639_2T_ToENLang.put("hun", "Hungarian");
        iso639_2T_ToFRLang.put("hun", "hongrois");
    
        iso639_1_To_639_2T.put("hu", "hun");
        iso639_2T_To_639_1.put("hun", "hu");
  
        // Hupa
        iso639_2T_ToENLang.put("hup", "Hupa");
        iso639_2T_ToFRLang.put("hup", "hupa");
    
        // Armenian
        iso639_2T_ToENLang.put("hye", "Armenian");
        iso639_2T_ToFRLang.put("hye", "arménien");
    
        iso639_2B_To_639_2T.put("arm", "hye");
  
        iso639_1_To_639_2T.put("hy", "hye");
        iso639_2T_To_639_1.put("hye", "hy");
  
        // Iban
        iso639_2T_ToENLang.put("iba", "Iban");
        iso639_2T_ToFRLang.put("iba", "iban");
    
        // Igbo
        iso639_2T_ToENLang.put("ibo", "Igbo");
        iso639_2T_ToFRLang.put("ibo", "igbo");
    
        // Icelandic
        iso639_2T_ToENLang.put("isl", "Icelandic");
        iso639_2T_ToFRLang.put("isl", "islandais");
    
        iso639_2B_To_639_2T.put("ice", "isl");
  
        iso639_1_To_639_2T.put("is", "isl");
        iso639_2T_To_639_1.put("isl", "is");
  
        // Ido
        iso639_2T_ToENLang.put("ido", "Ido");
        iso639_2T_ToFRLang.put("ido", "ido");
    
        iso639_1_To_639_2T.put("io", "ido");
        iso639_2T_To_639_1.put("ido", "io");
  
        // Ijo
        iso639_2T_ToENLang.put("ijo", "Ijo");
        iso639_2T_ToFRLang.put("ijo", "ijo");
    
        // Inuktitut
        iso639_2T_ToENLang.put("iku", "Inuktitut");
        iso639_2T_ToFRLang.put("iku", "inuktitut");
    
        iso639_1_To_639_2T.put("iu", "iku");
        iso639_2T_To_639_1.put("iku", "iu");
  
        // Interlingue
        iso639_2T_ToENLang.put("ile", "Interlingue");
        iso639_2T_ToFRLang.put("ile", "interlingue");
    
        iso639_1_To_639_2T.put("ie", "ile");
        iso639_2T_To_639_1.put("ile", "ie");
  
        // Iloko
        iso639_2T_ToENLang.put("ilo", "Iloko");
        iso639_2T_ToFRLang.put("ilo", "ilocano");
    
        // Interlingua (International Auxiliary Language Association)
        iso639_2T_ToENLang.put("ina", "Interlingua (International Auxiliary Language Association)");
        iso639_2T_ToFRLang.put("ina", "interlingua (langue auxiliaire internationale)");
    
        iso639_1_To_639_2T.put("ia", "ina");
        iso639_2T_To_639_1.put("ina", "ia");
  
        // Indic (Other)
        iso639_2T_ToENLang.put("inc", "Indic (Other)");
        iso639_2T_ToFRLang.put("inc", "indo-aryennes, autres langues");
    
        // Indonesian
        iso639_2T_ToENLang.put("ind", "Indonesian");
        iso639_2T_ToFRLang.put("ind", "indonésien");
    
        iso639_1_To_639_2T.put("id", "ind");
        iso639_2T_To_639_1.put("ind", "id");
  
        // Indo-European (Other)
        iso639_2T_ToENLang.put("ine", "Indo-European (Other)");
        iso639_2T_ToFRLang.put("ine", "indo-européennes, autres langues");
    
        // Inupiaq
        iso639_2T_ToENLang.put("ipk", "Inupiaq");
        iso639_2T_ToFRLang.put("ipk", "inupiaq");
    
        iso639_1_To_639_2T.put("ik", "ipk");
        iso639_2T_To_639_1.put("ipk", "ik");
  
        // Iranian (Other)
        iso639_2T_ToENLang.put("ira", "Iranian (Other)");
        iso639_2T_ToFRLang.put("ira", "iraniennes, autres langues");
    
        // Iroquoian languages
        iso639_2T_ToENLang.put("iro", "Iroquoian languages");
        iso639_2T_ToFRLang.put("iro", "iroquoises, langues (famille)");
    
        // Icelandic
        iso639_2T_ToENLang.put("isl", "Icelandic");
        iso639_2T_ToFRLang.put("isl", "islandais");
    
        iso639_2B_To_639_2T.put("ice", "isl");
  
        iso639_1_To_639_2T.put("is", "isl");
        iso639_2T_To_639_1.put("isl", "is");
  
        // Italian
        iso639_2T_ToENLang.put("ita", "Italian");
        iso639_2T_ToFRLang.put("ita", "italien");
    
        iso639_1_To_639_2T.put("it", "ita");
        iso639_2T_To_639_1.put("ita", "it");
  
        // Javanese
        iso639_2T_ToENLang.put("jav", "Javanese");
        iso639_2T_ToFRLang.put("jav", "javanais");
    
        iso639_1_To_639_2T.put("jv", "jav");
        iso639_2T_To_639_1.put("jav", "jv");
  
        // Japanese
        iso639_2T_ToENLang.put("jpn", "Japanese");
        iso639_2T_ToFRLang.put("jpn", "japonais");
    
        iso639_1_To_639_2T.put("ja", "jpn");
        iso639_2T_To_639_1.put("jpn", "ja");
  
        // Judeo-Persian
        iso639_2T_ToENLang.put("jpr", "Judeo-Persian");
        iso639_2T_ToFRLang.put("jpr", "judéo-persan");
    
        // Judeo-Arabic
        iso639_2T_ToENLang.put("jrb", "Judeo-Arabic");
        iso639_2T_ToFRLang.put("jrb", "judéo-arabe");
    
        // Kara-Kalpak
        iso639_2T_ToENLang.put("kaa", "Kara-Kalpak");
        iso639_2T_ToFRLang.put("kaa", "karakalpak");
    
        // Kabyle
        iso639_2T_ToENLang.put("kab", "Kabyle");
        iso639_2T_ToFRLang.put("kab", "kabyle");
    
        // Kachin
        iso639_2T_ToENLang.put("kac", "Kachin");
        iso639_2T_ToFRLang.put("kac", "kachin");
    
        // Kalaallisut
        iso639_2T_ToENLang.put("kal", "Kalaallisut");
        iso639_2T_ToFRLang.put("kal", "groenlandais");
    
        iso639_1_To_639_2T.put("kl", "kal");
        iso639_2T_To_639_1.put("kal", "kl");
  
        // Kamba
        iso639_2T_ToENLang.put("kam", "Kamba");
        iso639_2T_ToFRLang.put("kam", "kamba");
    
        // Kannada
        iso639_2T_ToENLang.put("kan", "Kannada");
        iso639_2T_ToFRLang.put("kan", "kannada");
    
        iso639_1_To_639_2T.put("kn", "kan");
        iso639_2T_To_639_1.put("kan", "kn");
  
        // Karen
        iso639_2T_ToENLang.put("kar", "Karen");
        iso639_2T_ToFRLang.put("kar", "karen");
    
        // Kashmiri
        iso639_2T_ToENLang.put("kas", "Kashmiri");
        iso639_2T_ToFRLang.put("kas", "kashmiri");
    
        iso639_1_To_639_2T.put("ks", "kas");
        iso639_2T_To_639_1.put("kas", "ks");
  
        // Georgian
        iso639_2T_ToENLang.put("kat", "Georgian");
        iso639_2T_ToFRLang.put("kat", "géorgien");
    
        iso639_2B_To_639_2T.put("geo", "kat");
  
        iso639_1_To_639_2T.put("ka", "kat");
        iso639_2T_To_639_1.put("kat", "ka");
  
        // Kanuri
        iso639_2T_ToENLang.put("kau", "Kanuri");
        iso639_2T_ToFRLang.put("kau", "kanouri");
    
        // Kawi
        iso639_2T_ToENLang.put("kaw", "Kawi");
        iso639_2T_ToFRLang.put("kaw", "kawi");
    
        // Kazakh
        iso639_2T_ToENLang.put("kaz", "Kazakh");
        iso639_2T_ToFRLang.put("kaz", "kazakh");
    
        iso639_1_To_639_2T.put("kk", "kaz");
        iso639_2T_To_639_1.put("kaz", "kk");
  
        // Khasi
        iso639_2T_ToENLang.put("kha", "Khasi");
        iso639_2T_ToFRLang.put("kha", "khasi");
    
        // Khoisan (Other)
        iso639_2T_ToENLang.put("khi", "Khoisan (Other)");
        iso639_2T_ToFRLang.put("khi", "khoisan, autres langues");
    
        // Khmer
        iso639_2T_ToENLang.put("khm", "Khmer");
        iso639_2T_ToFRLang.put("khm", "khmer");
    
        iso639_1_To_639_2T.put("km", "khm");
        iso639_2T_To_639_1.put("khm", "km");
  
        // Khotanese
        iso639_2T_ToENLang.put("kho", "Khotanese");
        iso639_2T_ToFRLang.put("kho", "khotanais");
    
        // Kikuyu; Gikuyu
        iso639_2T_ToENLang.put("kik", "Kikuyu; Gikuyu");
        iso639_2T_ToFRLang.put("kik", "kikuyu");
    
        iso639_1_To_639_2T.put("ki", "kik");
        iso639_2T_To_639_1.put("kik", "ki");
  
        // Kinyarwanda
        iso639_2T_ToENLang.put("kin", "Kinyarwanda");
        iso639_2T_ToFRLang.put("kin", "rwanda");
    
        iso639_1_To_639_2T.put("rw", "kin");
        iso639_2T_To_639_1.put("kin", "rw");
  
        // Kirghiz
        iso639_2T_ToENLang.put("kir", "Kirghiz");
        iso639_2T_ToFRLang.put("kir", "kirghize");
    
        iso639_1_To_639_2T.put("ky", "kir");
        iso639_2T_To_639_1.put("kir", "ky");
  
        // Kimbundu
        iso639_2T_ToENLang.put("kmb", "Kimbundu");
        iso639_2T_ToFRLang.put("kmb", "kimbundu");
    
        // Konkani
        iso639_2T_ToENLang.put("kok", "Konkani");
        iso639_2T_ToFRLang.put("kok", "konkani");
    
        // Komi
        iso639_2T_ToENLang.put("kom", "Komi");
        iso639_2T_ToFRLang.put("kom", "kom");
    
        iso639_1_To_639_2T.put("kv", "kom");
        iso639_2T_To_639_1.put("kom", "kv");
  
        // Kongo
        iso639_2T_ToENLang.put("kon", "Kongo");
        iso639_2T_ToFRLang.put("kon", "kongo");
    
        // Korean
        iso639_2T_ToENLang.put("kor", "Korean");
        iso639_2T_ToFRLang.put("kor", "coréen");
    
        iso639_1_To_639_2T.put("ko", "kor");
        iso639_2T_To_639_1.put("kor", "ko");
  
        // Kosraean
        iso639_2T_ToENLang.put("kos", "Kosraean");
        iso639_2T_ToFRLang.put("kos", "kosrae");
    
        // Kpelle
        iso639_2T_ToENLang.put("kpe", "Kpelle");
        iso639_2T_ToFRLang.put("kpe", "kpellé");
    
        // Kru
        iso639_2T_ToENLang.put("kro", "Kru");
        iso639_2T_ToFRLang.put("kro", "krou");
    
        // Kurukh
        iso639_2T_ToENLang.put("kru", "Kurukh");
        iso639_2T_ToFRLang.put("kru", "kurukh");
    
        // Kuanyama; Kwanyama
        iso639_2T_ToENLang.put("kua", "Kuanyama; Kwanyama");
        iso639_2T_ToFRLang.put("kua", "kuanyama; kwanyama");
    
        iso639_1_To_639_2T.put("kj", "kua");
        iso639_2T_To_639_1.put("kua", "kj");
  
        // Kumyk
        iso639_2T_ToENLang.put("kum", "Kumyk");
        iso639_2T_ToFRLang.put("kum", "koumyk");
    
        // Kurdish
        iso639_2T_ToENLang.put("kur", "Kurdish");
        iso639_2T_ToFRLang.put("kur", "kurde");
    
        iso639_1_To_639_2T.put("ku", "kur");
        iso639_2T_To_639_1.put("kur", "ku");
  
        // Kutenai
        iso639_2T_ToENLang.put("kut", "Kutenai");
        iso639_2T_ToFRLang.put("kut", "kutenai");
    
        // Ladino
        iso639_2T_ToENLang.put("lad", "Ladino");
        iso639_2T_ToFRLang.put("lad", "judéo-espagnol");
    
        // Lahnda
        iso639_2T_ToENLang.put("lah", "Lahnda");
        iso639_2T_ToFRLang.put("lah", "lahnda");
    
        // Lamba
        iso639_2T_ToENLang.put("lam", "Lamba");
        iso639_2T_ToFRLang.put("lam", "lamba");
    
        // Lao
        iso639_2T_ToENLang.put("lao", "Lao");
        iso639_2T_ToFRLang.put("lao", "lao");
    
        iso639_1_To_639_2T.put("lo", "lao");
        iso639_2T_To_639_1.put("lao", "lo");
  
        // Latin
        iso639_2T_ToENLang.put("lat", "Latin");
        iso639_2T_ToFRLang.put("lat", "latin");
    
        iso639_1_To_639_2T.put("la", "lat");
        iso639_2T_To_639_1.put("lat", "la");
  
        // Latvian
        iso639_2T_ToENLang.put("lav", "Latvian");
        iso639_2T_ToFRLang.put("lav", "letton");
    
        iso639_1_To_639_2T.put("lv", "lav");
        iso639_2T_To_639_1.put("lav", "lv");
  
        // Lezghian
        iso639_2T_ToENLang.put("lez", "Lezghian");
        iso639_2T_ToFRLang.put("lez", "lezghien");
    
        // Limburgan; Limburger; Limburgish
        iso639_2T_ToENLang.put("lim", "Limburgan; Limburger; Limburgish");
        iso639_2T_ToFRLang.put("lim", "limbourgeois");
    
        iso639_1_To_639_2T.put("li", "lim");
        iso639_2T_To_639_1.put("lim", "li");
  
        // Lingala
        iso639_2T_ToENLang.put("lin", "Lingala");
        iso639_2T_ToFRLang.put("lin", "lingala");
    
        iso639_1_To_639_2T.put("ln", "lin");
        iso639_2T_To_639_1.put("lin", "ln");
  
        // Lithuanian
        iso639_2T_ToENLang.put("lit", "Lithuanian");
        iso639_2T_ToFRLang.put("lit", "lituanien");
    
        iso639_1_To_639_2T.put("lt", "lit");
        iso639_2T_To_639_1.put("lit", "lt");
  
        // Mongo
        iso639_2T_ToENLang.put("lol", "Mongo");
        iso639_2T_ToFRLang.put("lol", "mongo");
    
        // Lozi
        iso639_2T_ToENLang.put("loz", "Lozi");
        iso639_2T_ToFRLang.put("loz", "lozi");
    
        // Luxembourgish; Letzeburgesch
        iso639_2T_ToENLang.put("ltz", "Luxembourgish; Letzeburgesch");
        iso639_2T_ToFRLang.put("ltz", "luxembourgeois");
    
        iso639_1_To_639_2T.put("lb", "ltz");
        iso639_2T_To_639_1.put("ltz", "lb");
  
        // Luba-Lulua
        iso639_2T_ToENLang.put("lua", "Luba-Lulua");
        iso639_2T_ToFRLang.put("lua", "luba-lulua");
    
        // Luba-Katanga
        iso639_2T_ToENLang.put("lub", "Luba-Katanga");
        iso639_2T_ToFRLang.put("lub", "luba-katanga");
    
        // Ganda
        iso639_2T_ToENLang.put("lug", "Ganda");
        iso639_2T_ToFRLang.put("lug", "ganda");
    
        // Luiseno
        iso639_2T_ToENLang.put("lui", "Luiseno");
        iso639_2T_ToFRLang.put("lui", "luiseno");
    
        // Lunda
        iso639_2T_ToENLang.put("lun", "Lunda");
        iso639_2T_ToFRLang.put("lun", "lunda");
    
        // Luo (Kenya and Tanzania)
        iso639_2T_ToENLang.put("luo", "Luo (Kenya and Tanzania)");
        iso639_2T_ToFRLang.put("luo", "luo (Kenya et Tanzanie)");
    
        // lushai
        iso639_2T_ToENLang.put("lus", "lushai");
        iso639_2T_ToFRLang.put("lus", "Lushai");
    
        // Macedonian
        iso639_2T_ToENLang.put("mkd", "Macedonian");
        iso639_2T_ToFRLang.put("mkd", "macédonien");
    
        iso639_2B_To_639_2T.put("mac", "mkd");
  
        iso639_1_To_639_2T.put("mk", "mkd");
        iso639_2T_To_639_1.put("mkd", "mk");
  
        // Madurese
        iso639_2T_ToENLang.put("mad", "Madurese");
        iso639_2T_ToFRLang.put("mad", "madourais");
    
        // Magahi
        iso639_2T_ToENLang.put("mag", "Magahi");
        iso639_2T_ToFRLang.put("mag", "magahi");
    
        // Marshallese
        iso639_2T_ToENLang.put("mah", "Marshallese");
        iso639_2T_ToFRLang.put("mah", "marshall");
    
        iso639_1_To_639_2T.put("mh", "mah");
        iso639_2T_To_639_1.put("mah", "mh");
  
        // Maithili
        iso639_2T_ToENLang.put("mai", "Maithili");
        iso639_2T_ToFRLang.put("mai", "maithili");
    
        // Makasar
        iso639_2T_ToENLang.put("mak", "Makasar");
        iso639_2T_ToFRLang.put("mak", "makassar");
    
        // Malayalam
        iso639_2T_ToENLang.put("mal", "Malayalam");
        iso639_2T_ToFRLang.put("mal", "malayalam");
    
        iso639_1_To_639_2T.put("ml", "mal");
        iso639_2T_To_639_1.put("mal", "ml");
  
        // Mandingo
        iso639_2T_ToENLang.put("man", "Mandingo");
        iso639_2T_ToFRLang.put("man", "mandingue");
    
        // Maori
        iso639_2T_ToENLang.put("mri", "Maori");
        iso639_2T_ToFRLang.put("mri", "maori");
    
        iso639_2B_To_639_2T.put("mao", "mri");
  
        iso639_1_To_639_2T.put("mi", "mri");
        iso639_2T_To_639_1.put("mri", "mi");
  
        // Austronesian (Other)
        iso639_2T_ToENLang.put("map", "Austronesian (Other)");
        iso639_2T_ToFRLang.put("map", "malayo-polynésiennes,autres langues");
    
        // Marathi
        iso639_2T_ToENLang.put("mar", "Marathi");
        iso639_2T_ToFRLang.put("mar", "marathe");
    
        iso639_1_To_639_2T.put("mr", "mar");
        iso639_2T_To_639_1.put("mar", "mr");
  
        // Masai
        iso639_2T_ToENLang.put("mas", "Masai");
        iso639_2T_ToFRLang.put("mas", "massaï");
    
        // Malay
        iso639_2T_ToENLang.put("msa", "Malay");
        iso639_2T_ToFRLang.put("msa", "malais");
    
        iso639_2B_To_639_2T.put("may", "msa");
  
        iso639_1_To_639_2T.put("ms", "msa");
        iso639_2T_To_639_1.put("msa", "ms");
  
        // Mandar
        iso639_2T_ToENLang.put("mdr", "Mandar");
        iso639_2T_ToFRLang.put("mdr", "mandar");
    
        // Mende
        iso639_2T_ToENLang.put("men", "Mende");
        iso639_2T_ToFRLang.put("men", "mendé");
    
        // Irish, Middle (900-1200)
        iso639_2T_ToENLang.put("mga", "Irish, Middle (900-1200)");
        iso639_2T_ToFRLang.put("mga", "irlandais moyen (900-1200)");
    
        // Micmac
        iso639_2T_ToENLang.put("mic", "Micmac");
        iso639_2T_ToFRLang.put("mic", "micmac");
    
        // Minangkabau
        iso639_2T_ToENLang.put("min", "Minangkabau");
        iso639_2T_ToFRLang.put("min", "minangkabau");
    
        // Miscellaneous languages
        iso639_2T_ToENLang.put("mis", "Miscellaneous languages");
        iso639_2T_ToFRLang.put("mis", "diverses, langues");
    
        // Macedonian
        iso639_2T_ToENLang.put("mkd", "Macedonian");
        iso639_2T_ToFRLang.put("mkd", "macédonien");
    
        iso639_2B_To_639_2T.put("mac", "mkd");
  
        iso639_1_To_639_2T.put("mk", "mkd");
        iso639_2T_To_639_1.put("mkd", "mk");
  
        // Mon-Khmer (Other)
        iso639_2T_ToENLang.put("mkh", "Mon-Khmer (Other)");
        iso639_2T_ToFRLang.put("mkh", "môn-khmer, autres langues");
    
        // Malagasy
        iso639_2T_ToENLang.put("mlg", "Malagasy");
        iso639_2T_ToFRLang.put("mlg", "malgache");
    
        iso639_1_To_639_2T.put("mg", "mlg");
        iso639_2T_To_639_1.put("mlg", "mg");
  
        // Maltese
        iso639_2T_ToENLang.put("mlt", "Maltese");
        iso639_2T_ToFRLang.put("mlt", "maltais");
    
        iso639_1_To_639_2T.put("mt", "mlt");
        iso639_2T_To_639_1.put("mlt", "mt");
  
        // Manchu
        iso639_2T_ToENLang.put("mnc", "Manchu");
        iso639_2T_ToFRLang.put("mnc", "mandchou");
    
        // Manipuri
        iso639_2T_ToENLang.put("mni", "Manipuri");
        iso639_2T_ToFRLang.put("mni", "manipuri");
    
        // Manobo languages 
        iso639_2T_ToENLang.put("mno", "Manobo languages ");
        iso639_2T_ToFRLang.put("mno", "manobo, langues");
    
        // Mohawk
        iso639_2T_ToENLang.put("moh", "Mohawk");
        iso639_2T_ToFRLang.put("moh", "mohawk");
    
        // Moldavian
        iso639_2T_ToENLang.put("mol", "Moldavian");
        iso639_2T_ToFRLang.put("mol", "moldave");
    
        iso639_1_To_639_2T.put("mo", "mol");
        iso639_2T_To_639_1.put("mol", "mo");
  
        // Mongolian
        iso639_2T_ToENLang.put("mon", "Mongolian");
        iso639_2T_ToFRLang.put("mon", "mongol");
    
        iso639_1_To_639_2T.put("mn", "mon");
        iso639_2T_To_639_1.put("mon", "mn");
  
        // Mossi
        iso639_2T_ToENLang.put("mos", "Mossi");
        iso639_2T_ToFRLang.put("mos", "moré");
    
        // Maori
        iso639_2T_ToENLang.put("mri", "Maori");
        iso639_2T_ToFRLang.put("mri", "maori");
    
        iso639_2B_To_639_2T.put("mao", "mri");
  
        iso639_1_To_639_2T.put("mi", "mri");
        iso639_2T_To_639_1.put("mri", "mi");
  
        // Malay
        iso639_2T_ToENLang.put("msa", "Malay");
        iso639_2T_ToFRLang.put("msa", "malais");
    
        iso639_2B_To_639_2T.put("may", "msa");
  
        iso639_1_To_639_2T.put("ms", "msa");
        iso639_2T_To_639_1.put("msa", "ms");
  
        // Multiple languages
        iso639_2T_ToENLang.put("mul", "Multiple languages");
        iso639_2T_ToFRLang.put("mul", "multilingue");
    
        // Munda languages
        iso639_2T_ToENLang.put("mun", "Munda languages");
        iso639_2T_ToFRLang.put("mun", "mounda, langues");
    
        // Creek
        iso639_2T_ToENLang.put("mus", "Creek");
        iso639_2T_ToFRLang.put("mus", "muskogee");
    
        // Marwari
        iso639_2T_ToENLang.put("mwr", "Marwari");
        iso639_2T_ToFRLang.put("mwr", "marvari");
    
        // Burmese
        iso639_2T_ToENLang.put("mya", "Burmese");
        iso639_2T_ToFRLang.put("mya", "birman");
    
        iso639_2B_To_639_2T.put("bur", "mya");
  
        iso639_1_To_639_2T.put("my", "mya");
        iso639_2T_To_639_1.put("mya", "my");
  
        // Mayan languages
        iso639_2T_ToENLang.put("myn", "Mayan languages");
        iso639_2T_ToFRLang.put("myn", "maya, langues");
    
        // Nahuatl
        iso639_2T_ToENLang.put("nah", "Nahuatl");
        iso639_2T_ToFRLang.put("nah", "nahuatl");
    
        // North American Indian
        iso639_2T_ToENLang.put("nai", "North American Indian");
        iso639_2T_ToFRLang.put("nai", "indiennes d'Amérique du Nord, autres langues");
    
        // Neapolitan
        iso639_2T_ToENLang.put("nap", "Neapolitan");
        iso639_2T_ToFRLang.put("nap", "napolitain");
    
        // Nauru
        iso639_2T_ToENLang.put("nau", "Nauru");
        iso639_2T_ToFRLang.put("nau", "nauruan");
    
        iso639_1_To_639_2T.put("na", "nau");
        iso639_2T_To_639_1.put("nau", "na");
  
        // Navajo; Navaho
        iso639_2T_ToENLang.put("nav", "Navajo; Navaho");
        iso639_2T_ToFRLang.put("nav", "navaho");
    
        iso639_1_To_639_2T.put("nv", "nav");
        iso639_2T_To_639_1.put("nav", "nv");
  
        // Ndebele, South; South Ndebele
        iso639_2T_ToENLang.put("nbl", "Ndebele, South; South Ndebele");
        iso639_2T_ToFRLang.put("nbl", "ndébélé du Sud");
    
        iso639_1_To_639_2T.put("nr", "nbl");
        iso639_2T_To_639_1.put("nbl", "nr");
  
        // Ndebele, North; North Ndebele
        iso639_2T_ToENLang.put("nde", "Ndebele, North; North Ndebele");
        iso639_2T_ToFRLang.put("nde", "ndébélé du Nord");
    
        iso639_1_To_639_2T.put("nd", "nde");
        iso639_2T_To_639_1.put("nde", "nd");
  
        // Ndonga
        iso639_2T_ToENLang.put("ndo", "Ndonga");
        iso639_2T_ToFRLang.put("ndo", "ndonga");
    
        iso639_1_To_639_2T.put("ng", "ndo");
        iso639_2T_To_639_1.put("ndo", "ng");
  
        // Low German; Low Saxon; German, Low; Saxon, Low
        iso639_2T_ToENLang.put("nds", "Low German; Low Saxon; German, Low; Saxon, Low");
        iso639_2T_ToFRLang.put("nds", "bas allemand; bas saxon; allemand, bas; saxon, bas");
    
        // Nepali
        iso639_2T_ToENLang.put("nep", "Nepali");
        iso639_2T_ToFRLang.put("nep", "népalais");
    
        iso639_1_To_639_2T.put("ne", "nep");
        iso639_2T_To_639_1.put("nep", "ne");
  
        // Newari
        iso639_2T_ToENLang.put("new", "Newari");
        iso639_2T_ToFRLang.put("new", "newari");
    
        // Nias
        iso639_2T_ToENLang.put("nia", "Nias");
        iso639_2T_ToFRLang.put("nia", "nias");
    
        // Niger-Kordofanian (Other)
        iso639_2T_ToENLang.put("nic", "Niger-Kordofanian (Other)");
        iso639_2T_ToFRLang.put("nic", "nigéro-congolaises, autres langues");
    
        // Niuean
        iso639_2T_ToENLang.put("niu", "Niuean");
        iso639_2T_ToFRLang.put("niu", "niué");
    
        // Dutch
        iso639_2T_ToENLang.put("nld", "Dutch");
        iso639_2T_ToFRLang.put("nld", "néerlandais");
    
        iso639_2B_To_639_2T.put("dut", "nld");
  
        iso639_1_To_639_2T.put("nl", "nld");
        iso639_2T_To_639_1.put("nld", "nl");
  
        // Norse, Old
        iso639_2T_ToENLang.put("non", "Norse, Old");
        iso639_2T_ToFRLang.put("non", "norrois, vieux");
    
        // Norwegian
        iso639_2T_ToENLang.put("nor", "Norwegian");
        iso639_2T_ToFRLang.put("nor", "norvégien");
    
        iso639_1_To_639_2T.put("no", "nor");
        iso639_2T_To_639_1.put("nor", "no");
  
        // Norwegian Nynorsk; Nynorsk, Norwegian
        iso639_2T_ToENLang.put("nno", "Norwegian Nynorsk; Nynorsk, Norwegian");
        iso639_2T_ToFRLang.put("nno", "norvégien nynorsk; nynorsk, norvégien ");
    
        iso639_1_To_639_2T.put("nn", "nno");
        iso639_2T_To_639_1.put("nno", "nn");
  
        // Norwegian Bokmål; Bokmål, Norwegian
        iso639_2T_ToENLang.put("nob", "Norwegian Bokmål; Bokmål, Norwegian");
        iso639_2T_ToFRLang.put("nob", "norvégien bokmål; bokmål, norvégien");
    
        iso639_1_To_639_2T.put("nb", "nob");
        iso639_2T_To_639_1.put("nob", "nb");
  
        // Sotho, Northern
        iso639_2T_ToENLang.put("nso", "Sotho, Northern");
        iso639_2T_ToFRLang.put("nso", "sotho du Nord");
    
        // Nubian languages
        iso639_2T_ToENLang.put("nub", "Nubian languages");
        iso639_2T_ToFRLang.put("nub", "nubiennes, langues");
    
        //  Chichewa; Chewa; Nyanja
        iso639_2T_ToENLang.put("nya", " Chichewa; Chewa; Nyanja");
        iso639_2T_ToFRLang.put("nya", "chichewa; chewa; nyanja");
    
        iso639_1_To_639_2T.put("ny", "nya");
        iso639_2T_To_639_1.put("nya", "ny");
  
        // Nyamwezi
        iso639_2T_ToENLang.put("nym", "Nyamwezi");
        iso639_2T_ToFRLang.put("nym", "nyamwezi");
    
        // Nyankole
        iso639_2T_ToENLang.put("nyn", "Nyankole");
        iso639_2T_ToFRLang.put("nyn", "nyankolé");
    
        // Nyoro
        iso639_2T_ToENLang.put("nyo", "Nyoro");
        iso639_2T_ToFRLang.put("nyo", "nyoro");
    
        // Nzima
        iso639_2T_ToENLang.put("nzi", "Nzima");
        iso639_2T_ToFRLang.put("nzi", "nzema");
    
        // Occitan (post 1500); Provençal
        iso639_2T_ToENLang.put("oci", "Occitan (post 1500); Provençal");
        iso639_2T_ToFRLang.put("oci", "occitan (après 1500); provençal");
    
        iso639_1_To_639_2T.put("oc", "oci");
        iso639_2T_To_639_1.put("oci", "oc");
  
        // Ojibwa
        iso639_2T_ToENLang.put("oji", "Ojibwa");
        iso639_2T_ToFRLang.put("oji", "ojibwa");
    
        // Oriya
        iso639_2T_ToENLang.put("ori", "Oriya");
        iso639_2T_ToFRLang.put("ori", "oriya");
    
        iso639_1_To_639_2T.put("or", "ori");
        iso639_2T_To_639_1.put("ori", "or");
  
        // Oromo
        iso639_2T_ToENLang.put("orm", "Oromo");
        iso639_2T_ToFRLang.put("orm", "galla");
    
        iso639_1_To_639_2T.put("om", "orm");
        iso639_2T_To_639_1.put("orm", "om");
  
        // Osage
        iso639_2T_ToENLang.put("osa", "Osage");
        iso639_2T_ToFRLang.put("osa", "osage");
    
        // Ossetian; Ossetic
        iso639_2T_ToENLang.put("oss", "Ossetian; Ossetic");
        iso639_2T_ToFRLang.put("oss", "ossète");
    
        iso639_1_To_639_2T.put("os", "oss");
        iso639_2T_To_639_1.put("oss", "os");
  
        // Turkish, Ottoman (1500-1928)
        iso639_2T_ToENLang.put("ota", "Turkish, Ottoman (1500-1928)");
        iso639_2T_ToFRLang.put("ota", "turc ottoman (1500-1928)");
    
        // Otomian languages
        iso639_2T_ToENLang.put("oto", "Otomian languages");
        iso639_2T_ToFRLang.put("oto", "otomangue, langues");
    
        // Papuan (Other)
        iso639_2T_ToENLang.put("paa", "Papuan (Other)");
        iso639_2T_ToFRLang.put("paa", "papoues, autres langues");
    
        // Pangasinan
        iso639_2T_ToENLang.put("pag", "Pangasinan");
        iso639_2T_ToFRLang.put("pag", "pangasinan");
    
        // Pahlavi
        iso639_2T_ToENLang.put("pal", "Pahlavi");
        iso639_2T_ToFRLang.put("pal", "pahlavi");
    
        // Pampanga
        iso639_2T_ToENLang.put("pam", "Pampanga");
        iso639_2T_ToFRLang.put("pam", "pampangan");
    
        // Panjabi
        iso639_2T_ToENLang.put("pan", "Panjabi");
        iso639_2T_ToFRLang.put("pan", "pendjabi");
    
        iso639_1_To_639_2T.put("pa", "pan");
        iso639_2T_To_639_1.put("pan", "pa");
  
        // Papiamento
        iso639_2T_ToENLang.put("pap", "Papiamento");
        iso639_2T_ToFRLang.put("pap", "papiamento");
    
        // Palauan
        iso639_2T_ToENLang.put("pau", "Palauan");
        iso639_2T_ToFRLang.put("pau", "palau");
    
        // Persian, Old (ca.600-400 B.C.)
        iso639_2T_ToENLang.put("peo", "Persian, Old (ca.600-400 B.C.)");
        iso639_2T_ToFRLang.put("peo", "perse, vieux (ca. 600-400 av. J.-C.)");
    
        // Persian
        iso639_2T_ToENLang.put("fas", "Persian");
        iso639_2T_ToFRLang.put("fas", "persan");
    
        iso639_2B_To_639_2T.put("per", "fas");
  
        iso639_1_To_639_2T.put("fa", "fas");
        iso639_2T_To_639_1.put("fas", "fa");
  
        // Philippine (Other)
        iso639_2T_ToENLang.put("phi", "Philippine (Other)");
        iso639_2T_ToFRLang.put("phi", "philippines, autres langues");
    
        // Phoenician
        iso639_2T_ToENLang.put("phn", "Phoenician");
        iso639_2T_ToFRLang.put("phn", "phénicien");
    
        // Pali
        iso639_2T_ToENLang.put("pli", "Pali");
        iso639_2T_ToFRLang.put("pli", "pali");
    
        iso639_1_To_639_2T.put("pi", "pli");
        iso639_2T_To_639_1.put("pli", "pi");
  
        // Polish
        iso639_2T_ToENLang.put("pol", "Polish");
        iso639_2T_ToFRLang.put("pol", "polonais");
    
        iso639_1_To_639_2T.put("pl", "pol");
        iso639_2T_To_639_1.put("pol", "pl");
  
        // Pohnpeian
        iso639_2T_ToENLang.put("pon", "Pohnpeian");
        iso639_2T_ToFRLang.put("pon", "pohnpei");
    
        // Portuguese
        iso639_2T_ToENLang.put("por", "Portuguese");
        iso639_2T_ToFRLang.put("por", "portugais");
    
        iso639_1_To_639_2T.put("pt", "por");
        iso639_2T_To_639_1.put("por", "pt");
  
        // Prakrit languages
        iso639_2T_ToENLang.put("pra", "Prakrit languages");
        iso639_2T_ToFRLang.put("pra", "prâkrit");
    
        // Provençal, Old (to 1500)
        iso639_2T_ToENLang.put("pro", "Provençal, Old (to 1500)");
        iso639_2T_ToFRLang.put("pro", "provençal ancien (jusqu'à 1500)");
    
        // Pushto
        iso639_2T_ToENLang.put("pus", "Pushto");
        iso639_2T_ToFRLang.put("pus", "pachto");
    
        iso639_1_To_639_2T.put("ps", "pus");
        iso639_2T_To_639_1.put("pus", "ps");
  
        // Quechua
        iso639_2T_ToENLang.put("que", "Quechua");
        iso639_2T_ToFRLang.put("que", "quechua");
    
        iso639_1_To_639_2T.put("qu", "que");
        iso639_2T_To_639_1.put("que", "qu");
  
        // Rajasthani
        iso639_2T_ToENLang.put("raj", "Rajasthani");
        iso639_2T_ToFRLang.put("raj", "rajasthani");
    
        // Rapanui
        iso639_2T_ToENLang.put("rap", "Rapanui");
        iso639_2T_ToFRLang.put("rap", "rapanui");
    
        // Rarotongan
        iso639_2T_ToENLang.put("rar", "Rarotongan");
        iso639_2T_ToFRLang.put("rar", "rarotonga");
    
        // Romance (Other)
        iso639_2T_ToENLang.put("roa", "Romance (Other)");
        iso639_2T_ToFRLang.put("roa", "romanes, autres langues");
    
        // Raeto-Romance
        iso639_2T_ToENLang.put("roh", "Raeto-Romance");
        iso639_2T_ToFRLang.put("roh", "rhéto-roman");
    
        iso639_1_To_639_2T.put("rm", "roh");
        iso639_2T_To_639_1.put("roh", "rm");
  
        // Romany
        iso639_2T_ToENLang.put("rom", "Romany");
        iso639_2T_ToFRLang.put("rom", "tsigane");
    
        // Romanian
        iso639_2T_ToENLang.put("ron", "Romanian");
        iso639_2T_ToFRLang.put("ron", "roumain");
    
        iso639_2B_To_639_2T.put("rum", "ron");
  
        iso639_1_To_639_2T.put("ro", "ron");
        iso639_2T_To_639_1.put("ron", "ro");
  
        // Rundi
        iso639_2T_ToENLang.put("run", "Rundi");
        iso639_2T_ToFRLang.put("run", "rundi");
    
        iso639_1_To_639_2T.put("rn", "run");
        iso639_2T_To_639_1.put("run", "rn");
  
        // Russian
        iso639_2T_ToENLang.put("rus", "Russian");
        iso639_2T_ToFRLang.put("rus", "russe");
    
        iso639_1_To_639_2T.put("ru", "rus");
        iso639_2T_To_639_1.put("rus", "ru");
  
        // Sandawe
        iso639_2T_ToENLang.put("sad", "Sandawe");
        iso639_2T_ToFRLang.put("sad", "sandawe");
    
        // Sango
        iso639_2T_ToENLang.put("sag", "Sango");
        iso639_2T_ToFRLang.put("sag", "sango");
    
        iso639_1_To_639_2T.put("sg", "sag");
        iso639_2T_To_639_1.put("sag", "sg");
  
        // Yakut
        iso639_2T_ToENLang.put("sah", "Yakut");
        iso639_2T_ToFRLang.put("sah", "iakoute");
    
        // South American Indian (Other)
        iso639_2T_ToENLang.put("sai", "South American Indian (Other)");
        iso639_2T_ToFRLang.put("sai", "indiennes d'Amérique du Sud,autres langues");
    
        // Salishan languages 
        iso639_2T_ToENLang.put("sal", "Salishan languages ");
        iso639_2T_ToFRLang.put("sal", "salish, langues");
    
        // Samaritan Aramaic
        iso639_2T_ToENLang.put("sam", "Samaritan Aramaic");
        iso639_2T_ToFRLang.put("sam", "samaritain");
    
        // Sanskrit
        iso639_2T_ToENLang.put("san", "Sanskrit");
        iso639_2T_ToFRLang.put("san", "sanskrit");
    
        iso639_1_To_639_2T.put("sa", "san");
        iso639_2T_To_639_1.put("san", "sa");
  
        // Sasak
        iso639_2T_ToENLang.put("sas", "Sasak");
        iso639_2T_ToFRLang.put("sas", "sasak");
    
        // Santali
        iso639_2T_ToENLang.put("sat", "Santali");
        iso639_2T_ToFRLang.put("sat", "santal");
    
        // Serbian
        iso639_2T_ToENLang.put("srp", "Serbian");
        iso639_2T_ToFRLang.put("srp", "serbe");
    
        iso639_2B_To_639_2T.put("scc", "srp");
  
        iso639_1_To_639_2T.put("sr", "srp");
        iso639_2T_To_639_1.put("srp", "sr");
  
        // Scots
        iso639_2T_ToENLang.put("sco", "Scots");
        iso639_2T_ToFRLang.put("sco", "écossais");
    
        // Croatian
        iso639_2T_ToENLang.put("hrv", "Croatian");
        iso639_2T_ToFRLang.put("hrv", "croate");
    
        iso639_2B_To_639_2T.put("scr", "hrv");
  
        iso639_1_To_639_2T.put("hr", "hrv");
        iso639_2T_To_639_1.put("hrv", "hr");
  
        // Selkup
        iso639_2T_ToENLang.put("sel", "Selkup");
        iso639_2T_ToFRLang.put("sel", "selkoupe");
    
        // Semitic (Other) 
        iso639_2T_ToENLang.put("sem", "Semitic (Other) ");
        iso639_2T_ToFRLang.put("sem", "sémitiques, autres langues");
    
        // Irish, Old (to 900) 
        iso639_2T_ToENLang.put("sga", "Irish, Old (to 900) ");
        iso639_2T_ToFRLang.put("sga", "irlandais ancien (jusqu'à 900)");
    
        // Sign Languages
        iso639_2T_ToENLang.put("sgn", "Sign Languages");
        iso639_2T_ToFRLang.put("sgn", "langues des signes");
    
        // Shan
        iso639_2T_ToENLang.put("shn", "Shan");
        iso639_2T_ToFRLang.put("shn", "chan");
    
        // Sidamo
        iso639_2T_ToENLang.put("sid", "Sidamo");
        iso639_2T_ToFRLang.put("sid", "sidamo");
    
        // Sinhalese
        iso639_2T_ToENLang.put("sin", "Sinhalese");
        iso639_2T_ToFRLang.put("sin", "singhalais");
    
        iso639_1_To_639_2T.put("si", "sin");
        iso639_2T_To_639_1.put("sin", "si");
  
        // Siouan languages 
        iso639_2T_ToENLang.put("sio", "Siouan languages ");
        iso639_2T_ToFRLang.put("sio", "sioux, langues");
    
        // Sino-Tibetan (Other)
        iso639_2T_ToENLang.put("sit", "Sino-Tibetan (Other)");
        iso639_2T_ToFRLang.put("sit", "sino-tibétaines, autres langues");
    
        // Slavic (Other)
        iso639_2T_ToENLang.put("sla", "Slavic (Other)");
        iso639_2T_ToFRLang.put("sla", "slaves, autres langues");
    
        // Slovak
        iso639_2T_ToENLang.put("slk", "Slovak");
        iso639_2T_ToFRLang.put("slk", "slovaque");
    
        iso639_2B_To_639_2T.put("slo", "slk");
  
        iso639_1_To_639_2T.put("sk", "slk");
        iso639_2T_To_639_1.put("slk", "sk");
  
        // Slovenian
        iso639_2T_ToENLang.put("slv", "Slovenian");
        iso639_2T_ToFRLang.put("slv", "slovène");
    
        iso639_1_To_639_2T.put("sl", "slv");
        iso639_2T_To_639_1.put("slv", "sl");
  
        // Southern Sami
        iso639_2T_ToENLang.put("sma", "Southern Sami");
        iso639_2T_ToFRLang.put("sma", "sami du Sud");
    
        // Northern Sami
        iso639_2T_ToENLang.put("sme", "Northern Sami");
        iso639_2T_ToFRLang.put("sme", "sami du Nord");
    
        iso639_1_To_639_2T.put("se", "sme");
        iso639_2T_To_639_1.put("sme", "se");
  
        // Sami languages (Other) 
        iso639_2T_ToENLang.put("smi", "Sami languages (Other) ");
        iso639_2T_ToFRLang.put("smi", "sami, autres langues");
    
        // Lule Sami
        iso639_2T_ToENLang.put("smj", "Lule Sami");
        iso639_2T_ToFRLang.put("smj", "sami de Lule");
    
        // Inari Sami
        iso639_2T_ToENLang.put("smn", "Inari Sami");
        iso639_2T_ToFRLang.put("smn", "sami d'Inari");
    
        // Samoan
        iso639_2T_ToENLang.put("smo", "Samoan");
        iso639_2T_ToFRLang.put("smo", "samoan");
    
        iso639_1_To_639_2T.put("sm", "smo");
        iso639_2T_To_639_1.put("smo", "sm");
  
        // Skolt Sami
        iso639_2T_ToENLang.put("sms", "Skolt Sami");
        iso639_2T_ToFRLang.put("sms", "sami skolt");
    
        // Shona
        iso639_2T_ToENLang.put("sna", "Shona");
        iso639_2T_ToFRLang.put("sna", "shona");
    
        iso639_1_To_639_2T.put("sn", "sna");
        iso639_2T_To_639_1.put("sna", "sn");
  
        // Sindhi
        iso639_2T_ToENLang.put("snd", "Sindhi");
        iso639_2T_ToFRLang.put("snd", "sindhi");
    
        iso639_1_To_639_2T.put("sd", "snd");
        iso639_2T_To_639_1.put("snd", "sd");
  
        // Soninke 
        iso639_2T_ToENLang.put("snk", "Soninke ");
        iso639_2T_ToFRLang.put("snk", "soninké");
    
        // Sogdian
        iso639_2T_ToENLang.put("sog", "Sogdian");
        iso639_2T_ToFRLang.put("sog", "sogdien");
    
        // Somali
        iso639_2T_ToENLang.put("som", "Somali");
        iso639_2T_ToFRLang.put("som", "somali");
    
        iso639_1_To_639_2T.put("so", "som");
        iso639_2T_To_639_1.put("som", "so");
  
        // Songhai
        iso639_2T_ToENLang.put("son", "Songhai");
        iso639_2T_ToFRLang.put("son", "songhai");
    
        // Sotho, Southern 
        iso639_2T_ToENLang.put("sot", "Sotho, Southern ");
        iso639_2T_ToFRLang.put("sot", "sotho du Sud");
    
        iso639_1_To_639_2T.put("st", "sot");
        iso639_2T_To_639_1.put("sot", "st");
  
        // Spanish; Castilian
        iso639_2T_ToENLang.put("spa", "Spanish; Castilian");
        iso639_2T_ToFRLang.put("spa", "espagnol; castillan");
    
        iso639_1_To_639_2T.put("es", "spa");
        iso639_2T_To_639_1.put("spa", "es");
  
        // Albanian
        iso639_2T_ToENLang.put("sqi", "Albanian");
        iso639_2T_ToFRLang.put("sqi", "albanais");
    
        iso639_2B_To_639_2T.put("alb", "sqi");
  
        iso639_1_To_639_2T.put("sq", "sqi");
        iso639_2T_To_639_1.put("sqi", "sq");
  
        // Sardinian
        iso639_2T_ToENLang.put("srd", "Sardinian");
        iso639_2T_ToFRLang.put("srd", "sarde");
    
        iso639_1_To_639_2T.put("sc", "srd");
        iso639_2T_To_639_1.put("srd", "sc");
  
        // Serbian
        iso639_2T_ToENLang.put("srp", "Serbian");
        iso639_2T_ToFRLang.put("srp", "serbe");
    
        iso639_2B_To_639_2T.put("scc", "srp");
  
        iso639_1_To_639_2T.put("sr", "srp");
        iso639_2T_To_639_1.put("srp", "sr");
  
        // Serer
        iso639_2T_ToENLang.put("srr", "Serer");
        iso639_2T_ToFRLang.put("srr", "sérère");
    
        // Nilo-Saharan (Other)
        iso639_2T_ToENLang.put("ssa", "Nilo-Saharan (Other)");
        iso639_2T_ToFRLang.put("ssa", "nilo-sahariennes, autres langues");
    
        // Swati
        iso639_2T_ToENLang.put("ssw", "Swati");
        iso639_2T_ToFRLang.put("ssw", "swati");
    
        iso639_1_To_639_2T.put("ss", "ssw");
        iso639_2T_To_639_1.put("ssw", "ss");
  
        // Sukuma
        iso639_2T_ToENLang.put("suk", "Sukuma");
        iso639_2T_ToFRLang.put("suk", "sukuma");
    
        // Sundanese
        iso639_2T_ToENLang.put("sun", "Sundanese");
        iso639_2T_ToFRLang.put("sun", "soundanais");
    
        iso639_1_To_639_2T.put("su", "sun");
        iso639_2T_To_639_1.put("sun", "su");
  
        // Susu
        iso639_2T_ToENLang.put("sus", "Susu");
        iso639_2T_ToFRLang.put("sus", "soussou");
    
        // Sumerian
        iso639_2T_ToENLang.put("sux", "Sumerian");
        iso639_2T_ToFRLang.put("sux", "sumérien");
    
        // Swahili
        iso639_2T_ToENLang.put("swa", "Swahili");
        iso639_2T_ToFRLang.put("swa", "swahili");
    
        iso639_1_To_639_2T.put("sw", "swa");
        iso639_2T_To_639_1.put("swa", "sw");
  
        // Swedish
        iso639_2T_ToENLang.put("swe", "Swedish");
        iso639_2T_ToFRLang.put("swe", "suédois");
    
        iso639_1_To_639_2T.put("sv", "swe");
        iso639_2T_To_639_1.put("swe", "sv");
  
        // Syriac
        iso639_2T_ToENLang.put("syr", "Syriac");
        iso639_2T_ToFRLang.put("syr", "syriaque");
    
        // Tahitian
        iso639_2T_ToENLang.put("tah", "Tahitian");
        iso639_2T_ToFRLang.put("tah", "tahitien");
    
        iso639_1_To_639_2T.put("ty", "tah");
        iso639_2T_To_639_1.put("tah", "ty");
  
        // Tai (Other)
        iso639_2T_ToENLang.put("tai", "Tai (Other)");
        iso639_2T_ToFRLang.put("tai", "thaïes, autres langues");
    
        // Tamil
        iso639_2T_ToENLang.put("tam", "Tamil");
        iso639_2T_ToFRLang.put("tam", "tamoul");
    
        iso639_1_To_639_2T.put("ta", "tam");
        iso639_2T_To_639_1.put("tam", "ta");
  
        // Tatar
        iso639_2T_ToENLang.put("tat", "Tatar");
        iso639_2T_ToFRLang.put("tat", "tatar");
    
        iso639_1_To_639_2T.put("tt", "tat");
        iso639_2T_To_639_1.put("tat", "tt");
  
        // Telugu
        iso639_2T_ToENLang.put("tel", "Telugu");
        iso639_2T_ToFRLang.put("tel", "télougou");
    
        iso639_1_To_639_2T.put("te", "tel");
        iso639_2T_To_639_1.put("tel", "te");
  
        // Timne
        iso639_2T_ToENLang.put("tem", "Timne");
        iso639_2T_ToFRLang.put("tem", "temne");
    
        // Tereno
        iso639_2T_ToENLang.put("ter", "Tereno");
        iso639_2T_ToFRLang.put("ter", "tereno");
    
        // Tetum
        iso639_2T_ToENLang.put("tet", "Tetum");
        iso639_2T_ToFRLang.put("tet", "tetum");
    
        // Tajik
        iso639_2T_ToENLang.put("tgk", "Tajik");
        iso639_2T_ToFRLang.put("tgk", "tadjik");
    
        iso639_1_To_639_2T.put("tg", "tgk");
        iso639_2T_To_639_1.put("tgk", "tg");
  
        // Tagalog
        iso639_2T_ToENLang.put("tgl", "Tagalog");
        iso639_2T_ToFRLang.put("tgl", "tagalog");
    
        iso639_1_To_639_2T.put("tl", "tgl");
        iso639_2T_To_639_1.put("tgl", "tl");
  
        // Thai
        iso639_2T_ToENLang.put("tha", "Thai");
        iso639_2T_ToFRLang.put("tha", "thaï");
    
        iso639_1_To_639_2T.put("th", "tha");
        iso639_2T_To_639_1.put("tha", "th");
  
        // Tibetan
        iso639_2T_ToENLang.put("bod", "Tibetan");
        iso639_2T_ToFRLang.put("bod", "tibétain");
    
        iso639_2B_To_639_2T.put("tib", "bod");
  
        iso639_1_To_639_2T.put("bo", "bod");
        iso639_2T_To_639_1.put("bod", "bo");
  
        // Tigre
        iso639_2T_ToENLang.put("tig", "Tigre");
        iso639_2T_ToFRLang.put("tig", "tigré");
    
        // Tigrinya
        iso639_2T_ToENLang.put("tir", "Tigrinya");
        iso639_2T_ToFRLang.put("tir", "tigrigna");
    
        iso639_1_To_639_2T.put("ti", "tir");
        iso639_2T_To_639_1.put("tir", "ti");
  
        // Tiv
        iso639_2T_ToENLang.put("tiv", "Tiv");
        iso639_2T_ToFRLang.put("tiv", "tiv");
    
        // Tokelau
        iso639_2T_ToENLang.put("tkl", "Tokelau");
        iso639_2T_ToFRLang.put("tkl", "tokelau");
    
        // Tlingit
        iso639_2T_ToENLang.put("tli", "Tlingit");
        iso639_2T_ToFRLang.put("tli", "tlingit");
    
        // Tamashek
        iso639_2T_ToENLang.put("tmh", "Tamashek");
        iso639_2T_ToFRLang.put("tmh", "tamacheq");
    
        // Tonga (Nyasa) 
        iso639_2T_ToENLang.put("tog", "Tonga (Nyasa) ");
        iso639_2T_ToFRLang.put("tog", "tonga (Nyasa)");
    
        // Tonga (Tonga Islands)
        iso639_2T_ToENLang.put("ton", "Tonga (Tonga Islands)");
        iso639_2T_ToFRLang.put("ton", "tongan (Îles Tonga)");
    
        iso639_1_To_639_2T.put("to", "ton");
        iso639_2T_To_639_1.put("ton", "to");
  
        // Tok Pisin
        iso639_2T_ToENLang.put("tpi", "Tok Pisin");
        iso639_2T_ToFRLang.put("tpi", "tok pisin");
    
        // Tsimshian
        iso639_2T_ToENLang.put("tsi", "Tsimshian");
        iso639_2T_ToFRLang.put("tsi", "tsimshian");
    
        // Tswana
        iso639_2T_ToENLang.put("tsn", "Tswana");
        iso639_2T_ToFRLang.put("tsn", "tswana");
    
        iso639_1_To_639_2T.put("tn", "tsn");
        iso639_2T_To_639_1.put("tsn", "tn");
  
        // Tsonga
        iso639_2T_ToENLang.put("tso", "Tsonga");
        iso639_2T_ToFRLang.put("tso", "tsonga");
    
        iso639_1_To_639_2T.put("ts", "tso");
        iso639_2T_To_639_1.put("tso", "ts");
  
        // Turkmen
        iso639_2T_ToENLang.put("tuk", "Turkmen");
        iso639_2T_ToFRLang.put("tuk", "turkmène");
    
        iso639_1_To_639_2T.put("tk", "tuk");
        iso639_2T_To_639_1.put("tuk", "tk");
  
        // Tumbuka
        iso639_2T_ToENLang.put("tum", "Tumbuka");
        iso639_2T_ToFRLang.put("tum", "tumbuka");
    
        // Tupi languages
        iso639_2T_ToENLang.put("tup", "Tupi languages");
        iso639_2T_ToFRLang.put("tup", "tupi, langues");
    
        // Turkish
        iso639_2T_ToENLang.put("tur", "Turkish");
        iso639_2T_ToFRLang.put("tur", "turc");
    
        iso639_1_To_639_2T.put("tr", "tur");
        iso639_2T_To_639_1.put("tur", "tr");
  
        // Altaic (Other)
        iso639_2T_ToENLang.put("tut", "Altaic (Other)");
        iso639_2T_ToFRLang.put("tut", "altaïques, autres langues");
    
        // Tuvalu
        iso639_2T_ToENLang.put("tvl", "Tuvalu");
        iso639_2T_ToFRLang.put("tvl", "tuvalu");
    
        // Twi
        iso639_2T_ToENLang.put("twi", "Twi");
        iso639_2T_ToFRLang.put("twi", "twi");
    
        iso639_1_To_639_2T.put("tw", "twi");
        iso639_2T_To_639_1.put("twi", "tw");
  
        // Tuvinian
        iso639_2T_ToENLang.put("tyv", "Tuvinian");
        iso639_2T_ToFRLang.put("tyv", "touva");
    
        // Ugaritic
        iso639_2T_ToENLang.put("uga", "Ugaritic");
        iso639_2T_ToFRLang.put("uga", "ougaritique");
    
        // Uighur
        iso639_2T_ToENLang.put("uig", "Uighur");
        iso639_2T_ToFRLang.put("uig", "ouïgour");
    
        iso639_1_To_639_2T.put("ug", "uig");
        iso639_2T_To_639_1.put("uig", "ug");
  
        // Ukrainian
        iso639_2T_ToENLang.put("ukr", "Ukrainian");
        iso639_2T_ToFRLang.put("ukr", "ukrainien");
    
        iso639_1_To_639_2T.put("uk", "ukr");
        iso639_2T_To_639_1.put("ukr", "uk");
  
        // Umbundu
        iso639_2T_ToENLang.put("umb", "Umbundu");
        iso639_2T_ToFRLang.put("umb", "umbundu");
    
        // Undetermined 
        iso639_2T_ToENLang.put("und", "Undetermined ");
        iso639_2T_ToFRLang.put("und", "indéterminée");
    
        // Urdu
        iso639_2T_ToENLang.put("urd", "Urdu");
        iso639_2T_ToFRLang.put("urd", "ourdou");
    
        iso639_1_To_639_2T.put("ur", "urd");
        iso639_2T_To_639_1.put("urd", "ur");
  
        // Uzbek
        iso639_2T_ToENLang.put("uzb", "Uzbek");
        iso639_2T_ToFRLang.put("uzb", "ouszbek");
    
        iso639_1_To_639_2T.put("uz", "uzb");
        iso639_2T_To_639_1.put("uzb", "uz");
  
        // Vai
        iso639_2T_ToENLang.put("vai", "Vai");
        iso639_2T_ToFRLang.put("vai", "vaï");
    
        // Venda
        iso639_2T_ToENLang.put("ven", "Venda");
        iso639_2T_ToFRLang.put("ven", "venda");
    
        // Vietnamese
        iso639_2T_ToENLang.put("vie", "Vietnamese");
        iso639_2T_ToFRLang.put("vie", "vietnamien");
    
        iso639_1_To_639_2T.put("vi", "vie");
        iso639_2T_To_639_1.put("vie", "vi");
  
        // Volapük
        iso639_2T_ToENLang.put("vol", "Volapük");
        iso639_2T_ToFRLang.put("vol", "volapük");
    
        iso639_1_To_639_2T.put("vo", "vol");
        iso639_2T_To_639_1.put("vol", "vo");
  
        // Votic
        iso639_2T_ToENLang.put("vot", "Votic");
        iso639_2T_ToFRLang.put("vot", "vote");
    
        // Wakashan languages
        iso639_2T_ToENLang.put("wak", "Wakashan languages");
        iso639_2T_ToFRLang.put("wak", "wakashennes, langues");
    
        // Walamo
        iso639_2T_ToENLang.put("wal", "Walamo");
        iso639_2T_ToFRLang.put("wal", "walamo");
    
        // Waray
        iso639_2T_ToENLang.put("war", "Waray");
        iso639_2T_ToFRLang.put("war", "waray");
    
        // Washo
        iso639_2T_ToENLang.put("was", "Washo");
        iso639_2T_ToFRLang.put("was", "washo");
    
        // Welsh
        iso639_2T_ToENLang.put("cym", "Welsh");
        iso639_2T_ToFRLang.put("cym", "gallois");
    
        iso639_2B_To_639_2T.put("wel", "cym");
  
        iso639_1_To_639_2T.put("cy", "cym");
        iso639_2T_To_639_1.put("cym", "cy");
  
        // Sorbian languages
        iso639_2T_ToENLang.put("wen", "Sorbian languages");
        iso639_2T_ToFRLang.put("wen", "sorabes, langues");
    
        // Walloon
        iso639_2T_ToENLang.put("wln", "Walloon");
        iso639_2T_ToFRLang.put("wln", "wallon");
    
        iso639_1_To_639_2T.put("wa", "wln");
        iso639_2T_To_639_1.put("wln", "wa");
  
        // Wolof
        iso639_2T_ToENLang.put("wol", "Wolof");
        iso639_2T_ToFRLang.put("wol", "wolof");
    
        iso639_1_To_639_2T.put("wo", "wol");
        iso639_2T_To_639_1.put("wol", "wo");
  
        // Xhosa
        iso639_2T_ToENLang.put("xho", "Xhosa");
        iso639_2T_ToFRLang.put("xho", "xhosa");
    
        iso639_1_To_639_2T.put("xh", "xho");
        iso639_2T_To_639_1.put("xho", "xh");
  
        // Yao
        iso639_2T_ToENLang.put("yao", "Yao");
        iso639_2T_ToFRLang.put("yao", "yao");
    
        // Yapese
        iso639_2T_ToENLang.put("yap", "Yapese");
        iso639_2T_ToFRLang.put("yap", "yapois");
    
        // Yiddish
        iso639_2T_ToENLang.put("yid", "Yiddish");
        iso639_2T_ToFRLang.put("yid", "yiddish");
    
        iso639_1_To_639_2T.put("yi", "yid");
        iso639_2T_To_639_1.put("yid", "yi");
  
        // Yoruba
        iso639_2T_ToENLang.put("yor", "Yoruba");
        iso639_2T_ToFRLang.put("yor", "yoruba");
    
        iso639_1_To_639_2T.put("yo", "yor");
        iso639_2T_To_639_1.put("yor", "yo");
  
        // Yupik languages
        iso639_2T_ToENLang.put("ypk", "Yupik languages");
        iso639_2T_ToFRLang.put("ypk", "yupik, langues");
    
        // Zapotec
        iso639_2T_ToENLang.put("zap", "Zapotec");
        iso639_2T_ToFRLang.put("zap", "zapotèque");
    
        // Zenaga
        iso639_2T_ToENLang.put("zen", "Zenaga");
        iso639_2T_ToFRLang.put("zen", "zenaga");
    
        // Zhuang; Chuang
        iso639_2T_ToENLang.put("zha", "Zhuang; Chuang");
        iso639_2T_ToFRLang.put("zha", "zhuang; chuang");
    
        iso639_1_To_639_2T.put("za", "zha");
        iso639_2T_To_639_1.put("zha", "za");
  
        // Chinese
        iso639_2T_ToENLang.put("zho", "Chinese");
        iso639_2T_ToFRLang.put("zho", "chinois");
    
        iso639_2B_To_639_2T.put("chi", "zho");
  
        iso639_1_To_639_2T.put("zh", "zho");
        iso639_2T_To_639_1.put("zho", "zh");
  
        // Zande
        iso639_2T_ToENLang.put("znd", "Zande");
        iso639_2T_ToFRLang.put("znd", "zandé");
    
        // Zulu
        iso639_2T_ToENLang.put("zul", "Zulu");
        iso639_2T_ToFRLang.put("zul", "zoulou");
    
        iso639_1_To_639_2T.put("zu", "zul");
        iso639_2T_To_639_1.put("zul", "zu");
  
        // Zuni
        iso639_2T_ToENLang.put("zun", "Zuni");
        iso639_2T_ToFRLang.put("zun", "zuni");
    
        iso15924ToName.put("Ab", "Abur");
  
        iso15924ToName.put("Ah", "Ahom");
  
        iso15924ToName.put("Ai", "Aiha (Kesh)");
  
        iso15924ToName.put("Sq", "Albanian");
  
        iso15924ToName.put("Ar", "Arabic");
  
        iso15924ToName.put("Ak", "Arabic (Kufi variant)");
  
        iso15924ToName.put("Aw", "Aramaic");
  
        iso15924ToName.put("Hy", "Armenian");
  
        iso15924ToName.put("Xa", "Assyrian cuneiform (Babylonian & Akkadian)");
  
        iso15924ToName.put("Ay", "Aymara pictograms");
  
        iso15924ToName.put("Az", "Aztec pictograms");
  
        iso15924ToName.put("Bl", "Balinese");
  
        iso15924ToName.put("Bt", "Balti");
  
        iso15924ToName.put("Bm", "Bamum (Cameroon)");
  
        iso15924ToName.put("Bk", "Batak");
  
        iso15924ToName.put("Bn", "Bengali");
  
        iso15924ToName.put("Bs", "Bisaya");
  
        iso15924ToName.put("By", "Blissymbols");
  
        iso15924ToName.put("Bp", "Bopomofo");
  
        iso15924ToName.put("Bx", "Box-headed script");
  
        iso15924ToName.put("Br", "Brahmi (Ashoka)");
  
        iso15924ToName.put("Ba", "Braille");
  
        iso15924ToName.put("Bg", "Buginese (Makassar)");
  
        iso15924ToName.put("My", "Burmese");
  
        iso15924ToName.put("Bu", "Buthakukye (Albanian)");
  
        iso15924ToName.put("Sl", "Canadian Syllabic (Unified)");
  
        iso15924ToName.put("Cr", "Carian");
  
        iso15924ToName.put("Ck", "Chakma");
  
        iso15924ToName.put("Ch", "Cham");
  
        iso15924ToName.put("Jl", "Cherokee syllabary");
  
        iso15924ToName.put("Cn", "Chinook shorthand");
  
        iso15924ToName.put("Cl", "Chola");
  
        iso15924ToName.put("Cu", "Chu Nom");
  
        iso15924ToName.put("Ci", "Cirth");
  
        iso15924ToName.put("Zy", "Code for undetermined script");
  
        iso15924ToName.put("Zx", "Code for unwritten languages");
  
        iso15924ToName.put("Qb", "Coptic");
  
        iso15924ToName.put("Cp", "Cypriote syllabary");
  
        iso15924ToName.put("Cm", "Cypro-Minoan");
  
        iso15924ToName.put("Cy", "Cyrillic");
  
        iso15924ToName.put("Da", "Dai");
  
        iso15924ToName.put("Ds", "Deseret (Mormon)");
  
        iso15924ToName.put("Dn", "Devanagari");
  
        iso15924ToName.put("Ed", "Egyptian demotic");
  
        iso15924ToName.put("Ea", "Egyptian hieroglyphic alphabet");
  
        iso15924ToName.put("Eh", "Egyptian hieroglyphs");
  
        iso15924ToName.put("Es", "Elbassen (Albanian)");
  
        iso15924ToName.put("En", "Engsvanyali");
  
        iso15924ToName.put("Et", "Ethiopic");
  
        iso15924ToName.put("Eo", "Etruscan & Oscan");
  
        iso15924ToName.put("Gr", "Gargoyle");
  
        iso15924ToName.put("Gd", "Gaudiya");
  
        iso15924ToName.put("Ka", "Georgian (Mxedruli)");
  
        iso15924ToName.put("Kx", "Georgian (Xucuri)");
  
        iso15924ToName.put("Gl", "Glagolitic");
  
        iso15924ToName.put("Gt", "Gothic");
  
        iso15924ToName.put("El", "Greek");
  
        iso15924ToName.put("Gu", "Gujarati");
  
        iso15924ToName.put("Pa", "Gurmukhi");
  
        iso15924ToName.put("Hn", "Han ideographs");
  
        iso15924ToName.put("Hg", "Hangul");
  
        iso15924ToName.put("He", "Hebrew");
  
        iso15924ToName.put("Hr", "Hiragana");
  
        iso15924ToName.put("Xh", "Hittite cuneiform");
  
        iso15924ToName.put("Hh", "Hittite syllabic & hieroglyphic");
  
        iso15924ToName.put("Hm", "Hmong");
  
        iso15924ToName.put("Ib", "Iberian");
  
        iso15924ToName.put("Il", "Ilianore");
  
        iso15924ToName.put("Iv", "Indus Valley");
  
        iso15924ToName.put("Jw", "Javanese");
  
        iso15924ToName.put("Jn", "Jindai");
  
        iso15924ToName.put("Kb", "Kadamba");
  
        iso15924ToName.put("Ki", "Kaithi");
  
        iso15924ToName.put("Kn", "Kannada");
  
        iso15924ToName.put("Kr", "Karenni (Kayah Li)");
  
        iso15924ToName.put("Kk", "Katakana");
  
        iso15924ToName.put("Kd", "Kauder (Micmac)");
  
        iso15924ToName.put("Kw", "Kawi");
  
        iso15924ToName.put("Kh", "Khamti (Kham)");
  
        iso15924ToName.put("Ks", "Kharoshthi");
  
        iso15924ToName.put("Kt", "Khitan (Ch'i-tan, Liao)");
  
        iso15924ToName.put("Km", "Khmer");
  
        iso15924ToName.put("Kq", "Khotanese");
  
        iso15924ToName.put("Ky", "Kinya");
  
        iso15924ToName.put("Lb", "Kirat (Limbu)");
  
        iso15924ToName.put("Pq", "Klingon pIQaD");
  
        iso15924ToName.put("Hu", "Kök Turki runes");
  
        iso15924ToName.put("Kl", "Koleruttu");
  
        iso15924ToName.put("Ku", "Kuoyu");
  
        iso15924ToName.put("Kf", "Kutila");
  
        iso15924ToName.put("Lk", "Lahnda (Khudawadi)");
  
        iso15924ToName.put("Ln", "Lahnda (Sindhi)");
  
        iso15924ToName.put("Lm", "Lampong");
  
        iso15924ToName.put("Lo", "Lao");
  
        iso15924ToName.put("La", "Latin");
  
        iso15924ToName.put("Lf", "Latin (Fraktur variant)");
  
        iso15924ToName.put("Lg", "Latin (Gaelic variant)");
  
        iso15924ToName.put("Lp", "Lepcha (Róng)");
  
        iso15924ToName.put("Na", "Linear A");
  
        iso15924ToName.put("Nb", "Linear B");
  
        iso15924ToName.put("Ls", "Lisu");
  
        iso15924ToName.put("Ll", "Lolo");
  
        iso15924ToName.put("Lc", "Lycian");
  
        iso15924ToName.put("Ld", "Lydian");
  
        iso15924ToName.put("Mg", "Maghreb");
  
        iso15924ToName.put("Mj", "Maithli");
  
        iso15924ToName.put("Ml", "Malayalam");
  
        iso15924ToName.put("Mc", "Manchu");
  
        iso15924ToName.put("Md", "Mandaean");
  
        iso15924ToName.put("Ma", "Mangyan");
  
        iso15924ToName.put("Mi", "Manichaean");
  
        iso15924ToName.put("Mh", "Mayan hieroglyphs");
  
        iso15924ToName.put("Mp", "Meitei (Manipuri)");
  
        iso15924ToName.put("Me", "Meroitic");
  
        iso15924ToName.put("Mo", "Modi");
  
        iso15924ToName.put("Mn", "Mongolian");
  
        iso15924ToName.put("Mu", "Multani");
  
        iso15924ToName.put("Nt", "Nabataean");
  
        iso15924ToName.put("Mt", "Naxi (Moso) phonetic");
  
        iso15924ToName.put("Ms", "Naxi (Nahsi, Nasi, Moso) ideograms");
  
        iso15924ToName.put("Nw", "Newari");
  
        iso15924ToName.put("Nu", "Nuchen (Yu-Chen)");
  
        iso15924ToName.put("Nm", "Numidian");
  
        iso15924ToName.put("Og", "Ogham");
  
        iso15924ToName.put("Cv", "Old Church Slavonic");
  
        iso15924ToName.put("Hv", "Old Hungarian runes");
  
        iso15924ToName.put("Pg", "Old Peguan");
  
        iso15924ToName.put("Xp", "Old Persian cuneiform");
  
        iso15924ToName.put("Or", "Oriya");
  
        iso15924ToName.put("Os", "Osmanya");
  
        iso15924ToName.put("Av", "Pahlavi (Avestan)");
  
        iso15924ToName.put("Pk", "Pali (Kyoktsa & Painted)");
  
        iso15924ToName.put("Pm", "Palmyrene");
  
        iso15924ToName.put("Pc", "Pancartambo");
  
        iso15924ToName.put("Pr", "Parthian");
  
        iso15924ToName.put("Pp", "'Phags-pa");
  
        iso15924ToName.put("Ps", "Phaistos Disk");
  
        iso15924ToName.put("Ph", "Phoenician");
  
        iso15924ToName.put("Pl", "Pollard Phonetic");
  
        iso15924ToName.put("Pb", "Proto-Byblic");
  
        iso15924ToName.put("Pe", "Proto-Elamic");
  
        iso15924ToName.put("Py", "Pyu (Tircul)");
  
        iso15924ToName.put("Rj", "Rejang");
  
        iso15924ToName.put("Rr", "Rongo-rongo");
  
        iso15924ToName.put("Rn", "Runic (Germanic)");
  
        iso15924ToName.put("Sk", "Saki");
  
        iso15924ToName.put("Sm", "Samaritan");
  
        iso15924ToName.put("Sr", "Sarada");
  
        iso15924ToName.put("Sv", "Satavahana");
  
        iso15924ToName.put("Su", "Seuss");
  
        iso15924ToName.put("Sw", "Shavian (Shaw)");
  
        iso15924ToName.put("Sd", "Siddham");
  
        iso15924ToName.put("St", "Siddhamatrka");
  
        iso15924ToName.put("Se", "Sidetic");
  
        iso15924ToName.put("Si", "Sinhalese");
  
        iso15924ToName.put("Sg", "Sogdian");
  
        iso15924ToName.put("Ss", "Solresol");
  
        iso15924ToName.put("Sa", "South Arabian");
  
        iso15924ToName.put("Xs", "Sumerian pictograms");
  
        iso15924ToName.put("Sy", "Syriac");
  
        iso15924ToName.put("Tg", "Tagalog");
  
        iso15924ToName.put("Tl", "Tai Lue (Chiang Mai)");
  
        iso15924ToName.put("Tn", "Tai Nua (Tai Mau)");
  
        iso15924ToName.put("Tc", "Takri (Chameali)");
  
        iso15924ToName.put("Tj", "Takri (Jaunsari)");
  
        iso15924ToName.put("Ta", "Tamil");
  
        iso15924ToName.put("Tr", "Tamil Granta");
  
        iso15924ToName.put("Tk", "Tankri");
  
        iso15924ToName.put("Te", "Telugu");
  
        iso15924ToName.put("Tw", "Tengwar");
  
        iso15924ToName.put("Dv", "Thaana");
  
        iso15924ToName.put("Th", "Thai");
  
        iso15924ToName.put("Bo", "Tibetan");
  
        iso15924ToName.put("Tf", "Tifinagh");
  
        iso15924ToName.put("To", "Tocharian");
  
        iso15924ToName.put("Tu", "Tungut (Xixia) ideograms");
  
        iso15924ToName.put("Xu", "Ugaritic cuneiform");
  
        iso15924ToName.put("Ui", "Uighur");
  
        iso15924ToName.put("Un", "Unifon");
  
        iso15924ToName.put("Va", "Vai");
  
        iso15924ToName.put("Vt", "Vattelluttu");
  
        iso15924ToName.put("Vd", "Verdurian");
  
        iso15924ToName.put("Vb", "Veso Bei");
  
        iso15924ToName.put("Vs", "Visible Speech");
  
        iso15924ToName.put("Wo", "Woleai");
  
        iso15924ToName.put("Yy", "Yi");
  
    }

    /**
     * Get the canonical 2-letter ISO 3166 country code corresponding
     * to the argument.  ISO 3166 codes are upper case by convention.
     * @param code - the String code.
     * @return - the equivalent ISO 3166 code, or null if the
     * code is invalid.
     */
    public static String canonicalCountryCode(String code) {
        String hicode = code.toUpperCase();
        if (iso3166ToName.get(hicode) != null)
            return hicode;
        return null;
    }

    /**
     * Get the English name corresponding to a country code.
     * @param code - the String code.
     * @return - the English name of the country, or null if the
     * code is invalid.
     */
    public static String getCountryName(String code) {
      return (String)(iso3166ToName.get(canonicalCountryCode(code)));
    }

    /**
     * Get the canonical 3-letter ISO 639-2 Terminology code corresponding
     * to a language code.  The argument may be an ISO 639-2 Terminology
     * code, an ISO 639-2 Bibliographic code, or an ISO 639-1 2-letter code.
     * By convention, language codes are expressed in lower case.
     * @param code - the String code.
     * @return - the equivalent ISO 639-2T code, or the ISO 639_1 code if no
     * not three-letter code exists, or null if the code is invalid.
     */
    public static String canonicalLangCode(String code) {
        String wasBiblio;
        String wasIso639_1;
        String locode = code.toLowerCase();
        // Check for valid terminology code
        if (iso639_2T_ToENLang.get(locode) != null)
            return locode;
        // Check for valid 2-letter code
        if ((wasIso639_1 = (String)(iso639_1_To_639_2T.get(locode))) != null)
            return wasIso639_1;
        // Check for valid bibliographic code
        if ((wasBiblio = (String)(iso639_2B_To_639_2T.get(locode))) != null)
            return wasBiblio;
        return null;
    }

    /**
     * Get the canonical 2-letter ISO 639-1 code corresponding to an
     * ISO 639 code.  The argument must be an ISO 639-2 Terminology code.
     * By convention, language codes are expressed in lower case.
     * @param code - the 3-letter ISO 639-2 Terminology code.
     * @return - the equivalent ISO 639-1 code, or null if the
     * code is invalid.
     */
    public static String canonical639_1Code(String code) {
        // Check for valid 3-letter code
        return (String)(iso639_2T_To_639_1.get(code.toLowerCase()));
    }

    /**
     * Get the English name corresponding to a language code.
     * @param code - the String code.
     * @return - the English name of the language, or null if the
     * code is invalid.
     */
    public static String getEnglishName(String code) {
      return (String)(iso639_2T_ToENLang.get(canonicalLangCode(code)));
    }

    /**
     * Get the French name corresponding to a language code.
     * @param code - the String code.
     * @return - the French name of the language, or null if the
     * code is invalid.
     */
    public static String getFrenchName(String code) {
      return (String)(iso639_2T_ToFRLang.get(canonicalLangCode(code)));
    }

    /**
     * Get the canonical 2-letter ISO 15924 script code corresponding
     * to the argument.  ISO 15924 codes are camel case by convention.
     * @param code - the String code.
     * @return - the equivalent ISO 15924 code, or null if the
     * code is invalid.
     */
    public static String canonicalScriptCode(String code) {
        if (code.length() != 2)
            return null;
        String hilocode = code.substring(0,1).toUpperCase()
                                        + code.substring(1).toLowerCase();
        if (iso15924ToName.get(hilocode) != null)
            return hilocode;
        return null;
    }

    /**
     * Get the English name corresponding to a script code.
     * @param code - the String code.
     * @return - the English name of the script, or null if the
     * code is invalid.
     */
    public static String getScriptName(String code) {
      return (String)(iso15924ToName.get(canonicalScriptCode(code)));
    }

}
