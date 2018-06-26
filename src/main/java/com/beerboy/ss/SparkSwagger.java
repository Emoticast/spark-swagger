package com.beerboy.ss;

import com.beerboy.spark.typify.provider.GsonProvider;
import com.beerboy.spark.typify.spec.IgnoreSpec;
import com.beerboy.ss.conf.IpResolver;
import com.beerboy.ss.conf.VersionResolver;
import com.beerboy.ss.descriptor.EndpointDescriptor;
import com.beerboy.ss.model.Contact;
import com.beerboy.ss.model.ExternalDocs;
import com.beerboy.ss.model.Info;
import com.beerboy.ss.model.License;
import com.beerboy.ss.rest.Endpoint;
import com.beerboy.ss.rest.EndpointResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import spark.ExceptionHandler;
import spark.Filter;
import spark.HaltException;
import spark.Service;

/**
 * @author manusant
 */
public class SparkSwagger {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparkSwagger.class);

    public static final String CONF_FILE_NAME = "spark-swagger.conf";
    private String apiPath;
    private Swagger swagger;
    private Service spark;
    private Config config;
    private String version;

    private SparkSwagger(final Service spark, final String version, final Config config) {
        this.spark = spark;
        this.version = version;
        this.swagger = new Swagger();
        this.config = config;
        this.apiPath = this.config.getBasePath();
        this.swagger.setBasePath(this.apiPath);
        this.swagger.setExternalDocs(ExternalDocs.newBuilder().build());
        this.swagger.setHost(getHost());
        this.swagger.setInfo(getInfo());
        configDocRoute();
    }

    private void configDocRoute() {
        // Configure static mapping
        String basePath = config.getServiceName() + config.getDocPath();
        String baseUiFolder = SwaggerHammer.getUiFolder(basePath);
        String uiFolder = baseUiFolder.replaceAll(basePath, "");
        SwaggerHammer.createDir(SwaggerHammer.getSwaggerUiFolder());
        SwaggerHammer.createDir(uiFolder);
        spark.externalStaticFileLocation(uiFolder);
        LOGGER.debug("Spark-Swagger: UI folder deployed at " + uiFolder);

        // Enable CORS
        spark.options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }
                    return "OK";
                });

        spark.before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
        LOGGER.debug("Spark-Swagger: CORS enabled and allow Origin *");
    }

    public String getApiPath() {
        return apiPath;
    }

    public String getVersion() {
        return version;
    }

    public Service getSpark() {
        return spark;
    }

//    public static SparkSwagger of(final Service spark) {
//        return new SparkSwagger(spark, null, null);
//    }

    public static SparkSwagger of(final Service spark, final Config config) {
        return new SparkSwagger(spark, null, config);
    }

    public SparkSwagger version(final String version) {
        this.version = version;
        return this;
    }

    public SparkSwagger ignores(Supplier<IgnoreSpec> confSupplier) {
        this.swagger.ignores(confSupplier.get());
        GsonProvider.create(confSupplier.get());
        return this;
    }

    public void generateDoc() throws IOException {
        new SwaggerHammer().prepareUi(config, swagger);
    }

    public ApiEndpoint endpoint(final EndpointDescriptor.Builder descriptorBuilder, final Filter filter) {
        Optional.ofNullable(apiPath).orElseThrow(() -> new IllegalStateException("API Path must be specified in order to build REST endpoint"));
        EndpointDescriptor descriptor = descriptorBuilder.build();
        spark.before(apiPath + descriptor.getPath() + "/*", filter);
        ApiEndpoint apiEndpoint = new ApiEndpoint(this, descriptor);
        this.swagger.addApiEndpoint(apiEndpoint);
        return apiEndpoint;
    }

    public SparkSwagger endpoint(final EndpointDescriptor.Builder descriptorBuilder, final Filter filter, Consumer<ApiEndpoint> endpointDef) {
        Optional.ofNullable(apiPath).orElseThrow(() -> new IllegalStateException("API Path must be specified in order to build REST endpoint"));
        EndpointDescriptor descriptor = descriptorBuilder.build();
        spark.before(apiPath + descriptor.getPath() + "/*", filter);
        ApiEndpoint apiEndpoint = new ApiEndpoint(this, descriptor);
        endpointDef.accept(apiEndpoint);
        this.swagger.addApiEndpoint(apiEndpoint);
        return this;
    }

    public SparkSwagger endpoint(final Endpoint endpoint) {
        Optional.ofNullable(endpoint).orElseThrow(() -> new IllegalStateException("API Endpoint cannot be null"));
        endpoint.bind(this);
        return this;
    }

    public SparkSwagger endpoints(final EndpointResolver resolver) {
        Optional.ofNullable(resolver).orElseThrow(() -> new IllegalStateException("API Endpoint Resolver cannot be null"));
        resolver.endpoints().forEach(this::endpoint);
        return this;
    }

    public SparkSwagger before(Filter filter) {
        spark.before(apiPath + "/*", filter);
        return this;
    }

    public SparkSwagger after(Filter filter) {
        spark.after(apiPath + "/*", filter);
        return this;
    }

    public synchronized SparkSwagger exception(Class<? extends Exception> exceptionClass, final ExceptionHandler handler) {
        spark.exception(exceptionClass, handler);
        return this;
    }

    public HaltException halt() {
        return spark.halt();
    }

    public HaltException halt(int status) {
        return spark.halt(status);
    }

    public HaltException halt(String body) {
        return spark.halt(body);
    }

    public HaltException halt(int status, String body) {
        return spark.halt(status, body);
    }

    private String getHost() {
        String host = this.config.getHost();
        if (host == null || host.contains("localhost") && host.split(":").length != 2) {
            throw new IllegalArgumentException("Host is required. If host name is 'localhost' you also need to specify port");
        } else if (host.contains("localhost")) {
            String[] hostParts = host.split(":");
            host = IpResolver.resolvePublicIp() + ":" + hostParts[1];
        }
        LOGGER.debug("Spark-Swagger: Host resolved to " + host);
        return host;
    }

    private Info getInfo() {
        if (version == null) {
            Project projectConfig = config.getProject();
            if (projectConfig != null) {
                version = VersionResolver.resolveVersion(projectConfig.getGroupId(), projectConfig.getArtifactId());
            }
        }

        ExternalDoc externalDocConf = this.config.getExternalDoc();
        if (externalDocConf != null) {
            ExternalDocs doc = ExternalDocs.newBuilder()
                    .withDescription(externalDocConf.getDescription())
                    .withUrl(externalDocConf.getUrl())
                    .build();
            swagger.setExternalDocs(doc);
        }

        Info info = new Info();
        info.description(config.getDescription());
        info.version(version);
        info.title(config.getTitle());
        info.termsOfService(config.getTermsOfService());

        swagger.schemes(config.getSchemes());

        ConfigContact contactConfig = this.config.getContact();
        if (contactConfig != null) {
            Contact contact = new Contact();
            contact.name(contactConfig.getName());
            contact.email(contactConfig.getEmail());
            contact.url(contactConfig.getUrl());
            info.setContact(contact);
        }
        ConfigLicense licenseConfig = this.config.getLicense();
        if (licenseConfig != null) {
            License license = new License();
            license.name(licenseConfig.getName());
            license.url(licenseConfig.getUrl());
            info.setLicense(license);
        }
        return info;
    }
}
