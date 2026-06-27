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

using System.IO;
using System.Text;

namespace Fop.Util;

/// <summary>
/// Resolves RFC 2397 "data:" URIs, returning the decoded payload.
/// <para>
/// The Java <c>org.apache.fop.util.DataURIResolver</c> implements
/// <c>javax.xml.transform.URIResolver</c> by delegating to
/// <c>org.apache.xmlgraphics.util.uri.DataURIResolver</c>. Since xmlgraphics-commons and the
/// transform layer are not part of this port, the self-contained "data:"-decoding logic is folded
/// directly into this type and exposed through a clean <see cref="Resolve(string)"/> method.
/// </para>
/// <para>
/// TODO: the <c>URIResolver.resolve(href, base)</c> contract returns a
/// <c>javax.xml.transform.Source</c>. That transform-Source integration (and the base-URI
/// resolution it performs for non-data hrefs) lands with the parser/transform port; for now this
/// resolver only understands "data:" URIs and returns the raw decoded bytes / a stream over them.
/// </para>
/// </summary>
public class DataURIResolver
{
    /// <summary>
    /// Decodes a "data:" URI and returns its payload as a byte array.
    /// </summary>
    /// <param name="href">a "data:" URI (e.g. <c>data:image/png;base64,iVBOR...</c>).</param>
    /// <returns>the decoded payload bytes.</returns>
    /// <exception cref="ArgumentException">if <paramref name="href"/> is not a valid "data:" URI.</exception>
    public byte[] Resolve(string href)
    {
        ArgumentNullException.ThrowIfNull(href);

        if (!href.StartsWith("data:", StringComparison.Ordinal))
        {
            throw new ArgumentException(
                $"Not a data URI (must start with 'data:'): {href}", nameof(href));
        }

        // RFC 2397: data:[<mediatype>][;base64],<data>
        // The header is everything between "data:" and the first comma.
        int comma = href.IndexOf(',');
        if (comma < 0)
        {
            throw new ArgumentException(
                $"Malformed data URI (missing comma): {href}", nameof(href));
        }

        string header = href[5..comma];
        string data = href[(comma + 1)..];

        bool base64 = header.EndsWith(";base64", StringComparison.OrdinalIgnoreCase);

        return base64
            ? Convert.FromBase64String(data)
            // Non-base64 data is URL-encoded text; the default charset for "data:" is US-ASCII.
            : Encoding.ASCII.GetBytes(Uri.UnescapeDataString(data));
    }

    /// <summary>
    /// Decodes a "data:" URI and returns a readable stream over its payload.
    /// </summary>
    /// <param name="href">a "data:" URI.</param>
    /// <returns>a non-resizable, seekable <see cref="MemoryStream"/> over the decoded payload.</returns>
    public Stream ResolveStream(string href) => new MemoryStream(Resolve(href), writable: false);
}
