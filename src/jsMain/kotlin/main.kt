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

import com.xemantic.kotlin.js.dom.invoke
import kotlinx.browser.document
import kotlinx.browser.window
import org.intellij.lang.annotations.Language

/**
 * Default example diagram to show on first load.
 */
@Language("mermaid")
private val DEFAULT_DIAGRAM = """
graph TD
Start --> Stop
""".trimIndent()

/**
 * Main entry point for the Mermaid Creator application.
 *
 * Sets up the MVVM architecture by creating the ViewModel and View,
 * then renders the UI.
 */
fun main() {
    mermaid.initialize(
        js("{ startOnLoad: false, theme: 'neutral', securityLevel: 'loose' }")
    )
    val viewModel = MermaidViewModel(
        notifier = { message -> window.alert(message) }
    )
    val view = mermaidCreatorView(viewModel)
    document.body!! {
        +view
    }
    viewModel.updateCode(DEFAULT_DIAGRAM)
}
