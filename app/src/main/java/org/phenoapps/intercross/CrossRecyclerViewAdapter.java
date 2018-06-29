package org.phenoapps.intercross;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class CrossRecyclerViewAdapter extends RecyclerView.Adapter<CrossRecyclerViewAdapter.ViewHolder> {

    private List<AdapterEntry> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    CrossRecyclerViewAdapter(Context context, List<AdapterEntry> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AdapterEntry entry = mData.get(position);
        holder.crossView.setText(entry.crossId);
        holder.timestampView.setText(entry.timestamp);
        holder.countView.setText(entry.count);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView crossView;
        TextView timestampView;
        TextView countView;
        ViewHolder(View itemView) {
            super(itemView);
            crossView = (TextView) itemView.findViewById(R.id.crossEntryId);
            timestampView = (TextView) itemView.findViewById(R.id.timestamp);
            countView = (TextView) itemView.findViewById(R.id.count);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    AdapterEntry getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}