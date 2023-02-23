<script>
    import BodyParameterRow from "./BodyParameterRow.svelte";
    import Schema from "./Schema.svelte";

    export let schema;
    export let openApi;
    export let operationReference;
    export let contentType;

    $: currentSchema = (schema.$ref ? findSchema(openApi, schema.$ref) : schema);
    $: reference = operationReference + "_" + contentType.replaceAll(/[^A-Za-z0-9]/g, '_') + "_" + currentSchema.type;

    const initCollapse = el => {
        initCollapseToggle(el);
    }

</script>

{#if schema}

    <table class="table table-dark table-hover table-small-head parameters">
        <thead>
        <tr>
            <th>Name</th>
            <th>Entity</th>
            <th>Description</th>
            <th>Allowed Values</th>
        </tr>
        </thead>
        <tbody>
        <tr data-level="0"
            id={reference} class="parameter-highlight"
            data-parameter-highlight-name="{reference}" onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">
            <td class="clickable collapsed"
                data-bs-toggle="{currentSchema.extensions['x-java-type-expandable'] ? 'collapse' : ''}"
                data-bs-target=".{reference}"
                aria-expanded="false"
                use:initCollapse>
                {currentSchema.extensions['x-java-type'] ?? currentSchema.type}
                {#if currentSchema.extensions['x-java-type-expandable']}
                    <span>
                        <i class="icon-toggle fas fa-angle-right"></i>
                    </span>
                {/if}
            </td>
            <td>
                <Schema openApi={openApi} schema={schema} contentType={contentType} nested={false}/>
            </td>
            <td>
                {#if currentSchema.description}
                    {@html currentSchema.description}
                {/if}
            </td>
            <td></td>
        </tr>

        {#if currentSchema.extensions['x-java-type-expandable']}
            <BodyParameterRow openApi={openApi} schema={schema} level={1} parent={reference} contentType={contentType}
                              parentTypeRefs={[schema.$ref]}/>
        {/if}
        </tbody>
    </table>

{/if}

