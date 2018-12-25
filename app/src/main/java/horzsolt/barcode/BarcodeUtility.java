package horzsolt.barcode;

import android.util.Log;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.BarcodeManager.ScannerConnectionListener;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.ScannerInfo;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BarcodeUtility implements EMDKListener, DataListener, Scanner.StatusListener, ScannerConnectionListener {

    private EMDKManager emdkManager = null;
    private BarcodeManager barcodeManager = null;
    private Scanner scanner = null;
    private final Set<BarcodeListener> barcodeListeners = new HashSet<>();
    private final Set<StatusListener> statusListeners = new HashSet<>();
    private boolean continuousMode = true;
    private static final String TAG  = "BARCODE";

    public void startScan() {

        if (scanner != null) {
            try {

                if (scanner.isEnabled()) {
                    // Submit a new read.
                    scanner.read();
                } else {
                    broadcastStatusChange("Status: Scanner is not enabled");
                }

            } catch (ScannerException e) {

                broadcastStatusChange("Status: " + e.getMessage());
            }
        }

    }

    public void stopScan() {

        if (scanner != null) {

            try {

                scanner.cancelRead();
            } catch (ScannerException e) {
                broadcastStatusChange("Status: " + e.getMessage());
            }
        }
    }

    public void closeScanner() {

        // De-initialize scanner
        deInitScanner();

        // Remove connection listener
        if (barcodeManager != null) {
            barcodeManager.removeConnectionListener(this);
            barcodeManager = null;
        }

        // Release all the resources
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

    private void deInitScanner() {

        if (scanner != null) {

            try {
                scanner.cancelRead();
                scanner.disable();
            } catch (Exception e) {
                broadcastStatusChange("Status: " + e.getMessage());
            }

            try {
                scanner.removeDataListener(this);
                scanner.removeStatusListener(this);
            } catch (Exception e) {
                broadcastStatusChange("Status: " + e.getMessage());
            }

            try {
                scanner.release();
            } catch (Exception e) {
                broadcastStatusChange("Status: " + e.getMessage());
            }

            scanner = null;
        }
    }

    private Scanner getDefaultScanner() {
        Scanner result = null;

        if (barcodeManager != null) {
            List<ScannerInfo> deviceList = barcodeManager.getSupportedDevicesInfo();
            if ((deviceList != null) && (deviceList.size() != 0)) {
                Iterator<ScannerInfo> it = deviceList.iterator();
                while (it.hasNext()) {
                    ScannerInfo scnInfo = it.next();
                    if (scnInfo.isDefaultScanner()) {
                        result = barcodeManager.getDevice(scnInfo);
                    }
                }
            }
        }

        return result;
    }

    private void setScannerConfig() {
        try {
            ScannerConfig config = scanner.getConfig();
            config.decoderParams.ean8.enabled = true;
            config.decoderParams.ean13.enabled = true;
            config.decoderParams.code39.enabled = true;

            scanner.setConfig(config);
        } catch (ScannerException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    public boolean openScanner() {

        scanner = null;

        if (emdkManager != null) {
            barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);

            if (barcodeManager != null) {
                barcodeManager.addConnectionListener(this);

                scanner = getDefaultScanner();
                scanner.triggerType = Scanner.TriggerType.HARD;

                scanner.addDataListener(this);
                scanner.addStatusListener(this);

                try {
                    scanner.enable();
                } catch (ScannerException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }
        }

        return !(scanner == null);
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {

        this.emdkManager = emdkManager;
        openScanner();
    }

    @Override
    public void onClosed() {

        if (emdkManager != null) {
            if (barcodeManager != null) {
                barcodeManager.removeConnectionListener(this);
                barcodeManager = null;
            }
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();

            for (ScanDataCollection.ScanData data : scanData) {
                broadcastBarcodeData(data.getData());
            }
        }
    }

    public void addBarcodeListener(BarcodeListener listener) {
        barcodeListeners.add(listener);
    }

    public void removeBarcodeListener(BarcodeListener listener) {
        barcodeListeners.remove(listener);
    }

    private void broadcastBarcodeData(String data) {
        for (BarcodeListener listener : barcodeListeners) {
            listener.barcodeRead(data);
        }
    }

    public void addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
    }

    public void removeStatusListener(StatusListener listener) {
        statusListeners.remove(listener);
    }

    private void broadcastStatusChange(String status) {
        for (StatusListener listener : statusListeners) {
            listener.statusChanged(status);
        }
    }

    @Override
    public void onStatus(StatusData statusData) {

        StatusData.ScannerStates state = statusData.getState();

        switch (state) {
            case IDLE:

                broadcastStatusChange(statusData.getFriendlyName() + " is enabled and idle...");

                if (state == StatusData.ScannerStates.IDLE) {
                    if(!scanner.isReadPending()) {
                        setScannerConfig();
                    }
                }

                if (continuousMode) {
                    try {
                        // An attempt to use the scanner continuously and rapidly (with a delay < 100 ms between scans)
                        // may cause the scanner to pause momentarily before resuming the scanning.
                        // Hence add some delay (>= 100ms) before submitting the next read.
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, Log.getStackTraceString(e));
                            }
                        }

                        scanner.read();
                    } catch (ScannerException e) {
                        broadcastBarcodeData(e.getMessage());
                    }
                }
                break;
            case WAITING:
                broadcastStatusChange("Scanner is waiting for trigger press...");
                break;
            case SCANNING:
                broadcastStatusChange("Scanning...");
                break;
            case DISABLED:
                broadcastStatusChange(statusData.getFriendlyName() + " is disabled.");
                break;
            case ERROR:
                broadcastStatusChange("An error has occurred.");
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnectionChange(ScannerInfo scannerInfo, BarcodeManager.ConnectionState connectionState) {
        String status;

        String statusExtScanner = connectionState.toString();
        String scannerNameExtScanner = scannerInfo.getFriendlyName();

        switch (connectionState) {
            case CONNECTED:
                deInitScanner();
                openScanner();
                break;
            case DISCONNECTED:
                deInitScanner();
                break;
        }

        status = scannerNameExtScanner + ":" + statusExtScanner;
        broadcastStatusChange(status);
    }
}
