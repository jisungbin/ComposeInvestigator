/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package land.sungbin.composeinvestigator.runtime

// Get from https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/compiler/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/analysis/Stability.kt;l=74;drc=7d3e127599f8ce142445c944c703cf3c3e6d2b3a.

@ComposeInvestigatorCompilerApi
public sealed class DeclarationStability {
  @ComposeInvestigatorCompilerApi
  public class Certain(public val stable: Boolean) : DeclarationStability() {
    override fun toString(): String = if (stable) "Stable" else "Unstable"
  }

  @ComposeInvestigatorCompilerApi
  public class Runtime(public val name: String) : DeclarationStability() {
    override fun toString(): String = "Runtime($name)"
  }

  @ComposeInvestigatorCompilerApi
  public class Unknown(public val name: String) : DeclarationStability() {
    override fun toString(): String = "Uncertain($name)"
  }

  @ComposeInvestigatorCompilerApi
  public class Parameter(public val name: String) : DeclarationStability() {
    override fun toString(): String = "Parameter($name)"
  }

  @ComposeInvestigatorCompilerApi
  public class Combined(public vararg val elements: DeclarationStability) : DeclarationStability() {
    override fun toString(): String = elements.joinToString(",")
  }
}
