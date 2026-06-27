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

using Fop.Colors;

namespace Fop.Fo.Expr;

/// <summary>The datatype of an <see cref="ExprValue"/>.</summary>
public enum ExprValueKind
{
    /// <summary>A unitless number.</summary>
    Numeric,

    /// <summary>An absolute length, stored in millipoints.</summary>
    Length,

    /// <summary>A percentage (the percentage value, e.g. <c>50</c> for <c>50%</c>); base resolved later.</summary>
    Percentage,

    /// <summary>A resolved colour.</summary>
    Color,

    /// <summary>A string literal or unresolved keyword/NCName.</summary>
    String,
}

/// <summary>
/// The typed result of evaluating an XSL-FO property (sub)expression: a unitless
/// <see cref="ExprValueKind.Numeric"/>, an absolute <see cref="ExprValueKind.Length"/> (millipoints),
/// a <see cref="ExprValueKind.Percentage"/> (base resolved later), a <see cref="ExprValueKind.Color"/>,
/// or a <see cref="ExprValueKind.String"/> (a string literal or unresolved keyword).
/// </summary>
public readonly struct ExprValue
{
    private readonly double number;
    private readonly FopColor? color;
    private readonly string? text;

    private ExprValue(ExprValueKind kind, double number, FopColor? color, string? text)
    {
        Kind = kind;
        this.number = number;
        this.color = color;
        this.text = text;
    }

    /// <summary>The datatype of this value.</summary>
    public ExprValueKind Kind { get; }

    /// <summary>Creates a unitless numeric value.</summary>
    public static ExprValue FromNumber(double value) =>
        new(ExprValueKind.Numeric, value, null, null);

    /// <summary>Creates an absolute length value (millipoints).</summary>
    public static ExprValue FromLength(double millipoints) =>
        new(ExprValueKind.Length, millipoints, null, null);

    /// <summary>Creates a percentage value (e.g. <c>50</c> for <c>50%</c>).</summary>
    public static ExprValue FromPercentage(double percent) =>
        new(ExprValueKind.Percentage, percent, null, null);

    /// <summary>Creates a colour value.</summary>
    public static ExprValue FromColor(FopColor value) =>
        new(ExprValueKind.Color, 0, value, null);

    /// <summary>Creates a string/keyword value.</summary>
    public static ExprValue FromString(string value) =>
        new(ExprValueKind.String, 0, null, value);

    /// <summary>Whether this value is numeric (unitless).</summary>
    public bool IsNumeric => Kind == ExprValueKind.Numeric;

    /// <summary>Whether this value is an absolute length.</summary>
    public bool IsLength => Kind == ExprValueKind.Length;

    /// <summary>The numeric magnitude (unitless number or millipoints / percentage value).</summary>
    public double Value => number;

    /// <summary>The colour (only valid when <see cref="Kind"/> is <see cref="ExprValueKind.Color"/>).</summary>
    public FopColor? Color => color;

    /// <summary>The text (only valid when <see cref="Kind"/> is <see cref="ExprValueKind.String"/>).</summary>
    public string? Text => text;

    /// <summary>
    /// Returns the unitless numeric magnitude, throwing when the value is not a number or a length
    /// (lengths expose their millipoint magnitude, matching FOP's <c>getNumber()</c>/<c>getNumeric()</c>).
    /// </summary>
    public double AsNumber() => Kind switch
    {
        ExprValueKind.Numeric or ExprValueKind.Length or ExprValueKind.Percentage => number,
        _ => throw new PropertyException("expected a numeric operand"),
    };

    /// <inheritdoc/>
    public override string ToString() => Kind switch
    {
        ExprValueKind.Numeric => number.ToString(System.Globalization.CultureInfo.InvariantCulture),
        ExprValueKind.Length => new FoLength(number).ToString(),
        ExprValueKind.Percentage => number.ToString(System.Globalization.CultureInfo.InvariantCulture) + "%",
        ExprValueKind.Color => color?.ToString() ?? string.Empty,
        _ => text ?? string.Empty,
    };
}
