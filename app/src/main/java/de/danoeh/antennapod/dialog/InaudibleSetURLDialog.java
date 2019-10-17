package de.danoeh.antennapod.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.preferences.InaudiblePreferences;

/**
 * Creates a dialog that lets the user change the URl.
 */
public class InaudibleSetURLDialog {

    private InaudibleSetURLDialog(){}

    private static final String TAG = "InaudibleSetURLDialog";

    public static AlertDialog createDialog(final Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        final EditText et = new EditText(context);
        et.setText(InaudiblePreferences.getURL());
        et.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        dialog.setTitle(R.string.pref_inaudible_url)
                .setView(setupContentView(context, et))
                .setPositiveButton(R.string.confirm_label, (dialog1, which) -> {
                    final Editable e = et.getText();
                    if (e != null) {
                        InaudiblePreferences.setURL(e.toString());
                    }
                    dialog1.dismiss();
                })
                .setNegativeButton(R.string.cancel_label, (dialog1, which) -> dialog1.cancel())
                .setNeutralButton(R.string.pref_inaudible_seturl_use_default, (dialog1, which) -> {
                    InaudiblePreferences.setURL("https://example.com/");
                    dialog1.dismiss();
                })
                .setCancelable(true);
        return dialog.show();
    }

    private static View setupContentView(Context context, EditText et) {
        LinearLayout ll = new LinearLayout(context);
        ll.addView(et);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) et.getLayoutParams();
        if (params != null) {
            params.setMargins(8, 8, 8, 8);
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        return ll;
    }
}
