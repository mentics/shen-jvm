package com.stralos.shen.parser;

import com.stralos.shen.model.Location;


public class FileLocation implements Location {
    public final String filename;
    public final int line;
    public final int column;


    public FileLocation(String filename, int line, int column) {
        super();
        this.filename = filename;
        this.line = line;
        this.column = column;
    }
}
