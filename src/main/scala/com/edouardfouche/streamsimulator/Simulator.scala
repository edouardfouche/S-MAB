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

// A Simulator "simulates" a stream, given as a DataRef.
// It computes -- or simulate -- an action an action computed on a this stream on a the basis of a sliding window
/**
  * A Simulator "simulates" a stream, given as a DataRef.
  * It computes -- or simulate -- an action an action computed on a this stream on a the basis of a sliding window
  */
trait Simulator {
  val dataset: DataRef
  val action_name: String
  val windowSize: Int
  val stepSize: Int

  val ncols: Int
  val nbatches: Int
  val pairs: Array[(Int, Int)]
  val npairs: Int

  val data: Array[Array[Double]] // We assume that the data is already in the desired order

  val id: String

  var state = 0

  // The cache holds the result of the computation of the whole stream
  val cache: Array[Array[Double]]

  /**
    * Get the data for the next sliding window
    * @return the content of the sliding window at the next step
    */
  def next: Array[Array[Double]]

  /**
    * Get the computed action on each pair of stream for the next sliding window
    * @param indexes pairs of the stream on which to compute the action
    * @return the outcome of the action for each pair
    */
  def nextAndCompute(indexes: Array[Int]): Array[Double]

  def reset(): Unit = state = 0

}
