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

namespace Fop.Fonts.Substitution;

/// <summary>
/// Marks a font that can perform glyph substitution and combining-mark reordering for complex
/// scripts.
/// <para>
/// Minimal stand-in for <c>org.apache.fop.complexscripts.fonts.Substitutable</c>, which is not
/// ported in this slice. The full complex-scripts subsystem (GSUB processing) is out of scope; this
/// marker exists so <see cref="Fop.Fonts.Font"/> can detect substitution-capable metrics and
/// delegate to them. The associations/glyph-position-adjustments parameters are modelled loosely
/// because their ported representations do not yet exist.
/// </para>
/// </summary>
public interface ISubstitutable
{
    /// <summary>Determines if font performs glyph substitution.</summary>
    /// <returns><c>true</c> if performs substitution.</returns>
    bool PerformsSubstitution();

    /// <summary>
    /// Perform substitutions on characters to effect glyph substitution.
    /// </summary>
    /// <param name="cs">character sequence to map to output font encoding.</param>
    /// <param name="script">a script identifier.</param>
    /// <param name="language">a language identifier.</param>
    /// <param name="associations">an optional list to receive association objects.</param>
    /// <param name="retainControls">if <c>true</c>, then retain control characters.</param>
    /// <returns>output character sequence, encoded for output font.</returns>
    string PerformSubstitution(
        string cs,
        string script,
        string language,
        IList<object>? associations,
        bool retainControls);

    /// <summary>
    /// Reorder combining marks in character sequence so that they precede (within the sequence) the
    /// base character to which they are applied.
    /// </summary>
    /// <param name="cs">character sequence within which combining marks to be reordered.</param>
    /// <param name="gpa">glyph position adjustments that apply to <paramref name="cs"/>.</param>
    /// <param name="script">a script identifier.</param>
    /// <param name="language">a language identifier.</param>
    /// <param name="associations">an optional list of associations.</param>
    /// <returns>output character sequence, with combining marks reordered.</returns>
    string ReorderCombiningMarks(
        string cs,
        int[][]? gpa,
        string script,
        string language,
        IList<object>? associations);
}
