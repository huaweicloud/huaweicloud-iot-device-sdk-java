/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.utils;

public class ExceptionUtil {
    private static final int DEFAULT_LINE = 10;

    public static String getExceptionCause(Throwable e) {
        StringBuilder sb;
        for (sb = new StringBuilder(); e != null; e = e.getCause()) {
            sb.append(e.toString()).append("\n");
        }

        return sb.toString();
    }

    public static String getAllExceptionStackTrace(Throwable e) {
        if (e == null) {
            return "";
        } else {
            StringBuilder stackTrace = new StringBuilder(e.toString());
            StackTraceElement[] astacktraceelement = e.getStackTrace();
            StackTraceElement[] var3 = astacktraceelement;
            int var4 = astacktraceelement.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                StackTraceElement anAstacktraceelement = var3[var5];
                stackTrace.append("\r\n").append("\tat ").append(anAstacktraceelement);
            }

            return stackTrace.toString();
        }
    }

    private static String getExceptionStackTrace(Throwable e, int lineNum) {
        if (e == null) {
            return "";
        } else {
            StringBuilder stackTrace = new StringBuilder(e.toString());
            StackTraceElement[] astacktraceelement = e.getStackTrace();
            int size = lineNum > astacktraceelement.length ? astacktraceelement.length : lineNum;

            for (int i = 0; i < size; ++i) {
                stackTrace.append("\r\n").append("\tat ").append(astacktraceelement[i]);
            }

            return stackTrace.toString();
        }
    }

    public static String getBriefStackTrace(Throwable e) {
        return getExceptionStackTrace(e, DEFAULT_LINE);
    }
}
