package net.oneandone.neberus.test.unittest;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import net.oneandone.neberus.annotation.ApiDocumentation;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.oneandone.neberus.annotation.ApiSuccessResponse;
import net.oneandone.neberus.model.ApiStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ApiDocumentation
@Path("/rootPath")
public class DtoWithPublicFields {

    @GET
    @Path("/anotherGet/{pathParam}/anotherPathParam/{anotherPathParam}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiSuccessResponse(status = ApiStatus.OK, entityClass = SomeDto.class)
    public Response getMethod() {
        return null;
    }

    public static class SomeDto {

        /**
         * fieldDoc
         *
         * @see SomeEnum
         */
        public String jsonParam;
        /**
         * fieldDoc {@link SomeEnum}
         */
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
        public SomeEnum someEnum;
        public List<NestedDto> nestedDtoList;

    }

    public enum SomeEnum {
        FIRST, SECOND, THIRD
    }

    public static class NestedDto {

        public String nestedString;
    }

    public static class SomeChildDto extends SomeDto {

        public String childValue;
    }

}
