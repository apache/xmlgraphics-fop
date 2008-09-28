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

package org.apache.fop.render.pcl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import org.apache.xmlgraphics.util.UnitConv;

/**
 * This class represents a page format with PCL-specific properties.
 */
public class PCLPageDefinition {

    private static List pageDefinitions;
    private static PCLPageDefinition defaultPageDefinition;

    private final String name;
    private final int selector;
    private final Dimension physicalPageSize;
    private final Rectangle logicalPageRect;
    private final boolean landscape;

    static {
        createPageDefinitions();
    }

    /**
     * Main constructor
     * @param name the name of the page definition
     * @param selector the selector used by the <ESC>&l#A command (page size)
     * @param physicalPageSize the physical page size
     * @param logicalPageRect the rectangle defining the logical page
     * @param landscape true if it is a landscape format
     */
    public PCLPageDefinition(String name, int selector, Dimension physicalPageSize,
            Rectangle logicalPageRect, boolean landscape) {
        this.name = name;
        this.selector = selector;
        this.physicalPageSize = physicalPageSize;
        this.logicalPageRect = logicalPageRect;
        this.landscape = landscape;
    }

    /** @return the name of the page definition */
    public String getName() {
        return this.name;
    }

    /** @return the selector used by the <ESC>&l#A command (page size) */
    public int getSelector() {
        return this.selector;
    }

    /** @return true if it is a landscape format */
    public boolean isLandscapeFormat() {
        return this.landscape;
    }

    /** @return the physical page size */
    public Dimension getPhysicalPageSize() {
        return this.physicalPageSize;
    }

    /** @return the rectangle defining the logical page */
    public Rectangle getLogicalPageRect() {
        return this.logicalPageRect;
    }

    private boolean matches(long width, long height, int errorMargin) {
        return (Math.abs(this.physicalPageSize.width - width) < errorMargin)
            && (Math.abs(this.physicalPageSize.height - height) < errorMargin);
    }

    /** {@inheritDoc} */
    public String toString() {
        return getName();
    }

    /**
     * Tries to determine a matching page definition.
     * @param width the physical page width (in mpt)
     * @param height the physical page height (in mpt)
     * @param errorMargin the error margin for detecting the right page definition
     * @return the page definition or null if no match was found
     */
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

    /** @return the default page definition (letter) */
    public static PCLPageDefinition getDefaultPageDefinition() {
        return defaultPageDefinition;
    }

    /**
     * Converts an offset values for logical pages to millipoints. The values are given as pixels
     * in a 300dpi environment.
     * @param offset the offset as given in the PCL 5 specification (under "Printable Area")
     * @return the converted value in millipoints
     */
    private static int convert300dpiDotsToMpt(int offset) {
        return (int)Math.round(((double)offset) * 72000 / 300);
    }

    private static Dimension createPhysicalPageSizeInch(float width, float height) {
        return new Dimension(
                (int)Math.round(UnitConv.in2mpt(width)),
                (int)Math.round(UnitConv.in2mpt(height)));
    }

    private static Dimension createPhysicalPageSizeMm(float width, float height) {
        return new Dimension(
                (int)Math.round(UnitConv.mm2mpt(width)),
                (int)Math.round(UnitConv.mm2mpt(height)));
    }

    private static Rectangle createLogicalPageRect(int x, int y, int width, int height) {
        return new Rectangle(convert300dpiDotsToMpt(x), convert300dpiDotsToMpt(y),
                convert300dpiDotsToMpt(width), convert300dpiDotsToMpt(height));
    }

    private static void createPageDefinitions() {
        pageDefinitions = new java.util.ArrayList();
        pageDefinitions.add(new PCLPageDefinition("Letter", 2,
                createPhysicalPageSizeInch(8.5f, 11),
                createLogicalPageRect(75, 0, 2400, 3300), false));
        defaultPageDefinition = new PCLPageDefinition("Legal", 3,
                createPhysicalPageSizeInch(8.5f, 14),
                createLogicalPageRect(75, 0, 2400, 4200), false);
        pageDefinitions.add(defaultPageDefinition);
        pageDefinitions.add(new PCLPageDefinition("Executive", 1,
                createPhysicalPageSizeInch(7.25f, 10.5f),
                createLogicalPageRect(75, 0, 2025, 3150), false));
        pageDefinitions.add(new PCLPageDefinition("Ledger", 6,
                createPhysicalPageSizeInch(11, 17),
                createLogicalPageRect(75, 0, 3150, 5100), false));
        pageDefinitions.add(new PCLPageDefinition("A4", 26,
                createPhysicalPageSizeMm(210, 297),
                createLogicalPageRect(71, 0, 2338, 3507), false));
        pageDefinitions.add(new PCLPageDefinition("A3", 27,
                createPhysicalPageSizeMm(297, 420),
                createLogicalPageRect(71, 0, 3365, 4960), false));

        //TODO Add envelope definitions

        pageDefinitions.add(new PCLPageDefinition("LetterL", 2,
                createPhysicalPageSizeInch(11, 8.5f),
                createLogicalPageRect(60, 0, 3180, 2550), true));
        pageDefinitions.add(new PCLPageDefinition("LegalL", 3,
                createPhysicalPageSizeInch(14, 8.5f),
                createLogicalPageRect(60, 0, 4080, 2550), true));
        pageDefinitions.add(new PCLPageDefinition("ExecutiveL", 1,
                createPhysicalPageSizeInch(10.5f, 7.25f),
                createLogicalPageRect(60, 0, 3030, 2175), true));
        pageDefinitions.add(new PCLPageDefinition("LedgerL", 6,
                createPhysicalPageSizeInch(17, 11),
                createLogicalPageRect(60, 0, 4980, 3300), true));
        pageDefinitions.add(new PCLPageDefinition("A4L", 26,
                createPhysicalPageSizeMm(297, 210),
                createLogicalPageRect(59, 0, 3389, 2480), true));
        pageDefinitions.add(new PCLPageDefinition("A3L", 27,
                createPhysicalPageSizeMm(420, 297),
                createLogicalPageRect(59, 0, 4842, 3507), true));
    }

}
