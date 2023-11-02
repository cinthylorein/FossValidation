package FossValidation.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscordanceAgeCalibrationsLogger {
	

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java DiscordanceAgeCalibrationsLogger <inputLogFile1> <inputLogFile2> <outputLogFile>");
            System.exit(1);
        }

        String inputLogFile1 = args[0];
        String inputLogFile2 = args[1];
        String outputLogFile = args[2];

        try {
            List<List<Double>> data1 = loadLogData(inputLogFile1);
            List<List<Double>> data2 = loadLogData(inputLogFile2);

            List<Double> mean1 = calculateMean(data1);
            List<Double> mean2 = calculateMean(data2);

            List<Double> percentiles1 = calculatePercentiles(data1, 2.5, 97.5);
            List<Double> percentiles2 = calculatePercentiles(data2, 2.5, 97.5);

            List<Double> differences = calculateCladeHeightDifferences(mean1, mean2);

            saveTraceLog(outputLogFile, mean1, percentiles1, mean2, percentiles2, differences);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static List<List<Double>> loadLogData(String inputLogFile) throws IOException {
        List<List<Double>> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputLogFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.trim().split("\t");
                List<Double> values = new ArrayList<>();

                for (String column : columns) {
                    try {
                        double value = Double.parseDouble(column);
                        values.add(value);
                    } catch (NumberFormatException e) {
                        System.err.println("Error: Failed to parse value in the input log file.");
                        return data;
                    }
                }

                data.add(values);
            }
        }

        return data;
    }

    private static List<Double> calculateMean(List<List<Double>> data) {
        List<Double> means = new ArrayList<>();
        int numColumns = data.get(0).size();

        for (int i = 0; i < numColumns; i++) {
            double sum = 0.0;
            for (List<Double> row : data) {
                sum += row.get(i);
            }
            double mean = sum / data.size();
            means.add(mean);
        }

        return means;
    }

    private static List<Double> calculatePercentiles(List<List<Double>> data, double lowerPercentile, double upperPercentile) {
        List<Double> percentiles = new ArrayList<>();
        int numColumns = data.get(0).size();

        for (int i = 0; i < numColumns; i++) {
            double[] values = new double[data.size()];
            for (int j = 0; j < data.size(); j++) {
                values[j] = data.get(j).get(i);
            }
            Arrays.sort(values);

            int lowerIndex = (int) (data.size() * (lowerPercentile / 100));
            int upperIndex = (int) (data.size() * (upperPercentile / 100));
            double lowerPercentileValue = values[lowerIndex];
            double upperPercentileValue = values[upperIndex];

            percentiles.add(lowerPercentileValue);
            percentiles.add(upperPercentileValue);
        }

        return percentiles;
    }

    private static List<Double> calculateCladeHeightDifferences(List<Double> mean1, List<Double> mean2) {
        List<Double> differences = new ArrayList<>();

        for (int i = 0; i < mean1.size(); i++) {
            double difference = mean1.get(i) - mean2.get(i);
            differences.add(difference);
        }

        return differences;
    }

    private static void saveTraceLog(String outputLogFile, List<Double> mean1, List<Double> percentiles1, List<Double> mean2, List<Double> percentiles2, List<Double> differences) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputLogFile))) {
            int numColumns = mean1.size();

            // Header
            writer.print("Statistic\t");
            for (int i = 1; i <= numColumns; i++) {
                writer.print("Column_" + i + "_Mean\t");
                writer.print("Column_" + i + "_2.5%ile\t");
                writer.print("Column_" + i + "_97.5%ile\t");
            }
            writer.println("MeanDifferences");

            // Data
            writer.print("Mean\t");
            for (int i = 0; i < numColumns; i++) {
                writer.printf("%.4f\t%.4f\t%.4f\t", mean1.get(i), percentiles1.get(i * 2), percentiles1.get(i * 2 + 1));
            }
            writer.println();

            writer.print("Mean\t");
            for (int i = 0; i < numColumns; i++) {
                writer.printf("%.4f\t%.4f\t%.4f\t", mean2.get(i), percentiles2.get(i * 2), percentiles2.get(i * 2 + 1));
            }
            writer.println();

            writer.print("Differences\t");
            for (double difference : differences) {
                writer.printf("%.4f\t", difference);
            }
            writer.println();
        }
    }
}

