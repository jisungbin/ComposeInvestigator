/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.gradle

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

public abstract class ComposeInvestigatorPluginExtension @Inject constructor(objects: ObjectFactory) {
  public val enabled: Property<Boolean> = objects.property(Boolean::class.javaObjectType).convention(true)
  public val verbose: Property<Boolean> = objects.property(Boolean::class.javaObjectType).convention(false)
}
