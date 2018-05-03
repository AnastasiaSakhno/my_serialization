package com.ask.serialization.streams.test.entities;

import java.io.Serializable;

public class Animal implements Serializable {
    private boolean vegetarian;
    private int noOfLegs;

    public Animal() {
    }

    public Animal(boolean veg, int legs) {
        this.vegetarian = veg;
        this.noOfLegs = legs;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public int getNoOfLegs() {
        return noOfLegs;
    }

    public void setNoOfLegs(int noOfLegs) {
        this.noOfLegs = noOfLegs;
    }
}
