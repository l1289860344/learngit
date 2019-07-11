package KMeans;

import KMeans.api.Encoder;
import KMeans.impl.CosineDistance;
import KMeans.impl.KMeansImpl;
import KMeans.impl.TfidfEncoder;
import KMeans.rep.Cluster;
import KMeans.rep.ClusterList;
import KMeans.rep.Distance;
import KMeans.rep.DocumentList;

/**
 * KMeans.Main class to run k-means algorithm
 * @author hazoom
 * https://blog.csdn.net/qy20115549/article/details/82025563
 * https://github.com/Hazoom/documents-k-means
 */
public class Main {

	public static void main(String[] args) {	
		DocumentList documents = new DocumentList(args[0]);
		
		System.out.println("Finish preprocessing...");
		
		Encoder encoder = new TfidfEncoder(30000);
		encoder.encode(documents);

		System.out.println("Finish encoding...");
		
		Distance distancce = new CosineDistance();
		
		KMeansImpl kmeans = new KMeansImpl(distancce, 8, 10);
		ClusterList clusters = kmeans.cluster(documents);
		
		System.out.println("Finish K-means algorithm...");
		
		int i = 1;
		for (Cluster cluster : clusters.getClusters()) {
			System.out.println("Cluster no. " + i + " has " + cluster.getDocuments().size() + " documents.");
			i++;
		}
	}

}
