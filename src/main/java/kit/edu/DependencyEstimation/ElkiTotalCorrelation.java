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

public class ElkiTotalCorrelation extends MultidimensionalDependency {
    private final int DIMENSIONS;
    private final int OBJECTS;
    private ElkiKNN knn;
    private double[][] KNNDistances;

    /**
     * @param values are the values of the dataset of which we want to calculate the total correlation (row-oriented)
     * @param k      parameter for k-nearest neighbour (we use k+1 because each object is it's own 1NN)
     */

    public ElkiTotalCorrelation(double[][] values, int k) {
        k_nearestNeighbour = k;
        this.values = values;
        this.DIMENSIONS = values[0].length;
        this.OBJECTS = values.length;
        this.knn = new ElkiKNN(this.values, this.k_nearestNeighbour + 1, true);
        this.KNNDistances = this.knn.getAllKNNDistances();
    }


    /**
     * this is the Estimation for NoElkiTotalCorrelation based on "Estimating Mutual Information by Alexander Kraskov from 2008"
     *
     * @return the estimated NoElkiTotalCorrelation of our DataSet in nat (natural unit of information)
     */
    @Override
    public double estimate() {
        int[][] bins = getAllBinCardinalities(); // first of all we need the binCardinalities for all objects
        double digammaBinSum = 0; // initialize the binSum with zero
        for (int i = 0; i < OBJECTS; i++) { // we need the sum over the digammaValue of all bins
            for (int j = 0; j < DIMENSIONS; j++) {
                digammaBinSum += Digamma.digamma(bins[i][j]);
                // System.out.println("digammaBin: " + bins[i][j] + " => " + digammaBinSum); // Interesting
            }
        }
        //System.out.println("digammaBinSum: " + digammaBinSum);
        double digammaBinSumEstimation = digammaBinSum / OBJECTS; // then we have to normalize this same by dividing through the number of objects
        double k = this.k_nearestNeighbour;
        double estimation = Digamma.digamma(k) - ((DIMENSIONS - 1) / (k)) // this is the calculation base on equation 30
                + (DIMENSIONS - 1) * Digamma.digamma(OBJECTS) - digammaBinSumEstimation;
        double binaryEstimation = estimation * 1.443; // why times 1.443? to convert nat information to binary information you have to take nat times 1.443
        // System.out.println("NoElkiTotalCorrelation in Bit: " + binaryEstimation);
        this.valueEstimate = binaryEstimation;
        //System.out.println("binaryEstimation: " + binaryEstimation);
        return this.valueEstimate;
    }

    /**
     * @return the cardinalities of each dimensionBin for this object
     */
    public int[] getBinCardinalities(int id) {
        int[] binCounter = new int[DIMENSIONS];
        // we initialize the binCounter Array with zeroes
        for (int i = 0; i < DIMENSIONS; i++) {
            binCounter[i] = 0;
        }

        //DataPoint distanc = knn.guenther(new DataPoint(value), new MaximumNorm());
        //double[] distance = distanc.getCoordinates();

        double[] value = this.values[id];
        double[] distance = this.KNNDistances[id]; // we get the distance in each dimension for our value
        /*
         * what is this distance?
		 * we take the k next neighbors and the distances in each dimension
		 * this results in k x DIMENSIONS Array of distances
		 * we take the maximum in each dimension and put them into a single vector
		 * this is our distance vector
		 */
        for (int j = 0; j < DIMENSIONS; j++) { // iterate over all dimensions (by that for all bins)
            for (int k = 0; k < OBJECTS; k++) { // we look at all objects
                if (Math.abs(value[j] - values[k][j]) <= distance[j]) { // has this object a smaller distance in this dimension than our k distance
                    binCounter[j] = binCounter[j] + 1; // yes? than we have one more object in our dimensionBin
                }
            }
            binCounter[j] = binCounter[j] - 1;
        }
        return binCounter;
    }

    /**
     * calculate for each object (and each dimension of this object, so called bins):
     * how many objects have a smaller distance in this dimension than the object, the number is the so called binCardinality
     *
     * @return a complete 2d array of all binCardinalities
     */
    public int[][] getAllBinCardinalities() {
        int[][] allCardinalities = new int[OBJECTS][DIMENSIONS];
        for (int i = 0; i < OBJECTS; i++) { // for each object
            //double[] v = new double[DIMENSIONS]; // we have to calculate the objectVector
            //for (int j = 0; j < DIMENSIONS; j++) { // each dimension of this object
            //    v[j] = values[i][j];
            //}
            // v is now a double[] which is the vector of this object
            allCardinalities[i] = getBinCardinalities(i); // we calculate the Cardinalities for each object and safe them
        }
        return allCardinalities;
    }

}
