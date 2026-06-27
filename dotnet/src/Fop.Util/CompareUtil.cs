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

namespace Fop.Util;

/// <summary>
/// Helper methods for implementing <c>Equals</c> and <c>GetHashCode</c>.
/// <para>
/// Port of <c>org.apache.fop.util.CompareUtil</c>. The original used an
/// identity-hash / lock-ordering dance to avoid lock-ordering deadlocks when
/// comparing two objects whose <c>equals</c> could recurse. That hazard does not
/// arise in this port, so the comparison is expressed directly while preserving
/// the public contract.
/// </para>
/// </summary>
public static class CompareUtil
{
    /// <summary>
    /// Compares two objects for equality, treating two nulls as equal.
    /// </summary>
    /// <returns><c>true</c> if both are null or <c>o1.Equals(o2)</c>.</returns>
    public static bool Equal(object? o1, object? o2) => Equals(o1, o2);

    /// <summary>Generic, allocation-free equality for value or reference types.</summary>
    public static bool Equal<T>(T? o1, T? o2) => EqualityComparer<T?>.Default.Equals(o1, o2);

    /// <summary>Returns the hash code of the given object, or 0 if it is null.</summary>
    public static int GetHashCode(object? obj) => obj?.GetHashCode() ?? 0;

    /// <summary>
    /// Compares two numbers for equality using the same algorithm as Java's
    /// <c>Double.equals</c>: all NaN values are equal and +0.0 differs from -0.0.
    /// </summary>
    public static bool Equal(double n1, double n2) => DoubleToLongBits(n1) == DoubleToLongBits(n2);

    /// <summary>Returns a hash code for the given number, matching Java's <c>Double.hashCode</c>.</summary>
    public static int GetHashCode(double number)
    {
        long bits = DoubleToLongBits(number);
        return (int)(bits ^ ((long)((ulong)bits >> 32)));
    }

    /// <summary>
    /// Equivalent of Java's <c>Double.doubleToLongBits</c>: like the raw bit cast,
    /// but every NaN is collapsed to a single canonical representation.
    /// </summary>
    private static long DoubleToLongBits(double value) =>
        double.IsNaN(value) ? 0x7ff8000000000000L : BitConverter.DoubleToInt64Bits(value);
}
