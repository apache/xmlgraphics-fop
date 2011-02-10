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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.modca.Registry.ObjectType;
import org.apache.fop.afp.util.StringUtils;

/**
 * The Object Classification is used to classify and identify object data.
 * The object data may or may not be defined by an IBM presentation architecture
 */
public class ObjectClassificationTriplet extends AbstractTriplet {

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

    /** the object class */
    private final byte objectClass;

    /** the object type */
    private final ObjectType objectType;

    /** whether the container has an object environment group */
    private final boolean containerHasOEG;

    /** whether the data resides within the container */
    private final boolean dataInContainer;

    /** whether the data resides within the object container data */
    private final boolean dataInOCD;

    /** the object level (version) */
    private final String objectLevel;

    /** the company/organization name */
    private final String companyName;


    /**
     * Main constructor
     *
     * @param objectClass the object class type
     * @param objectType the object type registry entry
     * @param dataInContainer whether the data resides in the container
     * @param containerHasOEG whether the container has an object environment group
     * @param dataInOCD whether the data resides in a object container data structured field
     */
    public ObjectClassificationTriplet(byte objectClass, ObjectType objectType,
            boolean dataInContainer, boolean containerHasOEG, boolean dataInOCD) {
        // no object level or company name specified
        this(objectClass, objectType, dataInContainer, containerHasOEG, dataInOCD, null, null);
    }

    /**
     * Fully parameterized constructor
     *
     * @param objectClass the object class type
     * @param objectType the object type registry entry
     * @param dataInContainer whether the data resides in the container
     * @param containerHasOEG whether the container has an object environment group
     * @param dataInOCD whether the data resides in a object container data structured field
     * @param objLev the release level or version number of the object type
     * @param compName the name of the company or organization that owns the object definition
     */
    public ObjectClassificationTriplet(byte objectClass, ObjectType objectType,
            boolean dataInContainer, boolean containerHasOEG, boolean dataInOCD,
            String objLev, String compName) {
        super(OBJECT_CLASSIFICATION);

        this.objectClass = objectClass;
        if (objectType == null) {
            throw new IllegalArgumentException("MO:DCA Registry object type is null");
        }
        this.objectType = objectType;
        this.dataInContainer = dataInContainer;
        this.containerHasOEG = containerHasOEG;
        this.dataInOCD = dataInOCD;
        this.objectLevel = objLev;
        this.companyName = compName;
    }

    /**
     * Returns the structured field flags
     *
     * @param dataInContainer true if the object data in carried in the object container
     * @param containerHasOEG true if the object container has an object environment group
     * @param dataInOCD true if the object container data carries the object data
     *
     * @return the byte value of this structure
     */
    public byte[] getStructureFlagsAsBytes(boolean dataInContainer, boolean containerHasOEG,
            boolean dataInOCD) {
        byte[] strucFlgs = new byte[2];
        // Object Container (BOC/EOC)
        if (dataInContainer) {
            strucFlgs[0] |= 3 << 6;
        } else {
            strucFlgs[0] |= 1 << 6;
        }
        // Object Environment Group (OEG)
        if (containerHasOEG) {
            strucFlgs[0] |= 3 << 4;
        } else {
            strucFlgs[0] |= 1 << 4;
        }
        // Object Container Data (OCD) structured fields
        if (dataInOCD) {
            strucFlgs[0] |= 3 << 2;
        } else {
            strucFlgs[0] |= 1 << 2;
        }
        strucFlgs[1] = 0x00;
        return strucFlgs;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 96;
    }

    private static final int OBJECT_LEVEL_LEN = 8;
    private static final int OBJECT_TYPE_NAME_LEN = 32;
    private static final int COMPANY_NAME_LEN = 32;

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = getData();
        data[2] = 0x00; // reserved (must be zero)
        data[3] = objectClass; // ObjClass
        data[4] = 0x00; // reserved (must be zero)
        data[5] = 0x00; // reserved (must be zero)

        // StrucFlgs - Information on the structure of the object container
        byte[] structureFlagsBytes
            = getStructureFlagsAsBytes(dataInContainer, containerHasOEG, dataInOCD);
        data[6] = structureFlagsBytes[0];
        data[7] = structureFlagsBytes[1];

        byte[] objectIdBytes = objectType.getOID();
        // RegObjId - MOD:CA-registered ASN.1 OID for object type (8-23)
        System.arraycopy(objectIdBytes, 0, data, 8, objectIdBytes.length);

        // ObjTpName - name of object type (24-55)
        byte[] objectTypeNameBytes;
        objectTypeNameBytes
            = StringUtils.rpad(objectType.getName(), ' ', OBJECT_TYPE_NAME_LEN).getBytes(
                AFPConstants.EBCIDIC_ENCODING);
        System.arraycopy(objectTypeNameBytes, 0, data, 24, objectTypeNameBytes.length);

        // ObjLev - release level or version number of object type (56-63)
        byte[] objectLevelBytes;
        objectLevelBytes = StringUtils.rpad(objectLevel, ' ', OBJECT_LEVEL_LEN).getBytes(
                AFPConstants.EBCIDIC_ENCODING);
        System.arraycopy(objectLevelBytes, 0, data, 56, objectLevelBytes.length);

        // CompName - name of company or organization that owns object definition (64-95)
        byte[] companyNameBytes;
        companyNameBytes = StringUtils.rpad(companyName, ' ', COMPANY_NAME_LEN).getBytes(
                AFPConstants.EBCIDIC_ENCODING);
        System.arraycopy(companyNameBytes, 0, data, 64, companyNameBytes.length);

        os.write(data);
    }
}