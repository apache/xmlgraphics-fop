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

package org.apache.fop.traits;

import java.io.Serializable;

/** Base class for enumeration classes representing traits. */
public abstract class TraitEnum implements Serializable {

    private String name;
    private int enumValue;

    /**
     * Constructor to add a new named item.
     * @param name Name of the item.
     * @param enumValue the {@code Constants}.EN_* value
     */
    protected TraitEnum(String name, int enumValue) {
        this.name = name;
        this.enumValue = enumValue;
    }

    /**
     * Returns the name of the enumeration.
     * @return the name of the enumeration
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the enumeration value (one of {@code Constants}.EN_*).
     * @return the enumeration value
     */
    public int getEnumValue() {
        return this.enumValue;
    }

}
