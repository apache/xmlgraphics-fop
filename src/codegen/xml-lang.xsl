<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- $Id$ -->
  <xsl:variable name="licence"><![CDATA[/*
 * $Id]]><![CDATA[$

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
 */]]> 
</xsl:variable>
<!--
     This XSL file is used to generate the file
     org/apache/fop/datatypes/CountryLanguageScript.java
     from the input file
     xml-lang.xml
     found in the conf directory. -->
<xsl:output method="text" encoding="utf-8"/>
<xsl:template match="xml-lang">
  <xsl:value-of select="$licence"/>
  <xsl:variable name="languages">
    <xsl:value-of select="count(./languagecodes/language)"/>
  </xsl:variable>
  <xsl:variable name="countries">
    <xsl:value-of select="count(./countrycodes/country)"/>
  </xsl:variable>
  <xsl:variable name="scripts">
    <xsl:value-of select="count(./scriptcodes/script)"/>
  </xsl:variable>
package org.apache.fop.datatypes;

import java.util.HashMap;

/**
 * A class for accessing and validating:
 * ISO 3166 country codes,
 * ISO 639-2T, 639-2B and 639-1 language codes,
 * and ISO 15924 script codes.
 * @see &lt;a href="http://www.iso.ch/iso/en/prods-services/iso3166ma/02iso-3166-code-lists/index.html"&gt;iso3166&lt;/a&gt;
 * @see &lt;a href="http://www.loc.gov/standards/iso639-2/"&gt;iso639-2&lt;/a&gt;
 * @see &lt;a href="http://www.evertype.com/standards/iso15924/document/index.html"&gt;iso15924&lt;/a&gt;
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
        iso3166ToName = new HashMap(<xsl:value-of select="$countries"/>);

        iso639_2T_ToENLang = new HashMap((int)(<xsl:value-of select="$languages"/> * 1.4));
        iso639_2T_ToFRLang = new HashMap((int)(<xsl:value-of select="$languages"/> * 1.4));
        iso639_2B_To_639_2T = new HashMap((int)(<xsl:value-of select="$languages"/> * 1.4));
        iso639_1_To_639_2T = new HashMap((int)(<xsl:value-of select="$languages"/> * 1.4));
        iso639_2T_To_639_1 = new HashMap((int)(<xsl:value-of select="$languages"/> * 1.4));

        iso15924ToName = new HashMap(<xsl:value-of select="$scripts"/>);
    <xsl:apply-templates select="countrycodes/country"/>
    <xsl:apply-templates select="languagecodes/language"/>
    <xsl:apply-templates select="scriptcodes/script"/>
    }

    /**
     * Get the canonical 2-letter ISO 3166 country code corresponding
     * to the argument.  ISO 3166 codes are upper case by convention.
     * @param code - the <tt>String</tt> code.
     * @return - the equivalent ISO 3166 code, or <tt>null</tt> if the
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
     * @param code - the <tt>String</tt> code.
     * @return - the English name of the country, or <tt>null</tt> if the
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
     * @param code - the <tt>String</tt> code.
     * @return - the equivalent ISO 639-2T code, or the ISO 639_1 code if no
     * not three-letter code exists, or <tt>null</tt> if the code is invalid.
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
     * @return - the equivalent ISO 639-1 code, or <tt>null</tt> if the
     * code is invalid.
     */
    public static String canonical639_1Code(String code) {
        // Check for valid 3-letter code
        return (String)(iso639_2T_To_639_1.get(code.toLowerCase()));
    }

    /**
     * Get the English name corresponding to a language code.
     * @param code - the <tt>String</tt> code.
     * @return - the English name of the language, or <tt>null</tt> if the
     * code is invalid.
     */
    public static String getEnglishName(String code) {
      return (String)(iso639_2T_ToENLang.get(canonicalLangCode(code)));
    }

    /**
     * Get the French name corresponding to a language code.
     * @param code - the <tt>String</tt> code.
     * @return - the French name of the language, or <tt>null</tt> if the
     * code is invalid.
     */
    public static String getFrenchName(String code) {
      return (String)(iso639_2T_ToFRLang.get(canonicalLangCode(code)));
    }

    /**
     * Get the canonical 2-letter ISO 15924 script code corresponding
     * to the argument.  ISO 15924 codes are camel case by convention.
     * @param code - the <tt>String</tt> code.
     * @return - the equivalent ISO 15924 code, or <tt>null</tt> if the
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
     * @param code - the <tt>String</tt> code.
     * @return - the English name of the script, or <tt>null</tt> if the
     * code is invalid.
     */
    public static String getScriptName(String code) {
      return (String)(iso15924ToName.get(canonicalScriptCode(code)));
    }

}
</xsl:template>
  <xsl:template match="countrycodes/country">
        iso3166ToName.put("<xsl:value-of select="@code"/>", "<xsl:value-of select="@name"/>");
  </xsl:template>
  <xsl:template match="languagecodes/language">
        // <xsl:value-of select="@EnglishName"/>
        iso639_2T_ToENLang.put("<xsl:value-of select="@terminology"/>", "<xsl:value-of select="@EnglishName"/>");
        iso639_2T_ToFRLang.put("<xsl:value-of select="@terminology"/>", "<xsl:value-of select="@FrenchName"/>");
    <xsl:apply-templates select="@bibliographic"/>
    <xsl:apply-templates select="@iso639-1"/>
  </xsl:template>
  <xsl:template match="@bibliographic">
        iso639_2B_To_639_2T.put("<xsl:value-of select="."/>", "<xsl:value-of select="../@terminology"/>");
  </xsl:template>
  <xsl:template match="@iso639-1">
        iso639_1_To_639_2T.put("<xsl:value-of select="."/>", "<xsl:value-of select="../@terminology"/>");
        iso639_2T_To_639_1.put("<xsl:value-of select="../@terminology"/>", "<xsl:value-of select="."/>");
  </xsl:template>
  <xsl:template match="scriptcodes/script">
        iso15924ToName.put("<xsl:value-of select="@code"/>", "<xsl:value-of select="@name"/>");
  </xsl:template>
</xsl:stylesheet>
