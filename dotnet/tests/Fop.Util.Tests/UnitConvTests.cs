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

using System.Numerics;
using Fop.Util;
using Xunit;

namespace Fop.Util.Tests;

public class UnitConvTests
{
    // The conversion constants are floats (matching Java), so single-precision rounding error
    // (~1e-7 relative) propagates into the double results; assertions use a relative tolerance.
    private const double RelTolerance = 1e-5;

    private static void AssertClose(double expected, double actual)
        => Assert.Equal(expected, actual, Math.Abs(expected) * RelTolerance + 1e-9);

    [Fact]
    public void Constants_MatchXmlGraphicsValues()
    {
        Assert.Equal(25.4f, UnitConv.In2Mm);
        Assert.Equal(2.54f, UnitConv.In2Cm);
        Assert.Equal(72, UnitConv.In2Pt);
    }

    [Fact]
    public void Mm2Pt_OneInchIs72Points()
        => AssertClose(72.0, UnitConv.Mm2Pt(25.4));

    [Fact]
    public void Mm2Mpt_OneInchIs72000Millipoints()
        => AssertClose(72000.0, UnitConv.Mm2Mpt(25.4));

    [Fact]
    public void Pt2Mm_RoundTripsWithMm2Pt()
        => AssertClose(123.4, UnitConv.Pt2Mm(UnitConv.Mm2Pt(123.4)));

    [Fact]
    public void InchConversions()
    {
        AssertClose(25.4, UnitConv.InToMm(1.0));
        AssertClose(72000.0, UnitConv.InToMpt(1.0));
        AssertClose(72.0, UnitConv.InToPt(1.0));
        AssertClose(2.0, UnitConv.Mm2In(50.8));
        AssertClose(1.0, UnitConv.Mpt2In(72000.0));
    }

    [Fact]
    public void PixelConversions_ScaleByResolution()
    {
        AssertClose(96.0, UnitConv.Mm2Px(25.4, 96));
        AssertClose(96.0, UnitConv.Mpt2Px(72000.0, 96));
    }

    [Fact]
    public void MptToPt_DividesOnlyTranslationByThousand()
    {
        // Linear part 2,3,4,5 with translation 6000, 7000 millipoints.
        var at = new Matrix3x2(2f, 3f, 4f, 5f, 6000f, 7000f);
        Matrix3x2 pt = UnitConv.MptToPt(at);
        Assert.Equal(2f, pt.M11);
        Assert.Equal(3f, pt.M12);
        Assert.Equal(4f, pt.M21);
        Assert.Equal(5f, pt.M22);
        Assert.Equal(6f, pt.M31);
        Assert.Equal(7f, pt.M32);
    }

    [Fact]
    public void PtToMpt_MultipliesOnlyTranslationByThousand()
    {
        var at = new Matrix3x2(2f, 3f, 4f, 5f, 6f, 7f);
        Matrix3x2 mpt = UnitConv.PtToMpt(at);
        Assert.Equal(2f, mpt.M11);
        Assert.Equal(5f, mpt.M22);
        Assert.Equal(6000f, mpt.M31);
        Assert.Equal(7000f, mpt.M32);
    }

    [Fact]
    public void MptToPt_PtToMpt_RoundTrip()
    {
        var at = new Matrix3x2(1f, 0.5f, -0.25f, 1f, 12345f, -6789f);
        Assert.Equal(at, UnitConv.PtToMpt(UnitConv.MptToPt(at)));
    }
}
