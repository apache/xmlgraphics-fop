/*
 * $Id: SpaceVal.java,v 1.4 2003/03/05 20:38:26 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.traits;

import org.apache.fop.fo.Property;
import org.apache.fop.fo.SpaceProperty;
import org.apache.fop.fo.Constants;

/**
 * Store a single Space property value in simplified form, with all
 * Length values resolved. See section 4.3 in the specs.
 */
public class SpaceVal {
    
    private final MinOptMax space;
    private final boolean bConditional;
    private final boolean bForcing;
    private final int iPrecedence; //  Numeric only, if forcing, set to 0

    /**
     * Constructor for SpaceVal objects based on Space objects.
     * @param spaceprop Space object to use
     */
    public SpaceVal(SpaceProperty spaceprop) {
        space = new MinOptMax(spaceprop.getMinimum().getLength().getValue(),
                              spaceprop.getOptimum().getLength().getValue(),
                              spaceprop.getMaximum().getLength().getValue());
        bConditional = 
                (spaceprop.getConditionality().getEnum() == Constants.DISCARD);
        Property precProp = spaceprop.getPrecedence();
        if (precProp.getNumber() != null) {
            iPrecedence = precProp.getNumber().intValue();
            bForcing = false;
        } else {
            bForcing = (precProp.getEnum() == Constants.FORCE);
            iPrecedence = 0;
        }
    }

    /**
     * Constructor for SpaceVal objects based on the full set of properties.
     * @param space space to use
     * @param bConditional Conditionality value
     * @param bForcing Forcing value
     * @param iPrecedence Precedence value
     */
    public SpaceVal(MinOptMax space, boolean bConditional,
                    boolean bForcing, int iPrecedence) {
        this.space = space;
        this.bConditional = bConditional;
        this.bForcing = bForcing;
        this.iPrecedence = iPrecedence;
    }

    /**
     * Returns the Conditionality value.
     * @return the Conditionality value
     */
    public boolean isConditional() {
        return bConditional;
    }

    /**
     * Returns the Forcing value.
     * @return the Forcing value
     */
    public boolean isForcing() {
        return bForcing;
    }

    /**
     * Returns the Precedence value.
     * @return the Precedence value
     */
    public int getPrecedence() {
        return iPrecedence;
    }

    /**
     * Returns the Space value.
     * @return the Space value
     */
    public MinOptMax getSpace() {
        return space;
    }

    public String toString() {
        return "SpaceVal: " + getSpace().toString();
    }
}

