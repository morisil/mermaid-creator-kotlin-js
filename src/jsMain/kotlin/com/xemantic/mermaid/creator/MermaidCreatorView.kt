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

import com.xemantic.kotlin.js.dom.html.*
import com.xemantic.kotlin.js.dom.node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement

/**
 * Creates the Mermaid diagram creator view.
 *
 * Implements MVVM pattern by binding ViewModel state to DOM elements
 * using reactive StateFlow subscriptions.
 *
 * @param viewModel The view model managing diagram state
 * @param scope The coroutine scope for reactive bindings
 * @return The root DOM node
 */
public fun mermaidCreatorView(
  viewModel: MermaidViewModel,
  scope: CoroutineScope
) = node {

  div("mermaid-creator-app") {

    header("app-header") {

      h1 {
        +"Mermaid Creator"
      }

      div("export-buttons") {

        input(type = "button", value = "Load File") { button ->
          button.onclick = {
            // Placeholder for file loading
            console.log("File load functionality to be implemented")
          }
        }

        input(type = "button", value = "Clear") { button ->
          button.onclick = {
            viewModel.clear()
          }
        }

      }

    }

    main("app-main") {

      div("editor-panel") {

        h1 {
          +"Editor"
        }

        val editor: HTMLTextAreaElement = textarea(
          klass = "mermaid-editor",
          placeholder = "Enter Mermaid diagram code here..."
        ) { textarea ->
          textarea.oninput = {
            viewModel.updateCode(textarea.value)
          }
        }

        // Bind ViewModel diagram code to editor
        viewModel.diagram.onEach { diagram ->
          editor.value = diagram.code
        }.launchIn(scope)

      }

      div("preview-panel") {

        h1 {
          +"Preview"
        }

        val preview: HTMLDivElement = div("mermaid-preview") {}

        // Bind ViewModel diagram code to preview
        viewModel.diagram.onEach { diagram ->
          // Placeholder for Mermaid rendering
          // In a real implementation, this would use the Mermaid.js library
          preview.innerHTML = if (diagram.code.isNotBlank()) {
            "<pre>${diagram.code}</pre>"
          } else {
            "<p>No diagram to display</p>"
          }
        }.launchIn(scope)

      }

    }

  }

}
