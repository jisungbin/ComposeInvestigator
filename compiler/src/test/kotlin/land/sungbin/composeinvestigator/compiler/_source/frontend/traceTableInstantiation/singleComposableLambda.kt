// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("unused")

package land.sungbin.composeinvestigator.compiler._source.frontend.traceTableInstantiation

import androidx.compose.runtime.Composable

private fun m() {
  A().c {}
}

private class A {
  fun c(l: @Composable () -> Unit) {}
}
