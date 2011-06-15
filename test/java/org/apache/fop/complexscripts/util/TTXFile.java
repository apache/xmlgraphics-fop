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

package org.apache.fop.complexscripts.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fonts.GlyphDefinitionTable;
import org.apache.fop.fonts.GlyphSubstitutionTable;
import org.apache.fop.fonts.GlyphPositioningTable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


// CSOFF: InnerAssignmentCheck
// CSOFF: LineLengthCheck
// CSOFF: NoWhitespaceAfterCheck

/**
 * This class supports a subset of the <code>TTX</code> file as produced by the Adobe FLEX
 * SDK (AFDKO). In particular, it is used to parse a <code>TTX</code> file in order to
 * extract character to glyph code mapping data, glyph definition data, glyph substitution
 * data, and glyph positioning data.
 *
 * <code>TTX</code> files are used in FOP for testing and debugging purposes only. Such
 * files are used to represent font data employed by complex script processing, and
 * normally extracted directly from an opentype (or truetype) file. However, due to
 * copyright restrictions, it is not possible to include most opentype (or truetype) font
 * files directly in the FOP distribution. In such cases, <code>TTX</code> files are used
 * to distribute a subset of the complex script advanced table information contained in
 * certain font files to facilitate testing. This can be done because no glyph outline or
 * other proprietary information is included in the distributed <code>TTX</code> files.
 *
 * @author Glenn Adams
 */
public class TTXFile {

    /** logging instance */
    private static final Log log = LogFactory.getLog(TTXFile.class);                                                    // CSOK: ConstantNameCheck

    public TTXFile() {
    }

    public void parse ( String filename ) {
        parse ( new File ( filename ) );
    }

    public void parse ( File f ) {
        assert f != null;
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            sp.parse ( f, new Handler() );
        } catch ( FactoryConfigurationError e ) {
            throw new RuntimeException ( e.getMessage() );
        } catch ( ParserConfigurationException e ) {
            throw new RuntimeException ( e.getMessage() );
        } catch ( SAXException e ) {
            throw new RuntimeException ( e.getMessage() );
        } catch ( IOException e ) {
            throw new RuntimeException ( e.getMessage() );
        }
    }

    private class Handler extends DefaultHandler {
        private Handler() {
        }
        @Override
        public void startDocument() {
        }
        @Override
        public void endDocument() {
        }
        @Override
        public void startElement ( String uri, String localName, String qName, Attributes attrs )
            throws SAXException {
            if ( qName.equals ( "Alternate" ) ) {
            } else if ( qName.equals ( "AlternateSet" ) ) {
            } else if ( qName.equals ( "AlternateSubst" ) ) {
            } else if ( qName.equals ( "BacktrackCoverage" ) ) {
            } else if ( qName.equals ( "BaseAnchor" ) ) {
            } else if ( qName.equals ( "BaseArray" ) ) {
            } else if ( qName.equals ( "BaseCoverage" ) ) {
            } else if ( qName.equals ( "BaseRecord" ) ) {
            } else if ( qName.equals ( "ChainContextPos" ) ) {
            } else if ( qName.equals ( "ChainContextSubst" ) ) {
            } else if ( qName.equals ( "Class" ) ) {
            } else if ( qName.equals ( "ClassDef" ) ) {
                // child of GlyphClassDef
                // child of MarkAttachClassDef
            } else if ( qName.equals ( "ComponentRecord" ) ) {
            } else if ( qName.equals ( "Coverage" ) ) {
                // child of LigCaretList
                // child of MultipleSubst
                // child of CursivePos
            } else if ( qName.equals ( "CursivePos" ) ) {
            } else if ( qName.equals ( "DefaultLangSys" ) ) {
            } else if ( qName.equals ( "EntryAnchor" ) ) {
            } else if ( qName.equals ( "EntryExitRecord" ) ) {
            } else if ( qName.equals ( "ExitAnchor" ) ) {
            } else if ( qName.equals ( "Feature" ) ) {
            } else if ( qName.equals ( "FeatureIndex" ) ) {
                // child of DefaultLangSys
                // child of LangSys
            } else if ( qName.equals ( "FeatureList" ) ) {
                // child of GSUB
                // child of GPOS
            } else if ( qName.equals ( "FeatureRecord" ) ) {
            } else if ( qName.equals ( "FeatureTag" ) ) {
            } else if ( qName.equals ( "GDEF" ) ) {
            } else if ( qName.equals ( "GPOS" ) ) {
            } else if ( qName.equals ( "GSUB" ) ) {
            } else if ( qName.equals ( "Glyph" ) ) {
                // child of Coverage
                // child of InputCoverage
                // child of LookAheadCoverage
                // child of BacktrackCoverage
                // child of MarkCoverage
                // child of Mark1Coverage
                // child of Mark2Coverage
                // child of BaseCoverage
                // child of LigatureCoverage
            } else if ( qName.equals ( "GlyphClassDef" ) ) {
            } else if ( qName.equals ( "GlyphID" ) ) {
            } else if ( qName.equals ( "GlyphOrder" ) ) {
            } else if ( qName.equals ( "InputCoverage" ) ) {
            } else if ( qName.equals ( "LangSys" ) ) {
            } else if ( qName.equals ( "LangSysRecord" ) ) {
            } else if ( qName.equals ( "LangSysTag" ) ) {
            } else if ( qName.equals ( "LigCaretList" ) ) {
            } else if ( qName.equals ( "Ligature" ) ) {
            } else if ( qName.equals ( "LigatureAnchor" ) ) {
            } else if ( qName.equals ( "LigatureArray" ) ) {
            } else if ( qName.equals ( "LigatureAttach" ) ) {
            } else if ( qName.equals ( "LigatureCoverage" ) ) {
            } else if ( qName.equals ( "LigatureSet" ) ) {
            } else if ( qName.equals ( "LigatureSubst" ) ) {
            } else if ( qName.equals ( "LookAheadCoverage" ) ) {
            } else if ( qName.equals ( "Lookup" ) ) {
            } else if ( qName.equals ( "LookupFlag" ) ) {
            } else if ( qName.equals ( "LookupList" ) ) {
                // child of GSUB
                // child of GPOS
            } else if ( qName.equals ( "LookupListIndex" ) ) {
                // child of Feature
                // child of SubstLookupRecord
            } else if ( qName.equals ( "LookupType" ) ) {
            } else if ( qName.equals ( "Mark1Array" ) ) {
            } else if ( qName.equals ( "Mark1Coverage" ) ) {
            } else if ( qName.equals ( "Mark2Anchor" ) ) {
            } else if ( qName.equals ( "Mark2Array" ) ) {
            } else if ( qName.equals ( "Mark2Coverage" ) ) {
            } else if ( qName.equals ( "Mark2Record" ) ) {
            } else if ( qName.equals ( "MarkAnchor" ) ) {
            } else if ( qName.equals ( "MarkArray" ) ) {
            } else if ( qName.equals ( "MarkAttachClassDef" ) ) {
            } else if ( qName.equals ( "MarkBasePos" ) ) {
            } else if ( qName.equals ( "MarkCoverage" ) ) {
            } else if ( qName.equals ( "MarkLigPos" ) ) {
            } else if ( qName.equals ( "MarkMarkPos" ) ) {
            } else if ( qName.equals ( "MarkRecord" ) ) {
            } else if ( qName.equals ( "MultipleSubst" ) ) {
            } else if ( qName.equals ( "PairPos" ) ) {
            } else if ( qName.equals ( "PairSet" ) ) {
            } else if ( qName.equals ( "PairValueRecord" ) ) {
            } else if ( qName.equals ( "PosLookupRecord" ) ) {
            } else if ( qName.equals ( "ReqFeatureIndex" ) ) {
                // child of DefaultLangSys
                // child of LangSys
            } else if ( qName.equals ( "Script" ) ) {
            } else if ( qName.equals ( "ScriptList" ) ) {
                // child of GSUB
                // child of GPOS
            } else if ( qName.equals ( "ScriptRecord" ) ) {
            } else if ( qName.equals ( "ScriptTag" ) ) {
            } else if ( qName.equals ( "SecondGlyph" ) ) {
            } else if ( qName.equals ( "Sequence" ) ) {
            } else if ( qName.equals ( "SequenceIndex" ) ) {
            } else if ( qName.equals ( "SinglePos" ) ) {
            } else if ( qName.equals ( "SingleSubst" ) ) {
            } else if ( qName.equals ( "SubstLookupRecord" ) ) {
            } else if ( qName.equals ( "Substitute" ) ) {
            } else if ( qName.equals ( "Substitution" ) ) {
            } else if ( qName.equals ( "Value" ) ) {
            } else if ( qName.equals ( "Value1" ) ) {
            } else if ( qName.equals ( "Value2" ) ) {
            } else if ( qName.equals ( "ValueFormat" ) ) {
            } else if ( qName.equals ( "ValueFormat1" ) ) {
            } else if ( qName.equals ( "ValueFormat2" ) ) {
            } else if ( qName.equals ( "Version" ) ) {
                // child of GDEF
                // child of GSUB
                // child of GPOS
            } else if ( qName.equals ( "XCoordinate" ) ) {
                // child of EntryAnchor
                // child of MarkAnchor
            } else if ( qName.equals ( "YCoordinate" ) ) {
                // child of EntryAnchor
                // child of MarkAnchor
                // child of BaseAnchor
            } else if ( qName.equals ( "checkSumAdjustment" ) ) {
            } else if ( qName.equals ( "cmap" ) ) {
            } else if ( qName.equals ( "cmap_format_0" ) ) {
            } else if ( qName.equals ( "cmap_format_4" ) ) {
            } else if ( qName.equals ( "created" ) ) {
            } else if ( qName.equals ( "flags" ) ) {
            } else if ( qName.equals ( "fontDirectionHint" ) ) {
            } else if ( qName.equals ( "fontRevision" ) ) {
            } else if ( qName.equals ( "glyphDataFormat" ) ) {
            } else if ( qName.equals ( "head" ) ) {
            } else if ( qName.equals ( "hmtx" ) ) {
            } else if ( qName.equals ( "indexToLocFormat" ) ) {
            } else if ( qName.equals ( "lowestRecPPEM" ) ) {
            } else if ( qName.equals ( "macStyle" ) ) {
            } else if ( qName.equals ( "magicNumber" ) ) {
            } else if ( qName.equals ( "map" ) ) {
            } else if ( qName.equals ( "modified" ) ) {
            } else if ( qName.equals ( "mtx" ) ) {
            } else if ( qName.equals ( "tableVersion" ) ) {
            } else if ( qName.equals ( "tableVersion" ) ) {
            } else if ( qName.equals ( "ttFont" ) ) {
            } else if ( qName.equals ( "unitsPerEm" ) ) {
            } else if ( qName.equals ( "xMax" ) ) {
            } else if ( qName.equals ( "xMin" ) ) {
            } else if ( qName.equals ( "yMax" ) ) {
            } else if ( qName.equals ( "yMin" ) ) {
            } else {
                throw new SAXException ( "unknown element type {" + uri + "," + qName + "}" );
            }
        }
        @Override
        public void endElement ( String uri, String localName, String qName ) {
        }
        @Override
        public void characters ( char[] chars, int start, int length ) {
        }
    }

}
