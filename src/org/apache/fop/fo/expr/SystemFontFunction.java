/*
 * SystemFontFunction.java
 * Implement the system font function
 * $Id$
 * @author <a href="mailto: "Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.fo.expr;

import org.apache.fop.fo.Properties.Font;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.FunctionNotImplementedException;

/**
 * Implement the system font function.
 * <p>Eventually, provision will have to be made for the configuration of
 * system font names and characteristics on a per-instance basis.
 */
public class SystemFontFunction {
    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    public static PropertyValue systemFont(String font, int property)
        throws FunctionNotImplementedException
    {
        throw new FunctionNotImplementedException("system-font");
    }
}
