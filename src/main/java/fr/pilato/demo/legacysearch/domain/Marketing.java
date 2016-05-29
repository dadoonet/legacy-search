package fr.pilato.demo.legacysearch.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * We define here marketing meta data:
 * Number of clicks on each segment
 */
@Entity
public class Marketing {
    private Integer id = null;

    private Integer cars;
    private Integer shoes;
    private Integer toys;
    private Integer fashion;
    private Integer music;
    private Integer garden;
    private Integer electronic;
    private Integer hifi;
    private Integer food;

    /**
     * Gets id (primary key).
     */
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @JsonIgnore
    public Integer getId() {
        return id;
    }

    /**
     * Sets id (primary key).
     */
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

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.getClass().getName() + "-");
        sb.append("  cars=" + cars);
        sb.append("  shoes=" + shoes);
        sb.append("  toys=" + toys);
        sb.append("  fashion=" + fashion);
        sb.append("  music=" + music);
        sb.append("  garden=" + garden);
        sb.append("  electronic=" + electronic);
        sb.append("  hifi=" + hifi);
        sb.append("  food=" + food);

        return sb.toString();
    }

    /**
     * Indicates whether some other object is equal to this one.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Marketing other = (Marketing) obj;

        if (cars != other.cars) return false;
        if (shoes != other.shoes) return false;
        if (toys != other.toys) return false;
        if (fashion != other.fashion) return false;
        if (music != other.music) return false;
        if (garden != other.garden) return false;
        if (electronic != other.electronic) return false;
        if (hifi != other.hifi) return false;
        if (food != other.food) return false;

        return true;
    }



}
