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

/**
 * Sets the current painting position of the graphics object
 */
public class GraphicsSetCurrentPosition extends AbstractGraphicsCoord {

    /**
     * Constructor
     *
     * @param coords the x/y coordinates for this object
     */
    public GraphicsSetCurrentPosition(int[] coords) {
        super(coords);
    }

    /** {@inheritDoc} */
    protected byte getOrderCode() {
        return (byte)0x21;
    }
}