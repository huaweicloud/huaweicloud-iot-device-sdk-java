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

package com.huaweicloud.sdk.iot.bridge.sample.tcp.constants;

public class Constants {

    // 登录消息
    public static final String MSG_TYPE_DEVICE_LOGIN = "DEVICE_LOGIN";

    // 位置上报
    public static final String MSG_TYPE_REPORT_LOCATION_INFO = "REPORT_LOCATION_INFO";

    // 设备位置上报周期
    public static final String MSG_TYPE_FREQUENCY_LOCATION_SET = "FREQUENCY_LOCATION_SET";


    // 设备请求
    public static final int DIRECT_DEVICE_REQ = 3;

    // 云端响应消息
    public static final int DIRECT_CLOUD_RSP = 4;

    // 云端发给设备的消息
    public static final int DIRECT_CLOUD_REQ = 1;

    // 设备返回的响应消息
    public static final int DIRECT_DEVICE_RSP = 2;

    // 消息头分隔符
    public static final String HEADER_PARS_DELIMITER  = ",";

    // 消息体分隔符
    public static final String BODY_PARS_DELIMITER = "@";

    // 消息开始标志
    public static final String MESSAGE_START_DELIMITER = "[";

    // 消息结束标志
    public static final String MESSAGE_END_DELIMITER = "]";

}
