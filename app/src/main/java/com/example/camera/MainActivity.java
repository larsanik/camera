package com.example.camera;
import android.Manifest;
import android.content.Context;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
//import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
//import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
//import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
//import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

/* //Это осталось от получения списка устройсв
    public static final String LOG_TAG = "myLogs";
    String[] myCameras = null;
    private CameraManager mCameraManager    = null;
*/
    public static final String LOG_TAG = "myLogs"; //лог

    CameraService[] myCameras = null;

    private CameraManager mCameraManager    = null;
    private final int CAMERA1   = 0;
    private final int CAMERA2   = 1;

    private Button mButtonOpenCamera1 = null;
    private Button mButtonOpenCamera2 = null;
    private Button mButtonToMakeShot = null;
    private TextureView mImageView = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Установка прав на камеру
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||
                (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        )
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{

            // Получение списка камер с устройства

            myCameras = new CameraService[mCameraManager.getCameraIdList().length];

            for (String cameraID : mCameraManager.getCameraIdList()) {
                Log.i(LOG_TAG, "cameraID: "+cameraID);
                int id = Integer.parseInt(cameraID);

                // создаем обработчик для камеры
                myCameras[id] = new CameraService(mCameraManager,cameraID);
            }
        }
        catch(CameraAccessException e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }

/*
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            // Получение списка камер с устройства

            myCameras = new String[mCameraManager.getCameraIdList().length];

            // выводим информацию по камере
            for (String cameraID : mCameraManager.getCameraIdList()) {
                Log.i(LOG_TAG, "cameraID: "+cameraID);
                int id = Integer.parseInt(cameraID);

                // Получениe характеристик камеры
                CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(cameraID);
                // Получения списка выходного формата, который поддерживает камера
                StreamConfigurationMap configurationMap =
                        cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                //  Определение какая камера куда смотрит
                int Faceing = cc.get(CameraCharacteristics.LENS_FACING);

                if (Faceing ==  CameraCharacteristics.LENS_FACING_FRONT)
                {
                    Log.i(LOG_TAG,"Camera with ID: "+cameraID +  "  is FRONT CAMERA  ");
                }

                if (Faceing ==  CameraCharacteristics.LENS_FACING_BACK)
                {
                    Log.i(LOG_TAG,"Camera with: ID "+cameraID +  " is BACK CAMERA  ");
                }


                // Получения списка разрешений которые поддерживаются для формата jpeg
                Size[] sizesJPEG = configurationMap.getOutputSizes(ImageFormat.JPEG);

                if (sizesJPEG != null) {
                    for (Size item:sizesJPEG) {
                        Log.i(LOG_TAG, "w:"+item.getWidth()+" h:"+item.getHeight());
                    }
                }  else {
                    Log.i(LOG_TAG, "camera don`t support JPEG");
                }
            }
        }
        catch(CameraAccessException e){
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        }*/

        mButtonOpenCamera1 =  findViewById(R.id.button1);
        mButtonOpenCamera2 =  findViewById(R.id.button2);
        mButtonToMakeShot =findViewById(R.id.button3);
        mImageView = findViewById(R.id.textureView);

        mButtonOpenCamera1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myCameras[CAMERA2].isOpen()) myCameras[CAMERA2].closeCamera();
                if (myCameras[CAMERA1] != null) {
                    if (!myCameras[CAMERA1].isOpen()) myCameras[CAMERA1].openCamera();
                }
            }
        });

        mButtonOpenCamera2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myCameras[CAMERA1].isOpen()) myCameras[CAMERA1].closeCamera();
                if (myCameras[CAMERA2] != null) {
                    if (!myCameras[CAMERA2].isOpen()) myCameras[CAMERA2].openCamera();
                }
            }
        });


        mButtonToMakeShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                // тут пока пусто

            }
        });
    }

    public class CameraService {

        private String mCameraID;
        private CameraDevice mCameraDevice = null;
        private CameraCaptureSession mCaptureSession;

        public CameraService(CameraManager cameraManager, String cameraID) {

            mCameraManager = cameraManager;
            mCameraID = cameraID;
        }

        public boolean isOpen() {

            if (mCameraDevice == null) {
                return false;
            } else {
                return true;
            }
        }


        public void openCamera() {
            try {
                if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    mCameraManager.openCamera(mCameraID,mCameraCallback,null);
                }
            }
            catch (CameraAccessException e) {
                Log.i(LOG_TAG,e.getMessage());
            }
        }


        public void closeCamera() {

            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }


        private CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {

            @Override
            public void onOpened(CameraDevice camera) {
                mCameraDevice = camera;
                Log.i(LOG_TAG, "Open camera  with id:"+mCameraDevice.getId());

                createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                mCameraDevice.close();

                Log.i(LOG_TAG, "disconnect camera  with id:"+mCameraDevice.getId());
                mCameraDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                Log.i(LOG_TAG, "error! camera id:"+camera.getId()+" error:"+error);
            }
        };



        private void createCameraPreviewSession() {

            SurfaceTexture texture = mImageView.getSurfaceTexture();
            if (myCameras[CAMERA1].isOpen()) texture.setDefaultBufferSize(1920,1080);
            if (myCameras[CAMERA2].isOpen()) texture.setDefaultBufferSize(1280,960);
            Surface surface = new Surface(texture);

            try {
                final CaptureRequest.Builder builder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

                builder.addTarget(surface);
                mCameraDevice.createCaptureSession(Arrays.asList(surface),
                        new CameraCaptureSession.StateCallback() {

                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                mCaptureSession = session;
                                try {
                                    mCaptureSession.setRepeatingRequest(builder.build(),null,null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) { }}, null );
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }






}
