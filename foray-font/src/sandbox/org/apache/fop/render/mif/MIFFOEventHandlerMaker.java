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

package org.apache.fop.render.mif;

import java.io.OutputStream;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.render.AbstractFOEventHandlerMaker;

/**
 * Maker class for MIF support.
 */
public class MIFFOEventHandlerMaker extends AbstractFOEventHandlerMaker {

    private static final String[] MIMES = new String[] {MimeConstants.MIME_MIF};
    
    
    /** @see org.apache.fop.render.AbstractFOEventHandlerMaker */
    public FOEventHandler makeFOEventHandler(FOUserAgent ua, OutputStream out) {
        return new MIFHandler(ua, out);
    }

    /** @see org.apache.fop.render.AbstractFOEventHandlerMaker#needsOutputStream() */
    public boolean needsOutputStream() {
        return true;
    }

    /** @see org.apache.fop.render.AbstractFOEventHandlerMaker#getSupportedMimeTypes() */
    public String[] getSupportedMimeTypes() {
        return MIMES;
    }

}
