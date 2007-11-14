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
 
package org.apache.fop.image2;

/**
 * Represents an instance of an image (bitmap or vector graphic). The image may or may not be loaded
 * into memory. Implementing classes will expose additional methods to access the actual image data.
 */
public interface Image {

    /**
     * Returns an object with basic information (URI, MIME type, intrinsic size) about the image. 
     * @return the image information object
     */
    ImageInfo getInfo();
    
    /**
     * Returns the image's intrinsic size. This is a shortcut for getInfo().getSize().
     * @return the image's intrinsic size
     */
    ImageSize getSize();
    
    /**
     * Returns the flavor of the image.
     * @return the image flavor
     */
    ImageFlavor getFlavor();
    
}
