package FossValidation.tools;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Log;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.inference.Runnable;
import beast.base.parser.NexusParser;
import beastfx.app.tools.Application;
import beastfx.app.util.OutFile;
import beastfx.app.util.TreeFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Description("Evaluates discordance between posterior and prior node heights for fossil-calibrated clades.")
public class DiscordantAgeCalibrationsLogger extends Runnable {

    // Input for list of posterior tree files
    public Input<List<TreeFile>> posteriors = new Input<>("posteriors", "List of posterior trees (one per calibration set)");
    // Input CSV with clade prior mean, q025, q975
    public Input<File> priors = new Input<>("priors", "CSV file with clade prior mean, q025, q975");
    // Input CSV mapping clade names to taxa
    public Input<File> clades = new Input<>("clades", "CSV file mapping clades to taxa");
    // Output TSV file to write discordance results
    public Input<OutFile> output = new Input<>("output", "Output TSV file");

    // Register the element type for the list input
    {
        posteriors.setType(TreeFile.class);
    }

    // Helper class to store prior values
    class PriorAge {
        double mean, q025, q975;
        PriorAge(double m, double q1, double q2) { mean = m; q025 = q1; q975 = q2; }
    }

    @Override
    public void initAndValidate() {}

    @Override
    public void run() throws Exception {
        // Load prior calibration values per clade from CSV
        Map<String, PriorAge> priorMap = loadPriorFile(priors.get());
        // Load clade to taxa mappings from CSV
        Map<String, List<String>> cladeMap = loadCladeFile(clades.get());

        // Prepare writer for output TSV
        PrintWriter writer = new PrintWriter(new FileWriter(output.get()));
        writer.println("clade\tfossilSet\tD_mean\tD_0.025\tD_0.975");

        // Ensure the tree files input is a list
        List<TreeFile> treeFiles = new ArrayList<>();
        Object value = posteriors.get();
        if (value instanceof List) {
            treeFiles = (List<TreeFile>) value;
        } else if (value instanceof TreeFile) {
            treeFiles.add((TreeFile) value);
        } else {
            throw new IllegalArgumentException("Invalid type for posteriors input.");
        }

        // Validate input
        if (treeFiles == null || treeFiles.isEmpty()) {
            throw new IllegalArgumentException("No posterior trees provided.");
        }

        int fossilSet = 1; // Counter for fossil set index

        // Process each tree file
        for (TreeFile tf : treeFiles) {
            NexusParser parser = new NexusParser();
            parser.parseFile(tf);
            List<Tree> trees = parser.trees;

            // For each clade, extract MRCA heights and compare to prior
            for (String clade : cladeMap.keySet()) {
                List<Double> heights = extractMRCAHeights(trees, cladeMap.get(clade), clade);

                if (heights == null || heights.isEmpty()) {
                    Log.warning("No MRCA height data for clade: " + clade);
                    continue;
                }

                PriorAge prior = priorMap.get(clade);
                if (prior == null) {
                    Log.warning("No prior found for clade: " + clade);
                    continue;
                }

                // Calculate difference between posterior and prior values
                double dMean = mean(heights) - prior.mean;
                double d025 = quantile(heights, 0.025) - prior.q025;
                double d975 = quantile(heights, 0.975) - prior.q975;

                // Write results to output file
                writer.printf("%s\t%d\t%.4f\t%.4f\t%.4f\n", clade, fossilSet, dMean, d025, d975);
            }
            fossilSet++;
        }

        writer.close();
        Log.warning("Discordance analysis complete.");
    }

    // Load prior calibration values from CSV
    private Map<String, PriorAge> loadPriorFile(File file) throws IOException {
        Map<String, PriorAge> map = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine(); // skip header
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            map.put(parts[0], new PriorAge(Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
        }
        br.close();
        return map;
    }

    // Load clade-to-taxa mappings from CSV
    private Map<String, List<String>> loadCladeFile(File file) throws IOException {
        Map<String, List<String>> map = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine(); // skip header
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            map.computeIfAbsent(parts[0], k -> new ArrayList<>()).add(parts[1]);
        }
        br.close();
        return map;
    }

    // Extract MRCA node heights for a given clade across all trees, with fallback logging
    private List<Double> extractMRCAHeights(List<Tree> trees, List<String> taxa, String cladeName) {
        List<Double> heights = new ArrayList<>();
        for (Tree tree : trees) {
            // Collect all tip labels in the tree
            Set<String> tipLabels = new HashSet<>();
            for (Node node : tree.getNodesAsArray()) {
                if (node.isLeaf()) {
                    tipLabels.add(node.getID());
                }
            }

            // Filter to only taxa present in the tree
            List<String> presentTaxa = taxa.stream()
                .filter(tipLabels::contains)
                .collect(Collectors.toList());

            if (presentTaxa.size() < 2) {
                Log.warning("Too few matching taxa in tree for clade '" + cladeName + "': " + presentTaxa);
                continue;
            }

            // Attempt to get MRCA
            Node mrca = tree.getMRCA(presentTaxa);
            if (mrca != null) {
                heights.add(mrca.getHeight());
            } else {
                // Fallback message if MRCA computation fails despite having valid taxa
                Log.warning("MRCA not found for clade '" + cladeName + "' with present taxa: " + presentTaxa);
            }
        }
        return heights;
    }

    // Calculate mean of values
    private double mean(List<Double> values) {
        return values.stream().mapToDouble(v -> v).average().orElse(Double.NaN);
    }

    // Calculate specified quantile (e.g., 2.5%, 97.5%)
    private double quantile(List<Double> values, double q) {
        List<Double> sorted = values.stream().sorted().collect(Collectors.toList());
        int index = (int)Math.floor(q * (sorted.size() - 1));
        return sorted.get(index);
    }

    // Main entry point for running from the command line
    public static void main(String[] args) throws Exception {
        new Application(new DiscordantAgeCalibrationsLogger(), "DiscordantAgeCalibrationsLogger", args);
    }
}
