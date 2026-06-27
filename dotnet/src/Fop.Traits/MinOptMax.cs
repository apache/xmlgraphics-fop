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

using System.Globalization;

namespace Fop.Traits;

/// <summary>
/// Holds the resolved (in millipoints) form of a <c>LengthRange</c> or <c>Space</c> type property
/// value: a minimum, an optimum and a maximum, with <c>min &lt;= opt &lt;= max</c>.
/// <para>
/// Faithful port of <c>org.apache.fop.traits.MinOptMax</c>. Instances are immutable; every
/// arithmetic operation returns a new instance, so values can be passed around without copying.
/// <c>MinOptMax</c> values are used during layout calculations to express elasticity (shrink and
/// stretch).
/// </para>
/// <para>
/// In Java this was an immutable final reference type with value-based <c>equals</c>/<c>hashCode</c>.
/// Here it is a <c>readonly record struct</c>, which gives the same value semantics. Construction
/// goes through <see cref="GetInstance(int, int, int)"/> (mirroring the Java factory, including its
/// argument validation); the record's own positional constructor is therefore hidden as
/// <c>private</c> to force callers through the validating factory, exactly as the Java private
/// constructor did.
/// </para>
/// </summary>
public readonly record struct MinOptMax
{
    /// <summary>
    /// The zero <see cref="MinOptMax"/> with <c>min == opt == max == 0</c>.
    /// </summary>
    public static readonly MinOptMax Zero = new(0, 0, 0);

    // Private constructor without consistency checks (matches the Java private constructor that
    // only asserted min <= opt <= max). All public construction goes through GetInstance.
    private MinOptMax(int min, int opt, int max)
    {
        System.Diagnostics.Debug.Assert(min <= opt && opt <= max, "min <= opt <= max");
        Min = min;
        Opt = opt;
        Max = max;
    }

    /// <summary>Gets the minimum value of this <see cref="MinOptMax"/>.</summary>
    public int Min { get; }

    /// <summary>Gets the optimum value of this <see cref="MinOptMax"/>.</summary>
    public int Opt { get; }

    /// <summary>Gets the maximum value of this <see cref="MinOptMax"/>.</summary>
    public int Max { get; }

    /// <summary>
    /// Gets the shrinkability, the (always non-negative) difference between <see cref="Opt"/> and
    /// <see cref="Min"/>.
    /// </summary>
    public int Shrink => Opt - Min;

    /// <summary>
    /// Gets the stretchability, the (always non-negative) difference between <see cref="Max"/> and
    /// <see cref="Opt"/>.
    /// </summary>
    public int Stretch => Max - Opt;

    /// <summary>
    /// Returns an instance with the given values.
    /// </summary>
    /// <param name="min">the minimum value</param>
    /// <param name="opt">the optimum value</param>
    /// <param name="max">the maximum value</param>
    /// <returns>the corresponding instance</returns>
    /// <exception cref="ArgumentException">if <c>min &gt; opt</c> or <c>max &lt; opt</c>.</exception>
    public static MinOptMax GetInstance(int min, int opt, int max)
    {
        if (min > opt)
        {
            throw new ArgumentException(
                $"min ({min}) > opt ({opt})");
        }

        if (max < opt)
        {
            throw new ArgumentException(
                $"max ({max}) < opt ({opt})");
        }

        if (min == 0 && opt == 0 && max == 0)
        {
            return Zero;
        }

        return new MinOptMax(min, opt, max);
    }

    /// <summary>
    /// Returns an instance with one fixed value for all three properties (min, opt, max).
    /// </summary>
    /// <param name="value">the value for min, opt and max</param>
    /// <returns>the corresponding (stiff) instance</returns>
    /// <seealso cref="IsStiff"/>
    public static MinOptMax GetInstance(int value) => GetInstance(value, value, value);

    /// <summary>
    /// Returns the sum of this <see cref="MinOptMax"/> and the given operand.
    /// </summary>
    public MinOptMax Plus(MinOptMax operand) =>
        GetInstance(Min + operand.Min, Opt + operand.Opt, Max + operand.Max);

    /// <summary>
    /// Adds the given value to all three components and returns the result.
    /// </summary>
    public MinOptMax Plus(int value) => GetInstance(Min + value, Opt + value, Max + value);

    /// <summary>
    /// Returns the difference of this <see cref="MinOptMax"/> and the given operand. This instance
    /// must be a compound of the operand and another <see cref="MinOptMax"/> (the operand must have
    /// less shrink and stretch than this instance).
    /// </summary>
    /// <param name="operand">the value to be subtracted</param>
    /// <returns>the difference</returns>
    /// <exception cref="ArithmeticException">
    /// if this instance has strictly less shrink or stretch than the operand.
    /// </exception>
    public MinOptMax Minus(MinOptMax operand)
    {
        CheckCompatibility(Shrink, operand.Shrink, "shrink");
        CheckCompatibility(Stretch, operand.Stretch, "stretch");
        return GetInstance(Min - operand.Min, Opt - operand.Opt, Max - operand.Max);
    }

    private static void CheckCompatibility(int thisElasticity, int operandElasticity, string msge)
    {
        if (thisElasticity < operandElasticity)
        {
            throw new ArithmeticException(
                "Cannot subtract a MinOptMax from another MinOptMax that has less " + msge
                + " (" + thisElasticity + " < " + operandElasticity + ")");
        }
    }

    /// <summary>
    /// Subtracts the given value from all three components and returns the result.
    /// </summary>
    public MinOptMax Minus(int value) => GetInstance(Min - value, Opt - value, Max - value);

    /// <summary>
    /// Do not use, backwards compatibility only. Returns an instance with the given value added to
    /// the minimal value.
    /// </summary>
    /// <exception cref="ArgumentException">if <c>min + minOperand &gt; opt</c> or <c>max &lt; opt</c>.</exception>
    public MinOptMax PlusMin(int minOperand) => GetInstance(Min + minOperand, Opt, Max);

    /// <summary>
    /// Do not use, backwards compatibility only. Returns an instance with the given value
    /// subtracted from the minimal value.
    /// </summary>
    /// <exception cref="ArgumentException">if <c>min - minOperand &gt; opt</c> or <c>max &lt; opt</c>.</exception>
    public MinOptMax MinusMin(int minOperand) => GetInstance(Min - minOperand, Opt, Max);

    /// <summary>
    /// Do not use, backwards compatibility only. Returns an instance with the given value added to
    /// the maximal value.
    /// </summary>
    /// <exception cref="ArgumentException">if <c>min &gt; opt</c> or <c>max &lt; opt + maxOperand</c>.</exception>
    public MinOptMax PlusMax(int maxOperand) => GetInstance(Min, Opt, Max + maxOperand);

    /// <summary>
    /// Do not use, backwards compatibility only. Returns an instance with the given value
    /// subtracted from the maximal value.
    /// </summary>
    /// <exception cref="ArgumentException">if <c>min &gt; opt</c> or <c>max &lt; opt - maxOperand</c>.</exception>
    public MinOptMax MinusMax(int maxOperand) => GetInstance(Min, Opt, Max - maxOperand);

    /// <summary>
    /// Returns the product of this <see cref="MinOptMax"/> and the given factor.
    /// </summary>
    /// <param name="factor">the factor</param>
    /// <returns>the product</returns>
    /// <exception cref="ArgumentException">if the factor is negative.</exception>
    public MinOptMax Mult(int factor)
    {
        if (factor < 0)
        {
            throw new ArgumentException("factor < 0; was: " + factor);
        }

        if (factor == 1)
        {
            return this;
        }

        return GetInstance(Min * factor, Opt * factor, Max * factor);
    }

    /// <summary>
    /// Determines whether this <see cref="MinOptMax"/> represents a non-zero dimension, which means
    /// that not all values (min, opt, max) are zero.
    /// </summary>
    /// <remarks>
    /// Mirrors the Java implementation exactly: it tests only <c>min</c> and <c>max</c> (not
    /// <c>opt</c>), which is sufficient because the invariant <c>min &lt;= opt &lt;= max</c> means
    /// a non-zero <c>opt</c> forces a non-zero <c>min</c> or <c>max</c>.
    /// </remarks>
    public bool IsNonZero() => Min != 0 || Max != 0;

    /// <summary>
    /// Determines whether this <see cref="MinOptMax"/> does not allow for shrinking or stretching,
    /// i.e. all values (min, opt, max) are the same.
    /// </summary>
    /// <remarks>
    /// Mirrors the Java implementation, which tests only <c>min == max</c>; together with the
    /// invariant <c>min &lt;= opt &lt;= max</c> that forces <c>opt</c> to be equal as well.
    /// </remarks>
    /// <seealso cref="IsElastic"/>
    public bool IsStiff() => Min == Max;

    /// <summary>
    /// Determines whether this <see cref="MinOptMax"/> allows for shrinking or stretching, i.e. at
    /// least one of the min or max values is not equal to the opt value.
    /// </summary>
    /// <seealso cref="IsStiff"/>
    public bool IsElastic() => Min != Opt || Opt != Max;

    /// <summary>
    /// Extends the minimum length to the given length if necessary, and adjusts opt and max
    /// accordingly.
    /// </summary>
    /// <param name="newMin">the new minimum length</param>
    /// <returns>a <see cref="MinOptMax"/> with the minimum length extended</returns>
    public MinOptMax ExtendMinimum(int newMin)
    {
        if (Min < newMin)
        {
            int newOpt = System.Math.Max(newMin, Opt);
            int newMax = System.Math.Max(newOpt, Max);
            return GetInstance(newMin, newOpt, newMax);
        }

        return this;
    }

    // --- Operator overloads mirroring the named arithmetic methods. ---

    /// <summary>Operator form of <see cref="Plus(MinOptMax)"/>.</summary>
    public static MinOptMax operator +(MinOptMax left, MinOptMax right) => left.Plus(right);

    /// <summary>Operator form of <see cref="Plus(int)"/>.</summary>
    public static MinOptMax operator +(MinOptMax left, int right) => left.Plus(right);

    /// <summary>Operator form of <see cref="Minus(MinOptMax)"/>.</summary>
    public static MinOptMax operator -(MinOptMax left, MinOptMax right) => left.Minus(right);

    /// <summary>Operator form of <see cref="Minus(int)"/>.</summary>
    public static MinOptMax operator -(MinOptMax left, int right) => left.Minus(right);

    /// <summary>Operator form of <see cref="Mult(int)"/>.</summary>
    public static MinOptMax operator *(MinOptMax left, int factor) => left.Mult(factor);

    /// <summary>Returns the Java-compatible string representation.</summary>
    public override string ToString() =>
        string.Create(
            CultureInfo.InvariantCulture,
            $"MinOptMax[min = {Min}, opt = {Opt}, max = {Max}]");
}
