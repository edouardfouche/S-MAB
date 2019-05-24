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

import com.edouardfouche.monitoring.actions.Action
import com.edouardfouche.preprocess.DataRef
/*
The purpose of this class is to simulate a stream from a static data set, based on a sliding window
 */
/**
  * StreamSimulator simulates a stream from a static data set, based on a sliding window
  * @param dataset a reference to a data set
  * @param action an action to compute at each round
  * @param windowSize size of the sliding window in which to compute the action
  * @param stepSize size of the step between each window
  */
case class StreamSimulator(dataset: DataRef, action: Action, windowSize: Int, stepSize: Int) extends Simulator {
  val data: Array[Array[Double]] = dataset.open() // it is supposed to be row-oriented

  val ncols: Int = data(0).length
  val nbatches: Int = math.floor((data.length - windowSize) / stepSize).toInt + 1
  val pairs: Array[(Int, Int)] = (0 until ncols).flatMap(x => (0 until x).map(y => (x, y))).toArray
  val npairs: Int = pairs.size
  val action_name: String = action.name

  val id: String = dataset.id + "_" + action_name + "_" + windowSize + "_" + stepSize

  // The cache holds the result of the computation of the whole stream
  lazy val cache: Array[Array[Double]] = {
    (0 to data.length - windowSize by stepSize).par.map { x =>
      val window = data.slice(x, x + windowSize).transpose
      pairs.indices.map(y => action.compute(window(pairs(y)._1), window(pairs(y)._2))).toArray
    }.seq.toArray
  }

  // Return the data for the next sliding window
  def next: Array[Array[Double]] = {
    if(state*stepSize + windowSize > data.length) Array[Array[Double]]()
    else {
      val window = data.slice(state*stepSize, state*stepSize + windowSize)
      state += 1
      window
    }
  }

  // Return the computed action on each pair of stream for the next sliding window
  def nextAndCompute(indexes: Array[Int]): Array[Double] = {
    if (state*stepSize + windowSize > data.length) Array[Double]()
    else {
      val window = data.slice(state*stepSize, state*stepSize + windowSize).transpose
      state += 1
      def compute(index: Int) = action.compute(window(pairs(index)._1), window(pairs(index)._2))
      indexes.map(x => compute(x))
    }
  }
}
