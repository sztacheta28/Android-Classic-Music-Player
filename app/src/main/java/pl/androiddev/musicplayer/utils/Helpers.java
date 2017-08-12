package pl.androiddev.musicplayer.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import pl.androiddev.musicplayer.R;

public class Helpers {
    public static void showAbout(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog_about");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        new AboutDialog().show(ft, "dialog_about");
    }

    public static String getATEKey(Context context) {
        return "dark_theme";
    }

    public static class AboutDialog extends DialogFragment {
        String urlgithub = "https://github.com/sztacheta28";

        public AboutDialog() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout aboutBodyView = (LinearLayout) layoutInflater.inflate(R.layout.layout_about_dialog, null);

            TextView appname = (TextView) aboutBodyView.findViewById(R.id.app_version_name);
            TextView github = (TextView) aboutBodyView.findViewById(R.id.github);

            github.setPaintFlags(github.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


            github.setOnClickListener(v -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(urlgithub));
                startActivity(i);
            });

            appname.setText(getString(R.string.app_name));

            return new AlertDialog.Builder(getActivity())
                    .setView(aboutBodyView)
                    .create();
        }

    }

}
