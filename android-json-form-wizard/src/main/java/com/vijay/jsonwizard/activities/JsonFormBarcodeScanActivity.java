package com.vijay.jsonwizard.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.utils.barcode.JsonFormCameraSourcePreview;

import java.io.IOException;

public class JsonFormBarcodeScanActivity extends Activity implements Detector.Processor<Barcode> {
    private CameraSource cameraSource;
    private JsonFormCameraSourcePreview jsonFormCameraSourcePreview;

    private String TAG = JsonFormBarcodeScanActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.json_form_activity_scan_barcode);
        jsonFormCameraSourcePreview = findViewById(R.id.preview);
        createCameraSource();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison to
     * other detection examples to enable the barcode detector to detect small barcodes at long
     * distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        Context context = getApplicationContext();
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        barcodeDetector.setProcessor(this);

        if (!barcodeDetector.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true)
                .setRequestedFps(45.0f);

        cameraSource = builder.build();
    }

    @Override
    public void release() {
        //Todo
    }

    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        final SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
        if (barcodeSparseArray.size() > 0) {
            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
            assert vibrator != null;
            vibrator.vibrate(100);
            closeBarcodeActivity(barcodeSparseArray);
        }
    }

    public void closeBarcodeActivity(SparseArray<Barcode> sparseArray) {
        Intent intent = new Intent();
        if (sparseArray != null) {
            intent.putExtra(JsonFormConstants.BARCODE_CONSTANTS.BARCODE_KEY, sparseArray.valueAt(0));
        }
        setResult(RESULT_OK, intent);
        finish();

    }


    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    public void startCameraSource() throws SecurityException {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog errorDialog =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, JsonFormConstants.BARCODE_CONSTANTS.RC_HANDLE_GMS);
            errorDialog.show();
        }

        if (cameraSource != null) {
            try {
                jsonFormCameraSourcePreview.start(cameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (jsonFormCameraSourcePreview != null) {
            jsonFormCameraSourcePreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (jsonFormCameraSourcePreview != null) {
            jsonFormCameraSourcePreview.release();
        }
    }
}
