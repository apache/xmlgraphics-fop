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
 
package org.apache.fop.util;

/**
 * This interface defines constants used by the ASCII85 filters.
 *
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id$
 */
public interface ASCII85Constants {

    /** Special character "z" stands for four NULL bytes (short-cut for !!!!!) */
    public static final int ZERO          = 0x7A; //"z"
    /** ZERO as a byte array */
    public static final byte[] ZERO_ARRAY = {(byte)ZERO};
    /** The start index for ASCII85 characters (!) */
    public static final int START         = 0x21; //"!"
    /** The end index for ASCII85 characters (u) */
    public static final int END           = 0x75; //"u"
    /** The EOL indicator (LF) */
    public static final int EOL           = 0x0A; //"\n"
    /** The EOD (end of data) indicator */
    public static final byte[] EOD        = {0x7E, 0x3E}; //"~>"

    /** Array of powers of 85 (4, 3, 2, 1, 0) */
    public static final long POW85[] = new long[] {85 * 85 * 85 * 85, 
                                                    85 * 85 * 85,
                                                    85 * 85,
                                                    85,
                                                    1};

    /*
    public static final long BASE85_4 = 85;
    public static final long BASE85_3 = BASE85_4 * BASE85_4;
    public static final long BASE85_2 = BASE85_3 * BASE85_4;
    public static final long BASE85_1 = BASE85_2 * BASE85_4;
    */

}


