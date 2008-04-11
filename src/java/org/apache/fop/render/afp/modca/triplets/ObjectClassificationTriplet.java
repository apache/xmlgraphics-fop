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

import org.apache.fop.render.afp.modca.ObjectTypeRegistry;

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
     * @param entry
     *             the object type registry entry
     * @param strucFlgs
     *             the structured flags pertaining to this object classification triplet
     */
    public ObjectClassificationTriplet(byte objectClass, ObjectTypeRegistry.ObjectType entry,
            StrucFlgs strucFlgs) {
        super(OBJECT_CLASSIFICATION);
        byte[] data = new byte[93];
        data[0] = 0x00; // reserved (must be zero)
        data[1] = objectClass; // ObjClass
        data[2] = 0x00; // reserved (must be zero)
        data[3] = 0x00; // reserved (must be zero)
        // StrucFlgs - Information on the structure of the object container        
        data[4] = strucFlgs.getValue();
        data[5] = 0x00; // StrucFlgs
        
        if (entry == null) {
            throw new UnsupportedOperationException("Unknown registry entry");
        }
        
        byte[] oid = entry.getOID();
        // RegObjId - MOD:CA-registered ASN.1 OID for object type (8-23)
        System.arraycopy(oid, 0, data, 6, oid.length);
            
        byte[] objectTypeName = entry.getName();
        // ObjTpName - name of object type (24-55)
        System.arraycopy(objectTypeName, 0, data, 22, objectTypeName.length);
             
        // ObjLev (not specified) - Release level or version number of object type (56-63)

        // CompName (not specified) - Name of company or org that owns object definition (64-95)
    }
}