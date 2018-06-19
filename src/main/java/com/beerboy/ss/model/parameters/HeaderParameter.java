package com.beerboy.ss.model.parameters;

public class HeaderParameter extends AbstractSerializableParameter<HeaderParameter> {

    public HeaderParameter() {
        super.setIn("header");
    }

    @Override
    protected String getDefaultCollectionFormat() {
        return "multi";
    }
}
