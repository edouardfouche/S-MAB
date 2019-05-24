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

import com.edouardfouche.monitoring.bandits.nonstationary._
import com.edouardfouche.monitoring.bandits.adversarial._
import com.edouardfouche.monitoring.bandits.oracles.{OracleRandom, OracleStatic}
import com.edouardfouche.monitoring.bandits.stationary._
import com.edouardfouche.monitoring.scalingstrategies._
import com.edouardfouche.preprocess._

/**
  * Created by fouchee on 12.07.17.
  * This experiment compares the behavior of various bandits in the face of a global change (see GlobalGenerator)
  */
object BanditNonStaticGlobal extends BanditSyntheticExperiment {
  val d = 100
  val lmin = 1
  val lmax = d

  val generator= GlobalGenerator(d)

  val scalingstrategies: Array[ScalingStrategy] = Array(
    KLBasedScaling(lmin, lmax, 0.6)
    //KLBasedScaling(lmin, lmax, 0.7),
    //KLBasedScaling(lmin, lmax, 0.8),
    //KLBasedScaling(lmin, lmax, 0.9)
  )

  val nRep = 100
  //val nRep = 1

  override val banditConstructors = Vector(
    //OracleDynamic,
    OracleStatic,
    OracleRandom,
    CUCB, CUCBm,
    MPKLUCB, MPKLUCBPLUS,
    Exp3M,
    MPTS, IMPTS, MPOTS,
    MPDTS(0.7)(_,_,_,_), MPDTS(0.8)(_,_,_,_), MPDTS(0.9)(_,_,_,_), MPDTS(0.99)(_,_,_,_),
    MPEGreedy(0.7)(_, _, _, _), MPEGreedy(0.8)(_, _, _, _), MPEGreedy(0.9)(_, _, _, _), MPEGreedy(0.99)(_, _, _, _),
    MPSWUCB(50)(_, _, _, _), MPSWUCB(100)(_, _, _, _), MPSWUCB(500)(_, _, _, _), MPSWUCB(1000)(_, _, _, _),

    //OracleStatic_ADWIN(0.1)(_,_,_,_), OracleDynamic_ADWIN(0.1)(_,_,_,_), OracleSequential_ADWIN(0.1)(_,_,_,_),

    CUCB_ADWIN(0.1)(_,_,_,_),
    MPKLUCB_ADWIN(0.1)(_,_,_,_),
    //OracleRandom_ADWIN(0.1)(_,_,_,_),
    Exp3M_ADWIN(0.1)(_,_,_,_),
    MPTS_ADWIN(0.1)(_,_,_,_),

    MPTS_ADWIN(0.5)(_,_,_,_),
    MPTS_ADWIN(0.3)(_,_,_,_),
    MPTS_ADWIN(1)(_,_,_,_),
    MPTS_ADWIN(0.01)(_,_,_,_),
    MPTS_ADWIN(0.001)(_,_,_,_)
  )

  /*
  override val banditConstructors = Vector(
    CUCB, //CUCBm,
    MPKLUCB,
    Exp3Mauto,
    MPTS,// IMPTS, MPOTS,
    MPDTS(0.8)(_,_,_,_), MPDTS(0.9)(_,_,_,_), MPDTS(0.99)(_,_,_,_),
    MPEGreedy(0.8)(_, _, _, _), MPEGreedy(0.9)(_, _, _, _), MPEGreedy(0.99)(_, _, _, _),
    MPSWUCB(100)(_, _, _, _), MPSWUCB(1000)(_, _, _, _), MPSWUCB(10000)(_, _, _, _),

    CUCB_ADWIN(0.1)(_,_,_,_),
    MPKLUCB_ADWIN(0.1)(_,_,_,_),
    Exp3Mauto_ADWIN(0.1)(_,_,_,_),
    MPTS_ADWIN(0.1)(_,_,_,_)
  )
  */
}
