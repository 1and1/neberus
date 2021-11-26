<script>
    export let schema;
    export let openApi;
    export let contentType;
    export let nested;

    $: actualSchema = schema.$ref ? findSchema(openApi, schema.$ref) : schema;
    $: title = typeof (schema) == "string" ? schema : actualSchema.extensions['x-java-type'];

    function getFullSchemaHtml(openApi, schema, contentType, nested) {
        let fullSchemaHtml = {};

        if (schema && openApi) {

            let actualSchema = schema.$ref ? findSchema(openApi, schema.$ref) : schema;
            let title = typeof (schema) == "string" ? schema : actualSchema.extensions['x-java-type'];

            let fullSchema = resolveRefs(openApi, schema);

            if (!contentType || contentType.includes('json')) {
                fullSchemaHtml = toJsonPretty(fullSchema);
            } else if (contentType.includes('xml')) {
                fullSchemaHtml = nested ? toXmlPretty(fullSchema, 0) : toFullXmlPretty(fullSchema, title);
            } else if (contentType.includes('x-www-form-urlencoded')) {
                fullSchemaHtml = toFormUrlencodedPretty(fullSchema, '');
            }

            fullSchemaHtml = '<div class="code">' + fullSchemaHtml + '</div>';

            return fullSchemaHtml;
        }
    }

    const initPopover = el => {
        initPopoverButton(el);
    }

</script>

{#if actualSchema.extensions['x-java-type-expandable'] && !(nested && contentType.includes('form'))}
    {#each [getFullSchemaHtml(openApi, schema, contentType, nested)] as fullSchemaHtml}
        {#if fullSchemaHtml}
            <button class="btn btn-primary" data-bs-container="body" data-bs-toggle="popover" data-bs-placement="right" use:initPopover
                    data-bs-html="true" data-bs-content={fullSchemaHtml}>{title}
            </button>
        {/if}
    {/each}
{:else if actualSchema.enum}
    String
{:else}
    {title}
{/if}