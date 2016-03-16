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

package org.apache.fop.text.linebreak;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO add javadoc
 *
 *
 */
public class LineBreakUtilsTestCase {

    @Test
    public void testLineBreakProperty() {
        assertEquals(LineBreakUtils.getLineBreakProperty('A'), LineBreakUtils.LINE_BREAK_PROPERTY_AL);
        assertEquals(LineBreakUtils.getLineBreakProperty('1'), LineBreakUtils.LINE_BREAK_PROPERTY_NU);
        assertEquals(LineBreakUtils.getLineBreakProperty('\n'), LineBreakUtils.LINE_BREAK_PROPERTY_LF);
        assertEquals(LineBreakUtils.getLineBreakProperty('\r'), LineBreakUtils.LINE_BREAK_PROPERTY_CR);
        assertEquals(LineBreakUtils.getLineBreakProperty('('), LineBreakUtils.LINE_BREAK_PROPERTY_OP);
        assertEquals(LineBreakUtils.getLineBreakProperty('\u1F7E'), 0);
    }

    @Test
    public void testLineBreakPair() {
        assertEquals(
            LineBreakUtils.getLineBreakPairProperty(
                LineBreakUtils.LINE_BREAK_PROPERTY_CM,
                LineBreakUtils.LINE_BREAK_PROPERTY_CL),
            LineBreakUtils.PROHIBITED_BREAK);
        assertEquals(
            LineBreakUtils.getLineBreakPairProperty(
                LineBreakUtils.LINE_BREAK_PROPERTY_CL,
                LineBreakUtils.LINE_BREAK_PROPERTY_CM),
            LineBreakUtils.COMBINING_INDIRECT_BREAK);
        assertEquals(
            LineBreakUtils.getLineBreakPairProperty(
                LineBreakUtils.LINE_BREAK_PROPERTY_IS,
                LineBreakUtils.LINE_BREAK_PROPERTY_PR),
            LineBreakUtils.DIRECT_BREAK);
        assertEquals(
            LineBreakUtils.getLineBreakPairProperty(
                LineBreakUtils.LINE_BREAK_PROPERTY_AL,
                LineBreakUtils.LINE_BREAK_PROPERTY_OP),
            LineBreakUtils.INDIRECT_BREAK);
        assertEquals(
            LineBreakUtils.getLineBreakPairProperty(
                LineBreakUtils.LINE_BREAK_PROPERTY_LF,
                LineBreakUtils.LINE_BREAK_PROPERTY_CM),
            0);
    }

}
