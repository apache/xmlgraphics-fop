/*
 *
 * Copyright 2004 The Apache Software Foundation.
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
 * Created on 16/04/2004
 * $Id$
 */
package org.apache.fop.area;

/**
 * This class gathers all of of the components necessary to set up the basic
 * page area precursors for the resolution of <code>fo:flow</code> and
 * <code>fo:static-content</code> elements.
 * 
 * @author pbw
 * @version $Revision$ $Name$
 */
public class Page {
    
    /**
     * Constructs a null page in the absence of a particular
     * <code>FoSimplePageMaster</code>.  All dimensions are undefined, and
     * only the minimal set of areas is constructed.
     */
    public Page() {
        
    }
}
