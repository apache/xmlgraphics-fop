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

import java.io.UnsupportedEncodingException;

import org.apache.fop.render.afp.AFPConstants;
import org.apache.fop.render.afp.modca.Registry;
import org.apache.fop.render.afp.tools.StringUtils;

/**
 * The Object Classification is used to classify and identify object data.
 * The object data may or may not be defined by an IBM presentation architecture
 */
public class ObjectClassificationTriplet extends Triplet {

    /**
     * The scope of this object is the including page or overlay
     */
    public static final byte CLASS_TIME_INVARIANT_PAGINATED_PRESENTATION_OBJECT = 0x01;

    /**
     * The scope of this object is not defined
     */
    public static final byte CLASS_TIME_VARIANT_PRESENTATION_OBJECT = 0x10;

    /**
     * This is not a presentation object, the scope of this object is not defined
     */
    public static final byte CLASS_EXECUTABLE_PROGRAM = 0x20;

    /**
     * Setup information file, document level.  This is not a presentation object,
     */
    public static final byte CLASS_SETUP_FILE = 0x30;

    /**
     * This is a resource used by a presentation object that may itself be a resource.
     * The scope of the resource is the object that uses the resource.
     */
    public static final byte CLASS_SECONDARY_RESOURCE = 0x40;

    /**
     * Data object font.  This is a non-FOCA font resource used to present
     * text in a data object.  The scope of the resource is the object that
     * uses the resource.
     */
    public static final byte CLASS_DATA_OBJECT_FONT = 0x41;

    /**
     * Main constructor
     *
     * @param objectClass
     *             the object class type
     * @param objectType
     *             the object type registry entry
     * @param strucFlgs
     *             the structured flags pertaining to this object classification triplet
     */
    public ObjectClassificationTriplet(byte objectClass, Registry.ObjectType objectType,
            StrucFlgs strucFlgs) {
        // no object level or company name specified
        this(objectClass, objectType, strucFlgs, null, null);
    }


    private static final int OBJECT_LEVEL_LEN = 8;
    private static final int OBJECT_TYPE_NAME_LEN = 32;
    private static final int COMPANY_NAME_LEN = 32;

    /**
     * Fully parameterized constructor
     *
     * @param objectClass
     *             the object class type
     * @param objectType
     *             the object type registry entry
     * @param strucFlgs
     *             the structured flags pertaining to this object classification triplet
     * @param objLev
     *             the release level or version number of the object type
     * @param compName
     *             the name of the company or organization that owns the object definition
     */
    public ObjectClassificationTriplet(byte objectClass, Registry.ObjectType objectType,
            StrucFlgs strucFlgs, String objLev, String compName) {
        super(OBJECT_CLASSIFICATION);

        if (objectType == null) {
            throw new IllegalArgumentException("MO:DCA Registry object type is null");
        }

        byte[] data = new byte[94];
        data[0] = 0x00; // reserved (must be zero)
        data[1] = objectClass; // ObjClass
        data[2] = 0x00; // reserved (must be zero)
        data[3] = 0x00; // reserved (must be zero)
        // StrucFlgs - Information on the structure of the object container
        data[4] = strucFlgs.getValue();
        data[5] = 0x00; // StrucFlgs

        byte[] oid = objectType.getOID();
        // RegObjId - MOD:CA-registered ASN.1 OID for object type (8-23)
        System.arraycopy(oid, 0, data, 6, oid.length);

        // ObjTpName - name of object type (24-55)
        byte[] objTpName;
        try {
            objTpName = StringUtils.rpad(objectType.getName(), ' ', OBJECT_TYPE_NAME_LEN).getBytes(
                    AFPConstants.EBCIDIC_ENCODING);
            System.arraycopy(objTpName, 0, data, 22, objTpName.length);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("an encoding exception occurred");
        }

        // ObjLev - release level or version number of object type (56-63)
        byte[] objectLevel;
        try {
            objectLevel = StringUtils.rpad(objLev, ' ', OBJECT_LEVEL_LEN).getBytes(
                    AFPConstants.EBCIDIC_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("an encoding exception occurred");
        }
        System.arraycopy(objectLevel, 0, data, 54, objectLevel.length);

        // CompName - name of company or organization that owns object definition (64-95)
        byte[] companyName;
        try {
            companyName = StringUtils.rpad(compName, ' ', COMPANY_NAME_LEN).getBytes(
                    AFPConstants.EBCIDIC_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("an encoding exception occurred");
        }
        System.arraycopy(companyName, 0, data, 62, companyName.length);

        super.setData(data);
    }

}