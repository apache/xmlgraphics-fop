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
 * <p>Interface for RtfElements that can contain RtfTables.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch)
 * and Boris Poudérous.</p>
 */
public interface IRtfTableContainer {
   /**
    * Close current table, if any, and start a new one
    * @param tc Table Columns Info
    * @return new table object
    * @throws IOException for I/O problems
    */
    RtfTable newTable(ITableColumnsInfo tc) throws IOException;

   /**
    * Close current table, if any, and start a new one
    * @param attrs for the Table attributes
    * @param tc to process number-columns-spanned attribute
    * @return new table object
    * @throws IOException for I/O problems
    */
    RtfTable newTable(RtfAttributes attrs, ITableColumnsInfo tc) throws IOException;
}
