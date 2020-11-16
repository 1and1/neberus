<script>
    export let openApi;
    export let method;

    function getLinkedParamType(openApi, method, parameterName) {
        if (!method.linkedMethod) {
            return '';
        }

        let params = openApi.paths[method.path][method.httpMethod.toLowerCase()].parameters;

        for (let param of params) {
            if (param.name === parameterName) {
                return param.in;
            }
        }

        return '';
    }

    const initTooltip = el => {
        initTooltipBox(el);
    }

</script>


<div class="card card-nested usecase-method">
    <div class="card-body">
        <div class="card-text">
            <div class="usecase-description">{@html method.description}</div>

            <div class="code usecase-path">{method.httpMethod} {method.path}</div>

            {#if method.linkedMethod}
                <div class="usecase-path">
                    <a href="?resource={method.linkedMethod.resource}&operation={method.httpMethod.toUpperCase()}-{method.linkedMethod.label.replaceAll(' ', '_')}">
                        [{method.linkedMethod.resource}: {method.linkedMethod.label}]
                    </a>
                </div>
            {/if}

            {#if Object.keys(method.parameters).length > 0}
                <div class="card card-primary card-table usecase-parameters ">
                    <h6 class="card-header bg-dark">Parameters</h6>
                    <div class="card-body">
                        <div class="card-text">
                            <table class="table table-dark table-hover table-striped table-small-head parameters">
                                <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Type</th>
                                    <th>Value</th>
                                </tr>
                                </thead>
                                <tbody>
                                {#each Object.keys(method.parameters) as parameterName}
                                    <tr>
                                        <td>{parameterName}</td>
                                        <td>
                                            <span class="noselect valueHint">
                                                {getLinkedParamType(openApi, method, parameterName)}
                                            </span>
                                        </td>

                                        {#if method.parameters[parameterName].value && method.parameters[parameterName].valueHint}
                                            <td>
                                            <span data-container="body" data-toggle="tooltip" use:initTooltip data-placement="top"
                                                  title="{method.parameters[parameterName].valueHint}" data-original-title="">
                                                {method.parameters[parameterName].value}
                                            </span>
                                            </td>
                                        {:else if method.parameters[parameterName].value}
                                            <td>{method.parameters[parameterName].value}</td>
                                        {:else if method.parameters[parameterName].valueHint}
                                            <td>
                                                <span class="noselect valueHint">{method.parameters[parameterName].valueHint}</span>
                                            </td>
                                        {/if}
                                    </tr>
                                {/each}

                                </tbody>
                            </table>
                        </div>

                    </div>
                </div>
            {/if}


            {#each Object.keys(method.requestBody) as requestContentType}
                <div class="card card-primary usecase-request-body">
                    <h6 class="card-header bg-dark">Request Body <strong>[{requestContentType}]</strong></h6>
                    <div class="card-body">
                        <div class="card-text">
                            {#if method.requestBody[requestContentType].valueHint}
                                <div class="usecase-description">
                                    {method.requestBody[requestContentType].valueHint}
                                </div>
                            {/if}
                            <div class="code">
                                {method.requestBody[requestContentType].value}
                            </div>
                        </div>
                    </div>
                </div>
            {/each}

            {#each Object.keys(method.responseBody) as responseContentType}
                <div class="card card-primary usecase-response-body">
                    <h6 class="card-header bg-dark">Response Body <strong>[{responseContentType}]</strong></h6>
                    <div class="card-body">
                        <div class="card-text">
                            {#if method.responseBody[responseContentType].valueHint}
                                <div class="usecase-description">
                                    {method.responseBody[responseContentType].valueHint}
                                </div>
                            {/if}

                            <div class="code">
                                {method.responseBody[responseContentType].value}
                            </div>
                        </div>
                    </div>
                </div>
            {/each}

        </div>
    </div>
</div>

<style>

    .card {
        border: 0px;
        box-shadow: 4px 4px 10px 0px #0000007a;
    }

    .usecase-description {
        padding-top: 10px;
        padding-bottom: 10px;
    }

    .usecase-path {
        margin-bottom: 10px;
    }

</style>