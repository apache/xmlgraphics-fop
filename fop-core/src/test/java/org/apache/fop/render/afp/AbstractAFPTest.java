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

import java.io.File;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.AbstractRenderingTest;

/**
 * Abstract base class for AFP verification tests.
 */
abstract class AbstractAFPTest extends AbstractRenderingTest {

    /**
     * Renders a test file.
     * @param ua the user agent (with override set!)
     * @param resourceName the resource name for the FO file
     * @param suffix a suffix for the output filename
     * @return the output file
     * @throws Exception if an error occurs
     */
    protected File renderFile(FOUserAgent ua, String resourceName, String suffix)
                throws Exception {
        return renderFile(ua, resourceName, suffix, MimeConstants.MIME_AFP);
    }


}
