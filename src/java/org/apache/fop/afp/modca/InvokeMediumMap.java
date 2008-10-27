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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Invoke Medium Map structured field identifies the Medium Map that is to
 * become active for the document. An Invoke Medium Map structured field affects
 * the document's current environment. The Medium Map's effect on current environment
 * parameter values lasts until a new Medium Map is invoked.
 */
public class InvokeMediumMap extends AbstractNamedAFPObject {

    /**
     * Constructor for the Invoke Medium Map
     * 
     * @param name the name of the medium map
     */
    public InvokeMediumMap(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {

        byte[] data = new byte[17];
        copySF(data, Type.MAP, Category.MEDIUM_MAP);

        // Set the total record length
        byte[] len = BinaryUtils.convert(16, 2); //Ignore first byte
        data[1] = len[0];
        data[2] = len[1];

        os.write(data);
    }
}