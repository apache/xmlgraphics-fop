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

package org.apache.fop.util;

import java.util.LinkedList;

import org.apache.fop.layoutmgr.ElementListUtils;
import org.apache.fop.layoutmgr.KnuthBox;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;

import junit.framework.TestCase;

/**
 * Test class for ElementListUtils.
 */
public class ElementListUtilsTestCase extends TestCase {

    /**
     * Tests ElementListUtils.removeLegalBreaks().
     * @throws Exception if the test fails
     */
    public void testRemoveElementPenalty1() throws Exception {
        LinkedList lst = new LinkedList();
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, 0, false, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, 200, false, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, 0, false, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, null, false));
        lst.add(new KnuthGlue(0, Integer.MAX_VALUE, 0, null, false));
        lst.add(new KnuthPenalty(0, -KnuthElement.INFINITE, false, null, false));

        boolean res = ElementListUtils.removeLegalBreaks(lst, 9000);

        assertFalse(res);

        assertEquals(KnuthElement.INFINITE, ((KnuthPenalty)lst.get(1)).getPenalty());
        assertEquals(KnuthElement.INFINITE, ((KnuthPenalty)lst.get(3)).getPenalty());
        assertEquals(0, ((KnuthPenalty)lst.get(5)).getPenalty());
    }

    /**
     * Tests ElementListUtils.removeLegalBreaks().
     * @throws Exception if the test fails
     */
    public void testRemoveElementPenalty2() throws Exception {
        LinkedList lst = new LinkedList();
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthGlue(0, 0, 0, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthGlue(0, 0, 0, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthGlue(0, 0, 0, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, null, false));
        lst.add(new KnuthGlue(0, Integer.MAX_VALUE, 0, null, false));
        lst.add(new KnuthPenalty(0, -KnuthElement.INFINITE, false, null, false));

        boolean res = ElementListUtils.removeLegalBreaks(lst, 9000);

        assertFalse(res);

        //Must insert an INFINITE penalty
        assertEquals(KnuthElement.INFINITE, ((KnuthPenalty)lst.get(1)).getPenalty());
        assertEquals(0, ((KnuthGlue)lst.get(2)).getWidth());
        assertEquals(KnuthElement.INFINITE, ((KnuthPenalty)lst.get(4)).getPenalty());
        assertEquals(0, ((KnuthGlue)lst.get(5)).getWidth());
        assertEquals(0, ((KnuthGlue)lst.get(7)).getWidth());
    }

    /**
     * Tests ElementListUtils.removeLegalBreaksFromEnd().
     * @throws Exception if the test fails
     */
    public void testRemoveElementFromEndPenalty1() throws Exception {
        LinkedList lst = new LinkedList();
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, 0, false, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, 200, false, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, 0, false, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, null, false));
        lst.add(new KnuthGlue(0, Integer.MAX_VALUE, 0, null, false));
        lst.add(new KnuthPenalty(0, -KnuthElement.INFINITE, false, null, false));

        boolean res = ElementListUtils.removeLegalBreaksFromEnd(lst, 9000);

        assertFalse(res);

        assertEquals(0, ((KnuthPenalty)lst.get(1)).getPenalty());
        assertEquals(KnuthElement.INFINITE, ((KnuthPenalty)lst.get(3)).getPenalty());
        assertEquals(KnuthElement.INFINITE, ((KnuthPenalty)lst.get(5)).getPenalty());
    }

    /**
     * Tests ElementListUtils.removeLegalBreaksFromEnd().
     * @throws Exception if the test fails
     */
    public void testRemoveElementFromEndPenalty2() throws Exception {
        LinkedList lst = new LinkedList();
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, 0, false, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, 200, false, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthGlue(0, 0, 0, null, false));
        lst.add(new KnuthBox(4000, null, false));
        lst.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, null, false));
        lst.add(new KnuthGlue(0, Integer.MAX_VALUE, 0, null, false));
        lst.add(new KnuthPenalty(0, -KnuthElement.INFINITE, false, null, false));

        boolean res = ElementListUtils.removeLegalBreaksFromEnd(lst, 9000);

        assertFalse(res);

        //Must insert an INFINITE penalty
        assertEquals(0, ((KnuthPenalty)lst.get(1)).getPenalty());
        assertEquals(KnuthElement.INFINITE, ((KnuthPenalty)lst.get(3)).getPenalty());
        assertEquals(KnuthElement.INFINITE, ((KnuthPenalty)lst.get(5)).getPenalty());
        assertEquals(0, ((KnuthGlue)lst.get(6)).getWidth());
    }


}