package pl.ivmx.mappum.mule.config;

import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.specific.ComponentDefinitionParser;
import org.mule.config.spring.parsers.specific.TransformerDefinitionParser;
import org.mule.endpoint.URIBuilder;

import pl.ivmx.mappum.mule.MappumComponent;
import pl.ivmx.mappum.mule.MappumConnector;
import pl.ivmx.mappum.mule.MappumTransformer;

/**
 * Registers a Bean Definition Parser for handling elements defned in META-INF/mule-mappum.xsd
 *
 */
public class MappumNamespaceHandler extends AbstractMuleNamespaceHandler
{
    public void init()
    {
      registerStandardTransportEndpoints(MappumConnector.MAPPUM, URIBuilder.PATH_ATTRIBUTES);
      registerBeanDefinitionParser("component", new ComponentDefinitionParser(MappumComponent.class));
      registerConnectorDefinitionParser(MappumConnector.class);
      registerBeanDefinitionParser("transformer", new TransformerDefinitionParser(MappumTransformer.class));
    }
}