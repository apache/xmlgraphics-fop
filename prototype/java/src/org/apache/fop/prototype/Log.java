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

package org.apache.fop.prototype;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * TODO javadoc
 */
public class Log {

    public static final Logger LOG;

    private static class LogFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            StringBuilder msg = new StringBuilder();
            String className = record.getSourceClassName();
            className = className.substring(className.lastIndexOf('.') + 1);
            msg.append(className)
                    .append('.')
                    .append(record.getSourceMethodName())
                    .append(": ")
                    .append(record.getMessage())
                    .append("\n\n\n");
            return msg.toString();
        }

    }

    static {
        Handler handler = new StreamHandler(System.out, new LogFormatter());
        handler.setLevel(Level.ALL);
        LOG = Logger.getLogger("prototype");
        LOG.addHandler(handler);
        LOG.setLevel(Level.OFF);
        LOG.setUseParentHandlers(false);
    }
}
