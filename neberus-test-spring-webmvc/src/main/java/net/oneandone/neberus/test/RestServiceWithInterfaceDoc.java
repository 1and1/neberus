package net.oneandone.neberus.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * internal javadoc
 */
@RequestMapping(value = "/rootPath", name = "Super Awesome REST Service with doc in interface")
public class RestServiceWithInterfaceDoc implements RestServiceInterfaceDoc {

    /**
     * internal javadoc
     *
     * @param pathParam        internal doc that should be hidden
     * @param anotherPathParam internal doc
     *
     * @return internal doc
     */
    @GetMapping(path = "/anotherGet/{pathParam123}/anotherPathParam/{anotherPathParam123}",
                name = "This is an awesome method",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    @Deprecated
    public SomeDto justAnotherGetMethodWithInterfaceDoc(@PathVariable String pathParam,
                                                       @PathVariable("anotherPathParam123") String anotherPathParam,
                                                       @RequestParam("queryParam123") String queryParam,
                                                       SomeDto dto) {
        return null;
    }

    /**
     * internal javadoc
     */
    @GetMapping(path = "/anotherGet/",
                consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Override
    public void justYetAnotherGetMethodWithInterfaceDoc(@PathVariable("pathParam123") String pathParam,
                                                        @RequestParam("queryParam123") String queryParam,
                                                        SomeDto dto) {
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
    }

}
