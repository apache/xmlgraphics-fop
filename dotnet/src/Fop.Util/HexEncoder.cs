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
/// A helper class to create hex-encoded representations of numbers.
/// <para>Port of <c>org.apache.fop.util.HexEncoder</c>.</para>
/// </summary>
public static class HexEncoder
{
    /// <summary>
    /// Returns a hex encoding of the given number as a string of the given length,
    /// left-padded with zeros if necessary. The digits use upper-case letters,
    /// matching the original FOP implementation.
    /// </summary>
    /// <param name="n">a number.</param>
    /// <param name="width">required length of the string.</param>
    /// <returns>a hex-encoded representation of the number.</returns>
    public static string Encode(int n, int width)
    {
        char[] digits = new char[width];
        for (int i = width - 1; i >= 0; i--)
        {
            int digit = n & 0xF;
            digits[i] = (char)(digit < 10 ? '0' + digit : 'A' + digit - 10);
            n >>= 4;
        }

        return new string(digits);
    }

    /// <summary>
    /// Returns a hex encoding of the given character as:
    /// <list type="bullet">
    ///   <item><description>4-character string in case of a BMP character;</description></item>
    ///   <item><description>6-character string in case of a non-BMP character.</description></item>
    /// </list>
    /// </summary>
    /// <param name="c">a character.</param>
    /// <returns>a hex-encoded representation of the character.</returns>
    public static string Encode(int c) =>
        CharUtilities.IsBmpCodePoint(c) ? Encode(c, 4) : Encode(c, 6);
}
