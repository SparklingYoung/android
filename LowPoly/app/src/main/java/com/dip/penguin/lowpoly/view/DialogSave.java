package com.dip.penguin.lowpoly.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.dip.penguin.lowpoly.R;

public class DialogSave extends DialogFragment {

    public interface OnDialogSaveListener{
        void onDialogSave();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_save)
                .setNegativeButton(R.string.dialog_button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        getActivity().finish();
                    }
                }).setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                OnDialogSaveListener listener = (OnDialogSaveListener)getActivity();
                listener.onDialogSave();
            }
        });

        return builder.create();
    }
}
