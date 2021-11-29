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
                let paramReference = operation.operationId + '_param_' + param.extensions['x-name-escaped'];
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

    function getDescription(openApi, operation) {
        let description = operation.description;
        let linkedMethods = operation.extensions ? operation.extensions['x-linked-methods'] : undefined;

        if (linkedMethods) {
            linkedMethods.forEach(linkedMethod => {

                let replacement = '<a href="?resource=' + linkedMethod.resource
                    + '&operation=' + linkedMethod.operationId + '">['
                    + linkedMethod.resource + ':' + linkedMethod.label
                    + ']</a>';

                description = description.replace(/\{\@link[^}]+\}/, replacement);
            });

        }

        return description;
    }

    function getDeprecatedDescription(openApi, operation) {
        let description = operation.extensions['x-deprecated-description'];
        let linkedMethods = operation.extensions['x-linked-methods'];

        if (linkedMethods) {
            linkedMethods.forEach(linkedMethod => {

                let replacement = '<a href="?resource=' + linkedMethod.resource
                    + '&operation=' + linkedMethod.operationId + '">['
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
        <h2 id="{operation.operationId}" class="methodHeading">
            {method.toUpperCase()} - {operation.summary}
        </h2>

        {#if operation.extensions && operation.extensions['x-related-usecases']}
            <button class="btn btn-primary relatedUsecasesToggle" data-bs-container="body" data-bs-toggle="popover" data-bs-placement="left" use:initPopover
                    data-bs-html="true" data-bs-content={getRelatedUsecasesBody(openApi, operation.extensions['x-related-usecases'])}
                    data-bs-original-title="" title="">Related Usecases
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


    <div id="{operation.operationId}-details">
        {#if operation.description}
            <div class="methodDescription">{@html getDescription(openApi, operation)}</div>
        {/if}

        <OperationRequest operation={operation} openApi={openApi} method={method} path={path}/>
        <OperationResponse operation={operation} openApi={openApi} method={method}/>
    </div>
</div>

<style>

    .headingContainer {
        position: relative;
    }

    .operation {
        min-height: 20px;
        padding: 19px;
        margin-bottom: 10px;
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

