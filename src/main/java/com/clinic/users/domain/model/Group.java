package com.clinic.users.domain.model;

public enum Group {

    MEDICO("doctor"),
    ENFERMERA("nurse"),
    ADMINISTRATIVO("administrative"),
    RRHH("humanR"),
    SOPORTE("support");

    private final String iamName;

    Group(String iamName) {
        this.iamName = iamName;
    }

    public String getIamName() {
        return iamName;
    }
}
