package com.example.michael.gnssagent.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.example.michael.gnssagent.R;
import com.example.michael.gnssagent.data_processing.Constants;

public abstract class BaseActivity extends AppCompatActivity {

    protected static int log_time_format = Constants.GPS_WEEK_SECONDS;
    protected static boolean integerize_time = true;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {

            // go to settings
            case R.id.app_settings_details:
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
                break;

            // change time format that is logged
            case R.id.time_format:

                final int last_time_format = log_time_format;

                final String []items = new String[]
                        {this.getString(R.string.gps_standard),
                                this.getString(R.string.gps_week_seconds)};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder .setTitle(this.getString(R.string.log_time_format))
                        .setSingleChoiceItems(items, log_time_format, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                log_time_format = i;
                            }
                        })
                        .setCancelable(false)
                        .setPositiveButton(this.getString(R.string.save), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // do nothing
                            }
                        })
                        .setNeutralButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                log_time_format = last_time_format;
                                dialogInterface.cancel();
                            }
                        })
                        .create()
                        .show();
                break;
            case R.id.set_integerize:
                if (item.isChecked()) {
                    integerize_time = false;
                    item.setChecked(false);
                } else {
                    integerize_time = true;
                    item.setChecked(true);
                }
                break;

                default:
                    break;
        }

        return super.onOptionsItemSelected(item);
    }

}
