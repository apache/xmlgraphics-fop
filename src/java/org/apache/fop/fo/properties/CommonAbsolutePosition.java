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

package org.apache.fop.fo.properties;

/**
 * Store all common absolute position properties.
 * See Sec. 7.5 of the XSL-FO Standard.
 * Public "structure" allows direct member access.
 */
public class CommonAbsolutePosition {
    public int absolutePosition;
    public int top;
    public int right;
    public int bottom;
    public int left;

    public CommonAbsolutePosition() {
    }

}
