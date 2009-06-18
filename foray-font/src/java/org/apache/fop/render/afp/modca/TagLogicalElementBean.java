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

package org.apache.fop.render.afp.modca;

/**
 * The TagLogicalElementBean provides a bean for holding the attributes of
 * a tag logical element as key value pairs.
 * <p/>
 */
public class TagLogicalElementBean {

    /** The key attribute */
    private String _key;

    /** The value attribute */
    private String _value;

    /**
     * Constructor for the TagLogicalElementBean.
     * @param key the key attribute
     * @param value the value attribute
     */
    public TagLogicalElementBean(String key, String value) {
        _key = key;
        _value = value;
    }

    /**
     * Getter for the key attribute.
     * @return the key
     */
    public String getKey() {
        return _key;
    }

    /**
     * Getter for the value attribute.
     * @return the value
     */
    public String getValue() {
        return _value;
    }

}
