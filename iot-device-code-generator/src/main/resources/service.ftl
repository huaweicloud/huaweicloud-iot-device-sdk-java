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

package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.service.DeviceCommand;
import com.huaweicloud.sdk.iot.device.service.Property;
import java.util.Map;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This code is generated by FreeMarker
 *
 */
public class ${service.serviceType}Service extends AbstractService
{
    private static final Logger log = LogManager.getLogger(${service.serviceType}Service.class);
    private Random random = new Random();

    /********** attribute ***********/
<#list service.properties as property>
    @Property(writeable =  ${property.writeable})
    private ${property.javaType} ${property.propertyName};

</#list>


    /********** commands ***********/
<#list service.commands as command>
    @DeviceCommand
    public CommandRsp ${command.commandName} (Map<String, Object> paras) {
      // todo 请在这里添加命令处理代码
      return new CommandRsp(0);
    }

</#list>

    /********** get/set ***********/
<#list service.properties as property>
    public ${property.javaType} get${property.propertyName?cap_first}() {
        // 模拟从传感器读取数据
        ${property.propertyName} = ${property.val};
        log.info("report property ${property.propertyName} value =  "+ ${property.propertyName});

        return ${property.propertyName};
    }

    public void set${property.propertyName?cap_first}(${property.javaType} ${property.propertyName}) {
        this.${property.propertyName} = ${property.propertyName};
        log.info("property ${property.propertyName} set to "+ ${property.propertyName});
    }

</#list>

}