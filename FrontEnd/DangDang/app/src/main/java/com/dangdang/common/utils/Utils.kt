package com.dangdang.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Patterns
import android.webkit.MimeTypeMap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.dangdang.data.enums.DividerPosition
import com.dangdang.data.model.ResponseErrorModel
import com.dangdang.data.model.community.CameraImage
import com.dangdang.ui.theme.DarkGreen
import com.dangdang.ui.theme.HotPink
import com.dangdang.ui.theme.Orange
import com.dangdang.ui.theme.PrimaryBlue
import com.dangdang.ui.theme.PrimaryPurple
import com.dangdang.ui.theme.ThinLineDp
import com.dangdang.ui.theme.notoSansKR
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale
import androidx.core.graphics.scale

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

fun Modifier.dividerWidthModifier(
    position: DividerPosition,
    size: Dp? = null,
) = this
    .then(
        if(position == DividerPosition.Horizontal){
            if(size != null){
                Modifier.width(size)
            }else{
                Modifier.fillMaxWidth()
            }
        }else{
            Modifier.width(ThinLineDp)
        }
    )


fun Modifier.dividerHeightModifier(
    position: DividerPosition,
    size: Dp? = null,
) = this
    .then(
        if(position == DividerPosition.Vertical){
            if(size != null){
                Modifier.height(size)
            }else{
                Modifier.fillMaxHeight()
            }
        }else{
            Modifier.height(ThinLineDp)
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

fun addComma(number: Float): String {
    val formatter = DecimalFormat("#,##0.0")
    return formatter.format(number)
}

fun getMeterToKm(distance: Float): Float{
    return distance / 1000f
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
    val mimeType = contentResolver.getType(uri)

    val extension = MimeTypeMap.getSingleton()
        .getExtensionFromMimeType(mimeType)
        ?.let { ".$it" }
        ?: ".jpg"

    val file = File.createTempFile(
        "upload_",
        extension,
        cacheDir
    )

    contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return file
}

fun File.toMultipart(): MultipartBody.Part {
    val mimeType = MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(
            extension.lowercase()
        )
        ?: "application/octet-stream"

    val requestBody = asRequestBody(
        mimeType.toMediaType()
    )

    return MultipartBody.Part.createFormData(
        "image",
        name,
        requestBody
    )
}

fun File.deleteSafely(): Boolean {
    return try {
        if (exists()) {
            delete()
        } else {
            true
        }
    } catch (e: Exception) {
        false
    }
}

fun Context.uriToResizedFile(
    uri: Uri,
    maxSize: Int = 512
): File {
    val bitmap = contentResolver.openInputStream(uri).use { inputStream ->
        BitmapFactory.decodeStream(inputStream)
    } ?: throw IOException("이미지를 불러올 수 없습니다.")

    val width = bitmap.width
    val height = bitmap.height

    // 512 이하라면 원본 크기 그대로 사용
    if (width <= maxSize && height <= maxSize) {
        val file = File.createTempFile(
            "upload_",
            ".jpg",
            cacheDir
        )

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                outputStream
            )
        }

        bitmap.recycle()

        return file
    }

    // 비율 유지하면서 최대 크기를 512로 조정
    val scale = minOf(
        maxSize.toFloat() / width,
        maxSize.toFloat() / height
    )

    val resizedWidth = (width * scale).toInt()
    val resizedHeight = (height * scale).toInt()

    val resizedBitmap = bitmap.scale(resizedWidth, resizedHeight)

    bitmap.recycle()

    val file = File.createTempFile(
        "upload_",
        ".jpg",
        cacheDir
    )

    FileOutputStream(file).use { outputStream ->
        resizedBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            90,
            outputStream
        )
    }

    resizedBitmap.recycle()

    return file
}

fun String.toRequestBody() =
    toRequestBody("text/plain".toMediaType())

fun Context.createImage(): CameraImage {

    val file = File.createTempFile(
        "camera_",
        ".jpg",
        cacheDir
    )

    val uri = FileProvider.getUriForFile(
        this,
        "$packageName.provider",
        file
    )

    return CameraImage(
        file = file,
        uri = uri
    )
}

//errormessage 가져오기
fun <T> getResponseError(response: Response<T>): ResponseErrorModel{
    val errorString = response.errorBody()?.string()
    val errorJson = Gson().fromJson(errorString, ResponseErrorModel::class.java)
    return errorJson
}

val GuageColorList = listOf(
    PrimaryBlue,
    DarkGreen,
    Orange,
    PrimaryPurple,
    HotPink
)