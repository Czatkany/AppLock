package hu.uniobuda.nik.thisnameistoolon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LockerActivity extends Activity {
    private ArrayList<String> appName = new ArrayList<String>();
    private ArrayList<String> packName = new ArrayList<String>();
    private ArrayList<CheckBox> checkList = new ArrayList<CheckBox>();
    private ArrayList<String> selectedApps = new ArrayList<String>();
    private Button okButton;
    private Button codeButton;
    private Button exitButton;
    private String code;
    private boolean codeSetted;
    private Context context = LockerActivity.this;
    private AppListAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.root_layout);
        buildView(R.layout.app_list);
        buildView(R.layout.activity_locker);
        ListView apps = (ListView) findViewById(R.id.AppList);
        okButton = (Button) findViewById(R.id.OK);
        codeButton = (Button) findViewById(R.id.ChangeCode);
        exitButton = (Button) findViewById(R.id.Exit);
        getPackages();
        okButtonListener();
        codeButtonListener();
        exitButtonListener();
        adapter = new AppListAdapter(context, android.R.layout.simple_list_item_1, appName, checkList, packName);
        apps.setAdapter(adapter);
    }

    //This function builds the view from the xml-s.
    private void buildView(int resource) {
        RelativeLayout my_root = (RelativeLayout) findViewById(R.id.root_Layout);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(resource, null);
        RelativeLayout A = new RelativeLayout(this);
        A.addView(v);
        my_root.addView(A);
    }

    //This function gets the installed packages and populates the checkbox list
    private void getPackages() {
        PackageManager packageManager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = packageManager.queryIntentActivities(mainIntent, 0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(packageManager));
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        for (PackageInfo p : packs) {
            ApplicationInfo a = p.applicationInfo;
            String name = packageManager.getApplicationLabel(a).toString();
            // skip system apps if they shall not be included
            if ((a.flags & ApplicationInfo.FLAG_SYSTEM) == 1 || name.matches("AppLockV4")) {
                continue;
            }
            packName.add(p.packageName);
            appName.add(name);
            checkList.add(new CheckBox(getBaseContext()));
        }
    }

    //This function sets the ok button event.
    //If the code is set, and the apps are selected, it starts the app locker service.
    private void okButtonListener() {
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!codeSetted) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LockerActivity.this, 4);
                    alertDialogBuilder.setTitle(R.string.dialog_title);
                    alertDialogBuilder.setMessage(R.string.dialog_message);
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    saveArray();
                    if (selectedApps.size() > 0) {
                        startService(new Intent(AppLockerService.TAG));
                        finish();
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LockerActivity.this, 4);
                        alertDialogBuilder.setTitle(R.string.dialog_title);
                        alertDialogBuilder.setMessage(R.string.dialog_message2);
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }


                }
            }
        });
    }

    //This function sets the code button event
    //When the application is started, the user have to set the code first
    //The code button builds a dialogue box, where the user can input the code.
    private void codeButtonListener() {
        codeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(LockerActivity.this, 4);
                LayoutInflater inflater = LockerActivity.this.getLayoutInflater();
                final View layout = inflater.inflate(R.layout.input_window, null);
                builder.setView(layout);
                builder.setTitle(R.string.dialog_title2);
                builder.setNegativeButton(R.string.enterCode, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) layout.findViewById(R.id.editCodeText);
                        String emptyText = editText.getText().toString().trim();
                        if (emptyText.length() == 0) {
                            Toast.makeText(context, R.string.dialog_message, Toast.LENGTH_SHORT).show();
                        } else {
                            if (editText.getText().toString().length() == 4) {
                                code = editText.getText().toString();
                                saveCode(code);
                                codeSetted = true;
                                Toast.makeText(context, R.string.code_setted, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, R.string.code_lenght_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                builder.setPositiveButton(R.string.cancelCode, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                Dialog editTextDialog = builder.create();
                editTextDialog.show();
            }
        });
    }

    //This function sets the exit button event.
    //The app locker service runs in the background
    //until the user restart the app, and shuts it down by pressing this button.
    private void exitButtonListener() {
        exitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(LockerActivity.this, AppLockerService.class);
                LockerActivity.this.stopService(i);
                System.exit(0);
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.locker, menu);
        return true;
    }

    //This function handles the list elements event(check, uncheck)
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    //When the applications are selected,
    //the list of those applications (retrieved from the applistadapter)
    //is saved to a file before the app lock service starts.
    public boolean saveArray() {
        selectedApps = adapter.getSelectedApps();
        String PrefFileName = "SavedAppList";
        SharedPreferences sp = getSharedPreferences(PrefFileName, 0);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Status_size", selectedApps.size());
        for (int i = 0; i < selectedApps.size(); i++) {
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, selectedApps.get(i));
        }
        return mEdit1.commit();
    }

    //This function saves the code to a file.
    public void saveCode(String codeS) {
        String filename = "codeFile.txt";
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(codeS.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
