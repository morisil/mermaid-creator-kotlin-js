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

import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * Mermaid.js module imported as an npm/webpack dependency.
 */
@JsModule("mermaid")
@JsNonModule
external object MermaidModule {
    @JsName("default")
    val mermaid: Mermaid
}

/**
 * Access to the Mermaid.js API.
 */
internal val mermaid: Mermaid get() = MermaidModule.mermaid

/**
 * External interface for Mermaid.js library.
 */
external interface Mermaid {
    fun initialize(config: dynamic)
    fun render(id: String, code: String): Promise<RenderResult>
}

suspend fun Mermaid.suspendRender(id: String, code: String): RenderResult = render(id, code).await()

/**
 * Result of Mermaid rendering.
 */
external interface RenderResult {
    val svg: String
    val bindFunctions: dynamic
}
