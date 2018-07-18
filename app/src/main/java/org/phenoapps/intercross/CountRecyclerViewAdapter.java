package org.phenoapps.intercross;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class CountRecyclerViewAdapter extends RecyclerView.Adapter<CountRecyclerViewAdapter.ViewHolder> {

    private List<AdapterEntry> mData;
    private LayoutInflater mInflater;
    private org.phenoapps.intercross.CrossRecyclerViewAdapter.ItemClickListener mClickListener;
    private Context mContext;
    private IdEntryDbHelper mDbHelper;

    CountRecyclerViewAdapter(Context context, List<AdapterEntry> data) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mDbHelper = new IdEntryDbHelper(context);

    }

    @Override
    public CountRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row, parent, false);
        return new org.phenoapps.intercross.CountRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CountRecyclerViewAdapter.ViewHolder holder, int position) {
        AdapterEntry entry = mData.get(position);
        holder.crossView.setText(entry.crossId);
        holder.countView.setText(entry.timestamp);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView crossView;
        TextView countView;
        String crossName;
        ViewHolder(View itemView) {
            super(itemView);
            int childCount = ((LinearLayout) itemView).getChildCount();
            crossName = ((TextView) ((LinearLayout) itemView).getChildAt(0)).getText().toString();
            crossView = (TextView) itemView.findViewById(R.id.crossEntryId);
            crossView.setSingleLine();
            crossView.setEllipsize(TextUtils.TruncateAt.END);
            countView = (TextView) itemView.findViewById(R.id.timestamp);
            countView.setSingleLine();
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
