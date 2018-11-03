package Search;

import java.io.IOException;
import java.util.*;

import Classes.Query;
import Classes.Document;
import IndexingLucene.MyIndexReader;
import org.apache.lucene.index.IndexReader;

public class QueryRetrievalModel {
	
	protected MyIndexReader indexReader;
	private int colLen;
	public QueryRetrievalModel(MyIndexReader ixreader) {
		indexReader = ixreader;
		this.colLen = 0;
		for(int i = 0; i < indexReader.numDocs(); i++){	// for each document in the collection, calculate the collection length
			try {
				colLen += indexReader.docLength(i);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Search for the topic information. 
	 * The returned results (retrieved documents) should be ranked by the score (from the most relevant to the least).
	 * TopN specifies the maximum number of results to be returned.
	 * 
	 * @param aQuery The query to be searched for.
	 * @param TopN The maximum number of returned document
	 * @return
	 */
	
	public List<Document> retrieveQuery( Query aQuery, int TopN ) throws IOException {
		// NT: you will find our IndexingLucene.Myindexreader provides method: docLength()
		// implement your retrieval model here, and for each input query, return the topN retrieved documents
		// sort the documents based on their relevance score, from high to low

		HashMap<Integer, HashMap<String,Integer>> idTokenDocFrequencyMap = new HashMap<>();	//docid - (token - document frequency) map
		HashMap<String, Long> tokenColFrequencyMap = new HashMap<>();	// token - collection frequency map
		String content = aQuery.GetQueryContent();
		String[] tokens = content.split(" ");

		for(String token : tokens){
			tokenColFrequencyMap.put(token, indexReader.CollectionFreq(token)); 	// update the second map
			int [][]postingList = indexReader.getPostingList(token);
			if(postingList == null) continue;
			for(int i = 0; i < postingList.length; i++){
				int docid = postingList[i][0];
				int freq = postingList[i][1];
				if(idTokenDocFrequencyMap.containsKey(docid)){		// update the first map
					idTokenDocFrequencyMap.get(docid).put(token,freq);
				}else {
					HashMap<String,Integer> tokenFrequencyMap = new HashMap<>();
					tokenFrequencyMap.put(token,freq);
					idTokenDocFrequencyMap.put(docid,tokenFrequencyMap);
				}
			}
		}
		double U = 2000.0;
		List<Document> documentList = new ArrayList<>();
		// if use a docidList, it will have some duplicates.
		for(Map.Entry<Integer, HashMap<String,Integer>> entry : idTokenDocFrequencyMap.entrySet()){
			double score = 1.0;			// each document have one score
			int docid = entry.getKey();
			int docLen = indexReader.docLength(docid);	// get document length
			HashMap<String,Integer> tokenDocFrequencyMap = idTokenDocFrequencyMap.get(docid);
			for(String token : tokens){		// for each token, not the token just exist in the document
				long colFreq = tokenColFrequencyMap.getOrDefault(token,0l);	// get collection frequency
				if(colFreq != 0){
					int docFreq = 0;	// if this document don't have token, just set document frequency as 0
					if(tokenDocFrequencyMap.containsKey(token)){
						docFreq = tokenDocFrequencyMap.get(token);
					}
					score = score * (docFreq + U * ((double)colFreq / colLen)) / (docLen + U);	//equation
				}
			}
			Document document = new Document(docid+"", indexReader.getDocno(docid),score);
			documentList.add(document);
		}

		Collections.sort(documentList, new Comparator<Document>() {
			@Override
			public int compare(Document o1, Document o2) {
				return Double.compare(o2.score(),o1.score());
			}
		});
		List<Document> res = new ArrayList<>();
		for(int i = 0 ; i < TopN; i++){
			res.add(documentList.get(i));
		}
		return res;
	}
	
}