package pl.ivmx.mappum.mule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.MuleEvent;
import org.mule.component.AbstractComponent;

public class MappumComponent extends AbstractComponent {

	private final Log logger = LogFactory.getLog(this.getClass());
  private MappumConnector connector;

	@Override
	protected Object doInvoke(MuleEvent event) throws Exception {
	  logger.debug("Mappum Service Component called");
	  return connector.doInvoke(event);
	}

	public void setConnector(MappumConnector connector) {
		this.connector = connector;
	}
}
