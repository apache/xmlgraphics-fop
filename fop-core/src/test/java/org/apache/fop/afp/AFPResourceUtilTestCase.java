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

package org.apache.fop.afp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

import org.apache.commons.io.IOUtils;

import org.apache.fop.afp.util.AFPResourceUtil;

/**
 * Tests the {@link AFPResourceUtil} class.
 */
public class AFPResourceUtilTestCase {

    private static final String RESOURCE_FILENAME = "expected_resource.afp";
    private static final String NAMED_RESOURCE_FILENAME = "expected_named_resource.afp";

    private static final String RESOURCE_ANY_NAME = "resource_any_name.afp";
    private static final String RESOURCE_NAME_MATCH = "resource_name_match.afp";
    private static final String RESOURCE_NAME_MISMATCH = "resource_name_mismatch.afp";
    private static final String RESOURCE_NO_END_NAME = "resource_no_end_name.afp";

    private static final String PSEG_A = "XFEATHER";
    private static final String PSEG_B = "S1CODEQR";

    /**
     * Tests copyResourceFile()
     * @throws Exception -
     */
    @Test
    public void testCopyResourceFile() throws Exception {
        compareResources(new ResourceCopier() {
            public void copy(InputStream in, OutputStream out) throws IOException {
                AFPResourceUtil.copyResourceFile(in, out);
            }
        }, RESOURCE_FILENAME, RESOURCE_FILENAME);
    }

    /**
     * Tests copyNamedResource()
     * @throws Exception -
     */
    @Test
    public void testCopyNamedResource() throws Exception {
        compareResources(new ResourceCopier() {
            public void copy(InputStream in, OutputStream out) throws IOException {
                AFPResourceUtil.copyNamedResource(PSEG_A, in, out);
            }
        }, RESOURCE_FILENAME, NAMED_RESOURCE_FILENAME);
    }

    private void compareResources(ResourceCopier copyResource, String resourceA, String resourceB)
            throws IOException {
        ByteArrayOutputStream baos = copyResource(resourceA, copyResource);
        byte[] expectedBytes = resourceAsByteArray(resourceB);
        assertTrue(Arrays.equals(expectedBytes, baos.toByteArray()));
    }

    private ByteArrayOutputStream copyResource(String resource, ResourceCopier resourceCopier)
            throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream(resource);
            resourceCopier.copy(in, baos);
        } finally {
            in.close();
        }
        return baos;
    }

    private byte[] resourceAsByteArray(String resource) throws IOException {
        InputStream in = null;
        byte[] expectedBytes = null;
        try {
            in = getClass().getResourceAsStream(resource);
            expectedBytes = IOUtils.toByteArray(in);
        } finally {
            in.close();
        }
        return expectedBytes;
    }

    /**
     * Tests the validity of a closing structured field having an FF FF name which
     * allows it to match any existing matching starting field
     * @throws Exception -
     */
    @Test
    public void testResourceAnyName() throws Exception {
        testResource(RESOURCE_ANY_NAME, PSEG_B);
    }

    /**
     * Tests a matching end structured field name
     * @throws Exception -
     */
    @Test
    public void testResourceNameMatch() throws Exception {
        testResource(RESOURCE_NAME_MATCH, PSEG_B);
    }

    /**
     * Tests to see whether a matching structured field pair with mismatching
     * names fails.
     * @throws Exception -
     */
    @Test(expected = Exception.class)
    public void testResourceNameMismatch() throws Exception {
        testResource(RESOURCE_NAME_MISMATCH, PSEG_B);
    }

    /**
     * Tests a matching structured end field with no name
     * @throws Exception -
     */
    @Test
    public void testResourceNoEndName() throws Exception {
        testResource(RESOURCE_NO_END_NAME, PSEG_B);
    }

    private void testResource(String resource, final String pseg) throws Exception {
        copyResource(resource, new ResourceCopier() {
            public void copy(InputStream in, OutputStream out) throws IOException {
                AFPResourceUtil.copyNamedResource(pseg, in, out);
            }
        });
    }

    private interface ResourceCopier {
        void copy(InputStream in, OutputStream out) throws IOException;
    }
}
