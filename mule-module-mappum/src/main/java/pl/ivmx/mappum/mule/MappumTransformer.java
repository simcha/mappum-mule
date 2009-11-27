package pl.ivmx.mappum.mule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;

public class MappumTransformer extends AbstractTransformer {

  private final Log logger = LogFactory.getLog(this.getClass());
  private MappumConnector connector;
  private String fromQName;
  private String mapName;

  public void setConnector(MappumConnector connector) {
    this.connector = connector;
  }
  
  @Override
  protected Object doTransform(Object message, String arg1)
      throws TransformerException {
    logger.debug("Mappum Service Component called");
    try {
      return connector.doTransform(message, mapName, fromQName);
    } catch (NoSuchMethodException e) {
      throw new TransformerException(this, e);
    }
  }
  
  public void setFromQName(String fromQName){
    this.fromQName = fromQName;
  }
  
  public void setMapName(String mapName){
    this.mapName = mapName;
  }
}
