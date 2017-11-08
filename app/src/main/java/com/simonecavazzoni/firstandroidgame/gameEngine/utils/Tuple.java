package com.simonecavazzoni.firstandroidgame.gameEngine.utils;


@SuppressWarnings("WeakerAccess")
public class Tuple<X, Y> {
    private X x;
    private Y y;

    public Tuple() {
        this(null, null);
    }
    public Tuple(X x, Y y) {
        this.set(x, y);
    }
    public Tuple(Tuple<X, Y> t) {
        this.setTuple(t);
    }

    public X getX() {
        return x;
    }
    public Y getY() {
        return y;
    }
    @SuppressWarnings("unused")
    public Tuple<X, Y> getTuple() {
        return new Tuple<>(this);
    }

    public void setX(X x) {
        this.x = x;
    }
    public void setY(Y y) {
        this.y = y;
    }
    public void set(X x, Y y) {
        this.setX(x);
        this.setY(y);
    }
    public void setTuple(Tuple<X, Y> t) {
        this.set(t.getX(), t.getY());
    }
}

