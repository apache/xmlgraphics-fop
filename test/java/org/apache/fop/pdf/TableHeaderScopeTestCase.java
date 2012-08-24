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
import org.mockito.ArgumentMatcher;
import org.mockito.verification.VerificationMode;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.fop.pdf.StandardStructureAttributes.Table.Scope;
import org.apache.fop.pdf.StandardStructureTypes.Table;

public class TableHeaderScopeTestCase {

    private static final String ATTRIBUTE_ENTRY = "A";

    private VersionController controller;

    @Test
    public void pdfDocumentDelegatesToVersionController() {
        for (Scope scope : Scope.values()) {
            testMakeStructureElementWithScope(scope);
        }
    }

    private void testMakeStructureElementWithScope(Scope scope) {
        VersionController controller = mock(VersionController.class);
        PDFDocument document = new PDFDocument("Test", controller);
        document.makeStructTreeRoot(null);
        PDFStructElem th = new PDFStructElem(null, Table.TH);
        document.registerStructureElement(th, scope);
        verify(controller).addTableHeaderScopeAttribute(any(PDFStructElem.class), eq(scope));
    }

    @Test
    public void versionControllerMayDelegateToScope() {
        fixedController14doesNotAddAttribute();
        fixedController15addsAttribute();
        dynamicControllerAddsAttribute();
    }

    private void fixedController14doesNotAddAttribute() {
        controller = VersionController.getFixedVersionController(Version.V1_4);
        scopeMustNotBeAdded();
    }

    private void fixedController15addsAttribute() {
        controller = VersionController.getFixedVersionController(Version.V1_5);
        scopeMustBeAdded();
    }

    private void dynamicControllerAddsAttribute() {
        PDFDocument document = new PDFDocument("Test");
        controller = VersionController.getDynamicVersionController(Version.V1_4, document);
        scopeMustBeAdded();
        assertEquals(Version.V1_5, controller.getPDFVersion());
    }

    private void scopeMustBeAdded() {
        scopeMustBeAdded(times(1));
    }

    private void scopeMustNotBeAdded() {
        scopeMustBeAdded(never());
    }

    private void scopeMustBeAdded(VerificationMode nTimes) {
        PDFStructElem structElem = mock(PDFStructElem.class);
        controller.addTableHeaderScopeAttribute(structElem, Scope.COLUMN);
        verify(structElem, nTimes).put(eq(ATTRIBUTE_ENTRY), any());
    }

    @Test
    public void scopeAddsTheAttribute() {
        for (Scope scope : Scope.values()) {
            scopeAttributeMustBeAdded(scope);
        }
    }

    private void scopeAttributeMustBeAdded(Scope scope) {
        PDFStructElem structElem = mock(PDFStructElem.class);
        Scope.addScopeAttribute(structElem, scope);
        verify(structElem).put(eq(ATTRIBUTE_ENTRY), scopeAttribute(scope));
    }

    private PDFDictionary scopeAttribute(Scope scope) {
        return argThat(new isScopeAttribute(scope));
    }

    private static class isScopeAttribute extends ArgumentMatcher<PDFDictionary> {

        private final Scope expectedScope;

        public isScopeAttribute(Scope expectedScope) {
            this.expectedScope = expectedScope;
        }

        @Override
        public boolean matches(Object argument) {
            PDFDictionary attribute = (PDFDictionary) argument;
            return "/Table".equals(attribute.get("O").toString())
                    && expectedScope.getName().toString().equals(attribute.get("Scope").toString());
        }

    }

}
