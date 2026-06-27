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

using System.Collections.Concurrent;
using System.Globalization;

namespace Fop.Colors;

/// <summary>
/// Generic colour helper. Parses string colour values into <see cref="FopColor"/>
/// values and serializes colours back to re-parsable strings, with a table of
/// the standard CSS/SVG colour names.
/// <para>
/// Port of <c>org.apache.fop.util.ColorUtil</c>. The self-contained parsing and
/// serialization paths are ported faithfully (hex, <c>rgb()</c>, <c>cmyk()</c>,
/// <c>fop-rgb-icc()</c> with the <c>#CMYK</c>/<c>#Separation</c> pseudo-profiles,
/// the system-color keyword map, and <c>oca()</c>). Paths that need a real ICC
/// profile or the <c>FOUserAgent</c> are stubbed: the profile name/source and
/// components are retained, but actual ICC resolution is left as a TODO.
/// </para>
/// </summary>
public static class ColorUtil
{
    /// <summary>The name for the uncalibrated CMYK pseudo-profile.</summary>
    public const string CmykPseudoProfile = "#CMYK";

    /// <summary>The name for the Separation pseudo-profile used for spot colours.</summary>
    public const string SeparationPseudoProfile = "#Separation";

    /// <summary>The name for the alpha pseudo-profile.</summary>
    public const string AlphaPseudoProfile = "#alpha";

    // ColorWithFallback is used to preserve the sRGB fallback exclusively for the
    // purpose of regenerating textual color functions as specified in XSL-FO.

    // Keeps the predefined named colours and caches already-parsed colours.
    // The use of this cache assumes that all FopColor instances are immutable.
    private static readonly ConcurrentDictionary<string, FopColor> ColorMap = CreateColorMap();

    /// <summary>
    /// Creates a colour from a given string.
    /// <para>
    /// Supports: <c>#RGB</c>, <c>#RGBA</c>, <c>#RRGGBB</c>, <c>#RRGGBBAA</c> hex;
    /// <c>rgb(r,g,b)</c> (0..255 or 0%..100%); <c>system-color(name)</c>;
    /// <c>transparent</c>; a colour name; <c>fop-rgb-icc(...)</c>;
    /// <c>cmyk(c,m,y,k)</c> (0..1); and <c>oca(name)</c>.
    /// </para>
    /// </summary>
    /// <param name="foUserAgent">stand-in for the Java <c>FOUserAgent</c>; only used
    /// for ICC profile resolution, which is not yet ported. May be <c>null</c>.</param>
    /// <param name="value">the string to parse</param>
    /// <returns>the parsed colour, or <c>null</c> when <paramref name="value"/> is null</returns>
    /// <exception cref="ColorParseException">if the string cannot be parsed</exception>
    public static FopColor? ParseColorString(object? foUserAgent, string? value)
    {
        if (value is null)
        {
            return null;
        }

        if (ColorMap.TryGetValue(value.ToLowerInvariant(), out FopColor? cached))
        {
            return cached;
        }

        FopColor? parsedColor = null;
        if (value.StartsWith('#'))
        {
            parsedColor = ParseWithHash(value);
        }
        else if (value.StartsWith("rgb(", StringComparison.Ordinal))
        {
            parsedColor = ParseAsRgb(value);
        }
        else if (value.StartsWith("url(", StringComparison.Ordinal))
        {
            throw new ColorParseException("Colors starting with url( are not yet supported!");
        }
        else if (value.StartsWith("java.awt.Color", StringComparison.Ordinal))
        {
            parsedColor = ParseAsJavaAwtColor(value);
        }
        else if (value.StartsWith("system-color(", StringComparison.Ordinal))
        {
            parsedColor = ParseAsSystemColor(value);
        }
        else if (value.StartsWith("fop-rgb-icc", StringComparison.Ordinal))
        {
            parsedColor = ParseAsFopRgbIcc(foUserAgent, value);
        }
        else if (value.StartsWith("fop-rgb-named-color", StringComparison.Ordinal))
        {
            parsedColor = ParseAsFopRgbNamedColor(foUserAgent, value);
        }
        else if (value.StartsWith("cie-lab-color", StringComparison.Ordinal))
        {
            // TODO: ICC profile resolution (needs FOUserAgent port) - CIELab color space
            throw new ColorParseException("cie-lab-color() is not yet supported in the C# port");
        }
        else if (value.StartsWith("cmyk", StringComparison.Ordinal))
        {
            parsedColor = ParseAsCmyk(value);
        }
        else if (value.StartsWith("oca", StringComparison.Ordinal))
        {
            parsedColor = ParseAsOca(value);
        }

        if (parsedColor is null)
        {
            throw new ColorParseException("Unknown Color: " + value);
        }

        ColorMap[value] = parsedColor;
        return parsedColor;
    }

    /// <summary>
    /// Tries to parse a colour given with the <c>system-color()</c> function.
    /// </summary>
    private static FopColor? ParseAsSystemColor(string value)
    {
        int poss = value.IndexOf('(');
        int pose = value.IndexOf(')');
        if (poss == -1 || pose == -1)
        {
            throw new ColorParseException("Unknown color format: " + value + ". Must be system-color(x)");
        }

        string name = value.Substring(poss + 1, pose - poss - 1);
        return ColorMap.GetValueOrDefault(name);
    }

    /// <summary>
    /// Tries to parse the standard <c>java.awt.Color.toString()</c> output,
    /// i.e. <c>java.awt.Color[r=...,g=...,b=...]</c> with components in 0..255.
    /// </summary>
    private static FopColor ParseAsJavaAwtColor(string value)
    {
        int poss = value.IndexOf('[');
        int pose = value.IndexOf(']');
        if (poss == -1 || pose == -1)
        {
            throw new ColorParseException("Invalid format for a java.awt.Color: " + value);
        }

        try
        {
            string body = value.Substring(poss + 1, pose - poss - 1);
            string[] args = body.Split(',');
            if (args.Length != 3)
            {
                throw new ColorParseException("Invalid number of arguments for a java.awt.Color: " + body);
            }

            // Each component looks like "r=255"; skip the 2-char "x=" prefix.
            float red = ParseFloat(args[0].Trim()[2..]) / 255f;
            float green = ParseFloat(args[1].Trim()[2..]) / 255f;
            float blue = ParseFloat(args[2].Trim()[2..]) / 255f;
            if (red is < 0.0f or > 1.0f || green is < 0.0f or > 1.0f || blue is < 0.0f or > 1.0f)
            {
                throw new ColorParseException("Color values out of range");
            }

            return FopColor.FromFloat(red, green, blue);
        }
        catch (Exception re) when (re is not ColorParseException)
        {
            throw new ColorParseException(re.Message, re);
        }
    }

    /// <summary>
    /// Parses a colour given with the <c>rgb()</c> function.
    /// </summary>
    private static FopColor ParseAsRgb(string value)
    {
        int poss = value.IndexOf('(');
        int pose = value.IndexOf(')');
        if (poss == -1 || pose == -1)
        {
            throw new ColorParseException("Unknown color format: " + value + ". Must be rgb(r,g,b)");
        }

        string body = value.Substring(poss + 1, pose - poss - 1);
        try
        {
            string[] args = body.Split(',');
            if (args.Length != 3)
            {
                throw new ColorParseException("Invalid number of arguments: rgb(" + body + ")");
            }

            float red = ParseComponent255(args[0], body);
            float green = ParseComponent255(args[1], body);
            float blue = ParseComponent255(args[2], body);
            // Convert to ints to synchronize behaviour with the rgb serialization.
            int r = (int)(red * 255 + 0.5);
            int g = (int)(green * 255 + 0.5);
            int b = (int)(blue * 255 + 0.5);
            return FopColor.FromArgb(r, g, b);
        }
        catch (Exception re) when (re is not ColorParseException)
        {
            throw new ColorParseException(re.Message, re);
        }
    }

    private static float ParseComponent255(string str, string function)
    {
        str = str.Trim();
        float component = str.EndsWith('%')
            ? ParseFloat(str[..^1]) / 100f
            : ParseFloat(str) / 255f;
        if (component is < 0.0f or > 1.0f)
        {
            throw new ColorParseException("Color value out of range for " + function + ": "
                + str + ". Valid range: [0..255] or [0%..100%]");
        }

        return component;
    }

    private static float ParseComponent1(string argument, string function)
        => ParseComponent(argument, 0f, 1f, function);

    private static float ParseComponent(string argument, float min, float max, string function)
    {
        float component = ParseFloat(argument.Trim());
        if (component < min || component > max)
        {
            throw new ColorParseException("Color value out of range for " + function + ": "
                + argument + ". Valid range: [" + min + ".." + max + "]");
        }

        return component;
    }

    /// <summary>
    /// Parses the sRGB fallback components (with optional <c>#alpha</c>) at the
    /// head of an <c>fop-rgb-icc()</c>/<c>fop-rgb-named-color()</c> argument list.
    /// Advances <paramref name="index"/> past the consumed arguments.
    /// </summary>
    private static FopColor ParseFallback(string[] args, ref int index, string value)
    {
        float red = ParseComponent1(args[index++], value);
        float green = ParseComponent1(args[index++], value);
        float blue = ParseComponent1(args[index++], value);
        float alpha = 1f;
        if (AlphaPseudoProfile.Equals(args[index].Trim(), StringComparison.Ordinal))
        {
            index++;
            alpha = ParseComponent1(args[index++], value);
        }

        // Sun's classlib rounds differently with this constructor than when
        // converting to sRGB via CIE XYZ.
        return FopColor.FromFloat(red, green, blue, alpha);
    }

    /// <summary>
    /// Parses a colour given in the <c>#...</c> hex format.
    /// </summary>
    private static FopColor ParseWithHash(string value)
    {
        try
        {
            int len = value.Length;
            int alpha;
            if (len is 5 or 9)
            {
                alpha = ParseHex(value[(len == 5 ? 3 : 7)..]);
            }
            else
            {
                alpha = 0xFF;
            }

            int red;
            int green;
            int blue;
            if (len is 4 or 5)
            {
                // multiply by 0x11 = 17 = 255/15
                red = ParseHex(value.Substring(1, 1)) * 0x11;
                green = ParseHex(value.Substring(2, 1)) * 0x11;
                blue = ParseHex(value.Substring(3, 1)) * 0x11;
            }
            else if (len is 7 or 9)
            {
                red = ParseHex(value.Substring(1, 2));
                green = ParseHex(value.Substring(3, 2));
                blue = ParseHex(value.Substring(5, 2));
            }
            else
            {
                throw new FormatException();
            }

            return FopColor.FromArgb(red, green, blue, alpha);
        }
        catch (Exception re) when (re is not ColorParseException)
        {
            throw new ColorParseException("Unknown color format: " + value
                + ". Must be #RGB. #RGBA, #RRGGBB, or #RRGGBBAA", re);
        }
    }

    /// <summary>
    /// Parses a colour specified using the <c>fop-rgb-icc()</c> function. The sRGB
    /// fallback is parsed and the <c>#CMYK</c>/<c>#Separation</c> pseudo-profiles
    /// are handled; resolution of a real ICC profile (the <paramref name="foUserAgent"/>
    /// path) is deferred.
    /// </summary>
    private static FopColor ParseAsFopRgbIcc(object? foUserAgent, string value)
    {
        int poss = value.IndexOf('(');
        int pose = value.IndexOf(')');
        if (poss == -1 || pose == -1)
        {
            throw new ColorParseException("Unknown color format: " + value
                + ". Must be fop-rgb-icc(r,g,b,NCNAME,src,....)");
        }

        string[] argsStr = value.Substring(poss + 1, pose - poss - 1).Split(',');
        try
        {
            if (argsStr.Length < 5)
            {
                throw new ColorParseException("Too few arguments for rgb-icc() function");
            }

            int index = 0;

            // Set up fallback sRGB value.
            FopColor sRgb = ParseFallback(argsStr, ref index, value);

            // Get and verify ICC profile name.
            string iccProfileName = argsStr[index++].Trim();
            if (iccProfileName.Length == 0)
            {
                throw new ColorParseException("ICC profile name missing");
            }

            string iccProfile = argsStr[index++];
            string namedColorSpace = argsStr[index++];

            if (IsPseudoProfile(iccProfileName))
            {
                if (CmykPseudoProfile.Equals(iccProfileName, StringComparison.OrdinalIgnoreCase))
                {
                    // Java steps the iterator back one (args.previous()) for a
                    // non-NamedColorSpace, so the value read as namedColorSpace is
                    // actually the first CMYK component. Read from index - 1.
                    float[] iccComponents = GetIccComponents(argsStr, index - 1);
                    return new ColorWithAlternatives(sRgb, CmykPseudoProfile, iccComponents);
                }

                if (SeparationPseudoProfile.Equals(iccProfileName, StringComparison.OrdinalIgnoreCase))
                {
                    // For a NamedColorSpace, Java does NOT step back, so the colour
                    // name (namedColorSpace) is the single "component" identifier and
                    // the tint defaults to full (1.0) when no components follow.
                    float[] tint = GetIccComponents(argsStr, index);
                    if (tint.Length == 0)
                    {
                        tint = [1.0f]; // full tint if not specified
                    }

                    return new ColorWithFallback(SeparationPseudoProfile + ":" + namedColorSpace.Trim(),
                        tint, sRgb);
                }

                throw new ColorParseException("Incomplete implementation for pseudo-profile: " + iccProfileName);
            }

            // Get and verify ICC profile source.
            string iccProfileSrc = iccProfile.Trim();
            if (iccProfileSrc.Length == 0)
            {
                throw new ColorParseException("ICC profile source missing");
            }

            iccProfileSrc = UnescapeString(iccProfileSrc);

            // Real (non-pseudo) ICC profile: the remaining args (starting at the
            // namedColorSpace position, which Java steps back to) are components.
            float[] components = GetIccComponents(argsStr, index - 1);

            // TODO: ICC profile resolution (needs FOUserAgent port). Without the
            // FOUserAgent/color-space cache the profile cannot be loaded, so we
            // fall back to the sRGB replacement values exactly as the Java code
            // does when the profile is not found.
            _ = foUserAgent;
            _ = components;
            return sRgb;
        }
        catch (Exception re) when (re is not ColorParseException)
        {
            throw new ColorParseException(re.Message, re);
        }
    }

    private static float[] GetIccComponents(string[] args, int start)
    {
        if (start >= args.Length)
        {
            return [];
        }

        var list = new float[args.Length - start];
        for (int i = start; i < args.Length; i++)
        {
            list[i - start] = ParseFloat(args[i].Trim());
        }

        return list;
    }

    /// <summary>
    /// Parses a colour specified using the <c>fop-rgb-named-color()</c> function.
    /// Only the sRGB fallback and the named-colour identifier are retained; the
    /// actual named-colour ICC profile lookup is deferred.
    /// </summary>
    private static FopColor ParseAsFopRgbNamedColor(object? foUserAgent, string value)
    {
        int poss = value.IndexOf('(');
        int pose = value.IndexOf(')');
        if (poss == -1 || pose == -1)
        {
            throw new ColorParseException("Unknown color format: " + value
                + ". Must be fop-rgb-named-color(r,g,b,NCNAME,src,color-name)");
        }

        string[] args = value.Substring(poss + 1, pose - poss - 1).Split(',');
        try
        {
            if (args.Length != 6)
            {
                throw new ColorParseException("rgb-named-color() function must have 6 arguments");
            }

            int index = 0;
            FopColor sRgb = ParseFallback(args, ref index, value);

            string iccProfileName = args[3].Trim();
            if (iccProfileName.Length == 0)
            {
                throw new ColorParseException("ICC profile name missing");
            }

            if (IsPseudoProfile(iccProfileName))
            {
                throw new ColorParseException("Pseudo-profiles are not allowed with fop-rgb-named-color()");
            }

            string iccProfileSrc = args[4].Trim();
            if (iccProfileSrc.Length == 0)
            {
                throw new ColorParseException("ICC profile source missing");
            }

            iccProfileSrc = UnescapeString(iccProfileSrc);
            string colorName = UnescapeString(args[5].Trim());

            // TODO: ICC profile resolution (needs FOUserAgent port). The named
            // colour profile cannot be loaded without a FOUserAgent/color-space
            // cache, so we fall back to the sRGB replacement values exactly as the
            // Java code does when the profile is not found.
            _ = foUserAgent;
            _ = colorName;
            return sRgb;
        }
        catch (Exception re) when (re is not ColorParseException)
        {
            throw new ColorParseException(re.Message, re);
        }
    }

    private static string UnescapeString(string src)
    {
        if (src.StartsWith('"') || src.StartsWith('\''))
        {
            src = src[1..];
        }

        if (src.EndsWith('"') || src.EndsWith('\''))
        {
            src = src[..^1];
        }

        return src;
    }

    /// <summary>
    /// Parses a colour given with the <c>cmyk()</c> function. The CMYK components
    /// are retained as an alternate colour space and converted to an sRGB primary.
    /// </summary>
    private static FopColor ParseAsCmyk(string value)
    {
        int poss = value.IndexOf('(');
        int pose = value.IndexOf(')');
        if (poss == -1 || pose == -1)
        {
            throw new ColorParseException("Unknown color format: " + value + ". Must be cmyk(c,m,y,k)");
        }

        string body = value.Substring(poss + 1, pose - poss - 1);
        string[] args = body.Split(',');
        try
        {
            if (args.Length != 4)
            {
                throw new ColorParseException("Invalid number of arguments: cmyk(" + body + ")");
            }

            float cyan = ParseComponent1(args[0], body);
            float magenta = ParseComponent1(args[1], body);
            float yellow = ParseComponent1(args[2], body);
            float black = ParseComponent1(args[3], body);
            float[] comps = [cyan, magenta, yellow, black];
            float[] rgb = CmykToRgb(comps);
            FopColor sRgb = FopColor.FromFloat(rgb[0], rgb[1], rgb[2]);
            return new ColorWithAlternatives(sRgb, CmykPseudoProfile, comps);
        }
        catch (Exception re) when (re is not ColorParseException)
        {
            throw new ColorParseException(re.Message, re);
        }
    }

    /// <summary>
    /// Uncalibrated CMYK to sRGB conversion. Mirrors the naive device CMYK model
    /// used by xmlgraphics' <c>DeviceCMYKColorSpace</c>:
    /// <c>rgb = 1 - min(1, comp + k)</c> per channel (computed in float).
    /// </summary>
    private static float[] CmykToRgb(float[] cmyk)
    {
        float c = cmyk[0];
        float m = cmyk[1];
        float y = cmyk[2];
        float k = cmyk[3];
        return
        [
            1f - Math.Min(1f, c + k),
            1f - Math.Min(1f, m + k),
            1f - Math.Min(1f, y + k),
        ];
    }

    /// <summary>
    /// Parses a colour given with the <c>oca()</c> function.
    /// </summary>
    private static FopColor ParseAsOca(string value)
    {
        int poss = value.IndexOf('(');
        int pose = value.IndexOf(')');
        if (poss == -1 || pose == -1)
        {
            throw new ColorParseException("Unknown color format: " + value + ". Must be oca(color-name)");
        }

        string name = value.Substring(poss + 1, pose - poss - 1);
        OCAColorValue colorValue = name switch
        {
            "blue" => OCAColorValue.Blue,
            "red" => OCAColorValue.Red,
            "magenta" => OCAColorValue.Magenta,
            "green" => OCAColorValue.Green,
            "cyan" => OCAColorValue.Cyan,
            "yellow" => OCAColorValue.Yellow,
            "black" => OCAColorValue.Black,
            "brown" => OCAColorValue.Brown,
            "medium-color" => OCAColorValue.MediumColor,
            "device-default" => OCAColorValue.DeviceDefault,
            // "Unknwon" is the (misspelled) Java message; preserved as-is.
            _ => throw new ColorParseException("Unknwon OCA color: " + name),
        };
        return new OCAColor(colorValue);
    }

    /// <summary>
    /// Creates a re-parsable string representation of the given colour.
    /// <para>
    /// A plain sRGB colour is printed as <c>#rrggbb</c> (or <c>#rrggbbaa</c> when
    /// an alpha is present). Colours carrying an alternate colour space are
    /// printed as an <c>fop-rgb-icc(...)</c> function call.
    /// </para>
    /// </summary>
    public static string ColorToString(FopColor color)
    {
        ArgumentNullException.ThrowIfNull(color);
        if (color.HasAlternativeColors || (color is ColorWithFallback && color.ColorSpaceName is not null))
        {
            return ToFunctionCall(color);
        }

        return ToRgbFunctionCall(color);
    }

    private static string ToRgbFunctionCall(FopColor color)
    {
        var sb = new System.Text.StringBuilder();
        sb.Append('#');
        AppendHexByte(sb, color.Red);
        AppendHexByte(sb, color.Green);
        AppendHexByte(sb, color.Blue);
        if (color.Alpha != 255)
        {
            AppendHexByte(sb, color.Alpha);
        }

        return sb.ToString();
    }

    private static void AppendHexByte(System.Text.StringBuilder sb, int v)
    {
        // Java uses Integer.toHexString (lowercase, no leading zero) then pads to 2.
        string s = v.ToString("x", CultureInfo.InvariantCulture);
        if (s.Length == 1)
        {
            sb.Append('0');
        }

        sb.Append(s);
    }

    /// <summary>
    /// Serializes a colour that carries an alternate colour space as an
    /// <c>fop-rgb-icc(...)</c> function call. Mirrors the Java
    /// <c>toFunctionCall(ColorWithAlternatives)</c> for the pseudo-profile cases
    /// that this slice of the port supports.
    /// </summary>
    private static string ToFunctionCall(FopColor color)
    {
        FopColor fallback = GetSRgbFallback(color);
        float[] rgb = fallback.GetSRgbComponents();

        var sb = new System.Text.StringBuilder(40);
        sb.Append('(');
        sb.Append(FloatToString(rgb[0])).Append(',');
        sb.Append(FloatToString(rgb[1])).Append(',');
        sb.Append(FloatToString(rgb[2])).Append(',');
        if (rgb[3] != 1f)
        {
            sb.Append(AlphaPseudoProfile).Append(',').Append(FloatToString(rgb[3])).Append(',');
        }

        string spaceName = color.ColorSpaceName ?? CmykPseudoProfile;
        if (spaceName.StartsWith(SeparationPseudoProfile, StringComparison.Ordinal))
        {
            // "#Separation:<colorName>" -> "fop-rgb-icc(...,#Separation,,<colorName>)"
            string colorName = spaceName[(SeparationPseudoProfile.Length + 1)..];
            sb.Append(SeparationPseudoProfile).Append(','); // profile name
            // profile URI is empty for a pseudo-profile
            sb.Append(',').Append(colorName);
        }
        else
        {
            // CMYK pseudo-profile: name, empty URI, then the components.
            sb.Append(spaceName).Append(','); // profile name (e.g. #CMYK)
            // profile URI is empty for a pseudo-profile
            foreach (float comp in color.Components)
            {
                sb.Append(',').Append(FloatToString(comp));
            }
        }

        sb.Append(')');
        return "fop-rgb-icc" + sb;
    }

    private static FopColor GetSRgbFallback(FopColor color)
    {
        if (color is ColorWithFallback cwf)
        {
            return cwf.FallbackColor;
        }

        if (color is ColorWithAlternatives cwa)
        {
            // The sRGB primary (with its precise float components) is the fallback.
            return cwa.SRgb;
        }

        return FopColor.FromArgb(color.Red, color.Green, color.Blue, color.Alpha);
    }

    /// <summary>
    /// Renders a float exactly like Java's <c>Float.toString</c>/<c>StringBuilder.append(float)</c>:
    /// the shortest round-trippable decimal, always with a fractional part
    /// (e.g. <c>1.0</c>, not <c>1</c>).
    /// </summary>
    private static string FloatToString(float value)
    {
        // "R" gives the shortest round-trippable representation on .NET; ensure a
        // decimal point is present so "1" becomes "1.0" to match Java's output.
        string s = value.ToString("R", CultureInfo.InvariantCulture);
        if (!s.Contains('.') && !s.Contains('E') && !s.Contains('e'))
        {
            s += ".0";
        }

        return s;
    }

    /// <summary>
    /// Lightens up a colour for groove, ridge, inset and outset border effects.
    /// </summary>
    /// <param name="col">the colour to lighten up</param>
    /// <param name="factor">factor by which to lighten (negative values darken)</param>
    public static FopColor LightenColor(FopColor col, float factor)
    {
        ArgumentNullException.ThrowIfNull(col);
        // Port of org.apache.xmlgraphics.java2d.color.ColorUtil.lightenColor:
        // each sRGB component is moved toward white (factor > 0) or black.
        float r = col.RedFloat;
        float g = col.GreenFloat;
        float b = col.BlueFloat;
        if (factor > 0)
        {
            r += (1.0f - r) * factor;
            g += (1.0f - g) * factor;
            b += (1.0f - b) * factor;
        }
        else
        {
            r *= 1.0f + factor;
            g *= 1.0f + factor;
            b *= 1.0f + factor;
        }

        return FopColor.FromFloat(r, g, b, col.AlphaFloat);
    }

    /// <summary>
    /// Indicates whether the given colour profile name is one of the
    /// pseudo-profiles supported by FOP (e.g. <c>#CMYK</c>).
    /// </summary>
    public static bool IsPseudoProfile(string colorProfileName)
        => CmykPseudoProfile.Equals(colorProfileName, StringComparison.OrdinalIgnoreCase)
            || SeparationPseudoProfile.Equals(colorProfileName, StringComparison.OrdinalIgnoreCase);

    /// <summary>
    /// Indicates whether the colour is a gray value (red == green == blue).
    /// </summary>
    public static bool IsGray(FopColor col)
    {
        ArgumentNullException.ThrowIfNull(col);
        return col.IsGray;
    }

    private static float ParseFloat(string s)
        => float.Parse(s, NumberStyles.Float, CultureInfo.InvariantCulture);

    private static int ParseHex(string s)
        => int.Parse(s, NumberStyles.HexNumber, CultureInfo.InvariantCulture);

    private static ConcurrentDictionary<string, FopColor> CreateColorMap()
    {
        var map = new ConcurrentDictionary<string, FopColor>(StringComparer.Ordinal);
        void Put(string name, int r, int g, int b) => map[name] = FopColor.FromRgb(r, g, b);

        Put("aliceblue", 240, 248, 255);
        Put("antiquewhite", 250, 235, 215);
        Put("aqua", 0, 255, 255);
        Put("aquamarine", 127, 255, 212);
        Put("azure", 240, 255, 255);
        Put("beige", 245, 245, 220);
        Put("bisque", 255, 228, 196);
        Put("black", 0, 0, 0);
        Put("blanchedalmond", 255, 235, 205);
        Put("blue", 0, 0, 255);
        Put("blueviolet", 138, 43, 226);
        Put("brown", 165, 42, 42);
        Put("burlywood", 222, 184, 135);
        Put("cadetblue", 95, 158, 160);
        Put("chartreuse", 127, 255, 0);
        Put("chocolate", 210, 105, 30);
        Put("coral", 255, 127, 80);
        Put("cornflowerblue", 100, 149, 237);
        Put("cornsilk", 255, 248, 220);
        Put("crimson", 220, 20, 60);
        Put("cyan", 0, 255, 255);
        Put("darkblue", 0, 0, 139);
        Put("darkcyan", 0, 139, 139);
        Put("darkgoldenrod", 184, 134, 11);
        Put("darkgray", 169, 169, 169);
        Put("darkgreen", 0, 100, 0);
        Put("darkgrey", 169, 169, 169);
        Put("darkkhaki", 189, 183, 107);
        Put("darkmagenta", 139, 0, 139);
        Put("darkolivegreen", 85, 107, 47);
        Put("darkorange", 255, 140, 0);
        Put("darkorchid", 153, 50, 204);
        Put("darkred", 139, 0, 0);
        Put("darksalmon", 233, 150, 122);
        Put("darkseagreen", 143, 188, 143);
        Put("darkslateblue", 72, 61, 139);
        Put("darkslategray", 47, 79, 79);
        Put("darkslategrey", 47, 79, 79);
        Put("darkturquoise", 0, 206, 209);
        Put("darkviolet", 148, 0, 211);
        Put("deeppink", 255, 20, 147);
        Put("deepskyblue", 0, 191, 255);
        Put("dimgray", 105, 105, 105);
        Put("dimgrey", 105, 105, 105);
        Put("dodgerblue", 30, 144, 255);
        Put("firebrick", 178, 34, 34);
        Put("floralwhite", 255, 250, 240);
        Put("forestgreen", 34, 139, 34);
        Put("fuchsia", 255, 0, 255);
        Put("gainsboro", 220, 220, 220);
        Put("ghostwhite", 248, 248, 255);
        Put("gold", 255, 215, 0);
        Put("goldenrod", 218, 165, 32);
        Put("gray", 128, 128, 128);
        Put("green", 0, 128, 0);
        Put("greenyellow", 173, 255, 47);
        Put("grey", 128, 128, 128);
        Put("honeydew", 240, 255, 240);
        Put("hotpink", 255, 105, 180);
        Put("indianred", 205, 92, 92);
        Put("indigo", 75, 0, 130);
        Put("ivory", 255, 255, 240);
        Put("khaki", 240, 230, 140);
        Put("lavender", 230, 230, 250);
        Put("lavenderblush", 255, 240, 245);
        Put("lawngreen", 124, 252, 0);
        Put("lemonchiffon", 255, 250, 205);
        Put("lightblue", 173, 216, 230);
        Put("lightcoral", 240, 128, 128);
        Put("lightcyan", 224, 255, 255);
        Put("lightgoldenrodyellow", 250, 250, 210);
        Put("lightgray", 211, 211, 211);
        Put("lightgreen", 144, 238, 144);
        Put("lightgrey", 211, 211, 211);
        Put("lightpink", 255, 182, 193);
        Put("lightsalmon", 255, 160, 122);
        Put("lightseagreen", 32, 178, 170);
        Put("lightskyblue", 135, 206, 250);
        Put("lightslategray", 119, 136, 153);
        Put("lightslategrey", 119, 136, 153);
        Put("lightsteelblue", 176, 196, 222);
        Put("lightyellow", 255, 255, 224);
        Put("lime", 0, 255, 0);
        Put("limegreen", 50, 205, 50);
        Put("linen", 250, 240, 230);
        Put("magenta", 255, 0, 255);
        Put("maroon", 128, 0, 0);
        Put("mediumaquamarine", 102, 205, 170);
        Put("mediumblue", 0, 0, 205);
        Put("mediumorchid", 186, 85, 211);
        Put("mediumpurple", 147, 112, 219);
        Put("mediumseagreen", 60, 179, 113);
        Put("mediumslateblue", 123, 104, 238);
        Put("mediumspringgreen", 0, 250, 154);
        Put("mediumturquoise", 72, 209, 204);
        Put("mediumvioletred", 199, 21, 133);
        Put("midnightblue", 25, 25, 112);
        Put("mintcream", 245, 255, 250);
        Put("mistyrose", 255, 228, 225);
        Put("moccasin", 255, 228, 181);
        Put("navajowhite", 255, 222, 173);
        Put("navy", 0, 0, 128);
        Put("oldlace", 253, 245, 230);
        Put("olive", 128, 128, 0);
        Put("olivedrab", 107, 142, 35);
        Put("orange", 255, 165, 0);
        Put("orangered", 255, 69, 0);
        Put("orchid", 218, 112, 214);
        Put("palegoldenrod", 238, 232, 170);
        Put("palegreen", 152, 251, 152);
        Put("paleturquoise", 175, 238, 238);
        Put("palevioletred", 219, 112, 147);
        Put("papayawhip", 255, 239, 213);
        Put("peachpuff", 255, 218, 185);
        Put("peru", 205, 133, 63);
        Put("pink", 255, 192, 203);
        Put("plum ", 221, 160, 221); // note: trailing-space key preserved from Java
        Put("plum", 221, 160, 221);
        Put("powderblue", 176, 224, 230);
        Put("purple", 128, 0, 128);
        Put("red", 255, 0, 0);
        Put("rosybrown", 188, 143, 143);
        Put("royalblue", 65, 105, 225);
        Put("saddlebrown", 139, 69, 19);
        Put("salmon", 250, 128, 114);
        Put("sandybrown", 244, 164, 96);
        Put("seagreen", 46, 139, 87);
        Put("seashell", 255, 245, 238);
        Put("sienna", 160, 82, 45);
        Put("silver", 192, 192, 192);
        Put("skyblue", 135, 206, 235);
        Put("slateblue", 106, 90, 205);
        Put("slategray", 112, 128, 144);
        Put("slategrey", 112, 128, 144);
        Put("snow", 255, 250, 250);
        Put("springgreen", 0, 255, 127);
        Put("steelblue", 70, 130, 180);
        Put("tan", 210, 180, 140);
        Put("teal", 0, 128, 128);
        Put("thistle", 216, 191, 216);
        Put("tomato", 255, 99, 71);
        Put("turquoise", 64, 224, 208);
        Put("violet", 238, 130, 238);
        Put("wheat", 245, 222, 179);
        Put("white", 255, 255, 255);
        Put("whitesmoke", 245, 245, 245);
        Put("yellow", 255, 255, 0);
        Put("yellowgreen", 154, 205, 50);
        map["transparent"] = FopColor.FromArgb(0, 0, 0, 0);
        return map;
    }
}
