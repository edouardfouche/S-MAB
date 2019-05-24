kit.edu.DependencyEstimation
----------------------------------

This was developed by Hendrik Braun for the purpose of his Bachelor Thesis at the 
Karlsruhe Institute of Technology in 2017.

It provides an implementation for `Multivariate Spearman` (F. Schmid and R. Schmidt, 
“Multivariate Extensions of Spearman’s rho and Related Statistics,” Statistics & Probability Letters, 
vol. 77, no. 4, pp. 407–416, 2007), 
`Total Correlation` 
(W. McGill, “Multivariate Information Transmission,” Transactions of
the IRE Professional Group on Information Theory, vol. 4, no. 4, pp. 93–111, 1954.) 
and `Interaction Information` 
(W. McGill, “Multivariate Information Transmission,” Transactions of the IRE Professional Group 
on Information Theory, vol. 4, no. 4, pp. 93–111, 1954). 
See also (N. Timme, W. Alford, B. Flecker, and J. M. Beggs, “Synergy, Redundancy, and Multivariate 
Information Measures: An Experimentalist’s Perspective,” 
Journal of Computational Neuroscience, vol. 36, no. 2, pp. 119–140, Apr. 2014.)

The efficiency of `Total Correlation` and `Interaction Information` was improved by Edouard Fouché using the R*-tree 
implementation from ELKI (`https://github.com/elki-project/elki`)

The code is published under the AGPLv3 as a joint work of Hendrik Braun and Edouard Fouché. 