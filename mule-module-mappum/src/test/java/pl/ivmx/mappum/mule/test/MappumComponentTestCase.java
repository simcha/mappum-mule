package pl.ivmx.mappum.mule.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.transport.NullPayload;

public class MappumComponentTestCase extends FunctionalTestCase {

	protected String getConfigResources() {
		return "mappum-functional-test-config.xml";
	}

	public void testPersonToClient() throws Exception {

		String inputMessage = getExample("data/person.xml");

		MuleClient client = new MuleClient();
		MuleMessage result = client.send("vm://in", inputMessage, null);

		assertNotNull(result);
		assertNull(result.getExceptionPayload());
		assertFalse(result.getPayload() instanceof NullPayload);

		assertTrue("'person' not converted to 'client'", result
				.getPayloadAsString().contains("<client>"));
	}

	public void testClientToPerson() throws Exception {

		String inputMessage = getExample("data/client.xml");

		MuleClient client = new MuleClient();
		MuleMessage result = client.send("vm://in", inputMessage, null);

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
		MuleMessage result = client.send("vm://in", inputMessage,
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
		MuleMessage result = client.send("vm://in", inputMessage, null);

		assertNotNull(result);
		assertNotNull(result.getExceptionPayload());
		assertTrue(result.getPayload() instanceof NullPayload);

		assertTrue("'new_address' not converted to 'adresse'", result
				.getExceptionPayload().getRootException().getMessage()
				.matches(".*Map .*not found.*"));
	}

	public void testAddressToAdresseWithoutMapName() throws Exception {

		String inputMessage = getExample("data/address.xml");

		MuleClient client = new MuleClient();
		MuleMessage result = client.send("vm://in", inputMessage, null);

		assertNotNull(result);
		assertNull(result.getExceptionPayload());
		assertFalse(result.getPayload() instanceof NullPayload);

		assertTrue("'address' not converted to 'adresse'", result
				.getPayloadAsString().contains("<adresse>"));
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
