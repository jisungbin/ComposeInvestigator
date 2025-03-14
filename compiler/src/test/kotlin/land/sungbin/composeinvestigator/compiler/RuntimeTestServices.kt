// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import java.io.File
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices

private val investigatorRuntimeClasspath by lazy {
  System.getProperty("investigatorRuntime.classpath")?.split(File.pathSeparator)?.map(::File)
    ?: error("Unable to get a valid classpath from 'investigatorRuntime.classpath' property")
}

class RuntimeEnvironmentConfigurator(service: TestServices) : EnvironmentConfigurator(service) {
  override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
    configuration.addJvmClasspathRoots(investigatorRuntimeClasspath)
  }
}

class ComposeInvestigatorRuntimeClasspathProvider(service: TestServices) : RuntimeClasspathProvider(service) {
  override fun runtimeClassPaths(module: TestModule): List<File> = investigatorRuntimeClasspath
}
