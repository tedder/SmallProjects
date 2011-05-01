package net.inervo.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.ListDomainsRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;

public class Keystore {

	private static final String DEFAULT_DATA_DOMAIN = "generic";
	AmazonSimpleDB sdb = null;
	String itemKey = null;

	public Keystore( String itemKey ) throws IOException {
		sdb = new AmazonSimpleDBClient( new PropertiesCredentials( new File( "AwsCredentials.properties" ) ) );
		createDataDomainIfNecessary( DEFAULT_DATA_DOMAIN );
		this.itemKey = itemKey;
	}

	public static void main( String[] args ) {
		Keystore k = null;
		try {
			k = new Keystore( "test" );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		print( "funt: " + k.getKey( "Funtasticus update time" ) );

	}

	public void replace( String key, String value ) {
		put( key, value, true );
	}

	public void append( String key, String value ) {
		put( key, value, false );
	}

	public String getKey( String key ) {
		// List<Attribute> result = sdb.getAttributes( new GetAttributesRequest( DEFAULT_DATA_DOMAIN, itemKey )
		// ).getAttributes();
		List<Attribute> result = sdb.getAttributes( new GetAttributesRequest( DEFAULT_DATA_DOMAIN, itemKey ).withAttributeNames( key ) ).getAttributes();
		if ( result.size() == 0 ) {
			return null;
		}

		return result.get( 0 ).getValue();
	}

	public String getKey( String key, String defaultValue ) {
		String ret = getKey( key );
		return ret == null ? defaultValue : ret;
	}

	// public String select( String key ) {
	// StringBuilder selectString = new StringBuilder();
	// String select = "SELECT * FROM %s WHERE "
	// selectString.append("SELECT * FROM ");
	// selectString.append(DEFAULT_DATA_DOMAIN);
	// selectString.append("")
	// sdb.select( new SelectRequest( "").withNextToken(key) )
	// }

	public void put( String key, String value, boolean replace ) {
		List<ReplaceableAttribute> values = new ArrayList<ReplaceableAttribute>();
		values.add( new ReplaceableAttribute().withReplace( replace ).withName( key ).withValue( value ) );

		sdb.putAttributes( new PutAttributesRequest( DEFAULT_DATA_DOMAIN, itemKey, values ) );
	}

	private void createDataDomainIfNecessary( String domain ) {
		if ( dataDomainExists( domain ) == false ) {
			sdb.createDomain( new CreateDomainRequest( domain ) );
		}
	}

	private boolean dataDomainExists( String domain ) {
		boolean exists = false;

		ListDomainsRequest dr = new ListDomainsRequest().withNextToken( domain ).withMaxNumberOfDomains( 1 );
		for ( String domainName : sdb.listDomains( dr ).getDomainNames() ) {
			if ( domainName.equals( domain ) ) {
				exists = true;
				break;
			}
		}

		return exists;
	}

	private static void print( String s ) {
		System.out.println( s );
	}
}
