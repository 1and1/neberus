package net.oneandone.neberus.test.springwebmvc;

import net.oneandone.neberus.test.RestServiceInterfaceDoc;
import net.oneandone.neberus.test.request.SomeFieldDto;
import net.oneandone.neberus.test.request.SomeRecordDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.Size;

/**
 * ignored internal javadoc
 */
@RequestMapping(value = RestServiceInterfaceDoc.PATH_ROOT, name = "ignored internal name for REST Service with doc in interface")
public class RestServiceWithInterfaceDoc implements RestServiceInterfaceDoc {

    @GetMapping(path = PATH_GET,
                produces = MediaType.APPLICATION_JSON_VALUE)
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
    @GetMapping(path = PATH_GET_WITH_DISPLAYED_INTERNAL_DOC, name = "displayed internal name",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Object getMethodWithDisplayedInternalDoc(String queryParam) {
        return null;
    }

    @PutMapping(path = PATH_PUT, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Override
    public void putMethod(@RequestBody SomeFieldDto dto) {

    }

    @PostMapping(path = PATH_POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Override
    public void postMethod(@RequestBody SomeFieldDto dto) {

    }

    @PostMapping(path = PATH_POST + "/record",
                 consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    @Override
    public void postRecordMethod(@RequestBody SomeRecordDto dto) {

    }

    @DeleteMapping(path = PATH_DELETE)
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
    @GetMapping(path = PATH_GET_WITH_RESPONSES,
                name = "ignored internal name for method",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Object getMethod(@PathVariable String stringPathParam,
                            @PathVariable(PATH_PARAM_INT) @Size(max = 42) String anotherPathParam,
                            @RequestParam("queryParam123") String queryParam) {
        return null;
    }
}
