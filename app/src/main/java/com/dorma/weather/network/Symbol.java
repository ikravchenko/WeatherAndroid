package com.dorma.weather.network;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Symbol {
    @Attribute(name="name")
    public String name;

    public Symbol(
                 @Attribute(name = "name") String name) {
        this.name = name;
    }
}
