/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package simplezxing.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.swt.simplezxingdemo.R;

import simplezxing.assit.AmbientLightManager;
import simplezxing.assit.BeepManager;
import simplezxing.camera.CameraManager;
import simplezxing.view.ViewfinderView;

/**
 * @date 2016-11-18 9:07
 * @auther swt
 * @description modified
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener {

    public static final int REQ_CODE = 0xF0F0;
    public static final String KEY_NEED_BEEP = "NEED_BEEP";
    public static final boolean VALUE_BEEP = true; //default
    public static final boolean VALUE_NO_BEEP = false;
    public static final String KEY_NEED_VIBRATION = "NEED_VIBRATION";
    public static final boolean VALUE_VIBRATION = true;  //default
    public static final boolean VALUE_NO_VIBRATION = false;
    public static final String KEY_NEED_EXPOSURE = "NEED_EXPOSURE";
    public static final boolean VALUE_EXPOSURE = true;
    public static final boolean VALUE_NO_EXPOSURE = false; //default
    public static final String KEY_FLASHLIGHT_MODE = "FLASHLIGHT_MODE";
    public static final byte VALUE_FLASHLIGHT_AUTO = 2;
    public static final byte VALUE_FLASHLIGHT_ON = 1;
    public static final byte VALUE_FLASHLIGHT_OFF = 0;  //default
    public static final String KEY_ORIENTATION_MODE = "ORIENTATION_MODE";
    public static final byte VALUE_ORIENTATION_AUTO = 2;
    public static final byte VALUE_ORIENTATION_LANDSCAPE = 1;
    public static final byte VALUE_ORIENTATION_PORTRAIT = 0; //default
    public static final String EXTRA_SETTING_BUNDLE = "SETTING_BUNDLE";
    public static final String EXTRA_SCAN_RESULT = "SCAN_RESULT";
    private static final String TAG = CaptureActivity.class.getSimpleName();
    byte flashlightMode;
    byte orientationMode;
    boolean needBeep;
    boolean needVibration;
    boolean needExposure;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private BeepManager beepManager;
    private AmbientLightManager ambientLightManager;
    private MyOrientationDetector myOrientationDetector;
    private FrameLayout ll_root;
    private EditText etsearch;
    private ImageView ivdel;
    private TextView tvsearch;
    private LinearLayout llsearch;
    private LinearLayout llsh;
    private LinearLayout llljfh;

    ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
//        windowSetting();
        setContentView(R.layout.capture);
        initView();
        bundleSetting(getIntent().getBundleExtra(EXTRA_SETTING_BUNDLE));
        myOrientationDetector = new MyOrientationDetector(this);
        myOrientationDetector.setLastOrientation(getWindowManager().getDefaultDisplay().getRotation());
    }

    private void initView() {
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("扫一扫");
        ImageView btnLeft = (ImageView) findViewById(R.id.btnLeft);
        btnLeft.setVisibility(View.VISIBLE);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaptureActivity.this.finish();
            }
        });
        ll_root = (FrameLayout) findViewById(R.id.ll_root);
        etsearch = (EditText) findViewById(R.id.et_search);
        ivdel = (ImageView) findViewById(R.id.iv_del);
        tvsearch = (TextView) findViewById(R.id.tv_search);
        llsearch = (LinearLayout) findViewById(R.id.ll_search);
        llsh = (LinearLayout) findViewById(R.id.ll_sh);
        llljfh = (LinearLayout) findViewById(R.id.ll_ljfh);
        ll_root.setOnClickListener(this);
        tvsearch.setOnClickListener(this);
        llsh.setOnClickListener(this);
    }

    private void windowSetting() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void bundleSetting(Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        flashlightMode = bundle.getByte(KEY_FLASHLIGHT_MODE, VALUE_FLASHLIGHT_OFF);
        orientationMode = bundle.getByte(KEY_ORIENTATION_MODE, VALUE_ORIENTATION_PORTRAIT);
        needBeep = bundle.getBoolean(KEY_NEED_BEEP, VALUE_BEEP);
        needVibration = bundle.getBoolean(KEY_NEED_VIBRATION, VALUE_VIBRATION);
        needExposure = bundle.getBoolean(KEY_NEED_EXPOSURE, VALUE_NO_EXPOSURE);
        switch (orientationMode) {
            case VALUE_ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case VALUE_ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            default:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                break;
        }
        switch (flashlightMode) {
            case VALUE_FLASHLIGHT_AUTO:
                ambientLightManager = new AmbientLightManager(this);
                break;
        }
        beepManager = new BeepManager(this, needBeep, needVibration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (orientationMode == VALUE_ORIENTATION_AUTO) {
            myOrientationDetector.enable();
        }
        cameraManager = new CameraManager(getApplication(), needExposure);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        handler = null;
        beepManager.updatePrefs();
        if (ambientLightManager != null) {
            ambientLightManager.start(cameraManager);
        }
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }

    }

    @Override
    protected void onPause() {
        myOrientationDetector.disable();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        if (ambientLightManager != null) {
            ambientLightManager.stop();
        }
        beepManager.close();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
        if (flashlightMode == VALUE_FLASHLIGHT_ON) {
            if (cameraManager != null) {
                cameraManager.setTorch(true);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     */
    public void handleDecode(Result rawResult) {
        beepManager.playBeepSoundAndVibrate();
        returnResult(RESULT_OK, rawResult.getText());
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }
        } catch (Exception e) {
            Log.w(TAG, e);
            returnResult(RESULT_CANCELED, getString(R.string.msg_camera_framework_bug));
        }
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private void returnResult(int resultCode, String resultStr) {
        setResult(resultCode, new Intent().putExtra(EXTRA_SCAN_RESULT, resultStr));
        finish();
    }

    private void restartActivity() {
        onPause();
        onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_search:
                beepManager.playBeepSoundAndVibrate();
                String resultString = etsearch.getText().toString().trim();
                Log.i("swt", "^^^^^^^:" + resultString);
                if (resultString.equals("")) {
                    Toast.makeText(CaptureActivity.this, "请输入二维码", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString(EXTRA_SCAN_RESULT, resultString);
                    resultIntent.putExtras(bundle);
                    this.setResult(RESULT_OK, resultIntent);
                }
                CaptureActivity.this.finish();
                break;
            case R.id.ll_sh:
                llsh.setVisibility(View.GONE);
                llsearch.setVisibility(View.VISIBLE);
                break;

            case R.id.ll_root:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                break;
        }
    }

    private class MyOrientationDetector extends OrientationEventListener {

        private int lastOrientation = -1;

        MyOrientationDetector(Context context) {
            super(context);
        }

        void setLastOrientation(int rotation) {
            switch (rotation) {
                case Surface.ROTATION_90:
                    lastOrientation = 270;
                    break;
                case Surface.ROTATION_270:
                    lastOrientation = 90;
                    break;
                default:
                    lastOrientation = -1;
            }
        }

        @Override
        public void onOrientationChanged(int orientation) {
            Log.d(TAG, "orientation:" + orientation);
            if (orientation > 45 && orientation < 135) {
                orientation = 90;
            } else if (orientation > 225 && orientation < 315) {
                orientation = 270;
            } else {
                orientation = -1;
            }
            if ((orientation == 90 && lastOrientation == 270) || (orientation == 270 && lastOrientation == 90)) {
                Log.i(TAG, "orientation:" + orientation + "lastOrientation:" + lastOrientation);
                restartActivity();
                lastOrientation = orientation;
                Log.i(TAG, "SUCCESS");
            }
        }
    }
}
