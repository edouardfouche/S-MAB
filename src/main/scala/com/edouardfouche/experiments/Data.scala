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
package com.edouardfouche.experiments

import com.edouardfouche.preprocess._

/**
  * Created by fouchee on 26.07.17.
  * Here, we define the reference to the data we need.
  */
object Data {
  val home: String = System.getProperty("user.home")
  val currentdir: String = System.getProperty("user.dir")

  // Reference to the real-world data for the use case
  lazy val bioliq_1wx20 = ExternalDataRef("Bioliq_S-MAB_1wx20", currentdir + "/data/Bioliq_S-MAB_1wx20.csv", 1, ",", excludeIndex = true, "real")

  // Reference to the pre-computed data for the use case
  lazy val bioliq_1wx20_MI_1000_100 = ExternalDataRef("bioliq_1wx20_MI_1000_100", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", excludeIndex = false, "cache")

  lazy val bioliq_1wx10_1000 = ExternalDataRefResample("bioliq_1wx10_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 10, 1000)
  lazy val bioliq_1wx20_1000 = ExternalDataRefResample("bioliq_1wx20_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 20, 1000)
  lazy val bioliq_1wx50_1000 = ExternalDataRefResample("bioliq_1wx50_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 50, 1000)
  lazy val bioliq_1wx100_1000 = ExternalDataRefResample("bioliq_1wx100_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 100, 1000)
  lazy val bioliq_1wx190_1000 = ExternalDataRefResample("bioliq_1wx190_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 190, 1000)
  lazy val bioliq_1wx200_1000 = ExternalDataRefResample("bioliq_1wx200_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 200, 1000)
  lazy val bioliq_1wx300_1000 = ExternalDataRefResample("bioliq_1wx300_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 300, 1000)
  lazy val bioliq_1wx350_1000 = ExternalDataRefResample("bioliq_1wx350_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 350, 1000)
  lazy val bioliq_1wx500_1000 = ExternalDataRefResample("bioliq_1wx500_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 500, 1000)
  lazy val bioliq_1wx750_1000 = ExternalDataRefResample("bioliq_1wx750_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 750, 1000)
  lazy val bioliq_1wx1000_1000 = ExternalDataRefResample("bioliq_1wx1000_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 1000, 1000)
  lazy val bioliq_1wx1250_1000 = ExternalDataRefResample("bioliq_1wx1250_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 1250, 1000)
  lazy val bioliq_1wx1500_1000 = ExternalDataRefResample("bioliq_1wx1500_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 1500, 1000)
  lazy val bioliq_1wx1750_1000 = ExternalDataRefResample("bioliq_1wx1750_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 1750, 1000)
  lazy val bioliq_1wx2000_1000 = ExternalDataRefResample("bioliq_1wx2000_1000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 2000, 1000)

  lazy val bioliq_1wx10_10000 = ExternalDataRefResample("bioliq_1wx10_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 10, 10000)
  lazy val bioliq_1wx20_10000 = ExternalDataRefResample("bioliq_1wx20_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 20, 10000)
  lazy val bioliq_1wx50_10000 = ExternalDataRefResample("bioliq_1wx50_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 50, 10000)
  lazy val bioliq_1wx100_10000 = ExternalDataRefResample("bioliq_1wx100_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 100, 10000)
  lazy val bioliq_1wx190_10000 = ExternalDataRefResample("bioliq_1wx190_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 190, 10000)
  lazy val bioliq_1wx200_10000 = ExternalDataRefResample("bioliq_1wx200_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 200, 10000)
  lazy val bioliq_1wx300_10000 = ExternalDataRefResample("bioliq_1wx300_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 300, 10000)
  lazy val bioliq_1wx350_10000 = ExternalDataRefResample("bioliq_1wx350_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 350, 10000)
  lazy val bioliq_1wx500_10000 = ExternalDataRefResample("bioliq_1wx500_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 500, 10000)
  lazy val bioliq_1wx750_10000 = ExternalDataRefResample("bioliq_1wx750_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 750, 10000)
  lazy val bioliq_1wx1000_10000 = ExternalDataRefResample("bioliq_1wx1000_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 1000, 10000)
  lazy val bioliq_1wx1250_10000 = ExternalDataRefResample("bioliq_1wx1250_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 1250, 10000)
  lazy val bioliq_1wx1500_10000 = ExternalDataRefResample("bioliq_1wx1500_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 1500, 10000)
  lazy val bioliq_1wx1750_10000 = ExternalDataRefResample("bioliq_1wx1750_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 1750, 10000)
  lazy val bioliq_1wx2000_10000 = ExternalDataRefResample("bioliq_1wx2000_10000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 2000, 10000)



  lazy val bioliq_1wx100_100000 = ExternalDataRefResample("bioliq_1wx100_100000", currentdir + "/data/Bioliq_S-MAB_1wx20_MI_1000_100.csv", 0, ",", "cache", 100, 100000)
}
