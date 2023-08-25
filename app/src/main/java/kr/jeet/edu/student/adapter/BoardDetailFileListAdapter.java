package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;

public class BoardDetailFileListAdapter extends RecyclerView.Adapter<BoardDetailFileListAdapter.ViewHolder> {
    public enum Action{Delete, Download};
    public interface onItemClickListener{
        public void onItemClick(FileData file);
        public void onActionBtnClick(FileData file, Action action);
    }
    private Context mContext;
    private List<FileData> mList;
    private onItemClickListener _listener;
    private Action _action = Action.Delete;
    public BoardDetailFileListAdapter(Context mContext, List<FileData> mList, Action action, onItemClickListener listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = listener;
        this._action = action;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_file_pallet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if(position != NO_POSITION) {
                FileData item = mList.get(position);
//            holder.tvFileName.setText(Utils.getFileName(item.filePath));
                String mimeType = FileUtils.getMimeTypeFromExtension(item.extension);
                if(mimeType != null & mimeType.startsWith("application")){
                    holder.ivFileIcon.setImageResource(R.drawable.ic_vector_application_file);
                }else if(mimeType != null & mimeType.startsWith("text")) {
                    holder.ivFileIcon.setImageResource(R.drawable.ic_vector_text_file);
                }
                holder.tvFileName.setText(item.orgName);
            }
        }catch (Exception e){
            LogMgr.e("FileAdapter Exception : " + e.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivFileIcon;
        private TextView tvFileName;
        private ImageView ivAction;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            ivFileIcon = itemView.findViewById(R.id.iv_icon);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            ivAction = itemView.findViewById(R.id.iv_action);
            switch(_action) {
                case Delete:
                    ivAction.setImageResource(R.drawable.ic_vector_close);
                    break;
                case Download:
                    ivAction.setImageResource(R.drawable.ic_vector_download);
                    break;
            }
            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
            ivAction.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if(position != NO_POSITION) {
                    FileData data = mList.get(position);
                    if (mList.size() > 0) _listener.onActionBtnClick(data, _action);
                }
            });
        }
    }
}

