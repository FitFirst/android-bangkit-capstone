package com.example.capstoneapp.data.response

import com.google.gson.annotations.SerializedName

data class PostTrackerResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)
