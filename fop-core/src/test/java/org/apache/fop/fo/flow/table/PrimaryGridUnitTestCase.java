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

package org.apache.fop.fo.flow.table;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.properties.LengthPairProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;

/**
 * Tests that the fox:header property is correctly parsed and set up at the FO tree level.
 */
public class PrimaryGridUnitTestCase {

    private static final int WIDTH = 10;

    @Test
    public void testContentLength() {
        TableCell tableCell = mock(TableCell.class);
        Table table = mock(Table.class);
        LengthPairProperty lengthPair = mock(LengthPairProperty.class);
        Property property = mock(Property.class);
        Length length = mock(Length.class);

        when(length.getValue()).thenReturn(1);
        when(property.getLength()).thenReturn(length);
        when(lengthPair.getBPD()).thenReturn(property);
        when(table.getBorderSeparation()).thenReturn(lengthPair);
        when(tableCell.getTable()).thenReturn(table);
        when(table.isSeparateBorderModel()).thenReturn(true);

        int size = 3;
        PrimaryGridUnit unit = new PrimaryGridUnit(tableCell, 0);
        unit.setElements(createKnuthElementList(size));

        assertEquals("Content length must be the sum of the widths",
                WIDTH * size, unit.getContentLength());

        size = 4;
        unit.setElements(createKnuthElementList(size));

        assertEquals("ContentLength must be updated after adding new elements",
                WIDTH * size, unit.getContentLength());
    }

    private List<KnuthElement> createKnuthElementList(int size) {
        List<KnuthElement> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new KnuthBox(WIDTH, null, false));
        }

        return list;
    }
}
