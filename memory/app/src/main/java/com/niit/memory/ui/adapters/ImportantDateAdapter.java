package com.niit.memory.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.niit.memory.R;
import com.niit.memory.data.model.ImportantDate;
import java.util.ArrayList;
import java.util.List;

public class ImportantDateAdapter extends RecyclerView.Adapter<ImportantDateAdapter.ViewHolder> {

    private List<ImportantDate> items = new ArrayList<>();
    private OnDateActionListener listener;

    public interface OnDateActionListener {
        void onEdit(ImportantDate date);
        void onDelete(long id);
    }

    public void setOnDateActionListener(OnDateActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<ImportantDate> dates) {
        items = dates != null ? dates : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_important_date, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        ImportantDate item = items.get(pos);
        holder.icon.setText(item.getIcon() != null ? item.getIcon() : "📅");
        holder.title.setText(item.getTitle());
        holder.eventDate.setText(item.getEventDate());

        long daysLeft = item.getDaysLeft() != null ? item.getDaysLeft() : 0;
        if (daysLeft > 0) {
            holder.daysLeft.setText("还有 " + daysLeft + " 天");
            holder.daysLeft.setTextColor(0xFFE88D2E);
        } else if (daysLeft == 0) {
            holder.daysLeft.setText("就是今天!");
            holder.daysLeft.setTextColor(0xFF81C784);
        } else {
            holder.daysLeft.setText("已过");
            holder.daysLeft.setTextColor(0xFFC8BFB5);
        }

        long progressLong = 200 - daysLeft;
        if (progressLong < 0) progressLong = 0;
        if (progressLong > 200) progressLong = 200;
        holder.progress.setProgress((int) progressLong);

        holder.editBtn.setOnClickListener(v -> {
            if (listener != null && item.getId() != null) listener.onEdit(item);
        });
        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null && item.getId() != null) listener.onDelete(item.getId());
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon, title, eventDate, daysLeft, editBtn, deleteBtn;
        ProgressBar progress;
        ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.date_icon);
            title = v.findViewById(R.id.date_title);
            eventDate = v.findViewById(R.id.date_event_date);
            daysLeft = v.findViewById(R.id.date_days_left);
            progress = v.findViewById(R.id.date_progress);
            editBtn = v.findViewById(R.id.date_edit);
            deleteBtn = v.findViewById(R.id.date_delete);
        }
    }
}
