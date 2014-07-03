package com.dorma.weather.network.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Time {

    @Attribute(name="from")
    public String startTime;
    @Attribute(name="to")
    public String endTime;
    @Element(name="symbol")
    public Symbol symbol;
    @Element(name="temperature")

    public Temperature temperature;
    public Time(@Attribute(name = "from") String startTime,
                @Attribute(name = "to") String endTime,
                @Element(name = "symbol") Symbol symbol,
                @Element(name = "temperature") Temperature temperature) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.symbol = symbol;
        this.temperature = temperature;
    }


}
