package protocol;

class Polynomial {

    private int[] coefficients;

    Polynomial(int[] coefficients) {
        this.coefficients = coefficients;
    }

    public void setCoeffIndex(int i, int val)
    {
        this.coefficients[i] = val;
    }

    public int[] getCoeffs()
    {
        return this.coefficients;
    }
}
