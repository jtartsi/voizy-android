package com.voizy.android.middleware.firebase.model

data class VoizySearchRequest(
    val defaultOperator: SearchOperator = SearchOperator.AND,
    val searchKeyword: String = "",
    val size: Int = 20
) {

    enum class SearchOperator(var value: String) {
        AND("AND"),
        OR("OR")
    }
}