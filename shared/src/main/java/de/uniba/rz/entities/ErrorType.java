package de.uniba.rz.entities;


import javax.xml.bind.annotation.XmlEnum;

@XmlEnum(String.class)
public enum ErrorType {
    INVALID_PARAMETER,
    UNDEFINED,
    MALFORMED_BODY,
    RESOURCE_NOT_FOUND,
    QUANTITY_EXCEEDED,
    ALREADY_PROCESSED,
    SAME_STATE
}
