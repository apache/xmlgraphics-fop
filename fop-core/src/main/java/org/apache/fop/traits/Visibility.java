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

package org.apache.fop.traits;

import java.io.ObjectStreamException;

import org.apache.fop.fo.Constants;


public final class Visibility extends TraitEnum {
    private static final long serialVersionUID = 1L;

    private static final String[] VISIBILITY_NAMES = new String[]
            {"visible", "hidden", "collapse"};

    private static final int[] VISIBILITY_VALUES = new int[]
            {Constants.EN_VISIBLE, Constants.EN_HIDDEN, Constants.EN_COLLAPSE};

    /** border-style: none */
    public static final Visibility VISIBLE = new Visibility(0);
    /** border-style: hidden */
    public static final Visibility HIDDEN = new Visibility(1);
    /** border-style: dotted */
    public static final Visibility COLLAPSE = new Visibility(2);

    private static final Visibility[] VISIBILITIES = new Visibility[] {
            VISIBLE, HIDDEN, COLLAPSE};


    private Visibility(int index) {
        super(VISIBILITY_NAMES[index], VISIBILITY_VALUES[index]);
    }

    /**
     * Returns the enumeration/singleton object based on its name.
     * @param name the name of the enumeration value
     * @return the enumeration object
     */
    public static Visibility valueOf(String name) {
        for (Visibility v : VISIBILITIES) {
            if (v.getName().equalsIgnoreCase(name)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Illegal visibility value: " + name);
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(getName());
    }

    public String toString() {
        return getName();
    }
}
