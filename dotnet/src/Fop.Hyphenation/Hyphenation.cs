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

using System.Text;

namespace Fop.Hyphenation;

/// <summary>
/// Represents a hyphenated word: the original word plus the offsets at which it
/// may be hyphenated.
/// <para>
/// Faithful port of <c>org.apache.fop.hyphenation.Hyphenation</c>.
/// </para>
/// </summary>
public class Hyphenation
{
    private readonly int[] hyphenPoints;
    private readonly string word;

    /// <summary>
    /// Construct a hyphenated word.
    /// </summary>
    /// <param name="word">the word.</param>
    /// <param name="points">the hyphenation points (offsets into the word).</param>
    public Hyphenation(string word, int[] points)
    {
        ArgumentNullException.ThrowIfNull(word);
        ArgumentNullException.ThrowIfNull(points);
        this.word = word;
        hyphenPoints = points;
        Length = points.Length;
    }

    /// <summary>The number of hyphenation points in the word.</summary>
    public int Length { get; }

    /// <summary>
    /// Returns the pre-break text, not including the hyphen character.
    /// </summary>
    /// <param name="index">an index position.</param>
    public string GetPreHyphenText(int index) => word[..hyphenPoints[index]];

    /// <summary>Returns the post-break text.</summary>
    /// <param name="index">an index position.</param>
    public string GetPostHyphenText(int index) => word[hyphenPoints[index]..];

    /// <summary>
    /// Returns the hyphenation points. The Java original returned the internal
    /// array directly; this port preserves that (callers may observe mutations).
    /// </summary>
    public int[] GetHyphenationPoints() => hyphenPoints;

    /// <inheritdoc/>
    public override string ToString()
    {
        var str = new StringBuilder();
        int start = 0;
        for (int i = 0; i < Length; i++)
        {
            str.Append(word[start..hyphenPoints[i]]).Append('-');
            start = hyphenPoints[i];
        }

        str.Append(word[start..]);
        return str.ToString();
    }
}
