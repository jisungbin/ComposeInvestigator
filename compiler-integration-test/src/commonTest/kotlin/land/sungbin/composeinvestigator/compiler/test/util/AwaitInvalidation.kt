// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler.test.util

import kotlinx.coroutines.test.TestScope

@Suppress("NOTHING_TO_INLINE")
inline fun TestScope.awaitInvalidation() = testScheduler.advanceUntilIdle()
