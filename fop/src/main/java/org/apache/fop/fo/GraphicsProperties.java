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

package org.apache.fop.fo;

import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.properties.LengthRangeProperty;

/**
 * This interface provides access to properties necessary to calculate the size and positioning
 * of images and graphics inside a viewport.
 */
public interface GraphicsProperties {

    /**
     * @return the "inline-progression-dimension" property.
     */
    LengthRangeProperty getInlineProgressionDimension();

    /**
     * @return the "block-progression-dimension" property.
     */
    LengthRangeProperty getBlockProgressionDimension();

    /**
     * @return the "height" property.
     */
    Length getHeight();

    /**
     * @return the "width" property.
     */
    Length getWidth();

    /**
     * @return the "content-height" property.
     */
    Length getContentHeight();

    /**
     * @return the "content-width" property.
     */
    Length getContentWidth();

    /**
     * @return the "scaling" property.
     */
    int getScaling();

    /**
     * @return the "overflow" property.
     */
    int getOverflow();

    /**
     * @return the "display-align" property.
     */
    int getDisplayAlign();

    /**
     * @return the "text-align" property.
     */
    int getTextAlign();

}
