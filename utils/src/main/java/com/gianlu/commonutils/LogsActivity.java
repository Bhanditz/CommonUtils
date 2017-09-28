package com.gianlu.commonutils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

public class LogsActivity extends AppCompatActivity {
    private static final int DELETE_LOGS_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        setTitle(R.string.log_activity_title);

        final FrameLayout container = findViewById(R.id.logs_container);
        final Spinner spinner = findViewById(R.id.logs_spinner);
        final RecyclerView list = findViewById(R.id.logs_list);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        final List<Logging.LogFile> logFiles = Logging.listLogFiles(this, false);

        if (logFiles.isEmpty()) {
            MessageLayout.show(container, R.string.noLogs, R.drawable.ic_info_outline_black_48dp);
            spinner.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            return;
        }

        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, logFiles));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    list.setAdapter(new Logging.LogLineAdapter(LogsActivity.this, Logging.getLogLines(LogsActivity.this, logFiles.get(i)), new Logging.LogLineAdapter.IAdapter() {
                        @Override
                        public void onLogLineSelected(Logging.LogLine line) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            if (clipboard != null) {
                                ClipData clip = ClipData.newPlainText("stack trace", line.message);
                                clipboard.setPrimaryClip(clip);
                                Toaster.show(LogsActivity.this, Toaster.Message.COPIED_TO_CLIPBOARD);
                            }
                        }
                    }));
                } catch (IOException ex) {
                    Logging.logMe(LogsActivity.this, ex);
                    onBackPressed();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                onBackPressed();
            }
        });

        spinner.setSelection(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, DELETE_LOGS_ID, Menu.NONE, R.string.deleteAllLogs);
        return true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_LOGS_ID:
                File files[] = getFilesDir().listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        return s.toLowerCase().endsWith(".log");
                    }
                });

                for (File logFile : files)
                    logFile.delete();

                Toaster.show(this, Toaster.Message.LOGS_DELETED);
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
