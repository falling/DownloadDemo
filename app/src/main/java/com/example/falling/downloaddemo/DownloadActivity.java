package com.example.falling.downloaddemo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mDownloadButton;
    private EditText mUrlText;
    private String mUrl;
    private ProgressBar mProgressBar;
    private int mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downlaod);
        mDownloadButton = (Button) findViewById(R.id.download_button);
        mUrlText = (EditText) findViewById(R.id.Download_Url);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mDownloadButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download_button:
                if(InternetUtil.isNetworkConnected(v.getContext())) {
                    mUrl = mUrlText.getText().toString();
                    mProgressBar.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            downloadInBackGround(mUrl);
                        }
                    }).start();
                }else{
                    Toast.makeText(v.getContext(),"无法上网！",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void downloadInBackGround(String strUrl) {
        try {
            URL url = new URL(strUrl);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            int TotalSize = urlConnection.getContentLength();
            String downloadFolderName = Environment.getExternalStorageDirectory() + "/MyDownloadDemo/";
            Log.i("Download",downloadFolderName);
            File file = new File(downloadFolderName);
            if (!file.exists()) {
                if (!file.mkdir()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DownloadActivity.this, "创建文件失败，是否有读写权限？", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    return;
                }
            }

            String strings[] = strUrl.split("/");
            File apkFile = new File(downloadFolderName + strings[strings.length - 1]);
            Log.i("Download",downloadFolderName + strings[strings.length - 1]);
            if (apkFile.exists()) {
                if (!apkFile.delete()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DownloadActivity.this, "删除文件失败，是否有读写权限？", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    return;
                }
            }

            int downloadSize = 0;
            byte[] bytes = new byte[1024];
            int length = 0;
            OutputStream outputStream = new FileOutputStream(apkFile);
            while ((length = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, length);
                outputStream.flush();
                downloadSize += length;
                mProgress = downloadSize *100 / TotalSize;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setProgress(mProgress);
                        mDownloadButton.setText("下载中");

                    }
                });
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDownloadButton.setText("下载");
                    Toast.makeText(DownloadActivity.this,"下载完成",Toast.LENGTH_SHORT).show();

                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
