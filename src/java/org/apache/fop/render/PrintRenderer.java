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

package org.apache.fop.render;

// FOP
import org.apache.fop.apps.Document;
import org.apache.fop.fo.FOTreeControl;
import org.apache.fop.fonts.FontSetup;

// Java
import java.util.List;

/** Abstract base class of "Print" type renderers.  */
public abstract class PrintRenderer extends AbstractRenderer {

    /** Font configuration */
    protected FOTreeControl fontInfo;

    /** list of fonts */
    protected List fontList = null;

    /**
     * Set up the font info
     *
     * @param fontInfo  font info to set up
     */
    public void setupFontInfo(FOTreeControl foTreeControl) {
        this.fontInfo = foTreeControl;
        FontSetup.setup((Document)fontInfo, fontList);
    }

}
