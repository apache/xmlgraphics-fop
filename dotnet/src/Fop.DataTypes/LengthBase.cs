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

using Fop.Fo.Expr;

namespace Fop.DataTypes;

/// <summary>
/// Models a length which can be used as a factor in a percentage length calculation.
/// <para>
/// Port of <c>org.apache.fop.datatypes.LengthBase</c>. The original primary constructor takes a
/// <c>PropertyList</c> and reads the (inherited) font-size property from it; since the Fop.Fo
/// property layer is not ported yet, this port instead takes the FO object, the base type, and an
/// optional resolved base length directly.
/// </para>
/// <para>TODO: replace the object fobj parameter and add the PropertyList-based construction when Fop.Fo is ported.</para>
/// </summary>
public class LengthBase : IPercentBase
{
    // Standard kinds of percent-based length.

    /// <summary>Constant for a custom percent-based length.</summary>
    public const int CustomBase = 0;

    /// <summary>Constant for a font-size percent-based length.</summary>
    public const int FontSize = 1;

    /// <summary>Constant for an inherited font-size percent-based length.</summary>
    public const int InhFontSize = 2;

    /// <summary>Constant for a containing-box percent-based length.</summary>
    public const int ParentAreaWidth = 3;

    /// <summary>Constant for a containing reference-area percent-based length.</summary>
    public const int ContainingRefAreaWidth = 4;

    /// <summary>Constant for a containing-block percent-based length (width).</summary>
    public const int ContainingBlockWidth = 5;

    /// <summary>Constant for a containing-block percent-based length (height).</summary>
    public const int ContainingBlockHeight = 6;

    /// <summary>Constant for an image-intrinsic percent-based length (width).</summary>
    public const int ImageIntrinsicWidth = 7;

    /// <summary>Constant for an image-intrinsic percent-based length (height).</summary>
    public const int ImageIntrinsicHeight = 8;

    /// <summary>Constant for an image background-position horizontal percent-based length.</summary>
    public const int ImageBackgroundPositionHorizontal = 9;

    /// <summary>Constant for an image background-position vertical percent-based length.</summary>
    public const int ImageBackgroundPositionVertical = 10;

    /// <summary>Constant for a table-unit-based length.</summary>
    public const int TableUnits = 11;

    /// <summary>Constant for an alignment-adjust percent-based length.</summary>
    public const int AlignmentAdjust = 12;

    /// <summary>The FO for which this property is to be calculated.</summary>
    protected object? fobj;

    private readonly int baseType;

    /// <summary>For percentages based on other length properties.</summary>
    private readonly ILength? baseLength;

    /// <summary>Creates a new length base.</summary>
    /// <param name="fobj">The FO object for which the percentage is to be calculated.</param>
    /// <param name="baseType">One of the defined <c>LengthBase</c> type constants.</param>
    /// <param name="baseLength">
    /// The resolved base length, used for the <see cref="FontSize"/>/<see cref="InhFontSize"/> base types.
    /// </param>
    public LengthBase(object? fobj, int baseType, ILength? baseLength = null)
    {
        this.fobj = fobj;
        this.baseType = baseType;
        this.baseLength = baseLength;
    }

    /// <summary>The dimension of this object (always 1).</summary>
    public int Dimension => 1;

    /// <summary>The base value of this object (always 1.0).</summary>
    public double BaseValue => 1.0;

    /// <summary>The base length as an <see cref="ILength"/>.</summary>
    public ILength? BaseLength => baseLength;

    /// <summary>Returns the base length value in millipoints for the given context.</summary>
    /// <exception cref="PropertyException">If a problem occurs during evaluation of this value.</exception>
    public int GetBaseLength(IPercentBaseContext? context)
    {
        int baseLen = 0;
        if (context != null)
        {
            if (baseType is FontSize or InhFontSize)
            {
                // baseLength is expected to be non-null for the font-size base types.
                return baseLength!.GetValue(context);
            }

            baseLen = context.GetBaseLength(baseType, fobj);
        }
        else
        {
            // The Java original logs an error ("getBaseLength called without context") here;
            // logging is omitted in this standalone port but the return value (0) is preserved.
        }

        return baseLen;
    }

    /// <summary>Returns a string representation of this length base.</summary>
    public override string ToString() =>
        $"{base.ToString()}[fo={fobj},baseType={baseType},baseLength={baseLength}]";

    /// <inheritdoc/>
    public override int GetHashCode() => HashCode.Combine(baseLength, baseType, fobj);

    /// <inheritdoc/>
    public override bool Equals(object? obj)
    {
        if (ReferenceEquals(this, obj))
        {
            return true;
        }

        return obj is LengthBase other
            && Equals(baseLength, other.baseLength)
            && baseType == other.baseType
            && Equals(fobj, other.fobj);
    }
}
