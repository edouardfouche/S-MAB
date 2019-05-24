package com.edouardfouche.monitoring.bandits

/**
  * General trait for bandits based on Thompson Sampling
  */
trait BanditTS extends Bandit {
  var beta_params: Array[(Double, Double)] = (0 until narms).map(x => (1.0,1.0)).toArray // initialize beta parameters to 1

  override def reset: Unit = {
    super.reset
    beta_params = (0 until narms).map(x => (1.0,1.0)).toArray
  }
}
