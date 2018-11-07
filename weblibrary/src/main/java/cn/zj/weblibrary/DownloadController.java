package cn.zj.weblibrary;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.lang.invoke.MethodHandle;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 作者： zhangzhuojia  .
 * 日期： 2018/11/6
 * 版本： V1.0
 * 说明：
 */
public class DownloadController {
    private static final String Tag = "DownloadController";
    private DownloadManager mDownloadManager;
    private Context context;
    private DownloadManager.Query query;
    private Cursor cursor;

    private static DownloadController controller;

    public static DownloadController getInstance(Context context) {
        if (controller == null) {
            synchronized (DownloadController.class) {
                if (controller == null) {
                    controller = new DownloadController(context);
                }
            }
        }
        return controller;
    }

    public DownloadController(Context context) {
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.context = context;
    }


    public long download(String url, String name, boolean isWatching, Handler handler) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
//        String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(url);
//        LogUtils.info(mimeTypeFromExtension);
        request.setMimeType(MediaFile.getMIMEType(url));
        request.allowScanningByMediaScanner();
        long id = mDownloadManager.enqueue(request);
        //注册内容观察者
        if (isWatching) {
            //注册内容观察者
            if (null == observer) {
                observer = new DownloadObserver(id, handler);
                context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/"),
                        true, observer);
            }
        }
        return id;
    }


    private DownloadObserver observer;

    private ScheduledExecutorService scheduledExecutorService;

    private class DownloadObserver extends ContentObserver {
        private long id;
        private Handler mhandler;

        /**
         * Creates a content observer.
         *
         * @param id
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public DownloadObserver(long id, Handler handler) {
            super(handler);
            this.id = id;
            this.mhandler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //TODO 内容有变化时，查询
            if (null == scheduledExecutorService) {
                scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                //每两秒查询一下动静
                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        queryDownloadProgress(id, mhandler);
                    }
                }, 1, 2, TimeUnit.SECONDS);
            }
        }
    }

    private void queryDownloadProgress(long id, Handler handler) {
        Message msg = handler.obtainMessage();
        if (null == query) {
            query = new DownloadManager.Query().setFilterById(id);

        }
        cursor = mDownloadManager.query(query);

        if (null != cursor && cursor.moveToFirst()) {
            String[] columnNames = cursor.getColumnNames();
            String fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            int soFar = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

            StringBuilder builder = new StringBuilder();
            for (String name :
                    columnNames) {
                builder.append(name + " ");
            }
            Log.d(Tag, builder.toString());
            Log.d(Tag, fileName + ":" + soFar + "/" + total);
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    Log.d(Tag, "download paused");
                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.d(Tag, "download failed");
                    if (!scheduledExecutorService.isShutdown()) {
                        scheduledExecutorService.shutdownNow();
                    }
                    scheduledExecutorService = null;
                    Log.e(Tag, "download failed");
                    msg.what = DownloadManager.STATUS_FAILED;
                    handler.sendMessage(msg);
                    break;
                case DownloadManager.STATUS_PENDING:
                    Log.d(Tag, "download pending");
                    break;
                case DownloadManager.STATUS_RUNNING:
                    Log.d(Tag, "download running");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.d(Tag, "download successful");
                    if (!scheduledExecutorService.isShutdown()) {
                        scheduledExecutorService.shutdownNow();
                    }
                    msg.what = DownloadManager.STATUS_FAILED;
                    handler.sendMessage(msg);
                    scheduledExecutorService = null;
                    break;
            }

            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
    }
}
