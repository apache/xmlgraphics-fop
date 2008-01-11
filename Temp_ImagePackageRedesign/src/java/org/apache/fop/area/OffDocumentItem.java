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

/**
 * Interface for objects that are processed by the renderer outside
 * of the actual document.
 * An object implementing this interface can be handled by the renderer according to these
 * possibilities: IMMEDIATELY, AFTER_PAGE or END_OF_DOC.
 */
public interface OffDocumentItem {

    /**
     * Process this extension immediately when
     * being handled by the area tree.
     */
    int IMMEDIATELY = 0;

    /**
     * Process this extension after the next page is rendered
     * or prepared when being handled by the area tree.
     */
    int AFTER_PAGE = 1;

    /**
     * Process this extension at the end of the document once
     * all pages have been fully rendered.
     */
    int END_OF_DOC = 2;

    
    /**
     * Get an indicator of when this item should be processed
     * @return int constant (IMMEDIATELY, AFTER_PAGE, END_OF_DOC)
     */
    int getWhenToProcess();

    /**
     * Return a human-readable name for this ODI (for error messages, etc.)
     * @return String name of ODI
     */
    String getName();
    
}
