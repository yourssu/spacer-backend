package com.yourssu.spacer.spacehub.implement.domain.space

import com.yourssu.spacer.spacehub.implement.support.exception.InvalidCapacityException

class Capacity(
    val value: Int
) {

    init {
        if (value < 1) {
            throw InvalidCapacityException("수용인원은 1명 이상이어야 합니다.")
        }
    }
}
