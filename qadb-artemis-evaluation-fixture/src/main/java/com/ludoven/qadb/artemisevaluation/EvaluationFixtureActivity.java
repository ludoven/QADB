package com.ludoven.qadb.artemisevaluation;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A no-network, no-permission fixture for AR-06. Its state lives only in this
 * debuggable application's private storage so host-side truth collection can
 * use `adb shell run-as` without observing personal device data.
 */
public final class EvaluationFixtureActivity extends Activity {
    private static final String STATE_FILE = "state.json";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private EditText input;
    private TextView delayedLabel;
    private String selectedCandidate = "none";
    private String delayedState = "idle";
    private int eventCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int padding = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(padding, padding, padding, padding);

        root.addView(label("QADB Artemis 独立评测夹具", 22));
        root.addView(label("仅合成数据；不会访问账户、网络、支付或设备权限。", 14));
        root.addView(label("测试隐私标签：虚构账户 0000，仅允许界面读取。", 14));

        input = new EditText(this);
        input.setId(View.generateViewId());
        input.setHint("输入用于 AR-06 的中文测试文本");
        input.setSingleLine(false);
        input.setMinLines(3);
        root.addView(input, matchWidth());
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { saveState(); }
            @Override public void afterTextChanged(Editable s) { }
        });

        root.addView(button("候选项一", v -> selectCandidate("candidate-1")), matchWidth());
        root.addView(button("候选项二", v -> selectCandidate("candidate-2")), matchWidth());
        root.addView(button("显示测试 Toast", v -> {
            eventCount++;
            Toast.makeText(this, "QADB 延迟测试提示", Toast.LENGTH_SHORT).show();
            saveState();
        }), matchWidth());

        delayedLabel = label("延迟状态：idle", 16);
        root.addView(delayedLabel, matchWidth());
        root.addView(button("开始延迟状态", v -> startDelayedState()), matchWidth());
        root.addView(button("重置合成测试数据", v -> resetState()), matchWidth());
        setContentView(root);
        saveState();
    }

    private TextView label(String text, int size) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setPadding(0, dp(6), 0, dp(6));
        return view;
    }

    private Button button(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout.LayoutParams matchWidth() {
        return new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private void selectCandidate(String candidate) {
        selectedCandidate = candidate;
        eventCount++;
        saveState();
    }

    private void startDelayedState() {
        delayedState = "waiting";
        delayedLabel.setText("延迟状态：waiting");
        eventCount++;
        saveState();
        handler.postDelayed(() -> {
            delayedState = "completed";
            delayedLabel.setText("延迟状态：completed");
            eventCount++;
            saveState();
        }, 1500L);
    }

    private void resetState() {
        input.setText("");
        selectedCandidate = "none";
        delayedState = "idle";
        eventCount = 0;
        delayedLabel.setText("延迟状态：idle");
        saveState();
    }

    private void saveState() {
        if (input == null) return;
        try {
            JSONObject state = new JSONObject();
            state.put("text", input.getText().toString());
            state.put("selectedCandidate", selectedCandidate);
            state.put("delayedState", delayedState);
            state.put("eventCount", eventCount);
            try (FileOutputStream output = openFileOutput(STATE_FILE, MODE_PRIVATE)) {
                output.write(state.toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception error) {
            throw new IllegalStateException("Unable to store isolated evaluation state", error);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
