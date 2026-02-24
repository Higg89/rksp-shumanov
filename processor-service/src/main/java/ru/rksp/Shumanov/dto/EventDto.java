package ru.rksp.Shumanov.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDto {

    @JsonProperty("идентификатор")
    @JsonAlias("идентификатор")
    private String идентификатор;

    @JsonProperty("фио_сотрудника")
    @JsonAlias({"фио_сотрудника", "фиоСотрудника"})
    private String фиоСотрудника;

    @JsonProperty("номер_пропуска")
    @JsonAlias({"номер_пропуска", "номерПропуска"})
    private String номерПропуска;

    @JsonProperty("результат_проверки")
    @JsonAlias({"результат_проверки", "результатПроверки"})
    private String результатПроверки;

    @JsonProperty("дата_события")
    @JsonAlias({"дата_события", "датаСобытия"})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
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
