<script>
    import BodyParameters from "./BodyParameters.svelte";
    import AllowedValue from "./AllowedValue.svelte";

    export let operation;
    export let method;
    export let openApi;

    let reasonPhrases = {};
    reasonPhrases[100] = "Continue";
    reasonPhrases[101] = "Switching Protocols";
    reasonPhrases[102] = "Processing";
    reasonPhrases[103] = "Checkpoint";
    reasonPhrases[200] = "OK";
    reasonPhrases[201] = "Created";
    reasonPhrases[202] = "Accepted";
    reasonPhrases[203] = "Non-Authoritative Information";
    reasonPhrases[204] = "No Content";
    reasonPhrases[205] = "Reset Content";
    reasonPhrases[206] = "Partial Content";
    reasonPhrases[207] = "Multi-Status";
    reasonPhrases[208] = "Already Reported";
    reasonPhrases[226] = "IM Used";
    reasonPhrases[300] = "Multiple Choices";
    reasonPhrases[301] = "Moved Permanently";
    reasonPhrases[302] = "Found";
    reasonPhrases[302] = "Moved Temporarily";
    reasonPhrases[303] = "See Other";
    reasonPhrases[304] = "Not Modified";
    reasonPhrases[305] = "Use Proxy";
    reasonPhrases[307] = "Temporary Redirect";
    reasonPhrases[308] = "Permanent Redirect";
    reasonPhrases[400] = "Bad Request";
    reasonPhrases[401] = "Unauthorized";
    reasonPhrases[402] = "Payment Required";
    reasonPhrases[403] = "Forbidden";
    reasonPhrases[404] = "Not Found";
    reasonPhrases[405] = "Method Not Allowed";
    reasonPhrases[406] = "Not Acceptable";
    reasonPhrases[407] = "Proxy Authentication Required";
    reasonPhrases[408] = "Request Timeout";
    reasonPhrases[409] = "Conflict";
    reasonPhrases[410] = "Gone";
    reasonPhrases[411] = "Length Required";
    reasonPhrases[412] = "Precondition failed";
    reasonPhrases[413] = "Payload Too Large";
    reasonPhrases[413] = "Request Entity Too Large";
    reasonPhrases[414] = "URI Too Long";
    reasonPhrases[414] = "Request-URI Too Long";
    reasonPhrases[415] = "Unsupported Media Type";
    reasonPhrases[416] = "Requested Range Not Satisfiable";
    reasonPhrases[417] = "Expectation Failed";
    reasonPhrases[418] = "I'm a teapot";
    reasonPhrases[419] = "Insufficient Space On Resource";
    reasonPhrases[420] = "Method Failure";
    reasonPhrases[421] = "Destination Locked";
    reasonPhrases[422] = "Unprocessable Entity";
    reasonPhrases[423] = "Locked";
    reasonPhrases[424] = "Failed Dependency";
    reasonPhrases[426] = "Upgrade Required";
    reasonPhrases[428] = "Precondition Required";
    reasonPhrases[429] = "Too Many Requests";
    reasonPhrases[431] = "Request Header Fields Too Large";
    reasonPhrases[451] = "Unavailable For Legal Reasons";
    reasonPhrases[500] = "Internal Server Error";
    reasonPhrases[501] = "Not Implemented";
    reasonPhrases[502] = "Bad Gateway";
    reasonPhrases[503] = "Service Unavailable";
    reasonPhrases[504] = "Gateway Timeout";
    reasonPhrases[505] = "HTTP Version Not Supported";
    reasonPhrases[506] = "Variant Also Negotiates";
    reasonPhrases[507] = "Insufficient Storage";
    reasonPhrases[508] = "Loop Detected";
    reasonPhrases[509] = "Bandwidth Limit Exceeded";
    reasonPhrases[510] = "Not Extended";
    reasonPhrases[511] = "Network Authentication Required";

    function getReasonPhrase(statusCode) {
        let phrase = reasonPhrases[statusCode];
        return phrase ? phrase : "";
    }

    function getCategory(statusCode) {
        if (statusCode.startsWith('1')) {
            return 'category-information';
        } else if (statusCode.startsWith('2')) {
            return 'category-success';
        } else if (statusCode.startsWith('3')) {
            return 'category-redirect';
        } else if (statusCode.startsWith('4')) {
            return 'category-client-error';
        } else if (statusCode.startsWith('5')) {
            return 'category-server-error';
        }
    }

    const initCollapse = el => {
        initCollapseToggle(el);
    }

    const initTooltip = el => {
        initTooltipBox(el);
    }

</script>

{#if Object.keys(operation.responses).length > 0}
    <div class="card card-primary table-responsive">
        <h5 class="card-header bg-secondary">Response</h5>
        <div class="card-body">

            {#each Object.keys(operation.responses) as statusCode }
                <div class="card card-primary card-nested response">
                    <div class="card-header collapsed {getCategory(statusCode)}"
                         data-bs-toggle="{(operation.responses[statusCode].content || operation.responses[statusCode].headers) ? 'collapse' : ''}"
                         use:initCollapse
                         data-bs-target=".{operation.operationId}_{statusCode}"
                         aria-expanded="false">
                        <table>
                            <tbody>
                            <tr>
                                <td class="status-code">
                                    <strong>{statusCode} {getReasonPhrase(statusCode)}</strong>
                                    {#if operation.responses[statusCode].content || operation.responses[statusCode].headers}
                                        <span>
                                            <i class="icon-toggle fas fa-angle-right"></i>
                                        </span>
                                    {/if}
                                </td>
                                <td>{@html operation.responses[statusCode].description}</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>

                    {#if operation.responses[statusCode].content || operation.responses[statusCode].headers}
                        <div class="card-body collapse {operation.operationId}_{statusCode}">
                            <div class="card-text">

                                {#if operation.responses[statusCode].content}
                                    {#each Object.keys(operation.responses[statusCode].content) as contentType }
                                        <div class="card card-primary card-nested card-table {operation.responses[statusCode].content[contentType].examples ? 'parameters-with-examples' : ''}">
                                            <div class="card-header bg-dark">
                                                <table>
                                                    <tbody>
                                                    <tr>
                                                        <td class="content-type">
                                                            Body <strong>[{contentType}]</strong>
                                                        </td>
                                                        <td>
                                                            {#if operation.responses[statusCode].content[contentType].extensions && operation.responses[statusCode].content[contentType].extensions['x-description']}
                                                                {@html operation.responses[statusCode].content[contentType].extensions['x-description']}
                                                            {/if}
                                                        </td>
                                                    </tr>
                                                    </tbody>
                                                </table>
                                            </div>
                                            <div class="card-body">
                                                {#if operation.responses[statusCode].content[contentType].schema}
                                                    <div class="card-text">
                                                        <BodyParameters openApi={openApi}
                                                                        schema={operation.responses[statusCode].content[contentType].schema}
                                                                        operationReference={operation.operationId}_response
                                                                        contentType={contentType}/>
                                                    </div>
                                                {/if}
                                            </div>
                                            {#if operation.responses[statusCode].content[contentType].examples}
                                                <div class="card-footer">
                                                    {#each Object.keys(operation.responses[statusCode].content[contentType].examples) as example}
                                                        <div class="body-example">
                                                            <div>
                                                                {example}
                                                            </div>
                                                            <div class="code">
                                                                {@html operation.responses[statusCode].content[contentType].examples[example].value}
                                                            </div>
                                                        </div>
                                                    {/each}
                                                </div>
                                            {/if}
                                        </div>
                                    {/each}
                                {/if}

                                {#if operation.responses[statusCode].headers}
                                    <div class="card card-primary card-nested card-table">
                                        <h6 class="card-header bg-dark">Headers</h6>
                                        <div class="card-body">
                                            <div class="card-text">

                                                <table class="table table-dark table-hover table-small-head parameters">
                                                    <thead>
                                                    <tr>
                                                        <th>Name</th>
                                                        <th>Description</th>
                                                        <th>Value</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    {#each Object.keys(operation.responses[statusCode].headers) as header }
                                                        <tr>
                                                            <td>
                                                                <span class="optionalIndicator">
                                                                    <span data-bs-container="body" data-bs-toggle="tooltip"
                                                                          use:initTooltip data-bs-placement="top"
                                                                          title="{(operation.responses[statusCode].headers[header].required || (operation.responses[statusCode].headers[header].extensions && operation.responses[statusCode].headers[header].extensions['x-java-type-required'])) ? 'Mandatory' : 'Optional'}">
                                                                        <i class="{(operation.responses[statusCode].headers[header].required || (operation.responses[statusCode].headers[header].extensions && operation.responses[statusCode].headers[header].extensions['x-java-type-required'])) ? 'fas' : 'far'} fa-circle"></i>
                                                                    </span>
                                                                </span>

                                                                {#if operation.responses[statusCode].headers[header].deprecated}
                                                                    <span class="deprecated" data-bs-container="body"
                                                                          data-bs-toggle="tooltip" use:initTooltip
                                                                          data-bs-placement="top"
                                                                          title="{operation.responses[statusCode].headers[header].extensions && operation.responses[statusCode].headers[header].extensions['x-deprecated-description'] ? operation.responses[statusCode].headers[header].extensions['x-deprecated-description'] : ''}">
                                                                        {header}
                                                                    </span>
                                                                {:else}
                                                                    {header}
                                                                {/if}
                                                            </td>
                                                            <td>
                                                                {#if operation.responses[statusCode].headers[header].description}
                                                                    {@html operation.responses[statusCode].headers[header].description}
                                                                {/if}
                                                            </td>
                                                            <td>
                                                                <AllowedValue
                                                                        param={operation.responses[statusCode].headers[header]}/>
                                                            </td>
                                                        </tr>
                                                    {/each}

                                                    </tbody>
                                                </table>

                                            </div>
                                        </div>
                                    </div>
                                {/if}

                            </div>
                        </div>
                    {/if}

                </div>

            {/each}

        </div>
    </div>
{/if}

<style>
    .card {
        border: 0px;
        box-shadow: 4px 4px 10px 0px #0000007a;
    }

    .response .status-code {
        min-width: 250px;
        display: inline-block;
    }

    .response .content-type {
        min-width: 250px;
        display: inline-block;
        padding-right: 20px;
    }

    .card-footer {
        margin-top: 15px;
    }

    .body-example {
        padding-bottom: 15px;
    }

    .body-example:last-of-type {
        padding-bottom: 0px;
    }

</style>
