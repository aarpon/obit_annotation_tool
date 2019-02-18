package ch.ethz.scu.obit.processors.data.model;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;

public class Tag {

    private Sample tag;

    public Tag(Sample tag) {

        this.tag = tag;
    }

    @Override
    public String toString() {
        return this.tag.getCode();
    }

    public String getIdentifier() {
        return tag.getIdentifier().toString();
    }
}
