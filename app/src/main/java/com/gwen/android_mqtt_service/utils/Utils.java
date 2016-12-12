package com.gwen.android_mqtt_service.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gwen.android_mqtt_service.constants.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Utils {

    private static final String TAG = Utils.class.getName();

    /**
     * Instance Retrofit NodeRed
     * @return
     */
    public static Retrofit getInstanceNodRedRetrofit(){

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        final String basicAuth = "Basic " + Utils.encodeBase64String(Constants.MQTT_NODE_RED_USERNAME + ":" + Constants.MQTT_NODE_RED_PWD);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(Constants.OKHTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.OKHTTP_READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.OKHTTP_WRITE_TIMEOUT, TimeUnit.SECONDS);
        httpClient.addInterceptor(new Interceptor() {
                                      @Override
                                      public Response intercept(Interceptor.Chain chain) throws IOException {
                                          Request original = chain.request();

                                          Request request = original.newBuilder()
                                                  .header("Authorization", basicAuth.trim())
                                                  .header("Content-Type", "application/json")
                                                  .method(original.method(), original.body())
                                                  .build();

                                          return chain.proceed(request);
                                      }
                                  }
        );

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.getNodeRedUrlWS())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        return retrofit;

    }

    /**
     * Encodage image in Base64
     * @param bm image Bipmap
     * @return encodedImage
     */
    public static String encodeBase64Image(Bitmap bm) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();

        String encodedImage = Base64.encodeToString(b, Base64.NO_WRAP);

        return encodedImage;
    }

    /**
     * Encode a string in Base64
     * @param str
     * @return
     */
    public static String encodeBase64String(String str) {

        byte[] data = new byte[0];
        try {
            data = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodedString = Base64.encodeToString(data, Base64.NO_WRAP);

        return encodedString;
    }

    /**
     * Image resize
     * @param ctx
     * @param bitmap
     * @return
     */
    public static Bitmap resizeByWeight(Context ctx, Bitmap bitmap){
        int scaleMax = 100;
        Bitmap imageScaled = null;
        while (scaleMax != 0){
            Bitmap imageBitmap = Utils.scaleImage(ctx, bitmap, scaleMax);
            Log.d(TAG, "Scale : " + scaleMax + " - " + imageBitmap.getByteCount() + " - " + Utils.humanReadableByteCount(imageBitmap.getByteCount(),true));
            if (imageBitmap.getByteCount() < 25000000 ){
                imageScaled = imageBitmap;
                scaleMax = 0;
            } else {
                scaleMax = scaleMax - 5;
            }
        }
        return imageScaled;
    }

    /**
     * Redimensionnement d'une image
     * @param bmp image a resizer
     * @param scale pourcentage du redimensionnement
     * @return scaled
     */
    public static Bitmap scaleImage(Context ctx, Bitmap bmp, int scale){

        long nbBytes = bmp.getByteCount();
        Log.d(TAG, "Nb Bytes Origin : " + nbBytes + " - " + Utils.humanReadableByteCount(nbBytes,true));

        // Technique 1
        Display display = getDisplaySize(ctx);
        Point size = new Point();
        display.getSize(size);
        int sizeY = bmp.getHeight() * scale / 100;
        int sizeX = bmp.getWidth() * sizeY / bmp.getHeight();

        Bitmap scaled = Bitmap.createScaledBitmap(bmp, sizeX, sizeY, false);

        long nbBytesResized = scaled.getByteCount();
        Log.d(TAG, "Nb Bytes Resized : " + nbBytesResized + " - " + Utils.humanReadableByteCount(nbBytesResized,true));


        return scaled;
    }

    /**
     * Récuperation des dimensions de l'écran client
     * @param ctx contexte
     * @return display
     */
    public static Display getDisplaySize(Context ctx){
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display;
    }

    /**
     * Retourne un nombre de bytes en format lisible
     * @param bytes
     * @param si
     * @return
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Test la validite du message par la date
     * @param timestamp
     * @return boolean
     */
    public static boolean isDateValide(Long timestamp){
        Long tsNow = System.currentTimeMillis() / 1000L;
        long diff =  tsNow - timestamp;
        return diff < Constants.MQTT_MSG_VALIDITE;
    }

}
