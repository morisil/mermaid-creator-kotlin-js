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

import org.w3c.dom.svg.SVGElement

/**
 * Data model representing a Mermaid diagram.
 *
 * @property code The Mermaid diagram definition code
 * @property svgElement The rendered SVG element (null if not rendered or error occurred)
 * @property error The error message if rendering failed
 * @property isRendering Whether the diagram is currently being rendered
 */
data class MermaidDiagram(
    val code: String = "",
    val svgElement: SVGElement? = null,
    val error: String? = null,
    val isRendering: Boolean = false
) {
    /**
     * Whether the diagram has valid content.
     */
    val hasDiagram: Boolean
        get() = code.isNotBlank()
}
