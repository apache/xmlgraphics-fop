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
 * Store all common hyphenation properties.
 * See Sec. 7.9 of the XSL-FO Standard.
 * Public "structure" allows direct member access.
 */
public class CommonHyphenation {

    public int hyphenate;      // Enum true or false: store as boolean!
    public char hyphenationChar;
    public int hyphenationPushCharacterCount;
    public int hyphenationRemainCharacterCount;
    public String language;    // Language code or enum "NONE"
    public String country;     // Country code or enum "NONE"

}
