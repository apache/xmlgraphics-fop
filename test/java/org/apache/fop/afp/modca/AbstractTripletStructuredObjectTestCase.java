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

package org.apache.fop.afp.modca;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.fop.afp.modca.triplets.AbstractTriplet;
import org.apache.fop.afp.modca.triplets.AttributeQualifierTriplet;
import org.apache.fop.afp.modca.triplets.CommentTriplet;
import org.apache.fop.afp.modca.triplets.ObjectAreaSizeTriplet;
import org.apache.fop.afp.modca.triplets.Triplet;

/**
 * Test {@link AbstractTripletStructuredObject}
 */
public abstract class AbstractTripletStructuredObjectTestCase<S extends AbstractTripletStructuredObject>
        extends AbstractStructuredObjectTestCase<AbstractTripletStructuredObject> {

    private static final List<AbstractTriplet> TRIPLETS;

    static {
        List<AbstractTriplet> triplets = new ArrayList<AbstractTriplet>();

        triplets.add(new CommentTriplet((byte) 0x01, "test comment"));

        triplets.add(new AttributeQualifierTriplet(1, 1));

        triplets.add(new ObjectAreaSizeTriplet(10, 20));

        TRIPLETS = Collections.unmodifiableList(triplets);
    }

    private AbstractTripletStructuredObject emptyStructuredObject
            = new AbstractTripletStructuredObject() { };

    @Override
    public void setUp() throws Exception {
        super.setUp();

        AbstractTripletStructuredObject sut = getSut();

        for (AbstractTriplet triplet : TRIPLETS) {
            sut.addTriplet(triplet);
        }
    }


    /**
     * Test getTripletLength() - ensure a sum of all enclosing object lengths is returned.
     */
    public void testGetTripletLength() {

        int dataLength = 0;
        for (Triplet t : TRIPLETS) {
            dataLength += t.getDataLength();
        }
        assertEquals(dataLength, getSut().getTripletDataLength());
        assertEquals(0, emptyStructuredObject.getTripletDataLength());
    }

    /**
     * Test hasTriplets()
     */
    public void testHasTriplets() {
        assertTrue(getSut().hasTriplets());
        assertFalse(emptyStructuredObject.hasTriplets());
    }

    /**
     * Test writeTriplets() - Ensure the triplets are written properly.
     *
     * @throws IOException -
     */
    public void testWriteObjects() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (AbstractTriplet triplet : TRIPLETS) {
            triplet.writeToStream(baos);
        }
        byte[] expected = baos.toByteArray();
        baos.reset();
        getSut().writeTriplets(baos);
        assertTrue(Arrays.equals(expected, baos.toByteArray()));

        baos.reset();
        // Ensure it doesn't die if no data has been added
        emptyStructuredObject.writeTriplets(baos);
        byte[] emptyArray = baos.toByteArray();
        assertTrue(Arrays.equals(emptyArray, new byte[0]));
    }

    /**
     * Test hasTriplet() - ensure both positive and negative values are returned.
     */
    public void testHasTriplet() {
        for (AbstractTriplet triplet : TRIPLETS) {
            assertTrue(getSut().hasTriplet(triplet.getId()));
            assertFalse(emptyStructuredObject.hasTriplet(triplet.getId()));
        }
        CommentTriplet notInSystem = new CommentTriplet((byte) 0x30, "This should return false");
        assertFalse(getSut().hasTriplet(notInSystem.getId()));
    }

    /**
     * Test addTriplet() - mostly tested above, but check boundary cases
     */
    public void testAddTriplet() {
        // ensure null doesn't kill it... not sure what else to test
        getSut().addTriplet(null);
    }

    /**
     * Test addTriplets() - ensure all triplets are added.
     */
    public void testAddTriplets() {
        // Tested on empty object
        List<AbstractTriplet> expectedList = TRIPLETS;
        emptyStructuredObject.addTriplets(expectedList);
        // checks equals() on each member of both lists
        assertEquals(expectedList, emptyStructuredObject.getTriplets());

        // Add a list to an already populated list
        getSut().addTriplets(expectedList);

        List<AbstractTriplet> newExpected = new ArrayList<AbstractTriplet>(expectedList);
        newExpected.addAll(expectedList);
        assertEquals(newExpected, getSut().getTriplets());

        // Ensure null doesn't throw exception
        emptyStructuredObject.addTriplets(null);
    }

}