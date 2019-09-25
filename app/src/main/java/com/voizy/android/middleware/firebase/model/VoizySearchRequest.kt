package com.voizy.android.middleware.firebase.model

data class VoizySearchRequest(
    val defaultOperator: SearchOperator = SearchOperator.AND,
    val searchKeyword: String = "",
    val orderBy: VoizyField = VoizyField.CreatedAt,
    val sort: Sort = Sort.Descending,
    val size: Int = 20
) {

    enum class SearchOperator(var value: String) {
        AND("AND"),
        OR("OR")
    }

    enum class VoizyField(var value: String) {
        Name("name"),
        CreatedAt("createdAt")
    }

    enum class Sort(var value: String) {
        Ascending("ASC"),
        Descending("DESC")
    }
} 