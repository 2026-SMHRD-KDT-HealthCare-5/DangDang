package com.dangdang.common.utils

import android.util.Patterns
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

//생년월일 유효성 확인
fun isValidBirthDate(dateStr: String): Boolean {
    if (dateStr.length != 10) return false

    val parsedDate = listOf(
        "uuuu.MM.dd",
        "uuuu-MM-dd"
    ).firstNotNullOfOrNull { pattern ->
        try {
            LocalDate.parse(
                dateStr,
                DateTimeFormatter.ofPattern(pattern)
                    .withResolverStyle(ResolverStyle.STRICT)
            )
        } catch (e: DateTimeParseException) {
            null
        }
    } ?: return false

    return !parsedDate.isAfter(LocalDate.now().minusYears(14))
}

//이메일 유효성 확인
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS
        .matcher(email)
        .matches()
}

const val PasswordMinLength = 8

fun isValidPassword(password: String): Boolean{
    return password.length >= PasswordMinLength
}

const val HeightMinValue = 50f
const val HeightMaxValue = 500f

//키 유효성 확인
fun isValidHeight(input: String): Boolean {
    val value = input.toFloatOrNull() ?: return false
    return value in HeightMinValue..HeightMaxValue
}

const val WeightMinValue = 20f
const val WeightMaxValue = 300f
//몸무게 유효성 확인
fun isValidWeight(input: String): Boolean {
    val value = input.toFloatOrNull() ?: return false
    return value in WeightMinValue..WeightMaxValue
}

const val Hba1cMinValue = 4f
const val Hba1cMaxValue = 15f
//당화혈색소 유효성 확인
fun isValidHbA1c(input: String): Boolean {
    val value = input.toFloatOrNull() ?: return false
    return value in Hba1cMinValue..Hba1cMaxValue
}

const val GlucoseMinValue = 80
const val GlucoseMaxValue = 300
// 식후 2시간 목표 혈당 유효성 확인
fun isValidPostPrandialGlucose(input: String): Boolean {
    val value = input.toIntOrNull() ?: return false
    return value in GlucoseMinValue..GlucoseMaxValue
}