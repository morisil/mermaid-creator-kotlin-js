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

import com.xemantic.kotlin.js.dom.NodeBuilder
import com.xemantic.kotlin.js.dom.html.*
import com.xemantic.kotlin.js.dom.invoke
import com.xemantic.kotlin.js.dom.node
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.dom.clear
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get

/**
 * Creates the Mermaid diagram creator view.
 *
 * Implements MVVM pattern by binding ViewModel state to DOM elements
 * using reactive StateFlow subscriptions.
 *
 * @param viewModel The view model managing diagram state
 * @return The root DOM node
 */
fun mermaidCreatorView(
    viewModel: MermaidViewModel
) = node { div("mermaid-creator-app") {
    header("app-header") {
        h1 { +"Mermaid Creator" }
        div("export-buttons") {

            val fileInput = fileInput(viewModel) // (hidden)
            input(type = "button", value = "Load File") {
                it.onclick = { fileInput.click() }
            }

            val exportSvgButton = input(
                type = "button",
                value = "Export SVG"
            ) {
                it.disabled = true
                it.onclick = { viewModel.exportSvg() }
            }

            val exportPngButton = input(
                type = "button",
                value = "Export PNG"
            ) {
                it.disabled = true
                it.onclick = { viewModel.exportPng() }
            }

            // Save .mmd button
            val saveMmdButton: HTMLInputElement = input(
                type = "button",
                value = "Save .mmd",
                klass = "secondary"
            ) {
                it.disabled = true
                it.onclick = { viewModel.saveMmd() }
            }

            // Clear button
            input(type = "button", value = "Clear") {
                it.onclick = {
                    viewModel.clear()
                }
            }

            // Update button states based on diagram and export state
            viewModel.diagram.combine(viewModel.isExportingPng) { diagram, exporting ->
                val hasValidDiagram = diagram.svgElement != null
                exportSvgButton.disabled = !hasValidDiagram
                exportPngButton.disabled = !hasValidDiagram || diagram.isRendering || exporting
                exportPngButton.value = if (exporting) "Exporting..." else "Export PNG"
                saveMmdButton.disabled = diagram.code.isBlank()
            }.launchIn(viewModel.scope)

        }

    }

    main("app-main") {
        div("editor-panel") {
            h1 { +"Editor" }

            val editor = textarea(
                klass = "mermaid-editor",
                placeholder = "Enter Mermaid diagram code here..."
            ) { textarea ->
                textarea.oninput = {
                    viewModel.updateCode(textarea.value)
                }
            }

            // Bind ViewModel diagram code to editor
            // Guard against unnecessary updates to prevent cursor jumps
            viewModel.diagram.onEach { diagram ->
                if (editor.value != diagram.code) {
                    editor.value = diagram.code
                }
            }.launchIn(viewModel.scope)

        }

        div("preview-panel") {
            h1 { +"Preview" }

            val preview = div("mermaid-preview")

            // Bind ViewModel diagram state to preview
            viewModel.diagram.onEach { diagram ->

                preview.clear()

                when {
                    diagram.isRendering -> {
                        preview {
                            +"Rendering diagram..."
                        }
                    }

                    diagram.error != null -> {
                        preview {
                            div("error-message") {
                                +"Error: ${diagram.error}"
                            }
                        }
                    }

                    diagram.svgElement != null -> {
                        val clonedSvg = diagram.svgElement.cloneNode(deep = true)
                        preview {
                            +clonedSvg
                        }
                    }

                    diagram.code.isBlank() -> {
                        preview {
                            +"No diagram to display"
                        }
                    }

                }
            }.launchIn(viewModel.scope)

        }

    }

}}

fun NodeBuilder.fileInput(viewModel: MermaidViewModel) = input(
    type = "file"
) {
    it.accept = ".mmd,.txt,.md"
    it.style.display = "none"
    it.onchange = { _ ->
        val file = it.files?.get(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                val content = reader.result as String
                viewModel.loadFromFile(content)
            }
            reader.readAsText(file)
        }
    }
}
