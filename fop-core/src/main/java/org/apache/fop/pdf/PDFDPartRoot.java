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
package org.apache.fop.pdf;

public class PDFDPartRoot extends PDFDictionary {
    private PDFArray parts = new PDFArray();
    protected PDFDPart dpart;

    public PDFDPartRoot(PDFDocument document) {
        put("Type", new PDFName("DPartRoot"));
        dpart = new PDFDPart(this);
        document.registerTrailerObject(dpart);
        PDFArray dparts = new PDFArray();
        dparts.add(parts);
        dpart.put("DParts", dparts);
        put("DPartRootNode", dpart.makeReference());
        PDFArray nodeNameList = new PDFArray();
        nodeNameList.add(new PDFName("root"));
        nodeNameList.add(new PDFName("record"));
        put("NodeNameList", nodeNameList);
    }

    public void add(PDFDPart part) {
        parts.add(part);
    }
}
