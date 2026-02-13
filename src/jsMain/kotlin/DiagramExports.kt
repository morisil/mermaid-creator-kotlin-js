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

import com.xemantic.kotlin.js.dom.html.a
import com.xemantic.kotlin.js.dom.node
import kotlinx.browser.document
import org.w3c.dom.*
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * External interface for XMLSerializer.
 */
external class XMLSerializer {
    fun serializeToString(node: Node): String
}

/**
 * PNG export scale factor (2x for higher quality).
 */
private const val PNG_SCALE = 2

/**
 * Triggers a download of the given blob with the specified filename.
 */
private fun triggerDownload(blob: Blob, filename: String) {
    val url = URL.createObjectURL(blob)
    try {
        node { a {
            it.href = url
            it.download = filename
            it.click()
        }}
    } finally {
        URL.revokeObjectURL(url)
    }

}

/**
 * Exports the SVG element as an SVG file.
 */
fun exportSvg(svgElement: SVGElement, filename: String = "diagram.svg") {
    val serializer = XMLSerializer()
    val svgString = serializer.serializeToString(svgElement)
    val blob = Blob(
        arrayOf(svgString),
        BlobPropertyBag(type = "image/svg+xml")
    )
    triggerDownload(blob, filename)
}

/**
 * Exports the SVG element as a PNG file.
 */
suspend fun exportPng(svgElement: SVGElement, filename: String = "diagram.png") {
    try {
        // Serialize SVG to string
        val serializer = XMLSerializer()
        val svgString = serializer.serializeToString(svgElement)

        // Create image from SVG
        val img = Image()
        val svgBlob = Blob(
            arrayOf(svgString),
            BlobPropertyBag(type = "image/svg+xml;charset=utf-8")
        )
        val url = URL.createObjectURL(svgBlob)

        // Wait for image to load
        suspendCoroutine { continuation ->
            img.onload = {
                URL.revokeObjectURL(url)
                continuation.resume(Unit)
            }
            img.onerror = { _, _, _, _, _ ->
                URL.revokeObjectURL(url)
                continuation.resumeWithException(Exception("Failed to load image"))
            }
            img.src = url
        }

        // Create canvas and draw image with scaling
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        val width = img.width * PNG_SCALE
        val height = img.height * PNG_SCALE
        canvas.width = width
        canvas.height = height

        val ctx = canvas.getContext("2d") as? CanvasRenderingContext2D
            ?: throw Exception("Failed to get canvas context")

        // Fill white background
        ctx.fillStyle = "white"
        ctx.fillRect(0.0, 0.0, width.toDouble(), height.toDouble())

        // Draw image scaled
        ctx.drawImage(img, 0.0, 0.0, width.toDouble(), height.toDouble())

        // Convert canvas to blob and download
        suspendCoroutine<Unit> { continuation ->
            canvas.toBlob({ blob: Blob? ->
                if (blob != null) {
                    triggerDownload(blob, filename)
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(Exception("Failed to create PNG blob"))
                }
            }, "image/png")
        }

    } catch (e: Throwable) {
        console.error("Failed to export PNG:", e)
        throw e
    }
}

/**
 * Exports the diagram code as a .mmd file.
 */
fun exportMmd(code: String, filename: String = "diagram.mmd") {
    val blob = Blob(
        arrayOf(code),
        BlobPropertyBag(type = "text/plain")
    )
    triggerDownload(blob, filename)
}
