package pl.ivmx.mappum.mule.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.NullPayload;

public class MappumComponentTestCase extends FunctionalTestCase {

	@Override
	protected String getConfigResources() {
		return "mappum-config.xml";
	}

	public void testPersonToClient() throws Exception {

		String inputMessage = getExample("data/person.xml");

		MuleClient client = new MuleClient();
		MuleMessage result = client.send("vm://mappum", inputMessage, null);

		assertNotNull(result);
		assertNull(result.getExceptionPayload());
		assertFalse(result.getPayload() instanceof NullPayload);

		assertTrue("'person' not converted to 'client'", result
				.getPayloadAsString().contains("<client>"));
	}

	public void testClientToPerson() throws Exception {

		String inputMessage = getExample("data/client.xml");

		MuleClient client = new MuleClient();
		MuleMessage result = client.send("vm://mappum", inputMessage, null);

		assertNotNull(result);
		assertNull(result.getExceptionPayload());
		assertFalse(result.getPayload() instanceof NullPayload);

		assertTrue("'client' not converted to 'person'", result
				.getPayloadAsString().contains("<person>"));
	}

	public void testNewAddressToAdresseWithMapName() throws Exception {

		String inputMessage = getExample("data/address2.xml");

		MuleClient client = new MuleClient();
		Map<String, String> messageProperties = new HashMap<String, String>();
		messageProperties.put("mapName", "address");
		MuleMessage result = client.send("vm://mappum", inputMessage,
				messageProperties);

		assertNotNull(result);
		assertNull(result.getExceptionPayload());
		assertFalse(result.getPayload() instanceof NullPayload);

		assertTrue("'new_address' not converted to 'adresse'", result
				.getPayloadAsString().contains("<adresse>"));
	}

	public void testNewAddressToAdresseWithoutMapName() throws Exception {

		String inputMessage = getExample("data/address2.xml");

		MuleClient client = new MuleClient();
		MuleMessage result = client.send("vm://mappum", inputMessage, null);

		assertNotNull(result);
		assertNotNull(result.getExceptionPayload());
		assertTrue(result.getPayload() instanceof NullPayload);

		assertTrue("'new_address' not converted to 'adresse'", result
				.getExceptionPayload().getRootException().getMessage().matches(
						".*Map .*not found.*"));
	}

	public void testAddressToAdresseWithoutMapName() throws Exception {

		String inputMessage = getExample("data/address.xml");

		MuleClient client = new MuleClient();
		MuleMessage result = client.send("vm://mappum", inputMessage, null);

		assertNotNull(result);
		assertNull(result.getExceptionPayload());
		assertFalse(result.getPayload() instanceof NullPayload);

		assertTrue("'address' not converted to 'adresse'", result
				.getPayloadAsString().contains("<adresse>"));
	}

	public void testHttpPersonToClient() throws Exception {
		try {

			// create XML file in the input directory
			InputStream is = this.getClass().getClassLoader()
					.getResourceAsStream("data/person.xml");
			assertNotNull("Missing input file 'data/person.xml'", is);

			String url = "http://localhost:8888/services/Mappum/";

			HttpClient client = new HttpClient();

			PostMethod method = new PostMethod(url);

			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));

			method.setRequestBody(is);

			String retVal = null;

			try {
				// Execute the method.
				int statusCode = client.executeMethod(method);

				assertTrue("Method failed: " + method.getStatusLine(),
						statusCode == HttpStatus.SC_OK);

				// Read the response body.
				retVal = new String(method.getResponseBodyAsString());

			} catch (HttpException e) {
				fail("Fatal protocol violation: " + e.getMessage());
				logger.error("Fatal protocol violation: " + e.getMessage(), e);
			} catch (IOException e) {
				fail("Fatal transport error: " + e.getMessage());
				logger.error("Fatal transport error: " + e.getMessage(), e);
			} finally {
				// Release the connection.
				method.releaseConnection();
			}

			assertNotNull("No value returned from web service", retVal);
			assertTrue("Mappum not working - xml not translated", retVal
					.contains("<client>"));
		} catch (Exception e) {
			logger.error("Error when calling web service", e);
			fail("Error when calling web service");
		}
	}

	public void testHttpAddressToAdreseWithoutHeader() throws Exception {
		try {

			// create XML file in the input directory
			InputStream is = this.getClass().getClassLoader()
					.getResourceAsStream("data/address2.xml");
			assertNotNull("Missing input file 'data/address2.xml'", is);

			String url = "http://localhost:8888/services/Mappum/";

			HttpClient client = new HttpClient();

			PostMethod method = new PostMethod(url);

			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));

			method.setRequestBody(is);

			String retVal = null;

			try {
				// Execute the method.
				int statusCode = client.executeMethod(method);

				assertTrue("Method not failed: " + method.getStatusLine(),
						statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR);

				// Read the response body.
				retVal = new String(method.getResponseBodyAsString());

			} catch (HttpException e) {
				fail("Fatal protocol violation: " + e.getMessage());
				logger.error("Fatal protocol violation: " + e.getMessage(), e);
			} catch (IOException e) {
				fail("Fatal transport error: " + e.getMessage());
				logger.error("Fatal transport error: " + e.getMessage(), e);
			} finally {
				// Release the connection.
				method.releaseConnection();
			}
		} catch (Exception e) {
			logger.error("Error when calling web service", e);
			fail("Error when calling web service");
		}
	}

	public void testHttpAddressToAdreseWithoutHeaderWithMessagePropertiesTransformer()
			throws Exception {
		try {

			// create XML file in the input directory
			InputStream is = this.getClass().getClassLoader()
					.getResourceAsStream("data/address2.xml");
			assertNotNull("Missing input file 'data/address2.xml'", is);

			String url = "http://localhost:8899/services/Mappum/Address/";

			HttpClient client = new HttpClient();

			PostMethod method = new PostMethod(url);

			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));

			method.setRequestBody(is);

			String retVal = null;

			try {
				// Execute the method.
				int statusCode = client.executeMethod(method);

				assertTrue("Method not failed: " + method.getStatusLine(),
						statusCode == HttpStatus.SC_OK);

				// Read the response body.
				retVal = new String(method.getResponseBodyAsString());

			} catch (HttpException e) {
				fail("Fatal protocol violation: " + e.getMessage());
				logger.error("Fatal protocol violation: " + e.getMessage(), e);
			} catch (IOException e) {
				fail("Fatal transport error: " + e.getMessage());
				logger.error("Fatal transport error: " + e.getMessage(), e);
			} finally {
				// Release the connection.
				method.releaseConnection();
			}

			assertNotNull("No value returned from web service", retVal);
			assertTrue("Mappum not working - xml not translated", retVal
					.contains("<adresse>"));
		} catch (Exception e) {
			logger.error("Error when calling web service", e);
			fail("Error when calling web service");
		}
	}

	public void testHttpAddressToAdreseWithHeaderWithoutMessagePropertiesTransformer()
			throws Exception {
		try {

			// create XML file in the input directory
			InputStream is = this.getClass().getClassLoader()
					.getResourceAsStream("data/address2.xml");
			assertNotNull("Missing input file 'data/address2.xml'", is);

			String url = "http://localhost:8888/services/Mappum/";

			HttpClient client = new HttpClient();

			PostMethod method = new PostMethod(url);

			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler(3, false));

			method.setRequestHeader("mapName", "address");
			method.setRequestBody(is);

			String retVal = null;

			try {
				// Execute the method.
				int statusCode = client.executeMethod(method);

				assertTrue("Method not failed: " + method.getStatusLine(),
						statusCode == HttpStatus.SC_OK);

				// Read the response body.
				retVal = new String(method.getResponseBodyAsString());

			} catch (HttpException e) {
				fail("Fatal protocol violation: " + e.getMessage());
				logger.error("Fatal protocol violation: " + e.getMessage(), e);
			} catch (IOException e) {
				fail("Fatal transport error: " + e.getMessage());
				logger.error("Fatal transport error: " + e.getMessage(), e);
			} finally {
				// Release the connection.
				method.releaseConnection();
			}

			assertNotNull("No value returned from web service", retVal);
			assertTrue("Mappum not working - xml not translated", retVal
					.contains("<adresse>"));
		} catch (Exception e) {
			logger.error("Error when calling web service", e);
			fail("Error when calling web service");
		}
	}

	private String getExample(String file) throws Exception {

		InputStream is = this.getClass().getClassLoader().getResourceAsStream(
				file);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringWriter sw = new StringWriter();
		String line = br.readLine();
		while (line != null) {
			sw.write(line);
			sw.write("\n");
			line = br.readLine();
		}
		br.close();
		sw.flush();
		StringBuffer sb = sw.getBuffer();
		sw.close();

		return sb.toString();
	}
}
