/*
 * Copyright 2025 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.mermaid.creator

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.svg.SVGElement
import kotlin.js.Date

/**
 * Represents the state of the diagram preview panel.
 */
sealed interface PreviewState {

    /**
     * A status message to display.
     *
     * @property text The message text.
     */
    data class Message(val text: String) : PreviewState

    /**
     * Rendering failed with an error.
     *
     * @property message The error message.
     */
    data class Error(val message: String) : PreviewState

    /**
     * The diagram has been successfully rendered.
     *
     * @property svgElement The rendered SVG element.
     */
    data class Diagram(val svgElement: SVGElement) : PreviewState

}

/**
 * ViewModel for managing Mermaid diagram state.
 *
 * Implements MVVM pattern by exposing reactive state through individual StateFlows
 * and providing methods to update the diagram. Derived flows encapsulate UI decisions
 * so the View can bind directly without logic.
 *
 * @param notifier The notifier used to display messages to the user
 */
class MermaidViewModel(
    private val notifier: Notifier
) {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val maxCodeLength = 100_000
    private val debounceDelayMs = 300L
    private var renderJob: Job? = null

    /**
     * The Mermaid diagram definition code.
     */
    val code: StateFlow<String>
        field = MutableStateFlow("")

    private val svgElement = MutableStateFlow<SVGElement?>(null)
    private val error = MutableStateFlow<String?>(null)
    private val isRendering = MutableStateFlow(false)
    private val isExportingPng = MutableStateFlow(false)

    /**
     * Whether the Export SVG button should be enabled.
     */
    val exportSvgEnabled: StateFlow<Boolean> = svgElement
        .map { it != null }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /**
     * Whether the Export PNG button should be enabled.
     */
    val exportPngEnabled: StateFlow<Boolean> = combine(
        svgElement, isRendering, isExportingPng
    ) { svg, rendering, exporting ->
        svg != null && !rendering && !exporting
    }.stateIn(scope, SharingStarted.Eagerly, false)

    /**
     * The label for the Export PNG button.
     */
    val exportPngLabel: StateFlow<String> = isExportingPng
        .map { if (it) "Exporting..." else "Export PNG" }
        .stateIn(scope, SharingStarted.Eagerly, "Export PNG")

    /**
     * Whether the Save .mmd button should be enabled.
     */
    val saveMmdEnabled: StateFlow<Boolean> = code
        .map { it.isNotBlank() }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /**
     * The current state of the diagram preview panel.
     */
    val previewState: StateFlow<PreviewState> = combine(
        isRendering, error, svgElement, code
    ) { rendering, err, svg, _ ->
        when {
            rendering -> PreviewState.Message("Rendering diagram...")
            err != null -> PreviewState.Error(err)
            svg != null -> PreviewState.Diagram(svg.cloneNode(deep = true) as SVGElement)
            else -> PreviewState.Message("No diagram to display")
        }
    }.stateIn(scope, SharingStarted.Eagerly, PreviewState.Message("No diagram to display"))

    /**
     * Updates the Mermaid diagram code.
     *
     * Validates that the code length doesn't exceed the maximum allowed length.
     * Triggers debounced rendering.
     *
     * @param code The new Mermaid diagram definition
     */
    fun updateCode(code: String) {
        val validatedCode = if (code.length > maxCodeLength) {
            console.warn("Diagram code exceeds maximum length of $maxCodeLength characters. Truncating.")
            code.take(maxCodeLength)
        } else {
            code
        }

        this.code.value = validatedCode
        svgElement.value = null
        error.value = null

        renderJob?.cancel()
        renderJob = scope.launch {
            delay(debounceDelayMs)
            renderDiagram()
        }
    }

    /**
     * Renders the current diagram using Mermaid.js.
     */
    private suspend fun renderDiagram() {
        val currentCode = code.value

        if (currentCode.isBlank()) {
            svgElement.value = null
            error.value = null
            isRendering.value = false
            return
        }

        isRendering.value = true
        error.value = null

        try {
            val id = "mermaid-${Date.now().toLong()}"
            val result = mermaid.suspendRender(id, currentCode)

            val parser = DOMParser()
            val svgDoc = parser.parseFromString(result.svg, "image/svg+xml")
            val svg = svgDoc.documentElement as? SVGElement

            svgElement.value = svg
            error.value = null
            isRendering.value = false
        } catch (e: Throwable) {
            console.error("Failed to render diagram:", e)
            svgElement.value = null
            error.value = e.message ?: "Failed to render diagram"
            isRendering.value = false
        }
    }

    /**
     * Exports the current diagram as an SVG file.
     */
    fun exportSvg() {
        val svg = svgElement.value
        if (svg != null) {
            exportSvg(svg)
        } else {
            notifier.notify("No diagram to export. Check syntax.")
        }
    }

    /**
     * Exports the current diagram as a PNG file.
     *
     * Manages the exporting state to disable the button and show progress.
     */
    fun exportPng() {
        val svg = svgElement.value
        if (svg != null) {
            scope.launch {
                try {
                    isExportingPng.value = true
                    exportPng(svg)
                } catch (e: Throwable) {
                    notifier.notify("Failed to export PNG: ${e.message}")
                } finally {
                    isExportingPng.value = false
                }
            }
        } else {
            notifier.notify("No diagram to export. Check syntax.")
        }
    }

    /**
     * Saves the current diagram code as a .mmd file.
     */
    fun saveMmd() {
        val currentCode = code.value
        if (currentCode.isNotBlank()) {
            exportMmd(currentCode)
        } else {
            notifier.notify("No diagram code to save.")
        }
    }

    /**
     * Loads diagram code from a file.
     */
    fun loadFromFile(content: String) {
        updateCode(content)
    }

    /**
     * Clears the current diagram.
     */
    fun clear() {
        renderJob?.cancel()
        code.value = ""
        svgElement.value = null
        error.value = null
        isRendering.value = false
    }

}