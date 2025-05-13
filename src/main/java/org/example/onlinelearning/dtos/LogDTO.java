package org.example.onlinelearning.dtos;

import jakarta.persistence.*;
import lombok.Data;
import org.example.onlinelearning.models.User;

import java.time.LocalDateTime;

@Data
public class LogDTO {
        private Long id;
        private Long userId;
        private String title;
        private LocalDateTime logTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getLogTime() {
        return logTime;
    }

    public void setLogTime(LocalDateTime logTime) {
        this.logTime = logTime;
    }
}
