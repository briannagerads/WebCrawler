package hw5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class WikiCrawler {
	/**
	 * the seed to search on wikipedia
	 */
	private String seedUrl;
	/**
	 * the max number of pages to crawl
	 */
	private int max;
	/**
	 * the name of the file the user wants the information written to
	 */
	private String fileName;
	/**
	 * the file that the CS 311 instructors want the information written to
	 */
	//private String wikiText = "WikiCS.txt";
	/**
	 * the base url for wikipedia
	 */
	private final static String BASE_URL = "https://en.wikipedia.org";
	/**
	 * the url used as a holder for the base and seed combined to fetch a page
	 */
	private URL url;
	/**
	 * the pages extracted from another wiki page
	 */
	private ArrayList<String> pagesCrawled;
	/**
	 * the number of edges added to the graph
	 */
	private int numEdges;
	/**
	 * the string of the html page
	 */
	private String htmlPage;
	/**
	 * number of pages fetched/crawled
	 */
	private int numPagesCrawled;
	/**
	 * the string that holds the graph information until it is written to the file
	 */
	private String toWrite;
	/**
	 * holds the location of the index of the /wiki/ to add to determine where to parse the document string
	 */
	private static int wikiLoc;
	
	/**
	 * COnstructor for the wiki crawler
	 * @param seedUrl - relative address of the seed url (within Wiki domain)
	 * @param max - Maximum number pages to be crawled
	 * @param fileName - name of a file the graph will be writing to
	 */
	public WikiCrawler(String seedUrl, int max, String fileName) {
		//parameters required from user
		this.seedUrl = seedUrl;
		this.max = max;
		this.fileName = fileName;
		
		//initialize all other private variables
		pagesCrawled = new ArrayList<String>();
		numEdges = 0;
		numPagesCrawled = 0;
		htmlPage = "";
		toWrite = "";
		//wikiLoc = 0;
	}
	
	/**
	 * Extract only wiki links (links that are of form /wiki/xxx)
	 * Only extract links that appear after the first occurrence of the html tag <p> or <P>
	 * Should NOT extract any wiki link that contain the characters '#' or ':'
	 * @param doc - contents of a .html file
	 * @return ArrayList<String> consisting of links from doc in order they appear in the doc
	 */
	public static ArrayList<String> extractLinks(String doc) {
		ArrayList<String> result = new ArrayList<String>();
		
		//get the string of the document starting after the <p>
		int loc = getIndexOfP(doc);
		int index = 0;
		doc = doc.substring(loc);
		String add = getWiki(doc);
		//add if it is a valid /wiki/
		while(!add.equals("none")) {
			if (!(add.contains("#") || add.contains(":")) && !result.contains(add) && !add.equals("-1")) {
				result.add(add);
			}
			
			// get updated string without previous wiki
			index = doc.indexOf("/wiki/");
			doc = doc.substring(index+add.length()-1);
			add = getWiki(doc);
		}
		return result;
	}
	
	/**
	 * Construct the web graph over the following:
	 * - max many pages that are visited when you do a BFS with seedUrl
	 * write the graph to the file fileName
	 * write graph to WikiCS.txt - will contain all the edges of the graph
	 * -> each line should have one directed edge except for the first line
	 * -> the first line of the graph should indicate number of vertices
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException 
	 */
	public void crawl() {
		
		try {
			BFS(seedUrl);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//write to fileName
		writeToFile(fileName, toWrite);
	}
	
	
	
	// ---------------------------- HELPER FUNCTIONS ---------------------------- \\
	
	
	
	/**
	 * Construct file and write the designated information to it
	 * @param name - name of the file to be created
	 * @param graph - the graph to be written to the file
	 */
	private void writeToFile(String name, String graph) {
		//create file
		BufferedWriter write = null;
		try {
			write = new BufferedWriter(new FileWriter(name));
			write.write(Integer.toString(max));
			write.newLine();
			
			//write graph to file
			write.write(graph);
			if  (write != null) write.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * BFS the graph edges
	 * @param seed - the seed to BFS
	 * @throws IOException 
	 * WAIT 3 SECONDS every 100 requests
	 * @throws InterruptedException 
	 */
	private boolean BFS(String seed) throws IOException, InterruptedException {
		//initialize queue and list
		Queue<String> q = new LinkedList<String>();
		List<String> visited = new LinkedList<String>();
		int delay = 3; //3 second delay
		
		//add seed to queue and list
		q.add(seed);
		visited.add(seed);
		
		String currentPage;
		int numDelay = 0;
		
		while (!q.isEmpty()) {
			//let current page be first element of q
			currentPage = q.remove().toString();

			numPagesCrawled++;
			//if hit max pages, return to break out of cycle, else fetch the page
			if (numPagesCrawled > max) {
				System.out.println("edges: " + numEdges + ", numPagesCrawled: " + numPagesCrawled);
				return true;
			}
			//send request to server at currentPage and download currentPage
			//courtesy rule: wait 3s every 100 pages
			if (numPagesCrawled%100 == 0 && numPagesCrawled != 0) {
				TimeUnit.SECONDS.sleep(delay);
				System.out.println("delay: " + numDelay + ", pagesCrawled: " + numPagesCrawled);
			}
			fetchPage(currentPage);
			
			
			//extract all links from currentPage
			pagesCrawled = extractLinks(htmlPage);
			
			//for every link u that appears in currentPage
			for (String u : pagesCrawled) {
				//if u not visited, add u to the end of q and add u to visited
				if (!visited.contains(u)) {
					//write currentpage and u to file
					toWrite += currentPage + "  " + u;
					toWrite += "\r\n";
					
					
					q.add(u);
					visited.add(u);
					numEdges++;
				}
			}
		}
		System.out.println(numEdges);
		return false;
	}
	
	/**
	 * Helper method to get location of nearest <p> or <P>
	 * @param doc - string to look for <p> or <P>
	 * @return closest location of occurrence
	 */
	private static int getIndexOfP(String doc) {
		int index1 = -1;
		int index2 = -1;
		
		index1 = doc.indexOf("<p>");
		index2 = doc.indexOf("<P>");		
		
		if (index1 == -1 && index2 != -1) return index2;
		if (index2 == -1 && index1 != -1) return index1;
		if (index1 < index2 && index1 != -1) return index1;
		if (index2 < index1 && index2 != -1) return index2;
		
		return -1;
	}
	
	/**
	 * Helper method to get location of nearest /wiki/
	 * @param doc - string to look for <p> or <P>
	 * @return closest location of occurrence
	 */
	private static String getWiki(String doc) {
		int start = -1;
		int end = -1;
		
		// get wiki location
		start = doc.indexOf("/wiki/");
		if (start == -1) return "none";
		if (doc.charAt(start-1) != '"') return "-1";
		
		//parse the wiki string apart from the given string
		String temp = doc.substring(start);
		end = temp.indexOf("\"");
		String result = temp.substring(0, end);
		if (start != -1 && end != -1) return result;
		
		return "none";
	}
	
	/**
	 * @param seed - the wiki to fetch
	 * @return string of html page for the given seed
	 * @throws IOException
	 */
	private String fetchPage(String seed) throws IOException {
		// generate URL
		try {
			url = new URL(BASE_URL + seed);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		InputStream is = null;
		try {
			is = url.openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// write webpage to HTML page to output.html
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String inputLine = null;
		BufferedWriter output = new BufferedWriter(new FileWriter("output.html"));
		while ((inputLine = reader.readLine()) != null) {
			output.write(inputLine);
			output.newLine();
		}
		reader.close();
		output.close();
		
		//convert to string
		htmlPage = documentToString("output.html");
		
		return htmlPage;
	}
	
	/**
	 * Converts a document to a String
	 * @param filename - name of the file to convert
	 * @return document in String form
	 * @throws FileNotFoundException
	 */
	private static String documentToString(String filename) throws FileNotFoundException {
		File file = new File(filename);
		Scanner s = new Scanner(file);
		String fileContents = "";
		
		//convert file to string
		fileContents = s.next();
		while (s.hasNext()) {
			fileContents += s.next();
		}
		s.close();

		return fileContents;
	}
	
}
