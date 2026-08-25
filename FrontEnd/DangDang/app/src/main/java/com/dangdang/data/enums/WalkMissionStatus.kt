package com.dangdang.data.enums

import androidx.annotation.Keep

@Keep
enum class WalkMissionStatus {
    Loading,
    LoadingError,
    READY,
    IN_PROGRESS,
    COMPLETE,
    PARTIAL,
    EXPIRED
}