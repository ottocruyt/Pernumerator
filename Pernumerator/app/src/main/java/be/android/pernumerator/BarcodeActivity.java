package be.android.pernumerator;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;


public class BarcodeActivity extends AppCompatActivity {

    private CameraSource mCameraSource = null;
    private SurfaceView mCameraView = null;
    private TextView mBarcodeInfo = null;
    private BarcodeDetector mBarcodeDetector = null;
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        mCameraView = findViewById(R.id.camera_view);
        mBarcodeInfo = findViewById(R.id.barcode_textView);
        mBarcodeDetector = new BarcodeDetector.Builder(this) .setBarcodeFormats(Barcode.ALL_FORMATS) .build();
        mCameraSource = new CameraSource.Builder(this, mBarcodeDetector) .setAutoFocusEnabled(true) .setRequestedPreviewSize(640, 480) .build();
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override public void surfaceCreated(SurfaceHolder holder) {
                int rc = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (SecurityException se) {
                        Log.e("CAMERA SOURCE", se.getMessage());
                    } catch (IOException ie) {
                        Log.e("CAMERA SOURCE", ie.getMessage());
                    }
                } else {
                    requestCameraPermission();
                }


            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });


        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    mBarcodeInfo.post(new Runnable() {
                        // Use the post method of the TextView
                        public void run() {
                            Barcode barcode = (Barcode)barcodes.valueAt(0);
                            mBarcodeInfo.setText(barcode.displayValue);
                        }
                    });
                }

            }
        });

    }
    private void requestCameraPermission() {
        Log.w("CAMERA SOURCE", "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }
    }
}
