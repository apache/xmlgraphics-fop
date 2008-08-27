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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.render.afp.modca.triplets.MeasurementUnitsTriplet;
import org.apache.fop.render.afp.modca.triplets.ObjectAreaSizeTriplet;
import org.apache.fop.render.afp.modca.triplets.PresentationSpaceResetMixingTriplet;
import org.apache.fop.render.afp.modca.triplets.Triplet;
import org.apache.fop.render.afp.tools.BinaryUtils;

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

    /** {@inheritDoc} */
    protected byte[] getTripletData() throws IOException {
        if (tripletData == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Specifies the associated ObjectAreaPosition structured field
            final byte oapId = 0x01;
            Triplet triplet = new Triplet(Triplet.DESCRIPTOR_POSITION, oapId);
            triplet.writeToStream(baos);

            triplet = new MeasurementUnitsTriplet(widthRes, heightRes);
            triplet.writeToStream(baos);

            triplet = new ObjectAreaSizeTriplet(width, height);
            triplet.writeToStream(baos);

            triplet = new PresentationSpaceResetMixingTriplet(
                    PresentationSpaceResetMixingTriplet.NOT_RESET);
            triplet.writeToStream(baos);

            this.tripletData = baos.toByteArray();
        }
        return this.tripletData;
    }

    /** {@inheritDoc} */
    public void writeStart(OutputStream os) throws IOException {
        super.writeStart(os);
        byte[] data = new byte[9];
        copySF(data, Type.DESCRIPTOR, Category.OBJECT_AREA);
        byte[] len = BinaryUtils.convert(data.length + tripletData.length - 1, 2);
        data[1] = len[0]; // Length
        data[2] = len[1];
        os.write(data);
    }

}
