package net.inervo.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpleemail.AWSJavaMailTransport;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.ListVerifiedEmailAddressesResult;
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest;

public class AmazonEmail {
	private static final String FROM = "tedderbot@perljam.net";

	PropertiesCredentials credentials = null;
	AmazonSimpleEmailService email = null;
	Session session = null;
	Transport transport = null;

	public AmazonEmail() throws FileNotFoundException, IllegalArgumentException, IOException, MessagingException {
		this( new File( "AwsCredentials.properties" ) );
	}

	public AmazonEmail( File propFile ) throws FileNotFoundException, IllegalArgumentException, IOException, MessagingException {
		credentials = new PropertiesCredentials( propFile );
		email = new AmazonSimpleEmailServiceClient( credentials );

		init();
	}

	private void init() throws MessagingException {
		Properties props = new Properties();
		props.setProperty( "mail.transport.protocol", "aws" );

		props.setProperty( "mail.aws.user", credentials.getAWSAccessKeyId() );
		props.setProperty( "mail.aws.password", credentials.getAWSSecretKey() );

		session = Session.getInstance( props );

		transport = new AWSJavaMailTransport( session, null );
		transport.connect();
	}

	public void verifyEmail( String address ) {
		ListVerifiedEmailAddressesResult verifiedEmails = email.listVerifiedEmailAddresses();
		if ( !verifiedEmails.getVerifiedEmailAddresses().contains( address ) ) {
			email.verifyEmailAddress( new VerifyEmailAddressRequest().withEmailAddress( address ) );
			System.out.println( "Please check the email address " + address + " to verify it" );
			System.exit( 0 );
		}
	}

	public void sendEmail( String to, String subject, String body ) throws AddressException, MessagingException {
		Message msg = new MimeMessage( session );
		msg.setFrom( new InternetAddress( FROM ) );
		msg.addRecipient( Message.RecipientType.TO, new InternetAddress( to ) );
		msg.setSubject( subject );
		msg.setText( body );
		msg.saveChanges();

		transport.sendMessage( msg, null );
	}

	public void sendTestEmail() {
		String TO = "ted@perljam.net";
		String BODY = "Hello World!";
		String SUBJECT = "Hello World!";

		Properties props = new Properties();
		props.setProperty( "mail.transport.protocol", "aws" );

		props.setProperty( "mail.aws.user", credentials.getAWSAccessKeyId() );
		props.setProperty( "mail.aws.password", credentials.getAWSSecretKey() );

		Session session = Session.getInstance( props );

		try {
			// Create a new Message
			Message msg = new MimeMessage( session );
			msg.setFrom( new InternetAddress( FROM ) );
			msg.addRecipient( Message.RecipientType.TO, new InternetAddress( TO ) );
			msg.setSubject( SUBJECT );
			msg.setText( BODY );
			msg.saveChanges();

			transport.sendMessage( msg, null );

		} catch ( AddressException e ) {
			e.printStackTrace();
			System.out.println( "Caught an AddressException, which means one or more of your " + "addresses are improperly formatted." );
		} catch ( MessagingException e ) {
			e.printStackTrace();
			System.out.println( "Caught a MessagingException, which means that there was a "
					+ "problem sending your message to Amazon's E-mail Service check the " + "stack trace for more information." );
		}
	}

	public static void main( String[] args ) throws Exception {
		AmazonEmail e = new AmazonEmail();
		e.verifyEmail( FROM );
		// e.verifyEmail( TO );
		e.sendTestEmail();

	}
}
