package com.purelink.cluelin.barcodescanner;

import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;


/**
 * Created by cluelin on 2017-02-24.
 */

public class ServerSocket extends Thread {

    final String SERVER_ADDRESS;
    final int SERVER_PORT;
    static Socket clientSocket;

    OutputStream outputStream;
    PrintStream printStream;

    String BarcodeValue;
    String soNumber;


    Handler handler;

    public ServerSocket(String sAddr, int sPort) {
        SERVER_ADDRESS = sAddr;
        SERVER_PORT = sPort;

    }

    public void run() {

        try {
            Log.d("toServer", "소켓 접속 시도" +ServerInformation.SERVER_ADDRESS );
            clientSocket = new Socket(ServerInformation.SERVER_ADDRESS, ServerInformation.PORT);
            Log.d("fromServer", "소켓 접속 성공");



            //output stream 열어준다.
            outputStream = clientSocket.getOutputStream();
            printStream = new PrintStream(outputStream);


            //저장한 rx, tx를 전부 내보낸다. JSON이용하지않고있음.
            sendBarcodeValue();

            // SO Number를 JSON 으로 보내줌.
            if(soNumber != null && !soNumber.equals("")){
                JSONObject obj = new JSONObject();
                obj.put("soNumber", soNumber);
                printStream.println(obj.toString());
            }


            handler.sendMessage(handler.obtainMessage());


            //여기 달라짐. <- 소켓 종료가 이전버전까지는 없엇음.
            clientSocket.close();


        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void sendBarcodeValue() {

        try {
            printStream.println(BarcodeValue);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setHandler(Handler handler) {
        this.handler = handler;

    }

    public void setBarcodeValue(String BarcodeValue) {
        this.BarcodeValue = BarcodeValue;
    }

    public void setSoValue(String soNumber){
        this.soNumber = soNumber;

        Log.d("tag", "So : " + soNumber);
    }


}
