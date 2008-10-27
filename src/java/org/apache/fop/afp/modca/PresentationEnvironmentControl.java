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

import org.apache.fop.afp.modca.triplets.Triplet;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Presentation Environment Control structured field specifies parameters that
 * affect the rendering of presentation data and the appearance that is to be assumed
 * by the presentation device.
 */
public class PresentationEnvironmentControl extends AbstractStructuredAFPObject {

    /**
     * Main constructor
     */
    public PresentationEnvironmentControl() {
    }

    /**
     * Sets the object offset
     */
    public void setObjectOffset() {
        addTriplet(new ObjectOffsetTriplet());
    }

    /**
     * Sets the rendering intent
     */
    public void setRenderingIntent() {
        addTriplet(new RenderingIntentTriplet());
    }

    /**
     * Sets the device appearance
     */
    public void setDeviceAppearance() {
        addTriplet(new DeviceAppearanceTriplet());
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[11];
        copySF(data, Type.CONTROL, Category.DOCUMENT);
        int tripletDataLen = getTripletDataLength();
        byte[] len = BinaryUtils.convert(10 + tripletDataLen);
        data[1] = len[0];
        data[2] = len[1];
        data[9] = 0x00;  // Reserved; must be zero
        data[10] = 0x00; // Reserved; must be zero

        os.write(data);
        os.write(tripletData);
    }

    // TODO
    private class DeviceAppearanceTriplet extends Triplet {
        public DeviceAppearanceTriplet() {
            super(Triplet.DEVICE_APPEARANCE);
        }
    }

    // TODO
    private class RenderingIntentTriplet extends Triplet {
        public RenderingIntentTriplet() {
            super(Triplet.RENDERING_INTENT);
        }
    }

    // TODO
    private class ObjectOffsetTriplet extends Triplet {
        public ObjectOffsetTriplet() {
            super(Triplet.OBJECT_OFFSET);
        }
    }
}
