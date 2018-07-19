package org.phenoapps.intercross;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrossRecyclerViewAdapter extends RecyclerView.Adapter<CrossRecyclerViewAdapter.ViewHolder> {

    private List<AdapterEntry> mData;
    private LayoutInflater mInflater;
    private Context mContext;

    CrossRecyclerViewAdapter(Context context, List<AdapterEntry> data) {
        this.mContext = context;
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
        if (entry.crossName == null || entry.crossName.length() == 0)
            holder.crossView.setText(entry.crossId);
        else holder.crossView.setText(entry.crossName);
        holder.timestampView.setText(entry.timestamp);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView crossView;
        TextView timestampView;
        String crossName;
        ViewHolder(View itemView) {
            super(itemView);
            int childCount = ((LinearLayout) itemView).getChildCount();
            crossName = ((TextView) ((LinearLayout) itemView).getChildAt(0)).getText().toString();
            crossView = (TextView) itemView.findViewById(R.id.crossEntryId);
            crossView.setSingleLine();
            crossView.setEllipsize(TextUtils.TruncateAt.END);
            timestampView = (TextView) itemView.findViewById(R.id.timestamp);
            timestampView.setSingleLine();
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            Intent i = new Intent(mContext, AuxValueInputActivity.class);

            SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);

            Set<String> headerSet = prefs.getStringSet(SettingsActivity.HEADER_SET, new HashSet<>());

            String cross_id = ((TextView) ((LinearLayout) view).getChildAt(0)).getText().toString();

            i.putExtra("crossId", cross_id);

            i.putExtra("headers", headerSet.toArray(new String[] {}));

            mContext.startActivity(i);

        }
    }
}