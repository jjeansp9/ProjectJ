package kr.jeet.edu.student.adapter;

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

import java.util.ArrayList;

import kr.jeet.edu.student.R;
import kr.jeet.edu.student.common.Constants;
import kr.jeet.edu.student.model.data.BusRouteData;
import kr.jeet.edu.student.utils.Utils;

public class BusRouteListAdapter extends RecyclerView.Adapter<BusRouteListAdapter.ViewHolder>{

    public interface ItemClickListener{ public void onItemClick(ArrayList<BusRouteData> item, int position); }

    private Context mContext;
    private ArrayList<BusRouteData> mList;
    private ItemClickListener _listener;

    public BusRouteListAdapter(Context mContext, ArrayList<BusRouteData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_bus_route_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusRouteData item = mList.get(position);

        holder.tvBpName.setText(Utils.getStr(item.bpName));

        if("Y".equals(item.isArrive)) {
            if(!TextUtils.isEmpty(item.startDate)) {
                holder.tvArriveDate.setVisibility(View.VISIBLE);
                holder.tvArriveDate.setText(Utils.formatDate(item.startDate, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss, Constants.TIME_FORMATTER_HH_MM));
            }else{
                holder.tvArriveDate.setVisibility(View.GONE);
            }
            if (position == 0) {
                holder.viewRoadTop.setVisibility(View.INVISIBLE);
                holder.viewRoadBottom.setVisibility(View.VISIBLE);
                if(item.isAtThisStop){
                    Glide.with(mContext).load(R.drawable.icon_bus).into(holder.imgIconBus);
                }else {
                    Glide.with(mContext).load(R.drawable.icon_bus_garage_arrive).into(holder.imgIconBus);
                }
            } else if(position == getItemCount() - 1) {
                holder.viewRoadTop.setVisibility(View.VISIBLE);
                holder.viewRoadBottom.setVisibility(View.INVISIBLE);
                if(item.isAtThisStop){
                    Glide.with(mContext).load(R.drawable.icon_bus).into(holder.imgIconBus);
                }else {
                    Glide.with(mContext).load(R.drawable.icon_bus_garage_arrive).into(holder.imgIconBus);
                }
            }else {
                holder.viewRoadTop.setVisibility(View.VISIBLE);
                holder.viewRoadBottom.setVisibility(View.VISIBLE);
                if(item.isAtThisStop){
                    Glide.with(mContext).load(R.drawable.icon_bus).into(holder.imgIconBus);
                }else {
                    Glide.with(mContext).load(R.drawable.icon_bus_arrive).into(holder.imgIconBus);
                }
            }
        }else{
            holder.tvArriveDate.setVisibility(View.GONE);
            if (position == 0) {
                holder.viewRoadTop.setVisibility(View.INVISIBLE);
                holder.viewRoadBottom.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(R.drawable.icon_bus_garage_yet).into(holder.imgIconBus);

            } else if(position == getItemCount() - 1) {
                holder.viewRoadTop.setVisibility(View.VISIBLE);
                holder.viewRoadBottom.setVisibility(View.INVISIBLE);
                Glide.with(mContext).load(R.drawable.icon_bus_garage_yet).into(holder.imgIconBus);
            }else{
                holder.viewRoadTop.setVisibility(View.VISIBLE);
                holder.viewRoadBottom.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(R.drawable.icon_bus_yet).into(holder.imgIconBus);
            }

        }

    }



    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvBpName, tvArriveDate;
        private ImageView imgIconBus;
        private View viewRoadTop, viewRoadBottom;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBpName = itemView.findViewById(R.id.tv_bp_name);
            tvArriveDate = itemView.findViewById(R.id.tv_arrive_date);
            imgIconBus = itemView.findViewById(R.id.img_icon_bus);
            viewRoadTop = itemView.findViewById(R.id.road_top);
            viewRoadBottom = itemView.findViewById(R.id.road_bottom);

        }
    }
}
