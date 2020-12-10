package com.rain.arcast.ui.camera

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rain.arcast.R
import java.io.File
import java.util.*

class CameraFragment : Fragment() {

    private val MAX_PREVIEW_WIDTH = 1280
    private val MAX_PREVIEW_HEIGHT = 720

    private lateinit var cameraViewModel: CameraViewModel
    private lateinit var previewTextureView: TextureView
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader

    private lateinit var btnCapture: Button

    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "Camera device opened.")
            cameraDevice = camera
            previewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, "Camera device disconnected.")
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.d(TAG, "Camera device error!")
            this@CameraFragment.activity?.finish()
        }

    }

    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private val cameraManager by lazy {
        activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 101
        private val TAG = CameraFragment::class.qualifiedName

        @JvmStatic
        fun newInstance() = CameraFragment()
    }

    private val surfaceListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(p0: SurfaceTexture, width: Int, height: Int) {
            Log.d(TAG, "TextureSurface | Width: $width ,Height: $height")
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
        }

        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean = true

        override fun onSurfaceTextureUpdated(p0: SurfaceTexture) = Unit
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        cameraViewModel =
            ViewModelProvider(this).get(CameraViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_camera, container, false)

        previewTextureView = root.findViewById(R.id.previewTextureView)
        btnCapture = root.findViewById(R.id.btn_capture)
        //TODO: btnCapture disable until I fix capturing, for now preview is all.
        //btnCapture.setOnClickListener { capturePictureSession() }
        btnCapture.isEnabled = false

        return root
    }

    override fun onResume() {
        super.onResume()

        startBackgroundThread()
        if (previewTextureView.isAvailable)
            openCamera()
        else
            previewTextureView.surfaceTextureListener = surfaceListener
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun openCamera() {
        setupPerm()
        connectCamera()
    }

    @SuppressLint("MissingPermission")
    private fun connectCamera() {
        val devId = cameraId(CameraCharacteristics.LENS_FACING_BACK)
        Log.d(TAG, "deviceID: $devId")
        try {
            cameraManager.openCamera(devId, deviceStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            Log.e(TAG, "Camera device interrupted while opened! ${e.toString()}")
        }
    }

    private fun cameraId(lens: Int): String {
        var devId = listOf<String>()
        try {
            val cameraIdList = cameraManager.cameraIdList
            devId = cameraIdList.filter {
                lens == cameraCharacteristics(
                    it,
                    CameraCharacteristics.LENS_FACING
                )
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
        return devId[0]
    }

    private fun previewSession() {
        val surfaceTexture = previewTextureView.surfaceTexture
        surfaceTexture!!.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT)
        val surface = Surface(surfaceTexture)

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice.createCaptureSession(
            Arrays.asList(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureRequestBuilder.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                    captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Capture session creation failed!")
                }

            }, null
        )
    }

    private fun capturePictureSession() {
        val surfaceTexture = previewTextureView.surfaceTexture
        surfaceTexture!!.setDefaultBufferSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT)
        val surface = Surface(surfaceTexture)

        imageReader =
            ImageReader.newInstance(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, ImageFormat.JPEG, 2)

        captureRequestBuilder =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder.addTarget(surface)
        cameraDevice.createCaptureSession(
            Arrays.asList(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureRequestBuilder.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                    captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                    captureSession.capture(captureRequestBuilder.build(), null, null)
                    imageReader.setOnImageAvailableListener({
                        upload(requireContext(), imgToByte(it.acquireLatestImage()), "Test")
                    }, null)

                    Log.v(TAG, "Picture captured!")
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Capture session creation failed!")
                }

            }, null
        )
    }

    fun imgToByte(image: Image): ByteArray {
        val buffer = image.planes[0].buffer
        val byteArray = ByteArray(buffer.capacity())
        buffer.get(byteArray)

        return byteArray
    }

    fun upload(context: Context, byteData: ByteArray, fileName: String) {
        val imageRef: StorageReference =
            FirebaseStorage.getInstance().reference.child("images" + File.separator + fileName)
        //TODO: Update metadata with Firebase.getInstance().getUID()
        imageRef.putBytes(byteData)
            .addOnSuccessListener { task ->
                Toast.makeText(context, "Uploaded!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { task ->
                Toast.makeText(context, "Failed! Error: " + task.message, Toast.LENGTH_SHORT).show()
            }
            .addOnProgressListener { task ->
                val progress = (100.0 * task.bytesTransferred) / task.totalByteCount
                Log.i("UPLOAD", progress.toString())
            }
    }

    private fun closeCamera() {
        if (this::captureSession.isInitialized)
            captureSession.close()
        if (this::cameraDevice.isInitialized)
            cameraDevice.close()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera2").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun <T> cameraCharacteristics(cameraID: String, key: CameraCharacteristics.Key<T>): T {
        val characteristics = cameraManager.getCameraCharacteristics(cameraID)
        return when (key) {
            CameraCharacteristics.LENS_FACING -> characteristics.get(key)!!
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)!!
            else -> throw IllegalArgumentException("Key not recognized!")
        }
    }

    private fun setupPerm() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to use camera denied")
            /*
            * ActivityCompat.shouldShowRequestPermissionRationale(context, permission) should
            * return true if the user has previously denied request, and false if not*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    android.Manifest.permission.CAMERA
                )
            ) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(
                    "Permission to access the camera is " +
                            "required to upload pictures!"
                )

                builder.setPositiveButton("OK") { dialog, id ->
                    Log.i(TAG, "Clicked Okay")
                    makeRequest()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                makeRequest()
            }
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user.")
                } else {
                    Log.i(TAG, "Permission has been granted by user.")
                    connectCamera()
                }
            }
        }
    }


}