/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.purelink.cluelin.barcodescanner;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private CompoundButton onlyCode128;
    private TextView statusMessage;
    private TextView barcodeValueTextView;
    private EditText mailAddress;

    private ArrayList<String> barcodeValueList = new ArrayList<>();

    String serialSequence = "";


    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView) findViewById(R.id.status_message);
        barcodeValueTextView = (TextView) findViewById(R.id.barcode_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);
        onlyCode128 = (CompoundButton) findViewById(R.id.code_128);
        findViewById(R.id.read_barcode).setOnClickListener(this);

        mailAddress = (EditText) findViewById(R.id.mailID);



    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
            intent.putExtra(BarcodeCaptureActivity.Code128, onlyCode128.isChecked());

            //바코드로 찍은 값을 Result로 받아오게 함.
            //Barcode value return to Result.
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }

    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    //this value include Barcode information
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);

                    //중복확인
                    if (barcodeValueList.contains(barcode.displayValue)){

                        Log.d("태그", "중복됨");
                        Toast.makeText(this, "중복됨!", Toast.LENGTH_SHORT).show();

                    }else{
                        Log.d("태그", "barcode.displayValue : " + barcode.displayValue);
                        barcodeValueList.add(barcode.displayValue);

                        Log.d("태그", "barcode size : " + barcodeValueList.size());
                    }


                    //가지고 있는 Serial number 목록에 읽어온 Barcode값을 추가.

                    serialSequence = "";
                    for (int i = 0 ; i < barcodeValueList.size() ; i++){
                        serialSequence = serialSequence + barcodeValueList.get(i) + "\n";
                        Log.d("태그", "barcodeValueList.get(i) "  + barcodeValueList.get(i) );
                    }

                    //가지고있는 Serial number list를 textView에 출력.
                    barcodeValueTextView.setText(barcodeValueList.size() + "개 \n" + serialSequence );

                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void sendEmail(View v) {

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL,new String[]{mailAddress.getText() + ""});
        Log.d("name", mailAddress.getText() + "");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "시리얼 넘버");
        emailIntent.putExtra(Intent.EXTRA_TEXT, serialSequence);



        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));

        } catch (ActivityNotFoundException e) {
            //TODO: Handle case where no email app is available
            e.printStackTrace();
        }

    }
}
