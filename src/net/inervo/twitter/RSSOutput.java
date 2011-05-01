package net.inervo.twitter;

/*
 * Copyright (c) 2011, Ted Timmons, Inervo Networks
 * All rights reserved.
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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.digester.rss.Channel;
import org.apache.commons.digester.rss.Item;

public class RSSOutput {
	Channel newChannel;

	public RSSOutput( String title, String description, String url ) {
		prepareFeed( title, description, url );
	}

	private void prepareFeed( String title, String description, String url ) {
		newChannel = new Channel();
		newChannel.setTitle( title );
		newChannel.setDescription( description );
		newChannel.setLink( url );
		newChannel.setLanguage( "en" );

		SimpleDateFormat formatter = new SimpleDateFormat( "dd MMM yyyy HH:mm:ss Z" );
		String today = formatter.format( new Date() );
		newChannel.setPubDate( today );
	}

	public void addItem( String url, String title, String description ) {
		Item item = new Item();
		item.setTitle( title );
		item.setLink( url );
		item.setDescription( description );
		newChannel.addItem( item );
	}

	public ByteArrayOutputStream getRSSasOutputStream() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		newChannel.render( baos );
		return baos;
	}

	public void write( String filename ) {
		try {
			FileOutputStream fout = new FileOutputStream( filename );
			newChannel.render( fout );
			fout.close();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
}
