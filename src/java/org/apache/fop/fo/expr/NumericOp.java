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

/**
 * This class contains static methods to evaluate operations on Numeric 
 * operands. If the operands are absolute numerics the result is computed
 * rigth away and a new absolute numeric is return. If one of the operands are
 * relative a n operation node is created with the operation and the operands.
 * The evaluation of the operation can then occur when getNumericValue() is
 * called.     
 */
public class NumericOp {
    /**
     * Add the two operands and return a new Numeric representing the result.
     * @param op1 The first operand.
     * @param op2 The second operand.
     * @return A Numeric representing the result.
     * @throws PropertyException If the dimension of the operand is different
     * from the dimension of this Numeric.
     */
    public static Numeric addition(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.isAbsolute() && op2.isAbsolute()) {
            return addition2(op1, op2);
        } else {
            return new RelativeNumericProperty(RelativeNumericProperty.ADDITION, op1, op2);
        }
    }
    
    public static Numeric addition2(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.getDimension() != op2.getDimension()) {
            throw new PropertyException("Can't subtract Numerics of different dimensions");
        }
        return numeric(op1.getNumericValue() + op2.getNumericValue(), op1.getDimension());
    }
    
    /**
     * Add the second operand from the first and return a new Numeric 
     * representing the result.
     * @param op1 The first operand.
     * @param op2 The second operand.
     * @return A Numeric representing the result.
     * @throws PropertyException If the dimension of the operand is different
     * from the dimension of this Numeric.
     */
    public static Numeric subtraction(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.isAbsolute() && op2.isAbsolute()) {
            return subtraction2(op1, op2);
        } else {
            return new RelativeNumericProperty(RelativeNumericProperty.SUBTRACTION, op1, op2);
        }
    }

    public static Numeric subtraction2(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.getDimension() != op2.getDimension()) {
            throw new PropertyException("Can't subtract Numerics of different dimensions");
        }
        return numeric(op1.getNumericValue() - op2.getNumericValue(), op1.getDimension());
    }
    
    /**
     * Multiply the two operands and return a new Numeric representing the 
     * result.
     * @param op1 The first operand.
     * @param op2 The second operand.
     * @return A Numeric representing the result.
     * @throws PropertyException If the dimension of the operand is different
     * from the dimension of this Numeric.
     */
    public static Numeric multiply(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.isAbsolute() && op2.isAbsolute()) {
            return multiply2(op1, op2);
        } else {
            return new RelativeNumericProperty(RelativeNumericProperty.MULTIPLY, op1, op2);
        }
    }    

    public static Numeric multiply2(Numeric op1, Numeric op2) throws PropertyException {
        return numeric(op1.getNumericValue() * op2.getNumericValue(), 
                       op1.getDimension() + op2.getDimension());
    }
    
    /**
     * Divide the second operand into the first and return a new 
     * Numeric representing the 
     * result.
     * @param op1 The first operand.
     * @param op2 The second operand.
     * @return A Numeric representing the result.
     * @throws PropertyException If the dimension of the operand is different
     * from the dimension of this Numeric.
     */
    public static Numeric divide(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.isAbsolute() && op2.isAbsolute()) {
            return divide2(op1, op2);
        } else {
            return new RelativeNumericProperty(RelativeNumericProperty.DIVIDE, op1, op2);
        }
    }
    
    public static Numeric divide2(Numeric op1, Numeric op2) throws PropertyException {
        return numeric(op1.getNumericValue() / op2.getNumericValue(), 
                       op1.getDimension() - op2.getDimension());
    }
    
    /**
     * Return the remainder of a division of the two operand Numeric.
     * @param op1 The first operand.
     * @param op2 The second operand.
     * @return A new Numeric object representing the absolute value.
     */
    public static Numeric modulo(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.isAbsolute() && op2.isAbsolute()) {
            return modulo2(op1, op2);
        } else {
            return new RelativeNumericProperty(RelativeNumericProperty.MODULO, op1, op2);
        }
    }
    
    public static Numeric modulo2(Numeric op1, Numeric op2) throws PropertyException {
        return numeric(op1.getNumericValue() % op2.getNumericValue(), op1.getDimension());
    }

    /**
     * Return the absolute value of the operand Numeric.
     * @param op1 The first operand.
     * @param op2 The second operand.
     * @return A new Numeric object representing the absolute value.
     */
    public static Numeric abs(Numeric op) throws PropertyException {
        if (op.isAbsolute()) {
            return abs2(op);
        } else {
            return new RelativeNumericProperty(RelativeNumericProperty.ABS, op);
        }
    }

    public static Numeric abs2(Numeric op) throws PropertyException {
        return numeric(Math.abs(op.getNumericValue()), op.getDimension());
    }
    
    /**
     * Return the absolute value of the operand Numeric.
     * @param op1 The first operand.
     * @param op2 The second operand.
     * @return A new Numeric object representing the absolute value.
     */
    public static Numeric negate(Numeric op) throws PropertyException {
        if (op.isAbsolute()) {
            return negate2(op);
        } else {
            return new RelativeNumericProperty(RelativeNumericProperty.NEGATE, op);
        }
    }

    public static Numeric negate2(Numeric op) throws PropertyException {
        return numeric(- op.getNumericValue(), op.getDimension());
    }
    
    /**
     * Return the largest of the two operands.
     * @param op1 The first operand.
     * @param op2 The second operand.
     * @return a Numeric which is the maximum of the two operands.
     * @throws PropertyException If the dimensions or value types of the
     * object and the operand are different.
     */
    public static Numeric max(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.isAbsolute() && op2.isAbsolute()) {
            return max2(op1, op2);
        } else {
            return new RelativeNumericProperty(RelativeNumericProperty.MAX, op1, op2);
        }
    }

    public static Numeric max2(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.getDimension() != op2.getDimension()) {
            throw new PropertyException("Arguments to max() must have same dimensions");
        }
        return op1.getNumericValue() > op2.getNumericValue() ? op1 : op2;
    }
    
    /**
     * Return the smallest of the two operands.
     * @param op1 The first operand.
     * @param op2 The second operand.
     * @return a Numeric which is the minimum of the two operands.
     * @throws PropertyException If the dimensions or value types of the
     * object and the operand are different.
     */
    public static Numeric min(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.isAbsolute() && op2.isAbsolute()) {
            return min2(op1, op2);
        } else {
            return new RelativeNumericProperty(RelativeNumericProperty.MIN, op1, op2);
        }
    }

    public static Numeric min2(Numeric op1, Numeric op2) throws PropertyException {
        if (op1.getDimension() != op2.getDimension()) {
            throw new PropertyException("Arguments to min() must have same dimensions");
        }
        return op1.getNumericValue() <= op2.getNumericValue() ? op1 : op2;
    }
    
    /**
     * Create a new absolute numeric with the specified value and dimension. 
     * @param value
     * @param dimension
     * @return a new absolute numeric.
     */
    private static Numeric numeric(double value, int dimension) {
        return new NumericProperty(value, dimension);
    }
}
