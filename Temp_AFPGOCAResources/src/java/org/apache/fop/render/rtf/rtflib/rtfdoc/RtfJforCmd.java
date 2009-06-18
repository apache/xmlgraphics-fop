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

import java.io.Writer;
import java.util.Iterator;
import java.io.IOException;

/**
 * Process "jfor-cmd"
 */
public class RtfJforCmd extends RtfContainer {

    private static final String PARA_KEEP_ON = "para-keep:on";
    private static final String PARA_KEEP_OFF = "para-keep:off";

    private final RtfAttributes attrib;
    private ParagraphKeeptogetherContext paragraphKeeptogetherContext;



    RtfJforCmd(RtfContainer parent, Writer w, RtfAttributes attrs) throws IOException {
        super((RtfContainer)parent, w);
        attrib = attrs;
        paragraphKeeptogetherContext = ParagraphKeeptogetherContext.getInstance();
    }


    /**
     *
     * @return true (alway)
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * Execute all jfor-cmd commands
     * TODO: Consider creating one class for each jfor command.
     */
    public void process() {
        for (Iterator it = attrib.nameIterator(); it.hasNext();) {
            final String cmd = (String)it.next();

            if (cmd.equals(PARA_KEEP_ON)) {
                ParagraphKeeptogetherContext.keepTogetherOpen();
            } else if (cmd.equals(PARA_KEEP_OFF)) {
                ParagraphKeeptogetherContext.keepTogetherClose();
            } else {
//                this.getRtfFile ().getLog ().logInfo
//                        ("JFOR-CMD ignored, command not recognised:"+cmd);
            }

         }


    }

}
