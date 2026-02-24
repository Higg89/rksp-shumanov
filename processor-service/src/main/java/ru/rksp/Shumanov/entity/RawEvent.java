package ru.rksp.Shumanov.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "сырые_события_пропусков")
public class RawEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "идентификатор")
    private Long идентификатор;

    @Column(name = "фио_сотрудника")
    private String фиоСотрудника;

    @Column(name = "номер_пропуска")
    private String номерПропуска;

    @Column(name = "результат_проверки")
    private String результатПроверки;

    @Column(name = "дата_события")
    private LocalDateTime датаСобытия;

    public Long getИдентификатор() {
        return идентификатор;
    }

    public void setИдентификатор(Long идентификатор) {
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
