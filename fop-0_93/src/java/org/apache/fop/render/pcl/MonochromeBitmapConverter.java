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

package org.apache.fop.render.pcl;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

/**
 * Interface for converters that convert grayscale images to monochrome (1-bit) bitmap images.
 */
public interface MonochromeBitmapConverter {

    /**
     * Sets a hint to the implementation
     * @param name the name of the hint
     * @param value the value
     */
    void setHint(String name, String value);
    
    /**
     * Converts a grayscale bitmap image to a monochrome (1-bit) b/w bitmap image. 
     * @param img the grayscale image
     * @return the converted monochrome image
     */
    RenderedImage convertToMonochrome(BufferedImage img);
    
}
