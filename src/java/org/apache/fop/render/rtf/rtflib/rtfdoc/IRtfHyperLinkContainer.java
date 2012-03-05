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

/**
 * <p>Interface for RtfElements that can contain RtfHyperLinks.</p>
 *
 * <p>This work was authored by Andreas Putz (a.putz@skynamics.com).</p>
 */
public interface IRtfHyperLinkContainer extends IRtfTextContainer {
    /**
     * Creates a new hyperlink.
     * @param str Hyperlink string
     * @param attr Hyperlink attributes
     * @exception IOException on error
     * @return new Hyperlink object
     */
    RtfHyperLink newHyperLink (String str, RtfAttributes attr) throws IOException;
}