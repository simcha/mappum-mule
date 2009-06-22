package pl.ivmx.mappum.mule.component;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jruby.exceptions.RaiseException;
import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.component.AbstractComponent;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import pl.ivmx.mappum.mule.i18n.MappumMessages;

public class MappumComponent extends AbstractComponent {

	private final Log logger = LogFactory.getLog(this.getClass());

	public static final String LANGUAGE_RUBY = "jruby";

	public static final String DEFAULT_MAP_FOLDER = "map";
	public static final String DEFAULT_SCHEMA_FOLDER = "schema";
	public static final String DEFAULT_GENERATED_CLASSES_FOLDER = "generated_classes";

	public static final String MAP_NAME_PROPERTY_NAME = "mapName";

	private String mapFolder = DEFAULT_MAP_FOLDER;
	private String schemaFolder = DEFAULT_SCHEMA_FOLDER;
	private String generatedClassesFolder = DEFAULT_GENERATED_CLASSES_FOLDER;

	private ScriptEngine engine;
	private ScriptEngineManager manager;
	private String language = LANGUAGE_RUBY;

	private String xsd2rubyScriptCode;
	private Resource xsd2rubyScript;

	@Override
	protected void doInitialise() throws InitialisationException {
		super.doInitialise();
		logger.info("Mappum Service Component initialising...");

		try {
			setFullPaths();
		} catch (Exception e) {
			logger.fatal(e);
			throw new InitialisationException(MappumMessages
					.initializationErrorMessage(e.getLocalizedMessage()), this);
		}

		logger.info("\nMappumComponent information:\n\tmap folder = "
				+ getMapFolder() + "\n\tschema folder = " + getSchemaFolder()
				+ "\n\tgenerated classes folder = "
				+ getGeneratedClassesFolder());

		try {
			manager = new ScriptEngineManager();
			engine = manager.getEngineByName(language);
		} catch (Exception e) {
			logger.fatal(e);
			throw new InitialisationException(MappumMessages
					.initializationErrorMessage(e.getLocalizedMessage()), this);
		}

		if (engine == null) {
			logger.fatal(MappumMessages
					.missingScriptEngineErrorMessage(language));
			throw new InitialisationException(MappumMessages
					.missingScriptEngineErrorMessage(language), this);
		}

		generateAndRequire();

		logger.info("Mappum Service Component initialized succesfully.");
	}

	@Override
	protected Object doInvoke(MuleEvent event) throws Exception {
		logger.debug("Mappum Service Component called");

		String outMessage = null;

		if (engine != null) {

			Object inMessage = (String) event.transformMessageToString();

			logger.debug("mapName used : "
					+ event.getProperty(MAP_NAME_PROPERTY_NAME));

			String mapName = (String) event.getProperty(MAP_NAME_PROPERTY_NAME);

			String mappumRubyScript = "def mappum(xml,map)\n"
					+ "rt = Mappum::XmlTransform.new()\n"
					+ "content = rt.transform(xml,map)\n" + "end";

			try {
				logger.debug("IN MESSAGE:\n" + inMessage);

				engine.eval(mappumRubyScript);
				Invocable invEngine = (Invocable) engine;
				outMessage = (String) invEngine.invokeFunction("mappum",
						inMessage, mapName);

				logger.debug("OUT MESSAGE:\n" + outMessage);
			} catch (ScriptException e) {
				RaiseException re = (RaiseException) e.getCause();
				logger.error(re.getException());
				throw re;
			}

		} else {
			throw new MessagingException(MappumMessages
					.scriptingEnginNotInitializedErrorMessage(), event
					.transformMessage());
		}

		return outMessage;
	}

	public String getMapFolder() {
		return mapFolder;
	}

	public void setMapFolder(String mapFolder) {
		this.mapFolder = mapFolder;
	}

	public String getSchemaFolder() {
		return schemaFolder;
	}

	public void setSchemaFolder(String schemaFolder) {
		this.schemaFolder = schemaFolder;
	}

	public String getGeneratedClassesFolder() {
		return generatedClassesFolder;
	}

	public void setGeneratedClassesFolder(String generatedClassesFolder) {
		this.generatedClassesFolder = generatedClassesFolder;
	}

	private void setFullPaths() throws Exception {

		if (getMapFolder() == null || "".equals(getMapFolder().trim())) {
			setMapFolder(DEFAULT_MAP_FOLDER);
		}
		if (getSchemaFolder() == null || "".equals(getSchemaFolder().trim())) {
			setSchemaFolder(DEFAULT_SCHEMA_FOLDER);
		}
		if (getGeneratedClassesFolder() == null
				|| "".equals(getGeneratedClassesFolder().trim())) {
			setGeneratedClassesFolder(DEFAULT_GENERATED_CLASSES_FOLDER);
		}
		if (this.muleContext.getConfiguration().getWorkingDirectory() != null) {
			File generatedClassesFolderFile = new File(this.muleContext
					.getConfiguration().getWorkingDirectory()
					+ File.separator + getGeneratedClassesFolder());
			setGeneratedClassesFolder(generatedClassesFolderFile
					.getAbsolutePath());
			generatedClassesFolderFile.delete();
			generatedClassesFolderFile.mkdirs();
		}
		setMapFolder((new File(getMapFolder())).getAbsolutePath());
		setSchemaFolder((new File(getSchemaFolder())).getAbsolutePath());
	}

	private void generateAndRequire() throws InitialisationException {

		try {

			StringBuffer params = new StringBuffer();
			params.append("\n");
			params.append("require 'xml_transform'");
			params.append("\n\n");
			params.append("wl = Mappum::WorkdirLoader.new(\"");
			params.append(getSchemaFolder());
			params.append("\", \"");
			params.append(getGeneratedClassesFolder());
			params.append("\", \"");
			params.append(getMapFolder());
			params.append("\")");
			params.append("\n");
			params.append("wl.generate_and_require");
			params.append("\n");

			xsd2rubyScriptCode = params.toString();

			logger.debug(xsd2rubyScriptCode);

			xsd2rubyScript = new ByteArrayResource(xsd2rubyScriptCode
					.getBytes());

			// execute the script (non-compiled!)

			engine.eval(new InputStreamReader(xsd2rubyScript.getInputStream()));

		} catch (IOException e) {
			logger.fatal(e);
			throw new InitialisationException(MappumMessages
					.scriptNotLoadedErrorMessage(), this);
		} catch (ScriptException e) {
			RaiseException re = (RaiseException) e.getCause();
			logger.fatal(re.getException());
			throw new InitialisationException(MappumMessages
					.scriptExecutionErrorMessage(re.getException()
							.getBacktrace().toString()), this);
		}
	}
}
