/*
 * FObject.java
 * Created: Sun Jan 27 01:35:24 2002
 *
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;


/**
 * Base class for all Flow Objects
 */
public class FObject {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private int foIndex;

    public FObject(int foIndex) {
        this.foIndex = foIndex;
    }

    public FObject(String foName) {
        foIndex = FObjects.getFoIndex(foName);
    }

}// FObject
