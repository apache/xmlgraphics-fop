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

package org.apache.fop.fonts;

//Java
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.apps.TTFReader;

// CSOFF: LineLengthCheck

/**
 * Class for reading a metric.xml file and creating a font object.
 * Typical usage:
 * <pre>
 * FontReader reader = new FontReader(<path til metrics.xml>);
 * reader.setFontEmbedPath(<path to a .ttf or .pfb file or null to diable embedding>);
 * reader.useKerning(true);
 * Font f = reader.getFont();
 * </pre>
 */
public class FontReader extends DefaultHandler {

    private Locator locator = null;
    private boolean isCID = false;
    private CustomFont returnFont = null;
    private MultiByteFont multiFont = null;
    private SingleByteFont singleFont = null;
    private StringBuffer text = new StringBuffer();

    private List cidWidths = null;
    private int cidWidthIndex = 0;

    private Map currentKerning = null;

    private List bfranges = null;

    /* advanced typographic (script extras) support */
    private boolean inScriptExtras = false;
    private int seTable = -1;
    private Map seLookups = null;
    private String seScript = null;
    private String seLanguage = null;
    private String seFeature = null;
    private String seUseLookup = null;
    private List seUseLookups = null;
    private String luID = null;
    private int luType = -1;
    private List ltSubtables = null;
    private int luSequence = -1;
    private int luFlags = 0;
    private int lstSequence = -1;
    private int lstFormat = -1;
    private List lstCoverage = null;
    private List lstGIDs = null;
    private List lstRanges = null;
    private List lstEntries = null;
    private List lstLIGSets = null;
    private List lstLIGs = null;
    private int ligGID = -1;
    /* end of script extras parse state */

    private void createFont(InputSource source) throws FOPException {
        XMLReader parser = null;

        try {
            final SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newSAXParser().getXMLReader();
        } catch (Exception e) {
            throw new FOPException(e);
        }
        if (parser == null) {
            throw new FOPException("Unable to create SAX parser");
        }

        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              false);
        } catch (SAXException e) {
            throw new FOPException("You need a SAX parser which supports SAX version 2",
                                   e);
        }

        parser.setContentHandler(this);

        try {
            parser.parse(source);
        } catch (SAXException e) {
            throw new FOPException(e);
        } catch (IOException e) {
            throw new FOPException(e);
        }

    }

    /**
     * Sets the path to embed a font. A null value disables font embedding.
     * @param path URI for the embeddable file
     */
    public void setFontEmbedPath(String path) {
        returnFont.setEmbedFileName(path);
    }

    /**
     * Enable/disable use of kerning for the font
     * @param enabled true to enable kerning, false to disable
     */
    public void setKerningEnabled(boolean enabled) {
        returnFont.setKerningEnabled(enabled);
    }

    /**
     * Enable/disable use of advanced typographic features for the font
     * @param enabled true to enable, false to disable
     */
    public void setAdvancedEnabled(boolean enabled) {
        returnFont.setAdvancedEnabled(enabled);
    }

    /**
     * Sets the font resolver. Needed for URI resolution.
     * @param resolver the font resolver
     */
    public void setResolver(FontResolver resolver) {
        returnFont.setResolver(resolver);
    }


    /**
     * Get the generated font object
     * @return the font
     */
    public Typeface getFont() {
        return returnFont;
    }

    /**
     * Construct a FontReader object from a path to a metric.xml file
     * and read metric data
     * @param source Source of the font metric file
     * @throws FOPException if loading the font fails
     */
    public FontReader(InputSource source) throws FOPException {
        createFont(source);
    }

    /**
     * {@inheritDoc}
     */
    public void startDocument() {
    }

    /**
     * {@inheritDoc}
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * {@inheritDoc}
     */
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        if ( inScriptExtras ) {
            startElementScriptExtras ( uri, localName, qName, attributes );
        } else if (localName.equals("font-metrics")) {
            if ("TYPE0".equals(attributes.getValue("type"))) {
                multiFont = new MultiByteFont();
                returnFont = multiFont;
                isCID = true;
                TTFReader.checkMetricsVersion(attributes);
            } else if ("TRUETYPE".equals(attributes.getValue("type"))) {
                singleFont = new SingleByteFont();
                singleFont.setFontType(FontType.TRUETYPE);
                returnFont = singleFont;
                isCID = false;
                TTFReader.checkMetricsVersion(attributes);
            } else {
                singleFont = new SingleByteFont();
                singleFont.setFontType(FontType.TYPE1);
                returnFont = singleFont;
                isCID = false;
            }
        } else if ("embed".equals(localName)) {
            returnFont.setEmbedFileName(attributes.getValue("file"));
            returnFont.setEmbedResourceName(attributes.getValue("class"));
        } else if ("cid-widths".equals(localName)) {
            cidWidthIndex = getInt(attributes.getValue("start-index"));
            cidWidths = new java.util.ArrayList();
        } else if ("kerning".equals(localName)) {
            currentKerning = new java.util.HashMap();
            returnFont.putKerningEntry(new Integer(attributes.getValue("kpx1")),
                                        currentKerning);
        } else if ("bfranges".equals(localName)) {
            bfranges = new java.util.ArrayList();
        } else if ("bf".equals(localName)) {
            BFEntry entry = new BFEntry(getInt(attributes.getValue("us")),
                                        getInt(attributes.getValue("ue")),
                                        getInt(attributes.getValue("gi")));
            bfranges.add(entry);
        } else if ("wx".equals(localName)) {
            cidWidths.add(new Integer(attributes.getValue("w")));
        } else if ("widths".equals(localName)) {
            //singleFont.width = new int[256];
        } else if ("char".equals(localName)) {
            try {
                singleFont.setWidth(Integer.parseInt(attributes.getValue("idx")),
                        Integer.parseInt(attributes.getValue("wdt")));
            } catch (NumberFormatException ne) {
                throw new SAXException("Malformed width in metric file: "
                                   + ne.getMessage(), ne);
            }
        } else if ("pair".equals(localName)) {
            currentKerning.put(new Integer(attributes.getValue("kpx2")),
                               new Integer(attributes.getValue("kern")));
        } else if ("script-extras".equals(localName)) {
            inScriptExtras = true;
        }

    }

    private int getInt(String str) throws SAXException {
        int ret = 0;
        try {
            ret = Integer.parseInt(str);
        } catch (Exception e) {
            throw new SAXException("Error while parsing integer value: " + str, e);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String content = text.toString().trim();
        if ( inScriptExtras ) {
            endElementScriptExtras ( uri, localName, qName, content );
        } else if ("font-name".equals(localName)) {
            returnFont.setFontName(content);
        } else if ("full-name".equals(localName)) {
            returnFont.setFullName(content);
        } else if ("family-name".equals(localName)) {
            Set s = new java.util.HashSet();
            s.add(content);
            returnFont.setFamilyNames(s);
        } else if ("ttc-name".equals(localName) && isCID) {
            multiFont.setTTCName(content);
        } else if ("encoding".equals(localName)) {
            if (singleFont != null && singleFont.getFontType() == FontType.TYPE1) {
                singleFont.setEncoding(content);
            }
        } else if ("cap-height".equals(localName)) {
            returnFont.setCapHeight(getInt(content));
        } else if ("x-height".equals(localName)) {
            returnFont.setXHeight(getInt(content));
        } else if ("ascender".equals(localName)) {
            returnFont.setAscender(getInt(content));
        } else if ("descender".equals(localName)) {
            returnFont.setDescender(getInt(content));
        } else if ("left".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[0] = getInt(content);
            returnFont.setFontBBox(bbox);
        } else if ("bottom".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[1] = getInt(content);
            returnFont.setFontBBox(bbox);
        } else if ("right".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[2] = getInt(content);
            returnFont.setFontBBox(bbox);
        } else if ("top".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[3] = getInt(content);
            returnFont.setFontBBox(bbox);
        } else if ("first-char".equals(localName)) {
            returnFont.setFirstChar(getInt(content));
        } else if ("last-char".equals(localName)) {
            returnFont.setLastChar(getInt(content));
        } else if ("flags".equals(localName)) {
            returnFont.setFlags(getInt(content));
        } else if ("stemv".equals(localName)) {
            returnFont.setStemV(getInt(content));
        } else if ("italic-angle".equals(localName)) {
            returnFont.setItalicAngle(getInt(content));
        } else if ("missing-width".equals(localName)) {
            returnFont.setMissingWidth(getInt(content));
        } else if ("cid-type".equals(localName)) {
            multiFont.setCIDType(CIDFontType.byName(content));
        } else if ("default-width".equals(localName)) {
            multiFont.setDefaultWidth(getInt(content));
        } else if ("cid-widths".equals(localName)) {
            int[] wds = new int[cidWidths.size()];
            int j = 0;
            for (int count = 0; count < cidWidths.size(); count++) {
                Integer i = (Integer)cidWidths.get(count);
                wds[j++] = i.intValue();
            }

            //multiFont.addCIDWidthEntry(cidWidthIndex, wds);
            multiFont.setWidthArray(wds);

        } else if ("bfranges".equals(localName)) {
            multiFont.setBFEntries((BFEntry[])bfranges.toArray(new BFEntry[0]));
        }
        text.setLength(0); //Reset text buffer (see characters())
    }

    /**
     * {@inheritDoc}
     */
    public void characters(char[] ch, int start, int length) {
        text.append(ch, start, length);
    }

    private void validateScriptTag ( String tag )
        throws SAXException {
    }

    private void validateLanguageTag ( String tag, String script )
        throws SAXException {
    }

    private void validateFeatureTag ( String tag, int tableType, String script, String language )
        throws SAXException {
    }

    private int mapLookupType ( String type, int tableType ) {
        int t = -1;
        if ( tableType == GlyphTable.GLYPH_TABLE_TYPE_SUBSTITUTION ) {
            t = GlyphSubstitutionTable.getLookupTypeFromName ( type );
        } else if ( tableType == GlyphTable.GLYPH_TABLE_TYPE_POSITIONING ) {
            t = GlyphPositioningTable.getLookupTypeFromName ( type );
        }
        return t;
    }

    private void validateLookupType ( String type, int tableType )
        throws SAXException {
        if ( mapLookupType ( type, tableType ) == -1 ) {
            throw new SAXParseException ( "invalid lookup type \'" + type + "\'", locator );
        }
    }

    private void startElementScriptExtras ( String uri, String localName, String qName, Attributes attributes )
        throws SAXException {
        if ( "gsub".equals(localName) ) {
            assert seLookups == null;
            seLookups = new java.util.HashMap();
            seTable = GlyphTable.GLYPH_TABLE_TYPE_SUBSTITUTION;
        } else if ( "gpos".equals(localName) ) {
            assert seLookups == null;
            seLookups = new java.util.HashMap();
            seTable = GlyphTable.GLYPH_TABLE_TYPE_POSITIONING;
        } else if ( "script".equals(localName) ) {
            String tag = attributes.getValue("tag");
            if ( tag != null ) {
                assert seScript == null;
                validateScriptTag ( tag );
                seScript = tag;
            } else {
                throw new SAXParseException ( "missing tag attribute on <script/> element", locator );
            }
        } else if ( "lang".equals(localName) ) {
            String tag = attributes.getValue("tag");
            if ( tag != null ) {
                assert seLanguage == null;
                validateLanguageTag ( tag, seScript );
                seLanguage = tag;
            } else {
                throw new SAXParseException ( "missing tag attribute on <lang/> element", locator );
            }
        } else if ( "feature".equals(localName) ) {
            String tag = attributes.getValue("tag");
            if ( tag != null ) {
                validateFeatureTag ( tag, seTable, seScript, seLanguage );
                assert seFeature == null;
                seFeature = tag;
            } else {
                throw new SAXParseException ( "missing tag attribute on <feature/> element", locator );
            }
        } else if ( "use-lookup".equals(localName) ) {
            String ref = attributes.getValue("ref");
            if ( ref != null ) {
                assert seUseLookup == null;
                seUseLookup = ref;
            } else {
                throw new SAXParseException ( "missing ref attribute on <use-lookup/> element", locator );
            }
        } else if ( "lookup".equals(localName) ) {
            String id = attributes.getValue("id");
            if ( id != null ) {
                assert luID == null;
                luID = id; luSequence++; lstSequence = -1;
            } else {
                throw new SAXParseException ( "missing id attribute on <lookup/> element", locator );
            }
            String flags = attributes.getValue("flags");
            if ( flags != null ) {
                try {
                    luFlags = Integer.parseInt ( flags );
                } catch ( NumberFormatException e ) {
                    throw new SAXParseException ( "invalid flags attribute on <lookup/> element, must be integer", locator );
                }
            }
            String type = attributes.getValue("type");
            if ( type != null ) {
                validateLookupType ( type, seTable );
                assert luType == -1;
                luType = mapLookupType ( type, seTable );
            } else {
                throw new SAXParseException ( "missing type attribute on <lookup/> element", locator );
            }
        } else if ( "lst".equals(localName) ) {
            String format = attributes.getValue("format");
            if ( format != null ) {
                try {
                    lstSequence++;
                    lstFormat = Integer.parseInt ( format );
                } catch ( NumberFormatException e ) {
                    throw new SAXParseException ( "invalid format attribute on <lst/> element, must be integer", locator );
                }
                assert lstCoverage == null;
                assert lstEntries == null;
            } else {
                throw new SAXParseException ( "missing format attribute on <lst/> element", locator );
            }
        } else if ( "coverage".equals(localName) ) {
            assert lstGIDs == null;
            assert lstRanges == null;
            lstGIDs = new java.util.ArrayList();
            lstRanges = new java.util.ArrayList();
        } else if ( "range".equals(localName) ) {
            String gs = attributes.getValue("gs");
            String ge = attributes.getValue("ge");
            String ci = attributes.getValue("ci");
            if ( ( gs != null ) && ( ge != null ) && ( ci != null ) ) {
                try {
                    int s = Integer.parseInt ( gs );
                    int e = Integer.parseInt ( ge );
                    int i = Integer.parseInt ( ci );
                    lstRanges.add ( new GlyphCoverageTable.CoverageRange ( s, e, i ) );
                } catch ( NumberFormatException e ) {
                    throw new SAXParseException ( "invalid format attribute on <lst/> element, must be integer", locator );
                } catch ( IllegalArgumentException e ) {
                    throw new SAXParseException ( "bad gs, ge, or ci attribute on <range/> element, must be non-negative integers, with gs <= ge", locator );
                }
            } else {
                throw new SAXParseException ( "missing gs, ge, or ci attribute on <range/> element", locator );
            }
        } else if ( "entries".equals(localName) ) {
            initEntriesState ( seTable, luType, lstFormat );
        } else if ( "ligs".equals(localName) ) {
            assert lstLIGs == null;
            lstLIGs = new java.util.ArrayList();
        } else if ( "lig".equals(localName) ) {
            if ( lstLIGs == null ) {
                throw new SAXParseException ( "missing container <ligs/> element for <lig/> element", locator );
            } else {
                String gid = attributes.getValue("gid");
                if ( gid != null ) {
                    try {
                        ligGID = Integer.parseInt ( gid );
                    } catch ( NumberFormatException e ) {
                        throw new SAXParseException ( "invalid gid attribute on <lig/> element, must be integer", locator );
                    }
                } else {
                    throw new SAXParseException ( "missing gid attribute on <lig/> element", locator );
                }
            }
        }
    }

    private void endElementScriptExtras ( String uri, String localName, String qName, String content )
        throws SAXException {
        if ( "script-extras".equals(localName) ) {
            inScriptExtras = false;
        } else if ( "gsub".equals(localName) ) {
            if ( ( ltSubtables != null ) && ( ltSubtables.size() > 0 ) ) {
                if ( multiFont.getGSUB() == null ) {
                    multiFont.setGSUB ( new GlyphSubstitutionTable ( seLookups, ltSubtables ) );
                }
            }
            ltSubtables = null; seTable = -1; seLookups = null;
        } else if ( "gpos".equals(localName) ) {
            if ( ( ltSubtables != null ) && ( ltSubtables.size() > 0 ) ) {
                if ( multiFont.getGPOS() == null ) {
                    multiFont.setGPOS ( new GlyphPositioningTable ( seLookups, ltSubtables ) );
                }
            }
            ltSubtables = null; seTable = -1; seLookups = null;
        } else if ( "script".equals(localName) ) {
            assert seUseLookups == null;
            assert seUseLookup == null;
            assert seFeature == null;
            assert seLanguage == null;
            seScript = null;
        } else if ( "lang".equals(localName) ) {
            assert seUseLookups == null;
            assert seUseLookup == null;
            assert seFeature == null;
            seLanguage = null;
        } else if ( "feature".equals(localName) ) {
            if ( ( seScript != null ) && ( seLanguage != null ) && ( seFeature != null ) ) {
                if ( ( seUseLookups != null ) && ( seUseLookups.size() > 0 ) ) {
                    seLookups.put ( new GlyphTable.LookupSpec ( seScript, seLanguage, seFeature ), seUseLookups );
                }
            }
            seUseLookups = null; seFeature = null;
        } else if ( "use-lookup".equals(localName) ) {
            if ( seUseLookup != null ) {
                if ( seUseLookups == null ) {
                    seUseLookups = new java.util.ArrayList();
                }
                seUseLookups.add ( seUseLookup );
            }
            seUseLookup = null;
        } else if ( "lookup".equals(localName) ) {
            luType = -1;
            luFlags = 0;
        } else if ( "lst".equals(localName) ) {
            assert lstCoverage != null;
            assert lstEntries != null;
            addLookupSubtable ( seTable, luType, luID, luSequence, luFlags, lstFormat, lstCoverage, lstEntries );
            lstFormat = -1;
            lstCoverage = null;
            lstEntries = null;
        } else if ( "coverage".equals(localName) ) {
            assert lstGIDs != null;
            assert lstRanges != null;
            assert lstCoverage == null;
            if ( lstGIDs.size() > 0 ) {
                lstCoverage = lstGIDs;
            } else if ( lstRanges.size() > 0 ) {
                lstCoverage = lstRanges;
            }
            lstGIDs = null; lstRanges = null;
        } else if ( "gid".equals(localName) ) {
            if ( lstGIDs != null ) {
                try {
                    lstGIDs.add ( Integer.decode ( content ) );
                } catch ( NumberFormatException e ) {
                    throw new SAXParseException ( "invalid <gid/> element content, must be integer", locator );
                }
            }
        } else if ( "entries".equals(localName) ) {
            finishEntriesState ( seTable, luType, lstFormat );
        } else if ( "ligs".equals(localName) ) {
            assert lstLIGSets != null;
            assert lstLIGs != null;
            lstLIGSets.add ( new GlyphSubstitutionTable.LigatureSet ( lstLIGs ) );
            lstLIGs = null;
        } else if ( "lig".equals(localName) ) {
            assert lstLIGs != null;
            assert ligGID >= 0;
            int[] ligComponents = parseLigatureComponents ( content );
            if ( ligComponents != null ) {
                lstLIGs.add ( new GlyphSubstitutionTable.Ligature ( ligGID, ligComponents ) );
            }
            ligGID = -1;
        }
    }

    private void initEntriesState ( int tableType, int lookupType, int subtableFormat ) {
        if ( tableType == GlyphTable.GLYPH_TABLE_TYPE_SUBSTITUTION ) {
            switch ( lookupType ) {
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_SINGLE:
                assert lstGIDs == null;
                lstGIDs = new java.util.ArrayList();
                break;
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_LIGATURE:
                assert lstLIGSets == null;
                lstLIGSets = new java.util.ArrayList();
                break;
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_MULTIPLE:
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_ALTERNATE:
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_CONTEXT:
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_CHAINING_CONTEXT:
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION:
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_REVERSE_CHAINING_CONTEXT_SINGLE:
                throw new UnsupportedOperationException();
            default:
                break;
            }
        } else if ( tableType == GlyphTable.GLYPH_TABLE_TYPE_POSITIONING ) {
            switch ( lookupType ) {
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_SINGLE:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_PAIR:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_CURSIVE:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_BASE:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_MARK:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_CONTEXT:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_CHAINED_CONTEXT:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_EXTENSION_POSITIONING:
                throw new UnsupportedOperationException();
            default:
                break;
            }
        }
    }

    private void finishEntriesState ( int tableType, int lookupType, int subtableFormat ) {
        if ( tableType == GlyphTable.GLYPH_TABLE_TYPE_SUBSTITUTION ) {
            switch ( lookupType ) {
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_SINGLE:
                assert lstGIDs != null;
                lstEntries = lstGIDs; lstGIDs = null;
                break;
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_LIGATURE:
                assert lstLIGSets != null;
                lstEntries = lstLIGSets; lstLIGSets = null;
                break;
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_MULTIPLE:
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_ALTERNATE:
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_CONTEXT:
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_CHAINING_CONTEXT:
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_EXTENSION_SUBSTITUTION:
            case GlyphSubstitutionTable.GSUB_LOOKUP_TYPE_REVERSE_CHAINING_CONTEXT_SINGLE:
                throw new UnsupportedOperationException();
            default:
                break;
            }
        } else if ( tableType == GlyphTable.GLYPH_TABLE_TYPE_POSITIONING ) {
            switch ( lookupType ) {
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_SINGLE:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_PAIR:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_CURSIVE:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_BASE:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_LIGATURE:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_MARK_TO_MARK:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_CONTEXT:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_CHAINED_CONTEXT:
            case GlyphPositioningTable.GPOS_LOOKUP_TYPE_EXTENSION_POSITIONING:
                throw new UnsupportedOperationException();
            default:
                break;
            }
        }
    }

    private void addLookupSubtable                              // CSOK: ParameterNumber
        ( int tableType, int lookupType, String lookupID, int lookupSequence, int lookupFlags, int subtableFormat, List coverage, List entries ) {
        GlyphSubtable st = null;
        if ( tableType == GlyphTable.GLYPH_TABLE_TYPE_SUBSTITUTION ) {
            st = GlyphSubstitutionTable.createSubtable ( lookupType, lookupID, lookupSequence, lookupFlags, subtableFormat, coverage, entries );
        } else if ( tableType == GlyphTable.GLYPH_TABLE_TYPE_POSITIONING ) {
            st = GlyphPositioningTable.createSubtable ( lookupType, lookupID, lookupSequence, lookupFlags, subtableFormat, coverage, entries );
        }
        if ( st != null ) {
            if ( ltSubtables == null ) {
                ltSubtables = new java.util.ArrayList();
            }
            ltSubtables.add ( st );
        }
    }

    private int[] parseLigatureComponents ( String s )
        throws SAXParseException {
        String[] csa = s.split ( "\\s" );
        if ( ( csa == null ) || ( csa.length == 0 ) ) {
            throw new SAXParseException ( "invalid <lig/> element, must specify at least one component", locator );
        } else {
            int nc = csa.length;
            int[] components = new int [ nc ];
            for ( int i = 0, n = nc; i < n; i++ ) {
                String cs = csa [ i ];
                int c;
                try {
                    c = Integer.parseInt ( cs );
                    if ( ( c < 0 ) || ( c > 65535 ) ) {
                        throw new SAXParseException ( "invalid component value (" + c + ") in <lig/> element, out of range", locator );
                    } else {
                        components [ i ] = c;
                    }
                } catch ( NumberFormatException e ) {
                    throw new SAXParseException ( "invalid component \"" + cs + "\" in <lig/> element, must be integer", locator );
                }
                
            }
            return components;
        }
    }

}


