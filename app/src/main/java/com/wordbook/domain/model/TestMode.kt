package com.wordbook.domain.model

enum class TestMode(val displayName: String) {
    SEQUENTIAL("Sequential"),
    RANDOM("Random"),
    NEEDS_REVIEW_FIRST("Needs Review First"),
    SRS_DUE("SRS Due Cards")
}
