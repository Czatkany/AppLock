package hu.uniobuda.nik.thisnameistoolon;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CodeWindowActivity extends Activity {

    private String currentApp;
    private Button codeButton;
    private Button cancelButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.code_layout);
        currentApp = FoundApp();
        codeButton = (Button) findViewById(R.id.enterCode);
        cancelButton = (Button) findViewById(R.id.cancelCode);
        OnClickCode();
        OnClickCancel();
    }

    //When a selected application is launched, this activity pops up, and asks for the code.
    //If the user gives the right code, the selected app starts as well as a background service.
    public void OnClickCode() {
        codeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String code = GetCode();
                final EditText codeText = (EditText) findViewById(R.id.unBlockText);
                String text = codeText.getText().toString();
                if (text.length() > 0) {
                    if (text.matches(code)) {
                        PackageManager pmi = getPackageManager();
                        Intent intent;
                        intent = pmi.getLaunchIntentForPackage(currentApp);
                        if (intent != null) {
                            startActivity(intent);
                            startService(new Intent(CodeWindowActivity.this, RestartAppLockerService.class));
                        }
                        finish();

                    } else {
                        Toast.makeText(getBaseContext(), R.string.not_matches, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), R.string.empty, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Cancel button
    public void OnClickCancel() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(CodeWindowActivity.this, AppLockerService.class));
                finish();
            }
        });
    }

    //If the ForegroundApp() finds a match, it sends the found application's packagename
    //through an intent to this function
    public String FoundApp() {
        String selectedApp;
        Intent intent = getIntent();
        selectedApp = intent.getStringExtra("foundApp");
        return selectedApp;
    }

    //This function gets the user defined code from the saved file.
    public String GetCode() {
        StringBuilder data = new StringBuilder("");
        try {
            FileInputStream fIn = openFileInput ( "codeFile.txt" ) ;
            InputStreamReader isr = new InputStreamReader ( fIn ) ;
            BufferedReader buffReader = new BufferedReader(isr);

            String readString = buffReader.readLine();
            while ( readString != null ) {
                data.append(readString);
                readString = buffReader.readLine();
            }

            isr.close ( ) ;
        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }
        return data.toString();
    }
}
