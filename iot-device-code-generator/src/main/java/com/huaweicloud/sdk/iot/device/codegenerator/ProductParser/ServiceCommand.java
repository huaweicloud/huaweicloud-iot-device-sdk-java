package com.huaweicloud.sdk.iot.device.codegenerator.productparser;


import java.io.Serializable;
import java.util.List;


public class ServiceCommand implements Serializable {

    private static final long serialVersionUID = -8726398850035913800L;


    private String commandName;


    private List<ServiceCommandPara> paras;


    private List<ServiceCommandResponse> responses;

    public List<ServiceCommandResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<ServiceCommandResponse> responses) {
        this.responses = responses;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public List<ServiceCommandPara> getParas() {
        return paras;
    }

    public void setParas(List<ServiceCommandPara> paras) {
        this.paras = paras;
    }

    @Override
    public String toString() {

        return new StringBuilder().append("ServiceCommand [commandName=").append(commandName).append(", paras=")
                .append(paras).append(", responses=").append(responses).append("]").toString();

    }

}

