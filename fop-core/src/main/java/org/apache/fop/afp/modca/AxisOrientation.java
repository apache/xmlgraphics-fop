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

/**
 * Represents the 4 bytes that specify the axis-area rotation reference coordinate system
 */
public enum AxisOrientation {

    RIGHT_HANDED_0(Rotation.ROTATION_0, Rotation.ROTATION_90),
    RIGHT_HANDED_90(Rotation.ROTATION_90, Rotation.ROTATION_180),
    RIGHT_HANDED_180(Rotation.ROTATION_180, Rotation.ROTATION_270),
    RIGHT_HANDED_270(Rotation.ROTATION_270, Rotation.ROTATION_0);

    /**
     * The object area's X-axis rotation from the X axis of the reference coordinate system
     */
    private final Rotation xoaOrent;
    /**
     * The object area's Y-axis rotation from the Y axis of the reference coordinate system
     */
    private final Rotation yoaOrent;

    public void writeTo(byte[] out, int offset) {
        xoaOrent.writeTo(out, offset);
        yoaOrent.writeTo(out, offset + 2);
    }

    private AxisOrientation(Rotation xoaOrent, Rotation yoaOrent) {
        this.xoaOrent = xoaOrent;
        this.yoaOrent = yoaOrent;
    }

    /**
     * Writes the axis orientation area bytes to the output stream.
     *
     * @param stream the output stream to write to
     * @throws IOException if an I/O error occurs
     */
    public void writeTo(OutputStream stream) throws IOException {
        byte[] data = new byte[4];
        writeTo(data, 0);
        stream.write(data);
    }

    /**
     * Gets the right-handed axis orientation object for a given orientation in degrees.
     *
     * @param orientation the orientation in degrees
     * @return the {@link AxisOrientation} object
     */
    public static AxisOrientation getRightHandedAxisOrientationFor(int orientation) {
        switch (orientation) {
            case 0: return RIGHT_HANDED_0;
            case 90: return RIGHT_HANDED_90;
            case 180: return RIGHT_HANDED_180;
            case 270: return RIGHT_HANDED_270;
            default: throw new IllegalArgumentException(
            "The orientation must be one of the values 0, 90, 180, 270");
        }
    }
}
