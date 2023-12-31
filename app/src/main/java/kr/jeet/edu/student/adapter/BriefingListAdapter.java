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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.model.data.BriefingData;
import kr.jeet.edu.student.model.data.FileData;
import kr.jeet.edu.student.model.data.ReadData;
import kr.jeet.edu.student.server.RetrofitApi;
import kr.jeet.edu.student.utils.FileUtils;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

public class BriefingListAdapter extends RecyclerView.Adapter<BriefingListAdapter.ViewHolder> {

    private final static String TAG = "BrfListAdapter";

    public interface ItemClickListener{ public void onItemClick(BriefingData item, int position); }

    private Context mContext;
    private List<ReadData> mList;
    private ItemClickListener _listener;

    private static boolean isWholeCampusMode = false;
    private final boolean IMG_IS_EMPTY = true;
    private final boolean IMG_IS_NOT_EMPTY = false;

    public BriefingListAdapter(Context mContext, List<ReadData> mList, ItemClickListener listener) {
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_brf_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BriefingData item = (BriefingData) mList.get(position);
        try{
            String cnt;

            String dateStr = item.date+item.ptTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm", Locale.KOREA);

            Date date = sdf.parse(dateStr);
            Calendar calBrf = Calendar.getInstance();

            if (date != null) calBrf.setTime(date);
            Calendar calCurrent = Calendar.getInstance();

            int drawable = R.drawable.bg_brf_state_close;

            if (calCurrent.after(calBrf) || calCurrent.equals(calBrf)) {
                cnt = "종료";
            }
            else if (item.reservationCnt >= item.participantsCnt) {
                cnt = "마감";
                drawable = R.drawable.bg_brf_state_finish;
            }
            else {
                cnt = "예약";
                drawable = R.drawable.bg_brf_state_normal;
            }

            String str = "";

            if (!item.isRead) { // 읽지 않은 게시글
                holder.brfRoot.setBackgroundColor(mContext.getColor(R.color.bg_is_read));
            } else {
                holder.brfRoot.setBackgroundColor(Color.TRANSPARENT);
            }

            holder.tvCampusAndAcaGubun.setVisibility(View.VISIBLE);
            holder.tvLocation.setVisibility(View.VISIBLE);
            holder.tvRdCnt.setVisibility(View.VISIBLE);
            holder.imgRdCnt.setVisibility(View.VISIBLE);

            holder.tvState.setBackgroundResource(drawable);
            holder.tvState.setText(cnt);

            holder.tvDate.setText(TextUtils.isEmpty(item.date) || TextUtils.isEmpty(item.ptTime) ? "" : Utils.formatDate(item.date, item.ptTime, false));
            holder.tvTitle.setText(Utils.getStr(item.title));
            holder.tvLocation.setText(Utils.getStr(item.place));

            str = Utils.getStr(item.acaName) + (TextUtils.isEmpty(item.acaGubunName) ? "" : " / " + item.acaGubunName);
            holder.tvCampusAndAcaGubun.setText(str);

            holder.tvRdCnt.setText(Utils.getStr(Utils.decimalFormat(item.rdcnt)));

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.guideline.getLayoutParams();
            layoutParams.guidePercent = 0.77f;
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
                    setView(holder.tvTitle, holder.tvLocation, IMG_IS_EMPTY);

                }else{
                    holder.imgBrf.setVisibility(View.VISIBLE);
                    setView(holder.tvTitle, holder.tvLocation, IMG_IS_NOT_EMPTY);
                }
            }
            else {
                Glide.with(mContext).clear(holder.imgBrf);
                holder.imgBrf.setVisibility(View.INVISIBLE);
                setView(holder.tvTitle, holder.tvLocation, IMG_IS_EMPTY);
            }

        }catch (Exception e){
            LogMgr.e("ListAdapter Exception : " + e.getMessage());
        }
    }

    private void setView(TextView tvTitle, TextView tvLoc, boolean set){
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
        private TextView tvDate, tvTitle, tvLocation, tvState, tvCampusAndAcaGubun, tvRdCnt;

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
            tvRdCnt = itemView.findViewById(R.id.tv_brf_rd_cnt);
            imgRdCnt = itemView.findViewById(R.id.img_brf_rd_cnt);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick((BriefingData) mList.get(position), position);
            });
        }
    }
}
