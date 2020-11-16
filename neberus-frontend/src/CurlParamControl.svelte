<script>
    import {createEventDispatcher} from "svelte";

    export let params;
    export let name;
    export let type;
    export let baseReference;

    const dispatch = createEventDispatcher();

    function dispatchToggleParam(event) {
        dispatch('toggleParam', this);
    }

    function dispatchUpdateParam(event) {
        dispatch('updateParam', this);
    }

    function dispatchUpdateEnumParam(event) {
        this.selectedEnumValue = selectedEnumValue;
        dispatch('updateEnumParam', this);
    }

    function reference(type, name) {
        return baseReference + type + '_' + name.replaceAll('.', '_');
    }

    function getPlaceholder(param) {
        if (param.extensions && param.extensions['x-allowed-values']) {
            //FIXME return first valueHint?
        }
        return '';
    }

    let selectedEnumValue;

</script>


{#if params.length > 0}
    {#if name}
        <tr>
            <th colspan="4">{name}</th>
        </tr>
    {/if}
    {#each params as param}
        <tr id="{reference(type, param.name)}_controls" class="controls-row parameter-highlight {(param.required || !param.deprecated) ? '' : 'deactivated'}"
            data-parameter-highlight-name="{baseReference.replaceAll('_curl_', '')}_param_{param.name.replaceAll('.', '_')}"
            onmouseover="highlightParameter(this, event)" onmouseout="deHighlightParameter(this, event)">
            <th></th>
            <td>
                <div class="form-check">
                    <input class="form-check-input" data-type="{type}" data-name="{param.name}"
                           type="checkbox" on:change={dispatchToggleParam} checked={param.required || !param.deprecated}
                           disabled={param.required}/>
                </div>
            </td>
            <td>
                <label class="{param.deprecated ? 'curl-param-deprecated' : ''}">{param.name}</label>
            </td>
            <td>
                {#if param.schema.enum}
                    <select class="custom-select custom-select-sm mb-3" bind:value={selectedEnumValue} on:change={dispatchUpdateEnumParam}
                            data-type="{type}_value" data-name="{param.name}">
                        {#each param.schema.enum as enumValue}
                            <option value="{enumValue}">{enumValue}</option>
                        {/each}
                    </select>
                {:else}
                    <input data-type="{type}_value" data-name="{param.name}" type="text"
                           placeholder="{getPlaceholder(param)}"
                           on:keyup={dispatchUpdateParam}/>
                {/if}
            </td>
        </tr>
    {/each}
{/if}


<style>

    label {
        margin-bottom: 0px;
    }

    .form-check {
        margin-top: -0.75rem;
        padding-left: 30px;
        padding-right: 10px;
    }

    .controls-row input[type="text"] {
        width: 100%;
    }

    .table-curl tr th {
        width: 150px;
    }

    .table-curl tr td:first-of-type {
        max-width: 21px;
    }

    .table-curl tr td:nth-of-type(2) {
        min-width: 150px;
        padding-right: 10px;
    }

</style>