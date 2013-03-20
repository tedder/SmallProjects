package net.inervo.advrider

import groovy.xml.MarkupBuilder
import java.text.SimpleDateFormat
@Grapes(
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2.1')
)

import org.ccil.cowan.tagsoup.Parser

class GScrape {

	static main(args) {
		def xml = fetchPage(args[0] as int)
		writeToFile(args[1], xml)
	}

	// stolen: http://stackoverflow.com/a/4275240/6592984275240
	public static void writeToFile(String filewithpath, String infoList) {
		new File(filewithpath).withWriter { out ->
			infoList.eachLine {
				out.println it
			}
		}
	}


	static fetchPage(int forum, int page = 1) {
		def ret = ""
		def comparator = [ compare:	{a,b-> b as Integer <=> a as Integer } ] as Comparator
		def threadmap = new TreeMap( comparator )

		def slurper = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())

		def gurl = new URL("http://www.advrider.com/forums/forumdisplay.php?f=${forum}&pp=200&page=${page}")
		gurl.withReader { gReader ->

			def gHTML = slurper.parse(gReader)

			//			def try1 = gHTML.body.find { it['@id'].startsWith("thread_title_") }
			//			def try2 = gHTML.body.find { it['@id'] =~ /thread_title_/ }
			//def try3 = gHTML.body.find { it['@id'].name.startsWith("thread_title_") }
			//def try5 = gHTML.body.findAll { it.name() == 'a' && it.@id.startsWith("thread_title_") }
			def try6 = gHTML.'**'.findAll {	it.attributes().id?.startsWith("thread_title_") }

			def max = 0

			def stillNew = true;

			stillNew = false;
			try6.eachWithIndex { row, i ->
				def threadid = (row.attributes().id =~ '.*?_(\\d+)$')[0][1] as Integer
				if (threadid > max) { stillNew = true; max = threadid }
				threadmap[threadid] = row.text()
			}

			int seen = 0;
			def threads = []
			threadmap.eachWithIndex { row, i ->
				if (++seen > 10) { return false; }

				def threadid = row.getKey()
				def threadText = getSummary(threadid)
				def threadLink = getThreadLink(threadid)
				def entry = [title:row.getValue(), htmlLocation:threadLink, summary:threadText, id:threadid ]
				threads.add(entry)

			}
			ret += generateForEntries(forum, threads)

		}

		return ret;
	}

	static String getThreadLink(int threadid) {
		return "http://www.advrider.com/forums/showthread.php?t=${threadid}"
	}

	static String getOnePageThreadLink(int threadid) {
		return getThreadLink(threadid) + "&pp=1"
	}


	static String getSummary(int threadid) {
		def thread = new URL(getThreadLink(threadid))
		def slurper = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())

		def textSnippet = thread.withReader { tReader ->
			def gHTML = slurper.parse(tReader)
			//def post = gHTML.'**'.find { it.attributes().id?.startsWith("post_message_") }.toString().trim().substring(0,100)
			def snippet = gHTML.'**'.find { it.attributes().id?.startsWith("post_message_") }.toString().trim()
			def snipLength = snippet.length() > 100 ? 100 : snippet.length()
			return snippet?.substring(0, snipLength)
		}


		return textSnippet
	}

	// source: http://www.thecoderscorner.com/tcc/a/groovy-and-grails/groovy-introduction/read-and-write-xml-with-groovy-includes-atom-exam/page3
	static String generateForEntries(def threadid, def entries)
	{
		// we need a simple date formatter that formats as XML.
		SimpleDateFormat sdf =
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

		// create the builder as before.
		StringWriter writer = new StringWriter();
		MarkupBuilder xml = new MarkupBuilder(writer);

		// feed is the root level. In a namespace
		xml.feed(xmlns:'http://www.w3.org/2005/Atom') {

			// add the top level information about this feed.
			title("advrider forum ${threadid}")
			subtitle("automagically generated")
			id("uri:uuid:adv-${threadid}")
			//link(href:"http://blah")
			//author { name("MyName") }
			updated sdf.format(new Date());

			// for each entry we need to create an entry element
			entries.each { item ->
				entry {
					title item.title
					link(href:item.htmlLocation);
					id "url:advrider,${threadid},${item.id}"
					//updated sdf.format(item.lastModified)
					summary item.summary;
				}
			}
		}

		// lastly give back a string representation of the xml.
		return writer.toString();
	}

}

