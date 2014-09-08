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

package org.apache.fop.fo;

import java.util.NoSuchElementException;

/**
 * Class providing an (FO flavored) char iterator for a string.
 */
public class StringCharIterator extends CharIterator {

    private int index;
    private String str;

    /**
     * Constructor
     * @param c the character that this iterator should iterate.
     */
    public StringCharIterator(String s) {
        this.index = -1;
        this.str = s;
    }

    /**
     * @return true if there is another element in the collection over which to
     * iterate (since this iterator only handles one character, this will return
     * false if it is past that character).
     */
    public boolean hasNext() {
        return (index + 1) < str.length();
    }

    /**
     * @return the next character, if there is one (since there is only one
     * character over which to iterate, it must be the first character).
     * @throws NoSuchElementException if past the first character
     */
    public char nextChar() throws NoSuchElementException {
        if ((index + 1) < str.length()) {
            return str.charAt(++index);
        } else {
            throw new NoSuchElementException();
        }
    }

}

