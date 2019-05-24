/*
 * Copyright (C) 2017 Hendrik Braun, Edouard Fouché
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package kit.edu.DependencyEstimation;

public class ElkiInteractionInformation extends MultidimensionalDependency {
    private final int DIMENSIONS;
    private final int OBJECTS;
    private boolean[] subspace; // true equals dimension used in this subspace, false equals dimension unused

    /**
     * @param values are the values of the dataset of which we want to calculate the total correlation
     * @param k      parameter for k-nearest neighbor (we use k+1 because each object is it's own 1NN)
     */
    public ElkiInteractionInformation(double[][] values, int k) {
        k_nearestNeighbour = k;
        this.values = values;
        this.DIMENSIONS = values[0].length;
        this.OBJECTS = values.length;

        this.subspace = new boolean[DIMENSIONS];
        for (int j = 0; j < DIMENSIONS; j++) {
            subspace[j] = true;
        }
    }

    /**
     * @return the estimated InteractionInformation in nat (natural unit of information) based on equation 10 from paper:
     * "Multivariate information measures: an experimentalist's perspective" by Nicholas Timme from 2012
     * the entropys are based on the paper:
     * "Estimating Mutual Information" by Alexander Kraskov from 2008
     * equations 20-22
     */
    @Override
    public double estimate() {
        double entropy = calculateEntropy(/* of the whole dataSet*/);
        //System.out.println("Total Entropy: "+ entropy);

        this.valueEstimate += entropy; // entropy for the whole dataset
        while (reduceSubspace()) { // for all subspaces != 0 dimensions and != all dimensions
            int currentDimension = 0;
            for (int i = 0; i < DIMENSIONS; i++) { // count number of current dimensions
                if (subspace[i]) {
                    currentDimension++;
                }
            }
            double Sentropy = calculateEntropy(/*this.subspace*/);
            //System.out.println("Subspace Entropy: "+ Sentropy);
            this.valueEstimate += Math.pow((-1), DIMENSIONS - currentDimension) * Sentropy; // equation 10
            //System.out.println("Value estimate: "+ this.valueEstimate);
        }
        this.valueEstimate *= (-1); // we have to take the negative value (as stated in equation 10)
        double binaryEstimation = this.valueEstimate * 1.443; // bit = 1.443 * nat
        //System.out.println("InteractionInformation in Bit: " + binaryEstimation);
        this.valueEstimate = binaryEstimation;
        for (int j = 0; j < DIMENSIONS; j++) {
            subspace[j] = true;
        }
        return this.valueEstimate;
    }

    /**
     * the entropies are based on the paper:
     * "Estimating Mutual Information" by Alexander Kraskov from 2008
     * equations 20-22
     *
     * @return returns the entropy of the current this.subspace of the data
     */
    public double calculateEntropy(/*current subspace*/) {
        double sum = 0;
        int currentDimension = 0;
        for (int i = 0; i < DIMENSIONS; i++) { // count number of current dimensions
            if (subspace[i]) {
                currentDimension++;
            }
        }

        // getAllKDDistance in the current subspace (recall an ElkiKNN)
        double[][] subdata = new double[OBJECTS][currentDimension];

        if (currentDimension == DIMENSIONS) {
            subdata = values;
        } else {
            for (int i = 0; i < OBJECTS; i++) {
                int c = 0;
                for (int j = 0; j < DIMENSIONS; j++) {
                    if (subspace[j]) {
                        subdata[i][c] = values[i][j];
                        c++;
                    }
                }
            }
        }

        //System.out.println("Subspace Dimensionality: " + currentDimension);
        ElkiKNN knn = new ElkiKNN(subdata, this.k_nearestNeighbour + 1, false);
        double[] AllKDistances = knn.getAllKDistance(new EuklideanNorm());

        for (int i = 0; i < OBJECTS; i++) {
            //if(i==0) {System.out.println("KDistance of first object " + AllKDistances[i]);};
            //double[] v = new double[DIMENSIONS];
            //for (int j = 0; j < DIMENSIONS; j++) {
            //    v[j] = values[i][j];
            //}
            //if(currentDimension == DIMENSIONS && i == 0){System.out.println("KDistance: " + AllKDistances[i]);};
            sum += Math.log(2 * AllKDistances[i]); // only use the current subspace dimensions to calculate the distance
        }
        //System.out.println("Sum: " + sum);
        double dimensionality = currentDimension;
        double result = -Digamma.digamma(this.k_nearestNeighbour) + Digamma.digammaOpt(OBJECTS) + Math.log(this.volumeOfSubspaceUnitBall()) + (dimensionality / OBJECTS) * sum;
        double binaryResult = result * 1.443;
        // System.out.println("Anzahl Dimensionen: " + currentDimension + "; Entropie in Bit: " + binaryResult);
        //System.out.println("Result: " + binaryResult);
        return result;
    }

    /**
     * this method reduces the current used subspace of dimensions
     *
     * @return returns true, if there are dimensions left after the reduction, returns false if all dimensions are unused
     */
    private boolean reduceSubspace() {
        int intValue = 0;
        boolean status = false;
        for (int i = 0; i < DIMENSIONS; i++) {
            if (this.subspace[i]) {
                intValue += Math.pow(2, i);
            }
        }
        intValue -= 1;
        for (int i = 0; i < DIMENSIONS; i++) {
            if (intValue % 2 == 1) {
                subspace[i] = true;
                status = true;
            } else {
                subspace[i] = false;
            }
            intValue /= 2;
        }
        return status;
    }

    private double volumeOfSubspaceUnitBall() {
        int currentDimensions = 0;
        for (int i = 0; i < subspace.length; i++) {
            if (subspace[i]) {
                currentDimensions++;
            }
        }
        if (currentDimensions % 2 == 0) { // even number of dimensions
            return Math.pow(Math.PI, (currentDimensions / 2)) / fac(currentDimensions / 2);

        } else { //odd
            return Math.pow(Math.PI, (currentDimensions / 2)) * Math.pow(2, (currentDimensions / 2 + 1)) / doublefac(currentDimensions);
        }
    }

    private double fac(int value) {
        if (value == 1 || value == 0) {
            return 1;
        } else {
            return value * fac(value - 1);
        }
    }

    private double doublefac(int value) {
        double result = 1;
        if (value % 2 == 0) { // even
            for (int i = 1; i <= (value / 2); i++) {
                result *= 2 * i;
            }
        } else {//odd
            for (int i = 1; i <= ((value / 2) + 1); i++) {
                result *= (2 * i - 1);
            }
        }
        return result;
    }
}
