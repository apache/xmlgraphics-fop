// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

namespace Fop.Fo.Expr;

/// <summary>The kind of an <see cref="ExprToken"/> produced by the <see cref="ExprTokenizer"/>.</summary>
/// <remarks>
/// Mirrors the token set of FOP's <c>org.apache.fop.fo.expr.PropertyTokenizer</c>. Note that XSL-FO
/// uses the keyword operators <c>div</c>/<c>mod</c> and <c>*</c> for multiply -- a literal <c>/</c> is
/// not a division operator.
/// </remarks>
public enum ExprTokenKind
{
    /// <summary>End of input.</summary>
    Eof,

    /// <summary>An integer literal, e.g. <c>42</c>.</summary>
    Integer,

    /// <summary>A floating-point literal, e.g. <c>2.5</c>.</summary>
    Float,

    /// <summary>A number with a unit, e.g. <c>12pt</c> or <c>1.2em</c>.</summary>
    Numeric,

    /// <summary>A percentage, e.g. <c>50%</c> (the value excludes the trailing <c>%</c>).</summary>
    Percent,

    /// <summary>A colour hex literal, e.g. <c>#abc</c> or <c>#aabbcc</c>.</summary>
    ColorSpec,

    /// <summary>A quoted string literal (the surrounding quotes are stripped).</summary>
    Literal,

    /// <summary>An NCName (an identifier/keyword not directly followed by a parenthesis).</summary>
    NCName,

    /// <summary>A function name immediately followed by <c>(</c> (the value excludes the parenthesis).</summary>
    FunctionLPar,

    /// <summary>A left parenthesis <c>(</c>.</summary>
    LPar,

    /// <summary>A right parenthesis <c>)</c>.</summary>
    RPar,

    /// <summary>An argument-separating comma <c>,</c>.</summary>
    Comma,

    /// <summary>The addition operator <c>+</c>.</summary>
    Plus,

    /// <summary>The subtraction (or unary minus) operator <c>-</c>.</summary>
    Minus,

    /// <summary>The multiplication operator <c>*</c>.</summary>
    Multiply,

    /// <summary>The division keyword operator <c>div</c>.</summary>
    Div,

    /// <summary>The modulo keyword operator <c>mod</c>.</summary>
    Mod,
}

/// <summary>A single lexical token of an XSL-FO property expression.</summary>
/// <param name="Kind">The token kind.</param>
/// <param name="Value">The raw text of the token (for literals, with quotes stripped).</param>
/// <param name="UnitLength">For <see cref="ExprTokenKind.Numeric"/>, the length of the trailing unit name.</param>
public readonly record struct ExprToken(ExprTokenKind Kind, string Value, int UnitLength = 0)
{
    /// <summary>The numeric (value-only) part of a <see cref="ExprTokenKind.Numeric"/> token.</summary>
    public string NumberPart => Value[..(Value.Length - UnitLength)];

    /// <summary>The unit suffix of a <see cref="ExprTokenKind.Numeric"/> token.</summary>
    public string UnitPart => Value[(Value.Length - UnitLength)..];
}
