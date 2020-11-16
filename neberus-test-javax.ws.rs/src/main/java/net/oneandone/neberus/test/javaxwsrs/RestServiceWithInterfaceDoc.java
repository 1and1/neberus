package net.oneandone.neberus.test.javaxwsrs;

import net.oneandone.neberus.test.RestServiceInterfaceDoc;
import net.oneandone.neberus.test.request.SomeFieldDto;

import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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
