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
package org.apache.fop.fo.expr;

import org.apache.fop.fo.Property;
import org.apache.fop.fo.LengthProperty;
import org.apache.fop.fo.NumberProperty;
import org.apache.fop.fo.StringProperty;
import org.apache.fop.fo.ColorTypeProperty;
import org.apache.fop.datatypes.*;

import java.util.Hashtable;

/**
 * Class to parse XSL FO property expression.
 * This class is heavily based on the epxression parser in James Clark's
 * XT, an XSLT processor.
 */
public class PropertyParser extends PropertyTokenizer {
  private PropertyInfo propInfo; // Maker and propertyList related info
  
  static private final String RELUNIT = "em";
  static private final Numeric negOne = new Numeric(new Double(-1.0));
  static final private Hashtable functionTable = new Hashtable();

  static {
    // Initialize the Hashtable of XSL-defined functions
    functionTable.put("ceiling", new CeilingFunction());
    functionTable.put("floor", new FloorFunction());
    functionTable.put("round", new RoundFunction());
    functionTable.put("min", new MinFunction());
    functionTable.put("max", new MaxFunction());
    functionTable.put("abs", new AbsFunction());
    functionTable.put("rgb", new RGBColorFunction());
    functionTable.put("from-table-column", new FromTableColumnFunction());
    functionTable.put("inherited-property-value", new InheritedPropFunction());
    functionTable.put("from-parent", new FromParentFunction());
    functionTable.put("from-nearest-specified-value", new NearestSpecPropFunction());
    functionTable.put("proportional-column-width", new PPColWidthFunction());
    functionTable.put("label-end", new LabelEndFunction());
    functionTable.put("body-start", new BodyStartFunction());
    // NOTE: used from code generated for corresponding properties
    functionTable.put("_fop-property-value", new FopPropValFunction());

     /*** NOT YET IMPLEMENTED!!!
    functionTable.put("icc-color", new ICCcolorFunction());
    functionTable.put("system-color", new SystemColorFunction());

    functionTable.put("system-font", new SystemFontFunction());
    
    functionTable.put("merge-property-values", new MergePropsFunction());
    ***/
  }


  /**
   * Public entrypoint to the Property expression parser.
   * @param expr The specified value (attribute on the xml element).
   * @param propInfo A PropertyInfo object representing the context in
   * which the property expression is to be evaluated.
   * @return A Property object holding the parsed result.
   * @throws PropertyException If the "expr" cannot be parsed as a Property.
   */
  public static Property parse(String expr, PropertyInfo propInfo)
    throws PropertyException {
    return new PropertyParser(expr, propInfo).parseProperty();
  }


  /**
   * Private constructor. Called by the static parse() method.
   * @param propExpr The specified value (attribute on the xml element).
   * @param propInfo A PropertyInfo object representing the context in
   * which the property expression is to be evaluated.
   */
  private PropertyParser(String propExpr, PropertyInfo pInfo) {
    super(propExpr);
    this.propInfo = pInfo;
  }

  /**
   * Parse the property expression described in the instance variables.
   * Note: If the property expression String is empty, a StringProperty
   * object holding an empty String is returned.
   * @return A Property object holding the parsed result.
   * @throws PropertyException If the "expr" cannot be parsed as a Property.
   */
  private Property parseProperty() throws PropertyException {
    next();
    if (currentToken == TOK_EOF) {
      // if prop value is empty string, force to StringProperty
      return new StringProperty("");
    }
    Property prop = parseAdditiveExpr();
    if (currentToken != TOK_EOF)
      throw new PropertyException("unexpected token");
    return prop;
  }

  /**
   * Try to parse an addition or subtraction expression and return the
   *  resulting Property.
   */
  private Property parseAdditiveExpr() throws PropertyException {
    // Evaluate and put result on the operand stack
    Property prop = parseMultiplicativeExpr();
  loop:
    for (;;) {
      switch (currentToken) {
      case TOK_PLUS:
	next();
	prop = evalAddition(prop.getNumeric(),
		     parseMultiplicativeExpr().getNumeric() );
	break;
      case TOK_MINUS:
	next();
	prop = evalSubtraction(prop.getNumeric(),
		     parseMultiplicativeExpr().getNumeric() );
	break;
      default:
	break loop;
      }
    }
    return prop;
  }

  /**
   * Try to parse a multiply, divide or modulo expression and return
   * the resulting Property.
   */
  private Property parseMultiplicativeExpr() throws PropertyException {
    Property prop = parseUnaryExpr();
  loop:
    for (;;) {
      switch (currentToken) {
      case TOK_DIV:
	next();
	prop = evalDivide(prop.getNumeric(), parseUnaryExpr().getNumeric() );
 	break;
      case TOK_MOD:
	next();
	prop = evalModulo(prop.getNumber(), parseUnaryExpr().getNumber() );
	break;
      case TOK_MULTIPLY:
	next();
	prop = evalMultiply(prop.getNumeric(), parseUnaryExpr().getNumeric());
	break;
      default:
	break loop;
      }
    }
    return prop;
  }

  /**
   * Try to parse a unary minus expression and return the
   *  resulting Property.
   */
  private Property parseUnaryExpr() throws PropertyException {
    if (currentToken == TOK_MINUS) {
      next();
      return evalNegate(parseUnaryExpr().getNumeric());
    }
    return parsePrimaryExpr();
  }


  /**
   * Checks that the current token is a right parenthesis
   * and throws an exception if this isn't the case.
   */
  private final void expectRpar() throws PropertyException {
    if (currentToken != TOK_RPAR)
      throw new PropertyException("expected )");
    next();
  }

  /**
   * Try to parse a primary expression and return the
   * resulting Property.
   * A primary expression is either a parenthesized expression or an
   * expression representing a primitive Property datatype, such as a
   * string literal, an NCname, a number or a unit expression, or a
   * function call expression.
   */
  private Property parsePrimaryExpr() throws PropertyException {
    Property prop;
    switch (currentToken) {
    case TOK_LPAR:
      next();
      prop = parseAdditiveExpr();
      expectRpar();
      return prop;

    case TOK_LITERAL:
      prop = new StringProperty(currentTokenValue);
      break;

    case TOK_NCNAME:
      // Interpret this in context of the property or do it later?
      prop = new NCnameProperty(currentTokenValue);
      break;

    case TOK_FLOAT:
      prop = new NumberProperty(new Double(currentTokenValue));
      break;

    case TOK_INTEGER:
      prop = new NumberProperty(new Integer(currentTokenValue));
      break;

    case TOK_PERCENT:
      /* Get the length base value object from the Maker. If null, then
       * this property can't have % values. Treat it as a real number.
       */
      double pcval = new Double(currentTokenValue.substring(0,
				  currentTokenValue.length()-1)).
	doubleValue()/100.0;
      // LengthBase lbase = this.propInfo.getPercentLengthBase();
      PercentBase pcBase = this.propInfo.getPercentBase();
      if (pcBase != null) {
	if (pcBase.getDimension() == 0) {
	  prop = new NumberProperty(pcval * pcBase.getBaseValue());
	}
	else if (pcBase.getDimension() == 1) {
	  prop = new LengthProperty(new PercentLength(pcval, pcBase));
	}
	else {
	throw new PropertyException("Illegal percent dimension value");
	}
      }
      else {
	// WARNING? Interpret as a decimal fraction, eg. 50% = .5
	prop = new NumberProperty(pcval);
      }
      break;

    case TOK_NUMERIC:
      // A number plus a valid unit name.
      int numLen = currentTokenValue.length()-currentUnitLength;
      String unitPart = currentTokenValue.substring(numLen);
      Double numPart = new Double(currentTokenValue.substring(0,numLen));
      Length length= null;
      if (unitPart.equals(RELUNIT)) {
	  length = new Length(numPart.doubleValue(),
			      propInfo.currentFontSize());
      }
      else
	length = new Length(numPart.doubleValue(), unitPart);
      if (length == null) {
	throw new PropertyException("unrecognized unit name: "+ currentTokenValue);
      }
      else prop = new LengthProperty(length);
      break;

    case TOK_COLORSPEC:
      prop = new ColorTypeProperty(new ColorType(currentTokenValue));
      break;

    case TOK_FUNCTION_LPAR:
      {
	Function function = (Function)functionTable.get(currentTokenValue);
	if (function == null) {
	    throw new PropertyException("no such function: " + currentTokenValue);
	}
	next();
	// Push new function (for function context: getPercentBase())
	propInfo.pushFunction(function);
        prop = function.eval(parseArgs(function.nbArgs()), propInfo);
	propInfo.popFunction();
	return prop;
      }
    default:
      throw new PropertyException("syntax error");
    }
    next();
    return prop;
  }

  /**
   * Parse a comma separated list of function arguments. Each argument
   * may itself be an expression. This method consumes the closing right
   * parenthesis of the argument list.
   * @param nbArgs The number of arguments expected by the function.
   * @return An array of Property objects representing the arguments
   * found.
   * @throws PropertyException If the number of arguments found isn't equal
   * to the number expected.
   */
  Property[] parseArgs(int nbArgs) throws PropertyException {
    Property[] args = new Property[nbArgs];
    Property prop;
    int i=0;
    if (currentToken == TOK_RPAR) {
      // No args: func()
      next();
    }
    else {
      while(true) {

	prop = parseAdditiveExpr();	
	if (i < nbArgs) {
	  args[i++] = prop;
	}
	// ignore extra args
	if (currentToken != TOK_COMMA)
	  break;
	next();
      } 
      expectRpar();
    }
    if (nbArgs != i) {
      throw new PropertyException("Wrong number of args for function");
    }
    return args;
  }


  /**
   * Evaluate an addition operation. If either of the arguments is null,
   * this means that it wasn't convertible to a Numeric value.
   * @param op1 A Numeric object (Number or Length-type object)
   * @param op2 A Numeric object (Number or Length-type object)
   * @return A new NumericProperty object holding an object which represents
   * the sum of the two operands.
   * @throws PropertyException If either operand is null.
   */
  private Property evalAddition(Numeric op1, Numeric op2)
    throws PropertyException  {
    if (op1 == null || op2 == null)
      throw new PropertyException("Non numeric operand in addition");
     return new NumericProperty(op1.add(op2));
  }

  /**
   * Evaluate a subtraction operation. If either of the arguments is null,
   * this means that it wasn't convertible to a Numeric value.
   * @param op1 A Numeric object (Number or Length-type object)
   * @param op2 A Numeric object (Number or Length-type object)
   * @return A new NumericProperty object holding an object which represents
   * the difference of the two operands.
   * @throws PropertyException If either operand is null.
   */
  private Property evalSubtraction(Numeric op1, Numeric op2)
    throws PropertyException  {
     if (op1 == null || op2 == null)
      throw new PropertyException("Non numeric operand in subtraction");
     return new NumericProperty(op1.subtract(op2));
  }

  /**
   * Evaluate a unary minus operation. If the argument is null,
   * this means that it wasn't convertible to a Numeric value.
   * @param op A Numeric object (Number or Length-type object)
   * @return A new NumericProperty object holding an object which represents
   * the negative of the operand (multiplication by *1).
   * @throws PropertyException If the operand is null.
   */
  private Property evalNegate(Numeric op) throws PropertyException  {
    if (op == null)
      throw new PropertyException("Non numeric operand to unary minus");
    return new NumericProperty(op.multiply(negOne));
  }

  /**
   * Evaluate a multiplication operation. If either of the arguments is null,
   * this means that it wasn't convertible to a Numeric value.
   * @param op1 A Numeric object (Number or Length-type object)
   * @param op2 A Numeric object (Number or Length-type object)
   * @return A new NumericProperty object holding an object which represents
   * the product of the two operands.
   * @throws PropertyException If either operand is null.
   */
  private Property evalMultiply(Numeric op1, Numeric op2)
    throws PropertyException  {
     if (op1 == null || op2 == null)
      throw new PropertyException("Non numeric operand in multiplication");
    return new NumericProperty(op1.multiply(op2));
  }


  /**
   * Evaluate a division operation. If either of the arguments is null,
   * this means that it wasn't convertible to a Numeric value.
   * @param op1 A Numeric object (Number or Length-type object)
   * @param op2 A Numeric object (Number or Length-type object)
   * @return A new NumericProperty object holding an object which represents
   * op1 divided by op2.
   * @throws PropertyException If either operand is null.
   */
  private Property evalDivide(Numeric op1, Numeric op2)
    throws PropertyException  {
     if (op1 == null || op2 == null)
      throw new PropertyException("Non numeric operand in division");
    return new NumericProperty(op1.divide(op2));
  }

  /**
   * Evaluate a modulo operation. If either of the arguments is null,
   * this means that it wasn't convertible to a Number value.
   * @param op1 A Number object
   * @param op2 A Number object
   * @return A new NumberProperty object holding an object which represents
   * op1 mod op2.
   * @throws PropertyException If either operand is null.
   */
  private Property evalModulo(Number op1, Number op2)
    throws PropertyException  {
     if (op1 == null || op2 == null)
      throw new PropertyException("Non number operand to modulo");
    return new NumberProperty(op1.doubleValue()%op2.doubleValue());
  }

}
