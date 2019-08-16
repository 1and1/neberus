package net.oneandone.neberus.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;

/**
 * internal javadoc
 */
@Path("/rootPath")
public class RestServiceWithInterfaceDoc implements RestServiceInterfaceDoc {

    /**
     * internal javadoc
     *
     * @param pathParam        internal doc that should be hidden
     * @param anotherPathParam internal doc
     *
     * @return internal doc
     */
    @GET
    @Path("/anotherGet/{pathParam123}/anotherPathParam/{anotherPathParam123}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response justAnotherGetMethodWithInterfaceDoc(@PathParam("pathParam123") String pathParam,
                                                         @PathParam("anotherPathParam123") String anotherPathParam,
                                                         @QueryParam("queryParam123") String queryParam) {
        return null;
    }

    /**
     * internal javadoc
     */
    @GET
    @Path("/anotherGet")
    @Override
    @Deprecated
    public void justYetAnotherGetMethodWithInterfaceDoc(@PathParam("pathParam123") String pathParam,
                                                        @QueryParam("queryParam123") String queryParam) {
    }

    public static class SomeDto {

        public String jsonParam;
        public String jsonParam2;
        public int jsonIntParam;
        public byte[] jsonbyteArrayParam;

        @JsonIgnore
        public String ignoreThisParam;

        public Map<String, String> simpleMap;
        public Map<String, Map<String, String>> nestedMap;

        public List<String> simpleList;
        public List<List<String>> nestedList;

        public Set<String> simpleSet;
        public Set<Set<String>> nestedSet;

        public Map<String, List<Set<String>>> nestedMagic;
        public List<Map<String, Set<String>>> nestedMagic2;
        public NestedDto nestedDto;
    }

    public static class NestedDto {
        public String nestedString;
    }

}
