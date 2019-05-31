# Scaling Multi-Armed Bandits (S-MAB)

[![Build Status](https://travis-ci.com/edouardfouche/S-MAB.svg?branch=master)](https://travis-ci.com/edouardfouche/S-MAB)
[![License AGPL-3.0](https://img.shields.io/badge/License-AGPL--3-brightgreen.svg)](https://github.com/edouardfouche/MCDE-experiments/blob/master/LICENSE.md)

Welcome to the supplementary material for the paper:


- Edouard Fouché, Junpei Komiyama, and Klemens Böhm. 2019. Scaling Multi-Armed Bandit Algorithms. In The 25th ACM SIGKDD Conference on Knowledge Discovery and Data Mining (KDD ’19), August 4–8, 2019, Anchorage, AK, USA. ACM, New York, NY, USA, 11 pages. https://doi.org/10.1145/3292500.3330862

This repository contains the reference implementation of the "Scaling Bandits" (S-TS and S-TS-ADWIN) and all the
information required to reproduce the experiments in the paper. For this reason, it is partially frozen at the time of
publication.

This repository is released under the AGPLv3 license. Please see the [LICENSE.md](LICENSE.md) file.
The data from the [Bioliq®](https://www.bioliq.de/english/) pyrolisis plant,
which we use in the paper, is licensed under CC-BY-NC-SA-4.0.
You can download it from [here](https://www.dropbox.com/s/gyrb62ebtcmvy9h/Bioliq_S-MAB.zip).

If you are using the code or data from this repository, please cite our paper.

## Quick start

### Build it and run it

**Requirements** : ([Oracle JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or [OpenJDK 8](http://openjdk.java.net/install/)) and [sbt](https://www.scala-sbt.org/1.0/docs/Setup.html)

The project is built with sbt (version 1.2.8). You can compile, package or run the project as follows:

```
sbt compile
sbt package
sbt "run <arguments>"
```

You can also export a "fat" jar, including all dependencies and scala libraries using [`sbt-assembly`](https://github.com/sbt/sbt-assembly):

```
sbt assembly
```

This creates a jar in the folder `target/scala-2.12/` named `S-MAB-<version>.jar`, which can be run from java (no
sbt/scala installation required). The version of the package at the time of the experiments is 1.0.

Once you have built the jar, you can run it as follows:

```
java -jar target/scala-2.12/S-MAB-1.0.jar <arguments>
```

## Reproducing the experiments

In this section, we explain how to reproduce the experiments from our paper. The experiments create about 2.1GB of data
and require about 80 hours on a server with 32 cores at 3.0 GHz and 64GB RAM, using Java Open-JDK 8 and Scala 2.12.8. Results are saved in
the folder experiments as .csv files, along with logs.

Note that depending on your setting, you might want to increase the
memory available for the JVM (using the Xmx option, for example), or you might run
into some `java.lang.OutOfMemoryError: GC overhead limit exceeded` exception.

### Static

Evaluate Scaling Bandits in the static setting (Figure 1).
```
sbt "run com.edouardfouche.experiments.BanditStatic"  # ~ 6 hours, 131MB data
```

### Gradual

Evaluate Scaling Bandits in the non-static gradual setting (Figure 2 and 3, left part).
```
sbt "run com.edouardfouche.experiments.BanditNonStaticGradual"  # ~ 9 hours, 220MB data
```

### Abrupt

Evaluate Scaling Bandits in the non-static abrupt setting (Figure 2 and 3, right part).
```
sbt "run com.edouardfouche.experiments.BanditNonStaticAbrupt"  # ~ 16 hours, 220MB data
```

### Real-World

#### Pre-computation

We significantly speed up the experiments by pre-computing the pair-wise mutual information between every attributes
of the real-world data. We provide the results of this pre-computation in this repository as `data/Bioliq_S-MAB_1wx20_MI_1000_100.csv` (so you can
skip this first step).

You can access to the original data from the [Bioliq®](https://www.bioliq.de/english/) pyrolisis plant at the
following [link](https://www.dropbox.com/s/gyrb62ebtcmvy9h/Bioliq_S-MAB.zip?dl=0), licensed under CC-BY-NC-SA 4.0.
After downloading the archive, you may simply add the `Bioliq_S-MAB_1wx20.csv` file into the `data/` folder.
Note that we are currently working on releasing additional extended
versions of this data set with documentation on broader channels.

To perform the pre-computation (the file `data/Bioliq_S-MAB_1wx20.csv` is required), you may run:

```
sbt "run com.edouardfouche.experiments.BanditCache"  # ~ 5 min, 22MB data
```

#### Real-world experiments

Then, the following experiment evaluates Scaling Bandits in our real-world use case (Figure 5 and 6 top).

```
sbt "run com.edouardfouche.experiments.BanditRealWorld"  # ~ 4 hours, 100MB data
```

And this one evaluates Scaling Bandits in our real-world use case with different reward criterions (Figure 6 bottom).

```
sbt "run com.edouardfouche.experiments.BanditRealWorldRewards"  # ~ 2 hours, 320MB data
```

### Scalability

Evaluate the scalability of Scaling Bandits with increasing number of time steps T and number of arms K (Figure 7).

```
sbt "run com.edouardfouche.experiments.BanditScalabilityK"  # ~ 2 hours, 120MB data
```
```
sbt "run com.edouardfouche.experiments.BanditScalabilityT"  # ~ 2 days, 986MB data
```

## Visualize the results

Then, you can use the Jupyter notebooks in folder `visualize` to reproduce
the plots from the publication. By the time of the experiments, we use the following Python packages:

```
# Name                    Version
matplotlib                2.0.2
numpy                     1.13.1
pandas                    0.20.3
seaborn                   0.8
```

Each experiments have dedicated notebooks, in the folder `visualize/`:

`BanditStatic` -> `BanditStatic.ipynb`

`BanditNonStaticGradual` -> `BanditNonStaticGradual.ipynb`, `BanditNonStatic.ipynb`

`BanditNonStaticAbrupt` -> `BanditNonStaticAbrupt.ipynb`, `BanditNonStatic.ipynb`

`BanditCache` -> `RewardMatrix.ipynb`

`BanditRealWorld` -> `BanditRealWorld.ipynb`

`BanditRealWorldRewards` -> `BanditRealWorldRewards.ipynb`

`BanditScalabilityK`, `BanditScalabilityT` -> `BanditScalability.ipynb`


## Additional experiments

This repository also features a set of experiments which are not part of the paper.

Reproduce the experiments from [*Junpei Komiyama, Junya Honda, and Hiroshi Nakagawa. Optimal Regret Analysis of
Thompson Sampling in Stochastic Multi-armed Bandit Problem with Multiple Plays. In ICML'15*](http://proceedings.mlr.press/v37/komiyama15.html)

```
sbt "run com.edouardfouche.experiments.BanditKomiyama"  # ~ 1.25 hours, 63MB data
```

Evaluate against another non-static change (global change). See `com.edouardfouche.preprocess.GlobalGenerator`.
```
sbt "run com.edouardfouche.experiments.BanditNonStaticGlobal"  # ~ 10.5 hours, 218MB data
```

Evaluate several variants of the static generators.
```
sbt "run com.edouardfouche.experiments.BanditStaticGenerator"  # ~ 12 hours, 438MB data
```

Evaluate the static generator with various number of arms.
```
sbt "run com.edouardfouche.experiments.BanditStaticK"  # ~ 25 hours, 414MB data
```

Evaluate the impact of optimistic initialization in the static setting.
```
sbt "run com.edouardfouche.experiments.BanditStaticOptimistic"  # ~ 10.5 hours, 285MB data
```

Evaluate various scaling policies.
```
sbt "run com.edouardfouche.experiments.BanditScalingStrategies"  # ~ 12 hours, 354MB data
```

## Contributing

We welcome contributions to the repository and bug reports on GitHub.

For questions and comments, please contact `edouard.fouche@kit.edu`, or open an issue.

## Acknowledgements

- The [ELKI project](https://elki-project.github.io/) for the R*-Tree index structure, that we use to accelerate the
nearest neighbors queries for computing Mutual Information.

- Hendrik Braun, for the implementation of the Mutual Information estimator, as a part of his Bachelor's thesis at KIT.

- We thank the pyrolysis team of the [Bioliq®](https://www.bioliq.de/english/) process for providing the data for our real-world use case

- This work was supported by the DFG Research Training Group 2153: “Energy Status Data – Informatics Methods for its
Collection, Analysis and Exploitation” and the German Federal Ministry of Education and Research (BMBF) via Software
Campus (01IS17042).