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
/// Marks a font that can perform glyph positioning (kerning/mark attachment) for complex scripts.
/// <para>
/// Minimal stand-in for <c>org.apache.fop.complexscripts.fonts.Positionable</c>, which is not
/// ported in this slice. The full complex-scripts subsystem (GPOS processing) is out of scope; this
/// marker exists so <see cref="Fop.Fonts.Font"/> can detect positioning-capable metrics and
/// delegate to them.
/// </para>
/// </summary>
public interface IPositionable
{
    /// <summary>Determines if font performs glyph positioning.</summary>
    /// <returns><c>true</c> if performs positioning.</returns>
    bool PerformsPositioning();

    /// <summary>
    /// Perform glyph positioning.
    /// </summary>
    /// <param name="cs">character sequence to map to position offsets.</param>
    /// <param name="script">a script identifier.</param>
    /// <param name="language">a language identifier.</param>
    /// <param name="fontSize">font size.</param>
    /// <returns>array of 4-tuples of placement and advance adjustments, or <c>null</c> if no
    /// positioning occurred.</returns>
    int[][]? PerformPositioning(string cs, string script, string language, int fontSize);
}
