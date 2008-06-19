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

package org.apache.fop.prototype.knuth;

import org.apache.fop.prototype.TypographicElement;

/**
 * A Knuth element.
 */
public class KnuthElement implements TypographicElement {

    private int length;

    private String content;

    /**
     * @param length
     * @param content
     */
    protected KnuthElement(int length, String content) {
        this.length = length;
        this.content = content;
    }

    /**
     * @param length
     */
    protected KnuthElement(int length) {
        this(length, "");
    }

    public int getLength() {
        return length;
    }

    public String getLabel() {
        return content;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        if (content.isEmpty()) {
            return "";
        } else {
            return " \"" + content + "\"";
        }
    }

    public boolean isGlue() {
        return false;
    }

    public boolean isBox() {
        return false;
    }

    public boolean isPenalty() {
        return false;
    }
}
