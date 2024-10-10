// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.runtime

/** APIs annotated with this should only be used within Composable functions. */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER,
)
public annotation class ComposableScope
