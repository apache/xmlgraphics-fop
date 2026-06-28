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

using System.Buffers.Binary;

namespace Fop.Render.Pdf.Native;

/// <summary>
/// Reduces a TrueType font program to the glyphs actually used, keeping every glyph's <em>id</em>
/// unchanged. Only the <c>glyf</c>/<c>loca</c> tables are rebuilt: kept glyphs retain their outline,
/// every other glyph becomes a zero-length entry. Because glyph ids, advance widths (<c>hmtx</c>) and
/// the character map (<c>cmap</c>) are untouched, the embedding font dictionary's encoding and
/// <c>/Widths</c> remain valid -- only the (dominant) outline data shrinks. Layout-only tables
/// (<c>GSUB</c>/<c>GPOS</c>/<c>DSIG</c>/…) are dropped. Returns <c>null</c> if the font cannot be
/// subset, in which case the caller embeds the full program.
/// </summary>
internal static class TrueTypeSubsetter
{
    // The tables kept in the subset (others, e.g. GSUB/GPOS/DSIG, are dropped). glyf/loca are rebuilt.
    private static readonly HashSet<string> KeepTables = new(StringComparer.Ordinal)
    {
        "cmap", "cvt ", "fpgm", "glyf", "head", "hhea", "hmtx", "loca", "maxp", "name", "post", "prep",
        "OS/2", "gasp",
    };

    /// <summary>
    /// Subsets <paramref name="data"/> to <paramref name="usedGlyphs"/> (plus glyph 0 and the
    /// components of any composite glyphs), or returns <c>null</c> if the font cannot be processed.
    /// </summary>
    public static byte[]? Subset(byte[] data, IReadOnlySet<int> usedGlyphs)
    {
        try
        {
            return SubsetCore(data, usedGlyphs);
        }
        catch (Exception)
        {
            return null;
        }
    }

    private static byte[]? SubsetCore(byte[] d, IReadOnlySet<int> usedGlyphs)
    {
        if (d.Length < 12 || ReadU32(d, 0) != 0x00010000 && ReadU32(d, 0) != 0x74727565)
        {
            return null; // glyf-based TrueType only (CFF/OTTO has no glyf to subset here)
        }

        ushort numTables = ReadU16(d, 4);
        var tables = new Dictionary<string, (int Offset, int Length)>(StringComparer.Ordinal);
        for (int i = 0; i < numTables; i++)
        {
            int rec = 12 + i * 16;
            string tag = System.Text.Encoding.ASCII.GetString(d, rec, 4);
            tables[tag] = ((int)ReadU32(d, rec + 8), (int)ReadU32(d, rec + 12));
        }

        if (!tables.TryGetValue("glyf", out var glyf) || !tables.TryGetValue("loca", out var loca)
            || !tables.TryGetValue("head", out var head) || !tables.TryGetValue("maxp", out var maxp))
        {
            return null;
        }

        int numGlyphs = ReadU16(d, maxp.Offset + 4);
        bool longLoca = ReadI16(d, head.Offset + 50) == 1;

        // Read the original glyph offsets into glyf.
        var offsets = new int[numGlyphs + 1];
        for (int i = 0; i <= numGlyphs; i++)
        {
            offsets[i] = longLoca
                ? (int)ReadU32(d, loca.Offset + i * 4)
                : ReadU16(d, loca.Offset + i * 2) * 2;
        }

        // Close the used set over composite-glyph components.
        var kept = new HashSet<int> { 0 };
        foreach (int g in usedGlyphs)
        {
            if (g is >= 0 && g < numGlyphs)
            {
                kept.Add(g);
            }
        }

        var queue = new Queue<int>(kept);
        while (queue.Count > 0)
        {
            int g = queue.Dequeue();
            foreach (int component in CompositeComponents(d, glyf.Offset, offsets, g, numGlyphs))
            {
                if (kept.Add(component))
                {
                    queue.Enqueue(component);
                }
            }
        }

        // Rebuild glyf (kept glyphs only) and a long loca pointing into it.
        using var newGlyf = new MemoryStream();
        var newOffsets = new int[numGlyphs + 1];
        for (int g = 0; g < numGlyphs; g++)
        {
            newOffsets[g] = (int)newGlyf.Length;
            if (kept.Contains(g))
            {
                int start = glyf.Offset + offsets[g];
                int len = offsets[g + 1] - offsets[g];
                if (len > 0)
                {
                    newGlyf.Write(d, start, len);
                    // Pad to a 2-byte boundary so glyph data stays aligned.
                    if ((newGlyf.Length & 1) != 0)
                    {
                        newGlyf.WriteByte(0);
                    }
                }
            }
        }

        newOffsets[numGlyphs] = (int)newGlyf.Length;
        byte[] glyfData = newGlyf.ToArray();

        var locaData = new byte[(numGlyphs + 1) * 4];
        for (int i = 0; i <= numGlyphs; i++)
        {
            BinaryPrimitives.WriteUInt32BigEndian(locaData.AsSpan(i * 4, 4), (uint)newOffsets[i]);
        }

        // Assemble the new font. head is patched: long loca format, and checkSumAdjustment zeroed.
        var output = new Dictionary<string, byte[]>(StringComparer.Ordinal);
        foreach ((string tag, (int off, int len)) in tables)
        {
            if (!KeepTables.Contains(tag))
            {
                continue;
            }

            output[tag] = tag switch
            {
                "glyf" => glyfData,
                "loca" => locaData,
                "head" => PatchHead(d.AsSpan(off, len).ToArray()),
                _ => d.AsSpan(off, len).ToArray(),
            };
        }

        return Assemble(output);
    }

    private static byte[] PatchHead(byte[] head)
    {
        BinaryPrimitives.WriteUInt32BigEndian(head.AsSpan(8, 4), 0); // checkSumAdjustment = 0
        BinaryPrimitives.WriteInt16BigEndian(head.AsSpan(50, 2), 1); // indexToLocFormat = long
        return head;
    }

    /// <summary>Returns the component glyph ids of glyph <paramref name="g"/> if it is composite.</summary>
    private static IEnumerable<int> CompositeComponents(byte[] d, int glyfOffset, int[] offsets, int g,
        int numGlyphs)
    {
        if (g < 0 || g >= numGlyphs || offsets[g + 1] <= offsets[g])
        {
            yield break;
        }

        int p = glyfOffset + offsets[g];
        short numberOfContours = ReadI16(d, p);
        if (numberOfContours >= 0)
        {
            yield break; // simple glyph
        }

        p += 10; // skip numberOfContours + bounding box
        const int ArgsAreWords = 0x0001;
        const int WeHaveAScale = 0x0008;
        const int MoreComponents = 0x0020;
        const int XAndYScale = 0x0040;
        const int TwoByTwo = 0x0080;

        while (true)
        {
            int flags = ReadU16(d, p);
            int componentGlyph = ReadU16(d, p + 2);
            yield return componentGlyph;

            p += 4;
            p += (flags & ArgsAreWords) != 0 ? 4 : 2;
            if ((flags & WeHaveAScale) != 0)
            {
                p += 2;
            }
            else if ((flags & XAndYScale) != 0)
            {
                p += 4;
            }
            else if ((flags & TwoByTwo) != 0)
            {
                p += 8;
            }

            if ((flags & MoreComponents) == 0)
            {
                break;
            }
        }
    }

    /// <summary>Reassembles an sfnt from the given tables, with a correct directory and checksums.</summary>
    private static byte[] Assemble(Dictionary<string, byte[]> tables)
    {
        string[] tags = tables.Keys.OrderBy(t => t, StringComparer.Ordinal).ToArray();
        int numTables = tags.Length;

        int entrySelector = (int)Math.Floor(Math.Log2(numTables));
        int searchRange = (int)Math.Pow(2, entrySelector) * 16;
        int rangeShift = numTables * 16 - searchRange;

        int dataStart = 12 + numTables * 16;
        var offsets = new Dictionary<string, int>(StringComparer.Ordinal);
        int pos = dataStart;
        foreach (string tag in tags)
        {
            offsets[tag] = pos;
            pos += Pad4(tables[tag].Length);
        }

        var output = new byte[pos];
        BinaryPrimitives.WriteUInt32BigEndian(output.AsSpan(0, 4), 0x00010000);
        BinaryPrimitives.WriteUInt16BigEndian(output.AsSpan(4, 2), (ushort)numTables);
        BinaryPrimitives.WriteUInt16BigEndian(output.AsSpan(6, 2), (ushort)searchRange);
        BinaryPrimitives.WriteUInt16BigEndian(output.AsSpan(8, 2), (ushort)entrySelector);
        BinaryPrimitives.WriteUInt16BigEndian(output.AsSpan(10, 2), (ushort)rangeShift);

        for (int i = 0; i < numTables; i++)
        {
            string tag = tags[i];
            byte[] data = tables[tag];
            int rec = 12 + i * 16;
            System.Text.Encoding.ASCII.GetBytes(tag).CopyTo(output.AsSpan(rec, 4));
            BinaryPrimitives.WriteUInt32BigEndian(output.AsSpan(rec + 4, 4), Checksum(data));
            BinaryPrimitives.WriteUInt32BigEndian(output.AsSpan(rec + 8, 4), (uint)offsets[tag]);
            BinaryPrimitives.WriteUInt32BigEndian(output.AsSpan(rec + 12, 4), (uint)data.Length);
            data.CopyTo(output.AsSpan(offsets[tag], data.Length));
        }

        return output;
    }

    private static int Pad4(int n) => (n + 3) & ~3;

    /// <summary>The TrueType table checksum: the big-endian uint32 sum over the 4-byte-padded data.</summary>
    private static uint Checksum(byte[] data)
    {
        uint sum = 0;
        int i = 0;
        for (; i + 4 <= data.Length; i += 4)
        {
            sum += BinaryPrimitives.ReadUInt32BigEndian(data.AsSpan(i, 4));
        }

        if (i < data.Length)
        {
            // Final partial word, zero-padded on the right.
            uint last = 0;
            for (int b = 0; b < 4; b++)
            {
                last = (last << 8) | (i + b < data.Length ? data[i + b] : (uint)0);
            }

            sum += last;
        }

        return sum;
    }

    private static ushort ReadU16(byte[] d, int offset) => BinaryPrimitives.ReadUInt16BigEndian(d.AsSpan(offset, 2));

    private static short ReadI16(byte[] d, int offset) => BinaryPrimitives.ReadInt16BigEndian(d.AsSpan(offset, 2));

    private static uint ReadU32(byte[] d, int offset) => BinaryPrimitives.ReadUInt32BigEndian(d.AsSpan(offset, 4));
}
