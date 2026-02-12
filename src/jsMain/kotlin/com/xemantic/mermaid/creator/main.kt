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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Main entry point for the Mermaid Creator application.
 *
 * Sets up the MVVM architecture by creating the ViewModel and View,
 * then renders the UI.
 */
public fun main() {
  // Create application-level coroutine scope
  val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  // Create ViewModel
  val viewModel = MermaidViewModel(appScope)

  // Create and attach view to DOM
  val view = mermaidCreatorView(viewModel, appScope)

  document.getElementById("root")?.appendChild(view)
}
