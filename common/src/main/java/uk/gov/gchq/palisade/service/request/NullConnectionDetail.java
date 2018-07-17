package uk.gov.gchq.palisade.service.request;

public class NullConnectionDetail implements ConnectionDetail {
    @Override
    public boolean equals(final Object obj) {
        return this.getClass().equals(obj.getClass());
    }
}
