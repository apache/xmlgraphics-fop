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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

/**
 *
 *     This context is used to manage the "keepn" RTF attribute
 *  Used by ParagraphBuilder and JforCmd
 *
 */

public final class ParagraphKeeptogetherContext {

    private static int paraKeepTogetherOpen;
    private static boolean paraResetProperties;

    private ParagraphKeeptogetherContext() {
    }

    /**
     *  @return the level of current "keep whith next" paragraph
     */
    public static int getKeepTogetherOpenValue() {
        return paraKeepTogetherOpen;
    }

    /** Open a new "keep with next" paragraph */
    public static void keepTogetherOpen() {
        paraKeepTogetherOpen++;
    }

    /** Close a "keep with next" paragraph */
    public static void keepTogetherClose() {
        if (paraKeepTogetherOpen > 0) {
            paraKeepTogetherOpen--;

            //If the \pard control word is not present, the current paragraph
            //inherits all paragraph properties.
            //Also the next paragraph must reset the properties otherwise the \keepn don't stop.
            paraResetProperties = (paraKeepTogetherOpen == 0);
        }
    }

    /**
     * @return true if the next paragraph must reset the properties
     */
    public static boolean paragraphResetProperties() {
        return paraResetProperties;
    }

    /** Reset the flag if the paragraph properties have been resested */
    public static void setParagraphResetPropertiesUsed() {
        paraResetProperties = false;
    }

}
