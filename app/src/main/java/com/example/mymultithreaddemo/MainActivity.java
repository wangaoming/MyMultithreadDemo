package com.example.mymultithreaddemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //    常量的定义
    private static final int START_NUM = 1;
    private static final int ADDING_NUM = 2;
    private static final int ENDING_NUM = 3;
    private static final int CANCEL_NUM = 4;
    ProgressBar progressBar = findViewById ( R.id.progress_bar );
    private Button btnThread = findViewById ( R.id.thread_calculate ), btnAsynTask = findViewById ( R.id.asynch_task_cal ),
            btnHandler = findViewById ( R.id.han_img ), btnAsynTaskImg = findViewById ( R.id.async_img ), btnOther = findViewById ( R.id.other_async );
    private TextView showText = findViewById ( R.id.text_show );
    ImageView ivDownload = findViewById ( R.id.img );
    private CalculateThread calculateThread;
    //    Handler的消息变量
    private static final int MSG_SHOW_PROFRESS = 11;
    private static final int MSG_SHOW_IMAGE = 12;
    private static final String DOWNLOAD_OAD_URL = "https://source.unsplash.com/random/1000x600/?race,car";
    private Handler myHandler;
    private Handler uiHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );
        btnThread.setOnClickListener ( this );
        btnAsynTask.setOnClickListener ( this );
        btnHandler.setOnClickListener ( this );
        btnAsynTaskImg.setOnClickListener ( this );
        btnOther.setOnClickListener ( this );
        myHandler = new MyHandler ( this );
        uiHandler = new MyUIHandler ( this );
//
    }


    @Override
    public void onClick(View v) {
        switch (v.getId ( )) {
            case R.id.thread_calculate:
                show1 ( );
                break;
            case R.id.asynch_task_cal:
                show2 ( );
                break;
//                handler下载图片
            case R.id.han_img:
                show3 ( );
                break;
//
            case R.id.async_img:
                show4 ( );
                break;
            case R.id.other_async:
                show5 ( );
                break;

        }


    }


    //多线程的计算
    private void show1() {
        calculateThread = new CalculateThread ( );
        calculateThread.start ( );


    }

    private void show2() {
        new MyAsyncTask ( this ).execute ( 100 );
    }

    private void show3() {
        new Thread ( new DownloadImageFetcher ( DOWNLOAD_OAD_URL ) ).start ( );
        ;
    }

    private void show4() {
        new DownloadImage(this).execute(DOWNLOAD_OAD_URL);
    }


    private void show5() {

        runOnUiThread(new Runnable() {

            @Override

            public void run() {

                btnOther.setText("runOnUiThread方式更新");

                showText.setText("runOnUiThread方式更新TextView的内容");

            }

        });
    }

    //        计算子线程
    class CalculateThread extends Thread {
        @Override
        public void run() {
//            存放结果的变量
            int result = 0;
            boolean isCancel = false;
//            1.刚开始发送一个空消息
            myHandler.sendEmptyMessage ( START_NUM );
            for (int i = 0; i <= 100; i++) {
                try {
                    Thread.sleep ( 100 );
                    result += i;
                } catch (InterruptedException e) {
                    e.printStackTrace ( );
                    isCancel = true;
                    break;
                }
                if (i % 5 == 0) {
                    Message msg = Message.obtain ( );
                    msg.what = ADDING_NUM;
                    msg.arg1 = i;
                    myHandler.sendMessage ( msg );
                }

            }
            if (!isCancel) {
                Message msg = myHandler.obtainMessage ( );
                msg.what = ENDING_NUM;
                msg.arg1 = result;
                myHandler.sendMessage ( msg );
            }
            super.run ( );
        }
    }






    //    自定义静态类
    static class MyHandler extends Handler {
        //  定义引用对象
        private WeakReference <Activity> ref;

        //在构造方式中创建对象
        public MyHandler(Activity activity) {
            this.ref = new WeakReference <> ( activity );

        }

        //重写handler的方式
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage ( msg );
            MainActivity activity = (MainActivity) ref.get ( );
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case START_NUM:
                    activity.progressBar.setVisibility ( View.VISIBLE );
                    break;
                case ADDING_NUM:
                    activity.progressBar.setProgress ( msg.arg1 );
                    activity.showText.setText ( "计算已完成" + msg.arg1 + "%" );

                    break;
                case ENDING_NUM:
                    activity.progressBar.setVisibility ( View.GONE );
                    activity.showText.setText ( "计算以完成，结果为：" + msg.arg1 );
                    activity.myHandler.removeCallbacks ( activity.calculateThread );
                    break;
                case CANCEL_NUM:
                    activity.progressBar.setProgress ( 0 );
                    activity.progressBar.setVisibility ( View.GONE );
                    activity.showText.setText ( "计算已取消" );
                    break;
            }

        }

    }





//    对于下载图片

    private class MyUIHandler extends Handler {
        //        定义弱引用对象
        private WeakReference <Activity> ref;

        //        在构造方法中创建对象
        public MyUIHandler(Activity activity) {
            this.ref = new WeakReference <> ( activity );

        }
//        重写handler方法

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage ( msg );
            // 1. 获取弱引用指向的Activity对象

            MainActivity activity = (MainActivity) ref.get ( );

            if (activity == null || activity.isFinishing ( ) || activity.isDestroyed ( )) {

                removeCallbacksAndMessages ( null );

                return;

            }
//            2.根据Message的what属性值处理消息
            switch (msg.what) {
                case MSG_SHOW_PROFRESS:
                    //显示进度条
                    activity.progressBar.setVisibility ( View.VISIBLE );
                    break;
                case MSG_SHOW_IMAGE:
//                    显示下载图片
                    activity.progressBar.setVisibility ( View.GONE );
//                   给ImageVIew设置图片
                    activity.ivDownload.setImageBitmap ( (Bitmap) msg.obj );
                    break;

            }
        }
    }

    //    下载图片的线程
    private class DownloadImageFetcher implements Runnable {

        private String imgUrl;


        public DownloadImageFetcher(String strUrl) {

            this.imgUrl = strUrl;

        }

        @Override
        public void run() {
            InputStream in = null;
//            发送一个空消息到handleMessage（）去处理
            uiHandler.obtainMessage ( MSG_SHOW_PROFRESS ).sendToTarget ( );
            try {

                // 1. 将url字符串转为URL对象

                URL url = new URL ( imgUrl );

                // 2. 打开url对象的http连接

                HttpURLConnection connection = (HttpURLConnection) url.openConnection ( );

                // 3. 获取这个连接的输入流

                in = connection.getInputStream ( );

                // 4. 将输入流解码为Bitmap图片

                Bitmap bitmap = BitmapFactory.decodeStream ( in );

                // 5. 通过handler发送消息

//                uiHandler.obtainMessage(MSG_SHOW_IMAGE, bitmap).sendToTarget();

                Message msg = uiHandler.obtainMessage ( );

                msg.what = MSG_SHOW_IMAGE;

                msg.obj = bitmap;

                uiHandler.sendMessage ( msg );

            } catch (IOException e) {

                e.printStackTrace ( );

            } finally {

                if (in != null) {

                    try {

                        in.close ( );

                    } catch (IOException e) {

                        e.printStackTrace ( );

                    }

                }

            }

        }

    }

    /**
     * 1. 创建AsyncTask子类继承AsyncTask类
     * <p>
     * 2. 为3个泛型参数指定类型；若不使用，可用java.lang.Void类型代替，
     * <p>
     * 输入参数 = Integer类型、执行进度 = Integer类型、执行结果 = Integer类型
     */

    static class MyAsyncTask extends AsyncTask <Integer, Integer, Integer> {

        private WeakReference <AppCompatActivity> ref;


        public MyAsyncTask(AppCompatActivity activity) {

            this.ref = new WeakReference <> ( activity );

        }
        // 执行线程任务前的操作

        @Override

        protected void onPreExecute() {

            super.onPreExecute ( );

            MainActivity activity = (MainActivity) this.ref.get ( );

            activity.progressBar.setVisibility ( View.VISIBLE );

        }


        // 接收输入参数、执行任务中的耗时操作、返回线程任务执行的结果

        @Override

        protected Integer doInBackground(Integer... params) {

            int sleep = params[0];

            int result = 0;


            for (int i = 0; i < 101; i++) {

                try {

                    Thread.sleep ( sleep );

                    result += i;

                } catch (InterruptedException e) {

                    e.printStackTrace ( );

                }


                if (i % 5 == 0) {

                    publishProgress ( i );

                }


                if (isCancelled ( )) {

                    break;

                }

            }

            return result;

        }

    }

}