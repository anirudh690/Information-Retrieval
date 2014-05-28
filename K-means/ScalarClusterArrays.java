package edu.asu.irs13;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Iterator;

import org.apache.lucene.index.Term;

public class ScalarClusterArrays {
	public static double[][] scalarCluster(ArrayList<Term> unique_terms,
			HashMap<Integer, HashMap<Term, Double>> docTermMatrix, int top_50[]) {
		// Get size of unique terms arraylist
		int size = unique_terms.size();
		// Store term-term correlations in a two dimensional array
		double correlation[][] = new double[size][size];
		// Iterate through the unique terms array and calculate term-term
		// correlation
		for (int t1 = 0; t1 < unique_terms.size(); t1++) {
			for (int t2 = 0; t2 < unique_terms.size(); t2++) {
				double sum = 0;
				for (int i = 0; i < 50; i++) {
					if (docTermMatrix.get(top_50[i]).containsKey(
							unique_terms.get(t1))
							&& docTermMatrix.get(top_50[i]).containsKey(
									unique_terms.get(t2))) {
						sum += (double) (docTermMatrix.get(top_50[i]).get(
								unique_terms.get(t1)) * docTermMatrix.get(
								top_50[i]).get(unique_terms.get(t2)));
					}
				}
				correlation[t1][t2] = sum;
			}
		}

		double normalisedCluster[][] = new double[size][size];
		// Normalize the correlation matrix obtained above
		for (int t1 = 0; t1 < unique_terms.size(); t1++) {
			for (int t2 = 0; t2 < unique_terms.size(); t2++) {
				normalisedCluster[t1][t2] = (double) correlation[t1][t2]
						/ (correlation[t1][t1] + correlation[t2][t2] - correlation[t1][t2]);
			}
		}

		// Get Scalar cluster from normalised cluster
		double scalarCluster[][] = new double[size][size];

		for (int t1 = 0; t1 < unique_terms.size(); t1++) {
			for (int t2 = 0; t2 < unique_terms.size(); t2++) {
				scalarCluster[t1][t2] = dotProduct(normalisedCluster[t1],
						normalisedCluster[t2]);
			}
		}
		
		return scalarCluster;
	}
	
	public static double dotProduct(double term1[], double term2[]) {
		double dotProductValue = 0, term1_norm=0, term2_norm=0;
		for(int i=0; i<term1.length; i++) {
			dotProductValue += term1[i]*term2[i];
			term1_norm += term1[i]*term1[i];
			term2_norm += term2[i]*term2[i];
		}
		return dotProductValue/(Math.sqrt(term1_norm)*Math.sqrt(term2_norm));
	}

}
