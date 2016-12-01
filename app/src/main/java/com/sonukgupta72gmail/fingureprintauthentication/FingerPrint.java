package com.sonukgupta72gmail.fingureprintauthentication;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Context.FINGERPRINT_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FingerPrint implements FingerprintHandler.Callback{

    View fingerPrint;

    private static final String KEY_NAME = "example_key";
    private KeyStore keyStore;
    private Cipher cipher;
    Context context;
    public FingerPrint(Context ctx) {
        context = ctx;
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void fingerPrintAuthentication(){

//        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
//        final EditText password = new EditText(MainActivity.this);
//        ab.setView(password);
//        ab.setMessage("Set password");
//        ab.setPositiveButton("USE PASSWORD", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                //KEY_NAME = password.getText().toString();
//            }
//        });
//        ab.setNegativeButton("CANCEL", null);
//        ab.show();

        FingerprintManager.CryptoObject cryptoObject;
        FingerprintManager fingerprintManager;
        KeyguardManager keyguardManager;

        keyguardManager =
                (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
        fingerprintManager =
                (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);


        if (!keyguardManager.isKeyguardSecure()) {

            Toast.makeText(context,
                    "Lock screen security not enabled in Settings",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,
                    "Fingerprint authentication permission not enabled",
                    Toast.LENGTH_LONG).show();

            return;
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {

            // This happens when no fingerprints are registered.
            Toast.makeText(context,
                    "Register at least one fingerprint in Settings",
                    Toast.LENGTH_LONG).show();
            return;
        }

        generateKey();

        if (cipherInit()) {
            cryptoObject = new FingerprintManager.CryptoObject(cipher);
            FingerprintHandler helper = new FingerprintHandler(context);
            helper.startAuth(fingerprintManager, cryptoObject);
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey() {
        KeyGenerator keyGenerator;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
            throw new RuntimeException(
                    "Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @Override
    public void onAuthenticated() {

    }

    @Override
    public void onError() {
        goToBackup();
    }

    public void goToBackup() {

        FingerprintHandler helper = new FingerprintHandler(context);
        // Fingerprint is not used anymore. Stop listening for it.
        helper.stopListening();
    }

}
