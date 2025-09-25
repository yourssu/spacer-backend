package com.yourssu.spacer.spacehub.application.support.constants

object SlackConstants {
    const val WORKSPACE_LINK_MODAL_SUBMIT = "workspace_link_modal_submit"

    const val RESERVATION_CREATE_SPACE_SELECT = "reservation_create_space_select"
    const val RESERVATION_CREATE_MODAL_SUBMIT = "reservation_create_modal_submit"

    const val RESERVATION_READ_SPACE_SELECT = "reservation_read_space_select"
    const val RESERVATION_READ_MODAL_SUBMIT = "reservation_read_modal_submit"

    const val RESERVATION_DELETE_SPACE_SELECT = "reservation_delete_space_select"
    const val RESERVATION_DELETE_MODAL_SUBMIT = "reservation_delete_modal_submit"

    const val REGULAR_MEETING_CREATE_SPACE_SELECT = "regular_meeting_create_space_select"
    const val REGULAR_MEETING_CREATE_MODAL_SUBMIT = "regular_meeting_create_modal_submit"

    const val REGULAR_MEETING_READ_SPACE_SELECT = "regular_meeting_read_space_select"

    object BlockIds {
        const val EMAIL = "email_block"
        const val PASSWORD = "password_block"

        const val BOOKER_NAME = "booker_name_block"
        const val START_TIME = "start_time_block"
        const val END_TIME = "end_time_block"
        const val SPACE_PASSWORD = "space_password_block"
        const val PERSONAL_PASSWORD = "personal_password_block"

        const val RESERVATION_DATE = "date_block"
        const val RESERVATION_SELECT = "reservation_select_block"

        const val TEAM_NAME = "team_name_block"
        const val DAY_OF_WEEK = "day_of_week_block"
        const val START_DATE = "start_date_block"
        const val END_DATE = "end_date_block"
    }

    object ActionIds {
        const val EMAIL = "email_input"
        const val PASSWORD = "password_input"

        const val BOOKER_NAME = "booker_name_input"
        const val START_TIME = "start_time_input"
        const val END_TIME = "end_time_input"
        const val SPACE_PASSWORD = "space_password_input"
        const val PERSONAL_PASSWORD = "personal_password_input"

        const val RESERVATION_DATE = "date_input"
        const val RESERVATION_SELECT = "reservation_delete_select_input"

        const val TEAM_NAME = "team_name_input"
        const val DAY_OF_WEEK = "day_of_week_input"
        const val START_DATE = "start_date_input"
        const val END_DATE = "end_date_input"
    }

    object Keywords {
        const val EMAIL = "이메일"
        const val PASSWORD = "비밀번호"

        const val BOOKER_NAME = "예약자명"
        const val START_TIME = "시작 시간"
        const val END_TIME = "종료 시간"
        const val SPACE_PASSWORD = "공간 비밀번호"
        const val PERSONAL_PASSWORD = "개인 비밀번호"

        const val RESERVATION_DATE = "날짜"

        const val TEAM_NAME = "팀 이름"
        const val DAY_OF_WEEK = "요일"
        const val START_DATE = "시작일"
        const val END_DATE = "종료일"
    }
}
