/*
 * $Id: PropertyException.java,v 1.3.4.3 2003/03/31 02:38:43 pbwest Exp $
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */

package org.apache.fop.fo.expr;

import org.apache.fop.apps.FOPException;

public class PropertyException extends FOPException {
    private static final String tag = "$Name:  $";
    private static final String revision = "$Revision: 1.3.4.3 $";

    public PropertyException(String detail) {
        super(detail);
    }

    public PropertyException(Throwable e) {
        super(e);
    }

}
