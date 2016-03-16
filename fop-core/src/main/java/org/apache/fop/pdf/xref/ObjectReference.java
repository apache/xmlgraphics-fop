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

package org.apache.fop.pdf.xref;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A reference to an indirect object.
 */
interface ObjectReference {

    /**
     * Outputs this reference to the given stream, in the cross-reference stream format.
     * For example, a object may output the bytes 01 00 00 00 00 00 00 01 ff 00 to
     * indicate a non-compressed object (01), at offset 511 from the beginning of the file
     * (00 00 00 00 00 00 01 ff), of generation number 0 (00).
     *
     * @param out the stream to which to output the reference
     */
    void output(DataOutputStream out) throws IOException;
}
