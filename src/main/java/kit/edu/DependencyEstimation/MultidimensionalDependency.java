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

public abstract class MultidimensionalDependency {
    protected double valueEstimate = 0;
    protected int k_nearestNeighbour;
    protected double[][] values;


    public double getValueEstimate() {
        return valueEstimate;
    }

    public void setValueEstimate(double valueEstimate) {
        this.valueEstimate = valueEstimate;
    }

    /**
     * estimates the value of the multidimensional dependency of the file
     *
     * @return the estimated value
     */
    public abstract double estimate();
}
