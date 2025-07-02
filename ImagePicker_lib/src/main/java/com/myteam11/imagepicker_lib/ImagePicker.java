package com.myteam11.imagepicker_lib;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.unity3d.player.UnityPlayer;

public class ImagePicker {

    private static final String TAG = "ImagePickerLib";

    public static void pickImageFromGallery(String gameObject, String successCallback, String failureCallback) {
        Log.d(TAG, "Attempting to pick image from Gallery");
        startPickerActivity(ImagePickerActivity.MODE_GALLERY, gameObject, successCallback, failureCallback);
    }

    public static void pickImageFromCamera(String gameObject, String successCallback, String failureCallback) {
        Log.d(TAG, "Attempting to pick image from Camera");
        startPickerActivity(ImagePickerActivity.MODE_CAMERA, gameObject, successCallback, failureCallback);
    }

    private static void startPickerActivity(String mode, String gameObject, String successCallback, String failureCallback) {
        Activity currentActivity = UnityPlayer.currentActivity;
        if (currentActivity == null) {
            Log.e(TAG, "Could not get current Unity Activity.");
            UnityPlayer.UnitySendMessage(gameObject, failureCallback, "Could not get current Unity Activity.");
            return;
        }

        Intent intent = new Intent(currentActivity, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.EXTRA_MODE, mode);
        intent.putExtra(ImagePickerActivity.EXTRA_GAME_OBJECT, gameObject);
        intent.putExtra(ImagePickerActivity.EXTRA_SUCCESS_CALLBACK, successCallback);
        intent.putExtra(ImagePickerActivity.EXTRA_FAILURE_CALLBACK, failureCallback);

        currentActivity.startActivity(intent);
    }
}
