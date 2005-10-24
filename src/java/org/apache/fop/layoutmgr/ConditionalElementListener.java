/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.layoutmgr;

import org.apache.fop.traits.MinOptMax;

/**
 * This interface is used to notify layout managers about the situation around spaces, borders
 * and padding just before the addArea() method is called when a part is being painted.
 */
public interface ConditionalElementListener {

    /**
     * Notifies the layout manager about the effective length of its space.
     * @param side the side to which the space applies
     * @param effectiveLength the effective length after space-resolution (null means zero length)
     */
    void notifySpace(RelSide side, MinOptMax effectiveLength);

    /**
     * Notifies the layout manager about the effective length/width of its border.
     * @param side the side to which the border applies
     * @param effectiveLength the effective length in the current break situation
     *                        (null means zero length)
     */
    void notifyBorder(RelSide side, MinOptMax effectiveLength);

    /**
     * Notifies the layout manager about the effective length/width of its padding.
     * @param side the side to which the padding applies
     * @param effectiveLength the effective length in the current break situation
     *                        (null means zero length)
     */
    void notifyPadding(RelSide side, MinOptMax effectiveLength);
    
}
