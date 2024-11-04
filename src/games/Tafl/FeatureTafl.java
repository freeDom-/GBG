package games.Tafl;

import games.Feature;
import games.StateObservation;

import java.io.Serial;
import java.io.Serializable;

public class FeatureTafl implements Feature, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public FeatureTafl(int featMode) {

    }

    @Override
    public double[] prepareFeatVector(StateObservation so) {
        return new double[0];
    }

    @Override
    public String stringRepr(double[] featVec) {
        return "";
    }

    @Override
    public int getFeatmode() {
        return 0;
    }

    @Override
    public int[] getAvailFeatmode() {
        return new int[0];
    }

    @Override
    public int getInputSize(int featMode) {
        return 0;
    }
}
