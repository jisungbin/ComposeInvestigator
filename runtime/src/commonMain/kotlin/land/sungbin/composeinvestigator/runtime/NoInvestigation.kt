// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

/**
 * ComposeInvestigator does not work on annotated files or Composable functions.
 *
 * - File: `@file:NoInvestigation`
 * - Composable: `@NoInvestigation @Composable fun MyComposable()`
 *
 * If a file does not contain any Composables, it will automatically become `@file:NoInvestigation`.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FILE, AnnotationTarget.FUNCTION)
public annotation class NoInvestigation
