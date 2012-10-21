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

package org.apache.fop.render.intermediate;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.FontInfo;

/**
 * This interface is implemented by classes that configure an {@link IFDocumentHandler} instance.
 */
public interface IFDocumentHandlerConfigurator {

    /**
     * Configures a intermediate format document handler.
     * @param documentHandler the document handler instance
     * @throws FOPException if an error occurs while configuring the object
     */
    void configure(IFDocumentHandler documentHandler) throws FOPException;

    /**
     * Sets up the {@link FontInfo} object for the IFDocumentHandler.
     * @param documentHandler the document handler instance
     * @param fontInfo the font info object to set up
     * @throws FOPException if an error occurs while configuring the object
     */
    void setupFontInfo(String mimeType, FontInfo fontInfo) throws FOPException;

}
