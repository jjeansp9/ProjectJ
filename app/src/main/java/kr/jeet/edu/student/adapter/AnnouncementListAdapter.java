package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.db.PushMessage;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

public class AnnouncementListAdapter extends RecyclerView.Adapter<AnnouncementListAdapter.ViewHolder> {

    private String TAG = "AnnounceListAdapter";

    public interface ItemClickListener{ public void onItemClick(AnnouncementData item, TextView title, int position); }

    private Context mContext;
    private List<AnnouncementData> mList;
    private ItemClickListener _listener;

    private final boolean IMG_IS_EMPTY = true;
    private final boolean IMG_IS_NOT_EMPTY = false;

    private static boolean isWholeCampusMode = false;
    private static boolean isMain = false;

    public AnnouncementListAdapter(Context mContext, List<AnnouncementData> mList, boolean isMain, AnnouncementListAdapter.ItemClickListener listener) {
        this.mContext = mContext;
        this._listener = listener;
        this.mList = mList;
        this.isMain = isMain;
    }

    public void setWholeCampusMode(boolean flag) {
        this.isWholeCampusMode = flag;
    }
    public void setMainMode(boolean flag) {this.isMain = flag;}
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_brf_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnnouncementData item = mList.get(position);
        try{
            LogMgr.e("isMain: " + isMain);

            String str = "";

            if (isMain) {
                holder.brfRoot.setForeground(null);
                holder.tvRdCnt.setVisibility(View.VISIBLE);
                holder.imgRdCnt.setVisibility(View.VISIBLE);
                holder.brfRoot.setBackgroundColor(Color.TRANSPARENT);
            }else{
                holder.tvRdCnt.setVisibility(View.VISIBLE);
                holder.imgRdCnt.setVisibility(View.VISIBLE);
                holder.tvCampusAndAcaGubun.setVisibility(View.VISIBLE);
                LogMgr.e(TAG, "isRead: " + item.isRead);

                if (!item.isRead) { // 읽지 않은 게시글
                    holder.brfRoot.setBackgroundColor(mContext.getColor(R.color.bg_is_read));
                } else {
                    holder.brfRoot.setBackgroundColor(Color.TRANSPARENT);
                }
                //holder.tvLocation.setVisibility(View.VISIBLE);

                str = TextUtils.isEmpty(item.acaName) ? "캠퍼스 정보없음" : item.acaName + (TextUtils.isEmpty(item.acaGubunName) ? "" : " / " + item.acaGubunName);
                holder.tvCampusAndAcaGubun.setText(str);

                //str = TextUtils.isEmpty(item.memberResponseVO.name) ? "이름 정보없음" : item.memberResponseVO.name;
                //holder.tvLocation.setText(str);
            }

            holder.tvState.setVisibility(View.GONE);
            holder.tvDate.setVisibility(View.GONE);
            holder.tvAnnouncementDate.setVisibility(View.VISIBLE);

            holder.tvTitle.setText(Utils.getStr(item.title));
            holder.tvAnnouncementDate.setText(Utils.getStr(item.insertDate));
            holder.tvRdCnt.setText(Utils.getStr(Utils.decimalFormat(item.rdcnt)));

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.guideline.getLayoutParams();
            layoutParams.guidePercent = 0.70f;
            holder.guideline.setLayoutParams(layoutParams);

            if (item.fileList != null && item.fileList.size() > 0){

                boolean isContainImage = false;

                for(FileData data : item.fileList) {

                    String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);

                    if (mimeType != null && mimeType.startsWith("image")){
                        isContainImage = true;
                        String url = RetrofitApi.FILE_SUFFIX_URL + data.path + "/" + data.saveName;
                        url = FileUtils.replaceMultipleSlashes(url);

                        FileUtils.loadImage(mContext, url, holder.imgBrf);
                        LogMgr.e(TAG+" UrlTest", url);
                        break;
                    }else{
                        continue;
                    }
                }
                if(!isContainImage) {
                    Glide.with(mContext).clear(holder.imgBrf);
                    holder.imgBrf.setVisibility(View.INVISIBLE);
                    setView(holder.tvTitle, IMG_IS_EMPTY);

                }else{
                    holder.imgBrf.setVisibility(View.VISIBLE);
                    setView(holder.tvTitle, IMG_IS_NOT_EMPTY);
                }
            }
            else {
                Glide.with(mContext).clear(holder.imgBrf);
                holder.imgBrf.setVisibility(View.INVISIBLE);
                setView(holder.tvTitle, IMG_IS_EMPTY);
            }

        }catch (Exception e){
            LogMgr.e("ListAdapter Exception : " + e.getMessage());
        }

    }

    private void setView(TextView tvTitle, boolean set){
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) tvTitle.getLayoutParams();

        if (set){
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.endToStart = ConstraintLayout.LayoutParams.UNSET;

        }else{
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.endToStart = R.id.img_brf;

        }
        tvTitle.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout brfRoot;
        private Guideline guideline;
        private ImageView imgBrf, imgRdCnt;
        private TextView tvDate, tvTitle, tvLocation, tvState, tvCampusAndAcaGubun, tvAnnouncementDate, tvRdCnt;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            brfRoot = itemView.findViewById(R.id.brf_root);
            tvDate = itemView.findViewById(R.id.tv_brf_date);
            tvTitle = itemView.findViewById(R.id.tv_brf_title);
            tvLocation = itemView.findViewById(R.id.tv_brf_location);
            tvState = itemView.findViewById(R.id.tv_brf_state);
            tvCampusAndAcaGubun = itemView.findViewById(R.id.tv_brf_campus_and_aca_gubun);
            imgBrf = itemView.findViewById(R.id.img_brf);
            guideline = itemView.findViewById(R.id.guideline);
            tvAnnouncementDate = itemView.findViewById(R.id.tv_announcement_date);
            tvRdCnt = itemView.findViewById(R.id.tv_rd_cnt);
            imgRdCnt = itemView.findViewById(R.id.img_rd_cnt);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position), tvTitle, position);
            });
        }
    }
}
