package edu.columbia.cs.sdarts.backend.www.classifier;

public class ProfileEntry implements Comparable {
    String term;
    double of;
    double rf;

    public ProfileEntry() {
        this.term = new String();
        this.of = -1;
        this.rf = -1;
    }

    public ProfileEntry(String term) {
        this.term = term.toLowerCase();
        this.of = 1;
        this.rf = -1;
    }

    public ProfileEntry(String term, double of, double rf) {
        this.term = term.toLowerCase();
        this.of = of;
        this.rf = rf;
    }

    public void increaseOF() {
        this.of++;
    }

    public void set(ProfileEntry p) {
        this.term = p.term;
        this.of = p.of;
        this.rf = p.rf;
    }

    public void setOF(double observedFreq) {
        this.of = observedFreq;
    }

    public void setRF(double realFreq) {
        this.rf = realFreq;
    }

    public String getTerm() {
        return new String(this.term);
    }

    public double getRF() {
        return this.rf;
    }

    public double getOF() {
        return this.of;
    }

    public String toString() {
        return new String(this.term+"#"+this.of+"#"+this.rf);
    }

    public int compareTo(Object o) {
        ProfileEntry pe = null;

        try {
            pe = (ProfileEntry) o;
        }
        catch (Exception e) {
            throw new ClassCastException("Trying to compare a ProfileEntry with a non-compatible class") ;
        }

        return this.term.compareTo(pe.getTerm());

    }

}
