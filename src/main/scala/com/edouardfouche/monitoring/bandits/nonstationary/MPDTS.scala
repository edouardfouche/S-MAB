package com.edouardfouche.monitoring.bandits.nonstationary

import breeze.stats.distributions.Beta
import com.edouardfouche.monitoring.bandits.BanditTS
import com.edouardfouche.monitoring.rewards.Reward
import com.edouardfouche.monitoring.scalingstrategies.ScalingStrategy
import com.edouardfouche.streamsimulator.Simulator

/**
  * Discounted Thompson Sampling with Multiple Plays
  * The idea of "Discounted" TS comes from "Taming Non-stationary Bandits: A Bayesian Approach" (Raj2017)
  *
  * @param gamma the discounting factor, applied to the beta parameters at each stage
  * @param stream a stream simulator on which we let this bandit run
  * @param reward the reward function which derives the gains for each action
  * @param scalingstrategy the scaling strategy, which decides how many arms to pull for the next step
  * @param k the initial number of pull per round
  */
case class MPDTS(gamma: Double)(val stream: Simulator, val reward: Reward, val scalingstrategy: ScalingStrategy, var k: Int) extends BanditTS {
  require((0 <= gamma) & (gamma <= 1.0))
  val name = s"MP-DTS; g=$gamma"

  def next: (Array[(Int, Int)], Array[Double], Double) = {
    t = t * gamma
    counts.map(x => x * gamma)
    sums.map(x => x * gamma)

    val sorteddraws = beta_params.zipWithIndex.map(x => (x._2, new Beta(x._1._1,x._1._2).draw())).sortBy(- _._2)
    val indexes = sorteddraws.take(k).map(_._1)
    val notindexes = sorteddraws.drop(k).map(_._1)

    val arms = indexes.map(combinations(_))

    val newValues = stream.nextAndCompute(indexes)
    if (newValues.isEmpty) return (Array[(Int, Int)](), Array[Double](), 0)

    val gains = (indexes zip newValues).map(x => {
      val d = reward.getReward(x._2, currentMatrix(x._1))
      beta_params(x._1) = ((beta_params(x._1)._1*gamma+d).max(0.001), (beta_params(x._1)._2*gamma+(1.0-d)).max(0.001))
      currentMatrix(x._1) = x._2 // replace
      counts(x._1) += 1.0
      sums(x._1) += d
      d
    })

    t += 1.0
    k = scalingstrategy.scale(gains, indexes, sums, counts, t)

    // We say the parameters cannot be lower than 0.001 because they need to be positive.
    // In some unfortunate circumstances, it might happen that rounding leads to a parameter becomes equal to 0
    // which create some error
    notindexes.foreach{x =>
      beta_params(x) = ((beta_params(x)._1*gamma).max(0.001), (beta_params(x)._2*gamma).max(0.001))
    }
    (arms, gains, gains.sum)
  }

}
