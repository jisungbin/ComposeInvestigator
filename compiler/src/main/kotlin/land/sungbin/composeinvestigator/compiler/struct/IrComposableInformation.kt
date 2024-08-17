/*
 * Developed by Ji Sungbin 2024.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.struct

import land.sungbin.composeinvestigator.compiler.COMPOSABLE_INFORMATION_FQN
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.utils.addToStdlib.cast

public class IrComposableInformation(context: IrPluginContext) {
  private val symbol = context.referenceClass(ClassId.topLevel(COMPOSABLE_INFORMATION_FQN))!!

  public operator fun invoke(
    name: IrConst<String>,
    packageName: IrConst<String>,
    fileName: IrConst<String>,
  ): IrConstructorCallImpl = IrConstructorCallImpl.fromSymbolOwner(
    type = symbol.defaultType,
    constructorSymbol = symbol.constructors.single(),
  ).apply {
    putValueArgument(0, name)
    putValueArgument(1, packageName)
    putValueArgument(2, fileName)
  }

  public fun copyFrom(
    target: IrConstructorCall,
    name: IrConst<String>,
  ): IrConstructorCallImpl = invoke(
    name = name,
    packageName = target.getValueArgument(1)!!.cast(),
    fileName = target.getValueArgument(2)!!.cast(),
  )

  public companion object {
    public fun getName(target: IrConstructorCall): IrConst<String> = target.getValueArgument(0)!!.cast()
  }
}
