package hw5;

public class WikiCSCrawler {
	
	public static void main(String[] args) {
		String param = "/wiki/Computer_Science";
		String param1 = "/wiki/Complexity_theory";
		//int max = 1000;
		int max = 100;
		//String file = "WikiCS.txt";
		String file = "Wikiwiki.txt";
		WikiCrawler crawler = new WikiCrawler(param, max, file);
		crawler.crawl();
	}
}
