package model;

public record SpinnerKV<T>(T value, String label) {

    @Override
    public String toString() {
        return label;
    }
}
