package com.example.mymultithreaddemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

class DownloadImage extends AsyncTask<String, Bitmap,Bitmap> {
    private WeakReference <AppCompatActivity> ref;



    public DownloadImage(AppCompatActivity activity) {

        this.ref = new WeakReference<>(activity);

    }


    @Override
    protected Bitmap doInBackground(String... strings) {
        String url = strings[0];

        return downloadImage ( url );
    }

    private Bitmap downloadImage(String strUrl) {
        InputStream stream = null;

        Bitmap bitmap = null;



        MainActivity activity = (MainActivity) this.ref.get();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();



        try {

            URL url = new URL(strUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            int totalLen = connection.getContentLength();

            if (totalLen == 0) {

                activity.progressBar.setProgress(0);

            }



            if (connection.getResponseCode() == 200) {

                stream = connection.getInputStream();


                int len = -1;

                int progress = 0;

                byte[] tmps = new byte[1024];

                while ((len = stream.read(tmps)) != -1) {

                    progress += len;

                    activity.progressBar.setProgress(progress);

                    bos.write(tmps, 0, len);

                }

                bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.size());

            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            if (stream != null) {

                try {

                    stream.close();

                } catch (IOException e) {

                    e.printStackTrace();

                }

            }

        }

        return bitmap;

    }

    @Override

    protected void onPreExecute() {

        super.onPreExecute();

        MainActivity activity = (MainActivity) this.ref.get();

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute ( bitmap );
        MainActivity activity = (MainActivity) this.ref.get();

        if (bitmap != null) {

            activity.ivDownload.setImageBitmap(bitmap);

        }
    }
}
