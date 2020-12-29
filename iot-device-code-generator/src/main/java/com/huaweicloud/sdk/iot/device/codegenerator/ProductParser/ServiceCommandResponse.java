package com.huaweicloud.sdk.iot.device.codegenerator.productparser;


import java.io.Serializable;
import java.util.List;


public class ServiceCommandResponse implements Serializable {
    private static final long serialVersionUID = 4535630761027464385L;


    private String responseName;

    private List<ServiceCommandPara> paras;

    public String getResponseName() {
        return responseName;
    }

    public void setResponseName(String responseName) {
        this.responseName = responseName;
    }

    public List<ServiceCommandPara> getParas() {
        return paras;
    }

    public void setParas(List<ServiceCommandPara> paras) {
        this.paras = paras;
    }

    @Override
    public String toString() {

        return new StringBuilder().append("ServiceCommandResponse [responseName=").append(responseName)
                .append(", paras=").append(paras).append("]").toString();

    }


}
