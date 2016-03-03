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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.modca.triplets.DescriptorPositionTriplet;
import org.apache.fop.afp.modca.triplets.MeasurementUnitsTriplet;
import org.apache.fop.afp.modca.triplets.ObjectAreaSizeTriplet;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Object Area Descriptor structured field specifies the size and attributes
 * of an object area presentation space.
 */
public class ObjectAreaDescriptor extends AbstractDescriptor {

    /**
     * Construct an object area descriptor for the specified object width
     * and object height.
     *
     * @param width the object width.
     * @param height the object height.
     * @param widthRes the object width resolution.
     * @param heightRes the object height resolution.
     */
    public ObjectAreaDescriptor(int width, int height, int widthRes, int heightRes) {
        super(width, height, widthRes, heightRes);
    }

    private static final byte OBJECT_AREA_POSITION_ID = 0x01;

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[9];
        copySF(data, Type.DESCRIPTOR, Category.OBJECT_AREA);

        addTriplet(new DescriptorPositionTriplet(OBJECT_AREA_POSITION_ID));
        addTriplet(new MeasurementUnitsTriplet(widthRes, heightRes));
        addTriplet(new ObjectAreaSizeTriplet(width, height));
        /* not allowed in Presentation Interchange Set 1
        addTriplet(new PresentationSpaceResetMixingTriplet(
                PresentationSpaceResetMixingTriplet.NOT_RESET));
        */

        int tripletDataLength = getTripletDataLength();
        byte[] len = BinaryUtils.convert(data.length + tripletDataLength - 1, 2);
        data[1] = len[0]; // Length
        data[2] = len[1];
        os.write(data);

        writeTriplets(os);
    }

}
