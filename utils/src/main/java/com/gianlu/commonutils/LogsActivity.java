package com.gianlu.commonutils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Keep
public class LogsActivity extends AppCompatActivity {
    private static final int DELETE_LOGS_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.log_activity_title);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int dpPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        layout.setPadding(dpPadding, dpPadding, dpPadding, dpPadding);

        Spinner logs = new Spinner(this);
        logs.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(logs);
        final ListView log = new ListView(this);
        LinearLayout.LayoutParams logParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        logParams.setMargins(0, 16, 0, 0);
        log.setLayoutParams(logParams);
        layout.addView(log);

        setContentView(layout);

        log.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("stack trace", ((LoglineItem) adapterView.getItemAtPosition(i)).getMessage());
                clipboard.setPrimaryClip(clip);

                CommonUtils.UIToast(LogsActivity.this, CommonUtils.ToastMessage.COPIED_TO_CLIPBOARD);
            }
        });

        final File files[] = getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(".log");
            }
        });

        List<String> spinnerList = new ArrayList<>();
        for (File logFile : files) {
            spinnerList.add(logFile.getName());
        }

        logs.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerList));
        logs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                List<LoglineItem> logLines = new ArrayList<>();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(adapterView.getItemAtPosition(i).toString())));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("--ERROR--")) {
                            logLines.add(new LoglineItem(TYPE.ERROR, line.replace("--ERROR--", "")));
                        } else if (line.startsWith("--INFO--")) {
                            logLines.add(new LoglineItem(TYPE.INFO, line.replace("--INFO--", "")));
                        }
                    }
                } catch (IOException ex) {
                    CommonUtils.UIToast(LogsActivity.this, CommonUtils.ToastMessage.FATAL_EXCEPTION, ex);
                    onBackPressed();
                }

                log.setAdapter(new LoglineAdapter(logLines));
                log.setSelection(log.getCount() - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                finishActivity(0);
            }
        });

        logs.setSelection(logs.getCount() - 1);
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

                for (File logFile : files) {
                    logFile.delete();
                }

                CommonUtils.UIToast(this, CommonUtils.ToastMessage.LOGS_DELETED);
                recreate();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public enum TYPE {
        INFO,
        WARNING,
        ERROR
    }

    private class LoglineItem {
        private final TYPE type;
        private final String message;

        LoglineItem(TYPE type, String message) {
            this.type = type;
            this.message = message;
        }

        TYPE getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }
    }

    private class LoglineAdapter extends BaseAdapter {
        private final List<LoglineItem> objs;

        LoglineAdapter(List<LoglineItem> objs) {
            this.objs = objs;
        }

        @Override
        public int getCount() {
            return objs.size();
        }

        @Override
        public LoglineItem getItem(int i) {
            return objs.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout linearLayout = new LinearLayout(LogsActivity.this);
            linearLayout.setPadding(12, 12, 12, 12);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            LoglineItem item = getItem(position);

            TextView type = new TextView(LogsActivity.this);
            type.setTypeface(Typeface.DEFAULT_BOLD);
            switch (item.getType()) {
                case INFO:
                    type.setText(R.string.infoTag);
                    type.setTextColor(Color.BLACK);
                    break;
                case WARNING:
                    type.setText(R.string.warningTag);
                    type.setTextColor(Color.YELLOW);
                    break;
                case ERROR:
                    type.setText(R.string.errorTag);
                    type.setTextColor(Color.RED);
                    break;
            }
            linearLayout.addView(type);
            linearLayout.addView(CommonUtils.fastTextView(LogsActivity.this, item.getMessage()));


            return linearLayout;
        }
    }
}
