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

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.DatabaseUtil;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.*;
import de.lmu.ifi.dbs.elki.database.ids.DBIDFactory;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.KNNList;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.MaximumDistanceFunction;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar.RStarTreeFactory;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.strategies.bulk.SortTileRecursiveBulkSplit;
import de.lmu.ifi.dbs.elki.persistent.AbstractPageFileFactory;
import de.lmu.ifi.dbs.elki.utilities.ELKIBuilder;

import java.util.Arrays;

public class ElkiKNN {
    private final int DIMENSIONS;
    private final int OBJECTS;
    private StaticArrayDatabase dataSet;
    private DataPoint[] dataPoints;
    private int k;
    private KNNQuery knnq;
    private StaticArrayDatabase dataBase;
    private Relation<NumberVector> relation;
    //private DistanceFunction distancefunction;


    public ElkiKNN(double[][] dataSet, int k, boolean max) {
        this.DIMENSIONS = dataSet[0].length;
        this.OBJECTS = dataSet.length;


        this.dataPoints = new DataPoint[OBJECTS];

        for (int i = 0; i < OBJECTS; i++) {
            for (int j = 0; j < DIMENSIONS; j++) {
                this.dataPoints[i] = new DataPoint(dataSet[i]);
            }
        }

        DBIDFactory factory = DBIDFactory.FACTORY;

        //ArrayAdapterDatabaseConnection dbc = new ArrayAdapterDatabaseConnection(dataSet, null, 0); // Adapter to load data from an existing array.
        DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(dataSet, null, 0);

        // Following the recommendations from Schubert at https://stackoverflow.com/questions/32741510/how-to-index-with-elki-optics-clustering
        RStarTreeFactory<?> indexfactory = new ELKIBuilder<>(RStarTreeFactory.class) //
                // If you have large query results, a larger page size can be better.
                .with(AbstractPageFileFactory.Parameterizer.PAGE_SIZE_ID, 1024 * this.DIMENSIONS / 2.0) //
                // Use bulk loading, for better performance.
                .with(RStarTreeFactory.Parameterizer.BULK_SPLIT_ID, SortTileRecursiveBulkSplit.class) //
                .build();

        //this.dataBase = new StaticArrayDatabase(dbc, null); // Create a database
        this.dataBase = new StaticArrayDatabase(dbc, Arrays.asList(indexfactory));
        this.dataBase.initialize();

        this.k = k;
        //MaximumDistanceFunction.STATIC
        this.relation = this.dataBase.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
        if (max) {
            this.knnq = DatabaseUtil.precomputedKNNQuery(this.dataBase, this.relation, MaximumDistanceFunction.STATIC, this.k);
        } else {
            this.knnq = DatabaseUtil.precomputedKNNQuery(this.dataBase, this.relation, EuclideanDistanceFunction.STATIC, this.k);
        }

    }

    /**
     * @return
     */
    public DataPoint[][] getAllKNN() {
        DataPoint[][] AllkNNeighbours = new DataPoint[this.OBJECTS][k];
        int j = 0;
        for (DBIDIter iter = this.relation.iterDBIDs(); iter.valid(); iter.advance()) {
            KNNList result = this.knnq.getKNNForDBID(iter, k);
            DataPoint[] kNNeighbours = new DataPoint[k];
            int i = 0;
            for (DBIDIter niter = result.iter(); niter.valid(); niter.advance()) {
                kNNeighbours[i] = new DataPoint(this.relation.get(niter).toArray());
                i++;
            }
            AllkNNeighbours[j] = kNNeighbours;
            j++;
        }
        return AllkNNeighbours;
    }

    /**
     * @return
     */
    public double[][] getAllKNNDistances() {
        double[][] Alldistances = new double[this.OBJECTS][k];

        int j = 0;
        for (DBIDIter iter = this.relation.iterDBIDs(); iter.valid(); iter.advance()) {

            KNNList result = this.knnq.getKNNForDBID(iter, k);
            DataPoint[] kNNeighbours = new DataPoint[result.size()];
            int i = 0;
            for (DBIDIter niter = result.iter(); niter.valid(); niter.advance()) {
                kNNeighbours[i] = new DataPoint(this.relation.get(niter).toArray());
                i++;
            }

            // Get the object first
            double[] value = this.relation.get(iter).toArray();

            double[] distances = new double[DIMENSIONS];
            for (int k = 0; k < kNNeighbours[0].getCoordinates().length; k++) {
                distances[k] = -1;
                for (DataPoint p : kNNeighbours) {
                    double distance = Math.abs(p.getCoordinates()[k] - value[k]);
                    if (distance > distances[k]) {
                        distances[k] = distance;
                    }
                }
            }

            Alldistances[j] = distances;
            j++;
        }
        return Alldistances;
    }

    /**
     * get the distance to the kNN in one Dimension
     *
     * @param norm  the norm (?)
     * @return the distance to the kNN
     */
    public double[] getAllKDistance(Norm norm) {
        double[] Alldistances = new double[this.OBJECTS];

        int j = 0;
        for (DBIDIter iter = this.relation.iterDBIDs(); iter.valid(); iter.advance()) {

            KNNList result = this.knnq.getKNNForDBID(iter, k);
            //System.out.println("Number of neighbors: " + result.size());
            DataPoint[] kNNs = new DataPoint[result.size()];
            int i = 0;
            for (DBIDIter niter = result.iter(); niter.valid(); niter.advance()) {

                kNNs[i] = new DataPoint(this.relation.get(niter).toArray());
                i++;
            }

            // Get the object first
            double[] value = this.relation.get(iter).toArray();

            double distance;
            double[] k_next_neighbour = new double[value.length];
            for (int m = 0; m < value.length; m++) {
                k_next_neighbour[m] = kNNs[kNNs.length - 1].getCoordinates()[m];
            }
            distance = norm.calculateNormDistance(value, k_next_neighbour);
            Alldistances[j] = distance;
            //System.out.println("Distance: " + Arrays.toString(value) + "/" + Arrays.toString(k_next_neighbour) + " = " + distance);
            //if(j==0) {
            //    System.out.println("Distance: " + Arrays.toString(value) + "/" + Arrays.toString(k_next_neighbour) + " = " + distance);
            //    for(int kk = 0; kk < k; kk++) {
            //        System.out.println("KNN" + kk + " : " + Arrays.toString(kNNs[kk].getCoordinates()));
            //    }
            //}
            j++;
        }
        return Alldistances;
    }
}
