package com.yourssu.spacer.spacehub.implement.support.security.password

enum class PasswordFormat(val regex: String, val errorMessage: String) {

    ORGANIZATION_PASSWORD(
        "^(?=(.*[a-zA-Z]))(?=(.*\\d))[a-zA-Z\\d!@#\$%^&*()_+=-]{8,}\$",
        "비밀번호는 영어+숫자 8글자 이상이어야 합니다."
    ),

    PERSONAL_RESERVATION_PASSWORD(
        "^.{4,}\$",
        "비밀번호는 대소문자, 숫자 무관 4글자 이상이어야 합니다."
    )
}
