package com.uploadimage;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class RequestHandler {

    public String postRequest(String requestURL, Bitmap bitmap, String filename) {
        URL url;
        String response = "";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try {
            url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            HttpsTrustManager.allowAllSSL();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("thumb_file", filename);
            //conn.setRequestProperty("up_file", filename);

            OutputStream os = conn.getOutputStream();

            os.write((twoHyphens + boundary + lineEnd).getBytes());
            os.write(("Content-Disposition: form-data; name=\"thumb_file\";filename=\""
                    + filename + "\"" + lineEnd).getBytes());
//            os.write((twoHyphens + boundary + lineEnd).getBytes());
//            os.write(("Content-Disposition: form-data; name=\"up_file\";filename=\""
//                    + filename + "\"" + lineEnd).getBytes());
            os.write(("Content-Type: text/plain;charset=UTF-8" + lineEnd).getBytes());
            os.write(lineEnd.getBytes());



            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapData = bos.toByteArray();

            os.write(bitmapData);
            os.write((lineEnd).getBytes());
            os.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                response = "Uploaded failed";
                while ((line = br.readLine()) != null) {
                    if(line.contains("Uploaded Successfully")) {
                        response = "Uploaded Successfully";
                    }
                }
            } else {
                response = "Error Registering";
            }
        } catch (Exception e) {
            response = "Error Registering";
            e.printStackTrace();
        }
        return response;
    }
}

