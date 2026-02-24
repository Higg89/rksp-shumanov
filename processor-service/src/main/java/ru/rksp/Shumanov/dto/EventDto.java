package ru.rksp.Shumanov.dto;

import java.time.LocalDateTime;

public class EventDto {

    private String идентификатор;
    private String фиоСотрудника;
    private String номерПропуска;
    private String результатПроверки;
    private LocalDateTime датаСобытия;

    public String getИдентификатор() {
        return идентификатор;
    }

    public void setИдентификатор(String идентификатор) {
        this.идентификатор = идентификатор;
    }

    public String getФиоСотрудника() {
        return фиоСотрудника;
    }

    public void setФиоСотрудника(String фиоСотрудника) {
        this.фиоСотрудника = фиоСотрудника;
    }

    public String getНомерПропуска() {
        return номерПропуска;
    }

    public void setНомерПропуска(String номерПропуска) {
        this.номерПропуска = номерПропуска;
    }

    public String getРезультатПроверки() {
        return результатПроверки;
    }

    public void setРезультатПроверки(String результатПроверки) {
        this.результатПроверки = результатПроверки;
    }

    public LocalDateTime getДатаСобытия() {
        return датаСобытия;
    }

    public void setДатаСобытия(LocalDateTime датаСобытия) {
        this.датаСобытия = датаСобытия;
    }
}
