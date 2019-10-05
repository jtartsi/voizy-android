package com.voizy.android.middleware.firebase.models

data class FirestoreVoizySearchRequest(
    val defaultOperator: SearchOperator = SearchOperator.AND,
    val searchKeyword: String = "",
    val size: Int = 50,
    val page: Int = 0
) {

    enum class SearchOperator(var value: String) {
        AND("AND"),
        OR("OR")
    }
}