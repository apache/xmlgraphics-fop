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
import java.io.OutputStream;

/**
 * A print-file resource group
 */
public class StreamedResourceGroup extends ResourceGroup {
    /** the outputstream to write to */
    private final OutputStream os;

    private boolean started = false;

    private boolean complete = false;

    /**
     * Main constructor
     *
     * @param name the resource group name
     * @param os the outputstream
     */
    public StreamedResourceGroup(String name, OutputStream os) {
        super(name);
        this.os = os;
    }

    /**
     * Adds a resource to the external resource group
     *
     * @param namedObject a named object
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    public void addObject(AbstractNamedAFPObject namedObject) throws IOException {
        if (!started) {
            writeStart(os);
            started = true;
        }
        try {
            namedObject.writeToStream(os);
        } finally {
            os.flush();
        }
    }

    /**
     * Closes this external resource group file
     *
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    public void close() throws IOException {
        writeEnd(os);
        complete = true;
    }

    /**
     * Returns true if this resource group is complete
     *
     * @return true if this resource group is complete
     */
    public boolean isComplete() {
        return this.complete;
    }

    /**
     * Returns the outputstream
     *
     * @return the outputstream
     */
    public OutputStream getOutputStream() {
        return this.os;
    }
}