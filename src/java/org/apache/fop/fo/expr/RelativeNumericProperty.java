/*
 * $Id$
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
package org.apache.fop.fo.expr;

import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.properties.Property;


/**
 * This class represent a node in a property expression tree. 
 * It is created when an operation involve relative expression and is used
 * to delay evaluation of the operation until the time where getNumericValue()
 * or getValue() is called. 
 */
public class RelativeNumericProperty extends Property implements Numeric, Length {
    public static final int ADDITION = 1;
    public static final int SUBTRACTION = 2;
    public static final int MULTIPLY = 3;
    public static final int DIVIDE = 4;
    public static final int MODULO = 5;
    public static final int NEGATE = 6;
    public static final int ABS = 7;
    public static final int MAX = 8;
    public static final int MIN = 9;
    
    // Used in the toString() method, indexed by operation id.
    private static String operations = " +-*/%";
    
    /**
     * The operation identifier.
     */
    private int operation;
    /**
     * The first (or only) operand.
     */
    private Numeric op1;
    /**
     * The second operand.
     */
    private Numeric op2;
    /**
     * The dimension of the result.
     */
    private int dimension;
    
    /**
     * Constructor for a two argument operation.
     * @param operation the operation opcode: ADDITION, SUBTRACTION, ...
     * @param op1 the first operand.
     * @param op2 the second operand
     */
    public RelativeNumericProperty(int operation, Numeric op1, Numeric op2) {
        this.operation = operation;
        this.op1 = op1;
        this.op2 = op2;
        // Calculate the dimension. We can do now.
        switch (operation) {
        case MULTIPLY:
            dimension = op1.getDimension() + op2.getDimension();
            break;
        case DIVIDE:
            dimension = op1.getDimension() - op2.getDimension();
            break;
        default:
            dimension = op1.getDimension();
        }
    }

    /**
     * Constructor for a one argument operation.
     * @param operation the operation opcode: NEGATE, ABS
     * @param op the operand.
     */
    public RelativeNumericProperty(int operation, Numeric op) {
        this.operation = operation;
        this.op1 = op;
        this.dimension = op.getDimension();
    }

    /**
     * Return a resolved (calculated) Numeric with the value of the expression.
     * @throws PropertyException when an exception occur during evaluation.
     */
    private Numeric getResolved() throws PropertyException {
        Numeric n;
        switch (operation) {
        case ADDITION:
            return NumericOp.addition2(op1, op2);
        case SUBTRACTION:
            return NumericOp.subtraction2(op1, op2);
        case MULTIPLY:
            return NumericOp.multiply2(op1, op2);
        case DIVIDE:
            return NumericOp.divide2(op1, op2);
        case MODULO:
            return NumericOp.modulo2(op1, op2);
        case NEGATE:
            return NumericOp.negate2(op1);
        case ABS:
            return NumericOp.abs2(op1);
        case MAX:
            return NumericOp.max2(op1, op2);
        case MIN:
            return NumericOp.min2(op1, op2);
        default:
            throw new PropertyException("Unknown expr operation " + operation);  
        }
    }

    /**
     * Return the resolved (calculated) value of the expression. 
     * @see Numeric#getNumericValue()
     */
    public double getNumericValue() throws PropertyException {
        return getResolved().getNumericValue();
    }

    /**
     * Return the dimension of the expression
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Return false since an expression is only created when there is relative
     * numerics involved. 
     */
    public boolean isAbsolute() {
        return false;
    }

    /**
     * Cast this numeric as a Length. 
     */
    public Length getLength() {
        if (dimension == 1) {
            return this;
        }
        System.err.print("Can't create length with dimension " + dimension);
        return null;
    }

    public Numeric getNumeric() {
        return this;
    }

    /**
     * Return a resolved length.
     */
    public int getValue() {
        try {
            return (int) getNumericValue();
        } catch (PropertyException exc) {
            exc.printStackTrace();
        }
        return 0;
    }

    /**
     * Return false, since a numeric is never the "auto" enum.
     */
    public boolean isAuto() {
        return false;
    }

    /**
     * Return a string represention of the expression. Only used for debugging. 
     */
    public String toString() {
        switch (operation) {
        case ADDITION: case SUBTRACTION: 
        case DIVIDE: case MULTIPLY: case MODULO:
            return "(" + op1 + " " + operations.charAt(operation) + op2 + ")";
        case NEGATE:
            return "-" + op1;
        case MAX:
            return "max(" + op1 + ", " + op2 + ")";
        case MIN:
           return "min(" + op1 + ", " + op2 + ")";
        case ABS:
           return "abs(" + op1 + ")";
        }
        return "unknown operation " + operation;
    }
}
