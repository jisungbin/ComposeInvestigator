/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvalidator/blob/main/LICENSE
 */

package land.sungbin.composeinvalidator.compiler.internal.transformer

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.Name

//internal fun IrPluginContext.irComposableInvalidationTrackTableClass(
//  val class
//): IrClass =
//  irFactory.buildClass {
//    kind = ClassKind.OBJECT
//    visibility = DescriptorVisibilities.INTERNAL
//    name = Name.identifier("LiveLiterals${"$"}$shortName")
//  }