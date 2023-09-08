package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;

public class AnnouncementListAdapter extends RecyclerView.Adapter<AnnouncementListAdapter.ViewHolder> {

    private String TAG = "AnnounceListAdapter";

    public interface ItemClickListener{ public void onItemClick(AnnouncementData item); }

    private Context mContext;
    private List<AnnouncementData> mList;
    private ItemClickListener _listener;

    private static boolean isWholeCampusMode = false;

    public AnnouncementListAdapter(Context mContext, List<AnnouncementData> mList, AnnouncementListAdapter.ItemClickListener listener) {
        this.mContext = mContext;
        this._listener = listener;
        this.mList = mList;
    }
    public void setWholeCampusMode(boolean flag) {
        this.isWholeCampusMode = flag;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_announcement_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnnouncementData item = mList.get(position);
        try{
            if (item.fileList != null && item.fileList.size() > 0){

                //String url = RetrofitApi.SERVER_BASE_URL + item.fileList.get(0).path + "/" + item.fileList.get(0).saveName;
                boolean isContainImage = false;

                for(FileData data : item.fileList) {

                    String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);

                    if (mimeType != null && mimeType.startsWith("image")){
                        isContainImage = true;
                        String url = RetrofitApi.FILE_SUFFIX_URL + data.path + "/" + data.saveName;
                        url = FileUtils.replaceMultipleSlashes(url);

                        FileUtils.loadImage(mContext, url, holder.imgAnnouncement);
                        LogMgr.e(TAG+" UrlTest", url);
                        break;
                    }else{
                        continue;
                    }
                }
                if(!isContainImage) {
                    Glide.with(mContext).clear(holder.imgAnnouncement);
                    holder.imgAnnouncement.setVisibility(View.GONE);
                }else{
                    holder.imgAnnouncement.setVisibility(View.VISIBLE);
                }
            }
            else {
                Glide.with(mContext).clear(holder.imgAnnouncement);
                holder.imgAnnouncement.setVisibility(View.GONE);
            }
            holder.tvTitle.setText(item.title);
            holder.tvName.setText(item.memberResponseVO.name);
            holder.tvDate.setText(item.insertDate);

//            if (isWholeCampusMode) {
//                holder.tvCampus.setVisibility(View.VISIBLE);
//                holder.tvCampus.setText(TextUtils.isEmpty(item.acaName) ? "" : item.acaName);
//
//            } else {
//                holder.tvCampus.setVisibility(View.GONE);
//            }

            holder.tvCampus.setText(TextUtils.isEmpty(item.acaName) ? "" : item.acaName);
        }catch (Exception e){
            LogMgr.e("ListAdapter Exception : " + e.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgAnnouncement;
        private TextView tvTitle, tvName, tvDate, tvCampus;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tv_announcement_title);
            tvName = itemView.findViewById(R.id.tv_announcement_name);
            tvDate = itemView.findViewById(R.id.tv_announcement_date);
            tvCampus = itemView.findViewById(R.id.tv_announcement_campus);
            imgAnnouncement = itemView.findViewById(R.id.img_announcement);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
