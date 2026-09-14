package com.ludoven.adbtool.util

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.dialogs.openFilePicker

object FileUtils {
    suspend fun selectApkFile(): String? {
        val file = FileKit.openFilePicker(type = FileKitType.File(listOf("apk")))
        return file?.absolutePath()
    }


    suspend fun selectFolder(): String? {
        val openDirectoryPicker = FileKit.openDirectoryPicker()
        return openDirectoryPicker?.absolutePath()
    }


    suspend fun selectFile(): String? {
        val file = FileKit.openFilePicker(type = FileKitType.File(emptyList()))
        return file?.absolutePath()
    }
}
