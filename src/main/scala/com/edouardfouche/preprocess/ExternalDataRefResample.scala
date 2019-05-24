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
package com.edouardfouche.preprocess

/**
  * The ExternalDataRefResample constructs an a data set with nDim dimensions and n samples from any real world data set
  * If the data set has more than nDim, then we take the first nDim dimensions
  * Otherwise, we duplicate the dimensions until the required number of dimensions nDim
  * Similarly with the number of rows
  *
  * @param id name to give to this data set
  * @param path location in the system
  * @param header number of header lines (will be deleted)
  * @param separator character used to separate each value
  * @param category "category" to which this data set belongs (free text)
  * @param nDim target number of dimension
  * @param n number of instances
  */
case class ExternalDataRefResample(id: String, path: String, header: Int, separator: String, category: String, nDim: Int, n:Int) extends DataRef {

  /**
    * Open the data ref
    * @return A 2-D Array of Double containing the values from the csv. (row oriented)
    */
  def open(): Array[Array[Double]] = {
    try {
      val data = Preprocess.open(path, header, separator, excludeIndex = false, dropClass = true, sample1000 = false)

      val dataN = if(data.length == n) data
      else{
        if(data.length < n) {
          (0 until n).map(x => data(x %data.length)).toArray
        } else {
          data.take(n) // take the first dims
        }
      }


      if(dataN(0).length == nDim) dataN
      else{
        if(dataN(0).length < nDim) {
          dataN.map(x => (0 until nDim).map(y => x(y % x.length)).toArray) // duplicate
        } else {
          dataN.map(x => x.take(nDim)) // take the first dims
        }
      }
    } catch {
      case e: Exception => println(s"Exception caught open $path" + e); null
    }
  }
}