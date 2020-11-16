package net.oneandone.neberus.test.springwebmvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.oneandone.neberus.test.RestServiceInterfaceDoc.PATH_GET;
import static net.oneandone.neberus.test.RestServiceInterfaceDoc.PATH_ROOT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link RestServiceWithInterfaceDoc}
 */
public class RestServiceWithInterfaceDocTest {

    private static JsonNode document;

    @BeforeClass
    public static void readHtml() throws Exception {
        document = new ObjectMapper().readTree(new File("../neberus-test-spring-webmvc/target/apidocs/openApi.json"));
    }

    /**
     * @see RestServiceWithInterfaceDoc#getMethod()
     */
    @Test
    public void test_get() {
        String path = PATH_ROOT + PATH_GET;

        // assert method was documented
        assertThat(document.get("paths").has(path)).isTrue();

        // assert method data
        JsonNode methodNode = document.get("paths").get(path).get("get");
        assertThat(methodNode.get("summary").asText()).isEqualTo("GET method with interface doc");
        assertThat(asList(methodNode.get("tags"))).contains("resource:RestServiceWithInterfaceDoc");

    }

    private static List<String> asList(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException();
        }
        return StreamSupport.stream(node.spliterator(), false).map(JsonNode::textValue).collect(Collectors.toList());
    }

}
