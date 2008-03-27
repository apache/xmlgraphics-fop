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

import org.apache.fop.render.afp.modca.Registry;

/**
 * The Object Classification is used to classify and identify object data.
 * The object data may or may not be defined by an IBM presentation architecture
 */
public class ObjectClassificationTriplet extends Triplet {    
    public static final byte CLASS_TIME_INVARIANT_PAGINATED_PRESENTATION_OBJECT = 0x01;
    public static final byte CLASS_TIME_VARIANT_PRESENTATION_OBJECT = 0x10;
    public static final byte CLASS_EXECUTABLE_PROGRAM = 0x20;
    public static final byte CLASS_SETUP_FILE = 0x30;
    public static final byte CLASS_SECONDARY_RESOURCE = 0x40;
    public static final byte CLASS_DATA_OBJECT_FONT = 0x41;
    
    /**
     * Main constructor
     * 
     * @param objectClass
     *             the object class type
     * @param componentId
     *             the object componentId
     */
    public ObjectClassificationTriplet(byte objectClass, byte componentId) {
        super(OBJECT_CLASSIFICATION);
        byte[] data = new byte[93];
        data[0] = 0x00; // reserved (must be zero)
        data[1] = objectClass; // ObjClass
        data[2] = 0x00; // reserved (must be zero)
        data[3] = 0x00; // reserved (must be zero)
        data[4] = 0x00; // StrucFlgs - Information on the structure of the object container
        data[5] = 0x00; // StrucFlgs
        
        Registry.Entry entry = Registry.getInstance().getEntry(componentId);
        
        if (entry == null) {
            throw new UnsupportedOperationException("unknown registry entry " + componentId);
        }
        // RegObjId - MOD:CA-registered ASN.1 OID for object type (8-23)
        System.arraycopy(entry.getOID(), 0, data, 6, entry.getOID().length);
            
        // ObjTpName - name of object type (24-55)
        System.arraycopy(entry.getObjectTypeName(), 0, data, 22,
                entry.getObjectTypeName().length);
             
        // ObjLev (not specified) - Release level or version number of object type (56-63)

        // CompName (not specified) - Name of company or org that owns object definition (64-95)
    }        
}