package com.edouardfouche.monitoring.bandits.nonstationary

import com.edouardfouche.monitoring.bandits.{BanditAdwin, BanditKLUCB}
import com.edouardfouche.monitoring.rewards.Reward
import com.edouardfouche.monitoring.scalingstrategies.ScalingStrategy
import com.edouardfouche.streamsimulator.Simulator

/**
  * KL-UCB with multiple plays and combined to ADWIN
  * This is an extension of KL-UCB "Kullback-leibler upper confidence bounds for optimal sequential allocation" (Cappé2013)
  * as described in "Optimal Regret Analysis of Thompson Sampling in Stochastic Multi-armed Bandit Problem with Multiple Plays" (Komiyama2016)
  *
  * @param delta the parameter for ADWIN (upper bound for the false positive rate)
  * @param stream a stream simulator on which we let this bandit run
  * @param reward the reward function which derives the gains for each action
  * @param scalingstrategy the scaling strategy, which decides how many arms to pull for the next step
  * @param k the initial number of pull per round
  *
  * @note As proposed in Garivier2011, the constant c is set to 0. (better empirical results)
  * @note This is basically the same as C-KL-UCB in "Thompson Sampling for Combinatorial Semi-Bandits" (Wang2018)
  * @note The implementation is based on https://github.com/jkomiyama/multiplaybanditlib/blob/master/policy/policy_klucb.hpp
  */
case class MPKLUCB_ADWIN(delta: Double)(val stream: Simulator, val reward: Reward,
                                        val scalingstrategy: ScalingStrategy, var k: Int) extends BanditKLUCB with BanditAdwin {
  val name = s"MP-KLUCB-ADWIN-$delta"

  def next: (Array[(Int, Int)], Array[Double], Double) = {
    // Here I thought about replacing t by the sum of all the draws, it turned out that the results were slightly worse (tried on scenario1 and 2 from JK).
    val klindices:Array[(Int,Double)] = (0 until narms).map(x => if(t==0.0 | counts(x) == 0.0) (x,1.0) else (x,getKLUCBupper(x,t))).toArray

    val indexes = klindices.sortBy(-_._2).map(_._1).take(k)

    val arms = indexes.map(combinations(_))

    val newValues = stream.nextAndCompute(indexes)
    if (newValues.isEmpty) return (Array[(Int, Int)](), Array[Double](), 0)

    val updates = scala.collection.mutable.Map[Int, Double]()

    // Update the current Matrix, compute the gains and update the weights at the same time
    val gains = (indexes zip newValues).map(x => {
      val d = reward.getReward(x._2, currentMatrix(x._1))
      currentMatrix(x._1) = x._2 // replace
      counts(x._1) += 1.0
      sums(x._1) += d

      // Add into adwin and add the update into the map
      sharedAdwin.addElement(x._1, d)
      updates(x._1) = d
      d
    })
    history = history :+ updates
    t = t + 1

    k = scalingstrategy.scale(gains, indexes, sums, counts, t)

    // Here we, add up the size of the adwin (those are the number of pulls) and the number of unpulls, to get the
    // actual size of each pulled arm.
    val windows = (0 until narms).map(x => (x, sharedAdwin.getSingleSize(x) + (history.length-counts(x))))

    val smallest_window = windows.minBy(_._2) // this is the smallest window

    // Rolling back
    if(smallest_window._2.toInt < history.length) {
      for{
        x <- smallest_window._2.toInt until history.length
      } {
        val rollback = history.head
        history = history.tail
        for((key,value) <- rollback) {
          sums(key) = sums(key) - value // if(counts(key) == 1.0) 1.0 else weights(key) - (1.0/(counts(key)-1.0))*(value._1 - weights(key))
          counts(key) = counts(key) - 1 //- value._2
        }
      }
    }
    t = history.length + 1 // The time context is the same as the history, which is the same as the smallest window

    val gain = gains.sum
    (arms, gains, gain)
  }

}
