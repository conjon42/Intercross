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
    private ItemClickListener mClickListener;
    private Context mContext;
    private IdEntryDbHelper mDbHelper;

    CrossRecyclerViewAdapter(Context context, List<AdapterEntry> data) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mDbHelper = new IdEntryDbHelper(context);

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
        String crossName;
        ViewHolder(View itemView) {
            super(itemView);
            int childCount = ((LinearLayout) itemView).getChildCount();
            crossName = ((TextView) ((LinearLayout) itemView).getChildAt(0)).getText().toString();
            crossView = (TextView) itemView.findViewById(R.id.crossEntryId);
            timestampView = (TextView) itemView.findViewById(R.id.timestamp);
            countView = (TextView) itemView.findViewById(R.id.count);
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

            /*
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Manage headers");



            RecyclerView recyclerView = new RecyclerView(mContext);

            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            HashMap<String, String> colMap = new HashMap<>();

            try {
                String table = IdEntryContract.IdEntry.TABLE_NAME;
                Cursor cursor = db.query(table, headerSet.toArray(new String[] {}), "cross_id=?", new String[] {((TextView) ((LinearLayout) view).getChildAt(0)).getText().toString()}, null, null, null);

                if (cursor.moveToFirst()) {
                    do {
                        final String[] headers = cursor.getColumnNames();

                        for (String header : headers) {

                            final String val = cursor.getString(
                                    cursor.getColumnIndexOrThrow(header)
                            );

                            colMap.put(header, val);

                        }

                    } while (cursor.moveToNext());

                }
                cursor.close();

            } catch (SQLiteException e) {
                e.printStackTrace();
            }

            List<EditTextAdapterEntry> entries = new ArrayList<>();
            for (String header : headerSet) {
                EditText editText = new EditText(mContext);
                editText.setText(colMap.get(header));
                entries.add(new EditTextAdapterEntry(editText, header, cross_id));
            }

            EditTextRecyclerViewAdapter adapter = new EditTextRecyclerViewAdapter(mContext, entries);
            recyclerView.setAdapter(adapter);
            builder.setView(recyclerView);


            builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    SQLiteDatabase db = mDbHelper.getWritableDatabase();

                    for (EditTextAdapterEntry e : entries) {
                        String value = e.editText.getText().toString();

                    }

                }
            });

            AlertDialog dialog = builder.create();

            dialog.show();

            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);*/
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