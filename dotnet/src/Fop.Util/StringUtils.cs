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
/// String helper utilities.
/// <para>Port of <c>org.apache.fop.util.StringUtils</c>.</para>
/// </summary>
public static class StringUtils
{
    /// <summary>
    /// Replaces every soft hyphen (U+00AD) with an ordinary hyphen-minus ('-') unless the
    /// supplied painter declares support for soft hyphens, in which case the text is returned
    /// unchanged.
    /// </summary>
    /// <param name="text">the text to process.</param>
    /// <param name="painter">the painter whose capability decides the replacement.</param>
    /// <returns>the processed text.</returns>
    public static string ProcessSoftHyphen(string text, ISoftHyphenSupport painter)
    {
        ArgumentNullException.ThrowIfNull(text);
        ArgumentNullException.ThrowIfNull(painter);
        return painter.SupportsSoftHyphen
            ? text
            : text.Replace(CharUtilities.SoftHyphen, '-');
    }
}

/// <summary>
/// Abstraction of the single capability of <c>org.apache.fop.render.intermediate.IFPainter</c>
/// that <see cref="StringUtils.ProcessSoftHyphen"/> depends on.
/// </summary>
/// <remarks>
/// TODO: when the intermediate-format painter (<c>IFPainter</c>) is ported, have it implement
/// this interface (or fold this capability into it) so callers pass the real painter directly.
/// </remarks>
public interface ISoftHyphenSupport
{
    /// <summary>Gets a value indicating whether the painter supports soft hyphens.</summary>
    bool SupportsSoftHyphen { get; }
}
