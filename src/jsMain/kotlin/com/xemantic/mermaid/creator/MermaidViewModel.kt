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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for managing Mermaid diagram state.
 *
 * Implements MVVM pattern by exposing reactive state through StateFlow
 * and providing methods to update the diagram.
 *
 * @property scope The coroutine scope for managing asynchronous operations
 */
public class MermaidViewModel(
  private val scope: CoroutineScope
) {

  private val _diagram = MutableStateFlow(MermaidDiagram())

  /**
   * Observable state flow of the current diagram.
   */
  public val diagram: StateFlow<MermaidDiagram> = _diagram.asStateFlow()

  /**
   * Updates the Mermaid diagram code.
   *
   * @param code The new Mermaid diagram definition
   */
  public fun updateCode(code: String) {
    _diagram.update { current ->
      current.copy(
        code = code,
        hasDiagram = code.isNotBlank()
      )
    }
  }

  /**
   * Loads diagram code from a file content.
   *
   * @param content The file content containing Mermaid diagram definition
   */
  public fun loadFromFile(content: String) {
    updateCode(content)
  }

  /**
   * Clears the current diagram.
   */
  public fun clear() {
    _diagram.value = MermaidDiagram()
  }

}
