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
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
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
package org.apache.fop.datatypes;

/**
 * a "progression-dimension" quantity
 * ex. block-progression-dimension, inline-progression-dimension
 * corresponds to the triplet min-height, height, max-height (or width)
 */
public class LengthRange {

    private Length minimum;
    private Length optimum;
    private Length maximum;
  private static final int MINSET=1;
  private static final int OPTSET=2;
  private static final int MAXSET=4;
  private int bfSet = 0; // bit field

    /**
     * set the space values, and make sure that min <= opt <= max
     */
  public LengthRange (Length l) {
	this.minimum = l;
	this.optimum = l;
	this.maximum = l;
    }

    /** Set minimum value to min if it is <= optimum or optimum isn't set
     */
    public void setMinimum(Length min) {
	if ((bfSet&OPTSET)==0) {
	    if ((bfSet&MAXSET)!=0 && (min.mvalue() > maximum.mvalue()))
		min = maximum;
	}
	else if (min.mvalue() > optimum.mvalue())
	    min = optimum;
	minimum = min;
	bfSet |= MINSET;
    }

    /** Set maximum value to max if it is >= optimum or optimum isn't set
     */
    public void setMaximum(Length max) {
	if ((bfSet&OPTSET)==0) {
	    if ((bfSet&MINSET) != 0 && (max.mvalue() < minimum.mvalue()))
		max = minimum;
	}
	else if (max.mvalue() < optimum.mvalue())
	    max = optimum;
	maximum = max;
	bfSet |= MAXSET;
    }


    /**
     * Set the optimum value.
     */
    public void setOptimum(Length opt) {
	if (((bfSet&MINSET)==0 || opt.mvalue() >= minimum.mvalue()) &&
	    ((bfSet&MAXSET)==0 || opt.mvalue() <= maximum.mvalue())) {
	  optimum = opt;
	  bfSet |= OPTSET;
	}
    }

    public Length getMinimum() {
	return this.minimum;
    }

    public Length getMaximum() {
	return this.maximum;
    }

    public Length getOptimum() {
	return this.optimum;
    }
}
