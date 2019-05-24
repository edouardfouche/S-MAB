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

public class MaximumNorm extends Norm {

    @Override
    public  double calculateNorm(double[] value) {
        double maxValue = -1;
        for(int i = 0; i < value.length; i++){
            if (Math.abs(value[i])> maxValue){
                maxValue = Math.abs(value[i]);
            }
        }
        if (maxValue < 0){
            // System.out.println("Error while calculating Maximumnorm");
            return 0;
        }
        return maxValue;
    }

}
