package com.example.authregistr;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.authregistr.OnDeleteClickListener;
import com.example.authregistr.R;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends Fragment implements VideoAdapter.OnDeleteClickListener, VideoAdapter.OnDownloadClickListener{

    private static final int PICK_VIDEO_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST_GEN = 1;
    private static final int PICK_VIDEO_REQUEST_DONNA = 2;
    private View view;
    private Uri selectedVideoUri;
    private StorageReference storageReference;
    private VideoAdapter videoAdapterGen;
    private VideoAdapter videoAdapterDonna;
    private List<VideoModel> videoListGen;
    private List<VideoModel> videoListDonna;
    private RecyclerView recyclerViewGen;
    private RecyclerView recyclerViewDonna;
    private ProgressDialog progressDialog;
    private TextView textVideoName;
    private String targetFolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video, container, false);
        View item_view=inflater.inflate(R.layout.item_video, container, false);

        Button uploadButton = view.findViewById(R.id.uploadButton);
        Button uploadButton2 = view.findViewById(R.id.uploadButton2);
        recyclerViewGen = view.findViewById(R.id.recyclerViewGen);
        recyclerViewDonna = view.findViewById(R.id.recyclerViewDonna);
        textVideoName=item_view.findViewById(R.id.NomeVideo);
        storageReference = FirebaseStorage.getInstance().getReference();

        videoListGen = new ArrayList<>();
        videoAdapterGen = new VideoAdapter(videoListGen, this, this, "videos");

        videoListDonna = new ArrayList<>();
        videoAdapterDonna = new VideoAdapter(videoListDonna, this, this, "videosDonna");

        videoAdapterGen.setOnDeleteClickListener(this);
        videoAdapterGen.setOnDownloadClickListener(this);

        videoAdapterDonna.setOnDeleteClickListener(this);
        videoAdapterDonna.setOnDownloadClickListener(this);

        recyclerViewGen.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGen.setAdapter(videoAdapterGen);

        recyclerViewDonna.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDonna.setAdapter(videoAdapterDonna);

        storageReference = FirebaseStorage.getInstance().getReference();

        fetchVideoUrlsGen();
        fetchVideoUrlsDonna();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading video...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetFolder = "videos";
                openVideoChooser(PICK_VIDEO_REQUEST_GEN);
            }
        });

        uploadButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetFolder = "videosDonna";
                openVideoChooser(PICK_VIDEO_REQUEST_DONNA);
            }
        });



        return view;
    }

    @Override
    public void onDeleteClick(VideoModel videoModel, String targetFolder) {

        showDeleteConfirmationDialog(videoModel, targetFolder);
    }

    @Override
    public void onDownloadClick(VideoModel videoModel) {

        String videoUrl = videoModel.getVideoUrl();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));
        request.setTitle("Downloading Video");
        request.setDescription(videoModel.getName());

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, videoModel.getName());

        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);

        long downloadId = downloadManager.enqueue(request);



        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {
                    Toast.makeText(getContext(), "Download completed", Toast.LENGTH_SHORT).show();
                }
            }
        };

        requireContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    private void openVideoChooser(int requestCode) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            selectedVideoUri = data.getData();
            if (selectedVideoUri != null) {
                // Get the original file name
                String originalFileName = getFileName(selectedVideoUri);

                if (requestCode == PICK_VIDEO_REQUEST_GEN) {
                    targetFolder = "videos";  // Set targetFolder here
                    uploadVideo(selectedVideoUri, originalFileName, "videos");
                } else if (requestCode == PICK_VIDEO_REQUEST_DONNA) {
                    targetFolder = "videosDonna";  // Set targetFolder here
                    uploadVideo(selectedVideoUri, originalFileName, "videosDonna");
                }
            } else {
                Toast.makeText(getContext(), "Failed to get selected video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        Cursor cursor = null;

        try {
            cursor = getActivity().getContentResolver().query(uri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                if (displayNameIndex != -1) {
                    result = cursor.getString(displayNameIndex);
                } else {
                    result = uri.getLastPathSegment();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    private void uploadVideo(Uri videoUri, String originalFileName, String targetFolder) {
        if (videoUri != null) {
            String videoFileName = originalFileName;

            progressDialog.show();



            storageReference.child(targetFolder + "/" + videoFileName).putFile(videoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(getContext(), "Video uploaded successfully", Toast.LENGTH_SHORT).show();

                        if ("videosDonna".equals(targetFolder)) {
                            fetchVideoUrlsDonna();
                        } else {
                            fetchVideoUrlsGen();
                        }

                        progressDialog.dismiss();
                    })
                    .addOnFailureListener(exception -> {
                        Toast.makeText(getContext(), "Failed to upload video", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setProgress((int) progress);
                    });
        }
    }
    private void fetchVideoUrlsGen() {
        videoListGen.clear();

        StorageReference videosRef = storageReference.child("videos");

        videosRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                            String fileName = item.getName();
                            VideoModel videoModel = new VideoModel(downloadUrl.toString(),fileName );
                            videoListGen.add(videoModel);
                            videoAdapterGen.notifyDataSetChanged();

                        }).addOnFailureListener(exception -> {
                            Toast.makeText(getContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getContext(), "Failed to list videos", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchVideoUrlsDonna() {
        videoListDonna.clear();

        StorageReference videosRef = storageReference.child("videosDonna");

        videosRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                            String fileName = item.getName();
                            VideoModel videoModel = new VideoModel(downloadUrl.toString(),fileName);
                            videoListDonna.add(videoModel);
                            videoAdapterDonna.notifyDataSetChanged();
                        }).addOnFailureListener(exception -> {
                            Toast.makeText(getContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getContext(), "Failed to list Donna videos", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmationDialog(final VideoModel videoModel, final String targetFolder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Elimina File");
        builder.setMessage("Sei sicuro di voler eliminare questo file?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call a method to delete the file from Firebase and update the UI
                deleteVideo(videoModel, targetFolder);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteVideo(VideoModel videoModel, String targetFolder) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String videoUrl = videoModel.getVideoUrl();
        Uri uri = Uri.parse(videoUrl);
        String fileName = uri.getLastPathSegment();
        // Extract the file name from the video URL
        if (targetFolder != null && !targetFolder.isEmpty() && fileName.startsWith(targetFolder + "/")) {
            fileName = fileName.substring(targetFolder.length() + 1);
        }

        Log.d("VideoFragment", "Target Folder: " + targetFolder);
        Log.d("VideoFragment", "File Name: " + fileName);

        StorageReference videoReference = storageReference.child(targetFolder).child(fileName);

        videoReference.getMetadata().addOnSuccessListener(metadata -> {
            videoReference.delete().addOnSuccessListener(aVoid -> {
                if ("videosDonna".equals(targetFolder)) {
                    fetchVideoUrlsDonna();

// or
                    videoAdapterDonna.notifyDataSetChanged();
                } else {
                    fetchVideoUrlsGen();
                    videoAdapterGen.notifyDataSetChanged();
                }

                Toast.makeText(getContext(), "Video eliminato", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to delete video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "File does not exist", Toast.LENGTH_SHORT).show();
        });
    }






}
