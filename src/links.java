import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class links {
	private static HashMap<String, String> urlFileMap = new HashMap<>(); 
	private static HashMap<String, String> fileUrlMap = new HashMap<>(); 
	
	public static void main(String[] args) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader("/Users/tommybartecki/Downloads/URLtoHTML_fox_news.csv"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		        String[] values = line.split(",");
		        urlFileMap.put(values[1], values[0]);
		        fileUrlMap.put(values[0], values[1]);
		    }
		}
		
		File dir = new File("/Users/tommybartecki/Downloads/solr-8.8.2/foxnews");
		Set<String> edges = new HashSet<String>();
		
		for (File file: dir.listFiles()) {
			
	        if(fileUrlMap.get(file.getName()) != null){
				Document doc = Jsoup.parse(file, "UTF-8", fileUrlMap.get(file.getName()));
				Elements links = doc.select("a[href]"); // a with href
				Elements pngs = doc.select("[src]");
				for(Element link: links) {
					String url = link.attr("abs:href").trim();
				
					if(urlFileMap.containsKey(url)) {
						edges.add("/Users/tommybartecki/Downloads/solr-8.8.2/foxnews/" + file.getName() + " " + "/Users/tommybartecki/Downloads/solr-8.8.2/foxnews/" + urlFileMap.get(url));
					}
				}
	        }
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("edgeList.txt"));
		
		for (String s:edges) {
			writer.write(s);
			writer.newLine();
		}
		writer.flush();
		writer.close();
		
		
	}
	
	
}
