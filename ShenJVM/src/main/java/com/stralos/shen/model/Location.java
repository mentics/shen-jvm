package com.stralos.shen.model;

import com.stralos.shen.parser.FileLocation;

public interface Location {
    Location UNKNOWN = new FileLocation("UNKNOWN", -1, -1);
}
