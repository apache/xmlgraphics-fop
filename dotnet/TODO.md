# Port TODO — Apache FOP → C# / .NET 10

Status legend: `[x]` done · `[~]` in progress · `[ ]` not started.

The port proceeds **bottom-up**: each phase depends only on phases above it, and each item should
land with passing tests before the next begins. Line counts are the Java LOC of the corresponding
package, as a rough size signal.

---

## Phase 0 — Foundation & scaffolding  `[x]`

- [x] Analyse the Java codebase (modules, package map, dependency stack, LOC).
- [x] Create `dotnet/` solution: `global.json` (.NET 10), `Directory.Build.props`, `Fop.slnx`.
- [x] Establish projects: `Fop.Util`, `Fop.Events`, `Fop.Core`, test projects.
- [x] Wire modern libraries: SixLabors.ImageSharp / .Drawing / Fonts.
- [x] Author `CLAUDE.md` (conventions + library mapping) and this `TODO.md`.

## Phase 1 — Utilities & events  `[~]`

- [x] `Fop.Util.CharUtilities` (+ `CharClass`) — whitespace/codepoint helpers.
- [x] `Fop.Util.CompareUtil` — equals/hashCode helpers with Java `double` semantics.
- [x] `Fop.Events` — `Event`, `EventSeverity`, `IEventListener`, `IEventBroadcaster`,
      `CompositeEventListener`, `DefaultEventBroadcaster` (DispatchProxy producers), `EventException`.
- [x] Tests: `BasicEventTests` (mirrors `BasicEventTestCase`), `CharUtilitiesTests`, `CompareUtilTests`.
- [ ] Remaining `org.apache.fop.util` helpers: `StringUtils`, `HexEncoder`, `ListUtil`,
      `LanguageTags`, `ConversionUtils`, `XMLUtil`, `ColorUtil`, `QName`, `DataURLUtil`, …
- [ ] `fop-util` text: `AdvancedMessageFormat`, `XMLResourceBundle` (needed by `EventFormatter`).
- [ ] `Fop.Events` extras: `EventFormatter` (localised messages via `System.Resources`),
      `EventExceptionManager` parity, model classes if still needed.

## Phase 2 — Data types & traits  `[ ]`  (~2,500 LOC)

- [ ] `Fop.DataTypes`: `Length`, `Numeric`, `PercentBase`, `Fixed`/numeric, `LengthBase`, `URISpecification`.
- [ ] `Fop.Traits`: `BorderProps`, `SpaceVal`, `MinOptMax`, `Direction`, `WritingMode`, colour traits.
- [ ] Colour handling on top of ImageSharp / `System.Drawing`-free colour model.

## Phase 3 — Fonts  `[ ]`  (~26,000 LOC)

- [ ] `Fop.Fonts` core: `Font`, `FontInfo`, `FontMetrics`, `Typeface`, `FontTriplet`, `CodePointMapping`.
- [ ] TrueType/OpenType/Type1 loading & metrics via **SixLabors.Fonts** (replacing FOP's own parsers
      where practical; port the parsers where embedding needs raw tables).
- [ ] Font embedding/subsetting hooks for the PDF/PS/AFP renderers.
- [ ] `Fop.ComplexScripts` (bidi + shaping) — large; can be deferred behind a feature flag.

## Phase 4 — FO tree  `[ ]`  (~44,000 LOC)

- [ ] `Fop.Fo` core: `FONode`, `FObj`, `FOText`, `PropertyList`, `FOEventHandler`, `ElementMapping`.
- [ ] `Fop.Fo.Properties` — the property subsystem (largest single area).
- [ ] `Fop.Fo.Expr` — the XSL-FO property expression evaluator.
- [ ] `Fop.Fo.Flow`, `Fop.Fo.Flow.Table`, `Fop.Fo.Pagination` — the formatting objects.
- [ ] SAX/`XmlReader` driven `FOTreeBuilder`.

## Phase 5 — Area tree & layout  `[ ]`  (~46,000 LOC)

- [ ] `Fop.Area` — area model + `AreaTreeHandler`/`AreaTreeModel`.
- [ ] `Fop.LayoutManager` — Knuth-based line/page breaking, blocks, inlines, lists, tables, footnotes.

## Phase 6 — Renderers  `[ ]`  (~79,000 LOC)

Independent back-ends; port in priority order. Each can be its own project (`Fop.Render.Pdf`, …).

- [ ] `Fop.Render` shared: `Renderer`, `IFDocumentHandler` (intermediate format), painter abstractions.
- [ ] **PDF** (`Fop.Render.Pdf` + `Fop.Pdf`, ~24,000 LOC) — primary target.
- [ ] Bitmap/Java2D → **ImageSharp** raster renderer (`Fop.Render.Bitmap`).
- [ ] PostScript, AFP, PCL, RTF, TXT, intermediate XML — subsequent.
- [ ] Image loading pipeline (`Fop.Imaging`) — built on ImageSharp; `ImageDimensions` started.

## Phase 7 — Front-end & integration  `[ ]`

- [ ] `Fop.Cli` — command-line tool (port of `org.apache.fop.cli`).
- [ ] `FopFactory` / `Fop` user-facing API, configuration (`Fop.Configuration`).
- [ ] Hyphenation (`Fop.Hyphenation`) + bundled patterns.
- [ ] Accessibility / tagged PDF, SVG (Batik) integration strategy.

## Cross-cutting / ongoing

- [ ] Port JUnit suites alongside each module; add golden-file output comparisons vs. the Java FOP
      for end-to-end equivalence once a renderer works.
- [ ] CI workflow: `dotnet build` + `dotnet test` on the `dotnet/` solution.
- [ ] Decide SVG story (Batik has no direct .NET equivalent — evaluate ImageSharp.Drawing + a
      dedicated SVG parser, or scope SVG out initially).
- [ ] Replace `commons-logging` call sites with `Microsoft.Extensions.Logging`.
