/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.fop.fo;

import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentLength;
import org.apache.fop.datatypes.TableColLength;
import org.apache.fop.fo.expr.Numeric;
import org.apache.fop.apps.FOPException;

public class LengthProperty extends Property {

  public static class Maker extends Property.Maker {

    public /* protected*/ Maker(String name) {
	super(name);
    }

    /**
    protected Property checkPropertyKeywords(String value) {
      if (isAutoLengthAllowed() && value.equals("auto")) {
        return new LengthProperty(Length.AUTO);
      }
      return null;
    }
    **/

    protected boolean isAutoLengthAllowed() {
      return false;
    }

    public Property convertProperty(Property p, PropertyList propertyList,
				       FObj fo) throws FOPException {
      if (isAutoLengthAllowed()) {
	String pval = p.getString();
	if (pval != null && pval.equals("auto"))
	  return new LengthProperty(Length.AUTO);
      }
      if (p instanceof LengthProperty)
	return p;
      Length val = p.getLength();
      if (val != null)
	return new LengthProperty(val);
      return convertPropertyDatatype(p, propertyList, fo);
    }
  }

  /*
  public static Property.Maker maker(String prop) {
    return new Maker(prop);
  }
  */

  /** This object may be also be a subclass of Length, such
   * as PercentLength, TableColLength.
   */
  private Length length ;

  public LengthProperty(Length length) {
    this.length = length;
    //System.err.println("Set LengthProperty: " + length.toString());
  }

  public Numeric getNumeric() {
    // Can't just do new Numeric(length) because it always uses
    // the constructor for Length!
    // Otherwise, must make each class know about Numeric...
    // ie, return length.asNumeric(): cleaner
    if (length instanceof PercentLength)
      return new Numeric((PercentLength)length);
    if (length instanceof TableColLength)
      return new Numeric((TableColLength)length);
    return new Numeric(length);
  }

  public Length getLength() { return this.length; }

  public Object getObject() { return this.length; }

}
