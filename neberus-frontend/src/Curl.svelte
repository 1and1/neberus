<script>
    import {beforeUpdate} from "svelte";
    import CurlParamControl from "./CurlParamControl.svelte";

    export let operation;
    export let openApi;
    export let method;
    export let path;

    $: baseReference = '';
    $: curlTemplate = '';

    $: headers = [];
    $: queryParams = [];
    $: pathParams = [];

    function reference(type, escapedName) {
        return baseReference + type + '_' + escapedName;
    }

    function highlightReference(escapedName) {
        return baseReference.replaceAll('_curl_', '') + '_param_' + escapedName;
    }

    function getAcceptTypes(operation) {
        let acceptTypes = new Set();
        let optional = false;

        Object.keys(operation.responses).forEach((status) => {
            if (status < 400 && operation.responses[status].content) {
                Object.keys(operation.responses[status].content).forEach((contentType) => {
                    acceptTypes.add(contentType);
                });
            } else if (!operation.responses[status].content) {
                optional = true;
            }
        });
        let array = Array.from(acceptTypes);
        array.optional = optional;
        return array;
    }

    function generateCurl() {

        let curl = 'curl -i -X ' + method.toUpperCase() + ' ';

        headers.forEach(header => {
            curl += "<span id=" + reference('header', header.extensions['x-name-escaped']) +
                " data-parameter-highlight-name='" + highlightReference(header.extensions['x-name-escaped']) +
                "' class='parameter-highlight' onmouseover='highlightParameter(this, event)' onmouseout='deHighlightParameter(this, event)'>";
            curl += "-H '" + header.name + ": <span id=" + reference('header_value', header.extensions['x-name-escaped']) + ">{" + header.name + "}</span>'";
            curl += " </span>";
        });

        if (operation.responses) {
            let acceptTypes = getAcceptTypes(operation);

            if (acceptTypes.length > 0) {
                curl += "<span id=" + reference('header', 'Accept') + " data-parameter-highlight-name='" + highlightReference('Accept') +
                    "' class='parameter-highlight' onmouseover='highlightParameter(this, event)' onmouseout='deHighlightParameter(this, event)'" +
                    (acceptTypes.optional ? "style='display: none;'" : "") + ">";
                curl += "-H '" + "Accept" + ": <span id=" + reference('header_value', 'Accept') + ">" + acceptTypes[0] + "</span>'";
                curl += " </span>";
            }
        }

        if (operation.requestBody) {
            let contentType = Object.keys(operation.requestBody.content)[0];

            // console.log(contentType);

            curl += '<span id=' + reference('base', 'body') + '>';
            curl += getCurlBody(contentType);
            curl += '</span>';
        }

        curl += "'";
        curl += '<span id="' + reference('base', 'host')
            + '" data-parameter-highlight-name="' + highlightReference('base_host') + '" class="parameter-highlight" ' +
            ' onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">' +
            openApi.servers[0].url + '</span>';


        let expandedPath = path;

        pathParams.forEach(pathParam => {
            expandedPath = expandedPath.replaceAll(
                '{' + pathParam.name + '}',
                '<span id="' + reference('path_value', pathParam.extensions['x-name-escaped']) +
                '" data-parameter-highlight-name="' + highlightReference(pathParam.extensions['x-name-escaped']) + '" class="parameter-highlight" ' +
                ' onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">{' + pathParam.name + '}</span>'
            );
        });

        curl += expandedPath;

        let firstQueryParam = true;

        queryParams.forEach(queryParam => {
            let deprecatedClass = queryParam.deprecated ? 'curl-param-deprecated' : '';
            let deprecatedStyle = !queryParam.required && queryParam.deprecated ? ' style="display: none"' : '';

            curl += "<span id=" + reference('query', queryParam.extensions['x-name-escaped']) +
                " data-parameter-highlight-name='" + highlightReference(queryParam.extensions['x-name-escaped']) +
                "' class='parameter-highlight " + deprecatedClass + "' onmouseover='highlightParameter(this, event)' onmouseout='deHighlightParameter(this, event)'" +
                deprecatedStyle + ">";
            curl += '<span class="query-param-delimiter">' + (firstQueryParam ? '?' : '&') + '</span>';
            curl += queryParam.name + '=<span id="' + reference('query_value', queryParam.extensions['x-name-escaped']) + '">{' + queryParam.name + '}</span>';
            curl += "</span>";
            firstQueryParam = false;
        })

        curl += "'";

        return curl;
    }

    function getCurlBody(contentType) {
        let curlBody = '';

        curlBody += "-H 'Content-Type: " + contentType;

        if (!contentType.includes("charset")) {
            curlBody += ";charset=utf-8";
        }
        curlBody += "' ";


        let schemaString = getBodyString(contentType, false);

        curlBody += "-d '<span id='" + reference('base', 'body_content') + "'>" + schemaString + "</span>' ";

        return curlBody;
    }

    function getBodyString(contentType, pretty) {
        let schema = operation.requestBody.content[contentType].schema;
        let currentSchema = (schema.$ref ? findSchema(openApi, schema.$ref) : schema);
        let typeReference = baseReference.replaceAll('_curl_', '') + "_" + contentType.replaceAll(/[^A-Za-z0-9]/g, '_') + "_" + currentSchema.type;

        let schemaString = '';

        let fullSchema = resolveRefs(openApi, schema, false, []);

        if (contentType.includes('json')) {
            schemaString = pretty
                ? toJsonPretty(fullSchema).replaceAll(': ', ':')
                : getSchemaCustomJson(openApi, fullSchema, typeReference);
        } else if (contentType.includes('xml')) {
            let actualSchema = schema.$ref ? findSchema(openApi, schema.$ref) : schema;
            let title = typeof (schema) == "string" ? schema : actualSchema.extensions['x-java-type'];

            schemaString = pretty
                ? toFullXmlPretty(fullSchema, title).replaceAll('&lt;', '<').replaceAll('&gt;', '>')
                : getSchemaCustomXml(openApi, fullSchema, typeReference, title);
        } else if (contentType.includes('form-urlencoded')) {
            schemaString = pretty ? toFormUrlencodedPretty(fullSchema, '') : getSchemaFormUrlencoded(openApi, fullSchema, typeReference);
        }
        return schemaString;
    }

    function getSchemaCustomJson(openApi, fullSchema, parent) {

        let jsonString = '<span class="parameter-highlight" data-parameter-highlight-name="' + parent + '" ' +
            'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">';
        jsonString += toJsonString(fullSchema, parent);
        jsonString += '</span>';

        return jsonString;
    }

    function toJsonString(json, parent) {
        let s = '';

        if (Array.isArray(json)) {
            s += '['
            let first = true;
            json.forEach(elem => {
                if (!first) {
                    s += ',';
                }

                // s += '{' + toJsonString(elem, parent + '_array') + '}';
                let paramKey = parent + '_item';

                s += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramKey + '" ' +
                    'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">';
                s += toJsonString(elem, paramKey);
                s += '</span>';

                first = false;
            });

            s += ']'
        } else if (typeof json === 'string') {
            if (json === '{String}') {
                s += '"' + json + '"';
            } else {
                s += json;
            }
        } else {
            s += '{';

            let first = true;
            Object.keys(json).filter(key => !key.startsWith('__')).forEach(key => {
                if (!first) {
                    s += ',';
                }

                if (key === '{String}') {
                    let paramKey = parent + '_' + 'key';
                    let paramValue = parent + '_' + 'value';

                    s += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramKey + '" ' +
                        'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">'
                    s += '"' + key + '"';
                    s += '</span>';
                    s += ':';

                    s += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramValue + '" ' +
                        'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">'
                    s += toJsonString(json[key], paramValue);
                    s += '</span>'
                } else {
                    let paramKey = parent + '_' + key;
                    //FIXME make __deprecated available for primitive values
                    let deprecatedClass = json[key].__deprecated ? 'curl-param-deprecated' : '';

                    s += '<span class="parameter-highlight ' + deprecatedClass + '" data-parameter-highlight-name="' + paramKey + '" ' +
                        'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">'
                    s += '"' + key + '":' + toJsonString(json[key], paramKey);
                    s += '</span>';
                }

                first = false;
            });

            s += '}';
        }

        return s;
    }

    function getSchemaCustomXml(openApi, fullSchema, parent, title) {

        // let actualSchema = schema.$ref ? findSchema(openApi, schema.$ref) : schema;
        // let title = typeof (schema) == "string" ? schema : actualSchema.extensions['x-java-type'];

        let xmlString = '<span class="parameter-highlight" data-parameter-highlight-name="' + parent + '" ' +
            'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">';
        xmlString += toFullXml(fullSchema, title, parent);
        xmlString += '</span>';

        return xmlString.replaceAll('\n', '');
    }

    function toFullXml(o, root, parent) {
        root = root[0] + root.substring(1);

        let xmlSchema = '&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;\n';
        xmlSchema += '&lt;' + root + '&gt;\n';
        xmlSchema += toXml(o, 1, parent);
        xmlSchema += '&lt;/' + root + '&gt;';

        return xmlSchema;
    }

    function toXml(o, level, parent) {
        let indent = ''.repeat(level);
        let xmlSchema = '';

        if (!o) {
            return xmlSchema;
        }

        if (o.__type === 'array') {

            let paramKey = parent + '_item';

            xmlSchema += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramKey + '" ' +
                'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">';

            let child = toXml(o[0], level + 1, paramKey);

            if (child.trim().split('\n').length > 1 || child.split('&lt;').length > 1) {
                xmlSchema += indent + '&lt;element&gt;\n';
                xmlSchema += child;
                xmlSchema += indent + '&lt;/element&gt;\n';
            } else {
                xmlSchema += indent + '&lt;element&gt;';
                xmlSchema += child.trim();
                xmlSchema += '&lt;/element&gt;\n';
            }

            xmlSchema += '</span>';

        } else if (o.__type === 'object') {

            let paramKey = parent + '_' + 'key';
            let paramValue = parent + '_' + 'value';

            let mapKey = '';
            mapKey += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramKey + '" ' +
                'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">'
            mapKey += '{String}';
            mapKey += '</span>';

            xmlSchema += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramValue + '" ' +
                'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">'

            let child = toXml(o['{String}'], level + 1, paramValue);

            if (child.trim().split('\n').length > 1 || child.split('&lt;').length > 1) {
                xmlSchema += indent + '&lt;' + mapKey + '&gt;\n';
                xmlSchema += child;
                xmlSchema += indent + '&lt;/' + mapKey + '&gt;\n';
            } else {
                xmlSchema += indent + '&lt;' + mapKey + '&gt;';
                xmlSchema += child.trim();
                xmlSchema += '&lt;/' + mapKey + '&gt;\n';
            }

            xmlSchema += '</span>'

        } else if (o.__type === 'string' || o.__type === 'integer' || o.__type === 'number' || o.__type === 'boolean' || o.__type === 'null') {
            xmlSchema += '{' + o.__type + '}' + '\n';
        } else if (typeof o === 'string') {
            xmlSchema += o + '\n';
        } else {
            Object.keys(o).filter(key => !key.startsWith('__')).forEach(key => {

                let paramKey = parent + '_' + key;

                xmlSchema += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramKey + '" ' +
                    'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">'

                let child = toXml(o[key], level + 1, paramKey);

                if (child.trim().split('\n').length > 1 || child.split('&lt;').length > 1) {
                    xmlSchema += indent + '&lt;' + key + '&gt;\n';
                    xmlSchema += child;
                    xmlSchema += indent + '&lt;/' + key + '&gt;\n';
                } else {
                    xmlSchema += indent + '&lt;' + key + '&gt;';
                    xmlSchema += child.trim();
                    xmlSchema += '&lt;/' + key + '&gt;\n';
                }

                xmlSchema += '</span>';

            });
        }

        return xmlSchema;
    }

    function getSchemaFormUrlencoded(openApi, fullSchema, parent) {

        let formString = '<span class="parameter-highlight" data-parameter-highlight-name="' + parent + '" ' +
            'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">';
        formString += toFormUrlencoded(fullSchema, '', parent);
        formString += '</span>';

        return formString;
    }

    function toFormUrlencoded(o, formParent, parent) {
        let formSchema = '';

        if (!o) {
            return formSchema;
        }

        if (o.__type === 'array') {
            let paramKey = parent + '_item';

            formSchema += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramKey + '" ' +
                'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">';
            formSchema += toFormUrlencoded(o[0], formParent + '[0]', paramKey);
            formSchema += '</span>';

        } else if (o.__type === 'object') {

            let paramKey = parent + '_' + 'key';
            let paramValue = parent + '_' + 'value';

            let mapKey = '';
            mapKey += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramKey + '" ' +
                'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">'
            mapKey += '{String}';
            mapKey += '</span>';

            formSchema += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramValue + '" ' +
                'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">'
            formSchema += toFormUrlencoded(o['{String}'], formParent + '[' + mapKey + ']', paramValue);
            formSchema += '</span>'

        } else if (o.__type === 'string' || o.__type === 'integer' || o.__type === 'number' || o.__type === 'boolean' || o.__type === 'null') {
            formSchema += formParent + '={' + o.__type + '}';
        } else if (typeof o === 'string') {
            formSchema += formParent + '=' + o;
        } else {
            Object.keys(o).filter(key => !key.startsWith('__')).forEach(key => {

                let paramKey = parent + '_' + key;
                let newParent = formParent === '' ? key : (formParent + '[' + key + ']');

                let child = toFormUrlencoded(o[key], newParent, paramKey);

                if (child !== '') {

                    formSchema += '<span class="parameter-highlight" data-parameter-highlight-name="' + paramKey + '" ' +
                        'onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">'
                    formSchema += child;
                    formSchema += '</span>';
                    formSchema += '<span class="word-wrap"></span>&';
                }
            });
            formSchema = formSchema.substr(0, formSchema.length - 1);
        }

        return formSchema;
    }

    function updateParam(event) {
        let type = jQuery(event.detail).data('type');
        let name = jQuery(event.detail).data('name');
        let value = jQuery(event.detail).val();

        // console.log(type);

        let encodedValue = type === 'header_value' ? value : encodeURIComponent(value);
        jQuery('#' + reference(type, name)).html(encodedValue);
    }

    function updateEnumParam(event) {
        let type = jQuery(event.detail).data('type');
        let name = jQuery(event.detail).data('name');
        let value = event.detail.selectedEnumValue;

        let encodedValue = type === 'header_value' ? value : encodeURIComponent(value);
        jQuery('#' + reference(type, name)).html(encodedValue);
    }

    function toggleParam(event) {
        let element = event.detail ?? event.target;

        let type = jQuery(element).data('type');
        let name = jQuery(element).data('name');
        let value = this.checked;

        // toggle visibility
        let valueHolder = jQuery('#' + reference(type, name));
        valueHolder.toggle(value);
        jQuery('#' + reference(type, name) + '_controls').toggleClass('deactivated');

        // update visible param delimiters
        let first = true;
        jQuery('.query-param-delimiter:visible', valueHolder.parent()).each((idx, delim) => {
            jQuery(delim).html(first ? '?' : '&');
            first = false;
        });
    }

    function updateHost() {
        let customHostControl = jQuery('#' + reference('base', 'host_custom_control'));

        if (selectedHost === '__custom') {
            customHostControl.show();
            jQuery('#' + reference('base', 'host')).html(customHostControl.val());
        } else {
            customHostControl.hide();
            jQuery('#' + reference('base', 'host')).html(selectedHost);
        }

    }

    function updateCustomHost() {
        let value = jQuery(this).val();
        jQuery('#' + reference('base', 'host')).html(value);
    }

    function updateContentType() {
        // console.log(selectedContentType);
        let curlBody = getCurlBody(selectedContentType);
        jQuery('#' + reference('base', 'body')).html(curlBody);

        let curlBodyPretty = getBodyString(selectedContentType, true);

        jQuery('#' + reference('base', 'body_content_control')).val(curlBodyPretty);
    }

    function updateAcceptType() {
        // console.log(selectedAcceptType);

        jQuery('#' + reference('header_value', 'Accept')).html(selectedAcceptType);
    }

    function updateBodyContent() {
        let value = jQuery(this).val();

        let sanitizedValue = '';

        value.split('\n').forEach(line => {
            sanitizedValue += line.trim().replaceAll('<', '&lt;').replaceAll('>', '&gt;');
        });

        jQuery('#' + reference('base', 'body_content')).html(sanitizedValue);
    }

    beforeUpdate(async () => {
        baseReference = operation.operationId + '_curl_';

        if (operation.parameters) {
            let tmpHeaders = [];
            let tmpQueryParams = [];
            let tmpPathParams = [];

            operation.parameters.forEach(param => {
                if (param.in === 'header') {
                    tmpHeaders.push(param);
                } else if (param.in === 'query') {
                    tmpQueryParams.push(param);
                } else {
                    tmpPathParams.push(param);
                }
            });

            headers = tmpHeaders;
            queryParams = tmpQueryParams;
            pathParams = tmpPathParams;

            // console.log(queryParams);
        }

        curlTemplate = generateCurl();
    });

    let selectedHost;
    let selectedContentType;
    let selectedAcceptType;

    const initCollapse = el => {
        initCollapseToggle(el);
    }

</script>

{#if operation.extensions && operation.extensions['x-curl-enabled']}
    <div class="card card-primary card-nested">
        <div>
            <h6 class="card-header bg-dark collapsed" data-bs-toggle="collapse" use:initCollapse
                data-bs-target="#{baseReference}container" aria-expanded="false">Curl <i class="fas fa-cog"/></h6>

            <div class="table-curl-container collapse" id="{baseReference}container">

                <table class="table-curl">
                    <thead>
                    <th></th>
                    <th></th>
                    <th></th>
                    <th></th>
                    </thead>
                    <tbody>
                    <tr>
                        <th colspan="4">General</th>
                    </tr>
                    <tr id="{reference('base', 'host_control')}_controls" class="controls-row parameter-highlight"
                        data-parameter-highlight-name="{baseReference.replaceAll('_curl_', '')}_param_base_host"
                        onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">
                        <th></th>
                        <td></td>
                        <td>Host</td>
                        <td>
                            <select class="form-select custom-select" bind:value={selectedHost} on:change={updateHost}>
                                {#each openApi.servers as server}
                                    <option value="{server.url}">{server.url} {server.description ? '[' + server.description + ']' : ''}</option>
                                {/each}
                                <option value="__custom">Other</option>
                            </select>
                            <i class="fa fa-caret-down select-caret" aria-hidden="true"></i>
                            <input id={reference('base', 'host_custom_control')} type="text" placeholder="https://myserver.tld"
                                   on:keyup={updateCustomHost} style="display: none;"/>
                        </td>
                    </tr>

                    {#if headers.length > 0 || (operation.responses && getAcceptTypes(operation).length > 0)}
                        <tr>
                            <th colspan="4">Headers</th>
                        </tr>
                    {/if}

                    {#if operation.responses && getAcceptTypes(operation).length > 0}
                        {#each [getAcceptTypes(operation)] as acceptTypes}
                            {#if acceptTypes && acceptTypes.length > 0}
                                <tr id="{reference('header', 'Accept')}_controls"
                                    class="controls-row parameter-highlight {acceptTypes.optional ? 'deactivated' : ''}"
                                    data-parameter-highlight-name="{baseReference.replaceAll('_curl_', '')}_param_Accept"
                                    onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">
                                    <th></th>
                                    <td>
                                        {#if acceptTypes.optional}
                                            <div class="form-check">
                                                <input class="form-check-input" data-type="header" data-name="Accept"
                                                       type="checkbox" on:change={toggleParam} />
                                            </div>
                                        {/if}
                                    </td>
                                    <td>Accept</td>
                                    <td>
                                        <select class="form-select custom-select" bind:value={selectedAcceptType} on:change={updateAcceptType}>
                                            {#each acceptTypes as acceptType}
                                                {#if acceptType !== ""}
                                                    <option value="{acceptType}">{acceptType}</option>
                                                {/if}
                                            {/each}
                                        </select>
                                        <i class="fa fa-caret-down select-caret" aria-hidden="true"></i>
                                    </td>
                                </tr>
                            {/if}
                        {/each}
                    {/if}

                    <CurlParamControl params={headers} type="header" name="" baseReference={baseReference}
                                      on:toggleParam={toggleParam} on:updateParam={updateParam} on:updateEnumParam={updateEnumParam}/>

                    <CurlParamControl params={queryParams} type="query" name="Query Parameters" baseReference={baseReference}
                                      on:toggleParam={toggleParam} on:updateParam={updateParam} on:updateEnumParam={updateEnumParam}/>

                    <CurlParamControl params={pathParams} type="path" name="Path Parameters" baseReference={baseReference}
                                      on:toggleParam={toggleParam} on:updateParam={updateParam} on:updateEnumParam={updateEnumParam}/>

                    {#if operation.requestBody}
                        <tr>
                            <th colspan="4">Body</th>
                        </tr>
                        <tr>
                            <th></th>
                            <td></td>
                            <td>Content-Type</td>
                            <td>
                                <select class="form-select custom-select"
                                        bind:value={selectedContentType} on:change={updateContentType}>
                                    {#each Object.keys(operation.requestBody.content) as contentType}
                                        <option value="{contentType}">{contentType}</option>
                                    {/each}
                                </select>
                                <i class="fa fa-caret-down select-caret" aria-hidden="true"></i>
                            </td>
                        </tr>
                        <tr>
                            <th></th>
                            <td></td>
                            <td>Content</td>
                            <td>
                                <textarea id="{reference('base', 'body_content_control')}"
                                          on:keyup={updateBodyContent}>{getBodyString(Object.keys(operation.requestBody.content)[0], true)}</textarea>
                            </td>
                        </tr>
                    {/if}

                    </tbody>
                </table>
            </div>
        </div>
        <div class="card-body">
            <div class="card-text">
                <div class="code">
                    {#if operation.extensions['x-curl-example']}
                        {operation.extensions['x-curl-example']}
                    {:else}
                        {@html curlTemplate}
                    {/if}
                </div>
            </div>
        </div>
    </div>
{/if}

<style>

    label {
        margin-bottom: 0px;
    }

    .form-check {
        padding-left: 30px;
        padding-right: 10px;
    }

    .table-curl {
        width: 100%;
    }

    .table-curl-container {
        padding: 10px;
    }

    .table-curl input[type="text"] {
        width: 100%;
    }

    .table-curl tr th:first-of-type {
        width: 150px;
    }

    .table-curl tr td:first-of-type {
        max-width: 21px;
    }

    .table-curl tr td:nth-of-type(2) {
        min-width: 150px;
        padding-right: 10px;
    }

    textarea {
        width: 100%;
        height: 200px;
    }

    select {
        width: 100%;
    }

    .card {
        border: 0px;
        box-shadow: 4px 4px 10px 0px #0000007a;
        /*border-radius: 0rem;*/
    }

</style>
