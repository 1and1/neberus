package net.oneandone.neberus.test.jakartawsrs;

import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import net.oneandone.neberus.test.RestServiceInterfaceDoc;
import net.oneandone.neberus.test.request.SomeFieldDto;

/**
 * internal javadoc
 */
@Path("/rootPath")
public class RestServiceWithInterfaceDoc implements RestServiceInterfaceDoc {

    @GET
    @Path(PATH_GET)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Object getMethod() {
        return null;
    }

    /**
     * displayed internal javadoc
     *
     * @param pathParam displayed internal @param javadoc
     *
     * @return displayed internal @return javadoc
     */
    @GET
    @Path(PATH_GET_WITH_DISPLAYED_INTERNAL_DOC)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Object getMethodWithDisplayedInternalDoc(@QueryParam("queryParam") String queryParam) {
        return null;
    }

    @PUT
    @Path(PATH_PUT)
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public void putMethod(SomeFieldDto dto) {

    }

    @POST
    @Path(PATH_POST)
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Override
    public void postMethod(SomeFieldDto dto) {

    }

    @DELETE
    @Path(PATH_DELETE)
    @Override
    public void deleteMethod() {

    }

    /**
     * ignored internal javadoc
     *
     * @param stringPathParam  ignored internal doc
     * @param anotherPathParam ignored internal doc
     *
     * @return ignored internal doc
     */
    @GET
    @Path(PATH_GET_WITH_RESPONSES)
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Object getMethod(@PathParam(PATH_PARAM_STRING) String stringPathParam,
                            @PathParam(PATH_PARAM_INT) @Size(max = 42) String anotherPathParam,
                            @QueryParam("queryParam123") String queryParam) {
        return null;
    }

}
