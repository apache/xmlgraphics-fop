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

import java.io.IOException;

/**  Interface for RtfElements that can contain RtfText elements
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public interface IRtfTextContainer {
    /**
     * Close current text element, if any, and start a new one
     * @param str if not null, added to the RtfText created
     * @param attr attributes for text
     * @return new text object
     * @throws IOException for I/O problems
     */
    RtfText newText(String str, RtfAttributes attr) throws IOException;

    /**
     * Close current text run, if any, and start a new one with default attributes
     * @param str if not null, added to the RtfText created
     * @return a new text object
     * @throws IOException for I/O problems
     */
    RtfText newText(String str) throws IOException;

    /**
     * Add a line break
     * @throws IOException for I/O problems
     */
    void newLineBreak() throws IOException;

    /**
     * Text containers usually provide default attributes for all texts that they contain.
     * @return a copy of the container's attributes.
     */
    RtfAttributes getTextContainerAttributes();
}
