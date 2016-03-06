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

package org.apache.fop.afp.modca.triplets;

import org.apache.fop.afp.modca.AbstractAFPObject;

/**
 * A simple implementation of a MOD:CA triplet
 */
public abstract class AbstractTriplet extends AbstractAFPObject implements Triplet {

    /** the triplet identifier */
    protected final byte id;

    /**
     * Constructor
     *
     * @param id the triplet identifier (see static definitions above)
     */
    public AbstractTriplet(byte id) {
        this.id = id;
    }

    /**
     * Returns the triplet identifier
     *
     * @return the triplet identifier
     */
    public byte getId() {
        return this.id;
    }

    /**
     * Returns the structured triplet data array
     *
     * @return the structured triplet data array
     */
    public byte[] getData() {
        int dataLen = getDataLength();
        byte[] data = new byte[dataLen];
        data[0] = (byte)dataLen;
        data[1] = id;
        return data;
    }
}
