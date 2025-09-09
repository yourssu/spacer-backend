package com.yourssu.spacer.spacehub.application.domain.slack

import com.slack.api.bolt.context.builtin.ViewSubmissionContext

/**
 * 모달 제출 후, 요청한 사용자에게만 보이는 임시 메시지(ephemeral message)를 보냅니다.
 */
fun ViewSubmissionContext.postEphemeralSuccess(message: String) {
    this.client().chatPostEphemeral {
        it.channel(this.requestUserId) // DM 채널 ID (사용자 ID와 동일)
            .user(this.requestUserId)      // 메시지를 볼 수 있는 사용자
            .text("✅ $message")
    }
}

fun ViewSubmissionContext.postEphemeralError(message: String) {
    this.client().chatPostEphemeral {
        it.channel(this.requestUserId)
            .user(this.requestUserId)
            .text("❌ $message")
    }
}