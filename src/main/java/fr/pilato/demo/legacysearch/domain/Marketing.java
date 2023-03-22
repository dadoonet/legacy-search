package fr.pilato.demo.legacysearch.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * We define here marketing meta data:
 * Number of clicks on each segment
 */
@Entity
public class Marketing {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private Integer cars;
    private Integer shoes;
    private Integer toys;
    private Integer fashion;
    private Integer music;
    private Integer garden;
    private Integer electronic;
    private Integer hifi;
    private Integer food;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCars() {
        return cars;
    }

    public void setCars(Integer cars) {
        this.cars = cars;
    }

    public Integer getShoes() {
        return shoes;
    }

    public void setShoes(Integer shoes) {
        this.shoes = shoes;
    }

    public Integer getToys() {
        return toys;
    }

    public void setToys(Integer toys) {
        this.toys = toys;
    }

    public Integer getFashion() {
        return fashion;
    }

    public void setFashion(Integer fashion) {
        this.fashion = fashion;
    }

    public Integer getMusic() {
        return music;
    }

    public void setMusic(Integer music) {
        this.music = music;
    }

    public Integer getGarden() {
        return garden;
    }

    public void setGarden(Integer garden) {
        this.garden = garden;
    }

    public Integer getElectronic() {
        return electronic;
    }

    public void setElectronic(Integer electronic) {
        this.electronic = electronic;
    }

    public Integer getHifi() {
        return hifi;
    }

    public void setHifi(Integer hifi) {
        this.hifi = hifi;
    }

    public Integer getFood() {
        return food;
    }

    public void setFood(Integer food) {
        this.food = food;
    }
}
