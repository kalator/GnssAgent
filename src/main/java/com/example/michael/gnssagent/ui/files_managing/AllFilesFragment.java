package com.example.michael.gnssagent.ui.files_managing;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.michael.gnssagent.R;

import java.io.File;
import java.util.ArrayList;

public class AllFilesFragment extends Fragment {

    FilesAdapter filesAdapter;
    ListView lv;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all_files, container, false);

        lv = rootView.findViewById(R.id.allFilesListView);

        filesAdapter = new FilesAdapter(this.getActivity(), getAllFiles());

        lv.setAdapter(filesAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        filesAdapter = new FilesAdapter(this.getActivity(), getAllFiles());
        lv.setAdapter(filesAdapter);
        super.onResume();

    }

    private ArrayList<LogFile> getAllFiles() {
        ArrayList<LogFile> files = new ArrayList<>();

        File folder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "GnssAgent/");

        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                return null;
            }
        }

        LogFile f;

        File logFiles = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).getAbsolutePath(), "GnssAgent/");

        if (logFiles.exists() && logFiles.listFiles() != null) {
            for (File file : logFiles.listFiles()) {
                f = new LogFile(file);
                files.add(f);
            }
        }

        return files;
    }

    @Override
    public String toString() {
        return "All files";
    }
}
