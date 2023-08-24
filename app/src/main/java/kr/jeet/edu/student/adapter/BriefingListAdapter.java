package kr.jeet.edu.student.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

public class BriefingListAdapter extends RecyclerView.Adapter<BriefingListAdapter.ViewHolder> {

    private final static String TAG = "BrfListAdapter";

    public interface ItemClickListener{ public void onItemClick(BriefingData item); }

    private Context mContext;
    private List<BriefingData> mList;
    private ItemClickListener _listener;

    private final boolean IMG_IS_EMPTY = true;
    private final boolean IMG_IS_NOT_EMPTY = false;

    public BriefingListAdapter(Context mContext, List<BriefingData> mList, ItemClickListener listener) {
        this.mContext = mContext;
        this._listener = listener;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_brf_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BriefingData item = mList.get(position);
        try{
            holder.tvDate.setText(TextUtils.isEmpty(item.date) || TextUtils.isEmpty(item.ptTime) ? "" : Utils.formatDate(item.date, item.ptTime, false));
            holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);
            holder.tvLocation.setText(TextUtils.isEmpty(item.place) ? "" : item.place);

            if (item.campusAll) {
                holder.tvCampus.setVisibility(View.VISIBLE);
                holder.tvCampus.setText(TextUtils.isEmpty(item.acaName) ? "" : item.acaName);

            } else {
                holder.tvCampus.setVisibility(View.GONE);
            }

            String cnt;
            if (item.reservationCnt >= item.participantsCnt) {
                cnt = "신청마감";
                int color = ContextCompat.getColor(mContext, R.color.font_color_999);
                holder.tvSubscription.setTextColor(color);

            } else {
                cnt = item.reservationCnt + " / " + item.participantsCnt+"명";
                int color = ContextCompat.getColor(mContext, R.color.font_color_red);
                holder.tvSubscription.setTextColor(color);
            }
            holder.tvSubscription.setText(cnt);

            if (item.fileList != null && item.fileList.size() > 0){

                boolean isContainImage = false;

                for(FileData data : item.fileList) {

                    String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);

                    if (mimeType != null && mimeType.startsWith("image")){
                        isContainImage = true;
                        String url = RetrofitApi.FILE_SUFFIX_URL + data.path + "/" + data.saveName;
                        url = FileUtils.replaceMultipleSlashes(url);

                        Utils.loadImage(mContext, url, holder.imgBrf);
                        LogMgr.e(TAG+" UrlTest", url);
                        break;
                    }else{
                        continue;
                    }
                }
                if(!isContainImage) {
                    Glide.with(mContext).clear(holder.imgBrf);
                    holder.imgBrf.setVisibility(View.INVISIBLE);
                    setView(holder.tvTitle, holder.tvSubscription, IMG_IS_EMPTY);

                }else{
                    holder.imgBrf.setVisibility(View.VISIBLE);
                    setView(holder.tvTitle, holder.tvSubscription, IMG_IS_NOT_EMPTY);
                }
            }
            else {
                Glide.with(mContext).clear(holder.imgBrf);
                holder.imgBrf.setVisibility(View.INVISIBLE);
                setView(holder.tvTitle, holder.tvSubscription, IMG_IS_EMPTY);
            }

        }catch (Exception e){
            LogMgr.e("ListAdapter Exception : " + e.getMessage());
        }
    }

    private void setView(TextView tvTitle, TextView tvSub, boolean set){
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) tvTitle.getLayoutParams();

        if (set){
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.endToStart = ConstraintLayout.LayoutParams.UNSET;
            //tvSub.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

        }else{
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.endToStart = R.id.img_brf;
            //tvSub.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

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
        private ImageView imgBrf;
        private TextView tvDate, tvTitle, tvLocation, tvSubscription, tvCampus;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            brfRoot = itemView.findViewById(R.id.brf_root);
            tvDate = itemView.findViewById(R.id.tv_brf_date);
            tvTitle = itemView.findViewById(R.id.tv_brf_title);
            tvLocation = itemView.findViewById(R.id.tv_brf_location);
            tvSubscription = itemView.findViewById(R.id.tv_brf_subscription);
            tvCampus = itemView.findViewById(R.id.tv_brf_campus);
            imgBrf = itemView.findViewById(R.id.img_brf);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                _listener.onItemClick(mList.get(position));
            });
        }
    }
}
