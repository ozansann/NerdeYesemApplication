package com.ozproduction.nerdeyesemapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.Manifest;
import android.os.Build;
import android.os.CancellationSignal;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context context;
    LoginActivityInterface listener;


    public FingerprintHandler(Context mContext,LoginActivityInterface listener) {
        context = mContext;
        this.listener = listener;
    }

    //Implement the startAuth method, which is responsible for starting the fingerprint authentication process
    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    //onAuthenticationError is called when a fatal error has occurred. It provides the error code and error message as its parameters
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Log.d("FingerprintHandler", String.valueOf(R.string.AuthenticationError) + errString);
    }

    @Override
    //onAuthenticationFailed is called when the fingerprint doesn’t match with any of the fingerprints registered on the device
    public void onAuthenticationFailed() {
        Toast.makeText(context, String.valueOf(R.string.AuthenticationFailed), Toast.LENGTH_LONG).show();
    }

    @Override
    /*onAuthenticationHelp is called when a non-fatal error has occurred. This method provides additional information about the error,
    so to provide the user with as much feedback as possible I’m incorporating this information into my toast*/
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Toast.makeText(context, String.valueOf(R.string.AuthenticationFailed) + helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    //onAuthenticationSucceeded is called when a fingerprint has been successfully matched to one of the fingerprints stored on the user’s device
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {
            listener.LoginSuccessful();
    }
}