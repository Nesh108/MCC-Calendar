package com.example.nesh.mcc_calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.SQLException;

public class SettingsActivity extends AppCompatActivity {

    private DBHandler dbHandler = new DBHandler(this);

    EditText userEdit;
    EditText passEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        showIcon();

        userEdit = (EditText) findViewById(R.id.usernameEdit);
        passEdit = (EditText) findViewById(R.id.passwordEdit);

        userEdit.setText(PrefUtils.getFromPrefs(SettingsActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__"));
        passEdit.setText(PrefUtils.getFromPrefs(SettingsActivity.this, PrefUtils.PREFS_LOGIN_PASSWORD_KEY, "__UNKNOWN__"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(SettingsActivity.this);
        View promptsView = li.inflate(R.layout.about_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                SettingsActivity.this);

        // set xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        // set dialog message
        alertDialogBuilder
                .setCancelable(true)
                .setNegativeButton("Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void clearDB(View v) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:

                        try {
                            dbHandler.open();
                            dbHandler.clerDatabase(SettingsActivity.this);
                            Toast.makeText(SettingsActivity.this, "Database Cleared", Toast.LENGTH_SHORT).show();

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setMessage("Are you sure you want to clear the local Database? Any unsaved changes will be lost!").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public void saveChanges(View v) {

        boolean user_change = false;

        if (!userEdit.getText().toString().equals(PrefUtils.getFromPrefs(SettingsActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, "__UNKNOWN__"))) {
            PrefUtils.saveToPrefs(SettingsActivity.this, PrefUtils.PREFS_LOGIN_USERNAME_KEY, userEdit.getText().toString());
            user_change = true;
        }
        if (!passEdit.getText().toString().equals(PrefUtils.getFromPrefs(SettingsActivity.this, PrefUtils.PREFS_LOGIN_PASSWORD_KEY, "__UNKNOWN__")))
            PrefUtils.saveToPrefs(SettingsActivity.this, PrefUtils.PREFS_LOGIN_PASSWORD_KEY, passEdit.getText().toString());

        if (user_change) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:

                            clearDB(null);
                            goToMainActivity(null);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            goToMainActivity(null);
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setMessage("The user has changed. Do you want to clear the local Database?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        } else
            goToMainActivity(v);


    }

    public void goToMainActivity(View v) {
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
    }

    private void showIcon() {
        // Show icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }
}
