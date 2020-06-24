package com.metallicus.protonsdk.model

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class ActionTrace(
	@SerializedName("trx_id") val trxId: String,
	@SerializedName("act")
	@Embedded(prefix = "act_") val act: ActionTraceAct
)