package net.inervo.RSS;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.inervo.data.Keystore;
import net.inervo.output.AmazonEmail;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Funtasticus {
	private final static int DEFAULT_START_DAYS_AGO = 7;
	private final static String UPDATE_TIME_KEY = "update time";
	private final static String FEED_URL = "http://feeds2.feedburner.com/funtasticus";
	private static DateTimeFormatter ISODateFormatter = ISODateTimeFormat.dateTime();
	private DateTime lastUpdateTime = null;
	Keystore keystore = null;
	AmazonEmail emailer = null;

	public Funtasticus() throws Exception {
		openKeystore( "Funtasticus" );
		emailer = new AmazonEmail();
		getLastUpdateTime();
		readFeed();
	}

	public void readFeed() throws IOException, ParserConfigurationException, SAXException, AddressException, MessagingException {
		InputStream is = this.getRSS( FEED_URL );
		List<Item> items = parseFeed( is );
		StringBuilder body = new StringBuilder();

		if ( items == null || items.size() == 0 ) {
			print("no items in feed, we're outta.");
			return;
		}
		for ( Item item : items ) {
			body.append( item.toString() );
			body.append( "\n" );
		}

		emailer.sendEmail( "ted@perljam.net", "funtasticus foo", body.toString() );
	}

	private InputStream getRSS( String urlString ) throws IOException {
		URL rss = new URL( urlString );
		URLConnection connection = rss.openConnection();
		InputStream is = connection.getInputStream();
		return is;
	}

	private List<Item> parseFeed( InputStream is ) throws ParserConfigurationException, SAXException, IOException {
		List<Item> returnList = new LinkedList<Item>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		DateTimeFormatter rssDateTimeFormatter = DateTimeFormat.forPattern( "EEE, dd MMM yyyy HH:mm:ss +0000" );

		// parse using builder to get DOM representation of the XML file
		Document dom = db.parse( is );

		// get the root element
		Element docEle = dom.getDocumentElement();

		// get a nodelist of elements
		NodeList items = docEle.getElementsByTagName( "item" );

		DateTime maxTime = lastUpdateTime;
		for ( int i = 0; i < items.getLength(); ++i ) {

			Node nodeItem = items.item( i );
			Map<String, String> map = parseNode( nodeItem );

			// Fri, 29 Apr 2011 14:00:19 +0000
			String pubDate = map.get( "pubDate" );
			DateTime itemTime = rssDateTimeFormatter.parseDateTime( pubDate );

			// skip earlier entries
			if ( !itemTime.isAfter( lastUpdateTime ) ) {
				continue;
			}

			// update max
			if ( itemTime.isAfter( maxTime ) ) {
				maxTime = itemTime;
			}

			Item item = new Item( map );
			System.out.println( item );

			// print( map );
			returnList.add( item );
		}

		lastUpdateTime = maxTime;

		// ByteArrayOutputStream rssOutput = rss.getRSSasOutputStream();
		return returnList;
	}

	private Map<String, String> parseNode( Node node ) {
		Map<String, String> nodeMap = new HashMap<String, String>();

		NodeList children = node.getChildNodes();
		for ( int i = 0; i < children.getLength(); ++i ) {
			Node child = children.item( i );

			String name = child.getNodeName();
			String content = child.getTextContent();
			nodeMap.put( name, content );
		}

		return nodeMap;
	}

	public void openKeystore( String itemKey ) throws Exception {
		try {
			keystore = new Keystore( itemKey );
		} catch ( IOException e ) {
			throw new Exception( "Error getting AWS key: " + e.getLocalizedMessage() );
		}
	}

	public DateTime getLastUpdateTime() {
		String updateTimeString = keystore.getKey( UPDATE_TIME_KEY );
		print( "timestring: " + updateTimeString );
		if ( updateTimeString == null ) {
			lastUpdateTime = new DateTime().minusDays( DEFAULT_START_DAYS_AGO );
		} else {
			lastUpdateTime = ISODateFormatter.parseDateTime( updateTimeString );
		}

		print( "utime: " + ISODateFormatter.print( lastUpdateTime ) );
		return lastUpdateTime;
	}

	public String getUpdateTimeString() {
		return ISODateFormatter.print( lastUpdateTime );
	}

	public void setLastUpdateTime() {
		 keystore.replace( UPDATE_TIME_KEY, getUpdateTimeString() );
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main( String[] args ) throws Exception {
		Funtasticus f = new Funtasticus();
		f.setLastUpdateTime();

	}

	private static void print( String s ) {
		System.out.println( s );
	}

}

class Item {
	Map<String, String> map = null;

	public Item( Map<String, String> map ) {
		this.map = map;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();

		out.append( map.get( "title" ) );
		out.append( "\n" );
		out.append( "Category: " );
		out.append( map.get( "category" ) );
		out.append( "\n" );
		out.append( map.get( "feedburner:origLink" ) );
		out.append( "\n" );
		out.append( map.get( "pubDate" ) );
		out.append( "\n" );

		return out.toString();
	}

}