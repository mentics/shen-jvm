package com.stralos.shen.model;

import com.stralos.shen.parser.FileLocation;

public class Loc {
    public static int line(Location loc) {
        return loc != null ? ((FileLocation)loc).line : -1;
    }

    public static String path(Location loc) {
        return loc != null ? ((FileLocation)loc).filename : "unknown";
    }

    public static int line(S s) {
        return line(s.getLocation());
    }

    public static int column(Location loc) {
        return loc != null ? ((FileLocation)loc).column : -1;
    }

    public static String path(S s) {
        return path(s.getLocation());
    }
}
