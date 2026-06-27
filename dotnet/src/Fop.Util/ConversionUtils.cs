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
using System.Text.RegularExpressions;

namespace Fop.Util;

/// <summary>
/// Utility methods for conversions, like a <see cref="string"/> to an array of
/// <see cref="int"/> or <see cref="double"/>.
/// <para>Port of <c>org.apache.fop.util.ConversionUtils</c>.</para>
/// </summary>
public static class ConversionUtils
{
    /// <summary>
    /// Converts the given base string into an array of <see cref="int"/>, splitting the base
    /// along the given separator pattern.
    /// <para>
    /// Note: this method assumes the input is a string containing only decimal integers, signed
    /// or unsigned, that are parsable by <see cref="int.Parse(string)"/>. If this is not the case,
    /// the resulting <see cref="FormatException"/> will have to be handled by the caller.
    /// </para>
    /// </summary>
    /// <param name="baseString">the base string.</param>
    /// <param name="separatorPattern">the regex pattern separating the integer values (if this
    /// is <c>null</c> or empty, the base string is parsed as one integer value).</param>
    /// <returns>an array of <see cref="int"/> whose size is equal to the number of values in the
    /// input string; <c>null</c> if the input string is <c>null</c> or empty.</returns>
    public static int[]? ToIntArray(string? baseString, string? separatorPattern)
    {
        if (string.IsNullOrEmpty(baseString))
        {
            return null;
        }

        if (string.IsNullOrEmpty(separatorPattern))
        {
            return [int.Parse(baseString, CultureInfo.InvariantCulture)];
        }

        string[] values = Regex.Split(baseString, separatorPattern);
        int numValues = values.Length;
        if (numValues == 0)
        {
            return null;
        }

        int[] returnArray = new int[numValues];
        for (int i = 0; i < numValues; ++i)
        {
            returnArray[i] = int.Parse(values[i], CultureInfo.InvariantCulture);
        }

        return returnArray;
    }

    /// <summary>
    /// Converts the given base string into an array of <see cref="double"/>, splitting the base
    /// along the given separator pattern.
    /// <para>
    /// Note: this method assumes the input is a string containing only decimal doubles, signed
    /// or unsigned, that are parsable by <see cref="double.Parse(string)"/>. If this is not the
    /// case, the resulting <see cref="FormatException"/> will have to be handled by the caller.
    /// </para>
    /// </summary>
    /// <param name="baseString">the base string.</param>
    /// <param name="separatorPattern">the regex pattern separating the double values (if this is
    /// <c>null</c> or empty, the base string is parsed as one double value).</param>
    /// <returns>an array of <see cref="double"/> whose size is equal to the number of values in
    /// the input string; <c>null</c> if the input string is <c>null</c> or empty.</returns>
    public static double[]? ToDoubleArray(string? baseString, string? separatorPattern)
    {
        if (string.IsNullOrEmpty(baseString))
        {
            return null;
        }

        if (string.IsNullOrEmpty(separatorPattern))
        {
            return [double.Parse(baseString, CultureInfo.InvariantCulture)];
        }

        string[] values = Regex.Split(baseString, separatorPattern);
        int numValues = values.Length;
        if (numValues == 0)
        {
            return null;
        }

        double[] returnArray = new double[numValues];
        for (int i = 0; i < numValues; ++i)
        {
            returnArray[i] = double.Parse(values[i], CultureInfo.InvariantCulture);
        }

        return returnArray;
    }
}
