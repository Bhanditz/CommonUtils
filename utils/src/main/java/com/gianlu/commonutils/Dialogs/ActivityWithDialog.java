package com.gianlu.commonutils.Dialogs;

import android.app.Dialog;

import com.gianlu.commonutils.NightlyActivity;
import com.gianlu.commonutils.Toaster;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public abstract class ActivityWithDialog extends NightlyActivity {
    private Dialog mDialog;

    public final void showDialog(@NonNull Dialog dialog) {
        mDialog = dialog;
        DialogUtils.showDialogValid(this, mDialog);
    }

    public final void showDialog(@NonNull AlertDialog.Builder dialog) {
        DialogUtils.showDialogValid(this, dialog, dialog1 -> mDialog = dialog1);
    }

    public final void showDialog(@NonNull DialogFragment dialog) {
        FragmentManager manager = getSupportFragmentManager();
        dialog.show(manager, null);
        mDialog = dialog.getDialog();
    }

    public final void showToast(@NonNull Toaster toaster) {
        toaster.show(this);
    }

    public final void dismissDialog() {
        if (mDialog != null) mDialog.dismiss();
        mDialog = null;
    }

    public final void showProgress(@StringRes int res) {
        showDialog(DialogUtils.progressDialog(this, res));
    }

    public final boolean hasVisibleDialog() {
        return mDialog != null && mDialog.isShowing();
    }

    @Override
    @CallSuper
    protected void onDestroy() {
        super.onDestroy();
        dismissDialog();
    }
}
