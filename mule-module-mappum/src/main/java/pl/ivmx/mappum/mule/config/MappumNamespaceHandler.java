package pl.ivmx.mappum.mule.config;

import org.mule.config.spring.parsers.specific.ComponentDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import pl.ivmx.mappum.mule.component.MappumComponent;

/**
 * Registers a Bean Definition Parser for handling elements defned in META-INF/mule-mappum.xsd
 *
 */
public class MappumNamespaceHandler extends NamespaceHandlerSupport
{
    public void init()
    {
        registerBeanDefinitionParser("component", new ComponentDefinitionParser(MappumComponent.class));
    }
}