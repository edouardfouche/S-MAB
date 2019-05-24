package com.edouardfouche.monitoring.bandits

/**
  * General trait for bandits based on UCB
  */
trait BanditUCB extends Bandit {
  val logfactor: Double = 3.0/2.0 // Note that logfactor = 1.0/2.0 would correspond to CUCB-m
}
