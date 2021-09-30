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
package com.manicben.physicaloid.misc;

import android.util.Log;

import com.manicben.physicaloid.BuildConfig;

public class RingBuffer {

        private static final String TAG = RingBuffer.class.getSimpleName();
        private static final boolean DEBUG_SHOW_ADD = BuildConfig.DEBUG;
        private static final boolean DEBUG_SHOW_GET = BuildConfig.DEBUG;
        private int mRingBufSize;
        private byte[] mRingBuf;
        private int mAddIndex;     // top of data index
        private int mGetIndex;     // tail of data index

        /**
         * Ring buffer
         *
         * @param bufferSize buffer size. It needs enough size e.g.1024
         */
        public RingBuffer(int bufferSize) {
                mRingBufSize = bufferSize + 1;
                mRingBuf = new byte[mRingBufSize];
                mAddIndex = 0;
                mGetIndex = 0;
        }

        /**
         * Gets ring buffer size
         *
         * @return ring buffer size
         */
        public int getRingBufferSize() {
                return mRingBufSize - 1;
        }

        /**
         * Gets buffered length
         *
         * @return buffered length
         */
        public int getBufferdLength() {
                if(mAddIndex >= mGetIndex) {
                        return mAddIndex - mGetIndex;
                } else {
                        return mAddIndex + (mRingBufSize - mGetIndex);
                }
        }

        /**
         * Adds byte array to ring buffer
         *
         * @param buf    byte array
         * @param length added length
         *
         * @return actually added length
         */
        public synchronized int add(byte[] buf, int length) {
                int addLen = length;
                if(buf == null) {
                        return 0;
                }
                if(mAddIndex > mGetIndex) {
                        if((mAddIndex + length) >= mRingBufSize) {                             // When the result of adding straddles one lap
                                if((mRingBufSize - mAddIndex) + (mGetIndex - 1) < length) {    // When the length is still longer than one lap
                                        addLen = (mRingBufSize - mAddIndex) + (mGetIndex - 1); // Fixed to a size that does not overtake
                                }
                        }
                } else if(mAddIndex < mGetIndex) { // When straddling one lap
                        if((mGetIndex - 1) - mAddIndex < length) {
                                addLen = (mGetIndex - 1) - mAddIndex;
                        }
                }

                if(buf.length < addLen) {
                        addLen = buf.length;
                }

                if((mAddIndex + addLen) >= mRingBufSize) { // When store straddles the end of the buffer
                        int remain = mAddIndex + addLen - mRingBufSize;
                        int copyLen = addLen - remain;
                        if(copyLen != 0) {
                                System.arraycopy(buf, 0, mRingBuf, mAddIndex, copyLen);
                                if(DEBUG_SHOW_ADD) {
                                        Log.d(TAG, "add(" + length + ") : copy buf[0:" + (copyLen - 1) + "] to mRingBuf[" + mAddIndex + ":" + (mAddIndex + copyLen - 1) + "]");
                                }
                        }

                        mAddIndex = 0;

                        if(remain != 0) {
                                System.arraycopy(buf, copyLen, mRingBuf, mAddIndex, remain);
                                if(DEBUG_SHOW_ADD) {
                                        Log.d(TAG, "add(" + length + ") : copy buf[" + (copyLen) + ":" + (addLen - 1) + "] to mRingBuf[0:" + (remain - 1) + "]");
                                }
                                mAddIndex = remain;
                        }

                        if(DEBUG_SHOW_ADD) {
                                Log.d(TAG, "add(" + length + ") : addOffset = " + mAddIndex + ", getOffset = " + mGetIndex);
                        }

                        return addLen;
                } else {
                        System.arraycopy(buf, 0, mRingBuf, mAddIndex, addLen);

                        if(DEBUG_SHOW_ADD) {
                                Log.d(TAG, "add(" + length + ") : copy buf[0:" + (addLen - 1) + "] to mRingBuf[" + mAddIndex + ":" + (mAddIndex + addLen - 1) + "]");
                        }

                        mAddIndex += addLen;

                        if(DEBUG_SHOW_ADD) {
                                Log.d(TAG, "add(" + length + ") : addOffset = " + mAddIndex + ", getOffset = " + mGetIndex);
                        }

                        return addLen;
                }
        }
        /**
         * Adds byte array to ring buffer with starting offset
         *
         * @param buf    byte array
         * @param length added length
         * @param off    offset to start from
         *
         * @return actually added length
         */
        public synchronized int add(byte[] buf, int length, int off) {
                int addLen = length;
                if(buf == null) {
                        return 0;
                }
                if(mAddIndex > mGetIndex) {
                        if((mAddIndex + length) >= mRingBufSize) {                             // When the result of adding straddles one lap
                                if((mRingBufSize - mAddIndex) + (mGetIndex - 1) < length) {    // When the length is still longer than one lap
                                        addLen = (mRingBufSize - mAddIndex) + (mGetIndex - 1); // Fixed to a size that does not overtake
                                }
                        }
                } else if(mAddIndex < mGetIndex) { // When straddling one lap
                        if((mGetIndex - 1) - mAddIndex < length) {
                                addLen = (mGetIndex - 1) - mAddIndex;
                        }
                }

                if(buf.length < addLen) {
                        addLen = buf.length;
                }

                if((mAddIndex + addLen) >= mRingBufSize) { // When store straddles the end of the buffer
                        int remain = mAddIndex + addLen - mRingBufSize;
                        int copyLen = addLen - remain;
                        if(copyLen != 0) {
                                System.arraycopy(buf, off, mRingBuf, mAddIndex, copyLen);
                                if(DEBUG_SHOW_ADD) {
                                        Log.d(TAG, "add(" + length + ") : copy buf[0:" + (copyLen - 1) + "] to mRingBuf[" + mAddIndex + ":" + (mAddIndex + copyLen - 1) + "]");
                                }
                        }

                        mAddIndex = 0;

                        if(remain != 0) {
                                System.arraycopy(buf, copyLen+off, mRingBuf, mAddIndex, remain);
                                if(DEBUG_SHOW_ADD) {
                                        Log.d(TAG, "add(" + length + ") : copy buf[" + (copyLen) + ":" + (addLen - 1) + "] to mRingBuf[0:" + (remain - 1) + "]");
                                }
                                mAddIndex = remain;
                        }

                        if(DEBUG_SHOW_ADD) {
                                Log.d(TAG, "add(" + length + ") : addOffset = " + mAddIndex + ", getOffset = " + mGetIndex);
                        }

                        return addLen;
                } else {
                        System.arraycopy(buf, 0, mRingBuf, mAddIndex, addLen);

                        if(DEBUG_SHOW_ADD) {
                                Log.d(TAG, "add(" + length + ") : copy buf[0:" + (addLen - 1) + "] to mRingBuf[" + mAddIndex + ":" + (mAddIndex + addLen - 1) + "]");
                        }

                        mAddIndex += addLen;

                        if(DEBUG_SHOW_ADD) {
                                Log.d(TAG, "add(" + length + ") : addOffset = " + mAddIndex + ", getOffset = " + mGetIndex);
                        }

                        return addLen;
                }
        }

        /**
         * Gets ring buffer to byte array
         *
         * @param buf    byte array
         * @param length gotten length
         *
         * @return actually gotten length
         */
        public synchronized int get(byte[] buf, int length) {
                if(buf == null) {
                        return 0;
                }
                int getLen = length;
                if(mAddIndex == mGetIndex) {
                        return 0;
                } else if(mGetIndex < mAddIndex) { // Generally
                        if(mAddIndex - mGetIndex < length) {    // get request size is larger than the buffered size
                                getLen = mAddIndex - mGetIndex; // Returns the size currently buffered
                        }
                } else { // When the index spans one lap
                        if(mAddIndex + (mRingBufSize - mGetIndex) < length) {    // get request size is larger than the buffered size
                                getLen = mAddIndex + (mRingBufSize - mGetIndex); // Returns the size currently buffered
                        }
                }

                if(buf.length < getLen) {
                        getLen = buf.length;
                }

                if((mGetIndex + getLen) >= mRingBufSize) {
                        int remain = mGetIndex + getLen - mRingBufSize;
                        int copyLen = getLen - remain;
                        if(copyLen != 0) {
                                System.arraycopy(mRingBuf, mGetIndex, buf, 0, copyLen);
                                if(DEBUG_SHOW_GET) {
                                        Log.d(TAG, "get(" + length + ") : copy mRingBuf[" + mGetIndex + ":" + (mGetIndex + copyLen - 1) + "] to buf[0:" + (copyLen - 1) + "]");
                                }
                        }

                        mGetIndex = 0;

                        if(remain != 0) {
                                System.arraycopy(mRingBuf, mGetIndex, buf, copyLen, remain);
                                if(DEBUG_SHOW_GET) {
                                        Log.d(TAG, "get(" + length + ") : copy mRingBuf[0:" + (remain - 1) + "] to buf[" + copyLen + ":" + (remain - 1) + "]");
                                }
                                mGetIndex = remain;
                        }

                        if(DEBUG_SHOW_GET) {
                                Log.d(TAG, "get(" + length + ") : addOffset = " + mAddIndex + ", getOffset = " + mGetIndex);
                        }
                        return getLen;
                } else {
                        System.arraycopy(mRingBuf, mGetIndex, buf, 0, getLen);

                        if(DEBUG_SHOW_GET) {
                                Log.d(TAG, "get(" + length + ") : copy mRingBuf[" + mGetIndex + ":" + (mGetIndex + getLen - 1) + "] to buf[0:" + (getLen - 1) + "]");
                        }

                        mGetIndex += getLen;

                        if(DEBUG_SHOW_GET) {
                                Log.d(TAG, "get(" + length + ") : addOffset = " + mAddIndex + ", getOffset = " + mGetIndex);
                        }

                        return getLen;
                }
        }

        /**
         * Clear ring buffer
         */
        public synchronized void clear() {
                mAddIndex = 0;
                mGetIndex = 0;
        }
}
