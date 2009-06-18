/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 
package org.apache.fop.pdf;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.axsl.fontR.FontConsumer;
import org.axsl.fontR.FontUse;

/**
 * Class that stores a mapping of each FontUse to a corresponding internal name
 * (F1, F2...), used to refer to a font in the PDF file.
 */
public class FontMap  {
    
    /**
     * The Map iself. Keys are FontUse instances, values are String (internal
     * name).
     */
    private Map fontMap;
    
    /**
     * Font consumer associated to this map.
     */
    private FontConsumer fontConsumer;
    
    
    /**
     * Builds a font mapping of the FontUses available from the font server
     * associated to the given font consumer.
     * 
     * @param fontConsumer font consumer from which to get the FontUses
     */
    public FontMap(FontConsumer fontConsumer) {
        this.fontConsumer = fontConsumer;
        this.fontMap = new Hashtable();
    }

    /**
     * Builds a font mapping of (possibly all) the FontUses available from the
     * font server associated to the given font consumer.
     * 
     * @param fontConsumer
     *            font consumer from which to get the FontUses
     * @param registerAllFonts
     *            if <code>true</code>, all of the FontUses available from
     *            the font server are registered (useful for poscript output).
     *            Otherwise font uses are only registered when needed. This is
     *            the default.
     */
    public FontMap(FontConsumer fontConsumer, boolean registerAllFonts) {
        this.fontConsumer = fontConsumer;
        if (!registerAllFonts) {
            this.fontMap = new Hashtable();
        } else {
        	// TODO vh: re-enable
//            FontUse[] fontUses = fontConsumer.getFontServer().getAllFontUses(true, false);
//            fontMap = new Hashtable(fontUses.length);
//            for (int i = 0; i < fontUses.length; i++) {
//                fontMap.put(fontUses[i], "F" + new Integer(i));    
//            }        
        }
    }
    
    /**
     * Returns the font consumer associated to this font map.
     * @return the font consumer
     */
    public FontConsumer getFontConsumer() {
        return fontConsumer;
    }

    /**
     * Returns the internal name associated to the given FontUse instance.
     * @param fontUse a FontUse
     * @return the corresponding internal name
     */
    public String getInternalName(FontUse fontUse) {
        String internalName;
        if (!fontMap.containsKey(fontUse)) {
            internalName = "F" + (fontMap.size() + 1);
            fontMap.put(fontUse, internalName);
        } else {
            internalName = (String) fontMap.get(fontUse); 
        }
        return internalName;
    }
    
    /**
     * Returns the number of registered font uses.
     * @return number of mappings
     */
    public int getSize() {
        return fontMap.size();
    }
    
    /**
     * Returns the mappings contained in this font map. Each element in the
     * returned set is a Map.Entry, in which the key is a FontUse and the value
     * its corresponding internal name.
     * 
     * @return a set of the mappings contained in this font map.
     */
    public Set getMappings() {
        return fontMap.entrySet();
    }
}
