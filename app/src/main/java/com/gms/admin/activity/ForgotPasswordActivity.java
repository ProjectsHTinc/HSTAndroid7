package com.gms.admin.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gms.admin.R;
import com.gms.admin.helper.AlertDialogHelper;
import com.gms.admin.helper.ProgressDialogHelper;
import com.gms.admin.interfaces.DialogClickListener;
import com.gms.admin.servicehelpers.ServiceHelper;
import com.gms.admin.serviceinterfaces.IServiceListener;
import com.gms.admin.utils.CommonUtils;
import com.gms.admin.utils.GMSConstants;
import com.gms.admin.utils.GMSValidator;
import com.gms.admin.utils.PreferenceStorage;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener, IServiceListener, DialogClickListener {

    private static final String TAG = ForgotPasswordActivity.class.getName();

    private TextInputEditText edtEmailOrMobileNo;
    private Button btnSubmit;
    private ProgressDialogHelper progressDialogHelper;
    private ServiceHelper serviceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtEmailOrMobileNo = (TextInputEditText) findViewById(R.id.email_or_phone);
        btnSubmit = (Button) findViewById(R.id.signin);
        btnSubmit.setOnClickListener(this);

        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isNetworkAvailable(getApplicationContext())) {
            if (v == btnSubmit) {
                String username = edtEmailOrMobileNo.getText().toString();
                if ((GMSValidator.checkNullString(username))) {


                    progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
                    try {
                        JSONObject jsonObject = new JSONObject();

                        jsonObject.put(GMSConstants.KEY_USER_NAME, username);
                        String url = PreferenceStorage.getClientUrl(this) + GMSConstants.FORGOT_PASSWORD;
                        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    AlertDialogHelper.showSimpleAlertDialog(getApplicationContext(), "Email ID/Mobile number is required tot reset password");
                }
            }
        } else {
            AlertDialogHelper.showSimpleAlertDialog(getApplicationContext(), "No Network connection available");
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
    }

    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

    private boolean validateSignInResponse(JSONObject response) {
        boolean signInSuccess = false;
        if ((response != null)) {
            try {
                String status = response.getString("status");
                String msg = response.getString(GMSConstants.PARAM_MESSAGE);
                Log.d(TAG, "status val" + status + "msg" + msg);

                if ((status != null)) {
                    if (status.equalsIgnoreCase("Success")) {
                        signInSuccess = true;
                    } else {
                        signInSuccess = false;
                        Log.d(TAG, "Show error dialog");
                        AlertDialogHelper.showSimpleAlertDialog(this, msg);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return signInSuccess;
    }

    @Override
    public void onResponse(JSONObject response) {
        progressDialogHelper.hideProgressDialog();
            if (validateSignInResponse(response)) {

                Toast.makeText(getApplicationContext(), "We have mailed you a link to reset your password", Toast.LENGTH_LONG).show();
                Intent homeIntent = new Intent(getApplicationContext(), LoginActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                homeIntent.putExtra("mobile_no", edtEmailOrMobileNo.getText().toString());
                startActivity(homeIntent);
                finish();
            }

    }

    @Override
    public void onError(String error) {
        progressDialogHelper.hideProgressDialog();
        AlertDialogHelper.showSimpleAlertDialog(this, error);
    }
}