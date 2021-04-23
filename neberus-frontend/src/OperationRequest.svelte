<script>
    import BodyParameters from "./BodyParameters.svelte";
    import Curl from "./Curl.svelte";
    import AllowedValue from "./AllowedValue.svelte";

    export let operation;
    export let method;
    export let openApi;
    export let path;

    const initTooltip = el => {
        initTooltipBox(el);
    }
</script>

{#if (operation.parameters && operation.parameters.length > 0) || operation.requestBody || (operation.extensions && operation.extensions['x-curl-enabled'])}
    <div class="card card-primary table-responsive">
        <h5 class="card-header bg-secondary">Request</h5>
        <div class="card-body">

            <Curl operation={operation} openApi={openApi} method={method} path={path}/>

            {#if operation.parameters && operation.parameters.length > 0}
                <div class="card card-primary card-nested card-table">
                    <h6 class="card-header bg-dark">Parameters</h6>
                    <div class="card-body">
                        <div class="card-text">
                            <table class="table table-striped table-dark table-small-head parameters">
                                <thead>
                                <tr>
                                    <th scope="col">Name</th>
                                    <th scope="col">Type</th>
                                    <th scope="col">Description</th>
                                    <th scope="col">Allowed Values</th>
                                </tr>
                                </thead>
                                <tbody>
                                {#each operation.parameters as param}
                                    <tr data-parameter-highlight-name="{method.toUpperCase()}-{operation.summary.replaceAll(/[^A-Za-z0-9]/g, '_')}_param_{param.name.replaceAll('.', '_')}"
                                        class="parameter-highlight" onmouseover="highlightParameter(this, event)"
                                        onmouseout="deHighlightParameter(this, event)">
                                        <td>
                                        <span class="optionalIndicator">
                                            <span data-container="body" data-toggle="tooltip" use:initTooltip data-placement="top"
                                                  title="{param.required ? 'Mandatory' : 'Optional'}">
                                                <i class="{param.required ? 'fas' : 'far'} fa-circle"></i>
                                            </span>
                                        </span>
                                            {#if param.deprecated}
                                                <span class="deprecated" data-container="body" data-toggle="tooltip" use:initTooltip data-placement="top"
                                                      title="{param.extensions && param.extensions['x-deprecated-description'] ? param.extensions['x-deprecated-description'] : ''}">
                                                    {param.name}
                                                </span>
                                            {:else}
                                                {param.name}
                                            {/if}
                                        </td>
                                        <td class="valueHint noselect">{param.in}</td>
                                        <td>{@html param.description}</td>
                                        <td>
                                            <AllowedValue param={param}/>
                                        </td>
                                    </tr>
                                {/each}
                                </tbody>

                            </table>
                        </div>
                    </div>
                </div>
            {/if}

            {#if operation.requestBody}
                {#each Object.keys(operation.requestBody.content) as contentType}
                    <div class="card card-primary card-nested card-table request {operation.requestBody.content[contentType].examples ? 'parameters-with-examples' : ''}">
                        <div class="card-header bg-dark">
                            <table>
                                <tbody>
                                <tr>
                                    <td class="content-type">
                                        Body <strong>[{contentType}]</strong>
                                    </td>
                                    <td>
                                        {#if operation.requestBody.content[contentType].extensions && operation.requestBody.content[contentType].extensions['x-description']}
                                            {@html operation.requestBody.content[contentType].extensions['x-description']}
                                        {/if}
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="card-body">
                            <div class="card-text">
                                <BodyParameters openApi={openApi} schema={operation.requestBody.content[contentType].schema}
                                                operationReference={method.toUpperCase()}-{operation.summary.replaceAll(/[^A-Za-z0-9]/g, '_')}
                                                contentType={contentType}/>
                            </div>
                        </div>
                        {#if operation.requestBody.content[contentType].examples}
                            <div class="card-footer">
                                {#each Object.keys(operation.requestBody.content[contentType].examples) as example}
                                    <div class="body-example">
                                        <div>
                                            {example}
                                        </div>
                                        <div class="code">
                                            {@html operation.requestBody.content[contentType].examples[example].value}
                                        </div>
                                    </div>
                                {/each}
                            </div>
                        {/if}
                    </div>
                {/each}
            {/if}


        </div>

    </div>
{/if}

<style>

    .optionalIndicator {
        font-size: 8px;
        padding-right: 10px;
        white-space: nowrap;
        vertical-align: middle;
    }

    .valueHint {
        opacity: 0.6;
    }

    .noselect {
        -webkit-touch-callout: none; /* iOS Safari */
        -webkit-user-select: none; /* Safari */
        -khtml-user-select: none; /* Konqueror HTML */
        -moz-user-select: none; /* Firefox */
        -ms-user-select: none; /* Internet Explorer/Edge */
        user-select: none; /* Non-prefixed version, currently supported by Chrome and Opera */
    }

    .card {
        border: 0px;
        box-shadow: 4px 4px 10px 0px #0000007a;
    }

    .deprecated {
        text-decoration: line-through;
    }

    .request .content-type {
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