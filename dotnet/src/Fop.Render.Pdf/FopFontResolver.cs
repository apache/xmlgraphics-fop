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

using PdfSharp.Fonts;

namespace Fop.Render.Pdf;

/// <summary>
/// The PdfSharp <see cref="IFontResolver"/> used by the FOP-for-.NET PDF renderer. It consults a
/// <see cref="FontRegistry"/> of caller-supplied fonts first; if no custom font matches the requested
/// family, it delegates to the embedded-Liberation mapping (Arial/Helvetica/sans -&gt; LiberationSans,
/// Times/serif -&gt; LiberationSerif, Courier/monospace -&gt; LiberationMono, with bold/italic). This
/// preserves the default behaviour exactly when nothing is registered.
/// <para>
/// <strong>PdfSharp global-resolver caveat.</strong> <see cref="GlobalFontSettings.FontResolver"/> is
/// a single process-global slot, and PdfSharp caches resolved faces process-wide. So:
/// </para>
/// <list type="bullet">
///   <item>There is one shared resolver and one shared <see cref="Registry"/> for the whole process
///   (exposed as <see cref="Shared"/>); every <see cref="FopProcessor"/> sees the same registry.</item>
///   <item>Register a font <em>before</em> the first <c>XFont</c> for that family is created. Once
///   PdfSharp has cached a face for a family, re-registering under the same family will not be
///   picked up until the cache is cleared (process restart, or PdfSharp's cache reset).</item>
/// </list>
/// </summary>
public sealed class FopFontResolver : IFontResolver
{
    private static readonly FopFontResolver SharedInstance = new(new FontRegistry());
    private static readonly Lock InstallLock = new();
    private static bool installed;

    private readonly LiberationFontResolver liberation = LiberationFontResolver.Instance;

    /// <summary>Creates a resolver backed by the given registry (the shared one is usually preferred).</summary>
    public FopFontResolver(FontRegistry registry)
    {
        Registry = registry ?? throw new ArgumentNullException(nameof(registry));
    }

    /// <summary>The process-wide shared resolver installed as the PdfSharp global font resolver.</summary>
    public static FopFontResolver Shared => SharedInstance;

    /// <summary>The registry of caller-supplied fonts consulted before the Liberation fallback.</summary>
    public FontRegistry Registry { get; }

    /// <summary>Installs the shared resolver as the global PdfSharp font resolver (idempotent).</summary>
    public static void EnsureInstalled()
    {
        if (installed)
        {
            return;
        }

        lock (InstallLock)
        {
            if (!installed)
            {
                GlobalFontSettings.FontResolver = SharedInstance;
                installed = true;
            }
        }
    }

    /// <inheritdoc/>
    public FontResolverInfo? ResolveTypeface(string familyName, bool isBold, bool isItalic)
    {
        string? face = Registry.Resolve(familyName, isBold, isItalic);
        if (face is not null)
        {
            return new FontResolverInfo(face);
        }

        return liberation.ResolveTypeface(familyName, isBold, isItalic);
    }

    /// <inheritdoc/>
    public byte[]? GetFont(string faceName)
    {
        byte[]? registered = Registry.GetFaceBytes(faceName);
        if (registered is not null)
        {
            return registered;
        }

        return liberation.GetFont(faceName);
    }
}
