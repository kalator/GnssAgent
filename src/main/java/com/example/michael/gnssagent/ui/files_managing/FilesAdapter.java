package com.example.michael.gnssagent.ui.files_managing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.michael.gnssagent.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class FilesAdapter extends BaseAdapter {

    private Context c;
    private ArrayList<LogFile> logFiles;
    LayoutInflater inflater;

    public FilesAdapter(Context c, ArrayList<LogFile> logFiles) {
        this.c = c;
        this.logFiles = logFiles;

        sortFiles();
    }

    @Override
    public int getCount() {
        return logFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return logFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.model, parent, false);
        }

        TextView nameTxt = convertView.findViewById(R.id.nameTxt);

        final String name = logFiles.get(position).getName();
        nameTxt.setText(name);

        final View cv = convertView;

        cv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PopupMenu popup = new PopupMenu(c, cv, Gravity.NO_GRAVITY);
                popup.getMenuInflater().inflate(R.menu.file_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.read:
                                openFile(new File(logFiles.get(position).getPath()));
                                return true;
                            case R.id.share:
                                shareFile(new File(logFiles.get(position).getPath()));
                                return true;
                            case R.id.delete:
                                deleteFile(new File(logFiles.get(position).getPath()),
                                        logFiles.get(position).getName());
                                return true;
                            default:
                                return true;
                        }

                    }
                });
                popup.show();

                return true;
            }
        });


        return convertView;
    }

    private void openFile(File file) {

        Uri fileURI = FileProvider.getUriForFile(c,
                c.getPackageName() + ".provider", file);
        Intent readIntent = new Intent(Intent.ACTION_VIEW);
        readIntent.setDataAndType(fileURI, "text/plain");
        readIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        c.startActivity(readIntent);
    }

    private void shareFile(File fileToShare) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("*/*");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, c.getString(R.string.send_log));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "");
        // attach the file
        Uri fileURI = FileProvider.getUriForFile(c,
                "com.example.michael.gnssagent.provider", fileToShare);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileURI);
        c.startActivity(Intent.createChooser(sharingIntent, "Share log"));
    }

    private void deleteFile(final File fileToDelete, final String fileName) {
        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setTitle(c.getString(R.string.delete_file));
        alert.setMessage(c.getString(R.string.delete_file_question));
        alert.setCancelable(false);
        alert.setPositiveButton(c.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //               fileAdapter.remove(fileAdapter.getItem(fileId));
                fileToDelete.delete();
                fileToDelete.delete();
                for (LogFile file : logFiles) {
                    if (file.getName().equals(fileName)){
                        logFiles.remove(file);
                        break;
                    }
                }
                notifyDataSetChanged();
                Toast.makeText(c, c.getString(R.string.log_removed), Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton(c.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alert.create();
        alert.show();

    }

    private void sortFiles() {
        try {
            this.logFiles.sort(new Comparator<LogFile>() {
                @Override
                public int compare(LogFile t1, LogFile t2) {
                    return t2.getName().compareTo(t1.getName());
                }
            });
        }
        catch (NullPointerException e) {
            // do nothing
        }
    }
}
