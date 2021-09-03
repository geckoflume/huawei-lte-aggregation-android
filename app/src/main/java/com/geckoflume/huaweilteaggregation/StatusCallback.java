package com.geckoflume.huaweilteaggregation;

public interface StatusCallback {
    void updateLoginTextView(boolean status, boolean init);

    void updateBandTextView(String band, int bandNumber, boolean status, boolean init);
}