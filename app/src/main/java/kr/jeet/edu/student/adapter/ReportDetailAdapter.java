package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.model.data.AnnouncementData;
import kr.jeet.edu.student.model.data.ReportDetailData;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

public class ReportDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int VIEW_TYPE_GRADE = 0;
    private static final int VIEW_TYPE_MATH = 1;
    private static final int VIEW_TYPE_MATH_BOTTOM = 2;

    private Context mContext;
    private ArrayList<ReportDetailData> mList = new ArrayList<>();
    private int etTitleGubun = -1;

    public ReportDetailAdapter(Context mContext, ArrayList<ReportDetailData> mList, int etTitleGubun) {
        this.mContext = mContext;
        this.mList = mList;
        this.etTitleGubun = etTitleGubun;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        RecyclerView.ViewHolder holder;

        if (viewType == VIEW_TYPE_GRADE) holder = new GradeViewHolder(inflater.inflate(R.layout.layout_report_detail_grade_item, parent, false));
        else holder = new MathViewHolder(inflater.inflate(R.layout.layout_report_detail_math_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == NO_POSITION) return;

        if (holder.getItemViewType() == VIEW_TYPE_GRADE) {

        } else {
            ReportDetailData item = mList.get(position);

            if (item != null) {
                MathViewHolder mathVH = (MathViewHolder) holder;
                // TODO : 개발 진행중
                if (!TextUtils.isEmpty(item.esSub)) {
                    LogMgr.e("ADAPTER: ", position +"");
                    mathVH.tvNo.setText("번호");
                    mathVH.tvScore.setText("정오");

                } else {
                    LogMgr.e("ADAPTER2: ", position +"");
                    mathVH.tvNo.setText(TextUtils.isEmpty(item.esScore) ? "" : String.valueOf(position));
                    mathVH.tvScore.setText(Utils.getStr(item.esScore));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (etTitleGubun == Constants.ET_TITLE_GUBUN_ELEMENTARY) return VIEW_TYPE_GRADE;
        else if (etTitleGubun == Constants.ET_TITLE_GUBUN_MIDDLE) return VIEW_TYPE_GRADE;
        else return VIEW_TYPE_MATH;
    }

    public class GradeViewHolder extends RecyclerView.ViewHolder {

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }

    public class MathViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNo, tvScore;
        public MathViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNo = itemView.findViewById(R.id.tv_report_math_no);
            tvScore = itemView.findViewById(R.id.tv_report_math_score);
        }
    }
}
