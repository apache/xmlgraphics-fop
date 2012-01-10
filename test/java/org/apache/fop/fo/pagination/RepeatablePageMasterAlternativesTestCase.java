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

package org.apache.fop.fo.pagination;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.NumericProperty;
import org.apache.fop.fo.properties.Property;



/**
 * Unit Test for RepeatablePageMasterAlternatives
 *
 */
public class RepeatablePageMasterAlternativesTestCase
implements Constants {

    /**
     *
     * @throws Exception exception
     */
    @Test
    public void testIsInfinite1() throws Exception {
        //  Create fixture
        Property maximumRepeats = mock(Property.class);
        ConditionalPageMasterReference cpmr = createCPMR("empty");

        when(maximumRepeats.getEnum()).thenReturn(EN_NO_LIMIT);

        RepeatablePageMasterAlternatives objectUnderTest
        = createRepeatablePageMasterAlternatives(cpmr, maximumRepeats);

        assertTrue("is infinite", objectUnderTest.isInfinite());
    }

    /**
     *
     * @throws Exception exception
     */
    @Test
    public void testIsInfinite2() throws Exception {
        //  Create fixture
        Property maximumRepeats = mock(Property.class);
        ConditionalPageMasterReference cpmr = createCPMR("empty");

        NumericProperty numericProperty = mock(NumericProperty.class);

        final int maxRepeatNum = 0;
        assertTrue(maxRepeatNum != EN_NO_LIMIT);

        when(maximumRepeats.getEnum()).thenReturn(maxRepeatNum);
        when(maximumRepeats.getNumeric()).thenReturn(numericProperty);

        RepeatablePageMasterAlternatives objectUnderTest
        = createRepeatablePageMasterAlternatives(createCPMR("empty"),
                maximumRepeats);

        assertTrue("is infinite", !objectUnderTest.isInfinite());
    }

    /**
     * Test that an infinite sequence of empty page masters has
     * willTerminiate() returning false
     * @throws Exception exception
     */
    @Test
    public void testCanProcess1() throws Exception {
        //  Create fixture
        Property maximumRepeats = mock(Property.class);
        ConditionalPageMasterReference cpmr = createCPMR("empty");

        when(maximumRepeats.getEnum()).thenReturn(EN_NO_LIMIT);
        when(cpmr.isValid(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
        .thenReturn(true);

        RepeatablePageMasterAlternatives objectUnderTest
        = createRepeatablePageMasterAlternatives(cpmr, maximumRepeats);

        //Fixture assertion
        assertTrue("Should be infinite", objectUnderTest.isInfinite());

        //Test assertion
        assertTrue("Infinite sequences that do not process the main flow will "
                + " not terminate",
                !objectUnderTest.canProcess("main-flow"));
    }
    /**
     * Test that a finite sequence of simple page masters has
     * willTerminate() returning true
     *
     * @throws Exception exception
     */
    @Test
    public void testCanProcess2() throws Exception {
        //  Create fixture
        Property maximumRepeats = mock(Property.class);
        NumericProperty numericProperty = mock(NumericProperty.class);

        final int maxRepeatNum = 0;

        when(maximumRepeats.getEnum()).thenReturn(maxRepeatNum);
        when(maximumRepeats.getNumeric()).thenReturn(numericProperty);

        RepeatablePageMasterAlternatives objectUnderTest
        = createRepeatablePageMasterAlternatives(createCPMR("empty"),
                maximumRepeats);

        //Fixture assertion
        assertTrue("Should be finite sequence", !objectUnderTest.isInfinite());

        //Test assertion
        assertTrue("Finite sequences will terminate",
                objectUnderTest.canProcess("main-flow"));
    }

    private ConditionalPageMasterReference createCPMR(String regionName) {
        ConditionalPageMasterReference cpmr = mock(ConditionalPageMasterReference.class);
        SimplePageMaster master = mock(SimplePageMaster.class);
        Region region = mock(Region.class);
        when(master.getRegion(anyInt())).thenReturn(region);
        when(region.getRegionName()).thenReturn(regionName);
        when(cpmr.getMaster()).thenReturn(master);

        return cpmr;
    }

    private RepeatablePageMasterAlternatives createRepeatablePageMasterAlternatives(
            ConditionalPageMasterReference cpmr, Property maximumRepeats) throws Exception {

        PropertyList pList = mock(PropertyList.class);

        when(pList.get(anyInt())).thenReturn(maximumRepeats);

        PageSequenceMaster parent = mock(PageSequenceMaster.class);

        RepeatablePageMasterAlternatives sut = new RepeatablePageMasterAlternatives(parent);

        sut.startOfNode();
        sut.bind(pList);
        sut.addConditionalPageMasterReference(cpmr);
        return sut;
    }

}

