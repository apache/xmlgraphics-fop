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

package org.apache.fop.render.afp.modca;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.render.afp.ResourceInfo;
import org.apache.fop.render.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * An Include Object structured field references an object on a page or overlay.
 * It optionally contains parameters that identify the object and that specify
 * presentation parameters such as object position, size, orientation, mapping,
 * and default color.
 * <p>
 * Where the presentation parameters conflict with parameters specified in the
 * object's environment group (OEG), the parameters in the Include Object
 * structured field override. If the referenced object is a page segment, the
 * IOB parameters override the corresponding environment group parameters on all
 * data objects in the page segment.
 * </p>
 */
public class IncludeObject extends AbstractNamedAFPObject {

    /**
     * the include object is of type page segment
     */
    protected static final byte TYPE_PAGE_SEGMENT = (byte)0x5F;

    /**
     * the include object is of type other
     */
    protected static final byte TYPE_OTHER = (byte)0x92;

    /**
     * the include object is of type graphic
     */
    protected static final byte TYPE_GRAPHIC = (byte)0xBB;

    /**
     * the included object is of type barcode
     */
    protected static final byte TYPE_BARCODE = (byte)0xEB;
    
    /**
     * the included object is of type image
     */
    protected static final byte TYPE_IMAGE = (byte)0xFB;
        
    /**
     * The object type (default is other)
     */
    private byte objectType = TYPE_OTHER;

    /**
     * The orientation on the include object
     */
    private int orientation = -1;

    /**
     * The X-axis origin of the object area 
     */
    private int xOffset = -1; 
    
    /**
     * The Y-axis origin of the object area 
     */    
    private int yOffset = -1;

    /**
     * The X-axis origin defined in the object
     */
    private int xContentOffset = -1;

    /**
     * The Y-axis origin defined in the object
     */
    private int yContentOffset = -1;
    
//    /**
//     * The object referenced by this include object
//     */
//    private Resource resourceObj = null;
    
    /**
     * the level at which this resource object resides
     */
    private ResourceInfo level = null;

    /**
     * the referenced data object
     */
    private AbstractNamedAFPObject dataObj = null;
    
    /**
     * Constructor for the include object with the specified name, the name must
     * be a fixed length of eight characters and is the name of the referenced
     * object.
     *
     * @param resourceObj the resource object wrapper
     */
    public IncludeObject(AbstractNamedAFPObject dataObj) {
        super(dataObj.getName());
        this.dataObj = dataObj;
//      AbstractStructuredAFPObject referencedObject = resourceObj.getReferencedObject();
        if (dataObj instanceof ImageObject) {
            this.objectType = TYPE_IMAGE;
        } else if (dataObj instanceof GraphicsObject) {
            this.objectType = TYPE_GRAPHIC;
        } else if (dataObj instanceof PageSegment) {
            this.objectType = TYPE_PAGE_SEGMENT;
        } else {
            this.objectType = TYPE_OTHER;
        }

        // set data object reference triplet
        ResourceInfo resourceInfo = getResourceInfo();
        if (resourceInfo != null && resourceInfo.isExternal()) {
            String dest = resourceInfo.getExternalResourceGroupDest();
            super.setFullyQualifiedName(
                    FullyQualifiedNameTriplet.TYPE_DATA_OBJECT_EXTERNAL_RESOURCE_REF,
                    FullyQualifiedNameTriplet.FORMAT_URL, dest);
        } else {
            super.setFullyQualifiedName(
                    FullyQualifiedNameTriplet.TYPE_DATA_OBJECT_INTERNAL_RESOURCE_REF,
                    FullyQualifiedNameTriplet.FORMAT_CHARSTR, dataObj.getName());            
        }        
    }

    /**
     * Sets the resource level for this resource object
     * @param level the resource level
     */
    public void setResourceInfo(ResourceInfo level) {
        this.level = level;
    }
    
    /**
     * @return the resource info of this resource object
     */
    public ResourceInfo getResourceInfo() {
        return this.level;
    }

//    /**
//     * @return the resource container object referenced by this include object
//     */
//    public Resource getResource() {
//        return this.resourceObj;
//    }

    /**
     * @return the actual resource data object referenced by this include object
     */
    public AbstractStructuredAFPObject getReferencedObject() {
        return this.dataObj;
        //getResource().getReferencedObject();
    }

    /**
     * Sets the orientation to use for the Include Object.
     *
     * @param orientation
     *            The orientation (0,90, 180, 270)
     */
    public void setOrientation(int orientation) {
        if (orientation == 0 || orientation == 90 || orientation == 180
            || orientation == 270) {
            this.orientation = orientation;
        } else {
            throw new IllegalArgumentException(
                "The orientation must be one of the values 0, 90, 180, 270");
        }
    }

    /**
     * Sets the x and y offset to the origin in the object area 
     * @param x the X-axis origin of the object area
     * @param y the Y-axis origin of the object area
     */
    public void setObjectArea(int x, int y) {
        this.xOffset = x;
        this.yOffset = y;
    }
    
    /**
     * Sets the x and y offset of the content area to the object area 
     * @param x the X-axis origin defined in the object
     * @param y the Y-axis origin defined in the object
     */
    public void setContentArea(int x, int y) {
        this.xContentOffset = x;
        this.yContentOffset = y;
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeDataStream(OutputStream os) throws IOException {       
        byte[] data = new byte[36];
        data[0] = 0x5A;

        // Set the total record length
        byte[] len = BinaryUtils.convert(35 + getTripletDataLength(), 2); //Ignore first byte
        data[1] = len[0];
        data[2] = len[1];

        // Structured field ID for a IOB
        data[3] = (byte) 0xD3;
        data[4] = (byte) 0xAF;
        data[5] = (byte) 0xC3;

        data[6] = 0x00; // Reserved
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }

        data[17] = 0x00; // reserved
        data[18] = objectType;

        //XoaOset (object area)
        if (xOffset > 0) {
            byte[] x = BinaryUtils.convert(xOffset, 3);
            data[19] = x[0];
            data[20] = x[1];
            data[21] = x[2];            
        } else {
            data[19] = (byte)0xFF;
            data[20] = (byte)0xFF;
            data[21] = (byte)0xFF;            
        }

        // YoaOset (object area)
        if (yOffset > 0) {
            byte[] y = BinaryUtils.convert(yOffset, 3);
            data[22] = y[0];
            data[23] = y[1];
            data[24] = y[2];                        
        } else {
            data[22] = (byte)0xFF;
            data[23] = (byte)0xFF;
            data[24] = (byte)0xFF;            
        }

        switch (orientation) {
            case -1: // use x/y axis orientation defined in object
                data[25] = (byte)0xFF; // x axis rotation
                data[26] = (byte)0xFF; //
                data[27] = (byte)0xFF; // y axis rotation
                data[28] = (byte)0xFF;
                break;                
            case 90:
                data[25] = 0x2D;
                data[26] = 0x00;
                data[27] = 0x5A;
                data[28] = 0x00;
                break;
            case 180:
                data[25] = 0x5A;
                data[25] = 0x00;
                data[27] = (byte)0x87;
                data[28] = 0x00;
                break;
            case 270:
                data[25] = (byte)0x87;
                data[26] = 0x00;
                data[27] = 0x00;
                data[28] = 0x00;
                break;
            default:
                data[25] = 0x00;
                data[26] = 0x00;
                data[27] = 0x2D;
                data[28] = 0x00;
                break;
        }

        // XocaOset (object content)
        if (xContentOffset > 0) {
            byte[] y = BinaryUtils.convert(xContentOffset, 3);
            data[29] = y[0];
            data[30] = y[1];
            data[31] = y[2];            
        } else {
            data[29] = (byte)0xFF;
            data[30] = (byte)0xFF;
            data[31] = (byte)0xFF;
        }

        // YocaOset (object content)
        if (yContentOffset > 0) {
            byte[] y = BinaryUtils.convert(yContentOffset, 3);
            data[32] = y[0];
            data[33] = y[1];
            data[34] = y[2];                        
        } else {
            data[32] = (byte)0xFF;
            data[33] = (byte)0xFF;
            data[34] = (byte)0xFF;
        }
        data[35] = 0x01;

        // Write structured field data
        os.write(data);
        
        // Write triplet for FQN internal/external object reference 
        byte[] tripletData = super.getTripletData();
        os.write(tripletData);
    }
}