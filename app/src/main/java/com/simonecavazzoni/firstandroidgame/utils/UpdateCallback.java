package com.simonecavazzoni.firstandroidgame.utils;

public interface UpdateCallback<T> {
    void onUpdate(T object);
    void onFinish(T object);
}
