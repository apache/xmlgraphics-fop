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

package org.apache.fop.area;

import java.awt.Rectangle;

/**
 * A viewport-area that may clip its content.
 */
public interface Viewport {

    /**
     * Returns true if this area will clip overflowing content.
     *
     * @return {@code true} if the overflow trait has the value "hidden", "scroll" or
     * "error-if-overflow"
     */
    boolean hasClip();

    /**
     * Returns the clipping rectangle of this viewport area.
     *
     * @return the clipping rectangle expressed in the viewport's coordinate system, or
     * null if clipping is disabled
     */
    Rectangle getClipRectangle();
}
