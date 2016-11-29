package org.demoiselle.jee.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.configuration.extractor.AbstractConfigurationTest;
import org.demoiselle.jee.configuration.extractor.ConfigurationStringValueExtractorAmbiguosTest;
import org.demoiselle.jee.configuration.message.ConfigurationMessage;
import org.demoiselle.jee.configuration.model.ConfigIncompatibleTypeModel;
import org.demoiselle.jee.configuration.model.ConfigModel;
import org.demoiselle.jee.configuration.model.ConfigWithNameAnnotationEmptyModel;
import org.demoiselle.jee.configuration.model.ConfigWithValidationModel;
import org.demoiselle.jee.configuration.model.ConfigWithoutExtractorModel;
import org.demoiselle.jee.configuration.util.UtilTest;
import org.demoiselle.jee.core.annotation.Priority;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class ConfigurationLoaderTest extends AbstractConfigurationTest{
	
	@Inject
	private ConfigurationLoader configLoader;
	
	@Inject 
	private ConfigurationMessage message;
	
	private ConfigModel configModel = new ConfigModel();
	
	@Before
	public void setUp() throws IOException, Exception{
		Locale.setDefault(new Locale("pt", "BR"));
		makeConfigurationRuntime(ConfigModel.class, ConfigType.PROPERTIES, utilTest.createPropertiesFile("test"));
	}
	
	@Test
	public void shouldPopulateObject() throws IOException{
		
		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigString());
		configLoader.load(configModel, baseClass, false);
		
		assertNotNull(configModel.getConfigString());
		
		assertEquals(UtilTest.CONFIG_STRING_VALUE, configModel.getConfigString());
		assertEquals(UtilTest.CONFIG_INTEGER_VALUE, configModel.getConfigInteger());
		assertEquals(UtilTest.CONFIG_SHORT_VALUE, configModel.getConfigShort());
		assertEquals(UtilTest.CONFIG_BOOLEAN_VALUE, configModel.getConfigBoolean());
		assertEquals(UtilTest.CONFIG_BYTE_VALUE, configModel.getConfigByte());
		assertEquals(UtilTest.CONFIG_CHARACTER_VALUE, configModel.getConfigCharacter());
		assertEquals(UtilTest.CONFIG_LONG_VALUE, configModel.getConfigLong());
		assertEquals(UtilTest.CONFIG_DOUBLE_VALUE, configModel.getConfigDouble());
		assertEquals(UtilTest.CONFIG_FLOAT_VALUE, configModel.getConfigFloat());
		assertEquals(UtilTest.CONFIG_INT_VALUE, configModel.getConfigInt());
		assertEquals(UtilTest.CONFIG_ENUM_VALUE, configModel.getConfigEnum());
		
		String keyIp = UtilTest.CONFIG_MAP_FIELD_IP.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
		String keyPort = UtilTest.CONFIG_MAP_FIELD_PORT.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
		String keyProtocol = UtilTest.CONFIG_MAP_FIELD_PROTOCOL.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
		
		
		assertEquals(UtilTest.CONFIG_MAP_VALUE_IP, configModel.getConfigMap().get(keyIp));
		assertEquals(UtilTest.CONFIG_MAP_VALUE_PROTOCOL, configModel.getConfigMap().get(keyProtocol));
		assertEquals(UtilTest.CONFIG_MAP_VALUE_PORT, configModel.getConfigMap().get(keyPort));
		
	}
	
	@Test
	public void shouldPopulateObjectWithNameAnnotation(){
		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigString());
		configLoader.load(configModel, baseClass, false);
		
		assertNotNull(configModel.getConfigStringWithName());
		assertEquals(UtilTest.CONFIG_STRING_NAME_ANNOTATION_VALUE, configModel.getConfigStringWithName());
	}
	
	@Test(expected = ConfigurationException.class)
	public void objectWithNoMatchExtractorShouldThrowException(){
		
		ConfigWithoutExtractorModel model = new ConfigWithoutExtractorModel();
		
		Class<?> baseClass = model.getClass();
		
		configLoader.load(model, baseClass, false);
	}
	
	@Test(expected = ConfigurationException.class)
	public void objectWithIncompatibleTypeShoutThrowConfigurationException() throws IOException, Exception{
		
		Properties properties = new Properties();
		properties.put("configBooleanIncompatible", "7");
		
		makeConfigurationRuntime(ConfigIncompatibleTypeModel.class, ConfigType.PROPERTIES, utilTest.createPropertiesFile("test", properties));
		
		ConfigIncompatibleTypeModel model = new ConfigIncompatibleTypeModel();
		
		Class<?> baseClass = model.getClass();
		
		configLoader.load(model, baseClass, false);
		
		assertNull(model.getConfigBooleanIncompatible());
	}
	
	@Test
	public void fieldWithIgnoreAnnotationShouldntLoad(){
		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigFieldWithIgnore());
		configLoader.load(configModel, baseClass, true);
		assertNull(configModel.getConfigFieldWithIgnore());		
	}
	
	@Test(expected = ConfigurationException.class)
	public void modelInvalidValuesWithBeanValidationShouldThrowConfigurationException(){
		ConfigWithValidationModel model = new ConfigWithValidationModel();
		Class<?> baseClass = model.getClass();
		configLoader.load(model, baseClass, true);		
	}
	
	@Test
	public void modelInvalidValueWithBeanValidationShouldShowError() throws NoSuchFieldException, IllegalAccessException{		
		
		preparePriorityForValueExtractor(Priority.L1_PRIORITY);
		
		StringBuilder sb = new StringBuilder();
		sb.append("private java.lang.String org.demoiselle.jee.configuration.model.ConfigWithValidationModel.configString não pode ser nulo\n");		
		
		try{
			ConfigWithValidationModel model = new ConfigWithValidationModel();
			Class<?> baseClass = model.getClass();
			configLoader.load(model, baseClass, true);
		}
		catch(ConfigurationException e){
			assertNotNull(e.getMessage());
			assertEquals(sb.toString(), e.getMessage());
		}
		
	}
	
	@Test
	public void twoExtractorValueThatSupportTheSameTypeShouldThrowConfigurationException() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		
		//Same Priority of ConfigurationStringValueExtractor
		preparePriorityForValueExtractor(Priority.L2_PRIORITY);
		
		try{
			Class<?> baseClass = configModel.getClass();
			assertNull(configModel.getConfigString());
			configLoader.load(configModel, baseClass, true);
		}
		catch(ConfigurationException e){
			assertNotNull(e.getMessage());
			assertThat(e.getMessage(), CoreMatchers.containsString("Foi detectada ambiguidade da interface org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor com as seguintes implementações:"));
			assertThat(e.getMessage(), CoreMatchers.containsString("Para resolver o conflito, defina explicitamente a implementação no demoiselle.properties."));
		}
	}

	@Test
	public void configurationWithInvalidResourceShouldThrowConfigurationException() throws IOException, Exception{
		
		makeConfigurationRuntime(ConfigModel.class, ConfigType.PROPERTIES, "file-not-found");
		
		Class<?> baseClass = configModel.getClass();
		
		try{
			configLoader.load(configModel, baseClass, false);
		}
		catch(ConfigurationException e){
			assertNotNull(e.getMessage());
			assertEquals(e.getMessage(), "O arquivo file-not-found.properties não foi encontrado");
		}
		
	}
	
	@Test
	public void modelWithEmptyNameAnnotationShouldThrowException(){
		ConfigWithNameAnnotationEmptyModel model = new ConfigWithNameAnnotationEmptyModel();
		Class<?> baseClass = model.getClass();
		
		try{
			configLoader.load(model, baseClass, true);
		}
		catch(ConfigurationException e){
			assertNotNull(e.getMessage());
			assertEquals(e.getMessage(), message.configurationNameAttributeCantBeEmpty());
		}
	}
	
	@Test
	public void configurationLoaderShouldLoadConfigurationXML() throws FileNotFoundException, IOException, Exception{
		makeConfigurationRuntime(ConfigModel.class, ConfigType.XML, utilTest.createXMLFile("test"));
		
		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigString());
		configLoader.load(configModel, baseClass, false);
		
		assertNotNull(configModel.getConfigString());
		assertEquals(UtilTest.CONFIG_STRING_VALUE, configModel.getConfigString());
	}
	
	@Test
	public void configurationLoaderShouldLoadConfigurationSystem() throws FileNotFoundException, IOException, Exception{
		utilTest.createSystemVariables();
		makeConfigurationRuntime(ConfigModel.class, ConfigType.SYSTEM, null);
		
		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigString());
		configLoader.load(configModel, baseClass, false);
		
		assertNotNull(configModel.getConfigString());
		assertEquals(UtilTest.CONFIG_STRING_VALUE, configModel.getConfigString());
	}
	
	private void preparePriorityForValueExtractor(int level) throws NoSuchFieldException, IllegalAccessException {

		final Priority oldPriority = ConfigurationStringValueExtractorAmbiguosTest.class.getDeclaredAnnotation(Priority.class);
		
		Priority priority = new Priority() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return oldPriority.annotationType();
			}
			
			@Override
			public int value() {				
				return level;
			}
		};
		
		Field annotationDataField = Class.class.getDeclaredField("annotationData");
		annotationDataField.setAccessible(true);
		
		Object object = annotationDataField.get(ConfigurationStringValueExtractorAmbiguosTest.class);
		
		Field field = object.getClass().getDeclaredField("annotations");
		field.setAccessible(true);
		
		@SuppressWarnings("unchecked")
		Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) field.get(object);
		
		annotations.put(Priority.class, priority);
	}
	
	private void makeConfigurationRuntime(Class<?> clazz, ConfigType configType, String pathFileTest) throws Exception{
		final Configuration oldConfiguration = clazz.getDeclaredAnnotation(Configuration.class);
		
		Configuration configuration = new Configuration() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return oldConfiguration.annotationType();
			}
			
			@Override
			public ConfigType type() { 
				return configType;
			}
			
			@Override
			public String resource() {
				
				if(configType.equals(ConfigType.SYSTEM)){
					return null;
				}
				
				String replacePattern = ".properties";
				
				if(configType.equals(ConfigType.XML)){
					replacePattern = ".xml";
				}
				
				return new File(pathFileTest).getName().replaceAll(replacePattern, "");
			}
			
			@Override
			public String prefix() {
 				return "";
			}
		};
		
		Field annotationDataField = Class.class.getDeclaredField("annotationData");
		annotationDataField.setAccessible(true);
		
		Object object = annotationDataField.get(clazz);
		
		Field field = object.getClass().getDeclaredField("annotations");
		field.setAccessible(true);
		
		@SuppressWarnings("unchecked")
		Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) field.get(object);
		
		annotations.put(Configuration.class, configuration);
		
		URLClassLoader ucl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		method.setAccessible(true);
		
		method.invoke(ucl, utilTest.getDirectoryTemp().toUri().toURL());
		
	}

}