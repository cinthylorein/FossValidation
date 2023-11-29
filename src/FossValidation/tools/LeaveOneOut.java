package FossValidation.tools;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



	public class LeaveOneOut extends Runnable {
	    final public Input<List<String>> logFilesInput = new Input<>("logFiles", "List of log files", new ArrayList<>());
	    final public Input<Table> ctTableInput = new Input<>("ctTable", "Table with CT labels, ytmean, ytmin, and ytmax columns");

	    // Additional inputs
	    // ...

	    @Override
	    public void run() {
	        List<String> logFiles = logFilesInput.get();
	        Table ctTable = ctTableInput.get();

	        List<FossilResult> fossilResults = new ArrayList<>();

	        for (String logFile : logFiles) {
	            try {
	                List<FossilData> fossilDataList = readFossilData(logFile, ctTable);

	                for (FossilData fossilData : fossilDataList) {
	                    FossilResult fossilResult = calculateIOs(fossilData);
	                    fossilResults.add(fossilResult);
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }

	        // Further processing or output
	        // ...
	    }

	    private List<FossilData> readFossilData(String logFile, Table ctTable) throws IOException {
	        List<FossilData> fossilDataList = new ArrayList<>();

	        try (CSVParser parser = CSVParser.parse(new FileReader(logFile), CSVFormat.TDF.withHeader())) {
	            for (CSVRecord record : parser) {
	                String calibrationPoint = record.get("CalibrationPoint");
	                double ytmean = Double.parseDouble(record.get("ytmean"));
	                double ytmin = Double.parseDouble(record.get("ytmin"));
	                double ytmax = Double.parseDouble(record.get("ytmax"));

	                FossilData fossilData = new FossilData(calibrationPoint, ytmean, ytmin, ytmax);
	                fossilDataList.add(fossilData);
	            }
	        }

	        return fossilDataList;
	    }

	    private FossilResult calculateIOs(FossilData fossilData) {
	        // Implementation similar to previous example
	        // ...
	    }

	    // Inner classes
	    private static class FossilData {
	        String calibrationPoint;
	        double ytmean;
	        double ytmin;
	        double ytmax;

	        FossilData(String calibrationPoint, double ytmean, double ytmin, double ytmax) {
	            this.calibrationPoint = calibrationPoint;
	            this.ytmean = ytmean;
	            this.ytmin = ytmin;
	            this.ytmax = ytmax;
	        }
	    }

	    private static class FossilResult {
	        String calibrationPoint;
	        double IOx;
	        double IOxmin;
	        double IOxmax;
	        String fossil;

	        FossilResult(String calibrationPoint, double IOx, double IOxmin, double IOxmax, String fossil) {
	            this.calibrationPoint = calibrationPoint;
	            this.IOx = IOx;
	            this.IOxmin = IOxmin;
	            this.IOxmax = IOxmax;
	            this.fossil = fossil;
	        }
	    }
	}
