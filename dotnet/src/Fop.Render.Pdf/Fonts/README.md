# Bundled fonts

The `Liberation*` TrueType fonts in this folder are embedded as resources and used by the default
`LiberationFontResolver` so the PDF renderer works out of the box on any platform (PdfSharp 6+ no
longer ships the PDF base-14 core fonts and requires a font resolver that supplies real font data).

The Liberation fonts are metric-compatible substitutes for Arial (Sans), Times New Roman (Serif),
and Courier New (Mono).

- Copyright (c) 2010 Google Corporation (Reserved Font Name: Arimo, Tinos, Cousine)
- Copyright (c) 2012 Red Hat, Inc. (Reserved Font Name: Liberation)
- Licensed under the **SIL Open Font License, Version 1.1** — <http://scripts.sil.org/OFL>

These fonts are redistributable under the OFL; this notice satisfies the attribution requirement.
Applications may register their own `IFontResolver` to use different fonts.
