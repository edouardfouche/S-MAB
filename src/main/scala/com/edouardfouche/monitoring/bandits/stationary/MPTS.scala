package com.edouardfouche.monitoring.bandits.stationary

import breeze.stats.distributions.Beta
import com.edouardfouche.monitoring.bandits.BanditTS
import com.edouardfouche.monitoring.rewards.Reward
import com.edouardfouche.monitoring.scalingstrategies.ScalingStrategy
import com.edouardfouche.streamsimulator.Simulator

/**
  * Multiple Play Thompson Sampling
  * The idea of MP-TS comes from "Optimal Regret Analysis of Thompson Sampling in Stochastic Multi-armed BanditK Problem with Multiple Plays" (Komiyama 2016)
  * In "Scaling Multi-Armed Bandit Algorithms" (Fouché 2019), this is referred to as S-TS
  *
  * @param stream a stream simulator on which we let this bandit run
  * @param reward the reward function which derives the gains for each action
  * @param scalingstrategy the scaling strategy, which decides how many arms to pull for the next step
  * @param k the initial number of pull per round
  */
case class MPTS(stream: Simulator, reward: Reward, scalingstrategy: ScalingStrategy, var k: Int) extends BanditTS {
  val name = "MP-TS"

  def next: (Array[(Int, Int)], Array[Double], Double) = {
    val draws = beta_params.zipWithIndex.map(x => (x._2, new Beta(x._1._1,x._1._2).draw())).sortBy(- _._2).take(k)
    val indexes = draws.map(_._1)
    val arms = indexes.map(combinations(_))

    val newValues = stream.nextAndCompute(indexes)
    if (newValues.isEmpty) return (Array[(Int, Int)](), Array[Double](), 0)

    val gains = (indexes zip newValues).map(x => {
      val d = reward.getReward(x._2, currentMatrix(x._1))
      beta_params(x._1) = (beta_params(x._1)._1+d, beta_params(x._1)._2+(1.0-d))
      currentMatrix(x._1) = x._2 // replace
      counts(x._1) += 1.0
      sums(x._1) += d // (http://www.cs.cmu.edu/~rsalakhu/10703/Lecture_Exploration.pdf slide 12)
      d
    })

    t += 1
    k = scalingstrategy.scale(gains, indexes, sums, counts, t)

    (arms, gains, gains.sum)
  }

}
