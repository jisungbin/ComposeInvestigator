// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("MemberVisibilityCanBePrivate")

package land.sungbin.composeinvestigator.runtime

/**
 * Represents Compose's stability system. See the
 * [official documentation](https://developer.android.com/jetpack/compose/performance/stability)
 * for more information about Compose's stability.
 *
 * The stability inference algorithm uses the
 * [Compose compiler's implementation](https://github.com/JetBrains/kotlin/blob/ede0373c4e5c0506b1491c6eb4c8bc0660ef7d21/plugins/compose/compiler-hosted/src/main/java/androidx/compose/compiler/plugins/kotlin/analysis/Stability.kt#L238)
 * of the AOSP.
 */
public sealed class Stability {
  abstract override fun toString(): String

  public data class Certain(public val stable: Boolean) : Stability() {
    override fun toString(): String = if (stable) "Stable" else "Unstable"
  }

  public data class Runtime(public val name: String) : Stability() {
    override fun toString(): String = "Runtime($name)"
  }

  public data class Unknown(public val name: String) : Stability() {
    override fun toString(): String = "Uncertain($name)"
  }

  public data class Parameter(public val name: String) : Stability() {
    override fun toString(): String = "Parameter($name)"
  }

  public class Combined(public vararg val elements: Stability) : Stability() {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || this::class != other::class) return false

      other as Combined

      return elements.contentEquals(other.elements)
    }

    override fun hashCode(): Int = elements.contentHashCode()
    override fun toString(): String = elements.joinToString(",")
  }

  public companion object {
    public val Stable: Certain = Certain(stable = true)
    public val Unstable: Certain = Certain(stable = false)
  }
}

public fun Stability.toCertainString(): String = if (knownStable()) "Stable" else "Unstable"

public fun Stability.knownStable(): Boolean =
  when (this) {
    is Stability.Certain -> stable
    is Stability.Runtime -> false
    is Stability.Unknown -> false
    is Stability.Parameter -> false
    is Stability.Combined -> elements.all(Stability::knownStable)
  }

public fun Stability.knownUnstable(): Boolean =
  when (this) {
    is Stability.Certain -> !stable
    is Stability.Runtime -> false
    is Stability.Unknown -> false
    is Stability.Parameter -> false
    is Stability.Combined -> elements.any(Stability::knownUnstable)
  }

public fun Stability.isUncertain(): Boolean =
  when (this) {
    is Stability.Certain -> false
    is Stability.Runtime -> true
    is Stability.Unknown -> true
    is Stability.Parameter -> true
    is Stability.Combined -> elements.any(Stability::isUncertain)
  }

public fun Stability.isExpressible(): Boolean =
  when (this) {
    is Stability.Certain -> true
    is Stability.Runtime -> true
    is Stability.Unknown -> false
    is Stability.Parameter -> true
    is Stability.Combined -> elements.all(Stability::isExpressible)
  }
