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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static hu.uniobuda.nik.thisnameistoolon.R.id.root_Layout;

public class LockerActivity extends Activity {
    private ArrayList<String> appName = new ArrayList<String>();
    private ArrayList<String> packName = new ArrayList<String>();
    private ArrayList<CheckBox> checkList = new ArrayList<CheckBox>();
    private ArrayList<String> selectedApps = new ArrayList<String>();
    private ListView Apps;
    private CheckBox Check;
    private Button okButton;
    private Button codeButton;
    private Button exitButton;
    private String code;
    private boolean codeSetted = false;
    private EditText editText;
    private Context context = LockerActivity.this;
    private AppListAdapter adapter;
    private AppFinderService appFinderService;

    protected void onCreate(Bundle savedInstanceState) {
        code = new String();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.root_layout);
        buildView(R.layout.app_list);
        buildView(R.layout.activity_locker);
        Apps = (ListView) findViewById(R.id.AppList);
        Check = (CheckBox) findViewById(R.id.CheckBox);
        okButton = (Button) findViewById(R.id.OK);
        codeButton = (Button) findViewById(R.id.ChangeCode);
        exitButton = (Button) findViewById(R.id.Exit);
        getPackages();
        okButtonListener(codeSetted);
        codeButtonListener();
        exitButtonListener();
        //This adapter set the list in the main window. The adapters getSelectedApps getter set the selected apps.
        adapter = new AppListAdapter(context, android.R.layout.simple_list_item_1, appName, checkList, packName);
        Apps.setAdapter(adapter);
    }

    private void buildView(int resource){ //This function build the view from the xml-s
        RelativeLayout my_root = (RelativeLayout) findViewById(root_Layout);
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(getBaseContext().LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(resource, null);
        RelativeLayout A = new RelativeLayout(this);
        A.addView(v);
        my_root.addView(A);
    }

    private void getPackages(){ //This function gets the installed packages
        PackageManager packageManager = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = packageManager.queryIntentActivities(mainIntent, 0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(packageManager));
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        for(int i=0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            ApplicationInfo a = p.applicationInfo;
            String name = packageManager.getApplicationLabel(a).toString();
            // skip system apps if they shall not be included
            if((a.flags & ApplicationInfo.FLAG_SYSTEM) == 1 || name.matches("AppLock")) {
                continue;
            }
            packName.add(p.packageName);
            appName.add(name);
            checkList.add(new CheckBox(getBaseContext()));
        }
    }
    //there seems to be an unknown issue witch prevents the app to apply the changes if the ok button is pressed the first time. It works perfectly on the second click
    private void okButtonListener(final boolean codeSetted){ //This function set the ok buttons events
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            //This function launch the AppFinderService
            //The code part is not ready, so it only blocks the selected applications.
            public void onClick(View v) {
                saveArray();
                Intent i = new Intent(LockerActivity.this, AppFinderService.class);
                LockerActivity.this.startService(i);
                finish();
            }
        });
    }
    //With this function it is possible to set the code later in a dialog box.
    private void codeButtonListener(){ //This function set the code buttons events.
        codeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(LockerActivity.this, 4);
                LayoutInflater inflater = LockerActivity.this.getLayoutInflater();
                final View layout = inflater.inflate(R.layout.input_window, null);
                builder.setView(layout);
                builder.setTitle("Kód megadása");
                builder.setPositiveButton("Beállít", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) layout.findViewById(R.id.editCodeText);
                        String emptyText = "";
                        emptyText = editText.getText().toString().trim();
                        if (emptyText.isEmpty() || emptyText.length() == 0 || emptyText.equals("") || emptyText == null) {
                            Toast.makeText(context, "Nincs érték!", Toast.LENGTH_SHORT).show();
                        } else {
                            if (editText.getText().toString().length() == 4) {
                                code = editText.getText().toString();
                                //codeSetted = true;
                                Toast.makeText(context, "Érték beállítva!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Nem megfelelő a hossz!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                builder.setNegativeButton("Mégse", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                Dialog editTextDialog = builder.create();
                editTextDialog.show();
            }
        });
    }
    private void exitButtonListener(){ //This function sets the exit buttons events, and kills the background service
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(LockerActivity.this, AppFinderService.class);
                LockerActivity.this.stopService(i);

                System.exit(0);
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.locker, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveArray(){//This function saves the selected apps
        selectedApps = adapter.getSelectedApps();
        String PrefFileName = "MyPrefFile";
        SharedPreferences sp = getSharedPreferences(PrefFileName, 0);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Status_size", selectedApps.size());
        for(int i=0;i<selectedApps.size();i++)
        {
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, selectedApps.get(i));
        }
        mEdit1.apply();
    }
}
