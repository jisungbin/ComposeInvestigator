/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

@file:Suppress("unused")
@file:OptIn(ExperimentalCompilerApi::class)

package land.sungbin.composeinvalidator.compiler

import com.google.auto.service.AutoService
import land.sungbin.composeinvalidator.compiler.util.VerboseLogger
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CompilerPluginRegistrar::class)
internal class TrackerPluginRegistrar : CompilerPluginRegistrar() {
  override val supportsK2 = false
  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    val verbose = configuration[KEY_VERBOSE]?.toBooleanStrictOrNull() ?: false
    val logger = VerboseLogger(configuration).apply { if (verbose) verbose() }

    IrGenerationExtension.registerExtension(InvalidationTrackExtension(logger = logger))
  }
}
