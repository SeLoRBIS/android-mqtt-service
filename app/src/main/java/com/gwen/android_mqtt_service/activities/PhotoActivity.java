package com.gwen.android_mqtt_service.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gwen.android_mqtt_service.R;
import com.gwen.android_mqtt_service.constants.Constants;
import com.gwen.android_mqtt_service.converters.MqttConverter;
import com.gwen.android_mqtt_service.dto.NodeRedMsg;
import com.gwen.android_mqtt_service.dto.PayloadMqttIn;
import com.gwen.android_mqtt_service.dto.PayloadMqttOutMatrix;
import com.gwen.android_mqtt_service.services.MQTTService;
import com.gwen.android_mqtt_service.utils.AssetsPropertyReader;
import com.gwen.android_mqtt_service.utils.CPreview;
import com.gwen.android_mqtt_service.utils.NodeRedApiEndpointInterface;
import com.gwen.android_mqtt_service.utils.Utils;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by gwendal.charles on 25/10/2016.
 */
@SuppressWarnings("deprecation")
public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = PhotoActivity.class.getName();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Camera mCamera;
    private CPreview mCameraPreview;
    private PayloadMqttIn mqtt_message;
    private String mCurrentPhotoPath;
    private CountDownTimer photoTimer;

    /**
     * Chargement de l'activite
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mqtt_message = (PayloadMqttIn) getIntent().getSerializableExtra(Constants.MQTT_MESSAGE);

        setContentView(R.layout.cpreview);

        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        mCameraPreview = new CPreview(this, mCamera);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                mCamera.takePicture(shutterCallback, null, jpegCallback);
            }
        });
        //dispatchTakePictureIntent();
    }

    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return camera
     */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            // TODO Verifier que le device possède une camera front
            if (mqtt_message.is_selfie()){
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return camera;
    }

    /** Callback shutterCallback */
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback()
    {
        public void onShutter(){
            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
            Log.d(TAG, "Callback Shutter");
        }
    };

    /** Callback rawCallback */
    Camera.PictureCallback rawCallback = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera camera)
        {
            Log.d(TAG, "Callback Raw");
        }
    };

    /** Callback jpegCallback */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera camera){
            Log.d(TAG, "JpegCallback - onPictureTaken");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Log.d(TAG, "Image rotation");
            // Bitmap bitmapRotate = setRotation(bitmap);
            Log.d(TAG, "Bytes Origin : " + bitmap.getByteCount() + " - " + Utils.humanReadableByteCount(bitmap.getByteCount(),true));
            Bitmap bitmapResize = Utils.resizeByWeight(getApplicationContext(), bitmap);
            Log.d(TAG, "Bytes Transform : " + bitmapResize.getByteCount() + " - " + Utils.humanReadableByteCount(bitmapResize.getByteCount(),true));

            NodeRedMsg nrm = createNodeRedMsg(bitmapResize);

            sendMessage(nrm);
        }
    };

    /**
     * Méthode une fois la photo prise avec succès
     * Gestion de la prise de photo par le SDK
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),contentUri);
            } catch (IOException e) {
                Log.d(TAG, "[Error] onActivityResult - erreur de récupération de l'image générée");
            }

            Log.d(TAG, "Nb Bytes Origin : " + bitmap.getByteCount() + " - " + Utils.humanReadableByteCount(bitmap.getByteCount(),true));
            Bitmap imageBitmap = Utils.resizeByWeight(this, bitmap);
            Log.d(TAG, "Nb Bytes Transform : " + imageBitmap.getByteCount() + " - " + Utils.humanReadableByteCount(imageBitmap.getByteCount(),true));

            NodeRedMsg nrm = createNodeRedMsg(imageBitmap);

            sendMessage(nrm);
        }
    }

    /**
     * Creation de l'objet Nod-Red à transmettre à l'API Node-Red
     * @param imageBitmap
     * @return nrm
     */
    private NodeRedMsg createNodeRedMsg(Bitmap imageBitmap){

        NodeRedMsg nrm = new NodeRedMsg();

        nrm.setFrom(Constants.MQTT_MAIL_FROM);
        nrm.setTo(mqtt_message.getEmail());
        nrm.setSubject(getResources().getString(R.string.mqtt_mail_subject));
        nrm.setBody(getResources().getString(R.string.mqtt_mail_body).replace("####", mqtt_message.getPrenom()));
        nrm.setMail_to(mqtt_message.isMail_to());
        nrm.setSend_to_twitter(mqtt_message.isSend_to_twitter());
        // nrm.setFilename(mqtt_message.getNom() + "-" + mqtt_message.getPrenom() + ".jpeg");
        nrm.setFilename(getResources().getString(R.string.mqtt_mail_filename) + ".jpeg");
        nrm.setPayload(Utils.encodeBase64Image(imageBitmap));
        Log.d(TAG, "Creation du message Node-Red : " + nrm.getFilename());

        return nrm;

    }

    /**
     * Envoi du message a l'API Node-Red
     * @param nrm Message Node-Red
     */
    private void sendMessage(NodeRedMsg nrm){

        final NodeRedApiEndpointInterface api = Utils.getInstanceNodRedRetrofit().create(NodeRedApiEndpointInterface.class);
        Call<NodeRedMsg> call = api.sendMailMQTT(nrm);
        call.enqueue(new Callback<NodeRedMsg>() {
            @Override
            public void onResponse(Call<NodeRedMsg> call, Response<NodeRedMsg> response) {
                int statusCode = response.code();
                String msgRetour = "";
                String status = "";
                if (statusCode != 200) {
                    Log.i(TAG, "[Error] Reponse API Node-Red : " + statusCode + " - " + response.message());
                    Toast.makeText(getApplicationContext(), R.string.http_send_msg_error, Toast.LENGTH_SHORT).show();
                    msgRetour = getResources().getString(R.string.http_send_msg_error);
                    status = Constants.MQTT_STATUT_KO;
                } else {
                    Log.i(TAG, "[Success] Reponse API Node-Red : " + statusCode + " - " + response.message());
                    Toast.makeText(getApplicationContext(), R.string.http_send_msg_confirm, Toast.LENGTH_SHORT).show();
                    msgRetour = getResources().getString(R.string.http_send_msg_confirm);
                    status = Constants.MQTT_STATUT_OK;
                }

                //publish(Constants.MQTT_TOPIC_MATRIX, msgRetour, status);
                publish(AssetsPropertyReader.getInstance(getApplicationContext()).getProperty("mqtt.topic.nao"), msgRetour, status);
                reconnectService();
            }
            @Override
            public void onFailure(Call<NodeRedMsg> call, Throwable t) {
                Log.i(TAG, "[Error] Reponse API Node-Red : " + t.getMessage());
                Toast.makeText(getApplicationContext(), R.string.http_send_msg_error + t.getMessage(), Toast.LENGTH_SHORT).show();
                //publish(Constants.MQTT_TOPIC_MATRIX, getResources().getString(R.string.http_send_msg_error), Constants.MQTT_STATUT_KO);
                publish(AssetsPropertyReader.getInstance(getApplicationContext()).getProperty("mqtt.topic.nao"), getResources().getString(R.string.http_send_msg_error), Constants.MQTT_STATUT_KO);
            }
        });
        Toast.makeText(this, R.string.http_send_msg, Toast.LENGTH_SHORT).show();
        textToSpeech(getResources().getString(R.string.http_send_msg));
        //publish(Constants.MQTT_TOPIC_MATRIX, getResources().getString(R.string.http_send_msg), Constants.MQTT_STATUT_OK);
        publish(AssetsPropertyReader.getInstance(getApplicationContext()).getProperty("mqtt.topic.nao"), getResources().getString(R.string.http_send_msg), Constants.MQTT_STATUT_OK);

        PhotoActivity.this.finish();
    }

    private void reconnectService(){
        Intent i = new Intent(PhotoActivity.this, MQTTService.class);
        startService(i);
    }

    /**
     * Publish message on topic
     * @param topic
     */
    private void publish(String topic, String msg, String status){
        MQTTService serv = new MQTTService();
        if (topic.equals(AssetsPropertyReader.getInstance(getApplicationContext()).getProperty("mqtt.topic.nao"))) {
            String payload = "";
            try {
                PayloadMqttOutMatrix pmo = new PayloadMqttOutMatrix();
                pmo.setDemo("Demo");
                pmo.setApplication(getResources().getString(R.string.app_name));
                pmo.setType(msg);
                pmo.setStatus(status);
                payload = MqttConverter.convertPayloadObjectToMqtt(pmo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            serv.publish(topic, payload);
        }else if (topic.equals(AssetsPropertyReader.getInstance(getApplicationContext()).getProperty("mqtt.topic.nao"))){
                serv.publish(topic, msg);
        }
    }

    /**
     * Rotation de l'image en fonction de son orientation
     * @param bitmap
     * @return bitmap
     */
    private Bitmap setRotation(Bitmap bitmap){
        Display display = getWindowManager().getDefaultDisplay();
        int rotation = 0;
        switch (display.getRotation()) {
            case Surface.ROTATION_0: // This is display orientation
                rotation = 90;
                break;
            case Surface.ROTATION_90:
                rotation = 0;
                break;
            case Surface.ROTATION_180:
                rotation = 270;
                break;
            case Surface.ROTATION_270:
                rotation = 180;
                break;
        }

        bitmap = rotateImage(bitmap, rotation);

        return bitmap;
    }

    /**
     * Image rotation according to an angle
     * @param source
     * @param angle
     * @return bitmap
     */
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    private void textToSpeech(String msg) {
         TextToSpeech ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                }
            }
        });
        ttobj.setLanguage(Locale.FRANCE);
        ttobj.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    }
}
