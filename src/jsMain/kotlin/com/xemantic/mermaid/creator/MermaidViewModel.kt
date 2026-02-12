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

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.w3c.dom.Document
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.svg.SVGElement
import kotlin.js.Date

/**
 * ViewModel for managing Mermaid diagram state.
 *
 * Implements MVVM pattern by exposing reactive state through StateFlow
 * and providing methods to update the diagram.
 */
public class MermaidViewModel(
  private val scope: CoroutineScope
) {

  private val _diagram = MutableStateFlow(MermaidDiagram())

  /**
   * Maximum allowed length for diagram code to prevent performance issues.
   */
  private val maxCodeLength = 100_000

  /**
   * Debounce delay for rendering in milliseconds.
   */
  private val debounceDelayMs = 300L

  /**
   * Job for debounced rendering.
   */
  private var renderJob: Job? = null

  /**
   * Observable state flow of the current diagram.
   */
  public val diagram: StateFlow<MermaidDiagram> = _diagram.asStateFlow()

  /**
   * Updates the Mermaid diagram code.
   *
   * Validates that the code length doesn't exceed the maximum allowed length.
   * Triggers debounced rendering.
   *
   * @param code The new Mermaid diagram definition
   */
  public fun updateCode(code: String) {
    // Validate code length to prevent performance issues
    val validatedCode = if (code.length > maxCodeLength) {
      console.warn("Diagram code exceeds maximum length of $maxCodeLength characters. Truncating.")
      code.take(maxCodeLength)
    } else {
      code
    }

    _diagram.update { current ->
      current.copy(code = validatedCode, svgElement = null, error = null)
    }

    // Debounce rendering
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
    val currentCode = _diagram.value.code

    if (currentCode.isBlank()) {
      _diagram.update { it.copy(svgElement = null, error = null, isRendering = false) }
      return
    }

    _diagram.update { it.copy(isRendering = true, error = null) }

    try {
      val id = "mermaid-${Date.now().toLong()}"
      val result = mermaid.render(id, currentCode).await()

      // Parse SVG string to SVGElement
      val parser = DOMParser()
      val svgDoc = parser.parseFromString(result.svg, "image/svg+xml")
      val svgElement = svgDoc.documentElement as? SVGElement

      _diagram.update { current ->
        current.copy(
          svgElement = svgElement,
          error = null,
          isRendering = false
        )
      }
    } catch (e: Throwable) {
      console.error("Failed to render diagram:", e)
      _diagram.update { current ->
        current.copy(
          svgElement = null,
          error = e.message ?: "Failed to render diagram",
          isRendering = false
        )
      }
    }
  }

  /**
   * Loads diagram code from a file.
   */
  public fun loadFromFile(content: String) {
    updateCode(content)
  }

  /**
   * Clears the current diagram.
   */
  public fun clear() {
    renderJob?.cancel()
    _diagram.value = MermaidDiagram()
  }

}
