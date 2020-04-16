package com.proton.protonchain.model

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class ActionTraceAct(
	@SerializedName("account") val account: String,
	@SerializedName("name") val name: String,
	@SerializedName("authorization") val authorization: List<ActionTraceActAuthorization>,
	@SerializedName("data")
	@Embedded(prefix = "data_") val data: ActionTraceActData?
)