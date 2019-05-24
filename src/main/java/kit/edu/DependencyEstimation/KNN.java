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


import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * @author Hendrik
 *
 */
public class KNN {
    private DataPoint[] dataSet;
    private final int DIMENSIONS;
    private final int OBJECTS;
    private int k;


    public KNN(double[][] dataSet, int k){
        this.DIMENSIONS = dataSet[0].length;
        this.OBJECTS = dataSet.length;
        this.dataSet = new DataPoint[OBJECTS];

        for(int i = 0; i < OBJECTS; i++){
            double[] point = new double[DIMENSIONS];
            for(int j = 0; j < DIMENSIONS; j++){
                point[j] = dataSet[i][j];
            }
            this.dataSet[i] = new DataPoint(point);
        }
        this.k = k;

    }

    public DataPoint[] getKNearestNeighbours(DataPoint value){
        return this.getKNearestNeighbours(value, this.k, new MaximumNorm());
    }

    public DataPoint[] getKNearestNeighbours(DataPoint value, int k){
        return this.getKNearestNeighbours(value, k, new MaximumNorm());
    }

    public DataPoint[] getKNearestNeighbours(DataPoint value, Norm norm){
        return this.getKNearestNeighbours(value, this.k, norm);
    }
    // be careful, the TreeMap doesn't allow two different values with the same key
    // because of that we add 10^-15 to the duplicated key
    /**
     *
     * @param value
     * @param k
     * @param norm
     * @return
     */
    public DataPoint[] getKNearestNeighbours(DataPoint value, int k, Norm norm){

        TreeMap<Double, DataPoint> sortedTree = new TreeMap<Double, DataPoint>();
        if (k > OBJECTS){
            System.out.println("Error, not enough objects to find k neighbours");
            return null;
        }
        for(int i = 0; i < k; i++){ // just copy the first k elements of our array
            double distance = norm.calculateNormDistance(dataSet[i].getCoordinates(),value.getCoordinates());
            // we have a duplicate key in our tree
            int power = -20;
            while(sortedTree.containsKey(distance)){
                while(distance == (distance + Math.pow(10, power))){
                    power++;
                }
                distance += Math.pow(10, power); // add a really small number which should hopefully not be a problem
            }
            sortedTree.put(distance, dataSet[i]);
        }

        for(int i = k; i < OBJECTS; i++){
            double distance = norm.calculateNormDistance(dataSet[i].getCoordinates(),value.getCoordinates());
            if (distance < sortedTree.lastKey()){
                sortedTree.remove(sortedTree.lastKey());
                // we have a duplicate key in our tree
                int power = -20;
                while(sortedTree.containsKey(distance)){
                    while(distance == (distance + Math.pow(10, power))){
                        power++;
                    }
                    distance += Math.pow(10, power); // add a really small number which should hopefully not be a problem
                }
                sortedTree.put((Double) distance, dataSet[i]);
            }
        }
        Collection<DataPoint> neighbours = sortedTree.values();
        DataPoint[] kNNeighbours = new DataPoint[k];
        int iter = 0;
        Iterator<DataPoint> iterator = neighbours.iterator();
        while (iterator.hasNext()){
            kNNeighbours[iter] = iterator.next();
            iterator.remove();
            iter++;
        }

        return kNNeighbours;
    }


    /**
     *
     * @param value
     * @return
     */
    public double[] getKNNDistances(double[] value){
        return getKNNDistances(value, new MaximumNorm());
    }
    /**
     *
     * @param value
     * @return
     */
    public double[] getKNNDistances (double[] value, Norm norm){
        DataPoint[] kNNs = this.getKNearestNeighbours(new DataPoint(value), norm);
        double[] result = new double[value.length];
        for(int i = 0; i < kNNs[0].getCoordinates().length; i++){
            result[i] = -1;
            for(DataPoint p : kNNs){
                double distance = Math.abs(p.getCoordinates()[i] - value[i]);
                if(distance > result[i]){
                    result[i] = distance;
                }
            }
        }
        return result;
    }

    /**
     * get the distance to the kNN in one Dimension
     * @param dimensionVector we need all values in this dimension
     * @param value and the 1D value of which we want to calculate the distance to
     * @return the distance to the kNN
     */
    public double getKDistance (double[] value, Norm norm){
        DataPoint[] kNNs = this.getKNearestNeighbours(new DataPoint(value), norm);
        double distance = -1;
        double[] k_next_neighbour = new double[kNNs[0].getCoordinates().length];
        for(int i = 0; i < kNNs[0].getCoordinates().length; i++){
            k_next_neighbour[i] = kNNs[kNNs.length-1].getCoordinates()[i];
        }
        distance = norm.calculateNormDistance(value, k_next_neighbour);
        return distance;
    }


}
