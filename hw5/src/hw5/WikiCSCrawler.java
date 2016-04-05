package hw5;

public class WikiCSCrawler {
	
	public static void main(String[] args) {
		String param = "/wiki/Computer_Science";
		//String param = "/wiki/Complexity_theory";
		int max = 1000;
		String file = "WikiCS.txt";
		WikiCrawler crawler = new WikiCrawler(param, max, file);
		crawler.crawl();
	}
}
