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

public class EuklideanNorm extends Norm {

    @Override
    public double calculateNorm(double[] value) {
        double euklideanValue = 0;
        int dimensionality = value.length;
        for (int i = 0; i < dimensionality; i++) {
            euklideanValue += Math.pow(value[i], 2);
        }
        euklideanValue = Math.sqrt(euklideanValue);
        return euklideanValue;
    }
}
