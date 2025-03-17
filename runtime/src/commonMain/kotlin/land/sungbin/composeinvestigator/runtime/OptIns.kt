// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

@RequiresOptIn(
  "This API is designed for use with the ComposeInvestigator compiler only; " +
    "manual use may cause undesired results.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
public annotation class ComposeInvestigatorCompilerApi
