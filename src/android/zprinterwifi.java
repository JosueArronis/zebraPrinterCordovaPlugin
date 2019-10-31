package cordova.plugin.zebra.printer.wifi;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.os.Looper;
import android.os.Bundle;
import android.content.Context;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.device.SmartcardReader;
import com.zebra.sdk.device.SmartcardReaderFactory;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.SGD;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class zprinterwifi extends CordovaPlugin {

    private static final String LOG_TAG = "zprinterwifi";
    private Connection connection = null;

    public zprinterwifi() {
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("print")) {
            try {
                Context context = this.cordova.getActivity().getApplicationContext();
                String ip = args.getString(0);
                int port = Integer.parseInt(args.getString(1));
                String msg = args.getString(2);
                sendData(callbackContext, ip, port, msg, context);
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
            return true;
        }
        callbackContext.error("Faild");
        return false;
    }

    /*
     * This will send data to be printed by the wifi printer
     */
    void sendData(final CallbackContext callbackContext, final String ip, int port, final String msg, Context ctx)
            throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    connection = new TcpConnection(ip, port);
                    connection.open();

                    ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                    ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);

                    File filepath = ctx.getFileStreamPath("TEST.LBL");
                    // File filepath = new File("TEST.LBL");
                    createFile(printer, "TEST.LBL", msg, ctx);

                    printer.sendFileContents(filepath.getAbsolutePath());

                    connection.close();

                    callbackContext.success("Done");

                } catch (Exception e) {
                    // Handle communications error here.
                    callbackContext.error(e.getMessage());
                }
            }
        }).start();
    }

    private void createFile(ZebraPrinter printer, String fileName, String datos, Context ctx) throws IOException {

        FileOutputStream os = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);

        byte[] configLabel = null;

        PrinterLanguage pl = printer.getPrinterControlLanguage();

        if (pl == PrinterLanguage.ZPL) {

            configLabel = datos.getBytes();

        } else if (pl == PrinterLanguage.CPCL) {
            String cpclConfigLabel = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n"
                    + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            configLabel = cpclConfigLabel.getBytes();
        }
        os.write(configLabel);
        os.flush();
        os.close();
    }
}
