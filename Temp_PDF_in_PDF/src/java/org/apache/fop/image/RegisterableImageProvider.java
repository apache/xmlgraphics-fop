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
 
package org.apache.fop.image;

/**
 * This interface is used to dynamically register FopImage implementations.
 * <p>
 * NOTE: Please don't rely on this interface too much. It is a temporary measure
 * until the whole image package can be redesigned. The redesign will likely
 * provide a different mechanism to dynamically register new implementations. 
 */
public interface RegisterableImageProvider {

    /**
     * Returns the MIME type the implementation supports.
     * @return the MIME type
     */
    String getSupportedMimeType();
    
    /**
     * Returns the name of the implementation.
     * @return the name
     */
    String getName();
    
    /**
     * Returns the fully qualified class name for the implementing class.
     * @return the class name
     */
    String getClassName();

}
