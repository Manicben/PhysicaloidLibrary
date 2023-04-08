package com.manicben.physicaloid.lib;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public enum UsbSerialDevice {
    UNKNOWN    (UsbVid.UNKNOWN,   0,      Driver.UNKNOWN,  DTR.OFF),

    ARDUINO    (UsbVid.ARDUINO,   0,      Driver.CDCADM,   DTR.OFF),
    UNO_0      (UsbVid.ARDUINO,   0x0043, Driver.CDCADM,   DTR.OFF),
    UNO_1      (UsbVid.ARDUINO,   0x0001, Driver.CDCADM,   DTR.OFF),
    UNO_2      (UsbVid.ARDUINO,   0x0243, Driver.CDCADM,   DTR.OFF),
    UNO_3      (UsbVid.ARDUINO_1, 0x0043, Driver.CDCADM,   DTR.OFF),
    LEONARDO_0 (UsbVid.ARDUINO,   0x0036, Driver.CDCADM,   DTR.ON ),
    LEONARDO_1 (UsbVid.ARDUINO,   0x8036, Driver.CDCADM,   DTR.ON ),
    LEONARDO_2 (UsbVid.ARDUINO_1, 0x0036, Driver.CDCADM,   DTR.ON ),
    LEONARDO_3 (UsbVid.ARDUINO_1, 0x8036, Driver.CDCADM,   DTR.ON ),

    FTDI       (UsbVid.FTDI,      0,      Driver.FTDI,     DTR.OFF),

    CP210X     (UsbVid.CP210X,    0,      Driver.CP210X,   DTR.OFF),

    CH341_0    (UsbVid.DCCDUINO,  0x5523, Driver.WINCH34X, DTR.OFF),
    CH340_0    (UsbVid.DCCDUINO,  0x7523, Driver.WINCH34X, DTR.OFF),
    CH341_1    (UsbVid.WCH,       0x5523, Driver.WINCH34X, DTR.OFF),
    CH340_1    (UsbVid.WCH,       0x7523, Driver.WINCH34X, DTR.OFF),

    SPARKFUN   (UsbVid.SPARKFUN,  0,      Driver.CDCADM,   DTR.ON ),

    ADAFRUIT   (UsbVid.ADAFRUIT,  0,      Driver.CDCADM,   DTR.ON ),

    RASPBERRY  (UsbVid.RASPBERRY, 0,      Driver.CDCADM,   DTR.ON ),

    XIAO_RP2040 (UsbVid.SEEEDUINO, 0x0042, Driver.CDCADM, DTR.ON),

    MBED_LPC1768                 (UsbVid.MBED_LPC1768,                 0, Driver.CDCADM, DTR.OFF),
    MBED_LPC11U24                (UsbVid.MBED_LPC11U24,                0, Driver.CDCADM, DTR.OFF),
    MBED_FRDM_KL25Z_OPENSDA_PORT (UsbVid.MBED_FRDM_KL25Z_OPENSDA_PORT, 0, Driver.CDCADM, DTR.OFF),
    MBED_FRDM_KL25Z_KL25Z_PORT   (UsbVid.MBED_FRDM_KL25Z_KL25Z_PORT,   0, Driver.CDCADM, DTR.OFF);

    private final UsbVid vid;
    private final int pid;
    private final int driver;
    private final boolean dtr;

    UsbSerialDevice(UsbVid vid, int pid, int driver, boolean dtr) {
        this.vid    = vid;
        this.pid    = pid;
        this.driver = driver;
        this.dtr    = dtr;
    }

    public int getVid() {
        return vid.getVid();
    }

    public int getPid() {
        return pid;
    }

    public int getDriver() {
        return driver;
    }

    public boolean getDtr() {
        return dtr;
    }

    public static class Driver {
        public static final int UNKNOWN  = 0;
        public static final int CDCADM   = 1;
        public static final int FTDI     = 2;
        public static final int CP210X   = 3;
        public static final int WINCH34X = 4;
    }

    public static class DTR {
        public static final boolean ON  = true;
        public static final boolean OFF = false;
    }

    private static final Table<Integer, Integer, UsbSerialDevice> idsToUsbDeviceMapping = HashBasedTable.create();

    static {
        for(UsbSerialDevice usbDevice : UsbSerialDevice.values()) {
            idsToUsbDeviceMapping.put(
                    usbDevice.getVid(),
                    usbDevice.getPid(),
                    usbDevice
            );
        }
    }

    public static UsbSerialDevice idsToUsbSerialDevice(int vid, int pid) {
        if(idsToUsbDeviceMapping.contains(vid, pid)) {
            return idsToUsbDeviceMapping.get(vid, pid);
        } else if (idsToUsbDeviceMapping.containsRow(vid)) {
            return idsToUsbDeviceMapping.get(vid, 0);
        }
        return UNKNOWN;
    }
}
