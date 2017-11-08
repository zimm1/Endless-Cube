package com.simonecavazzoni.firstandroidgame.gameEngine.utils;


@SuppressWarnings("unused")
public class TupleInt extends Tuple<Integer, Integer> {

    public TupleInt() {
        this.set(0, 0);
    }
    public TupleInt(int x, int y) {
        super(x, y);
    }
    public TupleInt(Tuple<Integer, Integer> t) {
        super(t);
    }
}
