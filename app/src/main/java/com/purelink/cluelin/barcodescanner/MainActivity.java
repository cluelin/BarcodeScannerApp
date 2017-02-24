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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    MyHandler handler = new MyHandler(this);

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private CompoundButton onlyCode128;
    private TextView statusMessage;
    private EditText rxBarcodeValueView;
    private EditText txBarcodeValueView;
    private EditText mailAddress;

    private ArrayList<String> rxBarcodeValueList = new ArrayList<>();
    private ArrayList<String> txBarcodeValueList = new ArrayList<>();

    String serialSequence = "";


    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView) findViewById(R.id.status_message);
        rxBarcodeValueView = (EditText) findViewById(R.id.barcode_value_rx);
        txBarcodeValueView = (EditText) findViewById(R.id.barcode_value_tx);

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


                    //Rx에 속하는 값인지 Tx에 속하는 값인지 확인
                    if (getWindow().getCurrentFocus() == rxBarcodeValueView) {
                        //중복확인
                        if (rxBarcodeValueList.contains(barcode.displayValue)) {

                            Toast.makeText(this, "중복됨!", Toast.LENGTH_SHORT).show();

                        } else {

                            rxBarcodeValueList.add(barcode.displayValue);
                            addBarcodeValueToBarcodeArea(rxBarcodeValueView, rxBarcodeValueList.get(rxBarcodeValueList.size() - 1));

                        }

                    } else if (getWindow().getCurrentFocus() == txBarcodeValueView) {

                        //중복확인
                        if (txBarcodeValueList.contains(barcode.displayValue)) {

                            Toast.makeText(this, "중복됨!", Toast.LENGTH_SHORT).show();

                        } else {

                            txBarcodeValueList.add(barcode.displayValue);
                            addBarcodeValueToBarcodeArea(txBarcodeValueView, txBarcodeValueList.get(txBarcodeValueList.size() - 1));

                        }
                    }


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

    private void addBarcodeValueToBarcodeArea(EditText targetArea, String targetString) {

        if (targetArea.getText().toString() != "") {
            targetString = targetArea.getText().toString() + "\n" + targetString;
        }

        targetArea.setText(targetString);

    }

    public void sendBarcodeValueToServer(View v) {

        ServerSocket serverSocket = new ServerSocket(ServerInformation.SERVER_ADDRESS, ServerInformation.PORT);
        serverSocket.setRxBarcodeValue("rx \n" + rxBarcodeValueView.getText().toString() + "\n" + "tx \n" + txBarcodeValueView.getText().toString());
        serverSocket.setHandler(handler);
        serverSocket.start();



    }

    public void sendEmail(View v) {

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailAddress.getText() + ""});

        //제목
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "시리얼 넘버");
        //내용
        emailIntent.putExtra(Intent.EXTRA_TEXT, "" + rxBarcodeValueView.getText() + txBarcodeValueView.getText());


        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));

        } catch (ActivityNotFoundException e) {
            //TODO: Handle case where no email app is available
            e.printStackTrace();
        }

    }


    private void handleMessage(Message msg) {
        Toast.makeText(getApplicationContext(), "전송 성공", Toast.LENGTH_LONG).show();
    }


    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }
}
