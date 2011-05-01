package net.inervo.twitter;

/*
 * Copyright (c) 2011, Ted Timmons, Inervo Networks All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * Inervo Networks nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TwilterDOM {
	String urlString = null;
	String username = null;
	Document dom = null;

	public TwilterDOM( String url, String user ) {
		urlString = url;
		username = user;
	}

	// protected, we need a URL.
	protected TwilterDOM() {
	}

	public static void main( String[] args ) throws Exception {
		String outputFile = "twitter.xml";
		if ( args.length > 0 ) {
			outputFile = args[0];
		}

		TwilterDOM tw = new TwilterDOM( "http://twitter.com/statuses/user_timeline/14667502.rss", "tedder42" );
		ByteArrayOutputStream rssOutput = tw.parseTwitter( tw.getRSS() );

		FileOutputStream fos = new FileOutputStream( outputFile );
		Writer out = new OutputStreamWriter( fos, "UTF8" );
		out.write( rssOutput.toString() );
		out.close();
		System.out.println( "writing RSS: " + outputFile );
	}

	private InputStream getRSS() throws IOException {
		URL rss = new URL( urlString );
		URLConnection connection = rss.openConnection();
		InputStream is = connection.getInputStream();
		return is;
	}

	private ByteArrayOutputStream parseTwitter( InputStream is ) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		RSSOutput rss = new RSSOutput( "tedder42's filtered twitter", "tedder42's filtered twitter", "http://twitter.com/tedder42/" );

		// parse using builder to get DOM representation of the XML file
		dom = db.parse( is );

		// get the root elememt
		Element docEle = dom.getDocumentElement();

		// get a nodelist of <employee> elements
		NodeList items = docEle.getElementsByTagName( "item" );

		for ( int i = 0; i < items.getLength(); ++i ) {

			Node item = items.item( i );
			Map<String, String> map = parseNode( item );
			if ( shouldRepublish( item ) ) {
				String title = map.get( "title" ).replace( "tedder42: ", "" );
				// String link = map.get( "link" );
				String link = "";
				rss.addItem( link, title, parseDescription( title ) );

			}
		}

		ByteArrayOutputStream rssOutput = rss.getRSSasOutputStream();
		return rssOutput;
	}

	private String parseDescription( String desc ) {
		String ret = desc.replaceAll( "(http://\\S+)", "<a href=\"$1\">$1</a>" );
		ret = ret.replaceAll( "(@(\\S+))", "<a href=\"http://twitter.com/$2\">$1</a>" );

		return ret;
	}

	private boolean shouldRepublish( Node node ) {
		boolean shouldPublish = true;
		NodeList children = node.getChildNodes();
		for ( int i = 0; i < children.getLength(); ++i ) {
			Node child = children.item( i );
			if ( child.getNodeName().equalsIgnoreCase( "title" ) && child.getTextContent().startsWith( username + ": @" ) ) {
				shouldPublish = false;
			}
		}

		return shouldPublish;
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

}
