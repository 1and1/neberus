# Neberus REST Documentation

[![Build Status](https://travis-ci.org/1and1/neberus.svg?branch=master)](https://travis-ci.org/1and1/neberus)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.oneandone.neberus/neberus-doclet/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.oneandone.neberus/neberus-doclet)

JavaDoc Doclet that automatically generates REST Documentation from your code!
Out of the box compatibility with javax.ws.rs and spring-webmvc.

- [Neberus REST Documentation](#neberus-rest-documentation)
	- [Setup](#setup)
		- [Maven](#maven)
			- [Using classes from dependencies](#using-classes-from-dependencies)
	- [Viewing the Apidocs](#viewing-the-apidocs)
	- [Service Documentation](#service-documentation)
	- [Method Documentation](#method-documentation)
		- [Annotations](#annotations)
		- [Example Usage](#example-usage)
		- [Example Usage with Interface](#example-usage-with-interface)
		- [Deprecated Methods](#deprecated-methods)
	- [Usecase Documentation](#usecase-documentation)
		- [Annotations](#annotations-1)
		- [Example](#example)
	- [Extensions](#extensions)

## Setup

### Java Version Compatibility

Java 8 is only supported with version 1.x.

Java 11 is only supported with version 2.x and 3.x.

All newer versions require Java 17+. 

### Maven

- Add a dependency to `neberus-core` to get access to Neberus annotations in your code.
- Configure the maven-javadoc-plugin to use the Neberus doclet


`pom.xml`

```xml
<dependencies>
    ...
    <dependency>
        <groupId>net.oneandone.neberus</groupId>
        <artifactId>neberus-core</artifactId>
        <version>${neberus.version}</version>
    </dependency>
    ...
</dependencies>

 <build>
    <plugins>
        ...
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-javadoc-plugin</artifactId>
            <executions>
                <execution>
                    <id>generate-service-docs</id>
                    <phase>generate-resources</phase>
                    <configuration>
                        <doclet>net.oneandone.neberus.Neberus</doclet>
                        <docletArtifact>
                            <groupId>net.oneandone.neberus</groupId>
                            <artifactId>neberus-doclet</artifactId>
                            <version>${neberus.version}</version>
                        </docletArtifact>
                        <reportOutputDirectory>
                        <!-- recommended path for traditional '*.war' services -->
                        ${project.build.directory}/${project.build.finalName}
                        <!-- recommended path for spring boot services -->
                        ${project.build.directory}/classes/resources/
                        </reportOutputDirectory>
                        <useStandardDocletOptions>false</useStandardDocletOptions>
                        <additionalOptions><!-- ATTENTION: this has been renamed from 'additionalparam' with maven-javadoc-plugin version 3.0.0 -->
                            --apiVersion ${project.version}
                            --apiTitle "${project.name}" <!-- remember to put enclosing quotes when the name contains blanks -->
                            --docBasePath .
                            --apiBasePath ${apiBasePath}
                            --apiHosts http://yourhost.com <!-- the leading 'http://' can be omitted, but it must be provided for 'https://' -->
                            --scanPackages net.oneandone <!-- semicolon separated list of packages to be included -->
                        </additionalOptions>
                    </configuration>
                    <goals>
                        <goal>javadoc</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ...
    </plugins>
</build>

```

#### Doclet Options

```text
Provided by the Neberus doclet:
    --apiBasePath <path>
                  Root path of the Api on the server (e.g. '/rest').
    --apiHosts <host[description]>(;<host[description]>)*
                  List of hosts where the Api can be accessed, separated by semicolon (;). Description for each host can be provided inside optional trailing brackets. Example: "https://testserver.com[the default testserver];https://otherserver.com[the other testserver]"
    --apiTitle <title>
                  Api Title.
    --apiVersion <version>
                  Api version.
    -d <string>   outputDirectory
    --docBasePath <path>
                  Root path where the generated documentation is placed inside reportOutputDirectory.
    -ignoreErrors
                  Ignore generation errors.
    --scanPackages <package>(;<package>)*
                  List of packages that include classes relevant for the apidoc
    --markup [HTML|MARKDOWN|ASCIIDOC]
    			  Global markup option. Valid for all descriptions and used javadoc. Default: HTML.
```

#### Markup Options

Per default the normal javadoc style HTML tags are supported and will be converted.

With the `--markup` option it is possible to use Markdown (see [CommonMark](https://commonmark.org/)) or AsciiDoc (see [AsciiDoctor](https://asciidoctor.org/)) instead.

The chosen markup can also be used for the `package.html` file inside the `<body>` tag. 

#### Using classes from dependencies

If classes from dependencies are used as entities of parameters or response values of some methods, it is required to include those dependencies in the configuration of the maven-javadoc-plugin.
Otherwise it is not possible to properly generate the templates for these classes.

More information about how the inclusion works can be found [here](https://maven.apache.org/plugins/maven-javadoc-plugin/examples/aggregate-dependency-sources.html).

## Viewing the Apidocs

Neberus generates `html` files and adds some `json`, `css` and `js` files. This means that the docs can be served from many places and viewed in any browser.

The apidocs can be deployed to an external server, accessed over the repository or included within the webapp.

## Service Documentation

If a `package.html` file is found in the same package as classes/interfaces annotated with `@ApiDocumentation`, the content of this file is included on the index site.

Headings should start with `h2`, since Neberus generates a `h1` heading with the service name.

## Method Documentation

REST methods can be documented either directly on the method or in a separate interface (recommended) that is implemented by the class containing the methods.

Definitions in the REST-class will be used as fallback in case the interface does not provide sufficient Documentation.

![neberus-method-doc](docs/neberus-method-doc.png)

### Annotations

| Name                 | Description                                                                                                                                                                                                           | Target                                 |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------|
| @ApiDocumentation    | Enables Neberus documentation for a class containing REST methods.                                                                                                                                                    | Type                                   |
| @ApiCurl             | Generate an example curl.                                                                                                                                                                                             | Method                                 |
| @ApiAllowedValue     | Define the allowed values for a parameter.                                                                                                                                                                            | Method, Parameter, Field, TypeArgument |
| @ApiDescription      | If provided, the value of this will be used as description of the method instead of the javadoc comment. If defined on a class, this will be used as short description of the resource on the service overview page.  | Method, Type                           |
| @ApiHeader           | Define a response Header within a @ApiResponse.                                                                                                                                                                       | -                                      |
| @ApiHeaderDefinition | Define a Header on class level, this can be used to provided descriptions of headers that are reused many times. The header name will be used for reference.                                                          | Type                                   |
| @ApiLabel            | Defines the name of a REST class or method.                                                                                                                                                                           | Method, Type                           |
| @ApiParameter        | Defines a custom parameter for a REST method. Most likely used to document fields inside the body.                                                                                                                    | Method                                 |
| @ApiResponse         | Document a Response.                                                                                                                                                                                                  | Method                                 |
| @ApiCommonResponse   | Document a common Response on class level, the response will be added to every method inside that class. An @ApiResponse defined on the method with the same status will be favoured.                                 | Type                                   |
| @ApiEntity           | Define a response entity.                                                                                                                                                                                             | -                                      |
| @ApiRequestEntity    | Define a request entity for a specific content-type.                                                                                                                                                                  | Method                                 |
| @ApiExample          | Define a example for a specific value.                                                                                                                                                                                | -                                      |
| @ApiType             | Define the type that should be displayed instead of the actual type. This can be used to hide internal wrapper DTOs.                                                                                                  | Parameter                              |
| @ApiOptional         | Declare a parameter as optional. This will have precedence over framework declarations.                                                                                                                               | Method, Parameter, Field               |
| @ApiRequired         | Declare a parameter as required. This will have precedence over framework declarations.                                                                                                                               | Method, Parameter, Field               |
| @ApiIgnore           | Exclude a request parameter, DTO field or a whole REST method from documentation.                                                                                                                                     | Method, Parameter, Field               |
| @ApiFormParam        | Declare a @RequestParam annotated parameter as form parameter instead of query parameter.                                                                                                                             | Parameter                              |

### Example Usage

```java
/*
 * REST Class Documentation
 */
@ApiDocumentation
@RequestMapping(path = "/rootPath", name = "Super Awesome REST Service")
@ApiHeaderDefinition(name = "header1", description = "description1")
@ApiHeaderDefinition(name = "header2", description = "description2")
@ApiHeaderDefinition(name = "Predefined", description = "one description to rule them all")
@ApiCommonResponse(status = ApiStatus.INTERNAL_SERVER_ERROR, description = "internal server error defined on class")
public class RestService {

	/**
	 * ApiDescription of this awesomely awesome method defined as javadoc!
	 * 
	 * @param pathParam description of the pathParam
	 */
	@RequestMapping(method = RequestMethod.GET,
					path = "/anotherGet/{pathParam}/anotherPathParam/{anotherPathParam}/{wrappedPathParam}",
					produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiLabel("This is an awesome method")
	@ApiDescription("Description of this awesomely awesome method defined in annotation!")
	@ApiResponse(status = ApiStatus.OK, description = "success",
				 headers = {
						 @ApiHeader(name = "header2", allowedValues = {
								 @ApiAllowedValue(value = "this one value", valueHint = "only that one"),
								 @ApiAllowedValue(value = "this other value", valueHint = "or only that one")
						 }) },
				 entities = {
						 @ApiEntity(entityClass = SomeDto.class, examples = {
								 @ApiExample(title = "example response", value = "{\"string1\":\"some value example\"}"),
								 @ApiExample(title = "other example response", value = "{\"string1\":\"some other value example\"}")
						 }),
						 @ApiEntity(entityClass = SomeDtoList.class, contentType = "application/list+json", examples = {
								 @ApiExample(title = "list example response", value = "[{\"string1\":\"some value example\"}]")
						 }),
						 @ApiEntity(entityClass = SomeDto[].class, contentType = "application/array+json", examples = {
								 @ApiExample(title = "array example response", value = "[{\"string1\":\"some value example\"}]")
						 })
				 })
	@ApiResponse(status = ApiStatus.BAD_REQUEST, description = "success", headers = {
			@ApiHeader(name = "header2", allowedValues = {
					@ApiAllowedValue(value = "this second value", valueHint = "only that one"),
					@ApiAllowedValue(value = "this second other value", valueHint = "or only that one")
			})
	})
	@ApiParameter(name = "anotherQueryParam", type = ApiParameter.Type.QUERY, deprecated = true, deprecatedDescription = "use queryParam instead",
				  optional = true)
	@ApiCurl
	public String justAnotherGetMethod(@PathVariable @ApiAllowedValue("default") String pathParam,
									   @PathVariable("anotherPathParam") String anotherPathParam,
									   @PathVariable("wrappedPathParam") @ApiType(String.class) WrappedString wrappedPathParam,
									   @Deprecated @RequestParam("queryParam") String queryParam) {
	}

	public static class SomeDto {
		/**
		 * Parameter can be placed here.
		 * {@link SomeEnum} <- this will place the enum values as 'allowed values'
		 * @see SomeEnum <- this will do the same
		 */
		public String string1;
		
		/**
		 * @see SomeWrapperClass#value <- can be used to reference the javax.validation constraints of another field
         */
		public String string2;

		/**
		 * If the fields are private, the doc can be placed on the getter
		 */
		public String getString1() {}

		/**
		 * @param string1 or it can be placed on the ctor if all fields are private and no getters are found
		 *                {@link SomeEnum} <- can also be used here
		 */
		public SomeDto(String string1) {}
	}

	/**
	 * A custom interface like this can be used when the entity should be a list of SomeDto.
	 * Alternatively just use SomeDto[].class in @ApiEntity.
	 */
	public static interface SomeDtoList extends List<SomeDto> {}
}
```

### Example Usage with Interface

```java
/*
 * REST Class Documentation
 */
@Path("/rootPath")
public class RestService implements RestDoc {

	/**
	 * Internal JavaDoc not visible in the ApiDocumentation because it is overwritten in the interface
         * @param pathParam this will be visible in the apidocs
	 */
	@GET
	@Path("/anotherGet/{pathParam}/anotherPathParam/{anotherPathParam}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void justAnotherGetMethod(
		@PathParam("pathParam") String pathParam,
		@PathParam("anotherPathParam") String anotherPathParam,
		@QueryParam("queryParam") String queryParam) {
	}
}
```

```java
@ApiDocumentation
@ApiHeaderDefinition(name = "header1", description = "description1")
@ApiHeaderDefinition(name = "header2", description = "description2")
@ApiHeaderDefinition(name = "Predefined", description = "one description to rule them all")
@ApiCommonResponse(status = ApiStatus.INTERNAL_SERVER_ERROR, description = "internal server error defined on class")
public interface RestDoc {

	/**
	 * ApiDescription of this awesomely awesome method defined as javadoc!
	 *
	 * @param dto the body
	 */
	@ApiLabel("This is an awesome method")
	@ApiDescription("Description of this awesomely awesome method defined in annotation!")
	@ApiResponse(status = ApiStatus.OK, description = "success",
				 headers = {
						 @ApiHeader(name = "header2", allowedValues = {
								 @ApiAllowedValue(value = "this one value", valueHint = "only that one"),
								 @ApiAllowedValue(value = "this other value", valueHint = "or only that one")
						 }) },
				 entities = {
						 @ApiEntity(entityClass = SomeDto.class, examples = {
								 @ApiExample(title = "example response", value = "{\"string1\":\"some value example\"}"),
								 @ApiExample(title = "other example response", value = "{\"string1\":\"some other value example\"}")
						 })
				 })
	@ApiResponse(status = ApiStatus.BAD_REQUEST, description = "success", headers = {
			@ApiHeader(name = "header2", allowedValues = {
					@ApiAllowedValue(value = "this second value", valueHint = "only that one"),
					@ApiAllowedValue(value = "this second other value", valueHint = "or only that one")
			})
	})
	@ApiParameter(name = "anotherQueryParam", type = ApiParameter.Type.QUERY, deprecated = true, deprecatedDescription = "use queryParam instead",
				  optional = true)
	@ApiCurl
	void justAnotherGetMethod(@ApiAllowedValue("default") String pathParam,
							  String anotherPathParam,
							  @ApiType(String.class) WrappedString wrappedPathParam,
							  @RequestParam("queryParam") String queryParam);

}
```

### Deprecated Methods

Methods annotated with @Deprecated will be specially marked in the generated documentation and can provide links to other methods that shouls be used instead inside the @deprecated javadoc tag with @link tags.
The referenced method may be in the same class or in another documented class within the same project.

```java
/**
 * @deprecated use this one {@link #justYetAnotherPostMethod(java.lang.String, java.lang.String, net.oneandone.neberus.test.RestService.SomeDto)}
 */
@POST
@Path("/anotherPost/{pathParam}/anotherPathParam/{anotherPathParam}")
@ApiLabel("This is a POST method")
@Consumes(MediaType.APPLICATION_JSON)
@Deprecated
public void justAnotherPostMethod(@PathParam("pathParam") @ApiAllowedValue("toast") String pathParam,
                                  @PathParam("anotherPathParam") String anotherPathParam,
                                  @QueryParam("queryParam") String queryParam,
                                  SomeDto dto) {
}

@POST
@Path("/anotherPOST")
@ApiLabel("This is another POST method")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public void justYetAnotherPostMethod(@PathParam("pathParam") @ApiAllowedValue("only this one") String pathParam,
                                     @QueryParam("queryParam") String queryParam,
                                     SomeDto dto) {
}
```

## Usecase Documentation

It is also possible to write documentation for usecases that span multiple methods or use already documented methods in a non-trivial way.

For this purpose a new dedicated `class` or `interface` should be defined that only contains the usecase documentation.

The generated documentation will be appended to the index page.

![neberus-usecase-doc](docs/neberus-usecase-doc.png)

### Annotations

| Name  | Description  |  Target |
|---|---|---|
| @ApiUsecase | Defines a specific usecase of the REST service. Usecases must be defined in seperate classes or interfaces apart from the normal apidoc.<br>The javadoc of the defining class (must be placed above the annotations) will be used as introduction. | Type |
| @ApiUsecaseMethod | A specific REST method used in an usecase.<br>If the method is documented within the same service, it can be referenced by providing the restClass and name (label) of the method. In this case a link will be created and all parameters and responseValues will be cross-checked so they actually exist in the referenced method. | - |
| @ApiUsecaseParam | Parameter used by a REST method in a usecase. If the method is linked to an actual REST method, the name must exist in the linked method. Use dot-syntax for nested parameters, e.g. 'myDto.myField' | - |
| @ApiUsecaseRequestBody | Request body for a REST method in a usecase. | - |
| @ApiUsecaseResponseBody | Response body for a REST method in a usecase. | - |

### Example

```java
/**
 * This javadoc has to be located ABOVE the annotations.
 * This contains only usecases that are not trivial or require multiple calls.
 */
@ApiUsecase(name = "do it with hints", description = "just do it already",
			methods = {
					@ApiUsecaseMethod(
							path = PATH_ROOT + PATH_POST,
							httpMethod = "GET",
							description = "Call this first",
							requestBody = @ApiUsecaseRequestBody(contentType = "application/json",
																 valueHint = "the expected request body",
																 value = "{\"key\":\"value\"}")),
					@ApiUsecaseMethod(
							path = PATH_ROOT + PATH_GET,
							httpMethod = "GET",
							description = "Then call this",
							responseBody = @ApiUsecaseResponseBody(contentType = "application/json",
																   valueHint = "and some hint",
																   value = "{\"stringField\":\"with some value\"}"))
			})
@ApiUsecase(name = "do it without hints", description = "just do it already",
			methods = {
					@ApiUsecaseMethod(
							path = PATH_ROOT + PATH_GET_WITH_RESPONSES,
							httpMethod = "GET",
							description = "Call this first",
							parameters = {
									@ApiUsecaseParam(name = "stringPathParam", value = "myId"),
									@ApiUsecaseParam(name = "queryParam123", value = "not my type")
							}),
					@ApiUsecaseMethod(
							path = PATH_ROOT + PATH_GET,
							httpMethod = "GET",
							description = "Then call this",
							responseBody = @ApiUsecaseResponseBody(contentType = "application/json",
																   valueHint = "the expected request body",
																   value = "{\"stringField\":\"with some value\"}"))
			})
public class UsecaseDoc {

}
```

## Extensions

Neberus can be extended with modules and shortcodes. To include an extension it must be 
added to the `configuration` of the `maven-javadoc-plugin` as `resourcesArtifact`.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <executions>
        <execution>
            <id>generate-service-docs</id>
            <phase>generate-resources</phase>
            <configuration>
                ...
                <resourcesArtifacts>
                    <resourcesArtifact>
                        <groupId>${your-extension-groupid}</groupId>
                        <artifactId>${your-extension-artifactid}</artifactId>
                        <version>${your-extension.version}</version>
                    </resourcesArtifact>
                </resourcesArtifacts>
            </configuration>
            ...
        </execution>
    </executions>
</plugin>

```
