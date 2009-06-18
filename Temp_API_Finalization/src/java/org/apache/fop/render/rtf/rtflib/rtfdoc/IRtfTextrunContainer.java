/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project.
 */

import java.io.IOException;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTextrun;

/**  Interface which enables an implementing class to contain linear text runs.
 *  @author Peter Herweg, pherweg@web.de
 */

public interface IRtfTextrunContainer {
    
    /**
     * Returns the current RtfTextrun object.
     * Opens a new one if necessary.
     * @return The RtfTextrun object
     * @throws IOException Thrown when an IO-problem occurs
     */
    RtfTextrun getTextrun() throws IOException;
}
