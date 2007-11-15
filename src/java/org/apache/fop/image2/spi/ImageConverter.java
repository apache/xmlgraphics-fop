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
 
package org.apache.fop.image2.spi;

import java.io.IOException;
import java.util.Map;

import org.apache.fop.image2.Image;
import org.apache.fop.image2.ImageException;
import org.apache.fop.image2.ImageFlavor;

/**
 * Defines an image converter that can convert one image representation into another.
 */
public interface ImageConverter {

    /** Used if the conversion penalty is negligible (for example a simple cast). */
    int NO_CONVERSION_PENALTY = 0;
    /** Used if the conversion penalty is minimal */
    int MINIMAL_CONVERSION_PENALTY = 1;
    /** Default/Medium conversion penalty (if there's some effort to convert the image format) */
    int MEDIUM_CONVERSION_PENALTY = 10;
    
    /**
     * Converts an image into a different representation.
     * @param src the source image
     * @param hints the conversion hints
     * @return the converted image
     * @throws ImageException if an error occurs while converting the image
     * @throws IOException if an I/O error occurs while converting the image
     */
    Image convert(Image src, Map hints) throws ImageException, IOException;
    
    /**
     * Returns the flavor that this converter converts images into.
     * @return the target flavor
     */
    ImageFlavor getTargetFlavor();
    
    /**
     * Returns the flavor that this converter expects.
     * @return the source flavor
     */
    ImageFlavor getSourceFlavor();
    
    /**
     * Returns the conversion penalty for the conversion that this implementation supports.
     * @return the conversion penalty (must be a non-negative integer)
     */
    int getConversionPenalty();
    
}
