package de.uniba.rz.entities;

import java.io.Serializable;

public class SearchDto implements Serializable {

    private String name;
    private Type type;

    public SearchDto() {
    }

    public SearchDto(String name) {
        this.name = name;
    }

    public SearchDto(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}
