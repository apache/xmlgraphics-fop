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
 
package org.apache.fop;

import org.apache.batik.transcoder.Transcoder;
import org.apache.fop.render.ps.PSTranscoder;

/**
 * Basic runtime test for the PS transcoder. It is used to verify that 
 * nothing obvious is broken after compiling.
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 */
public class BasicPSTranscoderTestCase extends AbstractBasicTranscoderTestCase {

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public BasicPSTranscoderTestCase(String name) {
        super(name);
    }

    /**
     * @see org.apache.fop.AbstractBasicTranscoderTestCase#createTranscoder()
     */
    protected Transcoder createTranscoder() {
        return new PSTranscoder();
    }

}
