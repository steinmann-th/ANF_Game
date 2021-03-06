package com.p3212.EntityClasses;


import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;


@Entity
@Table(name = "pvp_fights")
public class FightPVP extends Fight {

    public FightPVP() {
        //pvpId = id;
        fightDate = new Date();
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pvpId;

    @ManyToOne
    @JoinColumn(name = "firstFighter", nullable = false)
    private Character firstFighter;

    @ManyToOne
    @JoinColumn(name = "secondFighter", nullable = false)
    private Character secondFighter;

    @Column(name = "fight_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fightDate;

    @Column(nullable = false)
    private boolean firstWon;

    @Column(nullable = false)
    private int ratingChange;

    @Transient
    private int biggerRatingChange;

    @Transient
    private int lessRatingChange;

    public int getBiggerRatingChange() {
        return biggerRatingChange;
    }

    public void setBiggerRatingChange(int biggerRatingChange) {
        this.biggerRatingChange = biggerRatingChange;
    }

    public int getLessRatingChange() {
        return lessRatingChange;
    }

    public void setLessRatingChange(int lessRatingChange) {
        this.lessRatingChange = lessRatingChange;
    }

    public boolean isFirstWon() {
        return firstWon;
    }

    public void setFirstWon(boolean firstWon) {
        this.firstWon = firstWon;
    }

    public int getRatingChange() {
        return ratingChange;
    }

    public void setFirstFighter(Character firstFighter) {
        this.firstFighter = firstFighter;
    }

    public void setSecondFighter(Character secondFighter) {
        this.secondFighter = secondFighter;
    }

    public Character getFirstFighter() {
        return firstFighter;
    }

    public Character getSecondFighter() {
        return secondFighter;
    }


    public void setRatingChange(int ratingChange) {
        this.ratingChange = ratingChange;
    }

    public void setFighters(Character char1, Character char2) {
        fighter1 = char1.getUser();
        fighter2 = char2.getUser();
        firstFighter = char1;
        secondFighter = char2;
    }

    public int getPvpId() {
        return pvpId;
    }

    public void setPvpId(int pvpId) {
        this.pvpId = pvpId;
    }

    public Date getFightDate() {
        return fightDate;
    }

    public User getFighter1() {
        return fighter1;
    }

    public User getFighter2() {
        return fighter2;
    }

    @Override
    public String toString() {
        return "{\"id\":" + id +
                ", \"type\": \"pvp\"" +
                ", \"fighters1\":" + fighter1.toString() +
                ", \"timeLeft\":" + timeLeft +
                ", \"currentName\":\"" + currentName +
                "\", \"fighters2\":" + fighter2.toString() + "}";
    }
}
