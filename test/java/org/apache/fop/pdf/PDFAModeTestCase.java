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

package org.apache.fop.pdf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PDFAModeTestCase {

    private static class PDFAModeChecker {

        private final PDFAMode mode;

        PDFAModeChecker(PDFAMode mode) {
            this.mode = mode;
        }

        PDFAModeChecker isEnabled() {
            assertTrue(mode.isEnabled());
            return this;
        }

        PDFAModeChecker isDisabled() {
            assertFalse(mode.isEnabled());
            return this;
        }

        PDFAModeChecker isPart1() {
            assertEquals(1, mode.getPart());
            assertTrue(mode.isPart1());
            return this;
        }

        PDFAModeChecker isNotPart1() {
            assertFalse(mode.getPart() == 1);
            assertFalse(mode.isPart1());
            return this;
        }

        PDFAModeChecker isPart2() {
            assertTrue(mode.getPart() == 1 || mode.getPart() == 2);
            assertTrue(mode.isPart2());
            return this;
        }

        PDFAModeChecker isNotPart2() {
            assertFalse(mode.getPart() == 2);
            assertFalse(mode.isPart2());
            return this;
        }

        PDFAModeChecker hasConformanceLevel(char level) {
            assertEquals(level, mode.getConformanceLevel());
            return this;
        }

        PDFAModeChecker isLevelA() {
            assertEquals('A', mode.getConformanceLevel());
            assertTrue(mode.isLevelA());
            return this;
        }

        PDFAModeChecker isNotLevelA() {
            assertFalse(mode.getConformanceLevel() == 'A');
            assertFalse(mode.isLevelA());
            return this;
        }
    }

    @Test
    public void checkDisabled() {
        new PDFAModeChecker(PDFAMode.DISABLED)
                .isDisabled()
                .isNotPart1()
                .isNotPart2()
                .isNotLevelA();
    }

    @Test
    public void checkPDFA_1a() {
        new PDFAModeChecker(PDFAMode.PDFA_1A)
                .isEnabled()
                .isPart1()
                .isPart2()
                .isLevelA();
    }

    @Test
    public void checkPDFA_1b() {
        new PDFAModeChecker(PDFAMode.PDFA_1B)
                .isEnabled()
                .isPart1()
                .isPart2()
                .isNotLevelA();
    }

    @Test
    public void checkPDFA_2a() {
        new PDFAModeChecker(PDFAMode.PDFA_2A)
                .isEnabled()
                .isNotPart1()
                .isPart2()
                .isLevelA();
    }

    @Test
    public void checkPDFA_2b() {
        new PDFAModeChecker(PDFAMode.PDFA_2B)
                .isEnabled()
                .isNotPart1()
                .isPart2()
                .isNotLevelA();
    }

    @Test
    public void checkPDFA_2u() {
        new PDFAModeChecker(PDFAMode.PDFA_2U)
                .isEnabled()
                .isNotPart1()
                .isPart2()
                .isNotLevelA()
                .hasConformanceLevel('U');
    }

}
