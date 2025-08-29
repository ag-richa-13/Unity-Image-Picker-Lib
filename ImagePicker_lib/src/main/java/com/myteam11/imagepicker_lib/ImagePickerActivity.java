package com.myteam11.imagepicker_lib;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.unity3d.player.UnityPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePickerActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_GAME_OBJECT = "extra_game_object";
    public static final String EXTRA_SUCCESS_CALLBACK = "extra_success_callback";
    public static final String EXTRA_FAILURE_CALLBACK = "extra_failure_callback";
    public static final String MODE_GALLERY = "mode_gallery";
    public static final String MODE_CAMERA = "mode_camera";
    private static final String TAG = "ImagePickerActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private String unityGameObject, unitySuccessCallback, unityFailureCallback;
    private Uri cameraImageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private String pendingMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        pendingMode = intent.getStringExtra(EXTRA_MODE);
        unityGameObject = intent.getStringExtra(EXTRA_GAME_OBJECT);
        unitySuccessCallback = intent.getStringExtra(EXTRA_SUCCESS_CALLBACK);
        unityFailureCallback = intent.getStringExtra(EXTRA_FAILURE_CALLBACK);

        registerLaunchers();

        if (savedInstanceState == null) {
            if (MODE_CAMERA.equals(pendingMode)) {
                checkAndRequestCameraPermission();
            } else if (MODE_GALLERY.equals(pendingMode)) {
                launchGallery();
            } else {
                fail("Invalid mode specified.");
                finish();
            }
        }
    }

    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                fail("Camera permission denied.");
                finish();
            }
        }
    }

    private void registerLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            String path = copyUriToCache(imageUri);
                            if (path != null) launchCropper(path);
                            else fail("Failed to copy gallery image to cache.");
                        } else fail("Failed to retrieve image from gallery.");
                    } else fail("Gallery selection cancelled or failed.");
                    finish();
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), success -> {
                    if (success) {
                        String path = copyUriToCache(cameraImageUri);
                        if (path != null) launchCropper(path);
                        else fail("Failed to copy captured image to cache.");
                    } else fail("Camera capture cancelled or failed.");
                    finish();
                });
    }

    private void launchGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        galleryLauncher.launch(i);
    }

    private void launchCamera() {
        cameraImageUri = createImageFileUri();
        if (cameraImageUri != null) {
            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cam.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cam.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraLauncher.launch(cameraImageUri);
        } else {
            fail("Could not create file for camera image.");
            finish();
        }
    }

    private Uri createImageFileUri() {
        try {
            File file = createImageFile();
            return FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
            return null;
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fn = "IMG_" + timeStamp + "_";
        File dir = new File(getExternalCacheDir(), "images");
        if (!dir.exists()) dir.mkdirs();
        return File.createTempFile(fn, ".jpg", dir);
    }

    private String copyUriToCache(Uri src) {
        try (InputStream in = getContentResolver().openInputStream(src)) {
            if (in == null) return null;
            File dst = createImageFile();
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }

            if (dst.length() > 5 * 1024 * 1024) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                android.graphics.Bitmap bmp = BitmapFactory.decodeFile(dst.getAbsolutePath(), options);
                File cmp = createImageFile();
                int q = 90;
                while (q >= 30) {
                    try (FileOutputStream fos = new FileOutputStream(cmp)) {
                        bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, q, fos);
                    }
                    if (cmp.length() <= 5 * 1024 * 1024) return cmp.getAbsolutePath();
                    q -= 10;
                }
                return cmp.getAbsolutePath();
            }

            return dst.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "copyUriToCache failed", e);
            return null;
        }
    }

    private void launchCropper(String path) {
        Intent ci = new Intent(this, CropperActivity.class);
        ci.putExtra(CropperActivity.EXTRA_IMAGE_URI, Uri.fromFile(new File(path)));
        ci.putExtra(CropperActivity.EXTRA_GAME_OBJECT, unityGameObject);
        ci.putExtra(CropperActivity.EXTRA_SUCCESS_CALLBACK, unitySuccessCallback);
        ci.putExtra(CropperActivity.EXTRA_FAILURE_CALLBACK, unityFailureCallback);
        startActivity(ci);
    }

    private void fail(String msg) {
        Log.e(TAG, "Failure: " + msg);
        UnityPlayer.UnitySendMessage(unityGameObject, unityFailureCallback, msg);
    }
}
