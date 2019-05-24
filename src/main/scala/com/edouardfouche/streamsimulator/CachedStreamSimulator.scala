/*
 * Copyright (C) 2018 Edouard Fouché
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
package com.edouardfouche.streamsimulator

import com.edouardfouche.preprocess.DataRef

/*
The purpose of this class is to simulate a stream from a static dataset, based on a sliding window
In opposition to the "StreamSimulator", it does NOT compute the action.
The assumption is that the DataRef (dataset) points to a pre-computed stream.
 */
/**
  * The purpose of this class is to simulate a stream from a static dataset, based on a sliding window
  * In opposition to the "StreamSimulator", it does NOT compute the action.
  * The assumption is that the DataRef (dataset) points to a pre-computed stream.
  * @param dataset a reference to a data set, which actually is the corresponding action matrix
  */
case class CachedStreamSimulator(dataset: DataRef) extends Simulator {
  require(dataset.category == "cache")

  val data = dataset.open() // in CachedStreamSimulator, the dataset is the cache
  val id = dataset.id
  val nbatches: Int = data.length
  val npairs: Int = data(0).length

  // Note: The assumption is that the number of columns in the given data set corresponds to the number of combination
  // in a matrix, i.e., that there exists a n such that n(n-1)/2 = data(0) (number of dimensions/pairs)
  val ncols: Int = ((1.0/2.0)*(scala.math.sqrt(8.0*data(0).length+1.0)+1.0)).toInt // this gives back the number of cols from the number of pairs

  val pairs: Array[(Int, Int)] = (0 until npairs).map(x => (x,x)).toArray // obviously, this one is not so relevant.

  val windowSize = 1
  val stepSize = 1
  val action_name = "U"

  // The cache holds the result of the computation of the whole stream, in that case, it actually just the data
  lazy val cache: Array[Array[Double]] = data

  // Returns the precomputed actions in the next window.
  def next: Array[Array[Double]] = {
    if (state > nbatches-1) Array[Array[Double]]()
    else {
      val window = Array(data(state))
      state += 1
      window
    }
  }

  def nextAndCompute(indexes: Array[Int]): Array[Double] = {
    if (state > nbatches-1) Array[Double]()
    else {
      val actions = indexes.map(x => data(state)(x))
      state += 1
      actions
    }
  }
}
