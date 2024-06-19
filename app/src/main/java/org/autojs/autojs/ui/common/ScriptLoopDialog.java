package org.autojs.autojs.ui.common;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.stardust.app.DialogUtils;
import com.stardust.app.GlobalAppContext;

import org.autojs.autojs.model.script.ScriptFile;
import org.autojs.autojs.model.script.Scripts;
import org.autojs.autoxjs.R;

/**
 * Created by Stardust on 2017/7/8.
 */

public class ScriptLoopDialog {

    private final ScriptFile mScriptFile;
    private final MaterialDialog mDialog;

    private EditText mLoopTimes;

    private EditText mLoopInterval;

    private EditText mLoopDelay;


    public ScriptLoopDialog(Context context, ScriptFile file) {
        mScriptFile = file;
        View view = View.inflate(context, R.layout.dialog_script_loop, null);
        mDialog = new MaterialDialog.Builder(context)
                .title(R.string.text_run_repeatedly)
                .customView(view, true)
                .positiveText(R.string.ok)
                .onPositive((dialog, which) -> startScriptRunningLoop())
                .build();
        bindView(view);
    }

    private void startScriptRunningLoop() {
        try {
            int loopTimes = Integer.parseInt(mLoopTimes.getText().toString());
            float loopInterval = Float.parseFloat(mLoopInterval.getText().toString());
            float loopDelay = Float.parseFloat(mLoopDelay.getText().toString());
            Scripts.INSTANCE.runRepeatedly(mScriptFile, loopTimes, (long) (1000L * loopDelay), (long) (loopInterval * 1000L));
        } catch (NumberFormatException e) {
            GlobalAppContext.toast(R.string.text_number_format_error);
        }
    }

    public ScriptLoopDialog windowType(int windowType) {
        Window window = mDialog.getWindow();
        if (window != null) {
            window.setType(windowType);
        }
        return this;
    }

    public void show() {
        DialogUtils.showDialog(mDialog);
    }

    private void bindView(@NonNull View bindSource) {
        mLoopTimes = bindSource.findViewById(R.id.loop_times);
        mLoopInterval = bindSource.findViewById(R.id.loop_interval);
        mLoopDelay = bindSource.findViewById(R.id.loop_delay);
    }
}