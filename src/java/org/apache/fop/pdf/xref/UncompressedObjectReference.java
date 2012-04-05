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
 * A reference to an indirect object that is not stored in an object stream.
 */
class UncompressedObjectReference implements ObjectReference {

    final long offset;

    /**
     * Creates a new reference.
     *
     * @param offset offset of the object from the beginning of the PDF file
     */
    UncompressedObjectReference(long offset) {
        this.offset = offset;
    }

    public void output(DataOutputStream out) throws IOException {
        out.write(1);
        out.writeLong(offset);
        out.write(0);
        out.write(0);
    }

}
