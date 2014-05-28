package edu.asu.irs13;

//import java.util.ArrayList;
//import java.util.List;
//import java.util.Arrays;
//import java.util.HashMap;

//import org.apache.commons.lang3.ArrayUtils;

public class PageRankCalculation {
	public double[][] pgCalc() {
		//System.out.println("In pg calc");
		int maxDocs = 25054;
		LinkAnalysis.numDocs = maxDocs;
		LinkAnalysis l = new LinkAnalysis();
		
		//To store citations
		//HashMap<Integer,List<Integer>> cit_map = new HashMap<Integer, List<Integer>>();
		//To store sink nodes
		double sink_nodes[]=new double[25054];
		//Declare rank matrix
		double rank_new[][] = new double[maxDocs][1];
		//Declare old matrix
		double rank_old[][] = new double[maxDocs][1];
		//Initialize value for initial rank matrix
		double s= 1.0/(double) maxDocs;
		//Declare c value
		double c=0.2;
		
		//int for_count=0;
		for(int i=0; i<maxDocs; i++) {
			//for_count++;
			//Store citations
			//int cit[] = l.getCitations(i);
			//cit_map.put(i, Arrays.asList(ArrayUtils.toObject(cit)));
			//Store sink nodes
			int link_size = l.getLinks(i).length;
			if(link_size == 0) {
				sink_nodes[i]=s;
			}
			//Initialize rank matrices
			rank_new[i][0]=s;
			rank_old[i][0]=s;
			//System.out.println(for_count);
		}
		int flag;
		int times=0;
		//Start power iteration
		while(true) {
			//System.out.println("In while");
			times++;
			flag=1;
			//Copy new values into old rank array
			for(int i=0; i<maxDocs;i++) {
				rank_old[i][0]=rank_new[i][0];
			}
			
			//int outer_count=0;

			for(int i=0; i<maxDocs; i++) {
				//System.out.println("Inside for loop 1");
				double sum=0;
				//Store citations
				double m[]=new double[25054];
				int cit[] = l.getCitations(i);
				
				//int inner_count=0;
				
				for(int x=0; x<cit.length;x++) {
					//if(l.getLinks(cit[x]).length!=0) {
					m[cit[x]]= 1 / (double) (l.getLinks(cit[x]).length);
					//}
				}

				for(int j=0;j<maxDocs;j++) {
					//inner_count++;
					
					/*
					if(sink_nodes.contains(j)) {
						sum=sum + s*rank_old[j][0]; 
					}
					else if(cit_map.get(i).contains(j)) {
						sum=sum+ ( ( (c/l.getLinks(j).length) + ((1-c)/maxDocs) )  * rank_old[j][0]);
					}
					else {
						sum=sum+ (((1-c)/maxDocs)*rank_old[j][0]);
					}
					*/
					sum=sum+ ( ( (c*(m[j]+sink_nodes[j])) + (1-c)*s) * rank_old[j][0]) ;
				}
				//System.out.println(inner_count);
				rank_new[i][0]=sum;
				//outer_count++;
				
			}
			//System.out.println(outer_count);
			
			//Normalise the rank_new array. Find the largest element and divide each by this element
			
			
			
			for(int i=0;i<maxDocs;i++) {
				if(Math.abs( rank_new[i][0] - rank_old[i][0] ) > 0.00001) {
					flag=0;
					break;
				}
			}
			if(flag==0) {
				continue;
			}
			else {
				break;
			}
			
		}
		//System.out.println("Loop took place for " + times + "times");
		//System.out.println("Power iteration done");
		double max = rank_new[0][0];
		double min = rank_new[0][0];
		for(int i=0;i<maxDocs;i++)
		{
			if(rank_new[i][0] > max)
			{
				max = rank_new[i][0];
			}
			if(rank_new[i][0] < min) {
				min = rank_new[0][0];
			}
			
		}
		for(int i=0;i<maxDocs;i++)
		{
			rank_new[i][0]=((rank_new[i][0]-min)/(max-min));
		}
		
		for(int i=0; i<10; i++) {
			//System.out.println(rank_new[i][0]);
		}
		return rank_new;
	}
}
