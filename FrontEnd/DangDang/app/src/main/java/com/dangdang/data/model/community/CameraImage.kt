package com.dangdang.data.model.community

import android.net.Uri
import androidx.annotation.Keep
import java.io.File

@Keep
data class CameraImage(
    val file: File,
    val uri: Uri
)