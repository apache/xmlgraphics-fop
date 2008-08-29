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

/* $Id: $ */

package org.apache.fop.render.afp.modca.triplets;

/**
 * Used by ObjectClassificationTriplet to provide
 * information on the structure of the object and its container
 */
public class StrucFlgs {

    private static final int OBJECT_DATA_NOT_CARRIED_IN_OBJECT_CONTAINER = 1;
    private static final int OBJECT_DATA_OBJECT_CONTAINER_STRUCTURE_UNKNOWN = 2;
    private static final int OBJECT_DATA_CARRIED_IN_OBJECT_CONTAINER = 3;

    private static final int OBJECT_CONTAINER_NOT_INCLUDE_OBJECT_ENVIRONMENT_GROUP = 4;
    private static final int OBJECT_CONTAINER_OBJECT_ENVIRONMENT_GROUP_CONTAINMENT_UNKNOWN = 8;
    private static final int OBJECT_CONTAINER_INCLUDES_OBJECT_ENVIRONMENT_GROUP = 12;

    private static final int OBJECT_CONTAINER_DATA_NOT_CARRIED_IN_OBJECT_DATA = 16;
    private static final int OBJECT_CONTAINER_DATA_OBJECT_DATA_CONTAINMENT_UNKNOWN = 32;
    private static final int OBJECT_CONTAINER_DATA_CARRIES_OBJECT_DATA = 48;

//    private static final int OBJECT_DATA_NOT_CARRIED_IN_OBJECT_CONTAINER = 48;
//    private static final int OBJECT_DATA_OBJECT_CONTAINER_STRUCTURE_UNKNOWN = 32;
//    private static final int OBJECT_DATA_CARRIED_IN_OBJECT_CONTAINER = 16;
//
//    private static final int OBJECT_CONTAINER_NOT_INCLUDE_OBJECT_ENVIRONMENT_GROUP = 12;
//    private static final int OBJECT_CONTAINER_OBJECT_ENVIRONMENT_GROUP_CONTAINMENT_UNKNOWN = 8;
//    private static final int OBJECT_CONTAINER_INCLUDES_OBJECT_ENVIRONMENT_GROUP = 4;
//
//    private static final int OBJECT_CONTAINER_DATA_NOT_CARRIED_IN_OBJECT_DATA = 3;
//    private static final int OBJECT_CONTAINER_DATA_OBJECT_DATA_CONTAINMENT_UNKNOWN = 2;
//    private static final int OBJECT_CONTAINER_DATA_CARRIES_OBJECT_DATA = 1;

    /**
     * the default structured flags setting
     * - data is in container with no object environment group and data in object container data
     */
    public static final StrucFlgs DEFAULT = new StrucFlgs(true, false, true);


    private int value = 0;

    /**
     * Main constructor
     *
     * @param dataInContainer true if the object data in carried in the object container
     * @param containerHasOEG true if the object container has an object environment group
     * @param dataInOCD true if the object container data carries the object data
     */
    public StrucFlgs(boolean dataInContainer, boolean containerHasOEG, boolean dataInOCD) {
        if (dataInContainer) {
            this.value += OBJECT_DATA_CARRIED_IN_OBJECT_CONTAINER;
        } else {
            this.value += OBJECT_DATA_NOT_CARRIED_IN_OBJECT_CONTAINER;
        }
        if (containerHasOEG) {
            this.value += OBJECT_CONTAINER_INCLUDES_OBJECT_ENVIRONMENT_GROUP;
        } else {
            this.value += OBJECT_CONTAINER_NOT_INCLUDE_OBJECT_ENVIRONMENT_GROUP;
        }
        if (dataInOCD) {
            this.value += OBJECT_CONTAINER_DATA_CARRIES_OBJECT_DATA;
        } else {
            this.value += OBJECT_CONTAINER_DATA_NOT_CARRIED_IN_OBJECT_DATA;
        }
    }

    /**
     * Default constructor
     */
    public StrucFlgs() {
        this.value = OBJECT_DATA_OBJECT_CONTAINER_STRUCTURE_UNKNOWN
            + OBJECT_CONTAINER_OBJECT_ENVIRONMENT_GROUP_CONTAINMENT_UNKNOWN
            + OBJECT_CONTAINER_DATA_OBJECT_DATA_CONTAINMENT_UNKNOWN;
    }

    /**
     * Returns the value of structure flags value
     *
     * @return the value of structure flags value
     */
    public byte getValue() {
        return (byte)this.value;
    }
}