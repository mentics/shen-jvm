package com.stralos.shen.model;

public abstract class Atom implements S {
    protected Location loc;


    public Atom(Location loc) {
        this.loc = loc;
    }

    public Location getLocation() {
        return loc;
    }
}
