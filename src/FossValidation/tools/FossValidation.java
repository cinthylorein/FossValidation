package FossValidation.tools;


	import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

	public class FossValidation extends Runnable {
	    final public Input<Table> inputTable = new Input<>("inputTable", "Input table with DTCtmean, DTCtmin, and DTCtmax columns");

	    // Additional inputs
	    // ...

	    @Override
	    public void run() {
	        Table inputTable = inputTableInput.get();

	        FossilResult fossilResult = calculateFossilSSx(inputTable);

	        // Further processing or output
	        // ...
	    }

	    private FossilResult calculateFossilSSx(Table inputTable) {
	        double sumSSx = 0;
	        double sumSSxmin = 0;
	        double sumSSxmax = 0;

	        int n = 10;

	        SummaryStatistics ssxStats = new SummaryStatistics();
	        SummaryStatistics ssxminStats = new SummaryStatistics();
	        SummaryStatistics ssxmaxStats = new SummaryStatistics();

	        for (TableRow row : inputTable) {
	            double dtCtmean = row.getDouble("DTCtmean");
	            double dtCtmin = row.getDouble("DTCtmin");
	            double dtCtmax = row.getDouble("DTCtmax");

	            double ssx = Math.pow(dtCtmean, 2);
	            double ssxmin = Math.pow(dtCtmin, 2);
	            double ssxmax = Math.pow(dtCtmax, 2);

	            ssxStats.addValue(ssx);
	            ssxminStats.addValue(ssxmin);
	            ssxmaxStats.addValue(ssxmax);

	            sumSSx += ssx;
	            sumSSxmin += ssxmin;
	            sumSSxmax += ssxmax;
	        }

	        double meanSSx = ssxStats.getMean();
	        double meanSSxmin = ssxminStats.getMean();
	        double meanSSxmax = ssxmaxStats.getMean();

	        double sq = meanSSx / n * (n - 1);
	        double sqmin = meanSSxmin / n * (n - 1);
	        double sqmax = meanSSxmax / n * (n - 1);

	        return new FossilResult(sq, sqmin, sqmax, sumSSx, sumSSxmin, sumSSxmax, "FossilIncludePlaceholder");
	    }

	    // Inner class
	    private static class FossilResult {
	        double sq;
	        double sqmin;
	        double sqmax;
	        double sumSSx;
	        double sumSSxmin;
	        double sumSSxmax;
	        String fossil;

	        FossilResult(double sq, double sqmin, double sqmax, double sumSSx, double sumSSxmin, double sumSSxmax, String fossil) {
	            this.sq = sq;
	            this.sqmin = sqmin;
	            this.sqmax = sqmax;
	            this.sumSSx = sumSSx;
	            this.sumSSxmin = sumSSxmin;
	            this.sumSSxmax = sumSSxmax;
	            this.fossil = fossil;
	        }
	    }
	}

