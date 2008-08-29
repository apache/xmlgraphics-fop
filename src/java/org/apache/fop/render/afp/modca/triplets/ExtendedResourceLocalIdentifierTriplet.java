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

import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * The Extended Resource Local Identifier triplet specifies a resource type and a
 * four byte local identifier or LID. The LID usually is associated with a specific
 * resource name by a map structured field, such as a Map Data Resource structured
 * field, or a Map Media Type structured field.
 */
public class ExtendedResourceLocalIdentifierTriplet extends Triplet {

    /** the image resource type */
    public static final byte TYPE_IMAGE_RESOURCE = 0x10;

    /** the retired value type */
    public static final byte TYPE_RETIRED_VALUE = 0x30;

    /** the retired value type */
    public static final byte TYPE_MEDIA_RESOURCE = 0x40;

    /**
     * Main constructor
     *
     * @param type the resource type
     * @param localId the resource local id
     */
    public ExtendedResourceLocalIdentifierTriplet(byte type, int localId) {
        super(Triplet.EXTENDED_RESOURCE_LOCAL_IDENTIFIER);
        byte[] data = new byte[5];
        data[0] = type;
        byte[] resLID = BinaryUtils.convert(localId, 4);
        System.arraycopy(resLID, 0, data, 1, resLID.length);
        super.setData(data);
    }
}
