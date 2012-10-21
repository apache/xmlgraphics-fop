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
 * A reference to an indirect object stored in an object stream. Contains the relevant
 * information to add to a cross-reference stream.
 */
public class CompressedObjectReference implements ObjectReference {

    private final int objectNumber;

    private final int objectStreamNumber;

    private final int index;

    /**
     * Creates a new reference.
     *
     * @param objectNumber the number of the compressed object being referenced
     * @param objectStreamNumber the number of the object stream in which the compressed
     * object is to be found
     * @param index the index of the compressed object in the object stream
     */
    public CompressedObjectReference(int objectNumber, int objectStreamNumber, int index) {
        this.objectNumber = objectNumber;
        this.objectStreamNumber = objectStreamNumber;
        this.index = index;
    }

    public void output(DataOutputStream out) throws IOException {
        out.write(2);
        out.writeLong(objectStreamNumber);
        out.write(0);
        out.write(index);
    }

    public int getObjectNumber() {
        return objectNumber;
    }

    public int getObjectStreamNumber() {
        return objectStreamNumber;
    }

}
