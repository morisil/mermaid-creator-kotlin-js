# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the project
./gradlew build

# Run in browser with hot reload (development server)
./gradlew jsBrowserDevelopmentRun --continuous

# Run tests
./gradlew jsTest

# Check binary API compatibility
./gradlew apiCheck

# Update binary API dump after public API changes
./gradlew apiDump
```

CI runs `./gradlew build` for PRs and `./gradlew build publishToMavenCentral --no-configuration-cache` on main.

## Project Overview

Kotlin/JS browser application for creating and exporting Mermaid diagrams. Compiles to a single JS executable loaded by `src/jsMain/resources/index.html`. Mermaid.js v11 is included as an npm dependency via Gradle (`npm("mermaid", ...)`) and bundled by webpack.

## Architecture

**MVVM pattern** with three layers:

- **Model** (`MermaidDiagram.kt`) - Immutable data class holding diagram state: `code`, `svgElement`, `error`, `isRendering`
- **ViewModel** (`MermaidViewModel.kt`) - Owns `CoroutineScope(SupervisorJob())`, manages state via `MutableStateFlow<MermaidDiagram>` (explicit backing field), handles debounced rendering (300ms), code validation (100k char limit), and Mermaid.js interop through `Promise.await()`
- **View** (`MermaidCreatorView.kt`) - Builds DOM using type-safe DSL from `xemantic-kotlin-js`. Binds to ViewModel via `StateFlow.onEach().launchIn(scope)`. Two-panel layout: editor textarea + SVG preview

**Supporting files:**
- `MermaidJs.kt` - Kotlin external declarations for the Mermaid.js API (`window.mermaid`)
- `DiagramExports.kt` - Export functions (SVG, PNG via canvas with 2x scaling, .mmd file download)
- `main.kt` - Entry point: initializes Mermaid.js, creates `CoroutineScope(SupervisorJob())`, wires ViewModel to View

## Key Conventions

- Kotlin language version target: 2.3 with experimental features enabled (`-Xcontext-parameters`, `-Xcontext-sensitive-resolution`)
- All public API must have KDoc and be tracked in `api/xemantic-project-template.api` (binary compatibility validator)
- DOM construction uses `node { }` / `div { }` / `input { }` DSL from `com.xemantic.kotlin.js.dom`
- Tests use `com.xemantic.kotlin.test.assert` / `have` functions (power-assert enhanced)
- Apache License 2.0 header required on all source files