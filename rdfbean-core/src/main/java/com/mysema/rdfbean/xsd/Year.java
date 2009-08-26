/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.xsd;


/**
 * @author sasa
 *
 */
public final class Year implements Comparable<Year> {

    private int year;
    
    public Year(int year) {
        this.year = year;
    }
    
    public Year(String yearString) {
        this(Integer.parseInt(yearString.trim()));
    }
    
    public int getYear() {
        return year;
    }
    
    public boolean isBc() {
        return year < 1;
    }
    
    public boolean isBce() {
        return isBc();
    }
    
    @Override 
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Year) {
            return year == ((Year) o).year; 
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return year;
    }

    @Override
    public int compareTo(Year o) {
        return year - o.year;
    }

    @Override
    public String toString() {
        return Integer.toString(year);
    }
}
