package com.example.application.data.entity;

import com.example.application.data.AbstractEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Entity;

@Entity
public class Session extends AbstractEntity {

    private LocalDate date;
    private LocalDateTime iniTime;
    private LocalDateTime endTime;
    private Integer value;
    private String animation;
    private String audio;

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public LocalDateTime getIniTime() {
        return iniTime;
    }
    public void setIniTime(LocalDateTime iniTime) {
        this.iniTime = iniTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public Integer getValue() {
        return value;
    }
    public void setValue(Integer value) {
        this.value = value;
    }
    public String getAnimation() {
        return animation;
    }
    public void setAnimation(String animation) {
        this.animation = animation;
    }
    public String getAudio() {
        return audio;
    }
    public void setAudio(String audio) {
        this.audio = audio;
    }

}
