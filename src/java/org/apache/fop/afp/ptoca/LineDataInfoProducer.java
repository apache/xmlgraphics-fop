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

package org.apache.fop.afp.ptoca;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPLineDataInfo;

/**
 * {@link PtocaProducer} implementation that interprets {@link AFPLineDataInfo} objects.
 */
public class LineDataInfoProducer implements PtocaProducer, PtocaConstants {

    /** Static logging instance */
    private static final Log log // CSOK: ConstantName
        = LogFactory.getLog(LineDataInfoProducer.class);

    private AFPLineDataInfo lineDataInfo;

    /**
     * Main constructor.
     * @param lineDataInfo the info object
     */
    public LineDataInfoProducer(AFPLineDataInfo lineDataInfo) {
        this.lineDataInfo = lineDataInfo;
    }

    /** {@inheritDoc} */
    public void produce(PtocaBuilder builder) throws IOException {
        builder.setTextOrientation(lineDataInfo.getRotation());
        int x1 = ensurePositive(lineDataInfo.getX1());
        int y1 = ensurePositive(lineDataInfo.getY1());
        builder.absoluteMoveBaseline(y1);
        builder.absoluteMoveInline(x1);
        builder.setExtendedTextColor(lineDataInfo.getColor());

        int x2 = ensurePositive(lineDataInfo.getX2());
        int y2 = ensurePositive(lineDataInfo.getY2());
        int thickness = lineDataInfo.getThickness();
        if (y1 == y2) {
            builder.drawIaxisRule(x2 - x1, thickness);
        } else if (x1 == x2) {
            builder.drawBaxisRule(y2 - y1, thickness);
        } else {
            log.error("Invalid axis rule: unable to draw line");
            return;
        }
    }

    private static int ensurePositive(int value) {
        if (value < 0) {
            return 0;
        }
        return value;
    }

}
