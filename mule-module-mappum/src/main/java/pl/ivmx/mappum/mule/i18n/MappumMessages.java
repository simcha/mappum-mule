package pl.ivmx.mappum.mule.i18n;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;

public class MappumMessages extends MessageFactory {

	private static final MappumMessages factory = new MappumMessages();

	private static final String BUNDLE_PATH = getBundlePath("mappum");

	public static Message initializationErrorMessage(String details) {
		return factory.createMessage(BUNDLE_PATH, 1, details);
	}

	public static Message missingScriptEngineErrorMessage(String language) {
		return factory.createMessage(BUNDLE_PATH, 2, language);
	}

	public static Message scriptNotLoadedErrorMessage() {
		return factory.createMessage(BUNDLE_PATH, 3);
	}

	public static Message scriptExecutionErrorMessage(String details) {
		return factory.createMessage(BUNDLE_PATH, 4, details);
	}

	public static Message scriptingEnginNotInitializedErrorMessage() {
		return factory.createMessage(BUNDLE_PATH, 5);
	}
}
