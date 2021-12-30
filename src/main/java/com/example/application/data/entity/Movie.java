package com.example.application.data.entity;

import com.example.application.data.AbstractEntity;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class Movie extends AbstractEntity {

    @Lob
    private String image;
    private String title;
    private String describe;
    private Integer duration;

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescribe() {
        return describe;
    }
    public void setDescribe(String describe) {
        this.describe = describe;
    }
    public Integer getDuration() {
        return duration;
    }
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

}
