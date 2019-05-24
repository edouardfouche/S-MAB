package com.edouardfouche.monitoring.bandits

import com.edouardfouche.monitoring.resetstrategies.SharedAdwin

/**
  * General trait for bandits that use adwin
  */
trait BanditAdwin extends Bandit {
  val delta: Double // Parameter for adwin

  // Initialize a shared bandit instance and the history of updates
  var sharedAdwin = new SharedAdwin(stream.npairs, delta)
  var history: List[scala.collection.mutable.Map[Int,Double]] = List() // first el in the update for count, and last in the update for weight

  override def reset: Unit = {
    super.reset
    sharedAdwin = new SharedAdwin(stream.npairs, delta)
    history = List()
  }
}
