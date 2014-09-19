package net.ddns.mucaho.jnetrobust.util;

import java.util.Comparator;

public class SequenceComparator implements Comparator<Short> {
    public final static SequenceComparator instance = new SequenceComparator();


    public final static int MAX_SEQUENCE = Short.MAX_VALUE - Short.MIN_VALUE + 1;


    @Override
    public final int compare(Short seq1, Short seq2) {
        return difference(seq1 - Short.MIN_VALUE, seq2 - Short.MIN_VALUE, MAX_SEQUENCE);
    }


    /*
     * Example: maxSequence = 100; maxSequence/2 = 50;
     * seq1 = 10;
     * seq2 = 30;
     * diff = -20;
     * abs(-20 ) <= 50  ==>  return -20;
     *
     * seq1 = 30;
     * seq2 = 10;
     * diff = 20;
     * abs(20) <= 50  ==>  return 20;
     *
     *
     * seq1 = 70;
     * seq2 = 10;
     * diff = 60;
     * abs(60) !<= 50  ==>  return (-1 * 100) + 60 = -40
     *
     * seq1 = 10;
     * seq2 = 70;
     * diff = -60;
     * abs(-60) !<= 50  ==> return (--1 * 100) - 60 = 40
     * 
     */
    private final static int difference(int seq1, int seq2, int maxSequence) {
        int diff = seq1 - seq2;
        if (Math.abs(diff) <= maxSequence / 2)
            return diff;
        else
            return (-Integer.signum(diff) * maxSequence) + diff;
    }
}
