/*
 * Designed and developed by Ji Sungbin 2023.
 *
 * Licensed under the MIT.
 * Please see full license: https://github.com/jisungbin/ComposeInvestigator/blob/main/LICENSE
 */

package land.sungbin.composeinvestigator.compiler.internal.tracker.table

import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_KEY_NAME_FQN_GETTER_INTRINSIC
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN_GETTER_INTRINSIC
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN_SETTER_INTRINSIC
import land.sungbin.composeinvestigator.compiler.internal.COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN
import land.sungbin.composeinvestigator.compiler.internal.CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN_GETTER_INTRINSIC
import land.sungbin.composeinvestigator.compiler.internal.irBoolean
import land.sungbin.composeinvestigator.compiler.internal.irString
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.DurableWritableSlices
import land.sungbin.composeinvestigator.compiler.internal.tracker.key.irTracee
import land.sungbin.fastlist.fastLastOrNull
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.utils.addToStdlib.cast

internal class InvalidationTrackTableCallTransformer(
  private val context: IrPluginContext,
  private val table: IrInvalidationTrackTable,
) : IrElementTransformerVoidWithContext(), IrPluginContext by context {
  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    withinScope(declaration) { declaration.body?.transformChildrenVoid() }
    return super.visitSimpleFunction(declaration)
  }

  override fun visitCall(expression: IrCall): IrExpression {
    val callFqName = expression.symbol.owner.kotlinFqName
    val callParentFqName = expression.symbol.owner.parent.kotlinFqName
    return when {
      callFqName == CURRENT_COMPOSABLE_INVALIDATION_TRACKER_FQN_GETTER_INTRINSIC -> {
        table.propGetter()
      }
      callFqName == COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN_GETTER_INTRINSIC &&
        callParentFqName == COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN -> {
        val lastFoundComposable = lastFoundComposable()
        if (lastFoundComposable != null) {
          irString(lastFoundComposable.name.asString())
        } else {
          irString("<unknown>")
        }
      }
      callFqName == COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_NAME_FQN_SETTER_INTRINSIC &&
        callParentFqName == COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN -> {
        val lastFoundComposable = lastFoundComposable()
        var result = false
        if (lastFoundComposable != null) {
          val userProvideName = expression.getValueArgument(0).cast<IrConst<String>>().value
          val prevKey = irTracee[DurableWritableSlices.DURABLE_FUNCTION_KEY, lastFoundComposable]!!
          val newKey = prevKey.copy(userProvideName = userProvideName)
          irTracee[DurableWritableSlices.DURABLE_FUNCTION_KEY, lastFoundComposable] = newKey
          result = true
        }
        irBoolean(result)
      }
      callFqName == COMPOSABLE_INVALIDATION_TRACK_TABLE_CURRENT_COMPOSABLE_KEY_NAME_FQN_GETTER_INTRINSIC &&
        callParentFqName == COMPOSABLE_INVALIDATION_TRACK_TABLE_FQN -> {
        val lastFoundComposable = lastFoundComposable()
        if (lastFoundComposable != null) {
          val key = irTracee[DurableWritableSlices.DURABLE_FUNCTION_KEY, lastFoundComposable]!!
          irString(key.keyName)
        } else {
          irString("<unknown>")
        }
      }
      else -> super.visitCall(expression)
    }
  }

  private fun lastFoundComposable(): IrSimpleFunction? =
    allScopes
      .fastLastOrNull { scope -> scope.irElement is IrSimpleFunction }
      ?.irElement
      ?.cast()
}
