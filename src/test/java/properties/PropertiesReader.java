package properties;


import org.aeonbits.owner.Config;


@Config.Sources("classpath:testing.properties")
public interface PropertiesReader extends Config {

    @Key("baseUrl") String baseUrl();
    @Key("waitValue") Integer waitValue();
}