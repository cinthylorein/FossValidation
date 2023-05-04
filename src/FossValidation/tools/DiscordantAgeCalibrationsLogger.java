package FossValidation.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;


import beastfx.app.tools.Application;
import beastfx.app.util.OutFile;
import beastfx.app.util.TreeFile;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Runnable;
import beast.base.core.Log;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.parser.NexusParser;


@Description("Create trace log of difference between the molecular fossil age (tree1) and observed fossil age (tree2) for all other fossil-dated nodes on the tree. "
		+ "Usefull to measure the agreement, or consistency, between any fossil calibration point and other available fossil calibrations.")


//It extends the Runnable class, which means it can be run as a separate thread?


public class DiscordantAgeCalibrationsLogger extends Runnable {
	final public Input<TreeFile> summaryTree = new Input<>("tree1", "true tree file with tree in NEXUS format");
	final public Input<List<TreeFile>> srcInput = new Input<>("trees", " fossil n tree files in NEXUS format", new ArrayList<>());
	final public Input<OutFile> outputInput = new Input<>("output", "trace output file that can be processed in Tracer. Not produced if not specified.");	
	private List<List<?>> traces = new ArrayList<>();
	

	// @Override is an annotation used in Java to indicate that a method is meant to override a method from a superclass or interface.
	
	@Override
	
	// intAndValidate method: This means that this method is being replaced with a new implementation that is specific to this class. The purpose of this method is to initialize and validate the inputs provided to the class before running the main program logic.

	public void initAndValidate() { 
		
		// TODO Auto-generated method stub
		
	}
		

	
	@Override
	public void run() throws Exception {
		// sanity checks
				if (outputInput.get() == null) {
					throw new IllegalArgumentException("output must be specified");
				}
				if (summaryTree.get() == null) {
					throw new IllegalArgumentException("tree1 must be specified");
				}
				if (srcInput.get().size() == 0) {
					throw new IllegalArgumentException("tree2 or trees must be specified");
				}
			
			
	
	// load species trees 
	// It uses the NexusParser class to parse the species tree file specified in the summarytreeInput input. 
    //The resulting trees are stored in the speciesTrees list.			
	
	NexusParser speciesTreeParser = new NexusParser();
	speciesTreeParser.parseFile(summaryTree.get());
	List<Tree> speciesTree = speciesTreeParser.trees;
	
	// process tree2
	//For each fossil n tree file specified in the srcInput input, it calls the processTree() method, passing in the tree2 file and the summary tree list.
	//The processTree() method computes the difference between the tree 2 or trees files and the summary tree, and returns a list of these differences.

	for (TreeFile f : srcInput.get()) {
		List<Double> diferences = processTree(f, speciesTree);
		// The list of differences for each fossil n tree is added to the traces list.
		traces.add(diferences);
	}

	//The saveTrace() method is called to save the traces list to a file specified in the outputInput input, if it has been specified.
	saveTrace();
	
	Log.warning("Done!");
	
	}

	// The processTree() method computes the difference between each node in the fossil n tree and the summary tree (true tree), and returns a list of these differences.
	private List<Double> processTree(TreeFile f, List<Tree> speciesTree) throws IOException {
	    List<Double> differences = new ArrayList<>();

	    NexusParser fossilTreeParser = new NexusParser();
	    fossilTreeParser.parseFile(f);
	    List<Tree> fossilTrees = fossilTreeParser.trees;

	    for (int k = 0; k < speciesTree.size() && k < fossilTrees.size(); k++) {
	        Tree trueTree = speciesTree.get(k);
	        Tree fossilTree = fossilTrees.get(k);
	        normaliseLabels(fossilTree);
	        int totalNodes = trueTree.getInternalNodeCount();
	        Node[] fossilNodes = fossilTree.getNodesAsArray();
	        Node[] speciesNodes = trueTree.getNodesAsArray();

	        double differenceSum = 0.0;
	        for (int i = 0; i < fossilNodes.length; i++) {
	            Node fossilNode = fossilNodes[i];
	            Node speciesNode = speciesNodes[i];
	            if (!fossilNode.isLeaf() && !speciesNode.isLeaf()) {
	                double fossilHeight = fossilNode.getHeight();
	                double speciesHeight = speciesNode.getHeight();
	                double difference = Math.abs(fossilHeight - speciesHeight);
	                differenceSum += difference;
	            }
	        }
	        double differenceAvg = differenceSum / totalNodes;
	        differences.add(differenceAvg);
	    }

	    return differences;
	}
	
	private void normaliseLabels(Tree geneTree) {
		Node [] nodes = geneTree.getNodesAsArray();
		for (int i = 0; i < nodes.length/2+1; i++);
			}
	
	/**
	 * save entries as tab separated file, which can be used in Tracer
	 */
	private void saveTrace() throws IOException {
		PrintStream out = new PrintStream(outputInput.get());
		
		// header
		out.print("Sample\t");
		for (TreeFile f : srcInput.get()) {
			String name = f.getName();
			out.print(name+"\t");
		}
		out.println();
		
		// tab separated data
		int min = Integer.MAX_VALUE;
		for (int j = 0; j < traces.size(); j++) {
			min = Math.min(traces.get(j).size(), min);
		}
		
		for (int i = 0; i < min; i++) {
			out.print(i + "\t");
			for (int j = 0; j < traces.size(); j++) {
				out.print(traces.get(j).get(i) + "\t");
			}
			out.println();
		}
		out.close();
	}
	
	public static void main(String[] args) throws Exception {
		new Application(new DiscordantAgeCalibrationsLogger(), "DiscordantAgeCalibrationsLogger", args);
	}

}
