/*
 *
 * Copyright 2004 The Apache Software Foundation.
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
 * Created on 24/05/2004
 * $Id$
 */
package org.apache.fop.fonts;

import org.apache.fop.apps.FOPException;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class FontException extends FOPException {
    private static final String tag = "$Name$";
    private static final String revision = "$Revision";

    public FontException(String detail) {
        super(detail);
    }

    public FontException(Throwable e) {
        super(e);
    }

}
