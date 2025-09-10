//
// Copyright 2025 LY Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.linecorp.brushkit.sample.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.IOException

/**
 * A utility class to save a bitmap to the device's gallery using MediaStore.
 */
internal class MediaStoreHelper(private val context: Context) {
    /**
     * Saves a bitmap to the device's gallery using MediaStore.
     *
     * @param bitmap The bitmap to save
     * @return true if the save was successful, false otherwise
     */
    fun saveBitmapToGallery(bitmap: Bitmap): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "brush_drawing_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val imageUri = uri ?: return false
        try {
            contentResolver
                .openOutputStream(imageUri)
                ?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(imageUri, contentValues, null, null)
            }
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Error saving bitmap: ${e.message}")
            return false
        }
    }

    companion object {
        private const val TAG = "MediaStoreHelper"
        private const val MIME_TYPE = "image/png"
    }
}
