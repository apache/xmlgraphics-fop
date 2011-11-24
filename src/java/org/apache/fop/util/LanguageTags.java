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

package org.apache.fop.util;

import java.util.Locale;

/**
 * Provides utility methods for manipulating language tags compliant with the
 * RFC 3066 specification available at http://www.ietf.org/rfc/rfc3066.txt. A
 * typical language tag is a 2-letter language code sometimes followed by a country
 * code. For example: en, en-US.
 */
public final class LanguageTags {

    private LanguageTags() {
    }

    /**
     * Converts the given locale to an RFC 3066 compliant language tag.
     *
     * @param locale a locale
     * @return the corresponding language tag
     * @throws NullPointerException if the specified locale is null
     */
    public static String toLanguageTag(Locale locale) {
        StringBuffer sb = new StringBuffer(5);
        sb.append(locale.getLanguage());
        String country = locale.getCountry();
        if (country.length() > 0) {
            sb.append('-');
            sb.append(country);
        }
        return sb.toString();
    }

    /**
     * Converts an RFC 3066 compliant language tag to a locale.
     *
     * @throws NullPointerException if the specified language tag is null
     */
    public static Locale toLocale(String languageTag) {
        String[] parts = languageTag.split("-");
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else {
            return new Locale(parts[0], parts[1]);
        }
    }
}
