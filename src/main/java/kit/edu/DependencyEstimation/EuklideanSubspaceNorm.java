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

public class EuklideanSubspaceNorm extends EuklideanNorm {
    boolean[] indices;

    public EuklideanSubspaceNorm() {
        this.indices = null;
    }

    public EuklideanSubspaceNorm(boolean[] indices) {
        this.indices = indices;
    }

    @Override
    public double calculateNorm(double[] value) {
        double normValue;
        if (this.indices == null) {
            normValue = 0.0;
            int dimensionality = value.length;
            for (int i = 0; i < dimensionality; i++) {
                normValue += Math.pow(value[i], 2);
            }
            normValue = Math.sqrt(normValue);
        } else {
            normValue = 0.0;
            int dimensionality = value.length;
            for (int i = 0; i < dimensionality; i++) {
                if (indices[i]) {
                    normValue += Math.pow(value[i], 2);
                }
            }
            normValue = Math.sqrt(normValue);
        }
        return normValue;
    }
}
