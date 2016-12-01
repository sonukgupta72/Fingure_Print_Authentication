package com.sonukgupta72gmail.fingureprintauthentication;

import android.Manifest;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    EditText emailAdd, password;
    TextView errorText;
    Button login;
    LinearLayout loginLayout;
    RelativeLayout mainContent;
    ImageView fingerPrintImg;
    FingerprintManager fingerprintManager;
    KeyguardManager keyguardManager;
    Dialog dialog;
    TextView cancelTextView, fnStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailAdd = (EditText) findViewById(R.id.editText);
        password = (EditText) findViewById(R.id.editText2);
        errorText = (TextView) findViewById(R.id.textView);
        login = (Button) findViewById(R.id.loginButton);
        loginLayout = (LinearLayout) findViewById(R.id.loginLayout);
        mainContent = (RelativeLayout) findViewById(R.id.mainContent);
        //fingerPrintImg = (ImageView) findViewById(R.id.fingerPrintImg);

        keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager =
                (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || (!keyguardManager.isKeyguardSecure()) || (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) || (!fingerprintManager.hasEnrolledFingerprints())) {
            //mainContent.removeAllViews();
            dialog = new Dialog(this);
            dialog.setContentView(R.layout.fn_alert);
            cancelTextView = (TextView) dialog.findViewById(R.id.cancelText);
            fnStatusTextView = (TextView) dialog.findViewById(R.id.fingerprint_status);
            cancelTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FingerPrint(MainActivity.this).goToBackup();
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(false);
            new FingerPrint(this).fingerPrintAuthentication();
            dialog.show();
        } else {
            fingerPrintImg.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailAdd.getText().toString().equals("sonu@gmail.com") && password.getText().toString().equals("12345678")){
                    //emailAdd.setError("");
                    errorText.setVisibility(View.GONE);
                    callFragmentHome();

                }else{
                    errorText.setVisibility(View.VISIBLE);
                }

            }
        });
    }



    public void callFragmentHome(){

        dialog.dismiss();
        mainContent.removeAllViews();
        Fragment homeFragment = new HomeFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.mainContent, homeFragment)
                .addToBackStack("homeFragment")
                .commit();
    }
}
