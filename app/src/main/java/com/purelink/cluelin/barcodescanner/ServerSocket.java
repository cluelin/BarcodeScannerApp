package com.purelink.cluelin.barcodescanner;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.net.Socket;


/**
 * Created by cluelin on 2017-02-24.
 */

public class ServerSocket extends Thread {

    final String SERVER_ADDRESS;
    final int SERVER_PORT;
    static Socket serverSocket;

    OutputStream outputStream;
    PrintStream printStream;

    String rxBarcodeValue;
    String txBarcodeValue;

    Handler handler;

    public ServerSocket(String sAddr, int sPort){
        SERVER_ADDRESS = sAddr;
        SERVER_PORT = sPort;

    }

    public void run(){

        try {
            Log.d("toServer", "소켓 접속 시도");
            serverSocket = new Socket(ServerInformation.SERVER_ADDRESS, ServerInformation.PORT);
            Log.d("fromServer", "소켓 접속 성공");


            outputStream = serverSocket.getOutputStream();
            printStream = new PrintStream(outputStream);

            sendBarcodeValue();

            handler.sendMessage(handler.obtainMessage());


        }catch (Exception e){
            e.printStackTrace();

        }

    }

    public void sendBarcodeValue(){

        try {
            printStream.println(rxBarcodeValue);
            printStream.println(txBarcodeValue);
            Log.d("toServer", "소켓으로 가게정보 보내기 : " + rxBarcodeValue.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setHandler(Handler handler){
        this.handler = handler;

    }

    public void setRxBarcodeValue(String rxBarcodeValue) {
        this.rxBarcodeValue = rxBarcodeValue;
    }

    public void setTxBarcodeValue(String txBarcodeValue) {
        this.txBarcodeValue = txBarcodeValue;
    }


}
