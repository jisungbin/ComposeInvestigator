// Copyright 2024 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.gradle

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class ComposeInvestigatorPluginExtension @Inject public constructor(objects: ObjectFactory) {
  public val enabled: Property<Boolean> = objects.property<Boolean>().convention(true)
  public val verbose: Property<Boolean> = objects.property<Boolean>().convention(false)
}
