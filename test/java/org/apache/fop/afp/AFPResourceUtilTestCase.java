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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.fop.afp.util.AFPResourceUtil;
import org.junit.Test;

/**
 * Tests the {@link AFPResourceUtil} class.
 */
public class AFPResourceUtilTestCase {

    private static final String RESOURCE_FILENAME = "expected_resource.afp";

    private static final String NAMED_RESOURCE_FILENAME = "expected_named_resource.afp";

    private static final String PSEG = "XFEATHER";

    /**
     * Tests copyResourceFile()
     * @throws Exception -
     */
    @Test
    public void testCopyResourceFile() throws Exception {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        InputStream in = null;

        try {
            in = getClass().getResourceAsStream(RESOURCE_FILENAME);
            AFPResourceUtil.copyResourceFile(in, baos);
        } finally {
            in.close();
        }

        byte[] expectedBytes = null;

        try {
            in = getClass().getResourceAsStream(RESOURCE_FILENAME);
            expectedBytes = IOUtils.toByteArray(in);
        } finally {
            in.close();
        }

        assertTrue(Arrays.equals(expectedBytes, baos.toByteArray()));

    }

    /**
     * Tests copyNamedResource()
     * @throws Exception -
     */
    @Test
    public void testCopyNamedResource() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        InputStream in = null;

        try {
            in = getClass().getResourceAsStream(RESOURCE_FILENAME);
            AFPResourceUtil.copyNamedResource(PSEG, in, baos);
        } finally {
            in.close();
        }

        byte[] expectedBytes = null;

        try {
            in = getClass().getResourceAsStream(NAMED_RESOURCE_FILENAME);
            expectedBytes = IOUtils.toByteArray(in);
        } finally {
            in.close();
        }

        assertTrue(Arrays.equals(expectedBytes, baos.toByteArray()));
    }
}
