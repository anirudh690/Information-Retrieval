package edu.asu.irs13;

//import java.io.*;
import java.io.File;
import java.util.ArrayList;
//import java.util.Locale;




import org.apache.commons.lang3.time.StopWatch;
//import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import Jama.Matrix;

public class auth_hub_calc {

	// Function to calculate the base set

	public ArrayList<Integer> getBaseSet(ArrayList<Integer> myDocs)
			throws Exception {
		
		StopWatch sw1 = new StopWatch();
		sw1.reset();
		sw1.start();

		// int size = 0;

		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));

		// Calculating the total documents in the corpus

		int max_doc = r.maxDoc();

		LinkAnalysis.numDocs = max_doc;
		LinkAnalysis l = new LinkAnalysis();

		// System.out.println("Inside getBase function");

		// Creating the base set

		ArrayList<Integer> base_set = new ArrayList<Integer>();

		for (Integer key : myDocs) {

			if (!base_set.contains(key)) {
				base_set.add(key);
			}

			// Getting all the links to a document

			int[] link = l.getLinks(key);
			for (int p = 0; p < link.length; p++) {
				if (!base_set.contains(link[p])) {
					base_set.add(link[p]);
				}
				// System.out.print( + ",");
			}

			// Getting all the citations to a document

			int[] cit = l.getCitations(key);
			for (int k = 0; k < cit.length; k++) {
				if (!base_set.contains(cit[k])) {
					base_set.add(cit[k]);
				}
				// System.out.print(pb + ",");
			}
		}

		//System.out.println("Base set = " + base_set);

		sw1.stop();
		System.out.println("Time taken to obtain the base set ="
				+ (double) sw1.getNanoTime() / 1000000000 + " seconds");

		return base_set;

	}

	// Function to calculate the adjacency matrix

	public Matrix getAdjacencyMatrix(ArrayList<Integer> base_set,
			ArrayList<Integer> top_ten_docs) throws Exception {

		StopWatch sw1 = new StopWatch();
		sw1.reset();
		sw1.start();
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));

		// Calculating the total documents in the corpus

		int max_doc = r.maxDoc();

		int len = base_set.size();
		// System.out.println("Size of base set is = " + len);
		LinkAnalysis.numDocs = max_doc;
		LinkAnalysis l = new LinkAnalysis();
		Matrix adjacencyMatrix = new Matrix(len, len);

		for (Integer doc : top_ten_docs) {
			int links[] = l.getLinks(doc);

			int index = base_set.indexOf(doc);

			if (links.length > 0) {

				for (int p = 0; p < links.length; p++) {

					int link = base_set.indexOf(links[p]);

					// System.out.println("Index and link value are : " + index
					// + ":" + link);

					adjacencyMatrix.set(index, link, 1);

				}
			}

			int[] cit = l.getCitations(doc);

			if (cit.length > 0) {

				for (int q = 0; q < cit.length; q++) {

					int cita = base_set.indexOf(cit[q]);

					adjacencyMatrix.set(cita, index, 1);

				}
			}
		}
		
		sw1.stop();
		System.out.println("Time taken to obtain the adjacency matrix ="
				+ (double) sw1.getNanoTime() / 1000000000 + " seconds");


		//adjacencyMatrix.print(0, 0);

		return adjacencyMatrix;

	}

}
