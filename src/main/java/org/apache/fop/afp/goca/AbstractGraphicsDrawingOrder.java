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

package org.apache.fop.afp.goca;

import org.apache.fop.afp.StructuredData;
import org.apache.fop.afp.modca.AbstractAFPObject;

/**
 * A base GOCA drawing order
 */
public abstract class AbstractGraphicsDrawingOrder extends AbstractAFPObject
    implements StructuredData {

    /**
     * Returns the order code of this structured field
     *
     * @return the order code of this structured field
     */
    abstract byte getOrderCode();

    /**
     * Returns the coordinate data
     *
     * @return the coordinate data
     */
    byte[] getData() {
        int len = getDataLength();
        byte[] data = new byte[len];
        data[0] = getOrderCode();
        data[1] = (byte)(len - 2);
        return data;
    }

    /**
     * Returns the short name of this GOCA object
     *
     * @return the short name of this GOCA object
     */
    public String getName() {
        String className = getClass().getName();
        return className.substring(className.lastIndexOf(".") + 1);
    }
}
