package com.dangdang.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.core.content.FileProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.dangdang.data.enums.LayoutSize
import androidx.core.net.toUri
import com.dangdang.Application.Companion.InquiryEmail
import com.dangdang.ui.theme.DarkGreen
import com.dangdang.ui.theme.HotPink
import com.dangdang.ui.theme.Orange
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.PrimaryPurple
import com.dangdang.ui.theme.notoSansKR
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

//화면마다 공통으로 사용하는 modifier
fun Modifier.screen() = this
    .fillMaxSize()
    .background(Color.White)
    .systemBarsPadding()

fun Modifier.mainScreen() = this
    .fillMaxSize()
    .background(Color.White)

fun Modifier.componentWidthModifier(
    fixWidth: Dp? = null,
    sizeType: LayoutSize = LayoutSize.DefaultSize,
) = this
    .then(
        when(sizeType){
            LayoutSize.DefaultSize ->
                Modifier
            LayoutSize.FixSize ->
                if(fixWidth != null){
                    Modifier.width(fixWidth)
                } else {
                    Modifier
                }
            LayoutSize.FillMaxSize ->
                Modifier.fillMaxWidth()
        }
    )

val TextStyle.regular: TextStyle
    get() = copy(
        fontFamily = notoSansKR,
        fontWeight = FontWeight.Normal
    )

val TextStyle.medium: TextStyle
    get() = copy(
        fontFamily = notoSansKR,
        fontWeight = FontWeight.Medium
    )

val TextStyle.bold: TextStyle
    get() = copy(
        fontFamily = notoSansKR,
        fontWeight = FontWeight.Bold
    )

fun addComma(number: Int): String {
    val formatter = DecimalFormat("#,###")
    val formattedNumber = formatter.format(number)
    return formattedNumber
}

fun navigateBottomTab(
    navController: NavHostController,
    route: String
) {
    navController.navigate(route) {

        popUpTo(
            navController.graph.findStartDestination().id
        ) {
            saveState = true
        }

        launchSingleTop = true
        restoreState = true
    }
}

fun sendMail(context: Context){
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(InquiryEmail))
    }

    context.startActivity(intent)
}

fun Context.uriToFile(uri: Uri): File {

    val input = contentResolver.openInputStream(uri)

    val file = File.createTempFile(
        "upload",
        ".png",
        cacheDir
    )

    file.outputStream().use {
        input?.copyTo(it)
    }

    return file
}

fun File.toMultipart(): MultipartBody.Part {

    val requestBody =
        asRequestBody("image/*".toMediaType())

    return MultipartBody.Part.createFormData(
        "image",
        name,
        requestBody
    )
}

fun String.toRequestBody() =
    toRequestBody("text/plain".toMediaType())

fun Context.createImageUri(): Uri {

    val file = File.createTempFile(
        "camera",
        ".jpg",
        cacheDir
    )

    return FileProvider.getUriForFile(
        this,
        "$packageName.provider",
        file
    )
}

//생년월일 유효성 확인
fun isValidBirthDate(dateStr: String): Boolean {
    if (dateStr.length != 10) return false

    return try {
        val formatter = DateTimeFormatter.ofPattern("uuuu.MM.dd")
            .withResolverStyle(ResolverStyle.STRICT)

        val parsedDate = LocalDate.parse(dateStr, formatter)

        !parsedDate.isAfter(LocalDate.now()) &&
                !parsedDate.isAfter(LocalDate.now().minusYears(14))
    } catch (e: Exception) {
        false
    }
}

//이메일 유효성 확인
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS
        .matcher(email)
        .matches()
}

fun isValidPassword(password: String): Boolean{
    return password.length >= 8
}

//키 유효성 확인
fun isValidHeight(input: String): Boolean {
    val value = input.toIntOrNull() ?: return false
    return value in 50..500
}

//몸무게 유효성 확인
fun isValidWeight(input: String): Boolean {
    val value = input.toFloatOrNull() ?: return false
    return value in 20f..300f
}

//당화혈색소 유효성 확인
fun isValidHbA1c(input: String): Boolean {
    val value = input.toFloatOrNull() ?: return false
    return value in 4f..15f
}

// 식후 2시간 목표 혈당 유효성 확인
fun isValidPostPrandialGlucose(input: String): Boolean {
    val value = input.toIntOrNull() ?: return false
    return value in 80..300
}

val GuageColorList = listOf(
    PrimaryBlue,
    DarkGreen,
    Orange,
    PrimaryPurple,
    HotPink
)