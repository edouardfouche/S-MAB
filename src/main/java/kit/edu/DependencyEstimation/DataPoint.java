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

public class DataPoint {
    private double[] coordinates;

    public DataPoint(double[] coord) {
        this.coordinates = new double[coord.length];
        for (int i = 0; i < coord.length; i++) {
            coordinates[i] = coord[i];
        }
    }

    public static void print2dArray(int[][] d) {
        String s = "";
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                s += d[i][j] + ",";
            }
            s = s.substring(0, s.length() - 1);
            s += "\n";
        }
        s = s.substring(0, s.length() - 1);
        System.out.println(s);
    }

    public static void print2dArray(double[][] d) {
        String s = "";
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                s += d[i][j] + ",";
            }
            s = s.substring(0, s.length() - 1);
            s += "\n";
        }
        s = s.substring(0, s.length() - 1);
        System.out.println(s);
    }

    public double[] getCoordinates() {
        double[] result = new double[this.coordinates.length];
        for (int i = 0; i < this.coordinates.length; i++) {
            result[i] = this.coordinates[i];
        }
        return result;
    }
}
