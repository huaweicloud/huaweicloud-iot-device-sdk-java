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

package com.huaweicloud.sdk.iot.device.client.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.listener.DeviceMessageListener;
import com.huaweicloud.sdk.iot.device.client.listener.RawDeviceMessageListener;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.RawDeviceMessage;
import com.huaweicloud.sdk.iot.device.service.AbstractDevice;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.charset.StandardCharsets;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.crypto.*", "javax.script.*"})
public class MessageHandlerTest {
    @Mock
    private DeviceClient deviceClientMock;

    private MessageHandler messageHandlerUnderTest;

    private static class TestData {
        public byte[] payload;
        boolean isSystemFormat;
        boolean isSubDeviceMessage;

        public TestData(byte[] payload, boolean isSystemFormat, boolean isSubDeviceMessage) {
            this.payload = payload;
            this.isSystemFormat = isSystemFormat;
            this.isSubDeviceMessage = isSubDeviceMessage;
        }

        public TestData(String payload, boolean isSystemFormat, boolean isSubDeviceMessage) {
            this(payload.getBytes(), isSystemFormat, isSubDeviceMessage);
        }
    }

    @Before
    public void setUp() {
        messageHandlerUnderTest = new MessageHandler(deviceClientMock);
    }

    public void TestMessageHandler(TestData data) {
        System.out.println(new String(data.payload, StandardCharsets.UTF_8));
        final RawMessage message = new RawMessage("$oc/devices/{device_id}/sys/messages/down", data.payload);
        RawDeviceMessageListener rawDeviceMessageListenerMock = PowerMockito.mock(RawDeviceMessageListener.class);
        DeviceMessageListener deviceMessageListenerMock = PowerMockito.mock(DeviceMessageListener.class);

        AbstractDevice abstractDeviceMock = PowerMockito.mock(AbstractDevice.class);;

        when(deviceClientMock.getDeviceMessageListener()).thenReturn(deviceMessageListenerMock);
        when(deviceClientMock.getRawDeviceMessageListener()).thenReturn(rawDeviceMessageListenerMock);
        when(deviceClientMock.getDevice()).thenReturn(abstractDeviceMock);

        // Run the test
        messageHandlerUnderTest.messageHandler(message);

        // Verify the results
        assertNotEquals(null, deviceClientMock.getRawDeviceMessageListener());
        // make sure onRawDeviceMessage is called for only once

        verify(deviceMessageListenerMock, times((data.isSystemFormat && !data.isSubDeviceMessage) ? 1 : 0))
                .onDeviceMessage(any(DeviceMessage.class));

        verify(rawDeviceMessageListenerMock).onRawDeviceMessage(any(RawDeviceMessage.class));

        verify(abstractDeviceMock, times((data.isSystemFormat && data.isSubDeviceMessage) ? 1 : 0))
                .onDeviceMessage(any(DeviceMessage.class));
    }

    @Test
    public void test_not_set_callback() {
        // make sure it doesn't crash when both callback is null
        final RawMessage message = new RawMessage("$oc/devices/{device_id}/sys/messages/down", "dummy data".getBytes());

        when(deviceClientMock.getDeviceMessageListener()).thenReturn(null);
        when(deviceClientMock.getRawDeviceMessageListener()).thenReturn(null);

        // Run the test
        messageHandlerUnderTest.messageHandler(message);

        // Verify the results
        assertNull(deviceClientMock.getDeviceMessageListener());
        assertNull(deviceClientMock.getRawDeviceMessageListener());
    }

    @Test
    public void test_message_in_system_format() {
        final String deviceIdMock = "test_device_id";
        when(deviceClientMock.getDeviceId()).thenReturn(deviceIdMock);
        final TestData[] testData = {
                new TestData("{\"name\":\"1\",\"id\":\"2\",\"content\":\"3\",\"object_device_id\":null}", true, false),
                new TestData("{\"name\": null,\"id\":\"2\",\"content\":\"3\",\"object_device_id\":1}", true, true),
                new TestData("{\"name\":\"1\",\"id\":null,\"content\":\"3\",\"object_device_id\":1}", true, true),
                new TestData("{\"name\":\"1\",\"id\":\"2\",\"content\":\"3\"}", true, false), new TestData(
                String.format("{\"content\":\"3\", \"object_device_id\": \"%s\"}", deviceIdMock), true, false)};

        for (TestData d : testData) {
            TestMessageHandler(d);
        }
    }

    @Test
    public void test_message_in_non_system_format() {
        final TestData[] testData = {
                new TestData("{\"name1\":\"1\",\"id\":\"2\",\"content\":\"3\",\"object_device_id\":null}", false,
                        false),
                new TestData("{\"content\":\"3\",\"object_device_id\":1,\"object_device_id22\":1}", false, true),
                new TestData("ddf", false, true),
                new TestData("{\"name\":[1],\"id\":null,\"content\":\"3\",\"object_device_id\":1}", false, false),};

        for (TestData d : testData) {
            TestMessageHandler(d);
        }
    }

    @Test
    public void test_message_in_binary_format() {
        final TestData[] testData = {new TestData(new byte[]{0x1, 0x2, 0x3, 0x4, 0x5}, false, false),
                new TestData(new byte[]{107, 114, 91, 29}, false, false),};

        for (TestData d : testData) {
            TestMessageHandler(d);
        }
    }

    @Test
    public void test_raw_message_method() {
        String testData = "abcdefghijklmn";
        byte[] payload = testData.getBytes();
        final RawMessage message = new RawMessage("$oc/devices/{device_id}/sys/messages/down", payload);
        RawDeviceMessageListener rawDeviceMessageListenerMock = PowerMockito.mock(RawDeviceMessageListener.class);
        DeviceMessageListener deviceMessageListenerMock = PowerMockito.mock(DeviceMessageListener.class);

        when(deviceClientMock.getDeviceMessageListener()).thenReturn(deviceMessageListenerMock);
        when(deviceClientMock.getRawDeviceMessageListener()).thenReturn(rawDeviceMessageListenerMock);

        Mockito.doAnswer((Answer<Object>) a -> {
            RawDeviceMessage s = a.getArgument(0);
            assertEquals(payload, s.getPayload());
            return null;
        }).when(rawDeviceMessageListenerMock).onRawDeviceMessage(any());

        // Run the test
        messageHandlerUnderTest.messageHandler(message);

        // Verify the results
        assertNotEquals(null, deviceClientMock.getRawDeviceMessageListener());
        // make sure onRawDeviceMessage is called for only once
        verify(deviceMessageListenerMock, times(0)).onDeviceMessage(any(DeviceMessage.class));
        verify(rawDeviceMessageListenerMock).onRawDeviceMessage(any(RawDeviceMessage.class));
    }
}