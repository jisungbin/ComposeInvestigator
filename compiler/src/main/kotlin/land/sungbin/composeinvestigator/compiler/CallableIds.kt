// Copyright 2025 Ji Sungbin
// SPDX-License-Identifier: Apache-2.0
package land.sungbin.composeinvestigator.compiler

import androidx.compose.compiler.plugins.kotlin.ComposeCallableIds
import androidx.compose.compiler.plugins.kotlin.ComposeClassIds
import org.jetbrains.kotlin.builtins.StandardNames.BUILT_INS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.builtins.StandardNames.COLLECTIONS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.builtins.StandardNames.HASHCODE_NAME
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier

public object StandardCallableIds {
  public val mutableListOf: CallableId = CallableId(COLLECTIONS_PACKAGE_FQ_NAME, identifier("mutableListOf"))
  public val mutableListAdd: CallableId = CallableId(COLLECTIONS_PACKAGE_FQ_NAME, FqName("MutableList"), identifier("add"))

  public val hashCode: CallableId = CallableId(BUILT_INS_PACKAGE_FQ_NAME, HASHCODE_NAME)
}

@Suppress("UnusedReceiverParameter")
public val ComposeCallableIds.rememberSaveable: CallableId
  get() = CallableId(FqName("androidx.compose.runtime.saveable"), identifier("rememberSaveable"))

@Suppress("UnusedReceiverParameter")
public val ComposeCallableIds.skipToGroupEnd: CallableId
  get() = CallableId(ComposeClassIds.Composer, identifier("skipToGroupEnd"))

public object InvestigatorCallableIds {
  public val getCurrentComposableName: CallableId =
    CallableId(InvestigatorClassIds.ComposeInvestigator, InvestigatorNames.getCurrentComposableName)
}
