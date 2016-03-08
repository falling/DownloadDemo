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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int INVISIBLE = -2;
    public static final int NOCHANGE = -1;
    private Button mDownloadButton;
    private EditText mUrlText;
    private String mUrl;
    private ProgressBar mProgressBar;
    private String mButton_Text;
    private String mToast_Text;
    private int mProgress;
    private Toast mToast;

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
                if (InternetUtil.isNetworkConnected(v.getContext())) {
                    mUrl = mUrlText.getText().toString();
                    mProgressBar.setVisibility(View.VISIBLE);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                downloadInBackGround(mUrl);
                            }
                        }).start();
                } else {
                    Toast.makeText(v.getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void downloadInBackGround(String strUrl) {
        int TotalSize; //总文件大小
        String downloadFolderName; //下载的目录文件夹
        String downloadFileName;    //下载的文件名
        InputStream inputStream; //下载的流
        try {
            URL url = new URL(strUrl);
            URLConnection urlConnection = url.openConnection();
            inputStream = urlConnection.getInputStream();
            TotalSize = urlConnection.getContentLength();

            //文件路径
            downloadFolderName = Environment.getExternalStorageDirectory() + "/MyDownloadDemo/";
            File foldName = new File(downloadFolderName);
            //文件名
            String strings[] = strUrl.split("/");
            downloadFileName = downloadFolderName + strings[strings.length - 1];
            File fileName = new File(downloadFileName);

            if (checkFoldAndFile(foldName, fileName)) return;

            startDownload(TotalSize, inputStream, fileName);

        } catch (IOException e) {
            e.printStackTrace();
            myRunOnUiThread(null, INVISIBLE, getString(R.string.error_URL));
        }
    }

    /**
     * 检查文件夹是否创建，未创建则创建。 检查要下载的文件是否已经存在，存在则删除。
     * @param foldName
     * @param fileName
     * @return
     */
    private boolean checkFoldAndFile(File foldName, File fileName) {
        if (!foldName.exists()) {
            if (!foldName.mkdir()) {
                myRunOnUiThread(null, INVISIBLE, getString(R.string.error_read_write));
                return true;
            }
        }

        if (fileName.exists()) {
            if (!fileName.delete()) {
                myRunOnUiThread(null, INVISIBLE, getString(R.string.error_read_write));
                return true;
            }
        }
        return false;
    }

    /**
     * 开始下载文件
     * @param totalSize 总文件大小
     * @param inputStream 下载的输入流
     * @param fileName  文件名
     * @throws IOException
     */
    private void startDownload(int totalSize, InputStream inputStream, File fileName) throws IOException {
        int downloadSize = 0;
        byte[] bytes = new byte[2048];
        int length;
        OutputStream outputStream = new FileOutputStream(fileName);
        int progress=0;
        while ((length = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, length);
            outputStream.flush();
            downloadSize += length;
            progress = downloadSize * 100 / totalSize;
            myRunOnUiThread("下载中", progress, null);

        }
        myRunOnUiThread("下载", progress, getString(R.string.download_success));
        inputStream.close();
        outputStream.close();
    }

    /**
     * @param button_text 更改按钮文字,不更改则输入 null；
     * @param progress    更改进度条的进度显示，不更改则输入 NOCHANGE；隐藏则 输入 INVISIBLE；
     * @param toast_text  toast显示的文字 不更改则输入 null
     */
    private synchronized void myRunOnUiThread(String button_text, int progress, String toast_text) {
        mButton_Text = button_text;
        mProgress = progress;
        mToast_Text = toast_text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mButton_Text != null) {
                    mDownloadButton.setText(mButton_Text);
                }
                if (mProgress >= 0) {
                    mProgressBar.setProgress(mProgress);
                } else if (mProgress == INVISIBLE) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
                if (mToast_Text != null) {
                    if (mToast != null){
                        mToast.setText(mToast_Text);
                        mToast.setDuration(Toast.LENGTH_SHORT);
                        mToast.show();
                    } else{
                        mToast = Toast.makeText(DownloadActivity.this, mToast_Text, Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                }
            }
        });
    }
}
