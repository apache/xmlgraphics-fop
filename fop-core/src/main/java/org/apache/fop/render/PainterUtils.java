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

package org.apache.fop.render;

import org.apache.fop.render.intermediate.BorderPainter;

public final class PainterUtils {

    private PainterUtils() {
        //no implementation,  utility class
    }

    public static float getDashedSpaceWidth(float dashWidth, int spaceWidth) {
        float defaultSpaceWidth = BorderPainter.DASHED_BORDER_SPACE_RATIO * dashWidth;
        if (spaceWidth > 0) {
            defaultSpaceWidth = spaceWidth / 1000f;
        }

        return defaultSpaceWidth;
    }

    public static float getUnit(float unitSize, float length, int spaceWidth, boolean inMillis) {
        float unit = Math.abs(2 * unitSize);
        int rep = (int)(length / unit);
        if (rep % 2 == 0) {
            rep++;
        }
        unit = length / rep;
        if (spaceWidth > 0) {
            unit = spaceWidth;
            if (!inMillis) {
                unit = unit / 1000f;
            }
        }

        return unit;
    }
}
