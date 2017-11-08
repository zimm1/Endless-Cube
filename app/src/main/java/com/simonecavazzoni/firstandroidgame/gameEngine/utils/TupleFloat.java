package com.simonecavazzoni.firstandroidgame.gameEngine.utils;


public class TupleFloat extends Tuple<Float, Float> {

    public TupleFloat() {
        this.set((float)0, (float)0);
    }
    public TupleFloat(float x, float y) {
        super(x, y);
    }
    public TupleFloat(Tuple<Float, Float> t) {
        super(t);
    }
}