package kr.jeet.edu.student.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.model.data.ReportCardData;

public class ReportCardDetailListAdapter extends RecyclerView.Adapter<ReportCardDetailListAdapter.ViewHolder>{
    private static final String TAG = "reportcardListAdapter";
    public interface ItemClickListener {
        void onItemClick(int position, ReportCardData item);
    }
    SimpleDateFormat millisecFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss_SSS);
    SimpleDateFormat minuteFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);
    private Context _context;
    private List<ReportCardData> _list;
    private ItemClickListener _listener;

    public ReportCardDetailListAdapter(Context context, List<ReportCardData> list, ItemClickListener listener) {
        this._context = context;
        this._list = list;
        this._listener = listener;
    }

    @NonNull
    @Override
    public ReportCardDetailListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.layout_student_report_card_list_item, parent, false);
        return new ReportCardDetailListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportCardDetailListAdapter.ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        ReportCardData item = _list.get(position);
        holder.tvTitle.setText(item.etName);
        holder.tvGubun.setText(item.etTitleGubunName);
        Drawable gubunDrawable = _context.getResources().getDrawable(R.drawable.bg_usergubun_parent, null);
        Constants.ReportCardType type = Constants.ReportCardType.getByName(item.etTitleGubunName);
        gubunDrawable.setTint(_context.getResources().getColor(type.getColorRes(), null));
        holder.tvGubun.setBackground(gubunDrawable);
        holder.tvGrade.setText(item.etGubun);
        String dateString = item.regDate;
        try {
            Date regDate = millisecFormat.parse(item.regDate);
            dateString = minuteFormat.format(regDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDate.setText(dateString);
        holder.layoutRoot.setBackgroundColor(_context.getColor(R.color.white));

    }

    @Override
    public int getItemCount() {
        if(_list == null) return 0;
        return _list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout layoutRoot;
        private TextView tvGubun, tvTitle, tvGrade, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.root);
            tvGubun = itemView.findViewById(R.id.tv_report_card_state);
            tvGrade = itemView.findViewById(R.id.tv_grade);
            tvTitle = itemView.findViewById(R.id.tv_report_card_title);
            tvDate = itemView.findViewById(R.id.tv_report_card_date);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                ReportCardData item = _list.get(position);
                if (_list.size() > 0) _listener.onItemClick(position, item);
            });

            int selectableItemBackgroundResource = android.R.attr.selectableItemBackground;
            TypedValue typedValue = new TypedValue();
            _context.getTheme().resolveAttribute(selectableItemBackgroundResource, typedValue, true);
            int selectableItemBackgroundDrawableId = typedValue.resourceId;
            layoutRoot.setForeground(_context.getDrawable(selectableItemBackgroundDrawableId));
        }
    }
}
