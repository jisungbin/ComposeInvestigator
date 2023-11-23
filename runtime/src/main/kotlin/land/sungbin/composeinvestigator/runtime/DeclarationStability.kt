/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

// This code is based on https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/compiler/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/analysis/Stability.kt;l=74;drc=7d3e127599f8ce142445c944c703cf3c3e6d2b3a.

@file:Suppress("MemberVisibilityCanBePrivate")

package land.sungbin.composeinvestigator.runtime

// The name is prefixed with "Declaration" to distinguish it from AOSP's Stability.
public sealed class DeclarationStability {
  public class Certain(public val stable: Boolean) : DeclarationStability() {
    override fun toString(): String = if (stable) "Stable" else "Unstable"
  }

  public class Runtime(public val name: String) : DeclarationStability() {
    override fun toString(): String = "Runtime($name)"
  }

  public class Unknown(public val name: String) : DeclarationStability() {
    override fun toString(): String = "Uncertain($name)"
  }

  public class Parameter(public val name: String) : DeclarationStability() {
    override fun toString(): String = "Parameter($name)"
  }

  public class Combined(public vararg val elements: DeclarationStability) : DeclarationStability() {
    override fun toString(): String = elements.joinToString(",")
  }
}

public fun DeclarationStability.toCertainString(): String = if (knownStable()) "Stable" else "Unstable"

public fun DeclarationStability.knownStable(): Boolean =
  when (this) {
    is DeclarationStability.Certain -> stable
    is DeclarationStability.Runtime -> false
    is DeclarationStability.Unknown -> false
    is DeclarationStability.Parameter -> false
    is DeclarationStability.Combined -> elements.all(DeclarationStability::knownStable)
  }

public fun DeclarationStability.knownUnstable(): Boolean =
  when (this) {
    is DeclarationStability.Certain -> !stable
    is DeclarationStability.Runtime -> false
    is DeclarationStability.Unknown -> false
    is DeclarationStability.Parameter -> false
    is DeclarationStability.Combined -> elements.any(DeclarationStability::knownUnstable)
  }

public fun DeclarationStability.isUncertain(): Boolean =
  when (this) {
    is DeclarationStability.Certain -> false
    is DeclarationStability.Runtime -> true
    is DeclarationStability.Unknown -> true
    is DeclarationStability.Parameter -> true
    is DeclarationStability.Combined -> elements.any(DeclarationStability::isUncertain)
  }

public fun DeclarationStability.isExpressible(): Boolean =
  when (this) {
    is DeclarationStability.Certain -> true
    is DeclarationStability.Runtime -> true
    is DeclarationStability.Unknown -> false
    is DeclarationStability.Parameter -> true
    is DeclarationStability.Combined -> elements.all(DeclarationStability::isExpressible)
  }
