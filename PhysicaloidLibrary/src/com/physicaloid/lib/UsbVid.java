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

package com.physicaloid.lib;

import java.util.HashMap;
import java.util.Map;

public enum UsbVid {
    UNKNOWN                         (0),
    ARDUINO                         (0x2341), // arduino.cc (now merged with arduino.org)
    ARDUINO_1                       (0x2A03), // arduino.org (formerly Smart Projects)
    FTDI                            (0x0403),
    CP210X                          (0x10C4),
    DCCDUINO                        (0x1A86),
    WCH                             (0x4348),
    SPARKFUN                        (0x1B4F),
    ADAFRUIT                        (0x239A),
    RASPBERRY                       (0x2e8a),
    MBED_LPC1768                    (0x0d28),
    MBED_LPC11U24                   (0x0d28),
    MBED_FRDM_KL25Z_OPENSDA_PORT    (0x1357),
    MBED_FRDM_KL25Z_KL25Z_PORT      (0x15a2);

    private final int vid;

    UsbVid(int vid) {
        this.vid = vid;
    }

    public int getVid() {
        return vid;
    }

    private static final Map<Integer, UsbVid> vidToUsbVidMapping = new HashMap<>();

    static {
        for(UsbVid usbVid : UsbVid.values()) {
            vidToUsbVidMapping.put(
                 usbVid.getVid(),
                 usbVid
            );
        }
    }

    public static UsbVid vidToUsbVid(int vid) {
        UsbVid usbVid = vidToUsbVidMapping.get(vid);
        if(usbVid != null) {
            return usbVid;
        }
        return UNKNOWN;
    }
}