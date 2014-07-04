package com.dorma.weather.network.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Temperature {

    @Attribute(name = "value")
    public float value;

    public Temperature(@Attribute(name = "value") float value) {
        this.value = value;
    }
}

