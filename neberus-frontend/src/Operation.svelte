<script>
    import OperationRequest from "./OperationRequest.svelte";
    import OperationResponse from "./OperationResponse.svelte";

    export let operation;
    export let path;
    export let method;
    export let openApi;

    function expandPath(path, method, operation) {
        let expanded = path.replaceAll("/", "/<span class='word-wrap'></span>");

        operation.parameters.forEach(param => {
            if (param.in === 'path') {
                let paramReference = method.toUpperCase() + '-' + operation.summary.replaceAll(/[^A-Za-z0-9]/g, '_') + '_param_' + param.name.replaceAll('.', '_');
                expanded = expanded.replaceAll(
                    '{' + param.name + '}',
                    '<span class="parameter-highlight" onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)"' +
                    ' data-parameter-highlight-name="' + paramReference + '">{' + param.name + '}</span>'
                );
            }
        });

        return expanded;
    }

    function getRelatedUsecasesBody(openApi, usecases) {
        let body = '<div class="code">';

        usecases.forEach(usecaseId => {
            let name = openApi.extensions['x-usecases'].usecases[usecaseId].name;
            body += '<a href="?usecases=' + usecaseId + '">' + name + '</a>';
            body += '<br>';
        });

        body += '</div>';

        return body;
    }

    function getDeprecatedDescription(openApi, operation) {
        let description = operation.extensions['x-deprecated-description'];
        let linkedMethods = operation.extensions['x-deprecated-linked-methods'];

        if (linkedMethods) {
            linkedMethods.forEach(linkedMethod => {

                let replacement = '<a href="?resource=' + linkedMethod.resource
                    + '&operation=' + linkedMethod.httpMethod.toUpperCase() + '-' + linkedMethod.label.replaceAll(/[^A-Za-z0-9]/g, '_') + '">['
                    + linkedMethod.resource + ':' + linkedMethod.label
                    + ']</a>';

                description = description.replace(/\{\@link[^}]+\}/, replacement);
            });

        }

        return description;
    }

    const initPopover = el => {
        initPopoverButton(el);
    }

</script>

<div class="card card-primary operation {operation.deprecated?'deprecated':''}">
    <div class="headingContainer">
        <h2 id="{method.toUpperCase()}-{operation.summary.replaceAll(/[^A-Za-z0-9]/g, '_')}" class="methodHeading">
            {method.toUpperCase()} - {operation.summary}
        </h2>

        {#if operation.extensions && operation.extensions['x-related-usecases']}
            <button class="btn btn-primary relatedUsecasesToggle" data-container="body" data-toggle="popover" data-placement="left" use:initPopover
                    data-html="true" data-content={getRelatedUsecasesBody(openApi, operation.extensions['x-related-usecases'])}
                    data-original-title="" title="">Related Usecases
            </button>
        {/if}

    </div>

    {#if operation.deprecated && operation.extensions && operation.extensions['x-deprecated-description']}
        <div class="card bg-warning deprecated-description">
            <div class="card-header text-dark">Deprecated</div>
            <div class="card-body">
                {@html getDeprecatedDescription(openApi, operation)}
            </div>
        </div>
    {/if}

    <div class="card card-primary pathContainer">
        <div class="card-body">
            <div class="code">{@html expandPath(path, method, operation)}</div>
        </div>
    </div>

    {#if operation.description}
        <div class="methodDescription">{@html operation.description}</div>
    {/if}

    <OperationRequest operation={operation} openApi={openApi} method={method} path={path}/>
    <OperationResponse operation={operation} openApi={openApi} method={method}/>
</div>

<style>

    .headingContainer {
        position: relative;
    }

    .operation {
        min-height: 20px;
        padding: 19px;
        margin-bottom: 20px;
    }

    .deprecated .operation:after {
        content: "";
        display: block;
        position: absolute;
        top: 0;
        left: 0;
        height: 100%;
        width: 100%;
        z-index: 10;
        pointer-events: none;
    }

    .methodDescription {
        margin: 20px;
        margin-left: 10px;
    }

    .pathContainer .card-body {
        padding: 0.1rem;
    }

    .pathContainer .code {
        padding: 5px 10px;
    }

</style>

