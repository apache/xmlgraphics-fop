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

package org.apache.fop.fonts;

import java.io.File;

import org.apache.fop.apps.FOPException;


/**
 * Fop cache (currently only used for font info caching)
 */
public interface FontCacheManager {

    /**
     * Loads the font cache into memory from the given file.
     * @param file the serialized font cache
     * @return the de-serialized font cache
     */
    FontCache load(File file);

    /**
     * Serializes the font cache to file.
     * @param file the file to serialize the font cache to
     * @throws FOPException if an error occurs serializing the font cache
     */
    void save(File file) throws FOPException;

    /**
     * Deletes the font cache from the file-system.
     * @param file delete the serialized font cache
     * @throws FOPException if an error occurs deleting the font cache
     */
    void delete(File file) throws FOPException;

}
