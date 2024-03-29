package com.example.authregistr;



import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.authregistr.R;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<UploadedFile> fileList;
    private DocumentiFragment documentiFragment;

    public FileAdapter(List<UploadedFile> fileList, DocumentiFragment documentiFragment) {
        this.fileList = fileList;
        this.documentiFragment = documentiFragment;
    }

    public interface OnItemClickListener {
        void onItemClick(UploadedFile uploadedFile);

        void onDeleteClick(UploadedFile uploadedFile);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UploadedFile uploadedFile = fileList.get(position);
        holder.fileNameTextView.setText(uploadedFile.getFileName());

        holder.fileNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the clicked view is the TextView
                Log.d("FileAdapter", "Text clicked for item: " + uploadedFile.getFileName());
                if (listener != null) {
                    listener.onItemClick(uploadedFile);
                }
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(uploadedFile);
                }
            }
        });
    }






    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView fileNameTextView;
        public ImageButton deleteButton;

        public ViewHolder(View view) {
            super(view);
            fileNameTextView = view.findViewById(R.id.fileNameTextView);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}


