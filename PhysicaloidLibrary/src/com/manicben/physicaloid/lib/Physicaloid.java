/*
 * Copyright (C) 2013 Keisuke SUZUKI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * Distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.manicben.physicaloid.lib;

import android.content.Context;
import android.util.Log;
import com.manicben.physicaloid.BuildConfig;
import com.manicben.physicaloid.lib.framework.AutoCommunicator;
import com.manicben.physicaloid.lib.framework.SerialCommunicator;
import com.manicben.physicaloid.lib.framework.Uploader;
import com.manicben.physicaloid.lib.programmer.avr.UploadErrors;
import com.manicben.physicaloid.lib.usb.driver.uart.ReadListener;
import com.manicben.physicaloid.lib.usb.driver.uart.UartConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Physicaloid {

        /**
         * USB physical connection as a number
         */
        public static final int USB = 1;
        /**
         * WIFI physical connection as a number
         */
        public static final int WIFI = 2;
        /**
         * Bluetooth physical connection as a number
         */
        public static final int BLUETOOTH = 3;
        /**
         * USB physical connection as a string
         */
        public static final String USB_STRING = "USB";
        /**
         * WIFI physical connection as a string
         */
        public static final String WIFI_STRING = "WiFi";
        /**
         * Bluetooth physical connection as a string
         */
        public static final String BLUETOOTH_STRING = "BlueTooth";
        private static final boolean DEBUG_SHOW = BuildConfig.DEBUG;
        private static final String TAG = Physicaloid.class.getSimpleName();
        private Context mContext;
        private Boards mBoard;
        protected SerialCommunicator mSerial;
        private Uploader mUploader;
        private Thread mUploadThread;
        private UploadCallBack mCallBack;
        private InputStream mFileStream;
        private static final Object LOCK = new Object();
        protected static final Object LOCK_WRITE = new Object();
        protected static final Object LOCK_READ = new Object();
        private String mNetdest = null;
        private String mBlueName = null;
        private int mDport = 9001;
        private int mCport = 9002;
        private boolean USE_USB = true;
        private boolean USE_WIFI = false;
        private boolean USE_BLUETOOTH = false;

        /**
         * Default, USB only
         *
         * @param context
         */
        public Physicaloid(Context context) {
                USE_USB = true;
                USE_WIFI = false;
                USE_BLUETOOTH = false;
                this.mContext = context;
        }

        /**
         * Bluetooth, optional USB
         *
         * @param context
         * @param u true = use USB
         * @param BlueName Name of bluetooth, null for automatic default
         */
        public Physicaloid(Context context, boolean u, String BlueName) {
                USE_USB = u;
                USE_WIFI = false;
                USE_BLUETOOTH = true;
                mBlueName = BlueName;
                this.mContext = context;
        }

        // WiFi, optional USB
        /**
         *
         * @param context
         * @param u true = use USB
         * @param Netdest e.g. "192.168.4.1" or a host name
         * @param Dport port number for data
         * @param Cport port number for controls
         */
        public Physicaloid(Context context, boolean u, String Netdest, int Dport, int Cport) {
                USE_USB = u;
                USE_WIFI = true;
                USE_BLUETOOTH = false;
                if(Dport > 0) {
                        mDport = Dport;
                } else {
                        mDport = 9001;
                }
                if(Cport > 0) {
                        mCport = Cport;
                } else {
                        mCport = mDport + 1;
                }
                if(mNetdest != null) {
                        mNetdest = Netdest;
                } else {
                        mNetdest = "192.168.4.1";
                }
                this.mContext = context;
        }

        // WiFi, Bluetooth, optional USB
        /**
         *
         * @param context
         * @param u true = use USB
         * @param BlueName Name of bluetooth, null for automatic default
         * @param Netdest e.g. "192.168.4.1" or a host name, null defaults to
         * "192.168.4.1"
         * @param Dport port number for data, Zero = 9001
         * @param Cport port number for controls, Zero = Dport + 1
         */
        public Physicaloid(Context context, boolean u, String BlueName, String Netdest, int Dport, int Cport) {
                this.mContext = context;
                USE_USB = u;
                USE_WIFI = true;
                USE_BLUETOOTH = true;
                if(Dport > 0) {
                        mDport = Dport;
                } else {
                        mDport = 9001;
                }
                if(Cport > 0) {
                        mCport = Cport;
                } else {
                        mCport = mDport + 1;
                }
                if(mNetdest != null) {
                        mNetdest = Netdest;
                } else {
                        mNetdest = "192.168.4.1";
                }
                mBlueName = BlueName;
        }

        /**
         * Opens a device and communicate USB UART by default settings
         *
         * @return true : successful , false : fail
         * @throws RuntimeException
         */
        public boolean open() throws RuntimeException {
                return open(new UartConfig());
        }

        /**
         * Opens a device and communicate USB UART
         *
         * @param uart UART configuration
         * @return true : successful , false : fail
         * @throws RuntimeException
         */
        public boolean open(UartConfig uart) throws RuntimeException {
                synchronized(LOCK) {
                        if(mSerial == null) {
                                mSerial = new AutoCommunicator(USE_USB, USE_WIFI, USE_BLUETOOTH, mDport, mCport, mNetdest, mBlueName).getSerialCommunicator(mContext);
                                if(mSerial == null) {
                                        return false;
                                }
                        }
                        if(mSerial.open()) {
                                mSerial.setUartConfig(uart);
                                return true;
                        } else {
                                return false;
                        }
                }
        }

        /**
         * Closes a device.
         *
         * @return true : successful , false : fail
         * @throws RuntimeException
         */
        public boolean close() throws RuntimeException {
                synchronized(LOCK) {
                        if(mSerial == null) {
                                return true;
                        }
                        if(mSerial.close()) {
                                mSerial = null;
                                return true;
                        } else {
                                return false;
                        }
                }
        }

        /**
         * Reads from a device
         *
         * @param buf buffer to read into
         * @return read byte size
         * @throws RuntimeException
         */
        public int read(byte[] buf) throws RuntimeException {
                if(mSerial == null) {
                        return 0;
                }
                return read(buf, buf.length);
        }

        /**
         * Reads from a device
         *
         * @param buf buffer to read into
         * @param size size of buffer
         * @return read byte size
         * @throws RuntimeException
         */
        public int read(byte[] buf, int size) throws RuntimeException {
                synchronized(LOCK_READ) {
                        if(mSerial == null) {
                                return 0;
                        }
                        return mSerial.read(buf, size);
                }
        }

        /**
         * Adds read listener
         *
         * @param listener ReadListener
         * @return true : successful , false : fail
         * @throws RuntimeException
         */
        public boolean addReadListener(ReadListener listener) throws RuntimeException {
                synchronized(LOCK_READ) {
                        if(mSerial == null) {
                                return false;
                        }
                        if(listener == null) {
                                return false;
                        }
                        mSerial.addReadListener(listener);
                        return true;
                }
        }

        /**
         * Clears read listener
         *
         * @throws RuntimeException
         */
        public void clearReadListener() throws RuntimeException {
                synchronized(LOCK_READ) {
                        if(mSerial == null) {
                                return;
                        }
                        mSerial.clearReadListener();
                }
        }

        /**
         * Writes to a device.
         *
         * @param buf buffer to write
         * @return written byte size
         * @throws RuntimeException
         */
        public int write(byte[] buf) throws RuntimeException {
                if(mSerial == null) {
                        return 0;
                }
                return write(buf, buf.length);
        }

        /**
         * Writes to a device.
         *
         * @param buf buffer to write
         * @param size size of buffer
         * @return written byte size
         * @throws RuntimeException
         */
        public int write(byte[] buf, int size) throws RuntimeException {
                synchronized(LOCK_WRITE) {
                        if(mSerial == null) {
                                return 0;
                        }
                        return mSerial.write(buf, size);
                }
        }

        /**
         * Uploads a binary file to a device on background process. No need to
         * open().
         *
         * @param board board profile e.g. Boards.ARDUINO_UNO
         * @param filePath a binary file path e.g. /sdcard/arduino/Blink.hex
         * @throws RuntimeException
         */
        public void upload(Boards board, String filePath) throws RuntimeException {
                upload(board, filePath, null);
        }

        /**
         * Uploads a binary file to a device on background process. No need to
         * open().
         *
         * @param board board profile e.g. Boards.ARDUINO_UNO
         * @param filePath a binary file path e.g. /sdcard/arduino/Blink.uno.hex
         * @param callback Upload callback
         * @throws RuntimeException
         */
        public void upload(Boards board, String filePath, UploadCallBack callback) throws RuntimeException {
                if(filePath == null) {
                        if(callback != null) {
                                callback.onError(UploadErrors.FILE_OPEN);
                        }
                        return;
                }

                File file = new File(filePath);
                if(!file.exists() || !file.isFile() || !file.canRead()) {
                        if(callback != null) {
                                callback.onError(UploadErrors.FILE_OPEN);
                        }
                        return;
                }

                InputStream is;
                try {
                        is = new FileInputStream(filePath);
                } catch(Exception e) {
                        if(callback != null) {
                                callback.onError(UploadErrors.FILE_OPEN);
                        }
                        return;
                }
                upload(board, is, callback);
        }

        /**
         * Uploads a binary file to a device on background process. No need to
         * open().
         *
         * @param board board profile e.g. Boards.ARDUINO_UNO
         * @param fileStream a binary stream e.g.
         * getResources().getAssets().open("Blink.uno.hex")
         * @throws RuntimeException
         */
        public void upload(Boards board, InputStream fileStream) throws RuntimeException {
                upload(board, fileStream, null);
        }
        boolean serialIsNull = false;

        /**
         * Uploads a binary file to a device on background process. No need to
         * open().
         *
         * @param board board profile e.g. Boards.ARDUINO_UNO
         * @param fileStream a binary stream e.g.
         * getResources().getAssets().open("Blink.uno.hex")
         * @param callback Upload callback
         * @throws RuntimeException
         */
        public void upload(Boards board, InputStream fileStream, UploadCallBack callback) throws RuntimeException {
                mUploader = new Uploader();
                mCallBack = callback;
                mFileStream = fileStream;
                mBoard = board;

                if(mSerial == null) { // if not open
                        if(DEBUG_SHOW) {
                                Log.d(TAG, "upload : mSerial is null");
                        }
                        mSerial = new AutoCommunicator(USE_USB, USE_WIFI, USE_BLUETOOTH, mDport, mCport, mNetdest, mBlueName).getSerialCommunicator(mContext);   // need to run on non-thread
                        serialIsNull = true;
                }

                mUploadThread = new Thread(new Runnable() {

                        @Override
                        @SuppressWarnings("NestedSynchronizedStatement")
                        public void run() {
                                synchronized(LOCK) {
                                        synchronized(LOCK_WRITE) {
                                                synchronized(LOCK_READ) {
                                                        UartConfig tmpUartConfig = new UartConfig();


                                                        if(mSerial == null) { // fail
                                                                if(DEBUG_SHOW) {
                                                                        Log.d(TAG, "upload : mSerial is null");
                                                                }
                                                                if(mCallBack != null) {
                                                                        mCallBack.onError(UploadErrors.OPEN_DEVICE);
                                                                }
                                                                mBoard = null;
                                                                mFileStream = null;
                                                                mCallBack = null;
                                                                mUploader = null;
                                                                mSerial = null;
                                                                return;
                                                        }

                                                        if(!mSerial.isOpened()) {
                                                                if(!mSerial.open()) {
                                                                        if(DEBUG_SHOW) {
                                                                                Log.d(TAG, "upload : cannot mSerial.open");
                                                                        }
                                                                        if(mCallBack != null) {
                                                                                mCallBack.onError(UploadErrors.OPEN_DEVICE);
                                                                        }
                                                                        mBoard = null;
                                                                        mFileStream = null;
                                                                        mCallBack = null;
                                                                        mUploader = null;
                                                                        mSerial = null;
                                                                        return;
                                                                }
                                                                if(DEBUG_SHOW) {
                                                                        Log.d(TAG, "upload : open successful");
                                                                }
                                                        } else { // if already open
                                                                UartConfig origUartConfig = mSerial.getUartConfig();
                                                                tmpUartConfig.baudrate = origUartConfig.baudrate;
                                                                tmpUartConfig.dataBits = origUartConfig.dataBits;
                                                                tmpUartConfig.stopBits = origUartConfig.stopBits;
                                                                tmpUartConfig.parity = origUartConfig.parity;
                                                                tmpUartConfig.dtrOn = origUartConfig.dtrOn;
                                                                tmpUartConfig.rtsOn = origUartConfig.rtsOn;
                                                                if(DEBUG_SHOW) {
                                                                        Log.d(TAG, "upload : already open");
                                                                }
                                                        }

                                                        mSerial.stopReadListener();
                                                        mSerial.clearBuffer();

                                                        mUploader.upload(mFileStream, mBoard, mSerial, mCallBack);

                                                        mSerial.setUartConfig(tmpUartConfig); // recover if already
                                                        // open
                                                        mSerial.clearBuffer();
                                                        mSerial.startReadListener();
                                                        if(serialIsNull) {
                                                                mSerial.close();
                                                        }

                                                        mBoard = null;
                                                        mFileStream = null;
                                                        mCallBack = null;
                                                        mUploader = null;
                                                }
                                        }
                                }
                        }
                });

                mUploadThread.start();
        }

        public void cancelUpload() {
                if(mUploadThread == null) {
                        return;
                }
                mUploadThread.interrupt();
        }


        /**
         * Callbacks of program process<br> normal process:<br> onPreUpload() ->
         * onUploading -> onPostUpload<br> cancel:<br> onPreUpload() ->
         * onUploading -> onCancel -> onPostUpload<br> error:<br> onPreUpload
         * |<br> onUploading | -> onError<br> onPostUpload |<br>
         *
         * @author keisuke
         *
         */
        public interface UploadCallBack {
                /*
                 * Callback methods
                 */

                void onPreUpload();

                void onUploading(int value);

                void onPostUpload(boolean success);

                void onCancel();

                void onError(UploadErrors err);
        }

        /**
         * Gets opened or closed status
         *
         * @return true : opened, false : closed
         * @throws RuntimeException
         */
        public boolean isOpened() throws RuntimeException {
                synchronized(LOCK) {
                        if(mSerial == null) {
                                return false;
                        }
                        return mSerial.isOpened();
                }
        }

        /**
         * Sets Serial Configuration
         *
         * @param settings Uart settings
         */
        public void setConfig(UartConfig settings) throws RuntimeException {
                synchronized(LOCK) {
                        if(mSerial == null) {
                                return;
                        }
                        mSerial.setUartConfig(settings);
                }
        }

        /**
         * Sets Baud Rate
         *
         * @param baudrate any baud-rate e.g. 9600
         * @return true : successful, false : fail
         */
        public boolean setBaudrate(int baudrate) throws RuntimeException {
                synchronized(LOCK) {
                        if(mSerial == null) {
                                return false;
                        }
                        return mSerial.setBaudrate(baudrate);
                }
        }

        /**
         * Sets Data Bits
         *
         * @param dataBits data bits e.g. UartConfig.DATA_BITS8
         * @return true : successful, false : fail
         */
        public boolean setDataBits(int dataBits) throws RuntimeException {
                synchronized(LOCK) {
                        if(mSerial == null) {
                                return false;
                        }
                        return mSerial.setDataBits(dataBits);
                }
        }

        /**
         * Sets Parity Bits
         *
         * @param parity parity bits e.g. UartConfig.PARITY_NONE
         * @return true : successful, false : fail
         */
        public boolean setParity(int parity) throws RuntimeException {
                synchronized(LOCK) {
                        if(mSerial == null) {
                                return false;
                        }
                        return mSerial.setParity(parity);
                }
        }

        /**
         * Sets Stop bits
         *
         * @param stopBits stop bits e.g. UartConfig.STOP_BITS1
         * @return true : successful, false : fail
         */
        public boolean setStopBits(int stopBits) throws RuntimeException {
                synchronized(LOCK) {
                        if(mSerial == null) {
                                return false;
                        }
                        return mSerial.setStopBits(stopBits);
                }
        }

        /**
         * Sets flow control DTR/RTS
         *
         * @param dtrOn true then DTR on
         * @param rtsOn true then RTS on
         * @return true : successful, false : fail
         */
        public boolean setDtrRts(boolean dtrOn, boolean rtsOn) throws RuntimeException {
                synchronized(LOCK) {
                        if(mSerial == null) {
                                return false;
                        }
                        return mSerial.setDtrRts(dtrOn, rtsOn);
                }
        }

        /**
         * Sets DTR control line automatically based on UsbSerialDevice
         *
         * @return true : successful, false : fail
         */
        public boolean setAutoDtr() throws RuntimeException {
                synchronized(LOCK) {
                        if(mSerial == null) {
                                return false;
                        }
                        return mSerial.setAutoDtr();
                }
        }

        public String getDriverName() {
                if(mSerial == null) {
                        return "None";
                }
                return mSerial.getClass().getName();
        }
        public String getPhysicalConnectionName() {
                if(mSerial == null) {
                        return "No Physical Connection";
                }
                return mSerial.getPhysicalConnectionName();
        }

        public int getPhysicalConnectionType() {
                if(mSerial == null) {
                        return 0;
                }
                return mSerial.getPhysicalConnectionType();
        }
        public void setDebug(boolean flag) {
                if(mSerial != null) {
                        mSerial.setDebug(flag);
                }
        }
        public int getVID() {
                if(mSerial == null) {
                        return 0;
                }
                return mSerial.getVID();
        }

        public int getPID() {
                if(mSerial == null) {
                        return 0;
                }
                return mSerial.getPID();
        }
}
