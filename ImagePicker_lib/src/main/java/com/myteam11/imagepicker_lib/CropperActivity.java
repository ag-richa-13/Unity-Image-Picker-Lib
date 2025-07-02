package com.myteam11.imagepicker_lib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.unity3d.player.UnityPlayer;

import java.io.File;
import java.io.FileOutputStream;

public class CropperActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_URI = "extra_image_uri";
    public static final String EXTRA_GAME_OBJECT = "extra_game_object";
    public static final String EXTRA_SUCCESS_CALLBACK = "extra_success_callback";
    public static final String EXTRA_FAILURE_CALLBACK = "extra_failure_callback";

    private CropOverlayView overlay;
    private Bitmap bitmap;
    private String gameObj, successCb, failureCb;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        FrameLayout root = new FrameLayout(this);
        setContentView(root);

        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(iv);

        overlay = new CropOverlayView(this);
        root.addView(overlay);

        Button cancelBtn = new Button(this);
        cancelBtn.setText("Cancel");
        cancelBtn.setOnClickListener(v -> setResult(RESULT_CANCELED, null));

        Button doneBtn = new Button(this);
        doneBtn.setText("Done");
        doneBtn.setOnClickListener(v -> {
            Bitmap cropped = overlay.getCroppedBitmap();
            if (cropped != null) {
                try {
                    File file = new File(getCacheDir(), "crop_" + System.currentTimeMillis() + ".jpg");
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        cropped.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    }
                    UnityPlayer.UnitySendMessage(gameObj, successCb, file.getAbsolutePath());
                } catch (Exception e) {
                    UnityPlayer.UnitySendMessage(gameObj, failureCb, "Crop failed");
                }
            } else {
                UnityPlayer.UnitySendMessage(gameObj, failureCb, "No crop area");
            }
            finish();
        });

        int btnW = dpToPx(100), btnH = dpToPx(50), margin = dpToPx(16);
        FrameLayout.LayoutParams lp1 = new FrameLayout.LayoutParams(btnW, btnH);
        lp1.leftMargin = margin; lp1.bottomMargin = margin;
        lp1.gravity = android.view.Gravity.START | android.view.Gravity.BOTTOM;
        cancelBtn.setLayoutParams(lp1);

        FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(btnW, btnH);
        lp2.rightMargin = margin; lp2.bottomMargin = margin;
        lp2.gravity = android.view.Gravity.END | android.view.Gravity.BOTTOM;
        doneBtn.setLayoutParams(lp2);

        root.addView(cancelBtn);
        root.addView(doneBtn);

        Uri uri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
        gameObj = getIntent().getStringExtra(EXTRA_GAME_OBJECT);
        successCb = getIntent().getStringExtra(EXTRA_SUCCESS_CALLBACK);
        failureCb = getIntent().getStringExtra(EXTRA_FAILURE_CALLBACK);

        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            iv.setImageBitmap(bitmap);
            overlay.setBitmap(bitmap);
        } catch (Exception e) {
            UnityPlayer.UnitySendMessage(gameObj, failureCb, "Failed load");
            finish();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
