package FossValidation.tools;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Java tool to generate BEAST2 XMLs for IncludeOne and CrossValidation experiments
 * Usage: java FossValidation.tools.GenerateCalibrationXMLs mode input.xml outputDir
 * Where mode is either "includeone" or "crossvalidation"
 */
public class GenerateCalibrationXMLs {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java FossValidation.tools.GenerateCalibrationXMLs <includeone|crossvalidation> input.xml outputDir");
            return;
        }

        String mode = args[0];
        String inputPath = args[1];
        String outputDir = args[2];

        List<String> lines = Files.readAllLines(Paths.get(inputPath));
        List<String> cladeIDs = new ArrayList<>();

        // Find calibration block IDs
        Pattern distPattern = Pattern.compile("<distribution id=\"Calib_(.*?)\"");
        for (String line : lines) {
            Matcher matcher = distPattern.matcher(line);
            if (matcher.find()) {
                cladeIDs.add(matcher.group(1));
            }
        }

        if (mode.equalsIgnoreCase("includeone")) {
            for (String clade : cladeIDs) {
                List<String> modified = commentOutAllBut(lines, cladeIDs, clade);
                String filename = outputDir + "/startbeast3_INCLUDEONE_" + clade + ".xml";
                Files.write(Paths.get(filename), modified);
            }
        } else if (mode.equalsIgnoreCase("crossvalidation")) {
            for (int k = cladeIDs.size(); k >= 2; k--) {
                combinations(cladeIDs, k, combo -> {
                    try {
                        List<String> modified = commentOutAllBut(lines, cladeIDs, combo);
                        String name = String.join("_", combo);
                        String filename = outputDir + "/startbeast3_CV_" + name + ".xml";
                        Files.write(Paths.get(filename), modified);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            System.err.println("Unknown mode: " + mode);
        }
    }

    static List<String> commentOutAllBut(List<String> lines, List<String> allClades, String keepClade) {
        return commentOutAllBut(lines, allClades, Collections.singletonList(keepClade));
    }

    static List<String> commentOutAllBut(List<String> lines, List<String> allClades, List<String> keepClades) {
        List<String> result = new ArrayList<>(lines);
        for (String clade : allClades) {
            if (!keepClades.contains(clade)) {
                result = commentOutDistribution(result, clade);
            }
        }
        return result;
    }

    static List<String> commentOutDistribution(List<String> lines, String clade) {
        List<String> modified = new ArrayList<>();
        boolean inBlock = false;
        for (String line : lines) {
            if (line.contains("<distribution id=\"Calib_" + clade)) {
                inBlock = true;
                modified.add("<!-- " + line);
            } else if (inBlock && line.contains("</distribution>")) {
                inBlock = false;
                modified.add(line + " -->");
            } else {
                modified.add(line);
            }
        }
        return modified;
    }

    interface CombinationConsumer {
        void accept(List<String> combo);
    }

    static void combinations(List<String> items, int k, CombinationConsumer consumer) {
        combinationsHelper(items, k, 0, new ArrayList<>(), consumer);
    }

    static void combinationsHelper(List<String> items, int k, int start, List<String> current, CombinationConsumer consumer) {
        if (current.size() == k) {
            consumer.accept(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < items.size(); i++) {
            current.add(items.get(i));
            combinationsHelper(items, k, i + 1, current, consumer);
            current.remove(current.size() - 1);
        }
    }
}
