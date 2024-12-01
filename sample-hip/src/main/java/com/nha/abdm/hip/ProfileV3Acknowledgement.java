package com.nha.abdm.hip;



public class ProfileV3Acknowledgement {
    private String status;
    private String abhaAddress;
    private TokenProfile profile;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAbhaAddress() {
        return abhaAddress;
    }

    public void setAbhaAddress(String abhaAddress) {
        this.abhaAddress = abhaAddress;
    }

    public TokenProfile getProfile() {
        return profile;
    }

    public void setProfile(TokenProfile profile) {
        this.profile = profile;
    }
}
