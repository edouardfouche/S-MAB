/*
 * Copyright (C) 2017 Hendrik Braun
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

import org.apache.commons.math3.stat.ranking.NaturalRanking;

public class MultivariateSpearman extends MultidimensionalDependency {
    private final int DIMENSIONS;
    private final int OBJECTS;
    private double[][] values;
    private double[][] ranking;

    public MultivariateSpearman(double[][] values) {
        this.values = values;
        this.DIMENSIONS = values[0].length;
        this.OBJECTS = values.length;
        this.ranking = new double[OBJECTS][DIMENSIONS];
        rank();
    }

    public double estimate() {
        this.valueEstimate = (spearmanRho(1) + spearmanRho(2)) / 2;
        //System.out.println("Multivariate Spearman's Rho: " + this.valueEstimate);
        return this.valueEstimate;
    }

    public double h() {
        return (DIMENSIONS + 1) / (Math.pow(2, DIMENSIONS) - (DIMENSIONS + 1));
    }

    private double spearmanRho(int branch) {
        double sum = 0;
        for (int j = 0; j < OBJECTS; j++) {
            double factor = 1;
            for (int i = 0; i < DIMENSIONS; i++) {
                if (branch == 1) {
                    factor *= (1 - ranking[j][i]);
                } else if (branch == 2) {
                    factor *= ranking[j][i]; // fuer DatenSatz1 Ergebnis > 1
                } else {
                    System.out.println("ERROR");
                }
            }
            sum += factor;
        }
        return h() * (Math.pow(2, DIMENSIONS) / OBJECTS * sum - 1);
    }

    //normierte raenge
    public void rank() {
        NaturalRanking nr = new NaturalRanking();
        for (int i = 0; i < DIMENSIONS; i++) {
            double[] oneD = new double[OBJECTS];
            for (int j = 0; j < OBJECTS; j++) {
                oneD[j] = values[j][i];
            }
            double[] ranks = nr.rank(oneD);
            for (int k = 0; k < OBJECTS; k++) {
                ranking[k][i] = ranks[k] / OBJECTS; //Normierung
            }
        }
    }

    public double[][] getRanks() {
        return this.ranking;
    }
}
