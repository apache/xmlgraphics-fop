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

using Fop.Util;
using Xunit;

namespace Fop.Util.Tests;

/// <summary>
/// Mirrors <c>org.apache.fop.util.HexEncoderTestCase</c>.
/// </summary>
public class HexEncoderTests
{
    [Fact]
    public void EncodeChar_ProducesFourDigitHexForBmp()
    {
        char[] digits = ['0', '0', '0', '0'];
        for (int c = 0; c <= 0xFFFF; c++)
        {
            Assert.Equal(new string(digits), HexEncoder.Encode((char)c));
            Increment(digits);
        }
    }

    [Fact]
    public void EncodeCodepoints_ProducesSixDigitHexForNonBmp()
    {
        char[] digits = ['0', '1', '0', '0', '0', '0'];
        for (int c = 0x10000; c <= 0x1FFFF; c++)
        {
            Assert.Equal(new string(digits), HexEncoder.Encode(c));
            Increment(digits);
        }
    }

    [Theory]
    [InlineData(0, 4, "0000")]
    [InlineData(0xABCD, 4, "ABCD")]
    [InlineData(0xF, 2, "0F")]
    [InlineData(0x10000, 6, "010000")]
    public void Encode_WithExplicitWidth_PadsAndUsesUpperCase(int n, int width, string expected)
        => Assert.Equal(expected, HexEncoder.Encode(n, width));

    private static void Increment(char[] digits)
    {
        int d = digits.Length;
        do
        {
            d--;
            digits[d] = Successor(digits[d]);
        }
        while (digits[d] == '0' && d > 0);
    }

    private static char Successor(char d) => d switch
    {
        '9' => 'A',
        'F' => '0',
        _ => (char)(d + 1),
    };
}
