package kr.jeet.edu.student.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import kr.jeet.edu.student.R;

public class DownloadReceiver extends BroadcastReceiver {

    public interface DownloadCompleteListener { void onDownloadComplete();}
    DownloadCompleteListener listener;

    public DownloadReceiver(DownloadCompleteListener listener){ this.listener = listener; }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            handleDownloadComplete(context, downloadId);
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {

        }
    }

    private void handleDownloadComplete(Context context, long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                Toast.makeText(context, R.string.msg_success_to_download, Toast.LENGTH_SHORT).show();
                listener.onDownloadComplete();
            } else {
                Toast.makeText(context, R.string.error_msg_fail_to_download, Toast.LENGTH_SHORT).show();
                listener.onDownloadComplete();
            }
        }
        cursor.close();
    }
}
