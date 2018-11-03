package Search;

import Classes.Path;
import Classes.Query;
import Classes.Stemmer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ExtractQuery {
	private int index = 0;
	private ArrayList<String> idList = new ArrayList<>();
	private ArrayList<String> contentList = new ArrayList<>();
	public ExtractQuery() {
		//you should extract the 4 queries from the Path.TopicDir
		//NT: the query content of each topic should be 1) tokenized, 2) to lowercase, 3) remove stop words, 4) stemming
		//NT: you can simply pick up title only for query, or you can also use title + description + narrative for the query content.

		// save stop words
		Set<String> stopWord = new HashSet<>();
		try {
			File file = new File(Path.StopwordDir);
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String temp = null;
			while((temp = bufferedReader.readLine())!= null){
				stopWord.add(temp);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			File file = new File(Path.TopicDir);
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String line = null;
			while((line = bufferedReader.readLine()) != null){
				if(line.contains("<num>")){
					String id = line.substring(14);
					idList.add(id);
				}else if(line.contains("<title>")){
					String content = line.substring(8);
					String words[] = content.replaceAll( "\\p{Punct}", "" ).split(" ");	// 1. tokenized
					StringBuilder sb = new StringBuilder();
					for(String word : words){
						word = word.toLowerCase();	// 2. to lower case
						if(!stopWord.contains(word)){		// 3. remove stop words
							String str = new String();
							char []chars = word.toCharArray();
							Stemmer s = new Stemmer();
							s.add(chars, chars.length);
							s.stem();						// 4. stem
							str = s.toString();
							sb.append(str+" ");
						}
					}
					sb.setLength(sb.length()-1);
					contentList.add(sb.toString());
				}
			}
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasNext()
	{
		if(index < idList.size()){
			return true;
		}else{
			return false;
		}
	}
	
	public Query next()
	{
		Query query = new Query();
		query.SetTopicId(idList.get(index));
		query.SetQueryContent(contentList.get(index));
		index++;
		return query;
	}
}
