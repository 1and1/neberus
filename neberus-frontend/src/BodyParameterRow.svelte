<script>
    import Schema from "./Schema.svelte";
    import AllowedValue from "./AllowedValue.svelte";

    export let schema;
    export let openApi;
    export let level;
    export let parent;
    export let contentType;
    export let parentTypeRefs;

    $: style = 'padding-left: ' + (level * 25) + 'px';

    $: currentSchema = initCurrentSchema(schema, openApi, parentTypeRefs);

    function initCurrentSchema(schema, openApi, parentTypes) {
        let current = schema.$ref ? findSchema(openApi, schema.$ref) : schema;

        if (schema.$ref) {
            parentTypes.push(schema.$ref);
        }
        return current;
    }

    const initTooltip = el => {
        initTooltipBox(el);
    }

    const initCollapse = el => {
        initCollapseToggle(el);
    }

</script>

{#if currentSchema.properties}
    <svelte:self schema={currentSchema.properties} openApi={openApi} level={level} parent={parent}
                 contentType={contentType} parentTypeRefs={[...parentTypeRefs]}/>
{:else if currentSchema.type === 'array'}
    {#each [(currentSchema.items.$ref ? findSchema(openApi, currentSchema.items.$ref) : currentSchema.items)] as itemSchema}
        <tr data-level="{level}"
            class="{parent} collapse parameter-highlight"
            id={(parent + "_" + 'item')}
            data-parameter-highlight-name={(parent + "_" + 'item')} onmouseover="highlightParameter(this, event)"
            onmouseout="deHighlightParameter(this, event)">
            <td class="{parent}-control clickable collapsed"
                data-bs-toggle="{itemSchema.extensions['x-java-type-expandable'] ? 'collapse' : ''}"
                use:initCollapse
                data-bs-target={("." + parent + "_" + 'item')}
                data-bs-toggle-parent={("#" + parent)}
                aria-expanded="false"
                {style}>
                <span class="noselect valueHint">[item]</span>
                {#if itemSchema.extensions && itemSchema.extensions['x-java-type-expandable']}
                    <span>
                        <i class="icon-toggle fas fa-angle-right"></i>
                    </span>
                {/if}
            </td>
            <td>
                <Schema openApi={openApi} schema={itemSchema} contentType={contentType} nested={true}/>
            </td>
            <td>
                {#if itemSchema.description}
                    {@html itemSchema.description}
                {/if}
            </td>
            <td>
                <AllowedValue param={itemSchema}/>
            </td>
        </tr>
        {#if itemSchema.extensions && itemSchema.extensions['x-java-type-expandable']}
            <svelte:self schema={itemSchema} openApi={openApi} level={level+1} parent={(parent + "_" + 'item')}
                         contentType={contentType} parentTypeRefs={[...parentTypeRefs]}/>
        {/if}
    {/each}
{:else if currentSchema.type === 'object' && currentSchema.additionalProperties}
    {#each [(currentSchema.additionalProperties.$ref ? findSchema(openApi, currentSchema.additionalProperties.$ref) : currentSchema.additionalProperties)] as valueSchema}
        <tr data-level="{level}" class="{parent} collapse parameter-highlight" id={(parent + "_" + "key")}
            data-bs-toggle-parent={("#" + parent)}
            data-parameter-highlight-name={(parent + "_" + "key")} onmouseover="highlightParameter(this, event)"
            onmouseout="deHighlightParameter(this, event)">
            <td {style}><span class="noselect valueHint">[key]</span></td>
            <td>String</td>
            <td></td>
            <td>
                <AllowedValue param={currentSchema} forMapKey=true />
            </td>
        </tr>
        <tr data-level="{level}"
            class="{parent} collapse parameter-highlight"
            id={(parent + "_" + "value")}
            data-parameter-highlight-name={(parent + "_" + "value")} onmouseover="highlightParameter(this, event)"
            onmouseout="deHighlightParameter(this, event)">
            <td class="{parent}-control clickable collapsed"
                data-bs-toggle="{valueSchema.extensions['x-java-type-expandable'] ? 'collapse' : ''}"
                use:initCollapse
                data-bs-target={("." + parent + "_" + "value")}
                data-bs-toggle-parent={("#" + parent)}
                aria-expanded="false"
                {style}>
                <span class="noselect valueHint">[value]</span>
                {#if valueSchema.extensions['x-java-type-expandable']}
                    <span>
                        <i class="icon-toggle fas fa-angle-right"></i>
                    </span>
                {/if}
            </td>
            <td>
                <Schema openApi={openApi} schema={valueSchema} contentType={contentType} nested={true}/>
            </td>
            <td>
                {#if valueSchema.description}
                    {@html valueSchema.description}
                {/if}
            </td>
            <td>
                <AllowedValue param={valueSchema}/>
            </td>
        </tr>
        <svelte:self schema={valueSchema} openApi={openApi} level={level+1} parent={(parent + "_" + "value")}
                     contentType={contentType} parentTypeRefs={[...parentTypeRefs]}/>
    {/each}
{:else if currentSchema.type === 'string' || currentSchema.type === 'integer' || currentSchema.type === 'number' || currentSchema.type === 'boolean' || currentSchema.type === 'null'}
    <!--noop-->
{:else}
    {#each Object.keys(currentSchema) as property}
        {#each [parent + "_" + property.replaceAll(/[^A-Za-z0-9]/g, '_')] as propertyReference}
            {#each [(currentSchema[property].$ref ? findSchema(openApi, currentSchema[property].$ref) : currentSchema[property])] as propertySchema}
                {#if property !== 'exampleSetFlag'}
                    <tr data-level="{level}"
                        class="{parent} collapse parameter-highlight"
                        id={(propertyReference)}
                        data-parameter-highlight-name={(propertyReference)} onmouseover="highlightParameter(this, event)"
                        onmouseout="deHighlightParameter(this, event)">
                        <td class="{parent}-control clickable collapsed"
                            data-bs-toggle="{propertySchema.extensions['x-java-type-expandable'] && !parentTypeRefs.includes(currentSchema[property].$ref) ? 'collapse' : ''}"
                            use:initCollapse
                            data-bs-target={("." + propertyReference)}
                            data-bs-toggle-parent={("#" + parent)}
                            aria-expanded="false"
                            {style}>
                            <span class="optionalIndicator">
                                <span data-bs-container="body" data-bs-toggle="tooltip" use:initTooltip data-bs-placement="top"
                                      title="{(propertySchema.required || (propertySchema.extensions && propertySchema.extensions['x-java-type-required'])) ? 'Mandatory' : 'Optional'}">
                                    <i class="{(propertySchema.required || (propertySchema.extensions && propertySchema.extensions['x-java-type-required'])) ? 'fas' : 'far'} fa-circle"></i>
                                </span>
                            </span>

                            {#if propertySchema.deprecated}
                                <span class="deprecated" data-bs-container="body" data-bs-toggle="tooltip" use:initTooltip data-bs-placement="top"
                                      title="{propertySchema.extensions && propertySchema.extensions['x-deprecated-description'] ? propertySchema.extensions['x-deprecated-description'] : ''}">
                                    {property}
                                </span>
                            {:else}
                                {property}
                            {/if}


                            {#if propertySchema.extensions && propertySchema.extensions['x-java-type-expandable']}
                                {#if !parentTypeRefs.includes(currentSchema[property].$ref)}
                                    <span>
                                        <i class="icon-toggle fas fa-angle-right"></i>
                                    </span>
                                {:else}
                                    <span class="valueHint">[recursive]</span>
                                {/if}
                            {/if}
                        </td>
                        <td>
                            <Schema openApi={openApi} schema={propertySchema} contentType={contentType} nested={true}/>
                        </td>
                        <td>{@html propertySchema.description}</td>
                        <td>
                            <AllowedValue param={propertySchema}/>
                        </td>
                    </tr>
                    {#if propertySchema.extensions && propertySchema.extensions['x-java-type-expandable']}
                        {#if !parentTypeRefs.includes(currentSchema[property].$ref)}
                            <svelte:self schema={currentSchema[property]} openApi={openApi} level={level+1} parent={(propertyReference)}
                                         contentType={contentType} parentTypeRefs={[...parentTypeRefs]}/>
                        {/if}
                    {/if}
                {/if}

            {/each}
        {/each}
    {/each}
{/if}

<style>

    .deprecated {
        text-decoration: line-through;
    }

</style>
