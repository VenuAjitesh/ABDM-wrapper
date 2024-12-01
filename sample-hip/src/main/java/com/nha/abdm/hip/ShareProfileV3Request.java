package com.nha.abdm.hip;

public class ShareProfileV3Request {
    private String token;

    public String getHipId() {
        return hipId;
    }

    public void setHipId(String hipId) {
        this.hipId = hipId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PatientV3Details getPatient() {
        return patient;
    }

    public void setPatient(PatientV3Details patient) {
        this.patient = patient;
    }

    private String hipId;
    private PatientV3Details patient;
    private String context;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
