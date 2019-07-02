# Deprecated

This project no longer receives maintenance.
Please use the official Liquibase library -> https://github.com/liquibase/liquibase-hibernate

# Liquibase Hibernate 5 Integration

This extension lets you use your Hibernate 5 configuration as a comparison database for diff, diffChangeLog and generateChangeLog in Liquibase.

This project is based on official [liquibase-hibernate](https://github.com/liquibase/liquibase-hibernate) for hibernate 4.3


This project is a lab for liquibase hibernate 5.x integration.

For now only supports ejb3 database.

Configurations
--------------

The hibernate ejb3 URL allows you to either reference a persistence-unit defined in your persistence.xml or specify a class name that implements liquibase/ext/hibernate/customfactory/CustomEjb3ConfigurationFactory

Supported params:
	
    hibernate.dialect
    hibernate.id.new_generator_mappings
    hibernate.implicit_naming_strategy
    hibernate.physical_naming_strategy
	
Examples:

    hibernate:ejb3:myPersistenceUnit
    hibernate:ejb3:com.example.MyConfigFactory
    hibernate:ejb3:myPersistenceUnit?hibernate.dialect=org.my.CustomDialect&amp;hibernate.id.new_generator_mappings=true&amp;hibernate.implicit_naming_strategy=com.mycompany.CustomImplicitNamingStrategy
	

Download:

```xml
<dependency>
    <groupId>com.github.jean-merelis</groupId>
    <artifactId>liquibase-hibernate5</artifactId>
    <version>1.0.0.Beta</version>
</dependency>
```
