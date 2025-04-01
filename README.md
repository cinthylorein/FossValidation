# FossValidation

**FossValidation** is a Java package for the BEAST2 platform that provides tools for assessing the consistency of fossil calibration points in molecular clock studies. The package implements the statistical methods proposed by Near (2005) and allows for cross-validation analysis of multiple fossil calibrations in a phylogeny.

---

## CompareNodeAges

`CompareNodeAges` quantifies the consistency between two tree posterior distributions by comparing the calibrated node heights. This tool helps detect potentially misleading fossil calibrations by measuring how much node heights estimated under a single calibration deviate from those estimated using the full set of calibrations.

### Purpose

Given two sets of tree posteriors:
- A **reference tree set** inferred using all calibration points.
- A **target tree set** inferred using a single calibration point.

This tool computes the **average height difference** at internal calibration-dated nodes to assess agreement between the full and partial calibrations.

### Method

For each node \( t \) in the calibration set \( \mathcal{C} \), the method computes:

\[
\delta(\mathcal{T}, \mathcal{R}) = \sum_{t \in \mathcal{C}} \mu(H_{\mathcal{T}_t}) - \mu(H_{\mathcal{R}_t})
\]

Where:
- \( \mathcal{T} \): Posterior tree set using a single calibration.
- \( \mathcal{R} \): Reference tree set using all calibrations.
- \( \mu(H) \): Mean height across the posterior samples of node \( t \).

The result is a log file with per-sample deviation values that can be visualized in **Tracer** or analyzed in **R**.

### Inputs

- `tree1`: Maximum clade credibility tree file (NEXUS format).
- `trees`: One or more target tree files with single calibrations.
- `output`: Path to the output `.log` file (tab-separated).

### Example usage

To run `CompareNodeAges`, use the command line and specify:

- `-tree1`: The reference tree (e.g., inferred using all calibration points)
- `-trees`: One or more tree files (each inferred using a single calibration point)
- `-output`: Path to save the output table (tab-separated)

```bash
java -cp beast.jar:FossValidation.jar FossValidation.tools.CompareNodeAges \
  -tree1 /Users/cjim882/eclipse-workspace/beast2.7/FossValidation/examples/Maximum_credibility_trees/starbeast3_COMB_ANN_10FBD.tree \
  -trees \
    /Users/cjim882/eclipse-workspace/beast2.7/FossValidation/examples/Maximum_credibility_trees/starbeast3_COMB_ANN_1FBD.tree \
    /Users/cjim882/eclipse-workspace/beast2.7/FossValidation/examples/Maximum_credibility_trees/starbeast3_COMB_ANN_3FBD.tree \
    /Users/cjim882/eclipse-workspace/beast2.7/FossValidation/examples/Maximum_credibility_trees/starbeast3_COMB_ANN_4FBD.tree \
    /Users/cjim882/eclipse-workspace/beast2.7/FossValidation/examples/Maximum_credibility_trees/starbeast3_COMB_ANN_6FBD.tree \
    /Users/cjim882/eclipse-workspace/beast2.7/FossValidation/examples/Maximum_credibility_trees/starbeast3_COMB_ANN_7FBD.tree \
    /Users/cjim882/eclipse-workspace/beast2.7/FossValidation/examples/Maximum_credibility_trees/starbeast3_COMB_ANN_8FBD.tree \
    /Users/cjim882/eclipse-workspace/beast2.7/FossValidation/examples/Maximum_credibility_trees/starbeast3_COMB_ANN_9FBD.tree \
    /Users/cjim882/eclipse-workspace/beast2.7/FossValidation/examples/Maximum_credibility_trees/starbeast3_COMB_ANN_10FBD.tree \
  -output /Users/cjim882/Dropbox/Phd_backup/Thesis/Chapter_3_4/Results/Assessing_consistency_results/differences.tsv

# References

Near TJ (2005) Assessing Concordance of Fossil Calibration Points in Molecular Clock Studies: An Example Using Turtles. American Naturalist 165(2): 137-146. DOI: 10.1086/427065.
