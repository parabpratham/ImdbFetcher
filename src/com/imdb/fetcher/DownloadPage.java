package com.imdb.fetcher;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.shekhar.facedetection.FaceDetector;

public class DownloadPage {

	public static final boolean proxyReqd = true;

	private static final String urlString = "http://www.imdb.com/title/tt4535650/";

	public static final String storagePath = "/home/pratham/workspace/ImdbFetcher/images/";

	private final FaceDetector detector;

	private URLConnection conn;

	private final DocumentBuilder builder;

	private static final DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();

	public DownloadPage() throws MalformedURLException, IOException,
			ParserConfigurationException {

		builder = factory.newDocumentBuilder();

		// detector = new FaceDetector();
		detector = null;
	}

	public static void main(String[] args) {

		try {
			DownloadPage downloadPage = new DownloadPage();
			downloadPage.processRequest(urlString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processRequest(String urlString) {
		try {

			if (proxyReqd)
				conn = new URL(urlString).openConnection(getProxies());
			else
				conn = new URL(urlString).openConnection();

			InputStream is = conn.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = null;

			// read each line and write to System.out
			String temp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n <info> <h2>Cast</h2>";

			String movieName = "";

			boolean found = false;
			while ((line = br.readLine()) != null) {

				if (!found
						&& line.contains("meta property='og:title' content=")) {
					String[] split = line.split("=");
					movieName = split[2].substring(1, split[2].length() - 4);
				}

				if (line.contains("<table class=\"cast_list\">"))
					found = true;

				if (!found)
					continue;

				temp += line + "\n";

				if (found && line.contains("</table>")) {
					break;
				}

			}

			temp += "</info>";

			temp = temp.replace("src=\"", " src=\"");
			temp = temp.replace("class=\"", " class=\"");
			temp = temp.replace("itemscope", "");

			File moviePage = new File(
					"/home/pratham/workspace/ImdbFetcher/Pages/" + movieName
							+ ".xml");
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
					moviePage));

			bufferedWriter.write(temp);
			bufferedWriter.flush();
			bufferedWriter.close();

			parseXML(moviePage);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseXML(File movieXML) {

		Document document;
		HashMap<String, String> castMap = new HashMap<>();
		try {

			document = builder.parse(movieXML);
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "/info/table/tr/td/a/img";
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
					document, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node nNode = nodeList.item(i);
				String name = nNode.getAttributes().getNamedItem("alt")
						.getTextContent();
				String url = nNode.getParentNode().getAttributes()
						.getNamedItem("href").getTextContent();
				String imageAdd = "";
				try {
					imageAdd = nNode.getAttributes().getNamedItem("loadlate")
							.getTextContent();
					imageAdd = imageAdd.replace("32", "600");
					imageAdd = imageAdd.replace("44", "800");
					File image = getImageURL(name, url, imageAdd);
				} catch (Exception e) {
					imageAdd = "No photo attached";
				}
				System.out.println(url + " -- " + name + " -- " + imageAdd);
				castMap.put(url, name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	private File getImageURL(String name, String url, String urlString) {

		String[] split = urlString.split("/");
		split = split[split.length - 1].split("@");
		split = split[0].split("._");
		String fileName = name + "_" + (split[0]);
		File opImage = new File(storagePath + fileName + ".jpg");
		try {
			if (proxyReqd)
				conn = new URL(urlString).openConnection(getProxies());
			else
				conn = new URL(urlString).openConnection();
			InputStream is = conn.getInputStream();
			BufferedImage bi = ImageIO.read(is);
			ImageIO.write(bi, "jpg", opImage);

			// detector.fetchFaces(fileName, storagePath + fileName + ".jpg");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return opImage;
	}

	private Proxy getProxies() {
		Proxy proxy = null;
		try {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
					"172.18.61.10", 3128));

			Authenticator authenticator = new Authenticator() {

				public PasswordAuthentication getPasswordAuthentication() {
					return (new PasswordAuthentication("142170003",
							"pratham1234".toCharArray()));
				}
			};
			Authenticator.setDefault(authenticator);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return proxy;
	}

}
