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

import java.io.IOException;

public abstract class AbstractStructuredObjectTest<S extends  AbstractStructuredObject>
        extends AbstractAFPObjectTest<S> {

    /**
     * Test writeStart() - test that the contract is maintained with
     * {@link AbstractStructuredObject}.
     *
     * @throws IOException
     */
    public void testwriteStart() throws IOException {
    }

    /**
     * Test writeEnd() - test that the contract is maintained with {@link AbstractStructuredObject}.
     *
     * @throws IOException
     */
    public void testWriteEnd() throws IOException {
    }

    /**
     * Test writeContent() - test that the contract is maintained with
     * {@link AbstractStructuredObject}.
     *
     * @throws IOException
     */
    public void testWriteContent() throws IOException {
    }

    /**
     * Test writeToStream() - test that the contract is maintained with
     * {@link AbstractStructuredObject}.
     *
     * @throws IOException
     */
    public void testWriteToStream() throws IOException {
        testwriteStart();
        testWriteEnd();
        testWriteContent();
    }
}
