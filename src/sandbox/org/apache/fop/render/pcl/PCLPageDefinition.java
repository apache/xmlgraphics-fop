/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.fop.render.pcl;

import java.util.Iterator;
import java.util.List;

import org.apache.fop.util.UnitConv;

/**
 * This class represents a page format with PCL-specific properties.
 */
public class PCLPageDefinition {

    private static List pageDefinitions;
    
    private String name;
    private long width; //in mpt
    private long height; //in mpt
    private int logicalPageXOffset; //in mpt
    private boolean landscape;
    
    static {
        createPageDefinitions();
    }
    
    public PCLPageDefinition(String name, long width, long height, int logicalPageXOffset) {
        this(name, width, height, logicalPageXOffset, false);
    }
    
    public PCLPageDefinition(String name, long width, long height, int logicalPageXOffset, 
            boolean landscape) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.logicalPageXOffset = logicalPageXOffset;
        this.landscape = landscape;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isLandscapeFormat() {
        return this.landscape;
    }
    
    public int getLogicalPageXOffset() {
        return this.logicalPageXOffset;
    }
    
    public boolean matches(long width, long height, int errorMargin) {
        return (Math.abs(this.width - width) < errorMargin) 
            && (Math.abs(this.height - height) < errorMargin);
    }
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        return getName();
    }

    public static PCLPageDefinition getPageDefinition(long width, long height, int errorMargin) {
        Iterator iter = pageDefinitions.iterator();
        while (iter.hasNext()) {
            PCLPageDefinition def = (PCLPageDefinition)iter.next();
            if (def.matches(width, height, errorMargin)) {
                return def;
            }
        }
        return null;
    }
    
    /**
     * Converts an offset values for logical pages to millipoints. The values are given as pixels
     * in a 300dpi environment.
     * @param offset the offset as given in the PCL 5 specification (under "Printable Area")
     * @return the converted value in millipoints
     */
    private static int convertLogicalPageXOffset(int offset) {
        return (int)Math.round(((double)offset) * 72000 / 300);
    }
    
    private static void createPageDefinitions() {
        pageDefinitions = new java.util.ArrayList();
        pageDefinitions.add(new PCLPageDefinition("Letter", 
                Math.round(UnitConv.in2mpt(8.5)), Math.round(UnitConv.in2mpt(11)),
                convertLogicalPageXOffset(75)));
        pageDefinitions.add(new PCLPageDefinition("Legal", 
                Math.round(UnitConv.in2mpt(8.5)), Math.round(UnitConv.in2mpt(14)),
                convertLogicalPageXOffset(75)));
        pageDefinitions.add(new PCLPageDefinition("Executive", 
                Math.round(UnitConv.in2mpt(7.25)), Math.round(UnitConv.in2mpt(10.5)),
                convertLogicalPageXOffset(75)));
        pageDefinitions.add(new PCLPageDefinition("Ledger", 
                Math.round(UnitConv.in2mpt(11)), Math.round(UnitConv.in2mpt(17)),
                convertLogicalPageXOffset(75)));
        pageDefinitions.add(new PCLPageDefinition("A4", 
                Math.round(UnitConv.mm2mpt(210)), Math.round(UnitConv.mm2mpt(297)),
                convertLogicalPageXOffset(71)));
        pageDefinitions.add(new PCLPageDefinition("A3", 
                Math.round(UnitConv.mm2mpt(297)), Math.round(UnitConv.mm2mpt(420)),
                convertLogicalPageXOffset(71)));

        //TODO Add envelope definitions
        
        pageDefinitions.add(new PCLPageDefinition("LetterL", 
                Math.round(UnitConv.in2mpt(11)), Math.round(UnitConv.in2mpt(8.5)),
                convertLogicalPageXOffset(60)));
        pageDefinitions.add(new PCLPageDefinition("LegalL", 
                Math.round(UnitConv.in2mpt(14)), Math.round(UnitConv.in2mpt(8.5)),
                convertLogicalPageXOffset(60)));
        pageDefinitions.add(new PCLPageDefinition("ExecutiveL", 
                Math.round(UnitConv.in2mpt(10.5)), Math.round(UnitConv.in2mpt(7.25)),
                convertLogicalPageXOffset(60)));
        pageDefinitions.add(new PCLPageDefinition("LedgerL", 
                Math.round(UnitConv.in2mpt(17)), Math.round(UnitConv.in2mpt(11)),
                convertLogicalPageXOffset(60)));
        pageDefinitions.add(new PCLPageDefinition("A4L", 
                Math.round(UnitConv.mm2mpt(297)), Math.round(UnitConv.mm2mpt(210)),
                convertLogicalPageXOffset(59), true));
        pageDefinitions.add(new PCLPageDefinition("A3L", 
                Math.round(UnitConv.mm2mpt(420)), Math.round(UnitConv.mm2mpt(297)),
                convertLogicalPageXOffset(59)));
    }

    
}
