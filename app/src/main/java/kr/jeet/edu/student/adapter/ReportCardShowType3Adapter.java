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
import kr.jeet.edu.student.activity.ReportCardShowActivity;
import kr.jeet.edu.student.model.data.ReportCardExamData;
import kr.jeet.edu.student.model.data.ReportCardExamFooterData;
import kr.jeet.edu.student.model.data.ReportNameData;
import kr.jeet.edu.student.model.data.ReportScoreData;
import kr.jeet.edu.student.utils.LogMgr;
import kr.jeet.edu.student.utils.Utils;

public class ReportCardShowType3Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final int VIEW_TYPE_MATH = 1;
    private static final int VIEW_TYPE_MATH_FOOTER = 2;

    private LayoutInflater inflater;
    private Context mContext;
    private ArrayList<ReportCardShowActivity.ExamListTypeItem> examList;

    public ReportCardShowType3Adapter(Context mContext, ArrayList<ReportCardShowActivity.ExamListTypeItem> list) {
        this.mContext = mContext;
        this.examList = list;
        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        RecyclerView.ViewHolder holder = null;

        if(viewType == VIEW_TYPE_MATH) {
            View view = inflater.inflate(R.layout.layout_report_show_math_item, parent, false);
            holder = new MathViewHolder(view);
        }else {
            View view = inflater.inflate(R.layout.layout_report_show_math_footer_item, parent, false);
            holder = new FooterViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == NO_POSITION) return;
        ReportCardShowActivity.ExamListTypeItem item = examList.get(position);
        if(holder == null) return;
        if (holder.getItemViewType() == VIEW_TYPE_MATH) {

            if (item != null) {
                ReportCardShowType3Adapter.MathViewHolder mathVH = (ReportCardShowType3Adapter.MathViewHolder) holder;
                ReportCardExamData contentItem = (ReportCardExamData) item;
                String str = String.valueOf(contentItem.esNum);
                mathVH.tvNo.setText(TextUtils.isEmpty(str) ? "" : str);
                mathVH.tvScore.setText(Utils.getStr(contentItem.esScore));
            }
        } else {
            ReportCardShowType3Adapter.FooterViewHolder bodyVH = (ReportCardShowType3Adapter.FooterViewHolder) holder;
            //ReportCardExamFooterData footerItem = (ReportCardExamFooterData) item;

            bodyVH.tvCorrect.setText("ss");
            bodyVH.tvTotal.setText("test");
        }
    }

    @Override
    public int getItemCount() {
        if(examList == null) return 0;
        return examList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return examList.get(position).getType();
    }

    public class MathViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNo, tvScore;
        public MathViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNo = itemView.findViewById(R.id.tv_report_math_no);
            tvScore = itemView.findViewById(R.id.tv_report_math_score);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCorrect, tvTotal;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCorrect = itemView.findViewById(R.id.tv_report_math_correct);
            tvTotal = itemView.findViewById(R.id.tv_report_math_total);
        }
    }
}
