package com.edouardfouche.monitoring.bandits.stationary

import breeze.stats.distributions.Beta
import com.edouardfouche.monitoring.bandits.BanditTS
import com.edouardfouche.monitoring.rewards.Reward
import com.edouardfouche.monitoring.scalingstrategies.ScalingStrategy
import com.edouardfouche.streamsimulator.Simulator

/**
  * Combinatorial Thompson Sampling (Multiple Plays) as described in "Thompson Sampling for Combinatorial Semi-Bandits" (Wang 2018)
  * @param stream a stream simulator on which we let this bandit run
  * @param reward the reward function which derives the gains for each action
  * @param scalingstrategy the scaling strategy, which decides how many arms to pull for the next step
  * @param k the initial number of pull per round
  *
  * @note Somehow, this is very similar to MP-TS
  */
case class CTS(stream: Simulator, reward: Reward, scalingstrategy: ScalingStrategy, var k: Int) extends BanditTS {
  val name = "CTS"

  def next: (Array[(Int, Int)], Array[Double], Double) = {
    val draws = beta_params.zipWithIndex.map(x => (x._2, new Beta(x._1._1,x._1._2).draw())).sortBy(- _._2).take(k)
    val indexes = draws.map(_._1)
    val arms = indexes.map(combinations(_))

    val newValues = stream.nextAndCompute(indexes)
    if (newValues.isEmpty) return (Array[(Int, Int)](), Array[Double](), 0)

    val gains = (indexes zip newValues).map(x => {
      val d = reward.getReward(x._2, currentMatrix(x._1))
      val y = if(math.random < d) 1.0 else 0.0
      beta_params(x._1) = (beta_params(x._1)._1+y, beta_params(x._1)._2+(1.0-y))
      currentMatrix(x._1) = x._2 // replace
      counts(x._1) += 1.0
      sums(x._1)  += d
      d
    })

    t += 1
    k = scalingstrategy.scale(gains, indexes, sums, counts, t)

    (arms, gains, gains.sum)
  }
}
