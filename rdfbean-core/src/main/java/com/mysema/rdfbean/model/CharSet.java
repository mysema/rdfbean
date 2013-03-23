package com.mysema.rdfbean.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author tiwe
 * 
 */
public class CharSet {

    private final Set<Character> chars = new HashSet<Character>();

    private CharSet(String chars) {
        int prev = -1;
        for (int i = 0; i < chars.length(); i++) {
            char current = chars.charAt(i);
            if (current == '-') {
                prev = chars.charAt(i - 1);
                continue;
            } else if (prev > -1) {
                while (prev < current) {
                    this.chars.add((char) prev++);
                }
                prev = -1;
            }
            this.chars.add(current);
        }
    }

    public static CharSet getInstance(String chars) {
        return new CharSet(chars);
    }

    public boolean contains(char ch) {
        return chars.contains(ch);
    }

}
