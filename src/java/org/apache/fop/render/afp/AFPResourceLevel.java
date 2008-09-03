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

package org.apache.fop.render.afp;

/**
 * A resource level
 */
public class AFPResourceLevel {

    /** directly in page **/
    public static final int INLINE = 0;

    /** page level **/
    public static final int PAGE = 1;

    /** page group level **/
    public static final int PAGE_GROUP = 2;

    /** document level **/
    public static final int DOCUMENT = 3;

    /** print file level **/
    public static final int PRINT_FILE = 4;

    /** external level **/
    public static final int EXTERNAL = 5;

    private static final String NAME_INLINE = "inline";
    private static final String NAME_PAGE = "page";
    private static final String NAME_PAGE_GROUP = "page-group";
    private static final String NAME_DOCUMENT = "document";
    private static final String NAME_PRINT_FILE = "print-file";
    private static final String NAME_EXTERNAL = "external";

    private static final String[] NAMES = new String[] {
        NAME_INLINE, NAME_PAGE, NAME_PAGE_GROUP, NAME_DOCUMENT, NAME_PRINT_FILE, NAME_EXTERNAL
    };


    /** where the resource will reside in the AFP output */
    private int level = PRINT_FILE; // default is print-file level

    /** the external resource group file path */
    private String extFilePath = null;

    /**
     * Sets the resource placement level within the AFP output
     *
     * @param levelString the resource level (page, page-group, document, print-file or external)
     * @return true if the resource level was successfully set
     */
    public static AFPResourceLevel valueOf(String levelString) {
        if (levelString != null) {
            levelString = levelString.toLowerCase();
            AFPResourceLevel resourceLevel = null;
            for (int i = 0; i < NAMES.length; i++) {
                if (NAMES[i].equals(levelString)) {
                    resourceLevel = new AFPResourceLevel(i);
                    break;
                }
            }
            return resourceLevel;
        }
        return null;
    }

    /**
     * Main constructor
     *
     * @param level the resource level
     */
    public AFPResourceLevel(int level) {
        setLevel(level);
    }

    /**
     * Sets the resource level
     *
     * @param level the resource level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns true if this is at page level
     *
     * @return true if this is at page level
     */
    public boolean isPage() {
       return level == PAGE;
    }

    /**
     * Returns true if this is at page group level
     *
     * @return true if this is at page group level
     */
    public boolean isPageGroup() {
        return level == PAGE_GROUP;
    }

    /**
     * Returns true if this is at document level
     *
     * @return true if this is at document level
     */
    public boolean isDocument() {
        return level == DOCUMENT;
    }

    /**
     * Returns true if this is at external level
     *
     * @return true if this is at external level
     */
    public boolean isExternal() {
        return level == EXTERNAL;
    }

    /**
     * Returns true if this is at print-file level
     *
     * @return true if this is at print-file level
     */
    public boolean isPrintFile() {
        return level == PRINT_FILE;
    }

    /**
     * Returns true if this resource level is inline
     *
     * @return true if this resource level is inline
     */
    public boolean isInline() {
        return level == INLINE;
    }

    /**
     * Returns the destination file path of the external resource group file
     *
     * @return the destination file path of the external resource group file
     */
    public String getExternalFilePath() {
        return this.extFilePath;
    }

    /**
     * Sets the external destination of the resource
     *
     * @param filePath the external resource group file
     */
    public void setExternalFilePath(String filePath) {
        this.extFilePath = filePath;
    }

    /** {@inheritDoc} */
    public String toString() {
        return NAMES[level] +  (isExternal() ? ", file=" + extFilePath : "");
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof AFPResourceLevel)) {
            return false;
        }

        AFPResourceLevel rl = (AFPResourceLevel)obj;
        return (level == level)
            && (extFilePath == rl.extFilePath
                    || extFilePath != null && extFilePath.equals(rl.extFilePath));
    }

    /** {@inheritDoc} */
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + level;
        hash = 31 * hash + (null == extFilePath ? 0 : extFilePath.hashCode());
        return hash;
    }
}