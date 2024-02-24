/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler.test._compilation

import land.sungbin.composeinvestigator.compiler.test._compilation.facade.SourceFile
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.codegen.GeneratedClassLoader
import java.io.File

abstract class AbstractCodegenTest(useFir: Boolean) : AbstractCompilerTest(useFir) {
  private fun dumpClasses(loader: GeneratedClassLoader) {
    val clazz = loader.allGeneratedFiles.filter { it.relativePath.endsWith(".class") }
    for (file in clazz) {
      println("------\nFILE: ${file.relativePath}\n------")
      println(file.asText())
    }
  }

  protected fun classLoader(
    @Language("kotlin")
    source: String,
    fileName: String,
    dumpClasses: Boolean = false,
  ): GeneratedClassLoader {
    val loader = createClassLoader(listOf(SourceFile(name = fileName, source = source)))
    if (dumpClasses) dumpClasses(loader)
    return loader
  }

  protected fun classLoader(
    sources: Map<String, String>,
    dumpClasses: Boolean = false,
  ): GeneratedClassLoader {
    val sourceFiles = sources.map { (fileName, source) -> SourceFile(name = fileName, source = source) }
    val loader = createClassLoader(platformSourceFiles = sourceFiles)
    if (dumpClasses) dumpClasses(loader)
    return loader
  }

  protected fun classLoader(
    platformSources: Map<String, String>,
    commonSources: Map<String, String>,
    dumpClasses: Boolean = false,
  ): GeneratedClassLoader {
    val loader = createClassLoader(
      platformSourceFiles = platformSources.map { (fileName, source) -> SourceFile(name = fileName, source = source) },
      commonSourceFiles = commonSources.map { (fileName, source) -> SourceFile(name = fileName, source = source) },
    )
    if (dumpClasses) dumpClasses(loader)
    return loader
  }

  protected fun classLoader(
    sources: Map<String, String>,
    additionalPaths: List<File>,
    dumpClasses: Boolean = false,
    forcedFirSetting: Boolean? = null,
  ): GeneratedClassLoader {
    val loader = createClassLoader(
      platformSourceFiles = sources.map { (fileName, source) -> SourceFile(name = fileName, source = source) },
      additionalPaths = additionalPaths,
      forcedFirSetting = forcedFirSetting,
    )
    if (dumpClasses) dumpClasses(loader)
    return loader
  }
}
