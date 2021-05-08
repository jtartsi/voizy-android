package com.voizy.android.middleware.firebase.models

data class FirestoreVoizySearchRequest(
    val defaultOperator: SearchOperator = SearchOperator.AND,
    val searchKeyword: String = "",
    val sortOrder: SortOrder = SortOrder.TOP,
    val size: Int = 50,
    val from: Int = 0,
    val locale: String = "",
    val localeLang: String = "",
    val localeCountry: String = ""
) {

    enum class SortOrder(var value: String) {
        TOP("TOP"),
        NEW("NEW")
    }

    enum class SearchOperator(var value: String) {
        AND("AND"),
        OR("OR")
    }
}