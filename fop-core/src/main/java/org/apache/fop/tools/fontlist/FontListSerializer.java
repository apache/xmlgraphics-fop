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

package org.apache.fop.tools.fontlist;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.util.GenerationHelperContentHandler;

/**
 * Turns the font list into SAX events.
 */
public class FontListSerializer {

    private static final String FONTS = "fonts";
    private static final String FAMILY = "family";
    private static final String FONT = "font";
    private static final String TRIPLETS = "triplets";
    private static final String TRIPLET = "triplet";

    private static final String NAME = "name";
    private static final String STRIPPED_NAME = "stripped-name";
    private static final String TYPE = "type";
    private static final String KEY = "key";
    private static final String STYLE = "style";
    private static final String WEIGHT = "weight";

    private static final String CDATA = "CDATA";

    /**
     * Generates SAX events from the font damily map.
     * @param fontFamilies the font families
     * @param handler the target SAX handler
     * @throws SAXException if an XML-related exception occurs
     */
    public void generateSAX(SortedMap fontFamilies,
            GenerationHelperContentHandler handler) throws SAXException {
        generateSAX(fontFamilies, null, handler);
    }

    /**
     * Generates SAX events from the font damily map.
     * @param fontFamilies the font families
     * @param singleFamily if not null, the output will be filtered so only this single font family
     *                          will be used
     * @param handler the target SAX handler
     * @throws SAXException if an XML-related exception occurs
     */
    public void generateSAX(SortedMap fontFamilies, String singleFamily,
            GenerationHelperContentHandler handler) throws SAXException {
        handler.startDocument();
        AttributesImpl atts = new AttributesImpl();
        handler.startElement(FONTS, atts);

        for (Object o : fontFamilies.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String familyName = (String) entry.getKey();
            if (singleFamily != null && !singleFamily.equals(familyName)) {
                continue;
            }
            atts.clear();
            atts.addAttribute("", NAME, NAME, CDATA, familyName);
            atts.addAttribute("", STRIPPED_NAME, STRIPPED_NAME, CDATA,
                    stripQuotes(familyName));
            handler.startElement(FAMILY, atts);

            List containers = (List) entry.getValue();
            generateXMLForFontContainers(handler, containers);
            handler.endElement(FAMILY);
        }

        handler.endElement(FONTS);
        handler.endDocument();
    }

    private final Pattern quotePattern = Pattern.compile("'");

    private String stripQuotes(String name) {
        return quotePattern.matcher(name).replaceAll("");
    }

    private void generateXMLForFontContainers(GenerationHelperContentHandler handler,
            List containers) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        for (Object container : containers) {
            FontSpec cont = (FontSpec) container;
            atts.clear();
            atts.addAttribute("", KEY, KEY, CDATA, cont.getKey());
            atts.addAttribute("", TYPE, TYPE, CDATA,
                    cont.getFontMetrics().getFontType().getName());
            handler.startElement(FONT, atts);
            generateXMLForTriplets(handler, cont.getTriplets());
            handler.endElement(FONT);
        }
    }

    private void generateXMLForTriplets(GenerationHelperContentHandler handler, Collection triplets)
                throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.clear();
        handler.startElement(TRIPLETS, atts);
        for (Object triplet1 : triplets) {
            FontTriplet triplet = (FontTriplet) triplet1;
            atts.clear();
            atts.addAttribute("", NAME, NAME, CDATA, triplet.getName());
            atts.addAttribute("", STYLE, STYLE, CDATA, triplet.getStyle());
            atts.addAttribute("", WEIGHT, WEIGHT, CDATA,
                    Integer.toString(triplet.getWeight()));
            handler.element(TRIPLET, atts);
        }
        handler.endElement(TRIPLETS);
    }

}
