package com.example.application.data.entity;

import com.example.application.data.AbstractEntity;
import javax.persistence.Entity;

@Entity
public class Room extends AbstractEntity {

    private String name;
    private Integer seat;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getSeat() {
        return seat;
    }
    public void setSeat(Integer seat) {
        this.seat = seat;
    }

}
