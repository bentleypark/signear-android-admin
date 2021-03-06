package com.sullivan.signearadmin.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ResponseCheckEmail(
    @SerializedName("isNext")
    val result: Boolean
)
